package com.app.profile.data.repository

import android.util.Log
import com.app.auth.data.services.SupabaseAuthService
import com.app.core.network.api.UserApiService
import com.app.core.utils.failures.Failure
import com.app.profile.domain.entities.UserProfile
import com.app.profile.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
    private val userApiService: UserApiService,
    private val supabaseAuthService: SupabaseAuthService
) : ProfileRepository {

    companion object {
        const val TAG = "ProfileRepositoryImpl"
    }

    override suspend fun getCurrentUserProfile(): UserProfile {
        try {
            val token = supabaseAuthService.getToken() ?: throw Failure.AuthenticationError("Supabase token is null")
            val response = userApiService.getUserProfile("Bearer $token")
            if(!response.success) throw Failure.ServerError("Failed to get user profile")

            return UserProfile(
                id = response.data.profile.id,
                name = response.data.profile.fullName,
                email = response.data.profile.email,
                profilePictureUrl = response.data.profile.avatarUrl
            )
        } catch (e : Throwable) {
            Log.e(TAG, "getCurrentUserProfile: ", e)
            throw e
        }
    }
}
