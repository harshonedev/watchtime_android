# Android TV Implementation - Step-by-Step Plan

## ✅ Phase 1: Project Setup (COMPLETED)

### Step 1: Add Dependencies
- ✅ Added TV dependencies (Compose for TV, Leanback)
- ✅ Added QR code libraries (ZXing for generation, ML Kit for scanning)
- ✅ Added CameraX dependencies
- ✅ Added Accompanist Permissions library

### Step 2: Create Module Structure
- ✅ Created `app-tv` module (TV application)
- ✅ Created `core:tv-ui` module (shared TV components)
- ✅ Created `auth:tv-ui` module (TV authentication)
- ✅ Created feature TV modules: `popular:tv-ui`, `discover:tv-ui`, `media:tv-ui`, `collections:tv-ui`
- ✅ Updated `settings.gradle.kts` with all TV modules

### Step 3: Configure TV App Module
- ✅ Created TV AndroidManifest with leanback support
- ✅ Created TV launcher activity (`TvMainActivity`)
- ✅ Created TV application class (`WatchTimeTvApplication`)
- ✅ Set up Koin DI for TV app
- ✅ Created TV navigation graph

---

## ✅ Phase 2: Authentication Flow (COMPLETED)

### Step 4: TV QR Code Display
- ✅ Created `TvAuthScreen` to display QR code
- ✅ Created `TvAuthViewModel` with:
  - QR code generation using ZXing
  - Session creation logic
  - Polling mechanism (5-second intervals, 5-minute timeout)
- ✅ Added Koin module for TV auth

### Step 5: Mobile QR Scanner
- ✅ Created `QrScanScreen` with CameraX integration
- ✅ Created `QrScanViewModel` for scanning logic
- ✅ Implemented ML Kit barcode scanning
- ✅ Added camera permission handling
- ✅ Updated mobile app manifest with CAMERA permission

### Step 6: Network Layer
- ✅ Created `TvAuthApiService` interface with endpoints:
  - `createTvAuthSession(sessionId)`
  - `checkTvAuthStatus(sessionId)`
  - `linkMobileToTv(token, sessionId)`
- ✅ Created response models (`TvAuthResponse.kt`)
- ✅ Added service to Network DI module

---

## 🔄 Phase 3: Backend Implementation (TODO)

### Step 7: Backend API Development
- [ ] Create session storage (Redis recommended)
- [ ] Implement `POST /auth/tv/create-session` endpoint
  - Generate session with 5-10 minute expiration
  - Return auth URL: `watchtime://tv-auth?sessionId={id}`
- [ ] Implement `GET /auth/tv/check-status` endpoint
  - Return authentication status
  - Return token if authenticated
- [ ] Implement `POST /auth/tv/link` endpoint
  - Validate mobile user token
  - Link user to TV session
  - Generate/assign auth token for TV

### Step 8: Security Implementation
- [ ] Add rate limiting to prevent abuse
- [ ] Implement session expiration (5-10 minutes)
- [ ] Ensure one-time use for sessions
- [ ] Add request validation and sanitization
- [ ] Implement proper error handling

### Step 9: Testing Backend
- [ ] Test session creation
- [ ] Test polling behavior
- [ ] Test mobile linking
- [ ] Test timeout scenarios
- [ ] Test error cases

---

## 🔄 Phase 4: Mobile App Integration (TODO)

### Step 10: Add QR Scanner to Navigation
- [ ] Add "Link TV Device" button to Profile screen or settings
- [ ] Update `Screen.kt` sealed class with QR scan route:
  ```kotlin
  @Serializable
  object QrScan : Screen()
  ```
- [ ] Add composable route in `AppNavigation.kt`:
  ```kotlin
  composable<Screen.QrScan> {
      QrScanScreen(
          onScanComplete = {
              navController.popBackStack()
          }
      )
  }
  ```

### Step 11: Update Profile Screen
- [ ] Add button/menu item: "Link TV Device"
- [ ] Navigate to QR scanner when clicked
- [ ] Show success/error feedback after linking

---

## 🔄 Phase 5: TV Home Screen (TODO)

### Step 12: Create TV Home Layout
- [ ] Design browsable rows layout:
  - Popular Movies row
  - Popular TV Shows row
  - Discover row
  - Collections row
- [ ] Implement D-pad navigation
- [ ] Add focus handling for cards
- [ ] Create horizontal scrollable rows

### Step 13: Implement TV Card Components
- [ ] Create `TvMediaCard` component
  - Poster image loading (Coil)
  - Title and metadata
  - Focus border animation
  - Scale animation on focus
- [ ] Create `TvMediaRow` component
  - Horizontal LazyRow
  - Proper focus ordering

### Step 14: Connect to ViewModels
- [ ] Reuse existing ViewModels from mobile:
  - `PopularViewModel`
  - `DiscoverViewModel`
  - `CollectionsViewModel`
- [ ] Share data layer between mobile and TV
- [ ] Create TV-specific UI screens

---

## 🔄 Phase 6: Media Details for TV (TODO)

### Step 15: Create TV Media Details Screen
- [ ] Large backdrop image
- [ ] Title, rating, overview
- [ ] Cast and crew
- [ ] Action buttons (Play, Add to Watchlist, etc.)
- [ ] Seasons/episodes for TV shows
- [ ] Recommendations

### Step 16: Navigation to Details
- [ ] Implement navigation from home cards to details
- [ ] Pass media ID and type
- [ ] Implement back navigation

---

## 🔄 Phase 7: Testing & Polish (TODO)

### Step 17: Testing on Devices
- [ ] Test on Android TV emulator
- [ ] Test on physical Android TV device
- [ ] Test QR flow end-to-end
- [ ] Test D-pad navigation
- [ ] Test focus handling

### Step 18: UI Polish
- [ ] Add loading states
- [ ] Add error states
- [ ] Add empty states
- [ ] Add animations and transitions
- [ ] Optimize for 10-foot UI

### Step 19: Performance Optimization
- [ ] Optimize image loading
- [ ] Implement pagination for rows
- [ ] Cache API responses
- [ ] Optimize focus transitions

---

## 🔄 Phase 8: Advanced Features (OPTIONAL)

### Step 20: Video Playback
- [ ] Integrate ExoPlayer for TV
- [ ] Create playback screen
- [ ] Add playback controls
- [ ] Implement resume functionality

### Step 21: Search
- [ ] Implement voice search
- [ ] Add keyboard search
- [ ] Create search results screen

### Step 22: Settings
- [ ] Create TV settings screen
- [ ] Add theme selection
- [ ] Add parental controls
- [ ] Add sign-out option

### Step 23: Multi-Profile Support
- [ ] Allow profile switching on TV
- [ ] Show profile avatars
- [ ] Maintain separate watchlists per profile

---

## Quick Start Guide

### 1. Build and Install TV App
```bash
# Sync Gradle
./gradlew sync

# Build TV APK
./gradlew :app-tv:assembleDebug

# Install on Android TV
adb connect <tv-ip-address>  # If using physical TV
./gradlew :app-tv:installDebug
```

### 2. Build and Install Mobile App
```bash
# Build with QR scanner support
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

### 3. Set Up Backend (Before Testing)
```bash
# Example using Node.js/Express
# Create the three endpoints in your backend:
# - POST /auth/tv/create-session
# - GET /auth/tv/check-status  
# - POST /auth/tv/link

# Start your backend server
npm run dev
```

### 4. Test Authentication Flow
1. Launch TV app → QR code appears
2. Open mobile app → Navigate to QR scanner (needs navigation update)
3. Scan QR code with mobile
4. TV automatically logs in and shows home screen

---

## Implementation Checklist Summary

### Completed ✅
- [x] All module structure created
- [x] TV app module with manifest and activities
- [x] QR code generation on TV (ZXing)
- [x] QR code scanning on mobile (CameraX + ML Kit)
- [x] Network API interfaces
- [x] Dependency injection setup
- [x] Basic TV home screen placeholder

### In Progress 🔄
- [ ] Backend API implementation
- [ ] Mobile navigation integration
- [ ] TV home content rows
- [ ] Media details for TV

### Not Started ❌
- [ ] Video playback
- [ ] Advanced search
- [ ] Settings screens
- [ ] Multi-profile support

---

## Estimated Timeline

| Phase | Duration | Priority |
|-------|----------|----------|
| Phase 1-2: Setup & Auth | ✅ DONE | High |
| Phase 3: Backend | 2-3 days | High |
| Phase 4: Mobile Integration | 1 day | High |
| Phase 5: TV Home Screen | 3-4 days | High |
| Phase 6: Media Details | 2-3 days | Medium |
| Phase 7: Testing & Polish | 2-3 days | High |
| Phase 8: Advanced Features | 5-7 days | Low |

**Total Estimated Time:** 15-23 days for complete implementation

---

## Key Technical Decisions Made

1. **Architecture**: Multi-module with shared domain/data layers
2. **UI Framework**: Compose for TV (modern, consistent with mobile)
3. **Auth Method**: QR code (simple, secure, TV-friendly)
4. **QR Library**: ZXing for generation (lightweight, reliable)
5. **Scanner**: ML Kit Barcode + CameraX (Google recommended)
6. **Polling**: 5-second intervals with 5-minute timeout
7. **Session Storage**: Redis recommended (fast, expiring keys)

---

## Resources

- [Android TV Development Guide](https://developer.android.com/training/tv)
- [Compose for TV](https://developer.android.com/jetpack/androidx/releases/tv)
- [ZXing Documentation](https://github.com/zxing/zxing)
- [ML Kit Barcode Scanning](https://developers.google.com/ml-kit/vision/barcode-scanning)
- [CameraX](https://developer.android.com/training/camerax)

---

## Next Immediate Steps

1. **Implement Backend API** (Phase 3)
   - This is the critical path item
   - Without backend, authentication won't work
   - Start with session storage and create-session endpoint

2. **Add QR Scanner to Mobile Navigation** (Phase 4)
   - Quick integration task
   - Needed to test end-to-end flow

3. **Build TV Home Screen** (Phase 5)
   - Start with one row (Popular Movies)
   - Get D-pad navigation working
   - Then expand to other content types

**Priority Order:** Backend → Mobile Nav → TV Home → Media Details → Polish

