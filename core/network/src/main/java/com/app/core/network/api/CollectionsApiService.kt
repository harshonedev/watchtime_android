package com.app.core.network.api

import com.app.core.network.model.AddItemToCollectionRequest
import com.app.core.network.model.ApiResponse
import com.app.core.network.model.CollectionDto
import com.app.core.network.model.CollectionItemDto
import com.app.core.network.model.CreateCollectionRequest
import com.app.core.network.model.UpdateCollectionRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CollectionsApiService {

    /**
     * Get all collections for the authenticated user
     * GET /api/collections
     */
    @GET("collections")
    suspend fun getCollections(
        @Header("Authorization") authorization: String
    ): ApiResponse<List<CollectionDto>>

    /**
     * Get a specific collection by ID
     * GET /api/collections/{id}
     */
    @GET("collections/{id}")
    suspend fun getCollectionById(
        @Path("id") collectionId: String,
        @Header("Authorization") authorization: String
    ): ApiResponse<CollectionDto>

    /**
     * Create a new collection
     * POST /api/collections
     */
    @POST("collections")
    suspend fun createCollection(
        @Header("Authorization") authorization: String,
        @Body request: CreateCollectionRequest
    ): ApiResponse<CollectionDto>

    /**
     * Update an existing collection
     * PUT /api/collections/{id}
     */
    @PUT("collections/{id}")
    suspend fun updateCollection(
        @Path("id") collectionId: String,
        @Header("Authorization") authorization: String,
        @Body request: UpdateCollectionRequest
    ): ApiResponse<CollectionDto>

    /**
     * Delete a collection
     * DELETE /api/collections/{id}
     */
    @DELETE("collections/{id}")
    suspend fun deleteCollection(
        @Path("id") collectionId: String,
        @Header("Authorization") authorization: String
    ): ApiResponse<Unit>

    /**
     * Add an item to a collection
     * POST /api/collections/{id}/items
     */
    @POST("collections/{id}/items")
    suspend fun addItemToCollection(
        @Path("id") collectionId: String,
        @Header("Authorization") authorization: String,
        @Body request: AddItemToCollectionRequest
    ): ApiResponse<CollectionItemDto>

    /**
     * Remove an item from a collection
     * DELETE /api/collections/{id}/items/{itemId}
     */
    @DELETE("collections/{id}/items/{itemId}")
    suspend fun removeItemFromCollection(
        @Path("id") collectionId: String,
        @Path("itemId") itemId: String,
        @Header("Authorization") authorization: String
    ): ApiResponse<Unit>

    /**
     * Create default collections (Watch Later, Already Watched) for a new user
     * POST /api/collections/setup/defaults
     */
    @POST("collections/setup/defaults")
    suspend fun createDefaultCollections(
        @Header("Authorization") authorization: String
    ): ApiResponse<List<CollectionDto>>
}
