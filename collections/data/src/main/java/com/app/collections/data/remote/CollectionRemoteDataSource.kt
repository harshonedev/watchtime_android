package com.app.collections.data.remote

import android.util.Log
import com.app.collections.data.mappers.toDomain
import com.app.collections.data.mappers.toAddItemRequest
import com.app.collections.data.mappers.toCreateRequest
import com.app.collections.data.mappers.toUpdateRequest
import com.app.collections.domain.models.Collection
import com.app.collections.domain.models.CollectionItem
import com.app.collections.domain.models.ContentMetadata
import com.app.core.network.api.CollectionsApiService
import com.app.core.network.model.CreateCollectionRequest
import com.app.core.network.model.UpdateCollectionRequest
import java.util.UUID

class CollectionRemoteDataSource(
    private val apiService: CollectionsApiService,
    private val getAuthToken: () -> String?
) {
    companion object {
        private const val TAG = "CollectionRemoteDS"
    }

    /**
     * Get all collections from server
     */
    suspend fun getCollections(): Result<List<Collection>> {
        return try {
            val token = getAuthToken() ?: return Result.failure(Exception("Not authenticated"))
            val response = apiService.getCollections("Bearer $token")
            
            val data = response.data
            if (response.success && data != null) {
                val collections = data.map { it.toDomain() }
                Result.success(collections)
            } else {
                Result.failure(Exception(response.error ?: "Failed to fetch collections"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching collections", e)
            Result.failure(e)
        }
    }

    /**
     * Get a specific collection by ID from server
     */
    suspend fun getCollectionById(collectionId: UUID): Result<Collection> {
        return try {
            val token = getAuthToken() ?: return Result.failure(Exception("Not authenticated"))
            val response = apiService.getCollectionById(collectionId.toString(), "Bearer $token")
            
            val data = response.data
            if (response.success && data != null) {
                Result.success(data.toDomain())
            } else {
                Result.failure(Exception(response.error ?: "Failed to fetch collection"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching collection by id", e)
            Result.failure(e)
        }
    }

    /**
     * Create a new collection on server
     */
    suspend fun createCollection(collection: Collection): Result<Collection> {
        return try {
            val token = getAuthToken() ?: return Result.failure(Exception("Not authenticated"))
            val request = CreateCollectionRequest(
                name = collection.name,
                description = collection.description,
                isPublic = collection.isPublic
            )
            val response = apiService.createCollection("Bearer $token", request)
            
            val data = response.data
            if (response.success && data != null) {
                Result.success(data.toDomain())
            } else {
                Result.failure(Exception(response.error ?: "Failed to create collection"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating collection", e)
            Result.failure(e)
        }
    }

    /**
     * Update a collection on server
     */
    suspend fun updateCollection(
        collectionId: UUID,
        name: String?,
        description: String?,
        isPublic: Boolean?
    ): Result<Collection> {
        return try {
            val token = getAuthToken() ?: return Result.failure(Exception("Not authenticated"))
            val request = UpdateCollectionRequest(
                name = name,
                description = description,
                isPublic = isPublic
            )
            val response = apiService.updateCollection(
                collectionId.toString(),
                "Bearer $token",
                request
            )
            
            val data = response.data
            if (response.success && data != null) {
                Result.success(data.toDomain())
            } else {
                Result.failure(Exception(response.error ?: "Failed to update collection"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating collection", e)
            Result.failure(e)
        }
    }

    /**
     * Delete a collection on server
     */
    suspend fun deleteCollection(collectionId: UUID): Result<Unit> {
        return try {
            val token = getAuthToken() ?: return Result.failure(Exception("Not authenticated"))
            val response = apiService.deleteCollection(collectionId.toString(), "Bearer $token")
            
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.error ?: "Failed to delete collection"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting collection", e)
            Result.failure(e)
        }
    }

    /**
     * Add an item to a collection on server
     */
    suspend fun addItemToCollection(
        collectionId: UUID,
        contentMetadata: ContentMetadata,
        notes: String?
    ): Result<CollectionItem> {
        return try {
            val token = getAuthToken() ?: return Result.failure(Exception("Not authenticated"))
            val request = contentMetadata.toAddItemRequest(notes)
            val response = apiService.addItemToCollection(
                collectionId.toString(),
                "Bearer $token",
                request
            )
            
            val data = response.data
            if (response.success && data != null) {
                // We need to construct the full CollectionItem with metadata
                val item = CollectionItem(
                    id = UUID.fromString(data.id),
                    collectionId = UUID.fromString(data.collectionId),
                    contentId = UUID.fromString(data.contentId),
                    tmdbId = data.tmdbId,
                    mediaType = data.mediaType,
                    addedAt = com.app.collections.data.mappers.parseIsoDate(data.addedAt),
                    notes = data.notes,
                    content = contentMetadata
                )
                Result.success(item)
            } else {
                Result.failure(Exception(response.error ?: "Failed to add item to collection"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding item to collection", e)
            Result.failure(e)
        }
    }

    /**
     * Remove an item from a collection on server
     */
    suspend fun removeItemFromCollection(collectionId: UUID, itemId: UUID): Result<Unit> {
        return try {
            val token = getAuthToken() ?: return Result.failure(Exception("Not authenticated"))
            val response = apiService.removeItemFromCollection(
                collectionId.toString(),
                itemId.toString(),
                "Bearer $token"
            )
            
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.error ?: "Failed to remove item from collection"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing item from collection", e)
            Result.failure(e)
        }
    }

    /**
     * Create default collections on server
     */
    suspend fun createDefaultCollections(): Result<List<Collection>> {
        return try {
            val token = getAuthToken() ?: return Result.failure(Exception("Not authenticated"))
            val response = apiService.createDefaultCollections("Bearer $token")
            
            val data = response.data
            if (response.success && data != null) {
                val collections = data.map { it.toDomain() }
                Result.success(collections)
            } else {
                Result.failure(Exception(response.error ?: "Failed to create default collections"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating default collections", e)
            Result.failure(e)
        }
    }
}
