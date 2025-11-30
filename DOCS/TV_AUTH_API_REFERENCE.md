# TV Authentication API Reference

**Version:** 1.0.0  
**Last Updated:** November 30, 2025  
**Base URL:** `https://boc4cgg8sgkkgw84wk8gk80c.harshone.dev/api/auth`

## Overview

The TV Authentication API provides QR code-based authentication for Android TV applications. Users scan a QR code displayed on their TV using their authenticated mobile app, enabling seamless login without typing credentials.

## Quick Start

### Flow Diagram

```
┌─────────────┐                    ┌─────────────┐                    ┌─────────────┐
│ Android TV  │                    │   Backend   │                    │   Mobile    │
└──────┬──────┘                    └──────┬──────┘                    └──────┬──────┘
       │                                  │                                  │
       │ 1. POST /create-session          │                                  │
       │─────────────────────────────────>│                                  │
       │                                  │                                  │
       │ 2. Return QR Code URL            │                                  │
       │<─────────────────────────────────│                                  │
       │                                  │                                  │
       │ 3. Display QR Code               │                                  │
       │                                  │                                  │
       │ 4. Poll GET /check-status        │                                  │
       │─────────────────────────────────>│                                  │
       │                                  │                                  │
       │ 5. Return: not authenticated     │                                  │
       │<─────────────────────────────────│                                  │
       │                                  │                                  │
       │         (User scans QR code)     │      6. POST /link (with JWT)    │
       │                                  │<─────────────────────────────────│
       │                                  │                                  │
       │                                  │ 7. Link session & return success │
       │                                  │─────────────────────────────────>│
       │                                  │                                  │
       │ 8. Poll GET /check-status        │                                  │
       │─────────────────────────────────>│                                  │
       │                                  │                                  │
       │ 9. Return: authenticated + token │                                  │
       │<─────────────────────────────────│                                  │
       │                                  │                                  │
       │ 10. Navigate to Home             │                                  │
       │                                  │                                  │
```

### Authentication States

| State | Description | TV Action | Mobile Action |
|-------|-------------|-----------|---------------|
| **Pending** | Session created, waiting for link | Display QR, poll status | - |
| **Scanning** | User scanning QR with mobile | Continue polling | Extract sessionId |
| **Linking** | Mobile app linking session | Continue polling | Call /link endpoint |
| **Authenticated** | Session linked successfully | Receive token, navigate | Show success |
| **Expired** | Session timed out (10 min) | Show timeout, retry | - |

---

## Endpoints

### 1. Create TV Auth Session

Creates a new authentication session and returns a QR code URL.

#### Endpoint
```
POST /auth/tv/create-session
```

#### Authentication
None (public endpoint)

#### Query Parameters

| Parameter | Type | Required | Validation | Description |
|-----------|------|----------|------------|-------------|
| `sessionId` | string (UUID) | ✅ Yes | Valid UUID v4 | Client-generated unique session identifier |

#### Request Example

```bash
curl -X POST \
  'http://localhost:5000/api/auth/tv/create-session?sessionId=550e8400-e29b-41d4-a716-446655440000' \
  -H 'Content-Type: application/json'
```

```javascript
// JavaScript/TypeScript
const sessionId = crypto.randomUUID(); // or uuid.v4()
const response = await fetch(
  `${BASE_URL}/auth/tv/create-session?sessionId=${sessionId}`,
  { method: 'POST' }
);
const data = await response.json();
```

```kotlin
// Kotlin (Android)
val sessionId = UUID.randomUUID().toString()
val response = apiService.createTvAuthSession(sessionId)
```

#### Success Response

**Status Code:** `200 OK`

```json
{
  "success": true,
  "data": {
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "authUrl": "watchtime://tv-auth?sessionId=550e8400-e29b-41d4-a716-446655440000",
    "expiresAt": 1701363000000
  }
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `success` | boolean | Indicates if request was successful |
| `data.sessionId` | string (UUID) | The created session identifier |
| `data.authUrl` | string | Deep link URL to encode in QR code |
| `data.expiresAt` | number | Unix timestamp in milliseconds when session expires |

#### Error Responses

**400 Bad Request - Missing Session ID**
```json
{
  "success": false,
  "error": "Session ID is required"
}
```

**400 Bad Request - Invalid UUID Format**
```json
{
  "success": false,
  "error": "Invalid session ID format"
}
```

**409 Conflict - Duplicate Session ID**
```json
{
  "success": false,
  "error": "Session ID already exists"
}
```

**500 Internal Server Error**
```json
{
  "success": false,
  "error": "Failed to create auth session"
}
```

#### Implementation Notes

- Session expires 10 minutes after creation
- Use the `authUrl` to generate a QR code for display
- Session ID must be unique and should be generated client-side
- Store `sessionId` for subsequent status polling

#### QR Code Generation

```kotlin
// Kotlin - Generate QR Code from authUrl
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

fun generateQRCode(content: String, size: Int = 512): Bitmap {
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

// Usage
val qrBitmap = generateQRCode(response.data.authUrl)
imageView.setImageBitmap(qrBitmap)
```

---

### 2. Check TV Auth Status

Polls the authentication status of a TV session. Should be called every 5 seconds by TV app.

#### Endpoint
```
GET /auth/tv/check-status
```

#### Authentication
None (public endpoint)

#### Query Parameters

| Parameter | Type | Required | Validation | Description |
|-----------|------|----------|------------|-------------|
| `sessionId` | string (UUID) | ✅ Yes | Valid UUID v4 | The TV session identifier to check |

#### Request Example

```bash
curl -X GET \
  'http://localhost:5000/api/auth/tv/check-status?sessionId=550e8400-e29b-41d4-a716-446655440000' \
  -H 'Content-Type: application/json'
```

```javascript
// JavaScript/TypeScript - Polling implementation
const pollInterval = 5000; // 5 seconds

async function pollAuthStatus(sessionId) {
  const maxAttempts = 120; // 10 minutes max (5s * 120 = 600s)
  let attempts = 0;
  
  while (attempts < maxAttempts) {
    const response = await fetch(
      `${BASE_URL}/auth/tv/check-status?sessionId=${sessionId}`
    );
    const data = await response.json();
    
    if (data.success && data.data.authenticated) {
      // Session authenticated!
      return data.data.token;
    }
    
    await new Promise(resolve => setTimeout(resolve, pollInterval));
    attempts++;
  }
  
  throw new Error('Authentication timeout');
}
```

```kotlin
// Kotlin (Android TV) - Polling with Coroutines
suspend fun pollAuthStatus(sessionId: String): String? {
    val maxAttempts = 120
    var attempts = 0
    
    while (attempts < maxAttempts) {
        try {
            val response = apiService.checkTvAuthStatus(sessionId)
            
            if (response.success && response.data.authenticated) {
                return response.data.token
            }
            
            delay(5000) // Wait 5 seconds
            attempts++
        } catch (e: Exception) {
            Log.e("Auth", "Error polling status", e)
            delay(5000)
            attempts++
        }
    }
    
    return null // Timeout
}
```

#### Success Response - Not Authenticated

**Status Code:** `200 OK`

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

#### Success Response - Authenticated

**Status Code:** `200 OK`

```json
{
  "success": true,
  "data": {
    "authenticated": true,
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJhdXRoZW50aWNhdGVkIiwiZXhwIjoxNzAxMzY2NjAwLCJzdWIiOiJhMWIyYzNkNC1lNWY2LTQ3ODktMGFiYy1kZWYxMjM0NTY3ODkiLCJlbWFpbCI6InVzZXJAZXhhbXBsZS5jb20iLCJwaG9uZSI6IiIsImFwcF9tZXRhZGF0YSI6e30sInVzZXJfbWV0YWRhdGEiOnt9LCJyb2xlIjoiYXV0aGVudGljYXRlZCJ9.signature",
    "userId": "a1b2c3d4-e5f6-4789-0abc-def123456789"
  }
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `success` | boolean | Indicates if request was successful |
| `data.authenticated` | boolean | `true` if session has been linked, `false` otherwise |
| `data.token` | string \| null | JWT authentication token (only when authenticated) |
| `data.userId` | string \| null | User ID of authenticated user (only when authenticated) |

#### Error Responses

**400 Bad Request - Missing Session ID**
```json
{
  "success": false,
  "error": "Session ID is required"
}
```

**400 Bad Request - Invalid UUID Format**
```json
{
  "success": false,
  "error": "Invalid session ID format"
}
```

**404 Not Found - Session Not Found or Expired**
```json
{
  "success": false,
  "error": "Session not found or expired"
}
```

**500 Internal Server Error**
```json
{
  "success": false,
  "error": "Failed to check auth status"
}
```

#### Implementation Notes

- Poll every 5 seconds (don't poll too frequently to avoid rate limiting)
- Stop polling when `authenticated` becomes `true`
- Implement timeout after 10 minutes (120 attempts × 5 seconds)
- Store the `token` securely when authenticated
- Handle network errors gracefully and continue polling

---

### 3. Link Mobile to TV

Links an authenticated mobile user's account to a TV session.

#### Endpoint
```
POST /auth/tv/link
```

#### Authentication
✅ **Required** - JWT Bearer token

#### Headers

| Header | Value | Required | Description |
|--------|-------|----------|-------------|
| `Authorization` | Bearer {token} | ✅ Yes | Supabase/Firebase JWT token |
| `Content-Type` | application/json | ✅ Yes | Request body format |

#### Request Body

```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Body Parameters:**

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| `sessionId` | string (UUID) | ✅ Yes | Valid UUID v4 | The TV session ID from scanned QR code |

#### Request Example

```bash
curl -X POST \
  'http://localhost:5000/api/auth/tv/link' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...' \
  -d '{
    "sessionId": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

```javascript
// JavaScript/TypeScript
const linkTvSession = async (sessionId, authToken) => {
  const response = await fetch(`${BASE_URL}/auth/tv/link`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${authToken}`
    },
    body: JSON.stringify({ sessionId })
  });
  
  return await response.json();
};
```

```kotlin
// Kotlin (Android Mobile)
suspend fun linkMobileToTv(sessionId: String, authToken: String): TvAuthLinkResponse {
    return apiService.linkMobileToTv(
        authorization = "Bearer $authToken",
        request = TvAuthLinkRequest(sessionId)
    )
}

// Usage after scanning QR code
val qrContent = "watchtime://tv-auth?sessionId=550e8400-..."
val sessionId = Uri.parse(qrContent).getQueryParameter("sessionId")

viewModelScope.launch {
    try {
        val token = authRepository.getCurrentUserToken()
        val response = linkMobileToTv(sessionId!!, token)
        
        if (response.success) {
            _linkState.value = LinkState.Success
        }
    } catch (e: Exception) {
        _linkState.value = LinkState.Error(e.message)
    }
}
```

#### Success Response

**Status Code:** `200 OK`

```json
{
  "success": true,
  "data": null,
  "message": "TV device linked successfully"
}
```

#### Error Responses

**400 Bad Request - Missing Session ID**
```json
{
  "success": false,
  "error": "Session ID is required"
}
```

**400 Bad Request - Invalid Session ID Format**
```json
{
  "success": false,
  "error": "Invalid session ID format"
}
```

**400 Bad Request - Expired Session**
```json
{
  "success": false,
  "error": "Invalid or expired session"
}
```

**401 Unauthorized - Missing User Authentication**
```json
{
  "success": false,
  "error": "User authentication required"
}
```

**401 Unauthorized - Missing Authorization Token**
```json
{
  "success": false,
  "error": "Authorization token is required"
}
```

**404 Not Found - Session Not Found**
```json
{
  "success": false,
  "error": "Session not found"
}
```

**409 Conflict - Already Authenticated**
```json
{
  "success": false,
  "error": "Session already authenticated"
}
```

**500 Internal Server Error**
```json
{
  "success": false,
  "error": "Failed to link device"
}
```

#### Implementation Notes

- Mobile app must be authenticated with valid JWT token
- Extract `sessionId` from scanned QR code deep link
- Session can only be linked once (cannot re-link)
- Session must not be expired (< 10 minutes old)
- After successful linking, TV app will receive token via check-status

---

## Complete Integration Examples

### Android TV App

```kotlin
// ViewModel
class TvAuthViewModel @Inject constructor(
    private val tvAuthApi: TvAuthApiService
) : ViewModel() {

    private val _authState = MutableStateFlow<TvAuthState>(TvAuthState.Initial)
    val authState: StateFlow<TvAuthState> = _authState.asStateFlow()
    
    private var pollingJob: Job? = null
    
    fun startTvAuth() {
        viewModelScope.launch {
            try {
                // Step 1: Create session
                val sessionId = UUID.randomUUID().toString()
                val response = tvAuthApi.createTvAuthSession(sessionId)
                
                // Step 2: Generate and display QR code
                val qrBitmap = generateQRCode(response.data.authUrl)
                _authState.value = TvAuthState.ShowingQRCode(qrBitmap, sessionId)
                
                // Step 3: Start polling
                startPolling(sessionId)
                
            } catch (e: Exception) {
                _authState.value = TvAuthState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    private fun startPolling(sessionId: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            var attempts = 0
            val maxAttempts = 120 // 10 minutes
            
            while (attempts < maxAttempts) {
                try {
                    val response = tvAuthApi.checkTvAuthStatus(sessionId)
                    
                    if (response.success && response.data.authenticated) {
                        // Authentication successful!
                        val token = response.data.token!!
                        saveAuthToken(token)
                        _authState.value = TvAuthState.Authenticated(token)
                        break
                    }
                    
                } catch (e: Exception) {
                    Log.e("TvAuth", "Polling error", e)
                }
                
                delay(5000)
                attempts++
            }
            
            if (attempts >= maxAttempts) {
                _authState.value = TvAuthState.Error("Authentication timeout")
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}

sealed class TvAuthState {
    object Initial : TvAuthState()
    data class ShowingQRCode(val qrBitmap: Bitmap, val sessionId: String) : TvAuthState()
    data class Authenticated(val token: String) : TvAuthState()
    data class Error(val message: String) : TvAuthState()
}
```

### Android Mobile App

```kotlin
// ViewModel
class QrScanViewModel @Inject constructor(
    private val tvAuthApi: TvAuthApiService,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _scanState = MutableStateFlow<QrScanState>(QrScanState.Scanning)
    val scanState: StateFlow<QrScanState> = _scanState.asStateFlow()
    
    fun onQrCodeScanned(qrContent: String) {
        viewModelScope.launch {
            try {
                _scanState.value = QrScanState.Processing
                
                // Parse QR code
                val uri = Uri.parse(qrContent)
                val sessionId = uri.getQueryParameter("sessionId")
                
                if (sessionId == null) {
                    _scanState.value = QrScanState.Error("Invalid QR code")
                    return@launch
                }
                
                // Get current user's token
                val user = authRepository.getCurrentUser()
                if (user == null) {
                    _scanState.value = QrScanState.Error("You must be signed in")
                    return@launch
                }
                
                val token = user.token
                
                // Link session
                val response = tvAuthApi.linkMobileToTv(
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
}

sealed class QrScanState {
    object Scanning : QrScanState()
    object Processing : QrScanState()
    object Success : QrScanState()
    data class Error(val message: String) : QrScanState()
}
```

---

## Error Handling

### Common Error Scenarios

| Error | Cause | Solution |
|-------|-------|----------|
| "Session ID is required" | Query parameter missing | Include sessionId in request |
| "Invalid session ID format" | Not a valid UUID | Use UUID v4 format |
| "Session ID already exists" | Duplicate session creation | Generate new UUID |
| "Session not found or expired" | Session > 10 minutes old | Create new session |
| "User authentication required" | No JWT token provided | Include Authorization header |
| "Session already authenticated" | Trying to re-link session | Normal - session already linked |
| "Failed to create auth session" | Server/database error | Retry request |

### Retry Logic

```javascript
async function createSessionWithRetry(sessionId, maxRetries = 3) {
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await createTvAuthSession(sessionId);
    } catch (error) {
      if (i === maxRetries - 1) throw error;
      await new Promise(resolve => setTimeout(resolve, 1000 * (i + 1)));
    }
  }
}
```

---

## Rate Limiting

| Endpoint | Limit | Window | Notes |
|----------|-------|--------|-------|
| `/create-session` | 10 requests | Per 15 minutes | Per IP address |
| `/check-status` | 180 requests | Per 15 minutes | Normal polling = 180 requests |
| `/link` | 20 requests | Per 15 minutes | Per authenticated user |

---

## Security Considerations

### Best Practices

1. **Session ID Generation**
   - Always use UUID v4 (cryptographically secure)
   - Never reuse session IDs
   - Generate client-side to avoid race conditions

2. **Token Storage**
   - Store JWT tokens securely (encrypted storage on TV)
   - Never log or expose tokens in plain text
   - Implement token refresh if needed

3. **QR Code Security**
   - Sessions expire after 10 minutes
   - One-time use only (cannot re-link)
   - Display expiration countdown on TV

4. **Network Security**
   - Always use HTTPS in production
   - Validate SSL certificates
   - Implement certificate pinning for enhanced security

5. **Error Messages**
   - Don't expose sensitive information in errors
   - Log detailed errors server-side only
   - Show user-friendly messages to users

---

## Testing

### Manual Testing with cURL

```bash
# Test 1: Create session
SESSION_ID=$(uuidgen)
curl -X POST "http://localhost:5000/api/auth/tv/create-session?sessionId=$SESSION_ID" | jq

# Test 2: Check status (not authenticated)
curl "http://localhost:5000/api/auth/tv/check-status?sessionId=$SESSION_ID" | jq

# Test 3: Link session (requires JWT)
JWT_TOKEN="your_jwt_token_here"
curl -X POST "http://localhost:5000/api/auth/tv/link" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d "{\"sessionId\": \"$SESSION_ID\"}" | jq

# Test 4: Check status (authenticated)
curl "http://localhost:5000/api/auth/tv/check-status?sessionId=$SESSION_ID" | jq
```

### Automated Test Script

Save as `test_tv_auth.sh` and run:

```bash
#!/bin/bash
SESSION_ID=$(uuidgen)
echo "Testing with session: $SESSION_ID"

echo "1. Creating session..."
curl -s -X POST "http://localhost:5000/api/auth/tv/create-session?sessionId=$SESSION_ID" | jq

echo "2. Checking initial status..."
curl -s "http://localhost:5000/api/auth/tv/check-status?sessionId=$SESSION_ID" | jq

echo "Enter JWT token to continue:"
read JWT_TOKEN

echo "3. Linking session..."
curl -s -X POST "http://localhost:5000/api/auth/tv/link" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d "{\"sessionId\": \"$SESSION_ID\"}" | jq

echo "4. Checking final status..."
curl -s "http://localhost:5000/api/auth/tv/check-status?sessionId=$SESSION_ID" | jq
```

---

## Changelog

### Version 1.0.0 (November 30, 2025)
- Initial release
- Create TV auth session endpoint
- Check TV auth status endpoint
- Link mobile to TV endpoint
- 10-minute session expiry
- Automatic session cleanup every 15 minutes

---

## Support

For questions or issues:
- Review the [QR Auth Implementation Guide](./QR_AUTH_IMPLEMENTATION_GUIDE.md)
- Check [SERVER_API_REQUIREMENTS.md](./SERVER_API_REQUIREMENTS.md)
- See [TV_AUTH_SETUP.md](../TV_AUTH_SETUP.md) for setup instructions

---

**© 2025 WatchTime. All rights reserved.**
