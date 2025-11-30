package com.app.collections.data.mappers

import com.app.collections.domain.models.Collection
import com.app.collections.domain.models.CollectionItem
import com.app.collections.domain.models.ContentMetadata
import com.app.core.network.model.AddItemToCollectionRequest
import com.app.core.network.model.CollectionDto
import com.app.core.network.model.CollectionItemDto
import com.app.core.network.model.ContentMetadataDto
import com.app.core.network.model.CreateCollectionRequest
import com.app.core.network.model.GenreDto
import com.app.core.network.model.UpdateCollectionRequest
import kotlinx.serialization.json.Json
import java.util.UUID

// Network DTO to Domain mapping
fun CollectionDto.toDomain(): Collection {
    return Collection(
        id = UUID.fromString(id),
        userId = userId,
        name = name,
        description = description,
        isDefault = isDefault,
        isPublic = isPublic,
        createdAt = parseIsoDate(createdAt),
        updatedAt = parseIsoDate(updatedAt),
        items = collectionItems?.map { it.toDomain() } ?: emptyList()
    )
}

fun CollectionItemDto.toDomain(): CollectionItem {
    return CollectionItem(
        id = UUID.fromString(id),
        collectionId = UUID.fromString(collectionId),
        contentId = UUID.fromString(contentId),
        tmdbId = tmdbId,
        mediaType = mediaType,
        addedAt = parseIsoDate(addedAt),
        notes = notes,
        content = contentMetadata?.toDomain() ?: throw IllegalStateException("Content metadata is required")
    )
}

fun ContentMetadataDto.toDomain(): ContentMetadata {
    val genresJson = genres?.let { genreList ->
        Json.encodeToString(kotlinx.serialization.builtins.ListSerializer(GenreDto.serializer()), genreList)
    }
    
    return ContentMetadata(
        id = UUID.fromString(id),
        tmdbId = tmdbId,
        mediaType = mediaType,
        title = title,
        originalTitle = originalTitle,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        genres = genresJson,
        voteAverage = voteAverage,
        voteCount = voteCount,
        popularity = popularity,
        adult = adult,
        createdAt = System.currentTimeMillis(), // Use current time for network data
        updatedAt = System.currentTimeMillis()
    )
}

// Domain to Network DTO mapping (for requests)
fun Collection.toCreateRequest(): CreateCollectionRequest {
    return CreateCollectionRequest(
        name = name,
        description = description,
        isPublic = isPublic
    )
}

fun Collection.toUpdateRequest(): UpdateCollectionRequest {
    return UpdateCollectionRequest(
        name = name,
        description = description,
        isPublic = isPublic
    )
}

fun ContentMetadata.toAddItemRequest(notes: String? = null): AddItemToCollectionRequest {
    val genresList = genres?.let {
        try {
            Json.decodeFromString<List<GenreDto>>(it)
        } catch (e: Exception) {
            null
        }
    }
    
    return AddItemToCollectionRequest(
        tmdbId = tmdbId,
        mediaType = mediaType,
        title = title,
        originalTitle = originalTitle,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        genres = genresList,
        voteAverage = voteAverage,
        voteCount = voteCount,
        popularity = popularity,
        adult = adult,
        notes = notes
    )
}

// Helper function to parse ISO 8601 date strings to milliseconds
internal fun parseIsoDate(isoDate: String): Long {
    return try {
        // Parse ISO 8601 format (e.g., "2024-01-01T00:00:00.000Z")
        java.time.Instant.parse(isoDate).toEpochMilli()
    } catch (e: Exception) {
        // Fallback to current time if parsing fails
        System.currentTimeMillis()
    }
}
