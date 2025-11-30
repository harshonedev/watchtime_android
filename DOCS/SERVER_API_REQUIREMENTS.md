# WatchTime Server API Requirements & Documentation

## Overview
This document provides comprehensive API specifications for the WatchTime backend server. The APIs support both Android mobile app and Android TV app functionalities including user authentication, TV-Mobile QR-based authentication, content management, collections, and user profiles.

**Base URL:** `https://boc4cgg8sgkkgw84wk8gk80c.harshone.dev/api/`

**API Version:** 1.0

**Date:** November 30, 2025

---

## Table of Contents
1. [Authentication](#1-authentication)
2. [TV Authentication (QR Code Flow)](#2-tv-authentication-qr-code-flow)
3. [User Management](#3-user-management)
4. [Collections Management](#4-collections-management)
5. [Content Endpoints](#5-content-endpoints)
6. [Error Handling](#6-error-handling)
7. [Data Models](#7-data-models)
8. [Implementation Notes](#8-implementation-notes)

---

## 1. Authentication

All protected endpoints require a Bearer token in the Authorization header obtained from your authentication provider (e.g., Supabase, Firebase Auth).

### Headers Format
```http
Authorization: Bearer <jwt_token>
```

### Authentication Flow
1. User authenticates via OAuth provider (Google, etc.)
2. Backend validates JWT token
3. User ID extracted from token for all subsequent requests

---

## 2. TV Authentication (QR Code Flow)

The TV authentication system allows users to link their Android TV app to their mobile account by scanning a QR code.

### Flow Overview
```
┌─────────────┐                    ┌─────────────┐                    ┌─────────────┐
│  Android TV │                    │   Backend   │                    │   Mobile    │
└──────┬──────┘                    └──────┬──────┘                    └──────┬──────┘
       │                                  │                                  │
       │ 1. Create TV Auth Session        │                                  │
       ├─────────────────────────────────>│                                  │
       │                                  │                                  │
       │ 2. Session ID + QR Code URL      │                                  │
       │<─────────────────────────────────┤                                  │
       │                                  │                                  │
       │ 3. Display QR Code               │                                  │
       │ (watchtime://tv-auth?sessionId)  │                                  │
       │                                  │                                  │
       │ 4. Start polling (every 5s)      │                                  │
       ├─────────────────────────────────>│                                  │
       │                                  │                                  │
       │                                  │  5. Scan QR Code                 │
       │                                  │<─────────────────────────────────┤
       │                                  │                                  │
       │                                  │  6. Link Mobile to TV Session    │
       │                                  │<─────────────────────────────────┤
       │                                  │                                  │
       │                                  │  7. Success Response             │
       │                                  ├─────────────────────────────────>│
       │                                  │                                  │
       │ 8. Poll Check Status             │                                  │
       ├─────────────────────────────────>│                                  │
       │                                  │                                  │
       │ 9. Authenticated + Token         │                                  │
       │<─────────────────────────────────┤                                  │
       │                                  │                                  │
       │ 10. TV App Authenticated         │                                  │
       │                                  │                                  │
```

### 2.1. Create TV Auth Session

**Endpoint:** `POST /auth/tv/create-session`

**Description:** Creates a new TV authentication session and returns a unique session ID and auth URL for QR code generation.

**Authentication:** None (public endpoint)

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| sessionId | string (UUID) | Yes | Client-generated unique session identifier |

**Request Example:**
```http
POST /api/auth/tv/create-session?sessionId=550e8400-e29b-41d4-a716-446655440000
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "authUrl": "watchtime://tv-auth?sessionId=550e8400-e29b-41d4-a716-446655440000",
    "expiresAt": 1733020800000
  }
}
```

**Response Fields:**
- `sessionId`: The unique session identifier
- `authUrl`: Deep link URL to be encoded in QR code
- `expiresAt`: Timestamp (milliseconds) when session expires (typically 5-10 minutes from creation)

**Error Responses:**

*400 Bad Request - Invalid Session ID:*
```json
{
  "success": false,
  "error": "Invalid session ID format"
}
```

*500 Internal Server Error:*
```json
{
  "success": false,
  "error": "Failed to create auth session"
}
```

**Implementation Notes:**
- Session should expire after 5-10 minutes
- Store session in database/cache with status: `pending`, `authenticated`, `expired`
- Session ID should be validated as UUID format
- Clean up expired sessions periodically

**Database Schema Example:**
```sql
CREATE TABLE tv_auth_sessions (
  session_id UUID PRIMARY KEY,
  user_id UUID NULL,
  status VARCHAR(20) DEFAULT 'pending',
  auth_token TEXT NULL,
  created_at TIMESTAMP DEFAULT NOW(),
  expires_at TIMESTAMP,
  authenticated_at TIMESTAMP NULL
);
```

---

### 2.2. Check TV Auth Status

**Endpoint:** `GET /auth/tv/check-status`

**Description:** Polls the authentication status of a TV session. Called every 5 seconds by the TV app.

**Authentication:** None (public endpoint - session-based)

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| sessionId | string (UUID) | Yes | The TV session identifier to check |

**Request Example:**
```http
GET /api/auth/tv/check-status?sessionId=550e8400-e29b-41d4-a716-446655440000
```

**Success Response - Not Yet Authenticated (200 OK):**
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
    "userId": "123e4567-e89b-12d3-a456-426614174000"
  }
}
```

**Response Fields:**
- `authenticated`: Boolean indicating if session has been linked
- `token`: JWT authentication token (null if not authenticated)
- `userId`: User ID of authenticated user (null if not authenticated)

**Error Responses:**

*404 Not Found - Invalid or Expired Session:*
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

**Implementation Notes:**
- Return `authenticated: false` for pending sessions
- Once authenticated, return token and user info
- Clean up session after successful authentication (optional)
- Handle concurrent polling gracefully

---

### 2.3. Link Mobile to TV

**Endpoint:** `POST /auth/tv/link`

**Description:** Links a mobile user's authentication to a TV session. Called when user scans QR code on mobile app.

**Authentication:** Required (Bearer token)

**Headers:**
```http
Authorization: Bearer <user_jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Request Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| sessionId | string (UUID) | Yes | The TV session ID from scanned QR code |

**Request Example:**
```http
POST /api/auth/tv/link
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "TV device linked successfully"
}
```

**Error Responses:**

*400 Bad Request - Invalid Session:*
```json
{
  "success": false,
  "message": "Invalid or expired session"
}
```

*401 Unauthorized:*
```json
{
  "success": false,
  "message": "Authentication required"
}
```

*404 Not Found:*
```json
{
  "success": false,
  "message": "Session not found"
}
```

*409 Conflict - Already Linked:*
```json
{
  "success": false,
  "message": "Session already authenticated"
}
```

*500 Internal Server Error:*
```json
{
  "success": false,
  "message": "Failed to link device"
}
```

**Implementation Notes:**
- Extract user ID from JWT token
- Validate session exists and is in `pending` status
- Update session with user ID and auth token
- Set status to `authenticated`
- Store authenticated_at timestamp
- Prevent re-linking already authenticated sessions

---

## 3. User Management

### 3.1. Get User Profile

**Endpoint:** `GET /user/profile`

**Description:** Retrieves the authenticated user's profile information.

**Authentication:** Required

**Headers:**
```http
Authorization: Bearer <jwt_token>
```

**Request Example:**
```http
GET /api/user/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "email": "user@example.com",
    "profile": {
      "id": "profile-uuid",
      "email": "user@example.com",
      "full_name": "John Doe",
      "avatar_url": "https://example.com/avatar.jpg",
      "preferences": {
        "language": "en",
        "theme": "dark"
      },
      "created_at": "2024-01-01T00:00:00.000Z",
      "updated_at": "2024-01-01T00:00:00.000Z"
    }
  }
}
```

**Error Responses:**

*401 Unauthorized:*
```json
{
  "success": false,
  "error": "Authentication required"
}
```

*404 Not Found:*
```json
{
  "success": false,
  "error": "User profile not found"
}
```

*500 Internal Server Error:*
```json
{
  "success": false,
  "error": "Failed to fetch user profile"
}
```

---

### 3.2. Setup User Profile

**Endpoint:** `POST /user/setup`

**Description:** Creates initial user profile and default collections for new users.

**Authentication:** Required

**Headers:**
```http
Authorization: Bearer <jwt_token>
```

**Request Example:**
```http
POST /api/user/setup
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "profile": {
      "id": "profile-uuid",
      "email": "user@example.com",
      "full_name": "John Doe",
      "avatar_url": "https://example.com/avatar.jpg",
      "preferences": {},
      "created_at": "2024-01-01T00:00:00.000Z",
      "updated_at": "2024-01-01T00:00:00.000Z"
    },
    "collections": [
      {
        "id": "collection-uuid-1",
        "user_id": "123e4567-e89b-12d3-a456-426614174000",
        "name": "Watch Later",
        "description": "Movies and shows to watch later",
        "is_default": true,
        "is_public": false,
        "created_at": "2024-01-01T00:00:00.000Z",
        "updated_at": "2024-01-01T00:00:00.000Z"
      },
      {
        "id": "collection-uuid-2",
        "user_id": "123e4567-e89b-12d3-a456-426614174000",
        "name": "Already Watched",
        "description": "Movies and shows I've already watched",
        "is_default": true,
        "is_public": false,
        "created_at": "2024-01-01T00:00:00.000Z",
        "updated_at": "2024-01-01T00:00:00.000Z"
      }
    ]
  }
}
```

**Error Responses:**

*400 Bad Request - Already Setup:*
```json
{
  "success": false,
  "error": "User profile already exists"
}
```

*401 Unauthorized:*
```json
{
  "success": false,
  "error": "Authentication required"
}
```

*500 Internal Server Error:*
```json
{
  "success": false,
  "error": "Failed to setup user profile"
}
```

**Implementation Notes:**
- Create user profile if doesn't exist
- Create two default collections: "Watch Later" and "Already Watched"
- Extract user info from JWT token (email, name, avatar)

---

## 4. Collections Management

Collections allow users to organize movies and TV shows into custom lists.

### 4.1. Get All Collections

**Endpoint:** `GET /collections`

**Description:** Retrieves all collections for the authenticated user, including collection items and metadata.

**Authentication:** Required

**Headers:**
```http
Authorization: Bearer <jwt_token>
```

**Query Parameters:** None

**Request Example:**
```http
GET /api/collections
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "user_id": "123e4567-e89b-12d3-a456-426614174000",
      "name": "Watch Later",
      "description": "Movies and shows to watch later",
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
          "notes": "Must watch this classic",
          "content_metadata": {
            "id": "content-uuid",
            "tmdb_id": 550,
            "media_type": "movie",
            "title": "Fight Club",
            "original_title": "Fight Club",
            "overview": "A ticking-time-bomb insomniac...",
            "poster_path": "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
            "backdrop_path": "/fCayJrkfRaCRCTh8GqN30f8oyQF.jpg",
            "release_date": "1999-10-15",
            "genres": [
              {
                "id": 18,
                "name": "Drama"
              }
            ],
            "vote_average": 8.4,
            "vote_count": 26280,
            "popularity": 61.416,
            "adult": false
          }
        }
      ]
    }
  ]
}
```

**Error Responses:**

*401 Unauthorized:*
```json
{
  "success": false,
  "error": "Authentication required"
}
```

*500 Internal Server Error:*
```json
{
  "success": false,
  "error": "Failed to fetch collections"
}
```

---

### 4.2. Get Collection by ID

**Endpoint:** `GET /collections/{id}`

**Description:** Retrieves a specific collection with all its items.

**Authentication:** Required

**Headers:**
```http
Authorization: Bearer <jwt_token>
```

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | string (UUID) | Yes | Collection ID |

**Request Example:**
```http
GET /api/collections/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "user_id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Watch Later",
    "description": "Movies and shows to watch later",
    "is_default": true,
    "is_public": false,
    "created_at": "2024-01-01T00:00:00.000Z",
    "updated_at": "2024-01-01T00:00:00.000Z",
    "collection_items": [...]
  }
}
```

**Error Responses:**

*401 Unauthorized:*
```json
{
  "success": false,
  "error": "Authentication required"
}
```

*403 Forbidden:*
```json
{
  "success": false,
  "error": "Access denied to this collection"
}
```

*404 Not Found:*
```json
{
  "success": false,
  "error": "Collection not found"
}
```

---

### 4.3. Create Collection

**Endpoint:** `POST /collections`

**Description:** Creates a new custom collection for the authenticated user.

**Authentication:** Required

**Headers:**
```http
Authorization: Bearer <jwt_token>
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

**Request Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | string | Yes | Collection name (non-empty after trim) |
| description | string | No | Collection description |
| is_public | boolean | No | Public visibility (default: false) |

**Request Example:**
```http
POST /api/collections
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "name": "My Sci-Fi Collection",
  "description": "Best sci-fi movies and shows",
  "is_public": false
}
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": "new-collection-uuid",
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

*400 Bad Request - Invalid Input:*
```json
{
  "success": false,
  "error": "Collection name is required"
}
```

*401 Unauthorized:*
```json
{
  "success": false,
  "error": "Authentication required"
}
```

---

### 4.4. Update Collection

**Endpoint:** `PUT /collections/{id}`

**Description:** Updates an existing collection's metadata.

**Authentication:** Required

**Headers:**
```http
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | string (UUID) | Yes | Collection ID to update |

**Request Body:**
```json
{
  "name": "Updated Collection Name",
  "description": "Updated description",
  "is_public": true
}
```

**Request Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | string | No | Updated collection name |
| description | string | No | Updated description (can be null) |
| is_public | boolean | No | Updated public visibility |

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "user_id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Updated Collection Name",
    "description": "Updated description",
    "is_default": false,
    "is_public": true,
    "created_at": "2024-01-01T00:00:00.000Z",
    "updated_at": "2024-01-03T10:30:00.000Z"
  }
}
```

**Error Responses:**

*400 Bad Request:*
```json
{
  "success": false,
  "error": "Invalid update data"
}
```

*401 Unauthorized:*
```json
{
  "success": false,
  "error": "Authentication required"
}
```

*403 Forbidden - Default Collection:*
```json
{
  "success": false,
  "error": "Cannot modify default collections"
}
```

*404 Not Found:*
```json
{
  "success": false,
  "error": "Collection not found"
}
```

---

### 4.5. Delete Collection

**Endpoint:** `DELETE /collections/{id}`

**Description:** Deletes a custom collection. Default collections cannot be deleted.

**Authentication:** Required

**Headers:**
```http
Authorization: Bearer <jwt_token>
```

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | string (UUID) | Yes | Collection ID to delete |

**Request Example:**
```http
DELETE /api/collections/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": null
}
```

**Error Responses:**

*401 Unauthorized:*
```json
{
  "success": false,
  "error": "Authentication required"
}
```

*403 Forbidden - Default Collection:*
```json
{
  "success": false,
  "error": "Cannot delete default collections"
}
```

*404 Not Found:*
```json
{
  "success": false,
  "error": "Collection not found"
}
```

---

### 4.6. Add Item to Collection

**Endpoint:** `POST /collections/{id}/items`

**Description:** Adds a movie or TV show to a collection with metadata.

**Authentication:** Required

**Headers:**
```http
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | string (UUID) | Yes | Collection ID |

**Request Body:**
```json
{
  "tmdb_id": 550,
  "media_type": "movie",
  "title": "Fight Club",
  "original_title": "Fight Club",
  "overview": "A ticking-time-bomb insomniac...",
  "poster_path": "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
  "backdrop_path": "/fCayJrkfRaCRCTh8GqN30f8oyQF.jpg",
  "release_date": "1999-10-15",
  "genres": [
    {
      "id": 18,
      "name": "Drama"
    }
  ],
  "vote_average": 8.4,
  "vote_count": 26280,
  "popularity": 61.416,
  "adult": false,
  "notes": "Must watch this classic"
}
```

**Request Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| tmdb_id | integer | Yes | TMDB content ID |
| media_type | string | Yes | "movie" or "tv" |
| title | string | Yes | Content title |
| original_title | string | No | Original title |
| overview | string | No | Content description |
| poster_path | string | No | Poster image path |
| backdrop_path | string | No | Backdrop image path |
| release_date | string | No | Release date (YYYY-MM-DD) |
| genres | array | No | Array of genre objects |
| vote_average | number | No | TMDB rating (0-10) |
| vote_count | integer | No | Number of votes |
| popularity | number | No | Popularity score |
| adult | boolean | No | Adult content flag |
| notes | string | No | User notes about the item |

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
    "added_at": "2024-01-02T00:00:00.000Z",
    "notes": "Must watch this classic",
    "content_metadata": {
      "id": "content-uuid",
      "tmdb_id": 550,
      "media_type": "movie",
      "title": "Fight Club",
      "original_title": "Fight Club",
      "overview": "A ticking-time-bomb insomniac...",
      "poster_path": "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
      "backdrop_path": "/fCayJrkfRaCRCTh8GqN30f8oyQF.jpg",
      "release_date": "1999-10-15",
      "genres": [
        {
          "id": 18,
          "name": "Drama"
        }
      ],
      "vote_average": 8.4,
      "vote_count": 26280,
      "popularity": 61.416,
      "adult": false
    }
  }
}
```

**Error Responses:**

*400 Bad Request - Missing Required Fields:*
```json
{
  "success": false,
  "error": "tmdb_id, media_type, and title are required"
}
```

*401 Unauthorized:*
```json
{
  "success": false,
  "error": "Authentication required"
}
```

*404 Not Found:*
```json
{
  "success": false,
  "error": "Collection not found"
}
```

*409 Conflict - Duplicate Item:*
```json
{
  "success": false,
  "error": "Item already exists in collection"
}
```

**Implementation Notes:**
- Create or update content_metadata table
- Link to collection_items with foreign key
- Prevent duplicate items (same tmdb_id + media_type in same collection)

---

### 4.7. Remove Item from Collection

**Endpoint:** `DELETE /collections/{id}/items/{itemId}`

**Description:** Removes a specific item from a collection.

**Authentication:** Required

**Headers:**
```http
Authorization: Bearer <jwt_token>
```

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | string (UUID) | Yes | Collection ID |
| itemId | string (UUID) | Yes | Collection item ID to remove |

**Request Example:**
```http
DELETE /api/collections/550e8400-e29b-41d4-a716-446655440000/items/item-uuid
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": null
}
```

**Error Responses:**

*401 Unauthorized:*
```json
{
  "success": false,
  "error": "Authentication required"
}
```

*404 Not Found:*
```json
{
  "success": false,
  "error": "Collection or item not found"
}
```

---

### 4.8. Create Default Collections

**Endpoint:** `POST /collections/setup/defaults`

**Description:** Creates default collections (Watch Later, Already Watched) for a user. Usually called during user setup.

**Authentication:** Required

**Headers:**
```http
Authorization: Bearer <jwt_token>
```

**Request Example:**
```http
POST /api/collections/setup/defaults
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "data": [
    {
      "id": "collection-uuid-1",
      "user_id": "123e4567-e89b-12d3-a456-426614174000",
      "name": "Watch Later",
      "description": "Movies and shows to watch later",
      "is_default": true,
      "is_public": false,
      "created_at": "2024-01-01T00:00:00.000Z",
      "updated_at": "2024-01-01T00:00:00.000Z"
    },
    {
      "id": "collection-uuid-2",
      "user_id": "123e4567-e89b-12d3-a456-426614174000",
      "name": "Already Watched",
      "description": "Movies and shows I've already watched",
      "is_default": true,
      "is_public": false,
      "created_at": "2024-01-01T00:00:00.000Z",
      "updated_at": "2024-01-01T00:00:00.000Z"
    }
  ]
}
```

**Error Responses:**

*400 Bad Request - Already Exists:*
```json
{
  "success": false,
  "error": "Default collections already exist"
}
```

*401 Unauthorized:*
```json
{
  "success": false,
  "error": "Authentication required"
}
```

---

## 5. Content Endpoints

These endpoints can proxy to TMDB or implement caching/custom logic.

### 5.1. Search Content

**Endpoint:** `GET /content/search`

**Description:** Searches for movies and TV shows.

**Authentication:** Optional (can be public or authenticated)

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| query | string | Yes | Search term |
| type | string | No | "movie", "tv", or "multi" (default: "multi") |
| page | integer | No | Page number (default: 1) |

**Request Example:**
```http
GET /api/content/search?query=inception&type=multi&page=1
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "results": [
      {
        "id": 27205,
        "media_type": "movie",
        "title": "Inception",
        "overview": "Cobb, a skilled thief...",
        "poster_path": "/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg",
        "backdrop_path": "/s3TBrRGB1iav7gFOCNx3H31MoES.jpg",
        "release_date": "2010-07-15",
        "vote_average": 8.3,
        "vote_count": 31000,
        "popularity": 75.2
      }
    ],
    "total_results": 42,
    "total_pages": 3,
    "page": 1
  }
}
```

---

### 5.2. Get Popular Content

**Endpoint:** `GET /content/popular`

**Description:** Gets popular movies or TV shows.

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| type | string | No | "movie" or "tv" (default: "movie") |
| page | integer | No | Page number (default: 1) |

---

### 5.3. Get Trending Content

**Endpoint:** `GET /content/trending`

**Description:** Gets trending content.

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| type | string | No | "all", "movie", or "tv" (default: "all") |
| timeWindow | string | No | "day" or "week" (default: "day") |
| page | integer | No | Page number (default: 1) |

---

## 6. Error Handling

### Standard Error Response Format

All error responses follow this structure:

```json
{
  "success": false,
  "error": "Error message description",
  "message": "Additional context (optional)"
}
```

### HTTP Status Codes

| Code | Description | Usage |
|------|-------------|-------|
| 200 | OK | Successful GET, PUT, DELETE |
| 201 | Created | Successful POST creating resource |
| 400 | Bad Request | Invalid input data |
| 401 | Unauthorized | Missing or invalid authentication |
| 403 | Forbidden | Authenticated but not authorized |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Resource already exists |
| 500 | Internal Server Error | Server-side error |
| 503 | Service Unavailable | Temporary unavailability |

---

## 7. Data Models

### User Profile
```typescript
interface UserProfile {
  id: string;                    // UUID
  email: string;
  full_name: string;
  avatar_url: string;
  preferences: Record<string, string>;
  created_at: string;            // ISO 8601
  updated_at: string;            // ISO 8601
}
```

### Collection
```typescript
interface Collection {
  id: string;                    // UUID
  user_id: string;               // UUID
  name: string;
  description: string | null;
  is_default: boolean;
  is_public: boolean;
  created_at: string;            // ISO 8601
  updated_at: string;            // ISO 8601
  collection_items?: CollectionItem[];
}
```

### Collection Item
```typescript
interface CollectionItem {
  id: string;                    // UUID
  collection_id: string;         // UUID
  content_id: string;            // UUID
  tmdb_id: number;
  media_type: 'movie' | 'tv';
  added_at: string;              // ISO 8601
  notes: string | null;
  content_metadata?: ContentMetadata;
}
```

### Content Metadata
```typescript
interface ContentMetadata {
  id: string;                    // UUID
  tmdb_id: number;
  media_type: 'movie' | 'tv';
  title: string;
  original_title: string | null;
  overview: string | null;
  poster_path: string | null;
  backdrop_path: string | null;
  release_date: string | null;   // YYYY-MM-DD
  genres: Genre[] | null;
  vote_average: number | null;
  vote_count: number | null;
  popularity: number | null;
  adult: boolean;
}
```

### Genre
```typescript
interface Genre {
  id: number;
  name: string;
}
```

### TV Auth Session
```typescript
interface TvAuthSession {
  session_id: string;            // UUID
  user_id: string | null;        // UUID
  status: 'pending' | 'authenticated' | 'expired';
  auth_token: string | null;     // JWT
  created_at: Date;
  expires_at: Date;
  authenticated_at: Date | null;
}
```

---

## 8. Implementation Notes

### Database Schema Recommendations

#### Users Table
```sql
CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email VARCHAR(255) UNIQUE NOT NULL,
  full_name VARCHAR(255),
  avatar_url TEXT,
  preferences JSONB DEFAULT '{}',
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);
```

#### Collections Table
```sql
CREATE TABLE collections (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  is_default BOOLEAN DEFAULT false,
  is_public BOOLEAN DEFAULT false,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  CONSTRAINT unique_user_collection_name UNIQUE(user_id, name)
);

CREATE INDEX idx_collections_user_id ON collections(user_id);
```

#### Content Metadata Table
```sql
CREATE TABLE content_metadata (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tmdb_id INTEGER NOT NULL,
  media_type VARCHAR(10) NOT NULL,
  title VARCHAR(500) NOT NULL,
  original_title VARCHAR(500),
  overview TEXT,
  poster_path VARCHAR(255),
  backdrop_path VARCHAR(255),
  release_date DATE,
  genres JSONB,
  vote_average DECIMAL(3,1),
  vote_count INTEGER,
  popularity DECIMAL(10,3),
  adult BOOLEAN DEFAULT false,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  CONSTRAINT unique_tmdb_content UNIQUE(tmdb_id, media_type)
);

CREATE INDEX idx_content_metadata_tmdb ON content_metadata(tmdb_id, media_type);
```

#### Collection Items Table
```sql
CREATE TABLE collection_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  collection_id UUID NOT NULL REFERENCES collections(id) ON DELETE CASCADE,
  content_id UUID NOT NULL REFERENCES content_metadata(id) ON DELETE CASCADE,
  tmdb_id INTEGER NOT NULL,
  media_type VARCHAR(10) NOT NULL,
  added_at TIMESTAMP DEFAULT NOW(),
  notes TEXT,
  CONSTRAINT unique_collection_content UNIQUE(collection_id, content_id)
);

CREATE INDEX idx_collection_items_collection ON collection_items(collection_id);
CREATE INDEX idx_collection_items_content ON collection_items(content_id);
```

#### TV Auth Sessions Table
```sql
CREATE TABLE tv_auth_sessions (
  session_id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id) ON DELETE CASCADE,
  status VARCHAR(20) DEFAULT 'pending',
  auth_token TEXT,
  created_at TIMESTAMP DEFAULT NOW(),
  expires_at TIMESTAMP NOT NULL,
  authenticated_at TIMESTAMP,
  CHECK (status IN ('pending', 'authenticated', 'expired'))
);

CREATE INDEX idx_tv_auth_sessions_status ON tv_auth_sessions(status, expires_at);
```

### Security Considerations

1. **JWT Validation**: Always validate JWT tokens from your auth provider (Supabase/Firebase)
2. **User Isolation**: Ensure users can only access their own resources
3. **Rate Limiting**: Implement rate limiting on all endpoints
4. **Session Expiry**: Clean up expired TV auth sessions regularly
5. **Input Validation**: Sanitize all user inputs
6. **HTTPS Only**: Enforce HTTPS in production
7. **CORS**: Configure proper CORS policies

### Caching Strategy

1. **Content Metadata**: Cache TMDB data for 24 hours
2. **User Collections**: Cache for 5 minutes, invalidate on updates
3. **TV Auth Sessions**: Use Redis with TTL matching session expiry

### Background Jobs

1. **Cleanup Expired Sessions**: Run every 15 minutes
   ```sql
   DELETE FROM tv_auth_sessions 
   WHERE status = 'pending' AND expires_at < NOW();
   ```

2. **Update Content Metadata**: Refresh popular content metadata daily

### Performance Optimization

1. Use database indexes on foreign keys and frequently queried fields
2. Implement pagination for all list endpoints
3. Use connection pooling for database
4. Consider CDN for static content metadata
5. Implement Redis caching for frequently accessed data

### Testing Endpoints

Use these curl commands to test:

```bash
# Create TV Auth Session
curl -X POST "http://localhost:5000/api/auth/tv/create-session?sessionId=$(uuidgen)"

# Check Auth Status
curl "http://localhost:5000/api/auth/tv/check-status?sessionId=YOUR_SESSION_ID"

# Link Mobile to TV
curl -X POST "http://localhost:5000/api/auth/tv/link" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"sessionId": "YOUR_SESSION_ID"}'

# Get Collections
curl "http://localhost:5000/api/collections" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Appendix: Deep Link Configuration

### Android App Deep Link Setup

Add to `app/src/main/AndroidManifest.xml`:

```xml
<activity android:name=".MainActivity">
    <!-- Existing intent filters -->
    
    <!-- Deep link for TV Auth -->
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

### QR Code Format

The QR code should encode:
```
watchtime://tv-auth?sessionId=550e8400-e29b-41d4-a716-446655440000
```

---

**Document Version:** 1.0  
**Last Updated:** November 30, 2025  
**Contact:** Backend API Team

