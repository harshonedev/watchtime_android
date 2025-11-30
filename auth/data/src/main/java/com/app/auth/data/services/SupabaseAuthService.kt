package com.app.auth.data.services

import android.util.Log
import com.app.core.utils.failures.Failure
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.exceptions.RestException

class SupabaseAuthService (
    private val supabase : SupabaseClient
) {

    companion object {
        const val TAG = "SupabaseAuthService.kt"
    }

    suspend fun authWithGoogle(googleIdToken : String, rawNonce : String) {
        try {

            supabase.auth.signInWith(IDToken){
                idToken = googleIdToken
                provider = Google
                nonce = rawNonce
            }
        } catch (e: RestException) {
            Log.e(TAG, "Error on supabase auth: ${e.localizedMessage}")
            throw Failure.AuthenticationError(e.message)
        } catch (e : Exception) {
            Log.e(TAG, "Error on supabase auth with google sign in: ${e.localizedMessage}")
            throw Failure.AuthenticationError(e.message)
        }
    }

     fun getSupabaseUser() : UserInfo? {
         return supabase.auth.currentUserOrNull()
     }

    fun getToken() : String? {
        return supabase.auth.currentAccessTokenOrNull()
    }

    suspend fun signOut() {
        supabase.auth.signOut()
    }

    fun isLoggedIn() : Boolean {
        return supabase.auth.currentUserOrNull() != null
    }
}