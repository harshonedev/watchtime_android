package com.app.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Base Response Wrapper
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null,
    val message: String? = null
)

// Collection Models
@Serializable
data class CollectionDto(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val name: String,
    val description: String? = null,
    @SerialName("is_default")
    val isDefault: Boolean,
    @SerialName("is_public")
    val isPublic: Boolean,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("collection_items")
    val collectionItems: List<CollectionItemDto>? = null
)

@Serializable
data class CollectionItemDto(
    val id: String,
    @SerialName("collection_id")
    val collectionId: String,
    @SerialName("content_id")
    val contentId: String,
    @SerialName("tmdb_id")
    val tmdbId: Int,
    @SerialName("media_type")
    val mediaType: String,
    @SerialName("added_at")
    val addedAt: String,
    val notes: String? = null,
    @SerialName("content_metadata")
    val contentMetadata: ContentMetadataDto? = null
)

@Serializable
data class ContentMetadataDto(
    val id: String,
    @SerialName("tmdb_id")
    val tmdbId: Int,
    @SerialName("media_type")
    val mediaType: String,
    val title: String,
    @SerialName("original_title")
    val originalTitle: String? = null,
    val overview: String? = null,
    @SerialName("poster_path")
    val posterPath: String? = null,
    @SerialName("backdrop_path")
    val backdropPath: String? = null,
    @SerialName("release_date")
    val releaseDate: String? = null,
    val genres: List<GenreDto>? = null,
    @SerialName("vote_average")
    val voteAverage: Double? = null,
    @SerialName("vote_count")
    val voteCount: Int? = null,
    val popularity: Double? = null,
    val adult: Boolean = false
)

@Serializable
data class GenreDto(
    val id: Int,
    val name: String
)

// Request Bodies
@Serializable
data class CreateCollectionRequest(
    val name: String,
    val description: String? = null,
    @SerialName("is_public")
    val isPublic: Boolean = false
)

@Serializable
data class UpdateCollectionRequest(
    val name: String? = null,
    val description: String? = null,
    @SerialName("is_public")
    val isPublic: Boolean? = null
)

@Serializable
data class AddItemToCollectionRequest(
    @SerialName("tmdb_id")
    val tmdbId: Int,
    @SerialName("media_type")
    val mediaType: String,
    val title: String,
    @SerialName("original_title")
    val originalTitle: String? = null,
    val overview: String? = null,
    @SerialName("poster_path")
    val posterPath: String? = null,
    @SerialName("backdrop_path")
    val backdropPath: String? = null,
    @SerialName("release_date")
    val releaseDate: String? = null,
    val genres: List<GenreDto>? = null,
    @SerialName("vote_average")
    val voteAverage: Double? = null,
    @SerialName("vote_count")
    val voteCount: Int? = null,
    val popularity: Double? = null,
    val adult: Boolean = false,
    val notes: String? = null
)

// Response Types
typealias CollectionsResponse = ApiResponse<List<CollectionDto>>
typealias CollectionResponse = ApiResponse<CollectionDto>
typealias CollectionItemResponse = ApiResponse<CollectionItemDto>
typealias DeleteResponse = ApiResponse<Unit>
