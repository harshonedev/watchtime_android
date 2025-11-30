package com.app.auth.tvui.viewmodels

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.auth.domain.repository.AuthRepository
import com.app.core.network.api.TvAuthApiService
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

sealed class TvAuthState {
    object Idle : TvAuthState()
    data class ShowingQRCode(val qrBitmap: Bitmap, val sessionId: String) : TvAuthState()
    object Authenticated : TvAuthState()
    data class Error(val message: String) : TvAuthState()
}

class TvAuthViewModel(
    private val tvAuthApiService: TvAuthApiService,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = mutableStateOf<TvAuthState>(TvAuthState.Idle)
    val authState get() = _authState

    private var pollingJob: kotlinx.coroutines.Job? = null

    companion object {
        private const val TAG = "TvAuthViewModel"
    }

    fun initiateQRAuth() {
        viewModelScope.launch {
            try {
                // Generate unique session ID
                val sessionId = UUID.randomUUID().toString()
                Log.d(TAG, "Creating TV auth session with ID: $sessionId")

                // Create auth session on backend
                val response = tvAuthApiService.createTvAuthSession(sessionId)

                if (response.success) {
                    // Generate QR code with auth URL
                    val authUrl = response.data.authUrl // e.g., "watchtime://tv-auth?sessionId=xxx"
                    Log.d(TAG, "Auth session created successfully. Auth URL: $authUrl")
                    val qrBitmap = generateQRCode(authUrl)

                    _authState.value = TvAuthState.ShowingQRCode(qrBitmap, sessionId)

                    // Start polling for authentication
                    startPollingForAuth(sessionId)
                } else {
                    Log.e(TAG, "Failed to create auth session: response.success = false")
                    _authState.value = TvAuthState.Error("Failed to create auth session")
                }
            } catch (e: IOException) {
                val errorMessage = "Network connection error. Please check your internet connection."
                Log.e(TAG, "Network error during auth session creation", e)
                _authState.value = TvAuthState.Error(errorMessage)
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown error occurred"
                Log.e(TAG, "Error during auth session creation: $errorMessage", e)
                // Check if it's an HTTP error by examining the exception
                val displayMessage = if (errorMessage.contains("500")) {
                    "Server error (HTTP 500). The backend authentication service may not be implemented yet."
                } else if (errorMessage.contains("404")) {
                    "Authentication endpoint not found (HTTP 404)"
                } else {
                    errorMessage
                }
                _authState.value = TvAuthState.Error(displayMessage)
            }
        }
    }

    private fun generateQRCode(content: String, size: Int = 512): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }

        return bitmap
    }

    private fun startPollingForAuth(sessionId: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            var attempts = 0
            val maxAttempts = 60 // 5 minutes (5 seconds interval)

            Log.d(TAG, "Starting to poll for authentication status...")

            while (attempts < maxAttempts) {
                try {
                    val response = tvAuthApiService.checkTvAuthStatus(sessionId)

                    if (response.success && response.data.authenticated) {
                        // Authentication successful - save token and user ID
                        val token = response.data.token
                        val userId = response.data.userId

                        if (token != null && userId != null) {
                            Log.d(TAG, "Authentication successful! Saving token...")
                            authRepository.saveTvAuthToken(token, userId)
                            Log.d(TAG, "Token saved successfully")

                            // Update auth state
                            _authState.value = TvAuthState.Authenticated
                            break
                        } else {
                            Log.e(TAG, "Authentication successful but token or userId is null")
                            _authState.value = TvAuthState.Error("Authentication error: Invalid response")
                            break
                        }
                    }

                    delay(5000) // Poll every 5 seconds
                    attempts++
                } catch (e: IOException) {
                    Log.w(TAG, "Network error during polling (attempt $attempts): ${e.message}")
                    // Continue polling even on network errors
                    delay(5000)
                    attempts++
                } catch (e: Exception) {
                    Log.w(TAG, "Error during polling (attempt $attempts): ${e.message}", e)
                    // Continue polling even on errors
                    delay(5000)
                    attempts++
                }
            }

            // Timeout
            if (attempts >= maxAttempts && _authState.value !is TvAuthState.Authenticated) {
                Log.e(TAG, "Authentication timeout after $maxAttempts attempts")
                _authState.value = TvAuthState.Error("Authentication timeout. Please try again.")
            }
        }
    }

    @Suppress("unused")
    fun cancelAuth() {
        pollingJob?.cancel()
        _authState.value = TvAuthState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}

