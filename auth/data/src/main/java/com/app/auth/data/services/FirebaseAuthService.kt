package com.app.auth.data.services

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat.getString
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.app.auth.data.R
import com.app.core.utils.failures.Failure
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

class FirebaseAuthService(
    private val context: Context,
) {
    private val auth = Firebase.auth
    private val credentialManager = CredentialManager.create(context)

    companion object {
        private const val TAG = "FirebaseAuthService"
    }

    suspend fun googleSignIn(hashedNonce : String): String {
        val googleIdOption =  GetGoogleIdOption.Builder()
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false)
            .setNonce(hashedNonce)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(context, request)
            return getIdToken(result.credential)

        } catch (e: GetCredentialException) {
            Log.e(TAG, "Couldn't retrieve user's credentials: ${e.type}")
            if (e.type.contains("TYPE_USER_CANCELED")) {
                Log.w(TAG, "User cancelled the sign-in flow")
                throw Failure.AuthenticationCancelled("User cancelled the sign-in flow")
            }
            Log.e(TAG, "Error retrieving credentials: ${e.localizedMessage}")
            throw Failure.AuthenticationError("Failed to retrieve credentials")
        }
    }

    private fun getIdToken(credential: Credential): String {
        try {
            // Check if credential is of type Google ID
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                // Create Google ID Token
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                // Sign in to Firebase with using the token
                return googleIdTokenCredential.idToken
            } else {
                Log.w(TAG, "Credential is not of type Google ID!")
                throw Failure.AuthenticationError("Credential is not a Google ID token")
            }

        } catch (e: Throwable) {
            Log.e(TAG, "Error retrieving ID token: ${e.localizedMessage}")
            throw Failure.AuthenticationError(e.message)
        }

    }



    suspend fun firebaseAuthWithGoogle(idToken: String): FirebaseUser? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()

        return result.user

    }

    suspend fun signOut() {
        try {
            auth.signOut()
            val clearRequest = ClearCredentialStateRequest()
            credentialManager.clearCredentialState(clearRequest)
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out: ${e.localizedMessage}")
            throw Failure.AuthenticationError("Failed to sign out")
        }
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

}