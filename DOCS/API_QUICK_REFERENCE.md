# WatchTime API Quick Reference

**Base URL:** `https://boc4cgg8sgkkgw84wk8gk80c.harshone.dev/api`  
**Environment:** Production  
**Version:** 1.0.0

## 📋 Endpoints Summary

### TV Authentication (QR Code Flow)

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/auth/tv/create-session` | POST | ❌ | Create TV auth session, get QR URL |
| `/auth/tv/check-status` | GET | ❌ | Poll session authentication status |
| `/auth/tv/link` | POST | ✅ | Link mobile user to TV session |

### User Management

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/user/profile` | GET | ✅ | Get user profile |
| `/user/profile` | PUT | ✅ | Update user profile |
| `/user/setup` | POST | ✅ | Setup new user account |
| `/user/account` | DELETE | ✅ | Delete user account |

### Collections

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/collections` | GET | ✅ | Get all user collections |
| `/collections` | POST | ✅ | Create new collection |
| `/collections/{id}` | GET | ✅ | Get collection by ID |
| `/collections/{id}` | PUT | ✅ | Update collection |
| `/collections/{id}` | DELETE | ✅ | Delete collection |
| `/collections/{id}/items` | POST | ✅ | Add item to collection |
| `/collections/{id}/items/{itemId}` | DELETE | ✅ | Remove item from collection |
| `/collections/setup/defaults` | POST | ✅ | Create default collections |

### Content Discovery

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/content/search` | GET | ⭕ | Search movies/TV shows |
| `/content/movie/{id}` | GET | ⭕ | Get movie details |
| `/content/tv/{id}` | GET | ⭕ | Get TV show details |
| `/content/popular` | GET | ⭕ | Get popular content |
| `/content/trending` | GET | ⭕ | Get trending content |
| `/content/genres` | GET | ⭕ | Get genre list |
| `/content/{type}/{id}/recommendations` | GET | ⭕ | Get recommendations |

### AI Features

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/ai/chat` | POST | ⭕ | AI agent conversation |
| `/ai/recommendations` | POST | ⭕ | AI-powered recommendations |

**Legend:**  
✅ Required | ❌ Not Required | ⭕ Optional (better experience with auth)

---

## 🔑 Authentication

### Header Format
```
Authorization: Bearer <supabase_jwt_token>
```

### Get JWT Token
```bash
# Supabase Auth API
curl -X POST 'YOUR_SUPABASE_URL/auth/v1/token?grant_type=password' \
  -H "apikey: YOUR_ANON_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

---

## 🎯 TV Authentication Flow

### Quick Start Guide

**Step 1: TV App Creates Session**
```bash
SESSION_ID=$(uuidgen)
curl -X POST "https://api.example.com/api/auth/tv/create-session?sessionId=$SESSION_ID"
```

**Response:**
```json
{
  "success": true,
  "data": {
    "authUrl": "watchtime://tv-auth?sessionId=...",
    "expiresAt": 1701363000000
  }
}
```

**Step 2: TV App Polls Status**
```bash
# Poll every 5 seconds
curl "https://api.example.com/api/auth/tv/check-status?sessionId=$SESSION_ID"
```

**Step 3: Mobile App Links Session**
```bash
curl -X POST "https://api.example.com/api/auth/tv/link" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"sessionId": "$SESSION_ID"}'
```

**Step 4: TV App Receives Token**
```json
{
  "success": true,
  "data": {
    "authenticated": true,
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "a1b2c3d4-..."
  }
}
```

---

## 📊 Common Request Examples

### Create Collection
```bash
curl -X POST "https://api.example.com/api/collections" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Watchlist",
    "description": "Movies to watch",
    "is_public": false
  }'
```

### Add Item to Collection
```bash
curl -X POST "https://api.example.com/api/collections/{collection_id}/items" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "tmdb_id": 550,
    "media_type": "movie",
    "title": "Fight Club",
    "poster_path": "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg"
  }'
```

### Search Content
```bash
curl "https://api.example.com/api/content/search?query=Inception&type=movie&page=1"
```

### AI Chat
```bash
curl -X POST "https://api.example.com/api/ai/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Recommend me some sci-fi movies like Interstellar"
  }'
```

---

## ⚠️ Error Codes

| Code | Meaning | Common Causes |
|------|---------|---------------|
| 200 | OK | Request successful |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Invalid input, missing required fields |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Access denied to resource |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Duplicate resource, already exists |
| 429 | Too Many Requests | Rate limit exceeded |
| 500 | Internal Server Error | Server error, try again |

---

## 🔒 Security Notes

- ✅ Always use HTTPS in production
- ✅ Store JWT tokens securely (encrypted storage)
- ✅ Never log tokens in plain text
- ✅ Sessions expire after 10 minutes
- ✅ Validate all UUIDs before sending
- ✅ Implement retry logic for network errors

---

## 📱 Mobile/TV SDK Usage

### Kotlin (Android)
```kotlin
// Retrofit API Service
interface WatchTimeApi {
    @POST("auth/tv/create-session")
    suspend fun createTvSession(
        @Query("sessionId") sessionId: String
    ): TvAuthResponse
    
    @GET("auth/tv/check-status")
    suspend fun checkStatus(
        @Query("sessionId") sessionId: String
    ): TvAuthStatusResponse
    
    @POST("auth/tv/link")
    suspend fun linkSession(
        @Header("Authorization") auth: String,
        @Body request: LinkRequest
    ): ApiResponse
}

// Usage
val sessionId = UUID.randomUUID().toString()
val response = api.createTvSession(sessionId)
```

### JavaScript/TypeScript
```typescript
// API Client
class WatchTimeApiClient {
  constructor(private baseUrl: string) {}
  
  async createTvSession(sessionId: string) {
    const response = await fetch(
      `${this.baseUrl}/auth/tv/create-session?sessionId=${sessionId}`,
      { method: 'POST' }
    );
    return await response.json();
  }
  
  async checkStatus(sessionId: string) {
    const response = await fetch(
      `${this.baseUrl}/auth/tv/check-status?sessionId=${sessionId}`
    );
    return await response.json();
  }
  
  async linkSession(sessionId: string, token: string) {
    const response = await fetch(
      `${this.baseUrl}/auth/tv/link`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ sessionId })
      }
    );
    return await response.json();
  }
}
```

---

## 📝 Response Format

All responses follow this structure:

### Success Response
```json
{
  "success": true,
  "data": { /* response data */ },
  "message": "Optional success message"
}
```

### Error Response
```json
{
  "success": false,
  "error": "Error message",
  "timestamp": "2025-11-30T00:00:00.000Z"
}
```

---

## 🎓 Resources

- **Full API Docs:** [API_DOCUMENTATION.md](./API_DOCUMENTATION.md)
- **TV Auth Reference:** [TV_AUTH_API_REFERENCE.md](./TV_AUTH_API_REFERENCE.md)
- **Implementation Guide:** [QR_AUTH_IMPLEMENTATION_GUIDE.md](./QR_AUTH_IMPLEMENTATION_GUIDE.md)
- **Setup Instructions:** [TV_AUTH_SETUP.md](../TV_AUTH_SETUP.md)

---

## 🚀 Quick Test

```bash
# Save as test.sh
#!/bin/bash
SESSION=$(uuidgen)
echo "Session: $SESSION"

# Create
curl -X POST "http://localhost:5000/api/auth/tv/create-session?sessionId=$SESSION" | jq

# Check
curl "http://localhost:5000/api/auth/tv/check-status?sessionId=$SESSION" | jq

# Link (paste your JWT token)
read -p "JWT Token: " TOKEN
curl -X POST "http://localhost:5000/api/auth/tv/link" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"sessionId\": \"$SESSION\"}" | jq
```

---

**Last Updated:** November 30, 2025  
**API Version:** 1.0.0  
**Contact:** [GitHub](https://github.com/harshonedev/watchtime_backend)
