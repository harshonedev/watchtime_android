# WatchTime - Android TV Implementation ✨

> Multi-platform movie and TV show tracking app with QR code authentication for Android TV

## 📱 Platforms

- ✅ **Android Mobile** - Original app (Jetpack Compose)
- ✅ **Android TV** - NEW! TV-optimized experience with QR authentication

## 🎯 What's New - Android TV

### QR Code Authentication Flow
1. **TV App** displays a QR code on the big screen
2. **Mobile App** scans the QR code using the camera
3. **Instant Login** - TV automatically signs in without typing

### TV-Optimized UI
- 🎮 D-pad navigation support
- 📺 10-foot UI design (readable from couch)
- ⚡ Focus-based interactions
- 🎨 Leanback Material Design

## 🏗️ Architecture

```
watchtime_android/
├── app/                    # Mobile application
├── app-tv/                 # TV application (NEW)
├── auth/
│   ├── ui/                # Mobile auth UI (+ QR scanner)
│   ├── tv-ui/             # TV auth UI (QR display) (NEW)
│   ├── domain/            # Shared auth logic
│   └── data/              # Shared auth data
├── core/
│   ├── tv-ui/             # TV components (NEW)
│   ├── ui/                # Mobile components
│   ├── network/           # Shared API (+ TV auth API)
│   ├── room/              # Shared database
│   └── utils/             # Shared utilities
└── features/
    ├── popular/
    │   ├── ui/            # Mobile UI
    │   ├── tv-ui/         # TV UI (NEW)
    │   ├── domain/        # Shared logic
    │   └── data/          # Shared data
    ├── discover/          # Similar structure
    ├── media/             # Similar structure
    └── collections/       # Similar structure
```

## 🚀 Quick Start

### Prerequisites
- Android Studio Hedgehog or later
- JDK 11+
- Android SDK 26+ (Android 8.0)
- Android TV device/emulator (for TV app)

### Build & Run

#### Mobile App (with QR Scanner)
```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

#### TV App
```bash
./gradlew :app-tv:assembleDebug
./gradlew :app-tv:installDebug
```

## 📖 Documentation

| Document | Description |
|----------|-------------|
| [TV_SUMMARY.md](TV_SUMMARY.md) | Project overview and status |
| [TV_QUICK_REFERENCE.md](TV_QUICK_REFERENCE.md) | Quick reference guide |
| [TV_IMPLEMENTATION.md](TV_IMPLEMENTATION.md) | Full technical documentation |
| [TV_IMPLEMENTATION_STEPS.md](TV_IMPLEMENTATION_STEPS.md) | Step-by-step plan |
| [TV_ARCHITECTURE_DIAGRAM.md](TV_ARCHITECTURE_DIAGRAM.md) | Visual architecture diagrams |

## 🔑 Key Features

### Mobile App
- ✅ User authentication (Google Sign-In)
- ✅ Browse popular movies and TV shows
- ✅ Discover new content
- ✅ Personal watchlist and collections
- ✅ **QR Code Scanner** - Link TV devices

### TV App
- ✅ **QR Code Authentication** - No typing required
- ✅ Leanback UI optimized for TV
- ✅ D-pad navigation
- 🔄 Content browsing (in progress)
- 🔄 Media details (in progress)
- 🔄 Video playback (planned)

## 🛠️ Tech Stack

### Mobile & TV Shared
- **Language**: Kotlin
- **Architecture**: Multi-module Clean Architecture
- **DI**: Koin
- **Database**: Room
- **Networking**: Retrofit + OkHttp
- **Image Loading**: Coil
- **Serialization**: Kotlin Serialization

### Mobile Specific
- **UI**: Jetpack Compose
- **Camera**: CameraX
- **Barcode Scanning**: ML Kit
- **Theme**: Material Design 3

### TV Specific
- **UI**: Jetpack Compose for TV
- **Framework**: AndroidX Leanback
- **QR Generation**: ZXing
- **Theme**: TV Material Design

## 🔐 Authentication Flow

```
┌─────────┐                    ┌─────────┐                    ┌─────────┐
│   TV    │                    │ Backend │                    │ Mobile  │
└────┬────┘                    └────┬────┘                    └────┬────┘
     │                              │                              │
     │──── Create Session ─────────>│                              │
     │<─── Return QR URL ───────────│                              │
     │                              │                              │
     │ [Display QR Code]            │                              │
     │                              │                              │
     │                              │<──── Scan QR ───────────────│
     │                              │                              │
     │                              │<──── Link Request ──────────│
     │                              │   (with user token)          │
     │                              │                              │
     │                              │──── Success ────────────────>│
     │                              │                              │
     │──── Poll Status ────────────>│                              │
     │<─── Authenticated ───────────│                              │
     │    (with token)              │                              │
     │                              │                              │
     │ [Navigate to Home]           │                              │
```

## 📦 Modules

### Core Modules
- `core:ui` - Mobile UI components and theme
- `core:tv-ui` - TV UI components and theme (**NEW**)
- `core:network` - REST API and TV auth endpoints
- `core:room` - Local database
- `core:utils` - Shared utilities
- `core:navigation` - Navigation setup
- `core:home` - Home screen container

### Feature Modules
Each feature has:
- `domain` - Business logic (shared)
- `data` - Data layer (shared)
- `ui` - Mobile UI
- `tv-ui` - TV UI (**NEW**)

Features:
- `auth` - Authentication
- `popular` - Popular content
- `discover` - Content discovery
- `media` - Media details
- `collections` - User collections
- `profile` - User profile

## 🔧 Backend Requirements

The TV authentication requires these backend endpoints:

### 1. Create TV Session
```http
POST /auth/tv/create-session?sessionId={uuid}
```

### 2. Check TV Status (Polled)
```http
GET /auth/tv/check-status?sessionId={uuid}
```

### 3. Link Mobile to TV
```http
POST /auth/tv/link
Authorization: Bearer {mobile-token}
Body: { sessionId: string }
```

> See [TV_IMPLEMENTATION.md](TV_IMPLEMENTATION.md) for detailed API specs

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Android Tests
```bash
./gradlew connectedAndroidTest
```

### TV Testing
1. Start Android TV emulator or connect physical TV
2. Install TV app: `./gradlew :app-tv:installDebug`
3. Launch TV app
4. Scan QR with mobile app
5. Verify authentication flow

## 📊 Project Status

| Component | Status | Notes |
|-----------|--------|-------|
| Mobile App | ✅ Production | Existing app |
| QR Scanner | ✅ Complete | Mobile integration needed |
| TV App Structure | ✅ Complete | All modules created |
| QR Authentication | ✅ Complete | Backend needed |
| TV Home Screen | 🔄 In Progress | Placeholder UI |
| TV Media Details | ⏳ Planned | Not started |
| TV Playback | ⏳ Planned | Not started |
| Backend API | ⚠️ Required | Critical path |

**Legend**: ✅ Complete | 🔄 In Progress | ⏳ Planned | ⚠️ Required

## 🗺️ Roadmap

### Phase 1: Foundation ✅
- [x] Create TV app module
- [x] Set up multi-module structure
- [x] Implement QR authentication flow
- [x] Add QR scanner to mobile

### Phase 2: Backend Integration (Current)
- [ ] Implement backend TV auth endpoints
- [ ] Set up session storage (Redis)
- [ ] Test end-to-end auth flow

### Phase 3: Content Browsing
- [ ] Build TV home screen
- [ ] Implement content rows
- [ ] Add D-pad navigation
- [ ] Create media cards

### Phase 4: Details & Playback
- [ ] TV media details screen
- [ ] Video player integration
- [ ] Resume playback support

### Phase 5: Polish
- [ ] UI/UX refinements
- [ ] Performance optimization
- [ ] Testing and bug fixes

## 🤝 Contributing

1. Create feature branch
2. Make changes
3. Write tests
4. Submit PR

## 📄 License

[Your License Here]

## 👥 Team

[Your Team Information]

## 📞 Support

For issues and questions:
- Create an issue on GitHub
- Check documentation in `/docs`
- Review [TV_QUICK_REFERENCE.md](TV_QUICK_REFERENCE.md)

---

**Built with** ❤️ **using Kotlin & Jetpack Compose**

*Multi-platform. One codebase. Infinite possibilities.*

