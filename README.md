# 🎬 WatchTime - Movie & TV Show Tracker

A modern Android application for tracking movies and TV shows, available on both **Android Mobile** and **Android TV** platforms.

## 📱 Platforms

- **Android Mobile App** - Browse, search, and manage your watchlist
- **Android TV App** - Enjoy content discovery on your TV with QR-based authentication

## 🚀 Features

### Mobile App
- 🔐 Google Sign-In authentication
- 🔍 Search movies and TV shows (powered by TMDB)
- 📚 Create custom collections
- ⭐ Save to "Watch Later" and "Already Watched" lists
- 📱 QR code scanner for TV authentication

### TV App
- 📺 TV-optimized UI with D-pad navigation
- 🔐 QR code authentication (scan with mobile app)
- 🎯 Browse popular and trending content
- 🗂️ Access your collections on the big screen
- 🎨 Beautiful leanback experience

## 📚 Documentation

### 🎯 For Server Implementation

If you're implementing the backend server, **start here**:

1. **[SERVER_DOCS_README.md](./SERVER_DOCS_README.md)** - 📖 Main documentation index ⭐ **START HERE**
2. **[IMPLEMENTATION_SUMMARY.md](./IMPLEMENTATION_SUMMARY.md)** - ✅ Implementation checklist
3. **[SERVER_API_REQUIREMENTS.md](./SERVER_API_REQUIREMENTS.md)** - 🔧 Complete API specs
4. **[QR_AUTH_IMPLEMENTATION_GUIDE.md](./QR_AUTH_IMPLEMENTATION_GUIDE.md)** - 📱 QR auth guide

### 📺 For Android TV Development

- **[TV_SUMMARY.md](./TV_SUMMARY.md)** - TV app overview
- **[TV_IMPLEMENTATION.md](./TV_IMPLEMENTATION.md)** - Implementation guide
- **[TV_ARCHITECTURE_DIAGRAM.md](./TV_ARCHITECTURE_DIAGRAM.md)** - Architecture diagrams
- **[TV_QUICK_REFERENCE.md](./TV_QUICK_REFERENCE.md)** - Quick reference

### 📖 API Reference

- **[API_DOCS.md](./API_DOCS.md)** - API documentation

## 🏗️ Project Structure

```
watchtime_android/
├── app/                          # Android Mobile App
│   └── src/main/
│       ├── java/
│       └── AndroidManifest.xml
│
├── app-tv/                       # Android TV App
│   └── src/main/
│       ├── java/
│       └── AndroidManifest.xml
│
├── auth/                         # Authentication Module
│   ├── domain/                   # Business logic
│   ├── data/                     # Data layer
│   ├── ui/                       # Mobile UI (includes QR scanner)
│   └── tv-ui/                    # TV UI (includes QR display)
│
├── collections/                  # Collections Module
│   ├── domain/
│   ├── data/
│   ├── ui/                       # Mobile UI
│   └── tv-ui/                    # TV UI
│
├── core/                         # Core Modules
│   ├── network/                  # Network & API services
│   ├── ui/                       # Shared UI components
│   ├── tv-ui/                    # Shared TV UI components
│   ├── navigation/               # Navigation
│   └── utils/                    # Utilities
│
├── discover/                     # Content Discovery
├── media/                        # Media Details
├── popular/                      # Popular Content
└── profile/                      # User Profile

```

## 🛠️ Tech Stack

### Mobile & TV Apps
- **Language:** Kotlin
- **UI:** Jetpack Compose (Mobile) & Compose for TV
- **Architecture:** Clean Architecture (Domain/Data/UI layers)
- **DI:** Koin
- **Networking:** Retrofit + Kotlin Serialization
- **Authentication:** Firebase Auth + Supabase
- **Image Loading:** Coil
- **QR Code:** ZXing (generation) + ML Kit (scanning)

### Backend (To Be Implemented)
- **Recommended:** Node.js + Express or Python + FastAPI
- **Database:** PostgreSQL
- **Cache:** Redis
- **Authentication:** JWT token validation

## 🔑 Key Features Implementation

### QR Code Authentication Flow

```
┌─────────────┐                ┌─────────────┐                ┌─────────────┐
│  TV App     │                │   Backend   │                │  Mobile App │
└──────┬──────┘                └──────┬──────┘                └──────┬──────┘
       │                              │                              │
       │ 1. Create Session            │                              │
       ├─────────────────────────────>│                              │
       │                              │                              │
       │ 2. Return QR URL             │                              │
       │<─────────────────────────────┤                              │
       │                              │                              │
       │ 3. Display QR Code           │                              │
       │                              │                              │
       │ 4. Poll Status (every 5s)    │                              │
       ├─────────────────────────────>│                              │
       │                              │                              │
       │                              │  5. Scan QR                  │
       │                              │<─────────────────────────────┤
       │                              │                              │
       │                              │  6. Link Session             │
       │                              │<─────────────────────────────┤
       │                              │                              │
       │ 7. Status: Authenticated     │                              │
       │<─────────────────────────────┤                              │
       │                              │                              │
```

### Collections Management

- **Default Collections:** "Watch Later" & "Already Watched"
- **Custom Collections:** Create your own themed collections
- **Rich Metadata:** Full TMDB data stored with each item
- **Cross-Platform:** Access collections on mobile and TV

## 🚦 Getting Started

### Prerequisites

- Android Studio Hedgehog or newer
- JDK 17 or higher
- Android SDK with API 24+ (Mobile) and API 21+ (TV)
- Backend server (see [SERVER_DOCS_README.md](./SERVER_DOCS_README.md))

### Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd watchtime_android
   ```

2. **Configure API endpoints**
   ```kotlin
   // core/utils/src/main/java/com/app/core/utils/constants/Constants.kt
   const val API_SERVER_URL = "https://your-server.com/api/"
   const val TMDB_API_KEY = "your-tmdb-api-key"
   ```

3. **Add google-services.json**
   - Mobile: `app/google-services.json`
   - TV: `app-tv/google-services.json`

4. **Build and run**
   ```bash
   # Mobile app
   ./gradlew :app:installDebug
   
   # TV app
   ./gradlew :app-tv:installDebug
   ```

### Backend Setup

See **[SERVER_DOCS_README.md](./SERVER_DOCS_README.md)** for complete backend implementation guide.

Quick start:
1. Read [IMPLEMENTATION_SUMMARY.md](./IMPLEMENTATION_SUMMARY.md)
2. Implement APIs per [SERVER_API_REQUIREMENTS.md](./SERVER_API_REQUIREMENTS.md)
3. Test with apps

## 🔐 Authentication

### Mobile App
- Google Sign-In
- Supabase authentication
- JWT token storage

### TV App
- QR code-based authentication
- Links to mobile account
- Session polling mechanism

## 📡 API Endpoints

### Required Endpoints

**TV Authentication:**
- `POST /auth/tv/create-session` - Create TV session
- `GET /auth/tv/check-status` - Poll auth status
- `POST /auth/tv/link` - Link mobile to TV

**User Management:**
- `GET /user/profile` - Get user profile
- `POST /user/setup` - Setup new user

**Collections:**
- `GET /collections` - Get all collections
- `POST /collections` - Create collection
- `POST /collections/{id}/items` - Add item
- `DELETE /collections/{id}/items/{itemId}` - Remove item

See [SERVER_API_REQUIREMENTS.md](./SERVER_API_REQUIREMENTS.md) for complete API documentation.

## 🧪 Testing

### Mobile App Testing
1. Run on emulator or physical device
2. Sign in with Google
3. Search for movies/shows
4. Create collections
5. Test QR scanning (requires TV app or mock QR)

### TV App Testing
1. Run on Android TV emulator or device
2. Navigate with D-pad
3. Display QR code
4. Scan with mobile app
5. Verify authentication

### Backend Testing
```bash
# Test TV session creation
curl -X POST "http://localhost:5000/api/auth/tv/create-session?sessionId=$(uuidgen)"

# Test collection retrieval
curl "http://localhost:5000/api/collections" \
  -H "Authorization: Bearer YOUR_JWT"
```

## 📦 Modules Overview

### Core Modules
- **core/network** - API services, Retrofit setup
- **core/ui** - Shared Compose components
- **core/tv-ui** - TV-specific components
- **core/utils** - Utilities and constants

### Feature Modules
- **auth** - Authentication (mobile + TV)
- **collections** - Collection management
- **discover** - Content discovery
- **media** - Content details
- **popular** - Popular content
- **profile** - User profile

## 🎨 Design

### Mobile App
- Material 3 Design
- Dynamic color theming
- Bottom navigation
- Search functionality

### TV App
- Leanback UI guidelines
- D-pad optimized navigation
- Focus management
- Large touch targets

## 🔧 Configuration

### Build Variants
- **debug** - Development build with logging
- **release** - Production build with ProGuard

### Gradle Configuration
See `build.gradle.kts` files in each module for dependencies and build configuration.

## 📱 Deep Links

### TV Authentication
```
watchtime://tv-auth?sessionId={uuid}
```

Configure in `AndroidManifest.xml`:
```xml
<intent-filter android:autoVerify="true">
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="watchtime" android:host="tv-auth" />
</intent-filter>
```

## 🤝 Contributing

1. Follow Clean Architecture principles
2. Use Kotlin coding conventions
3. Write meaningful commit messages
4. Test on both mobile and TV platforms
5. Update documentation for API changes

## 📄 License

[Add your license here]

## 👥 Authors

[Add authors here]

## 🙏 Acknowledgments

- **TMDB** - Movie and TV show data
- **Firebase** - Authentication
- **Supabase** - Backend services
- **ZXing** - QR code generation
- **Google ML Kit** - QR code scanning

## 📞 Support

For issues or questions:
- Review the documentation in the `/docs` section
- Check [SERVER_DOCS_README.md](./SERVER_DOCS_README.md) for backend issues
- See [QR_AUTH_IMPLEMENTATION_GUIDE.md](./QR_AUTH_IMPLEMENTATION_GUIDE.md) for QR auth help

## 🗺️ Roadmap

- [ ] Backend API implementation
- [ ] User profile customization
- [ ] Watchlist sharing
- [ ] Recommendations engine
- [ ] Offline support
- [ ] Widget support
- [ ] TV home screen integration

---

**Made with ❤️ using Jetpack Compose**

For detailed implementation guides, see:
- 📖 [Server Documentation Index](./SERVER_DOCS_README.md)
- ✅ [Implementation Summary](./IMPLEMENTATION_SUMMARY.md)
- 🔧 [API Requirements](./SERVER_API_REQUIREMENTS.md)
- 📱 [QR Auth Guide](./QR_AUTH_IMPLEMENTATION_GUIDE.md)

