package com.app.auth.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.auth.domain.repository.AuthRepository
import com.app.core.network.api.TvAuthApiService
import com.app.core.network.api.TvAuthLinkRequest
import kotlinx.coroutines.launch
import java.io.IOException

sealed class QrScanState {
    object Idle : QrScanState()
    object Scanning : QrScanState()
    object Processing : QrScanState()
    object Success : QrScanState()
    data class Error(val message: String) : QrScanState()
}

class QrScanViewModel(
    private val tvAuthApiService: TvAuthApiService,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _scanState = mutableStateOf<QrScanState>(QrScanState.Idle)
    val scanState get() = _scanState

    companion object {
        private const val TAG = "QrScanViewModel"
    }

    fun startScanning() {
        _scanState.value = QrScanState.Scanning
    }

    fun onQrCodeScanned(qrContent: String) {
        viewModelScope.launch {
            try {
                _scanState.value = QrScanState.Processing
                Log.d(TAG, "Processing QR code: $qrContent")

                // Parse QR code content (e.g., "watchtime://tv-auth?sessionId=xxx")
                val sessionId = extractSessionId(qrContent)

                if (sessionId == null) {
                    Log.e(TAG, "Invalid QR code format: $qrContent")
                    _scanState.value = QrScanState.Error("Invalid QR code")
                    return@launch
                }

                Log.d(TAG, "Extracted session ID: $sessionId")

                // Get current user's auth token
                val user = authRepository.getCurrentUser()
                if (user == null) {
                    Log.e(TAG, "User not authenticated")
                    _scanState.value = QrScanState.Error("You must be signed in to link TV")
                    return@launch
                }

                // Get authentication token
                val token = authRepository.getAuthToken()
                if (token == null) {
                    Log.e(TAG, "Failed to retrieve auth token")
                    _scanState.value = QrScanState.Error("Authentication token not available")
                    return@launch
                }

                Log.d(TAG, "Linking session for user: ${user.email}")

                // Link mobile auth to TV session
                val response = tvAuthApiService.linkMobileToTv(
                    authorization = "Bearer $token",
                    request = TvAuthLinkRequest(sessionId)
                )

                if (response.success) {
                    Log.d(TAG, "Successfully linked TV device")
                    _scanState.value = QrScanState.Success
                } else {
                    Log.e(TAG, "Failed to link TV device: ${response.message}")
                    _scanState.value = QrScanState.Error(response.message ?: "Failed to link TV device")
                }

            } catch (e: IOException) {
                val errorMessage = "Network error. Please check your internet connection."
                Log.e(TAG, "Network error during TV link", e)
                _scanState.value = QrScanState.Error(errorMessage)
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown error occurred"
                Log.e(TAG, "Error during TV link: $errorMessage", e)
                _scanState.value = QrScanState.Error(errorMessage)
            }
        }
    }

    private fun extractSessionId(qrContent: String): String? {
        // Parse URL like "watchtime://tv-auth?sessionId=xxx"
        return try {
            val uri = android.net.Uri.parse(qrContent)
            if (uri.scheme == "watchtime" && uri.host == "tv-auth") {
                uri.getQueryParameter("sessionId")
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing QR content", e)
            null
        }
    }


    fun resetState() {
        _scanState.value = QrScanState.Idle
    }
}

