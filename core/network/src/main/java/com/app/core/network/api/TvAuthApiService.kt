package com.app.core.network.api

import com.app.core.network.model.TvAuthSessionResponse
import com.app.core.network.model.TvAuthStatusResponse
import com.app.core.network.model.TvAuthLinkResponse
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface TvAuthApiService {

    @POST("auth/tv/create-session")
    suspend fun createTvAuthSession(
        @Query("sessionId") sessionId: String
    ): TvAuthSessionResponse

    @GET("auth/tv/check-status")
    suspend fun checkTvAuthStatus(
        @Query("sessionId") sessionId: String
    ): TvAuthStatusResponse

    @POST("auth/tv/link")
    suspend fun linkMobileToTv(
        @Header("Authorization") authorization: String,
        @Body request: TvAuthLinkRequest
    ): TvAuthLinkResponse
}

@Serializable
data class TvAuthLinkRequest(
    val sessionId: String
)

