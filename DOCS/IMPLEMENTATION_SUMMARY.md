# WatchTime Server API Implementation Summary

## 📋 Overview

This document summarizes the complete API requirements for the WatchTime backend server to support both Android Mobile and Android TV applications.

## 📚 Documentation Files Created

### 1. **SERVER_API_REQUIREMENTS.md** (Main Documentation)
   - **Purpose:** Comprehensive API specifications for server implementation
   - **Sections:**
     - Authentication & TV QR-based authentication flow
     - User management endpoints
     - Collections management (CRUD operations)
     - Content endpoints (search, trending, popular)
     - Complete data models and database schemas
     - Security considerations and implementation notes

### 2. **QR_AUTH_IMPLEMENTATION_GUIDE.md** (Developer Guide)
   - **Purpose:** Step-by-step guide for implementing QR code authentication
   - **Covers:**
     - TV app: QR code generation and polling
     - Mobile app: QR code scanning and session linking
     - Deep link configuration
     - Testing procedures
     - Common issues and solutions

### 3. **API_DOCS.md** (Updated)
   - **Purpose:** General API reference (updated to reference SERVER_API_REQUIREMENTS.md)
   - **Content:** Quick reference with links to detailed documentation

## 🔑 Key Features Implemented

### 1. TV Authentication Flow
- **QR Code Based:** TV displays QR code, mobile scans to link accounts
- **Session-based:** Secure session management with expiration
- **Polling Mechanism:** TV polls for authentication status every 5 seconds
- **Deep Links:** Uses `watchtime://tv-auth?sessionId={uuid}` scheme

### 2. User Management
- User profile retrieval
- User setup with default collections
- Token-based authentication (JWT)

### 3. Collections Management
- Create, read, update, delete collections
- Add/remove items from collections
- Default collections: "Watch Later" and "Already Watched"
- Support for custom collections
- Rich metadata storage (TMDB data)

### 4. Content Discovery
- Search movies and TV shows
- Get popular content
- Get trending content
- Proxy to TMDB API

## 🏗️ Architecture

### Authentication Flow
```
TV App → Create Session → Backend → Return QR URL
TV App → Poll Status → Backend → Check if Linked
Mobile → Scan QR → Extract Session ID
Mobile → Link Session → Backend → Update Session
TV App → Poll Status → Backend → Return Auth Token
```

### Data Flow
```
Client Apps → JWT Token → Backend → Validate Token
                                  ↓
                          Access User Data
                                  ↓
                          Return Response
```

## 🛠️ Code Changes Made

### 1. Updated Android App Code

#### AuthRepository Interface
- **File:** `auth/domain/src/main/java/com/app/auth/domain/repository/AuthRepository.kt`
- **Change:** Added `getAuthToken(): String?` method
- **Purpose:** Allow ViewModels to retrieve current authentication token

#### AuthRepositoryImpl
- **File:** `auth/data/src/main/java/com/app/auth/data/repository/AuthRepositoryImpl.kt`
- **Change:** Implemented `getAuthToken()` method
- **Implementation:** Returns token from SupabaseAuthService

#### QrScanViewModel
- **File:** `auth/ui/src/main/java/com/app/auth/ui/viewmodels/QrScanViewModel.kt`
- **Changes:**
  - Updated to use `authRepository.getAuthToken()` instead of placeholder
  - Added proper error handling for network errors
  - Added logging for debugging
  - Improved QR code validation (checks scheme and host)
  - Better error messages for users

#### TvAuthViewModel
- **File:** `auth/tv-ui/src/main/java/com/app/auth/tvui/viewmodels/TvAuthViewModel.kt`
- **Status:** Already properly implemented
- **Features:**
  - QR code generation using ZXing
  - Session polling every 5 seconds
  - Timeout after 5 minutes
  - Error handling for network issues

### 2. API Service Definitions

All API services are already properly defined in:
- `TvAuthApiService.kt` - TV authentication endpoints
- `UserApiService.kt` - User profile endpoints
- `CollectionsApiService.kt` - Collection management endpoints

### 3. Response Models

All response models properly defined in:
- `TvAuthResponse.kt` - TV auth session, status, link responses
- `UserResponse.kt` - User profile and setup responses
- `CollectionResponse.kt` - Collection DTOs and requests

## 📊 Database Schema Requirements

### Tables Needed

1. **users**
   - id (UUID, PK)
   - email (VARCHAR, UNIQUE)
   - full_name (VARCHAR)
   - avatar_url (TEXT)
   - preferences (JSONB)
   - created_at, updated_at (TIMESTAMP)

2. **collections**
   - id (UUID, PK)
   - user_id (UUID, FK)
   - name (VARCHAR)
   - description (TEXT)
   - is_default (BOOLEAN)
   - is_public (BOOLEAN)
   - created_at, updated_at (TIMESTAMP)

3. **content_metadata**
   - id (UUID, PK)
   - tmdb_id (INTEGER)
   - media_type (VARCHAR)
   - title, overview, poster_path, etc.
   - genres (JSONB)
   - created_at, updated_at (TIMESTAMP)
   - UNIQUE(tmdb_id, media_type)

4. **collection_items**
   - id (UUID, PK)
   - collection_id (UUID, FK)
   - content_id (UUID, FK)
   - tmdb_id (INTEGER)
   - media_type (VARCHAR)
   - added_at (TIMESTAMP)
   - notes (TEXT)
   - UNIQUE(collection_id, content_id)

5. **tv_auth_sessions**
   - session_id (UUID, PK)
   - user_id (UUID, FK, nullable)
   - status (VARCHAR: pending/authenticated/expired)
   - auth_token (TEXT, nullable)
   - created_at (TIMESTAMP)
   - expires_at (TIMESTAMP)
   - authenticated_at (TIMESTAMP, nullable)

## 🔐 Security Requirements

1. **JWT Validation:** All protected endpoints must validate JWT tokens
2. **User Isolation:** Users can only access their own resources
3. **Session Expiry:** TV auth sessions expire after 5-10 minutes
4. **HTTPS Only:** Enforce HTTPS in production
5. **Rate Limiting:** Implement rate limiting on all endpoints
6. **Input Validation:** Sanitize all user inputs
7. **CORS Configuration:** Proper CORS policies for allowed origins

## 🚀 Implementation Checklist

### Backend Development

- [ ] Set up database with required tables
- [ ] Implement JWT authentication middleware
- [ ] Create TV auth endpoints:
  - [ ] `POST /auth/tv/create-session`
  - [ ] `GET /auth/tv/check-status`
  - [ ] `POST /auth/tv/link`
- [ ] Create user endpoints:
  - [ ] `GET /user/profile`
  - [ ] `POST /user/setup`
- [ ] Create collection endpoints:
  - [ ] `GET /collections`
  - [ ] `GET /collections/{id}`
  - [ ] `POST /collections`
  - [ ] `PUT /collections/{id}`
  - [ ] `DELETE /collections/{id}`
  - [ ] `POST /collections/{id}/items`
  - [ ] `DELETE /collections/{id}/items/{itemId}`
  - [ ] `POST /collections/setup/defaults`
- [ ] Implement session cleanup job (background task)
- [ ] Add rate limiting
- [ ] Configure CORS
- [ ] Set up Redis for caching (optional but recommended)
- [ ] Deploy to production
- [ ] Configure HTTPS/SSL

### Testing

- [ ] Test TV session creation
- [ ] Test mobile-to-TV linking
- [ ] Test session polling
- [ ] Test session expiration
- [ ] Test user profile endpoints
- [ ] Test collection CRUD operations
- [ ] Test authentication flow end-to-end
- [ ] Load testing for polling endpoints
- [ ] Security testing (token validation, etc.)

### Android App

- [x] QrScanViewModel updated with proper token retrieval
- [x] TvAuthViewModel properly implemented
- [x] AuthRepository with getAuthToken method
- [ ] Test QR code scanning UI
- [ ] Test TV authentication UI
- [ ] Configure deep links in AndroidManifest
- [ ] End-to-end testing with backend

## 📝 API Endpoints Summary

### TV Authentication
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/auth/tv/create-session` | POST | No | Create TV auth session |
| `/auth/tv/check-status` | GET | No | Poll auth status |
| `/auth/tv/link` | POST | Yes | Link mobile to TV |

### User Management
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/user/profile` | GET | Yes | Get user profile |
| `/user/setup` | POST | Yes | Setup new user |

### Collections
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/collections` | GET | Yes | Get all collections |
| `/collections` | POST | Yes | Create collection |
| `/collections/{id}` | GET | Yes | Get collection by ID |
| `/collections/{id}` | PUT | Yes | Update collection |
| `/collections/{id}` | DELETE | Yes | Delete collection |
| `/collections/{id}/items` | POST | Yes | Add item |
| `/collections/{id}/items/{itemId}` | DELETE | Yes | Remove item |
| `/collections/setup/defaults` | POST | Yes | Create defaults |

### Content (Optional - can proxy to TMDB)
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/content/search` | GET | Optional | Search content |
| `/content/popular` | GET | Optional | Get popular |
| `/content/trending` | GET | Optional | Get trending |

## 🔧 Technology Stack Recommendations

### Backend
- **Framework:** Node.js (Express/Fastify) or Python (FastAPI/Django)
- **Database:** PostgreSQL (recommended) or MySQL
- **Cache:** Redis (for sessions and frequently accessed data)
- **Authentication:** JWT validation library
- **ORM:** Prisma (Node.js) or SQLAlchemy (Python)

### Infrastructure
- **Hosting:** Current: harshone.dev
- **SSL:** Required for production
- **Environment:** Docker containers recommended

## 📖 Additional Resources

- **Main API Docs:** [SERVER_API_REQUIREMENTS.md](./SERVER_API_REQUIREMENTS.md)
- **Implementation Guide:** [QR_AUTH_IMPLEMENTATION_GUIDE.md](./QR_AUTH_IMPLEMENTATION_GUIDE.md)
- **API Reference:** [API_DOCS.md](./API_DOCS.md)
- **TV Implementation:** [TV_IMPLEMENTATION.md](./TV_IMPLEMENTATION.md)

## 🎯 Next Steps

1. **Review Documentation:**
   - Read SERVER_API_REQUIREMENTS.md thoroughly
   - Understand the TV authentication flow
   - Review database schema requirements

2. **Set Up Development Environment:**
   - Install required dependencies
   - Set up PostgreSQL database
   - Configure environment variables

3. **Implement Backend:**
   - Start with TV auth endpoints (simplest)
   - Then user management
   - Finally collections management

4. **Test Integration:**
   - Test with Android TV app
   - Test with Android Mobile app
   - Verify QR code flow works end-to-end

5. **Deploy:**
   - Deploy to production server
   - Configure SSL/HTTPS
   - Set up monitoring and logging

## 💡 Tips for Implementation

1. **Start Simple:** Implement TV auth first as it's the most critical
2. **Use Transactions:** For collection operations that modify multiple tables
3. **Index Properly:** Add indexes on foreign keys and frequently queried columns
4. **Cache Wisely:** Cache TMDB data and user collections
5. **Log Everything:** Comprehensive logging helps debug issues
6. **Test Thoroughly:** Test each endpoint before moving to the next
7. **Security First:** Always validate tokens and sanitize inputs

## 📞 Support

For questions or issues:
- Review the documentation files in this repository
- Check common issues in QR_AUTH_IMPLEMENTATION_GUIDE.md
- Ensure all required fields are included in requests
- Verify JWT token is valid and not expired

---

**Last Updated:** November 30, 2025  
**Documentation Version:** 1.0  
**Status:** Ready for Implementation ✅

