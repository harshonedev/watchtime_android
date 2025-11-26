package com.app.auth.data.repository

import android.util.Log
import com.app.auth.data.services.FirebaseAuthService
import com.app.auth.data.services.SupabaseAuthService
import com.app.auth.domain.entities.UserEntity
import com.app.auth.domain.repository.AuthRepository
import com.app.core.utils.failures.Failure
import java.security.MessageDigest
import java.util.UUID

class AuthRepositoryImpl(
    private val authService: FirebaseAuthService,
    private val supabaseAuthService: SupabaseAuthService
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepositoryImpl"
    }

    override suspend fun login(): UserEntity {
        try {

            // Generate a nonce and hash it with sha-256
            val rawNonce = UUID.randomUUID().toString() // Generate a random String. UUID should be sufficient, but can also be any other random string.
            val bytes = rawNonce.toString().toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) } // Hashed nonce to be passed to Google sign-in

            // start google sign-in flow
            val idToken = authService.googleSignIn(hashedNonce)
            // use the idToken to authenticate with Firebase
            val firebaseUser = authService.firebaseAuthWithGoogle(idToken)
                ?: throw Failure.AuthenticationError("Firebase user is null")

            supabaseAuthService.authWithGoogle(idToken, rawNonce)

            val supabaseUser = supabaseAuthService.getSupabaseUser()
            // map FirebaseUser to UserEntity
            return UserEntity(
                id = firebaseUser.uid,
                name = firebaseUser.displayName ?: "Unknown",
                email = firebaseUser.email ?: "No Email",
                profilePictureUrl = firebaseUser.photoUrl?.toString(),
            )

        } catch (e: Throwable) {
            // Handle any exceptions that may occur during the login process
            throw e

        }

    }

    override suspend fun logout(): Boolean {
        try {
            authService.signOut()
            return true
        } catch (e: Exception) {
            // Log the error or handle it as needed
            Log.e(TAG, "logout: ", e)
            return false
        }
    }

    override fun isLoggedIn(): Boolean {
        try {
            return authService.isLoggedIn()
        } catch (e: Exception) {
            // Log the error or handle it as needed
            Log.e(TAG, "isLoggedIn: e", e)
            return false
        }
    }

    override fun getCurrentUser(): UserEntity? {
        try {
            val user = authService.getCurrentUser()
            supabaseAuthService.getSupabaseUser()
            return if (user != null) {
                UserEntity(
                    id = user.uid,
                    name = user.displayName ?: "Unknown",
                    email = user.email ?: "No Email",
                    profilePictureUrl = user.photoUrl?.toString(),
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


}