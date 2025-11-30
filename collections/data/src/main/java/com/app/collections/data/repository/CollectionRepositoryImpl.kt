package com.app.collections.data.repository

import android.util.Log
import com.app.collections.data.remote.CollectionRemoteDataSource
import com.app.core.room.dao.CollectionDao
import com.app.collections.data.mappers.toDomain
import com.app.collections.data.mappers.toEntity
import com.app.collections.domain.models.Collection
import com.app.collections.domain.models.CollectionItem
import com.app.collections.domain.models.ContentMetadata
import com.app.collections.domain.models.DefaultCollectionType
import com.app.collections.domain.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

class CollectionRepositoryImpl(
    private val collectionDao: CollectionDao,
    private val remoteDataSource: CollectionRemoteDataSource
) : CollectionRepository {

    companion object {
        private const val TAG = "CollectionRepoImpl"
    }

    override suspend fun createCollection(
        userId: String,
        name: String,
        description: String?,
        isPublic: Boolean
    ): Result<Collection> {
        return try {
            val currentTime = System.currentTimeMillis()
            val localCollection = Collection(
                id = UUID.randomUUID(),
                userId = userId,
                name = name,
                description = description,
                isDefault = false,
                isPublic = isPublic,
                createdAt = currentTime,
                updatedAt = currentTime
            )

            // Try to create on server first
            val remoteResult = remoteDataSource.createCollection(localCollection)
            
            if (remoteResult.isSuccess) {
                val serverCollection = remoteResult.getOrThrow()
                // Save server-created collection to local DB
                collectionDao.insertCollection(serverCollection.toEntity())
                Result.success(serverCollection)
            } else {
                // Fallback to local-only creation if server fails
                Log.w(TAG, "Failed to create collection on server, saving locally", remoteResult.exceptionOrNull())
                collectionDao.insertCollection(localCollection.toEntity())
                Result.success(localCollection)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating collection", e)
            Result.failure(e)
        }
    }

    override suspend fun getCollection(collectionId: UUID): Flow<Collection?> {
        return collectionDao.getCollectionWithItems(collectionId)
            .map { it?.toDomain() }
    }

    override suspend fun getCollections(): Flow<List<Collection>> {
        // Sync from server in the background
        syncCollectionsFromServer()
        
        // Return local data as Flow
        return collectionDao.getAllCollections()
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getDefaultCollection(
        type: DefaultCollectionType
    ): Flow<Collection?> {
        return collectionDao.getCollectionByName(type.displayName)
            .map { it?.toDomain() }
    }

    override suspend fun deleteCollection(collectionId: UUID): Result<Unit> {
        return try {
            // Try to delete from server first
            val remoteResult = remoteDataSource.deleteCollection(collectionId)
            
            if (remoteResult.isSuccess) {
                // Delete from local DB
                collectionDao.deleteCollection(collectionId)
                Result.success(Unit)
            } else {
                // Fallback to local-only deletion if server fails
                Log.w(TAG, "Failed to delete collection on server, deleting locally", remoteResult.exceptionOrNull())
                collectionDao.deleteCollection(collectionId)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting collection", e)
            Result.failure(e)
        }
    }

    override suspend fun updateCollection(
        collectionId: UUID,
        name: String?,
        description: String?,
        isPublic: Boolean?
    ): Result<Unit> {
        return try {
            val existingCollection = collectionDao.getCollectionById(collectionId).first()
                ?: return Result.failure(Exception("Collection not found"))

            // Try to update on server first
            val remoteResult = remoteDataSource.updateCollection(
                collectionId,
                name,
                description,
                isPublic
            )
            
            if (remoteResult.isSuccess) {
                val serverCollection = remoteResult.getOrThrow()
                // Update local DB with server response
                collectionDao.updateCollection(serverCollection.toEntity())
                Result.success(Unit)
            } else {
                // Fallback to local-only update if server fails
                Log.w(TAG, "Failed to update collection on server, updating locally", remoteResult.exceptionOrNull())
                val updatedCollection = existingCollection.copy(
                    name = name ?: existingCollection.name,
                    description = description ?: existingCollection.description,
                    is_public = isPublic ?: existingCollection.is_public,
                    updated_at = System.currentTimeMillis()
                )
                collectionDao.updateCollection(updatedCollection)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating collection", e)
            Result.failure(e)
        }
    }

    override suspend fun addToCollection(
        collectionId: UUID,
        tmdbId: Int,
        mediaType: String,
        contentMetadata: ContentMetadata,
        notes: String?
    ): Result<CollectionItem> {
        return try {
            // Save or update content metadata locally
            collectionDao.insertContent(contentMetadata.toEntity())

            // Try to add to server first
            val remoteResult = remoteDataSource.addItemToCollection(
                collectionId,
                contentMetadata,
                notes
            )
            
            if (remoteResult.isSuccess) {
                val serverItem = remoteResult.getOrThrow()
                // Save server-created item to local DB
                collectionDao.insertItem(serverItem.toEntity())
                Result.success(serverItem)
            } else {
                // Fallback to local-only creation if server fails
                Log.w(TAG, "Failed to add item on server, saving locally", remoteResult.exceptionOrNull())
                val localItem = CollectionItem(
                    id = UUID.randomUUID(),
                    collectionId = collectionId,
                    contentId = contentMetadata.id,
                    tmdbId = tmdbId,
                    mediaType = mediaType,
                    addedAt = System.currentTimeMillis(),
                    notes = notes,
                    content = contentMetadata
                )
                collectionDao.insertItem(localItem.toEntity())
                Result.success(localItem)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to collection", e)
            Result.failure(e)
        }
    }

    override suspend fun removeFromCollection(
        collectionId: UUID,
        tmdbId: Int,
        mediaType: String
    ): Result<Unit> {
        return try {
            // Get the item ID from local DB first
            val items = collectionDao.getCollectionWithItems(collectionId).first()
            val itemToRemove = items?.items?.find { 
                it.item.tmdb_id == tmdbId && it.item.media_type == mediaType 
            }
            
            if (itemToRemove != null) {
                // Try to remove from server first
                val remoteResult = remoteDataSource.removeItemFromCollection(
                    collectionId,
                    itemToRemove.item.id
                )
                
                if (remoteResult.isSuccess || remoteResult.exceptionOrNull()?.message?.contains("404") == true) {
                    // Remove from local DB (also remove if item not found on server)
                    collectionDao.deleteCollectionItem(collectionId, tmdbId, mediaType)
                    Result.success(Unit)
                } else {
                    // Fallback to local-only deletion if server fails
                    Log.w(TAG, "Failed to remove item on server, removing locally", remoteResult.exceptionOrNull())
                    collectionDao.deleteCollectionItem(collectionId, tmdbId, mediaType)
                    Result.success(Unit)
                }
            } else {
                // Item not found, delete from local anyway
                collectionDao.deleteCollectionItem(collectionId, tmdbId, mediaType)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing from collection", e)
            Result.failure(e)
        }
    }

    override suspend fun isInCollection(
        collectionId: UUID,
        tmdbId: Int,
        mediaType: String
    ): Flow<Boolean> {
        return collectionDao.isInCollection(collectionId, tmdbId, mediaType)
    }

    override suspend fun addToWatchlist(
        userId: String,
        tmdbId: Int,
        mediaType: String,
        contentMetadata: ContentMetadata
    ): Result<CollectionItem> {
        return try {
            val watchlistCollection = getOrCreateDefaultCollection(userId, DefaultCollectionType.WATCHLIST)
            addToCollection(watchlistCollection.id, tmdbId, mediaType, contentMetadata)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addToAlreadyWatched(
        userId: String,
        tmdbId: Int,
        mediaType: String,
        contentMetadata: ContentMetadata
    ): Result<CollectionItem> {
        return try {
            val alreadyWatchedCollection = getOrCreateDefaultCollection(userId = userId,DefaultCollectionType.ALREADY_WATCHED)
            addToCollection(alreadyWatchedCollection.id, tmdbId, mediaType, contentMetadata)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFromWatchlist(
        userId: String,
        tmdbId: Int,
        mediaType: String
    ): Result<Unit> {
        return try {
            val watchlistCollection = getOrCreateDefaultCollection(userId,DefaultCollectionType.WATCHLIST)
            removeFromCollection(watchlistCollection.id, tmdbId, mediaType)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFromAlreadyWatched(
        userId: String,
        tmdbId: Int,
        mediaType: String
    ): Result<Unit> {
        return try {
            val alreadyWatchedCollection = getOrCreateDefaultCollection(userId,DefaultCollectionType.ALREADY_WATCHED)
            removeFromCollection(alreadyWatchedCollection.id, tmdbId, mediaType)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isInWatchlist(
        tmdbId: Int,
        mediaType: String
    ): Flow<Boolean> {
        return collectionDao.isInDefaultCollection(
            DefaultCollectionType.WATCHLIST.displayName,
            tmdbId,
            mediaType
        )
    }

    override suspend fun isAlreadyWatched(
        tmdbId: Int,
        mediaType: String
    ): Flow<Boolean> {
        return collectionDao.isInDefaultCollection(
            DefaultCollectionType.ALREADY_WATCHED.displayName,
            tmdbId,
            mediaType
        )
    }

    override suspend fun saveContentMetadata(contentMetadata: ContentMetadata): Result<Unit> {
        return try {
            collectionDao.insertContent(contentMetadata.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getContentMetadata(tmdbId: Int, mediaType: String): ContentMetadata? {
        return collectionDao.getContentMetadata(tmdbId, mediaType)?.toDomain()
    }

    override suspend fun initializeDefaultCollections(userId: String): Result<Unit> {
        return try {
            // Try to create default collections on server first
            val remoteResult = remoteDataSource.createDefaultCollections()
            
            if (remoteResult.isSuccess) {
                val serverCollections = remoteResult.getOrThrow()
                // Save server-created collections to local DB
                collectionDao.insertCollections(
                    serverCollections.map { it.toEntity() }
                )
                Result.success(Unit)
            } else {
                // Fallback to local-only creation if server fails
                Log.w(TAG, "Failed to create default collections on server, creating locally", remoteResult.exceptionOrNull())
                val currentTime = System.currentTimeMillis()

                val watchlistCollection = Collection(
                    id = UUID.randomUUID(),
                    userId = userId,
                    name = DefaultCollectionType.WATCHLIST.displayName,
                    description = "Movies and TV shows you want to watch",
                    isDefault = true,
                    isPublic = false,
                    createdAt = currentTime,
                    updatedAt = currentTime
                )

                val alreadyWatchedCollection = Collection(
                    id = UUID.randomUUID(),
                    userId = userId,
                    name = DefaultCollectionType.ALREADY_WATCHED.displayName,
                    description = "Movies and TV shows you have watched",
                    isDefault = true,
                    isPublic = false,
                    createdAt = currentTime,
                    updatedAt = currentTime
                )

                collectionDao.insertCollections(
                    listOf(
                        watchlistCollection.toEntity(),
                        alreadyWatchedCollection.toEntity()
                    )
                )

                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing default collections", e)
            Result.failure(e)
        }
    }

    private suspend fun getOrCreateDefaultCollection(
        userId: String,
        type: DefaultCollectionType
    ): Collection {
        // Try to get existing default collection using the Flow method
        val existingCollection = collectionDao.getCollectionByName(type.displayName).first()

        return if (existingCollection != null) {
            existingCollection.toDomain()
        } else {
            val currentTime = System.currentTimeMillis()
            val collection = Collection(
                id = UUID.randomUUID(),
                userId = userId, // You'll need to handle user ID properly
                name = type.displayName,
                description = when (type) {
                    DefaultCollectionType.WATCHLIST -> "Movies and TV shows you want to watch"
                    DefaultCollectionType.ALREADY_WATCHED -> "Movies and TV shows you have watched"
                },
                isDefault = true,
                isPublic = false,
                createdAt = currentTime,
                updatedAt = currentTime
            )

            collectionDao.insertCollection(collection.toEntity())
            collection
        }
    }

    /**
     * Sync collections from server to local database
     */
    private suspend fun syncCollectionsFromServer() {
        try {
            val remoteResult = remoteDataSource.getCollections()
            if (remoteResult.isSuccess) {
                val serverCollections = remoteResult.getOrThrow()
                // Update local database with server data
                serverCollections.forEach { collection ->
                    // Insert or update collection
                    collectionDao.insertCollection(collection.toEntity())
                    
                    // Insert or update items
                    collection.items.forEach { item ->
                        collectionDao.insertContent(item.content.toEntity())
                        collectionDao.insertItem(item.toEntity())
                    }
                }
                Log.d(TAG, "Successfully synced ${serverCollections.size} collections from server")
            } else {
                Log.w(TAG, "Failed to sync collections from server", remoteResult.exceptionOrNull())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing collections from server", e)
        }
    }
}
