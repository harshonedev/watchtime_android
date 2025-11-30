# WatchTime Backend API Documentation

Base URL: `http://localhost:5000` (development)

## Table of Contents
- [Authentication](#authentication)
- [TV Authentication (QR Code Flow)](#tv-authentication-qr-code-flow)
- [Content Endpoints](#content-endpoints)
- [Collections Endpoints](#collections-endpoints)
- [AI Endpoints](#ai-endpoints)
- [User Endpoints](#user-endpoints)

## Authentication

All protected endpoints require a Bearer token in the Authorization header:
```
Authorization: Bearer <supabase_jwt_token>
```

## TV Authentication (QR Code Flow)

The TV authentication system allows Android TV users to authenticate by scanning a QR code with their authenticated mobile app. This provides a seamless login experience without typing credentials on TV.

### Authentication Flow

```
1. TV App → Create Session → Display QR Code
2. Mobile App → Scan QR Code → Extract Session ID  
3. Mobile App → Link Session → Authenticate TV
4. TV App → Poll Status → Receive Auth Token
5. TV App → Authenticated → Access User Account
```

### Create TV Auth Session

Create a new authentication session for TV login.

```http
POST /api/auth/tv/create-session?sessionId={uuid}
```

**Authentication:** None (public endpoint)

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| sessionId | UUID | Yes | Client-generated unique session identifier |

**Request Example:**
```bash
curl -X POST "http://localhost:5000/api/auth/tv/create-session?sessionId=550e8400-e29b-41d4-a716-446655440000"
```

**Success Response (200 OK):**
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
- `sessionId` - The unique session identifier
- `authUrl` - Deep link URL to encode in QR code
- `expiresAt` - Unix timestamp (milliseconds) when session expires

**Error Responses:**

*400 Bad Request - Missing or Invalid Session ID:*
```json
{
  "success": false,
  "error": "Session ID is required"
}
```

```json
{
  "success": false,
  "error": "Invalid session ID format"
}
```

*409 Conflict - Duplicate Session:*
```json
{
  "success": false,
  "error": "Session ID already exists"
}
```

*500 Internal Server Error:*
```json
{
  "success": false,
  "error": "Failed to create auth session"
}
```

**Usage Notes:**
- Session expires after 10 minutes
- Session ID must be a valid UUID v4
- The `authUrl` should be encoded in a QR code for mobile scanning
- TV app should call this endpoint once on the login screen

---

### Check TV Auth Status

Poll the authentication status of a TV session. The TV app should call this endpoint every 5 seconds.

```http
GET /api/auth/tv/check-status?sessionId={uuid}
```

**Authentication:** None (public endpoint)

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| sessionId | UUID | Yes | The TV session identifier to check |

**Request Example:**
```bash
curl -X GET "http://localhost:5000/api/auth/tv/check-status?sessionId=550e8400-e29b-41d4-a716-446655440000"
```

**Success Response - Not Authenticated (200 OK):**
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

**Success Response - Authenticated (200 OK):**
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

**Response Fields:**
- `authenticated` - Boolean indicating if session has been linked
- `token` - JWT authentication token (null if not authenticated)
- `userId` - User ID of authenticated user (null if not authenticated)

**Error Responses:**

*400 Bad Request - Missing or Invalid Session ID:*
```json
{
  "success": false,
  "error": "Session ID is required"
}
```

```json
{
  "success": false,
  "error": "Invalid session ID format"
}
```

*404 Not Found - Session Not Found or Expired:*
```json
{
  "success": false,
  "error": "Session not found or expired"
}
```

*500 Internal Server Error:*
```json
{
  "success": false,
  "error": "Failed to check auth status"
}
```

**Usage Notes:**
- TV app should poll this endpoint every 5 seconds
- Polling should stop when `authenticated` becomes `true`
- Once authenticated, save the `token` for subsequent API calls
- Sessions expire after 10 minutes if not linked

---

### Link Mobile to TV

Link an authenticated mobile user to a TV session. Called by the mobile app after scanning the QR code.

```http
POST /api/auth/tv/link
```

**Authentication:** Required (JWT Bearer token)

**Headers:**
```
Authorization: Bearer <supabase_jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Body Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| sessionId | UUID | Yes | The TV session ID from scanned QR code |

**Request Example:**
```bash
curl -X POST "http://localhost:5000/api/auth/tv/link" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{"sessionId": "550e8400-e29b-41d4-a716-446655440000"}'
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": null,
  "message": "TV device linked successfully"
}
```

**Error Responses:**

*400 Bad Request - Missing Session ID:*
```json
{
  "success": false,
  "error": "Session ID is required"
}
```

*400 Bad Request - Invalid Session ID Format:*
```json
{
  "success": false,
  "error": "Invalid session ID format"
}
```

*400 Bad Request - Expired Session:*
```json
{
  "success": false,
  "error": "Invalid or expired session"
}
```

*401 Unauthorized - Missing Authentication:*
```json
{
  "success": false,
  "error": "User authentication required"
}
```

```json
{
  "success": false,
  "error": "Authorization token is required"
}
```

*404 Not Found - Session Not Found:*
```json
{
  "success": false,
  "error": "Session not found"
}
```

*409 Conflict - Already Authenticated:*
```json
{
  "success": false,
  "error": "Session already authenticated"
}
```

*500 Internal Server Error:*
```json
{
  "success": false,
  "error": "Failed to link device"
}
```

**Usage Notes:**
- Mobile app must be authenticated with a valid JWT token
- Extract `sessionId` from scanned QR code deep link
- Session can only be linked once
- After successful linking, TV app will receive the token via check-status endpoint

---

### TV Auth Implementation Guide

**For Android TV App:**

1. Generate a unique session ID:
   ```kotlin
   val sessionId = UUID.randomUUID().toString()
   ```

2. Create session and get QR code URL:
   ```kotlin
   val response = api.createTvAuthSession(sessionId)
   val qrCodeUrl = response.data.authUrl
   ```

3. Display QR code to user:
   ```kotlin
   val qrBitmap = generateQRCode(qrCodeUrl)
   imageView.setImageBitmap(qrBitmap)
   ```

4. Poll for authentication (every 5 seconds):
   ```kotlin
   viewModelScope.launch {
       while (!isAuthenticated) {
           val status = api.checkTvAuthStatus(sessionId)
           if (status.data.authenticated) {
               val token = status.data.token
               // Save token and navigate to home
               saveAuthToken(token)
               navigateToHome()
               break
           }
           delay(5000)
       }
   }
   ```

**For Android Mobile App:**

1. Scan QR code and extract session ID:
   ```kotlin
   val qrContent = "watchtime://tv-auth?sessionId=550e8400-..."
   val sessionId = Uri.parse(qrContent).getQueryParameter("sessionId")
   ```

2. Link mobile user to TV session:
   ```kotlin
   val token = getCurrentUserToken() // From Supabase/Firebase
   val response = api.linkMobileToTv(
       authorization = "Bearer $token",
       sessionId = sessionId
   )
   ```

3. Show success message to user

**Session Lifecycle:**
- **Created:** Session is created with status `pending`
- **Expires in:** 10 minutes from creation
- **Linked:** Mobile app authenticates, session becomes `authenticated`
- **Cleanup:** Expired sessions are automatically deleted every 15 minutes

**Security Considerations:**
- Sessions are single-use (cannot be re-linked after authentication)
- Sessions expire after 10 minutes to prevent stale QR codes
- UUID validation prevents injection attacks
- JWT tokens required for linking ensure only authenticated users can link devices
- Background cleanup job prevents database bloat

## Content Endpoints

### Search Content
```http
GET /api/content/search?query={search_term}&type={type}&page={page}
```

**Parameters:**
- `query` (required): Search term
- `type` (optional): `movie`, `tv`, or `multi` (default: `multi`)
- `page` (optional): Page number (default: 1)

**Response:**
```json
{
  "success": true,
  "data": {
    "results": [...],
    "total_results": 1000,
    "total_pages": 50,
    "page": 1
  }
}
```

### Get Movie Details
```http
GET /api/content/movie/{id}
```

### Get TV Details
```http
GET /api/content/tv/{id}
```

### Get Popular Content
```http
GET /api/content/popular?type={type}&page={page}
```

### Get Trending Content
```http
GET /api/content/trending?type={type}&timeWindow={timeWindow}&page={page}
```

**Parameters:**
- `type`: `all`, `movie`, or `tv` (default: `all`)
- `timeWindow`: `day` or `week` (default: `day`)

### Get Genres
```http
GET /api/content/genres?type={type}
```

### Get Recommendations
```http
GET /api/content/{type}/{id}/recommendations?page={page}
```

## Collections Endpoints
*All endpoints require authentication*

All collection endpoints require the `Authorization` header with a valid Bearer token.

### Get User Collections
Retrieve all collections for the authenticated user.

```http
GET /api/collections
```

**Headers:**
```
Authorization: Bearer <supabase_jwt_token>
```

**Query Parameters:** None

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "user_id": "123e4567-e89b-12d3-a456-426614174000",
      "name": "Watch Later",
      "description": "Movies and shows you want to watch",
      "is_default": true,
      "is_public": false,
      "created_at": "2024-01-01T00:00:00.000Z",
      "updated_at": "2024-01-01T00:00:00.000Z",
      "collection_items": [
        {
          "id": "item-uuid",
          "collection_id": "550e8400-e29b-41d4-a716-446655440000",
          "content_id": "content-uuid",
          "tmdb_id": 550,
          "media_type": "movie",
          "added_at": "2024-01-02T00:00:00.000Z",
          "notes": null,
          "content_metadata": {
            "id": "content-uuid",
            "tmdb_id": 550,
            "media_type": "movie",
            "title": "Fight Club",
            "overview": "A ticking-time-bomb insomniac...",
            "poster_path": "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
            "backdrop_path": "/fCayJrkfRaCRCTh8GqN30f8oyQF.jpg",
            "release_date": "1999-10-15",
            "genres": [{"id": 18, "name": "Drama"}],
            "vote_average": 8.4,
            "vote_count": 26280,
            "popularity": 61.416
          }
        }
      ]
    }
  ]
}
```

**Error Response (500 Internal Server Error):**
```json
{
  "success": false,
  "error": "Failed to fetch collections"
}
```

---

### Create Collection
Create a new custom collection for the authenticated user.

```http
POST /api/collections
```

**Headers:**
```
Authorization: Bearer <supabase_jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "My Sci-Fi Collection",
  "description": "Best sci-fi movies and shows",
  "is_public": false
}
```

**Body Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| name | string | Yes | Collection name (must be non-empty after trimming) |
| description | string | No | Collection description (trimmed, can be null) |
| is_public | boolean | No | Whether collection is publicly visible (default: false) |

**Success Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "user_id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "My Sci-Fi Collection",
    "description": "Best sci-fi movies and shows",
    "is_default": false,
    "is_public": false,
    "created_at": "2024-01-03T00:00:00.000Z",
    "updated_at": "2024-01-03T00:00:00.000Z"
  }
}
```

**Error Responses:**

*400 Bad Request - Missing name:*
```json
{
  "success": false,
  "error": "Collection name is required"
}
```

*500 Internal Server Error:*
```json
{
  "success": false,
  "error": "Failed to create collection"
}
```

---

### Get Collection by ID
Retrieve a specific collection with all its items.

```http
GET /api/collections/{id}
```

**Headers:**
```
Authorization: Bearer <supabase_jwt_token>
```

**URL Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | UUID | Yes | Collection ID |

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "user_id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Watch Later",
    "description": "Movies and shows you want to watch",
    "is_default": true,
    "is_public": false,
    "created_at": "2024-01-01T00:00:00.000Z",
    "updated_at": "2024-01-01T00:00:00.000Z",
    "collection_items": [
      {
        "id": "item-uuid",
        "tmdb_id": 550,
        "media_type": "movie",
        "added_at": "2024-01-02T00:00:00.000Z",
        "content_metadata": {
          "title": "Fight Club",
          "poster_path": "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
          "release_date": "1999-10-15",
          "vote_average": 8.4
        }
      }
    ]
  }
}
```

**Error Response (500 Internal Server Error):**
```json
{
  "success": false,
  "error": "Failed to fetch collection"
}
```

---

### Update Collection
Update an existing collection's details.

```http
PUT /api/collections/{id}
```

**Headers:**
```
Authorization: Bearer <supabase_jwt_token>
Content-Type: application/json
```

**URL Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | UUID | Yes | Collection ID |

**Request Body:**
```json
{
  "name": "Updated Collection Name",
  "description": "Updated description",
  "is_public": true
}
```

**Body Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| name | string | No | Updated collection name |
| description | string | No | Updated collection description |
| is_public | boolean | No | Updated visibility status |

**Note:** The following fields are automatically removed and cannot be updated:
- `id`
- `user_id`
- `created_at`
- `is_default` (default collections cannot have their status changed)

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "user_id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Updated Collection Name",
    "description": "Updated description",
    "is_default": false,
    "is_public": true,
    "created_at": "2024-01-03T00:00:00.000Z",
    "updated_at": "2024-01-03T10:00:00.000Z"
  }
}
```

**Error Response (500 Internal Server Error):**
```json
{
  "success": false,
  "error": "Failed to update collection"
}
```

---

### Delete Collection
Delete a collection and all its items.

```http
DELETE /api/collections/{id}
```

**Headers:**
```
Authorization: Bearer <supabase_jwt_token>
```

**URL Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | UUID | Yes | Collection ID |

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Collection deleted successfully"
}
```

**Error Responses:**

*400 Bad Request - Attempting to delete default collection:*
```json
{
  "success": false,
  "error": "Cannot delete default collections"
}
```

*500 Internal Server Error:*
```json
{
  "success": false,
  "error": "Failed to delete collection"
}
```

---

### Add Item to Collection
Add a movie or TV show to a collection.

```http
POST /api/collections/{id}/items
```

**Headers:**
```
Authorization: Bearer <supabase_jwt_token>
Content-Type: application/json
```

**URL Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | UUID | Yes | Collection ID |

**Request Body:**
```json
{
  "tmdb_id": 550,
  "media_type": "movie",
  "title": "Fight Club",
  "original_title": "Fight Club",
  "overview": "A ticking-time-bomb insomniac and a slippery soap salesman channel primal male aggression...",
  "poster_path": "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
  "backdrop_path": "/fCayJrkfRaCRCTh8GqN30f8oyQF.jpg",
  "release_date": "1999-10-15",
  "genres": [
    {"id": 18, "name": "Drama"}
  ],
  "vote_average": 8.4,
  "vote_count": 26280,
  "popularity": 61.416,
  "adult": false,
  "notes": "Optional personal notes about this item"
}
```

**Body Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| tmdb_id | integer | Yes | TMDB ID of the content |
| media_type | string | Yes | Type of content: "movie" or "tv" |
| title | string | Yes | Title of the content |
| original_title | string | No | Original title in original language |
| overview | string | No | Content description/synopsis |
| poster_path | string | No | Path to poster image |
| backdrop_path | string | No | Path to backdrop image |
| release_date | string (date) | No | Release date (YYYY-MM-DD format) |
| genres | array | No | Array of genre objects with id and name |
| vote_average | number | No | Average rating (0-10) |
| vote_count | integer | No | Number of votes |
| popularity | number | No | Popularity score |
| adult | boolean | No | Whether content is adult-rated |
| notes | string | No | Personal notes about the item |

**Success Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": "item-uuid",
    "collection_id": "550e8400-e29b-41d4-a716-446655440000",
    "content_id": "content-uuid",
    "tmdb_id": 550,
    "media_type": "movie",
    "added_at": "2024-01-04T00:00:00.000Z",
    "notes": "Optional personal notes about this item"
  }
}
```

**Error Responses:**

*400 Bad Request - Missing required fields:*
```json
{
  "success": false,
  "error": "Missing required fields: tmdb_id, media_type"
}
```

*409 Conflict - Item already exists:*
```json
{
  "success": false,
  "error": "Item already exists in collection"
}
```

*500 Internal Server Error:*
```json
{
  "success": false,
  "error": "Failed to add item to collection"
}
```

---

### Remove Item from Collection
Remove a specific item from a collection.

```http
DELETE /api/collections/{id}/items/{itemId}
```

**Headers:**
```
Authorization: Bearer <supabase_jwt_token>
```

**URL Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | UUID | Yes | Collection ID |
| itemId | UUID | Yes | Collection item ID (not TMDB ID) |

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Item removed from collection successfully"
}
```

**Error Response (500 Internal Server Error):**
```json
{
  "success": false,
  "error": "Failed to remove item from collection"
}
```

---

### Create Default Collections
Create default collections (Watch Later, Already Watched) for a new user.

```http
POST /api/collections/setup/defaults
```

**Headers:**
```
Authorization: Bearer <supabase_jwt_token>
```

**Request Body:** None

**Success Response (201 Created):**
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid-1",
      "user_id": "123e4567-e89b-12d3-a456-426614174000",
      "name": "Watch Later",
      "description": "Movies and shows you want to watch",
      "is_default": true,
      "is_public": false,
      "created_at": "2024-01-01T00:00:00.000Z",
      "updated_at": "2024-01-01T00:00:00.000Z"
    },
    {
      "id": "uuid-2",
      "user_id": "123e4567-e89b-12d3-a456-426614174000",
      "name": "Already Watched",
      "description": "Movies and shows you've already seen",
      "is_default": true,
      "is_public": false,
      "created_at": "2024-01-01T00:00:00.000Z",
      "updated_at": "2024-01-01T00:00:00.000Z"
    }
  ]
}
```

**Error Response (500 Internal Server Error):**
```json
{
  "success": false,
  "error": "Failed to create default collections"
}
```

---

### Collections - Common Notes

**Authentication:**
- All endpoints require a valid Supabase JWT token in the Authorization header
- Users can only access and modify their own collections
- Row Level Security (RLS) policies enforce data isolation

**Validation:**
- Collection names must be unique per user
- Default collections cannot be deleted
- Items in a collection must be unique (same tmdb_id and media_type cannot be added twice)

**Data Types:**
- All IDs are UUIDs
- Dates are in ISO 8601 format
- media_type must be either "movie" or "tv"

## AI Endpoints

### AI Agent Chat (Main Endpoint)
```http
POST /api/ai/chat
```

**Description:** Main AI Agent endpoint with structured JSON responses and tool calling support.

**Headers:**
- `Authorization: Bearer <token>` (optional - better with authentication)

**Body:**
```json
{
  "message": "Add Interstellar to my watch later and tell me where I can watch it",
  "preferences": {
    "favorite_genres": ["sci-fi", "drama"],
    "language": "en"
  }
}
```

**Response:**
```json
{
  "success": true,
  "reply": "I found Interstellar and added it to your Watch Later list! I also checked where you can stream it.",
  "results": [
    {
      "tool": "searchContent",
      "success": true,
      "data": {
        "query": "Interstellar",
        "topResult": {
          "id": 157336,
          "title": "Interstellar",
          "year": 2014,
          "mediaType": "movie"
        },
        "message": "Found Interstellar (2014) - TMDB ID: 157336"
      }
    },
    {
      "tool": "addToCollection",
      "success": true,
      "data": {
        "message": "Successfully added \"Interstellar\" to collection"
      }
    },
    {
      "tool": "getStreamingAvailability",
      "success": true,
      "data": {
        "streamingServices": [
          {
            "provider": "Netflix",
            "type": "subscription",
            "price": "Free with subscription"
          }
        ]
      }
    }
  ],
  "actions": [
    { "tool": "searchContent", "params": { "query": "Interstellar", "mediaType": "movie" } },
    { "tool": "addToCollection", "params": { "titleId": 157336, "collectionName": "Watch Later" } },
    { "tool": "getStreamingAvailability", "params": { "titleId": 157336 } }
  ]
}
```

### Search-Only Request Example

**Request:**
```json
{
  "message": "Search for The Matrix movies"
}
```

**Response:**
```json
{
  "success": true,
  "reply": "I found several results for The Matrix. The top result is The Matrix (1999) with TMDB ID 603. Would you like me to add it to a collection or get more information about it?",
  "results": [
    {
      "tool": "searchContent",
      "success": true,
      "data": {
        "query": "The Matrix",
        "results": [
          {
            "id": 603,
            "title": "The Matrix",
            "year": 1999,
            "mediaType": "movie",
            "overview": "Set in the 22nd century, The Matrix tells the story of a computer hacker...",
            "voteAverage": 8.2
          }
        ],
        "totalResults": 84,
        "message": "Found 10 results for \"The Matrix\". Top result: \"The Matrix\" (1999) - TMDB ID: 603"
      }
    }
  ],
  "actions": [
    { "tool": "searchContent", "params": { "query": "The Matrix", "mediaType": "multi" } }
  ]
}
```

### Available AI Tools
The AI Agent can execute these tools based on user requests:

1. **searchContent** - Search for movies/TV shows by title (used automatically when user mentions titles)
2. **findContentId** - Find TMDB ID for a specific title
3. **addToCollection** - Add movies/TV to user collections (requires TMDB ID from search)
4. **getStreamingAvailability** - Find where to watch content (requires TMDB ID from search)
5. **getCastInfo** - Get cast and crew information (requires TMDB ID from search)
6. **getSeasonRatings** - Get TV show season information (requires TMDB ID from search)

### Smart Title Resolution
When users mention movie or TV show titles by name, the AI automatically:
1. **Searches** for the title using TMDB API
2. **Finds** the most relevant match by popularity
3. **Uses** the TMDB ID for subsequent operations

**Example Workflow:**
- User: "Add Interstellar to my watchlist"
- AI: Searches "Interstellar" → Finds TMDB ID 157336 → Adds to collection
- Result: Movie successfully added with full metadata

### Get AI Recommendations
```http
POST /api/ai/recommendations
```

**Body:**
```json
{
  "message": "I want sci-fi movies like Blade Runner",
  "preferences": {
    "genres": ["sci-fi", "thriller"],
    "rating_min": 7.0
  }
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "aiResponse": "Based on your interest in Blade Runner...",
    "recommendations": [
      {
        "title": "Ghost in the Shell",
        "year": 1995,
        "description": "...",
        "reason": "..."
      }
    ]
  }
}
```

### Get Similar Content
```http
POST /api/ai/similar
```

**Body:**
```json
{
  "title": "Interstellar",
  "mediaType": "movie"
}
```

### Legacy Chat
```http
POST /api/ai/chat/legacy
```

**Body:**
```json
{
  "message": "What are the best Christopher Nolan movies?",
  "conversationHistory": [
    {"role": "user", "content": "Previous message"},
    {"role": "assistant", "content": "Previous response"}
  ]
}
```

## User Endpoints
*Requires Authentication*

### Get User Profile
```http
GET /api/user/profile
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "user-uuid",
    "email": "user@example.com",
    "profile": {
      "full_name": "John Doe",
      "avatar_url": "...",
      "preferences": {...}
    }
  }
}
```

### Update User Profile
```http
PUT /api/user/profile
```

**Body:**
```json
{
  "full_name": "John Doe",
  "preferences": {
    "favorite_genres": ["sci-fi", "action"],
    "language": "en"
  }
}
```

### Setup User Account
```http
POST /api/user/setup
```

Creates default collections and profile for new users.

### Delete User Account
```http
DELETE /api/user/account
```

## Error Responses

All endpoints return errors in this format:
```json
{
  "success": false,
  "error": "Error message",
  "timestamp": "2024-01-01T00:00:00.000Z"
}
```

## Status Codes

- `200` - Success
- `201` - Created
- `400` - Bad Request
- `401` - Unauthorized
- `403` - Forbidden
- `404` - Not Found
- `409` - Conflict
- `429` - Too Many Requests
- `500` - Internal Server Error

## Rate Limiting

API requests are limited to prevent abuse:
- 100 requests per 15-minute window per IP address
- Authenticated users may have higher limits

## Content Types

All API endpoints accept and return `application/json` unless otherwise specified.

## CORS

The API supports CORS and accepts requests from configured origins.

## Health Check

```http
GET /health
```

Returns server health status and uptime information.
