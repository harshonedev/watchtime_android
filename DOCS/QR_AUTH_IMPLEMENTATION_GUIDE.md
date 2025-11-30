# QR Code Authentication Implementation Guide

## Overview
This guide explains how to implement QR code-based authentication between Android TV and Android Mobile apps.

## Architecture

```
┌────────────────┐                                              ┌────────────────┐
│  Android TV    │                                              │ Android Mobile │
│  App           │                                              │ App            │
└────────┬───────┘                                              └────────┬───────┘
         │                                                               │
         │ 1. Create Session                                            │
         │──────────────────────────────┐                               │
         │                              │                               │
         │                              ▼                               │
         │                      ┌───────────────┐                       │
         │ 2. QR Code URL      │               │                       │
         │◀─────────────────────│    Backend    │                       │
         │                      │    Server     │                       │
         │ 3. Display QR       │               │                       │
         │    on TV            └───────────────┘                       │
         │                              ▲                               │
         │ 4. Poll Status              │                               │
         │─────────────────────────────┤                               │
         │                              │ 5. Scan QR                    │
         │                              │◀──────────────────────────────┤
         │                              │                               │
         │                              │ 6. Link Session               │
         │                              │◀──────────────────────────────┤
         │                              │                               │
         │ 7. Status: Authenticated    │                               │
         │◀─────────────────────────────┤                               │
         │                              │                               │
```

## Implementation Steps

### 1. Android TV App (Display QR Code)

The TV app creates a session and displays a QR code for users to scan.

**Location:** `auth/tv-ui/src/main/java/com/app/auth/tvui/viewmodels/TvAuthViewModel.kt`

```kotlin
// 1. Generate unique session ID
val sessionId = UUID.randomUUID().toString()

// 2. Create auth session on backend
val response = tvAuthApiService.createTvAuthSession(sessionId)

// 3. Generate QR code from auth URL
val authUrl = response.data.authUrl // "watchtime://tv-auth?sessionId=xxx"
val qrBitmap = generateQRCode(authUrl)

// 4. Display QR code and start polling
_authState.value = TvAuthState.ShowingQRCode(qrBitmap, sessionId)
startPollingForAuth(sessionId)
```

**QR Code Generation:**
```kotlin
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
```

**Polling for Authentication:**
```kotlin
private fun startPollingForAuth(sessionId: String) {
    pollingJob = viewModelScope.launch {
        var attempts = 0
        val maxAttempts = 60 // 5 minutes with 5-second intervals
        
        while (attempts < maxAttempts) {
            try {
                val response = tvAuthApiService.checkTvAuthStatus(sessionId)
                
                if (response.success && response.data.authenticated) {
                    // Store token and mark as authenticated
                    _authState.value = TvAuthState.Authenticated
                    break
                }
                
                delay(5000) // Poll every 5 seconds
                attempts++
            } catch (e: Exception) {
                // Handle error, continue polling
                delay(5000)
                attempts++
            }
        }
        
        if (attempts >= maxAttempts) {
            _authState.value = TvAuthState.Error("Authentication timeout")
        }
    }
}
```

### 2. Android Mobile App (Scan QR Code)

The mobile app scans the QR code and links the session to the user's account.

**Location:** `auth/ui/src/main/java/com/app/auth/ui/viewmodels/QrScanViewModel.kt`

**Dependencies Required:**
```gradle
// In auth/ui/build.gradle.kts
dependencies {
    // Camera X for QR scanning
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
    
    // ML Kit Barcode Scanning
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
}
```

**QR Scanning Implementation:**

```kotlin
fun onQrCodeScanned(qrContent: String) {
    viewModelScope.launch {
        try {
            _scanState.value = QrScanState.Processing
            
            // 1. Parse QR code content
            val sessionId = extractSessionId(qrContent)
            
            if (sessionId == null) {
                _scanState.value = QrScanState.Error("Invalid QR code")
                return@launch
            }
            
            // 2. Get current user's auth token
            val user = authRepository.getCurrentUser()
            if (user == null) {
                _scanState.value = QrScanState.Error("You must be signed in")
                return@launch
            }
            
            // 3. Link mobile auth to TV session
            val token = getAuthToken() // Get from Supabase/Firebase
            val response = tvAuthApiService.linkMobileToTv(
                authorization = "Bearer $token",
                request = TvAuthLinkRequest(sessionId)
            )
            
            if (response.success) {
                _scanState.value = QrScanState.Success
            } else {
                _scanState.value = QrScanState.Error("Failed to link TV device")
            }
        } catch (e: Exception) {
            _scanState.value = QrScanState.Error(e.message ?: "Unknown error")
        }
    }
}

private fun extractSessionId(qrContent: String): String? {
    return try {
        val uri = android.net.Uri.parse(qrContent)
        uri.getQueryParameter("sessionId")
    } catch (e: Exception) {
        null
    }
}
```

**UI Screen with Camera:**

Location: `auth/ui/src/main/java/com/app/auth/ui/screens/QrScanScreen.kt`

```kotlin
@Composable
fun QrScanScreen(
    viewModel: QrScanViewModel = koinViewModel()
) {
    val scanState by viewModel.scanState
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        CameraPreview(
            onQrCodeDetected = { qrContent ->
                if (qrContent.contains("watchtime://tv-auth")) {
                    viewModel.onQrCodeScanned(qrContent)
                }
            }
        )
        
        // Scanning overlay
        when (scanState) {
            is QrScanState.Scanning -> {
                // Show scanning frame
            }
            is QrScanState.Processing -> {
                CircularProgressIndicator()
            }
            is QrScanState.Success -> {
                // Show success message
                Text("TV linked successfully!")
            }
            is QrScanState.Error -> {
                // Show error message
                Text((scanState as QrScanState.Error).message)
            }
        }
    }
}
```

### 3. Deep Link Configuration

**Add to AndroidManifest.xml:**

```xml
<activity
    android:name=".MainActivity"
    android:exported="true">
    
    <!-- Existing intent filters -->
    
    <!-- Deep link for TV Auth QR Code -->
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="watchtime"
            android:host="tv-auth" />
    </intent-filter>
</activity>
```

**Handle Deep Link in MainActivity:**

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle deep link
        intent?.data?.let { uri ->
            if (uri.scheme == "watchtime" && uri.host == "tv-auth") {
                val sessionId = uri.getQueryParameter("sessionId")
                // Navigate to QR scan success/linking screen
                // Or automatically link the session
            }
        }
    }
}
```

### 4. API Service Definitions

**Location:** `core/network/src/main/java/com/app/core/network/api/TvAuthApiService.kt`

```kotlin
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

data class TvAuthLinkRequest(
    val sessionId: String
)
```

### 5. Response Models

**Location:** `core/network/src/main/java/com/app/core/network/model/TvAuthResponse.kt`

```kotlin
@Serializable
data class TvAuthSessionResponse(
    val success: Boolean,
    val data: TvAuthSessionData
)

@Serializable
data class TvAuthSessionData(
    val sessionId: String,
    val authUrl: String,
    val expiresAt: Long
)

@Serializable
data class TvAuthStatusResponse(
    val success: Boolean,
    val data: TvAuthStatusData
)

@Serializable
data class TvAuthStatusData(
    val authenticated: Boolean,
    val token: String? = null,
    val userId: String? = null
)

@Serializable
data class TvAuthLinkResponse(
    val success: Boolean,
    val message: String
)
```

## Testing Guide

### Test Flow:

1. **Start TV App:**
   ```
   - Open Android TV app
   - Navigate to Login screen
   - App creates session and displays QR code
   ```

2. **Scan with Mobile:**
   ```
   - Open Android Mobile app (must be logged in)
   - Navigate to "Link TV Device" or scan QR option
   - Point camera at TV QR code
   - App automatically detects and processes QR code
   ```

3. **Verify Link:**
   ```
   - Mobile app shows "Successfully linked"
   - TV app stops polling and shows "Authenticated"
   - TV app can now access user's account
   ```

### Manual Testing with cURL:

```bash
# 1. Create session (TV)
SESSION_ID=$(uuidgen)
curl -X POST "http://localhost:5000/api/auth/tv/create-session?sessionId=$SESSION_ID"

# 2. Link session (Mobile)
curl -X POST "http://localhost:5000/api/auth/tv/link" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"sessionId\": \"$SESSION_ID\"}"

# 3. Check status (TV)
curl "http://localhost:5000/api/auth/tv/check-status?sessionId=$SESSION_ID"
```

## Security Considerations

1. **Session Expiry:** Sessions expire after 5-10 minutes
2. **One-Time Use:** Sessions should be invalidated after successful authentication
3. **HTTPS Only:** Always use HTTPS in production
4. **Token Security:** Never log or expose JWT tokens
5. **Rate Limiting:** Implement rate limiting on all endpoints

## Common Issues & Solutions

### Issue: QR Code Not Scanning
- **Solution:** Ensure camera permissions are granted
- Check QR code size is sufficient (min 512x512)
- Verify QR code contains correct deep link format

### Issue: Session Expired
- **Solution:** Increase session timeout or implement auto-refresh
- Add visual countdown timer on TV screen

### Issue: Polling Timeout
- **Solution:** Increase maxAttempts or decrease delay interval
- Add retry mechanism for failed polls

### Issue: Deep Link Not Working
- **Solution:** Verify AndroidManifest.xml configuration
- Test deep link with: `adb shell am start -a android.intent.action.VIEW -d "watchtime://tv-auth?sessionId=test"`

## File Structure

```
watchtime_android/
├── auth/
│   ├── tv-ui/
│   │   └── src/main/java/com/app/auth/tvui/
│   │       ├── viewmodels/
│   │       │   └── TvAuthViewModel.kt          # TV QR display logic
│   │       └── screens/
│   │           └── TvAuthScreen.kt              # TV QR UI
│   │
│   └── ui/
│       └── src/main/java/com/app/auth/ui/
│           ├── viewmodels/
│           │   └── QrScanViewModel.kt           # Mobile QR scan logic
│           └── screens/
│               └── QrScanScreen.kt              # Mobile camera UI
│
├── core/
│   └── network/
│       └── src/main/java/com/app/core/network/
│           ├── api/
│           │   └── TvAuthApiService.kt          # API definitions
│           └── model/
│               └── TvAuthResponse.kt            # Response models
│
└── app/
    └── src/main/AndroidManifest.xml             # Deep link config
```

## Next Steps

1. ✅ Review `SERVER_API_REQUIREMENTS.md` for backend implementation
2. ✅ Implement backend endpoints as specified
3. ✅ Test TV QR generation on Android TV
4. ✅ Test QR scanning on Android Mobile
5. ✅ Implement deep link handling
6. ✅ Add error handling and user feedback
7. ✅ Deploy and test in production environment

## References

- [SERVER_API_REQUIREMENTS.md](./SERVER_API_REQUIREMENTS.md) - Complete API documentation
- [TV_IMPLEMENTATION.md](./TV_IMPLEMENTATION.md) - TV app implementation guide
- [API_DOCS.md](./API_DOCS.md) - General API documentation

