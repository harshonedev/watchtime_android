package com.app.auth.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.app.auth.data.services.FirebaseAuthService
import com.app.auth.data.services.SupabaseAuthService
import com.app.auth.domain.entities.UserEntity
import com.app.auth.domain.repository.AuthRepository
import com.app.core.network.api.UserApiService
import com.app.core.utils.failures.Failure
import java.security.MessageDigest
import java.util.UUID

class AuthRepositoryImpl(
    private val context: Context,
    private val authService: FirebaseAuthService,
    private val supabaseAuthService: SupabaseAuthService,
    private val apiService: UserApiService
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepositoryImpl"
        private const val PREFS_NAME = "watchtime_auth_prefs"
        private const val KEY_TV_AUTH_TOKEN = "tv_auth_token"
        private const val KEY_TV_USER_ID = "tv_user_id"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override suspend fun login(): UserEntity {
        try {

            // Generate a nonce and hash it with sha-256
            val rawNonce = UUID.randomUUID().toString() // Generate a random String. UUID should be sufficient, but can also be any other random string.
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) } // Hashed nonce to be passed to Google sign-in

            // start google sign-in flow
            val idToken = authService.googleSignIn(hashedNonce)
            // use the idToken to authenticate with Firebase
            authService.firebaseAuthWithGoogle(idToken)
                ?: throw Failure.AuthenticationError("Firebase user is null")

            supabaseAuthService.authWithGoogle(idToken, rawNonce)

            // print access token
            Log.d(TAG, "login: ${supabaseAuthService.getToken()}")

            val token = supabaseAuthService.getToken() ?: throw Failure.AuthenticationError("Supabase token is null")

            val response = apiService.setupUserProfile("Bearer $token")

            // print response
            Log.d(TAG, "setup profile response: $response")


            if (!response.success) throw Failure.ServerError("Failed to setup user profile")
            // map FirebaseUser to UserEntity
            return UserEntity(
                id = response.data.profile.id,
                name = response.data.profile.fullName,
                email = response.data.profile.email,
                profilePictureUrl = response.data.profile.avatarUrl,
            )

        } catch (e: Throwable) {
            // Handle any exceptions that may occur during the login process
            Log.e(TAG, "login: $e", )
            throw e

        }

    }

    override suspend fun logout(): Boolean {
        try {
            authService.signOut()
            supabaseAuthService.signOut()

            // Clear TV auth token
            sharedPreferences.edit().apply {
                remove(KEY_TV_AUTH_TOKEN)
                remove(KEY_TV_USER_ID)
                apply()
            }

            return true
        } catch (e: Exception) {
            // Log the error or handle it as needed
            Log.e(TAG, "logout: ", e)
            return false
        }
    }

    override fun isLoggedIn(): Boolean {
        try {
            return supabaseAuthService.isLoggedIn()
        } catch (e: Exception) {
            // Log the error or handle it as needed
            Log.e(TAG, "isLoggedIn: e", e)
            return false
        }
    }

    override fun getCurrentUser(): UserEntity? {
        try {
            val supabaseUser = supabaseAuthService.getSupabaseUser()
            return if (supabaseUser != null) {
                UserEntity(
                    id = supabaseUser.id,
                    name = removeQuotes(supabaseUser.userMetadata?.get("name").toString()),
                    email = supabaseUser.email ?: "",
                    profilePictureUrl = removeQuotes(supabaseUser.userMetadata?.get("avatar_url").toString()),
                )
            } else {
                null
            }
        } catch (e: Exception) {
            // Log the error or handle it as needed
            Log.e(TAG, "getCurrentUser: e", e)
            return null
        }
    }

    override fun getAuthToken(): String? {
        try {
            return supabaseAuthService.getToken()
        } catch (e: Exception) {
            Log.e(TAG, "getAuthToken: ", e)
            return null
        }
    }


    private fun removeQuotes(input: String): String {
        return input.trim('"')
    }

    // TV Auth Implementation
    override suspend fun saveTvAuthToken(token: String, userId: String) {
        try {
            sharedPreferences.edit().apply {
                putString(KEY_TV_AUTH_TOKEN, token)
                putString(KEY_TV_USER_ID, userId)
                apply()
            }
            Log.d(TAG, "TV auth token saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving TV auth token", e)
            throw e
        }
    }

    override fun getTvAuthToken(): String? {
        return try {
            sharedPreferences.getString(KEY_TV_AUTH_TOKEN, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting TV auth token", e)
            null
        }
    }

    override fun isTvAuthenticated(): Boolean {
        return try {
            getTvAuthToken() != null
        } catch (e: Exception) {
            Log.e(TAG, "Error checking TV auth status", e)
            false
        }
    }
}