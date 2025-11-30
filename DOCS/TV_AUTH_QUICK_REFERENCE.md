# TV Authentication Quick Reference Guide

## For Developers

### TV App Flow

#### 1. Starting the App
The app automatically checks authentication status:
```kotlin
// In TvAppNavigation.kt
val startDestination = if (authRepository.isTvAuthenticated()) {
    TvScreen.Home.route  // Already authenticated
} else {
    TvScreen.Auth.route  // Need to authenticate
}
```

#### 2. Authentication Process
```kotlin
// TvAuthViewModel handles the entire flow:

// Step 1: Generate session ID and create session
val sessionId = UUID.randomUUID().toString()
val response = tvAuthApiService.createTvAuthSession(sessionId)

// Step 2: Display QR code
val qrBitmap = generateQRCode(response.data.authUrl)

// Step 3: Poll for authentication (every 5 seconds)
while (attempts < maxAttempts) {
    val status = tvAuthApiService.checkTvAuthStatus(sessionId)
    if (status.data.authenticated) {
        // Save token and navigate
        authRepository.saveTvAuthToken(status.data.token, status.data.userId)
        break
    }
    delay(5000)
}
```

#### 3. Using Stored Token for API Calls
```kotlin
// Get token from repository
val token = authRepository.getTvAuthToken()

// Use in API calls
val response = apiService.someEndpoint(
    authorization = "Bearer $token"
)
```

#### 4. Sign Out
```kotlin
// Clear authentication
authRepository.logout()
navController.navigate(TvScreen.Auth.route)
```

### Mobile App Flow

#### 1. Scan QR Code
```kotlin
// QrScanScreen.kt uses ML Kit to scan
// Looks for: "watchtime://tv-auth?sessionId=xxx"

val scanner = BarcodeScanning.getClient()
scanner.process(image)
    .addOnSuccessListener { barcodes ->
        barcodes.forEach { barcode ->
            barcode.rawValue?.let { value ->
                if (value.contains("watchtime://tv-auth")) {
                    onQrCodeScanned(value)
                }
            }
        }
    }
```

#### 2. Link to TV
```kotlin
// Extract session ID
val sessionId = Uri.parse(qrContent).getQueryParameter("sessionId")

// Get user's auth token
val token = authRepository.getAuthToken()

// Link mobile to TV
val response = tvAuthApiService.linkMobileToTv(
    authorization = "Bearer $token",
    request = TvAuthLinkRequest(sessionId)
)
```

## API Endpoints Reference

### 1. Create TV Session
```http
POST /auth/tv/create-session?sessionId={uuid}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "authUrl": "watchtime://tv-auth?sessionId=550e8400-...",
    "expiresAt": 1701363000000
  }
}
```

### 2. Check Status
```http
GET /auth/tv/check-status?sessionId={uuid}
```

**Response (Not Authenticated):**
```json
{
  "success": true,
  "data": {
    "authenticated": false,
    "token": null,
    "userId": null
  }
}
```

**Response (Authenticated):**
```json
{
  "success": true,
  "data": {
    "authenticated": true,
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "a1b2c3d4-e5f6-4789-0abc-def123456789"
  }
}
```

### 3. Link Mobile to TV
```http
POST /auth/tv/link
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response:**
```json
{
  "success": true,
  "data": null,
  "message": "TV device linked successfully"
}
```

## Key Files Modified

### Repository Layer
- `auth/domain/src/main/java/com/app/auth/domain/repository/AuthRepository.kt`
  - Added: `saveTvAuthToken()`, `getTvAuthToken()`, `isTvAuthenticated()`

- `auth/data/src/main/java/com/app/auth/data/repository/AuthRepositoryImpl.kt`
  - Implemented token storage using SharedPreferences
  - Added logout token clearing

### TV UI Layer
- `auth/tv-ui/src/main/java/com/app/auth/tvui/viewmodels/TvAuthViewModel.kt`
  - QR code generation
  - Session polling
  - Token storage on success

- `auth/tv-ui/src/main/java/com/app/auth/tvui/screens/TvAuthScreen.kt`
  - QR code display
  - Status messages
  - Error handling

### Mobile UI Layer
- `auth/ui/src/main/java/com/app/auth/ui/viewmodels/QrScanViewModel.kt`
  - QR scanning logic
  - Session linking

- `auth/ui/src/main/java/com/app/auth/ui/screens/QrScanScreen.kt`
  - Camera integration
  - Permission handling

### Network Layer
- `core/network/src/main/java/com/app/core/network/api/TvAuthApiService.kt`
  - API endpoint definitions

- `core/network/src/main/java/com/app/core/network/model/TvAuthResponse.kt`
  - Response models

### App Layer
- `app-tv/src/main/java/com/app/watchtime/tv/navigation/TvAppNavigation.kt`
  - Auth state checking
  - Dynamic routing

- `app-tv/src/main/java/com/app/watchtime/tv/screens/TvHomeScreen.kt`
  - Sign out functionality

## Testing

### Manual Testing Steps

1. **TV App First Launch**
   ```
   1. Launch TV app
   2. Should show QR code auth screen
   3. Verify QR code is displayed
   4. Verify instructions are shown
   ```

2. **Mobile App Linking**
   ```
   1. Open mobile app (must be signed in)
   2. Navigate to "Scan QR Code" (or implement in menu)
   3. Grant camera permission if needed
   4. Point camera at TV QR code
   5. Wait for scan and link
   6. Verify success message
   ```

3. **TV App Post-Link**
   ```
   1. TV should detect authentication
   2. Navigate to home screen
   3. Verify user content loads
   ```

4. **TV App Subsequent Launch**
   ```
   1. Close and reopen TV app
   2. Should go directly to home screen
   3. Should NOT show QR code again
   ```

5. **Sign Out**
   ```
   1. Click "Sign Out" on TV home screen
   2. Should return to QR code auth screen
   3. Token should be cleared
   ```

### Error Scenarios to Test

- ❌ Network disconnected during session creation
- ❌ QR code expires (10 minutes)
- ❌ Mobile app not signed in
- ❌ Invalid QR code scanned
- ❌ Backend server down
- ❌ Session already linked (re-scan same QR)

## Troubleshooting

### Issue: QR Code Not Generating
**Check:**
- Network connectivity
- API endpoint accessibility
- Session ID generation (must be valid UUID)

**Log Tags:**
```kotlin
"TvAuthViewModel" // Check for session creation logs
```

### Issue: Polling Not Working
**Check:**
- Session ID matches between create and poll
- Polling interval (should be 5 seconds)
- Network stability

**Look for:**
```
"Starting to poll for authentication status..."
"Error during polling (attempt X):"
```

### Issue: Token Not Saving
**Check:**
- Token and userId are not null
- SharedPreferences write permissions
- Context is properly injected

**Log:**
```
"TV auth token saved successfully"
```

### Issue: Mobile Can't Link
**Check:**
- User is signed in on mobile
- JWT token is available
- Session ID extracted correctly
- Network connectivity

**Verify:**
```kotlin
authRepository.getCurrentUser() != null
authRepository.getAuthToken() != null
```

## Configuration

### Base URL
Update in `Constants.kt`:
```kotlin
const val API_SERVER_URL = "https://your-api-domain.com/api/"
```

### Session Timeout
Server-side: 10 minutes  
Client polling: 5 minutes (60 attempts × 5 seconds)

Adjust in `TvAuthViewModel.kt`:
```kotlin
val maxAttempts = 120 // 10 minutes
delay(5000) // 5 seconds
```

## Security Notes

1. **Token Storage:** SharedPreferences with MODE_PRIVATE
2. **No Logging:** Tokens are never logged
3. **HTTPS Only:** Production must use HTTPS
4. **Session Expiry:** 10-minute timeout enforced server-side
5. **One-Time Use:** Sessions can only be linked once

## Future Enhancements

- [ ] Token refresh mechanism
- [ ] User profile display on home screen
- [ ] Device management (view/revoke linked devices)
- [ ] Offline detection and graceful handling
- [ ] Analytics and monitoring
- [ ] Deep link handling improvements

---

**Support:** Refer to `TV_AUTH_API_REFERENCE.md` for complete API documentation.

