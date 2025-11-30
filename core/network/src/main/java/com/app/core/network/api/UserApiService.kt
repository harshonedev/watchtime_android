package com.app.core.network.api

import com.app.core.network.model.UserProfileResponse
import com.app.core.network.model.UserSetupResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface UserApiService {
    @GET("user/profile")
    suspend fun getUserProfile(
        @Header("Authorization") authorization: String
    ): UserProfileResponse

    @POST("user/setup")
    suspend fun setupUserProfile(
        @Header("Authorization") authorization: String
    ): UserSetupResponse

}