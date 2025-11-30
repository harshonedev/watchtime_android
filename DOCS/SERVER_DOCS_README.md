# 📚 Server API Documentation Index

Welcome to the WatchTime Server API documentation! This guide will help you implement the backend server to support the Android mobile and TV applications.

## 🎯 Quick Start

If you're implementing the server, start here:

1. **[IMPLEMENTATION_SUMMARY.md](./IMPLEMENTATION_SUMMARY.md)** - Overview and checklist ⭐ **START HERE**
2. **[SERVER_API_REQUIREMENTS.md](./SERVER_API_REQUIREMENTS.md)** - Complete API specifications
3. **[QR_AUTH_IMPLEMENTATION_GUIDE.md](./QR_AUTH_IMPLEMENTATION_GUIDE.md)** - QR authentication guide

## 📋 Documentation Files

### Essential Reading

#### 1. [IMPLEMENTATION_SUMMARY.md](./IMPLEMENTATION_SUMMARY.md)
**Read this first!** Contains:
- Overview of all documentation
- Complete implementation checklist
- Database schema summary
- Security requirements
- Quick reference for all endpoints
- Next steps and tips

#### 2. [SERVER_API_REQUIREMENTS.md](./SERVER_API_REQUIREMENTS.md)
**Complete API specifications** including:
- All endpoint definitions with request/response examples
- TV authentication flow (QR code-based)
- User management APIs
- Collections management APIs (CRUD)
- Complete data models (TypeScript interfaces)
- Database schemas (SQL DDL)
- Security considerations
- Implementation notes and best practices
- Testing examples with cURL

#### 3. [QR_AUTH_IMPLEMENTATION_GUIDE.md](./QR_AUTH_IMPLEMENTATION_GUIDE.md)
**Developer implementation guide** for:
- Android TV QR code display
- Android Mobile QR code scanning
- Deep link configuration
- Step-by-step code examples
- Testing procedures
- Troubleshooting common issues

### Reference Documentation

#### 4. [API_DOCS.md](./API_DOCS.md)
Quick API reference (updated to point to SERVER_API_REQUIREMENTS.md)

#### 5. TV-Specific Documentation
- [TV_IMPLEMENTATION.md](./TV_IMPLEMENTATION.md) - Android TV app guide
- [TV_SUMMARY.md](./TV_SUMMARY.md) - TV feature summary
- [TV_ARCHITECTURE_DIAGRAM.md](./TV_ARCHITECTURE_DIAGRAM.md) - Architecture diagrams

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     WatchTime Ecosystem                      │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────┐          ┌──────────────┐                │
│  │ Android TV   │          │ Android      │                │
│  │ App          │          │ Mobile App   │                │
│  └──────┬───────┘          └──────┬───────┘                │
│         │                          │                         │
│         │     ┌───────────────────┐│                        │
│         │     │                   ││                        │
│         └────►│  Backend Server   │◄┘                       │
│               │  (To Implement)   │                         │
│               └─────────┬─────────┘                         │
│                         │                                    │
│         ┌───────────────┼───────────────┐                   │
│         │               │               │                   │
│         ▼               ▼               ▼                   │
│  ┌──────────┐   ┌──────────┐   ┌──────────┐               │
│  │PostgreSQL│   │  Redis   │   │  TMDB    │               │
│  │ Database │   │  Cache   │   │   API    │               │
│  └──────────┘   └──────────┘   └──────────┘               │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

## 🔑 Key Features to Implement

### 1. TV Authentication (QR Code Flow)
- TV displays QR code with session ID
- User scans QR with authenticated mobile app
- TV polls server until authenticated
- **Endpoints:** `/auth/tv/create-session`, `/auth/tv/check-status`, `/auth/tv/link`

### 2. User Management
- User profile retrieval
- Initial user setup with default collections
- **Endpoints:** `/user/profile`, `/user/setup`

### 3. Collections Management
- CRUD operations for user collections
- Add/remove movies and TV shows
- Default collections: "Watch Later" and "Already Watched"
- **Endpoints:** `/collections/*`

### 4. Content Discovery (Optional)
- Can proxy to TMDB or implement caching
- **Endpoints:** `/content/search`, `/content/popular`, `/content/trending`

## 📊 Database Requirements

You'll need these tables:

1. **users** - User profiles
2. **collections** - User collections
3. **content_metadata** - Movie/TV show metadata (from TMDB)
4. **collection_items** - Items in collections (junction table)
5. **tv_auth_sessions** - TV authentication sessions

See [SERVER_API_REQUIREMENTS.md#8-implementation-notes](./SERVER_API_REQUIREMENTS.md#8-implementation-notes) for complete SQL schemas.

## 🚀 Implementation Steps

### Phase 1: Setup (Week 1)
1. Set up development environment
2. Create PostgreSQL database
3. Implement database schema
4. Set up JWT authentication middleware

### Phase 2: Core APIs (Week 2)
1. Implement TV auth endpoints
2. Implement user management endpoints
3. Test authentication flow

### Phase 3: Collections (Week 3)
1. Implement collections CRUD endpoints
2. Implement collection items management
3. Test with mobile and TV apps

### Phase 4: Polish & Deploy (Week 4)
1. Add error handling and validation
2. Implement rate limiting
3. Set up caching (Redis)
4. Deploy to production
5. Configure HTTPS/SSL

## 🔐 Security Checklist

- [ ] JWT token validation on all protected endpoints
- [ ] User isolation (users can only access own data)
- [ ] Input validation and sanitization
- [ ] Rate limiting on all endpoints
- [ ] HTTPS/SSL in production
- [ ] Session expiry for TV auth (5-10 minutes)
- [ ] CORS configuration
- [ ] SQL injection prevention (use parameterized queries)
- [ ] XSS prevention

## 🧪 Testing

### Manual Testing
```bash
# Create TV session
SESSION_ID=$(uuidgen)
curl -X POST "http://localhost:5000/api/auth/tv/create-session?sessionId=$SESSION_ID"

# Link mobile to TV
curl -X POST "http://localhost:5000/api/auth/tv/link" \
  -H "Authorization: Bearer YOUR_JWT" \
  -H "Content-Type: application/json" \
  -d "{\"sessionId\": \"$SESSION_ID\"}"

# Check status
curl "http://localhost:5000/api/auth/tv/check-status?sessionId=$SESSION_ID"
```

### Integration Testing
- Test with actual Android TV app
- Test with actual Android Mobile app
- Verify QR code scanning works end-to-end

## 📦 Technology Recommendations

### Option 1: Node.js + Express
```
- Express.js for API server
- Prisma for ORM
- PostgreSQL for database
- Redis for caching
- JWT for authentication
```

### Option 2: Python + FastAPI
```
- FastAPI for API server
- SQLAlchemy for ORM
- PostgreSQL for database
- Redis for caching
- PyJWT for authentication
```

### Option 3: Other
Any backend framework that can:
- Handle REST APIs
- Validate JWT tokens
- Connect to PostgreSQL
- Implement CORS

## 📚 API Endpoint Summary

| Category | Endpoint | Method | Auth | Status |
|----------|----------|--------|------|--------|
| **TV Auth** | `/auth/tv/create-session` | POST | No | 🔴 To Implement |
| **TV Auth** | `/auth/tv/check-status` | GET | No | 🔴 To Implement |
| **TV Auth** | `/auth/tv/link` | POST | Yes | 🔴 To Implement |
| **User** | `/user/profile` | GET | Yes | 🔴 To Implement |
| **User** | `/user/setup` | POST | Yes | 🔴 To Implement |
| **Collections** | `/collections` | GET | Yes | 🔴 To Implement |
| **Collections** | `/collections` | POST | Yes | 🔴 To Implement |
| **Collections** | `/collections/{id}` | GET | Yes | 🔴 To Implement |
| **Collections** | `/collections/{id}` | PUT | Yes | 🔴 To Implement |
| **Collections** | `/collections/{id}` | DELETE | Yes | 🔴 To Implement |
| **Collections** | `/collections/{id}/items` | POST | Yes | 🔴 To Implement |
| **Collections** | `/collections/{id}/items/{itemId}` | DELETE | Yes | �� To Implement |
| **Collections** | `/collections/setup/defaults` | POST | Yes | 🔴 To Implement |

## 🎓 Learning Resources

- **REST API Design:** https://restfulapi.net/
- **JWT Authentication:** https://jwt.io/introduction
- **PostgreSQL:** https://www.postgresql.org/docs/
- **API Security:** https://owasp.org/www-project-api-security/

## 🤝 Support & Contribution

### Getting Help
1. Review the relevant documentation file
2. Check the troubleshooting section in QR_AUTH_IMPLEMENTATION_GUIDE.md
3. Verify your implementation matches the examples

### Common Issues
- **Invalid JWT:** Ensure token is valid and not expired
- **Session not found:** Check session ID format and expiration
- **CORS errors:** Configure CORS to allow your app origins
- **401 Unauthorized:** Verify Authorization header format

## 📞 Contact

For questions about:
- **API Specifications:** See SERVER_API_REQUIREMENTS.md
- **QR Authentication:** See QR_AUTH_IMPLEMENTATION_GUIDE.md
- **Implementation Steps:** See IMPLEMENTATION_SUMMARY.md

## 📝 Document Versions

| Document | Version | Last Updated |
|----------|---------|--------------|
| IMPLEMENTATION_SUMMARY.md | 1.0 | Nov 30, 2025 |
| SERVER_API_REQUIREMENTS.md | 1.0 | Nov 30, 2025 |
| QR_AUTH_IMPLEMENTATION_GUIDE.md | 1.0 | Nov 30, 2025 |

---

## 🚀 Ready to Get Started?

1. **Read:** [IMPLEMENTATION_SUMMARY.md](./IMPLEMENTATION_SUMMARY.md)
2. **Implement:** Follow [SERVER_API_REQUIREMENTS.md](./SERVER_API_REQUIREMENTS.md)
3. **Test:** Use examples in [QR_AUTH_IMPLEMENTATION_GUIDE.md](./QR_AUTH_IMPLEMENTATION_GUIDE.md)
4. **Deploy:** Go live! 🎉

**Good luck with your implementation!** 🚀

---

*This documentation was created for the WatchTime project on November 30, 2025*

