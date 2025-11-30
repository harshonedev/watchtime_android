# Android TV Implementation Guide

## Overview
This document outlines the Android TV implementation for WatchTime, featuring QR code-based authentication where users scan a QR code displayed on their TV using their mobile device.

## Project Structure

### New Modules Created

#### 1. **app-tv** - TV Application Module
- Main TV application entry point
- Location: `/app-tv/`
- Key Files:
  - `TvMainActivity.kt` - Main activity for TV
  - `WatchTimeTvApplication.kt` - Application class with DI setup
  - `AndroidManifest.xml` - TV-specific manifest with leanback launcher
  - `navigation/TvAppNavigation.kt` - TV navigation graph

#### 2. **core:tv-ui** - Core TV UI Components
- Shared TV UI components and utilities
- Location: `/core/tv-ui/`
- Key Files:
  - `components/TvFocusableCard.kt` - Reusable focusable card for TV

#### 3. **auth:tv-ui** - TV Authentication Module
- QR code authentication for TV
- Location: `/auth/tv-ui/`
- Key Files:
  - `screens/TvAuthScreen.kt` - QR code display screen
  - `viewmodels/TvAuthViewModel.kt` - Handles QR generation and polling
  - `di/AuthTvUiModule.kt` - Dependency injection

#### 4. **Feature TV Modules**
- `popular/tv-ui` - Popular content for TV
- `discover/tv-ui` - Discover screen for TV
- `media/tv-ui` - Media details for TV
- `collections/tv-ui` - Collections for TV

### Modified Modules

#### **core:network**
- Added `TvAuthApiService.kt` - API service for TV authentication
- Added `model/TvAuthResponse.kt` - Response models for TV auth

#### **auth:ui** (Mobile)
- Added `screens/QrScanScreen.kt` - QR scanner screen
- Added `viewmodels/QrScanViewModel.kt` - QR scanning logic
- Updated dependencies to include CameraX and ML Kit

## Authentication Flow

### TV Side (QR Code Display)

1. **Session Creation**
   - TV app launches and shows auth screen
   - `TvAuthViewModel.initiateQRAuth()` is called
   - Generates unique session ID (UUID)
   - Calls backend: `POST /auth/tv/create-session?sessionId={id}`
   - Backend returns auth URL: `watchtime://tv-auth?sessionId={id}`

2. **QR Code Generation**
   - Uses ZXing library to generate QR code bitmap from auth URL
   - Displays QR code on screen (400dp x 400dp)
   - Shows instructions for mobile scanning

3. **Polling for Authentication**
   - Starts polling backend every 5 seconds
   - Calls: `GET /auth/tv/check-status?sessionId={id}`
   - Polls for max 60 attempts (5 minutes)
   - On success, receives auth token and navigates to home

### Mobile Side (QR Code Scanner)

1. **Camera Permission**
   - Requests CAMERA permission using Accompanist Permissions
   - Shows permission rationale if not granted

2. **QR Scanning**
   - Uses CameraX for camera preview
   - ML Kit Barcode Scanning to detect QR codes
   - Filters for `watchtime://tv-auth` URL scheme
   - Extracts session ID from URL

3. **Authentication Linking**
   - Calls: `POST /auth/tv/link` with:
     - User's auth token (Bearer)
     - Session ID from QR code
   - Backend links mobile user to TV session
   - Shows success/error feedback

## Backend API Requirements

### Endpoints to Implement

```typescript
// Create TV authentication session
POST /auth/tv/create-session?sessionId={string}
Response: {
  success: boolean,
  data: {
    sessionId: string,
    authUrl: string,
    expiresAt: number
  }
}

// Check TV authentication status (polled by TV)
GET /auth/tv/check-status?sessionId={string}
Response: {
  success: boolean,
  data: {
    authenticated: boolean,
    token?: string,
    userId?: string
  }
}

// Link mobile user to TV (called by mobile)
POST /auth/tv/link
Headers: Authorization: Bearer {mobile_token}
Body: {
  sessionId: string
}
Response: {
  success: boolean,
  message: string
}
```

### Backend Implementation Notes

1. **Session Storage**
   - Store TV sessions in Redis or in-memory cache
   - Key: `tv_auth:${sessionId}`
   - Value: `{ authenticated: false, userId: null, token: null, expiresAt: timestamp }`
   - TTL: 5-10 minutes

2. **Security Considerations**
   - Session IDs must be cryptographically random (UUID v4)
   - One-time use only - invalidate after successful auth
   - Short expiration time (5-10 minutes)
   - Rate limit session creation and polling
   - Validate mobile auth token before linking

3. **Linking Flow**
   - When mobile calls `/auth/tv/link`:
     - Verify mobile user's auth token
     - Check session exists and not expired
     - Generate new auth token for TV (or use mobile token)
     - Update session: `{ authenticated: true, userId, token }`
   - TV's next poll will receive the token
   - Invalidate session after TV retrieves token

## Dependencies Added

### Version Catalog (`libs.versions.toml`)

```toml
[versions]
composeTv = "1.1.0"
leanback = "1.2.0-alpha06"
tvFoundation = "1.1.0"
tvMaterial = "1.1.0"
zxingCore = "3.5.3"
zxingAndroid = "4.3.0"
camerax = "1.4.1"
mlkitBarcode = "17.3.0"
accompanist = "0.36.0"

[libraries]
# TV dependencies
androidx-tv-foundation = { module = "androidx.tv:tv-foundation", version.ref = "tvFoundation" }
androidx-tv-material = { module = "androidx.tv:tv-material", version.ref = "tvMaterial" }
androidx-leanback = { module = "androidx.leanback:leanback", version.ref = "leanback" }

# QR Code libraries
zxing-core = { module = "com.google.zxing:core", version.ref = "zxingCore" }
zxing-android = { module = "com.journeyapps:zxing-android-embedded", version.ref = "zxingAndroid" }

# Camera and ML Kit
androidx-camera-core = { module = "androidx.camera:camera-core", version.ref = "camerax" }
androidx-camera-camera2 = { module = "androidx.camera:camera-camera2", version.ref = "camerax" }
androidx-camera-lifecycle = { module = "androidx.camera:camera-lifecycle", version.ref = "camerax" }
androidx-camera-view = { module = "androidx.camera:camera-view", version.ref = "camerax" }
mlkit-barcode-scanning = { module = "com.google.mlkit:barcode-scanning", version.ref = "mlkitBarcode" }

# Accompanist
accompanist-permissions = { module = "com.google.accompanist:accompanist-permissions", version.ref = "accompanist" }
```

## Building and Running

### Build TV App

```bash
# Sync Gradle
./gradlew sync

# Build TV APK
./gradlew :app-tv:assembleDebug

# Install on Android TV device/emulator
./gradlew :app-tv:installDebug
```

### Build Mobile App

```bash
# Build mobile APK with QR scanner
./gradlew :app:assembleDebug

# Install on mobile device
./gradlew :app:installDebug
```

## Testing the Flow

1. **Start TV App**
   - Launch WatchTime TV on Android TV device/emulator
   - QR code will be displayed on screen

2. **Scan with Mobile**
   - Open WatchTime mobile app
   - Navigate to "Scan QR Code" (needs to be added to navigation)
   - Grant camera permission
   - Point camera at TV QR code

3. **Verify Authentication**
   - Mobile app shows "Linking TV device..."
   - TV app polls and receives authentication
   - TV app navigates to home screen
   - Mobile app shows success message

## Next Steps

### Mobile App Navigation Update
Add QR scanner to mobile app navigation:

```kotlin
// In HomeScreen or ProfileScreen, add a button/menu item:
Button(onClick = { navController.navigate("qr_scan") }) {
    Text("Link TV Device")
}

// In AppNavigation.kt, add route:
composable("qr_scan") {
    QrScanScreen(
        onScanComplete = {
            navController.popBackStack()
        }
    )
}
```

### TV Home Screen Enhancement
Currently shows placeholder content. Next steps:
1. Create browsable rows for Popular, Discover, Collections
2. Implement D-pad navigation and focus handling
3. Add media details screen optimized for TV
4. Implement video playback

### Backend Implementation
1. Create the three TV auth endpoints
2. Set up Redis for session storage
3. Implement security measures (rate limiting, token validation)
4. Add logging and monitoring

### Additional Features
1. **Profile Switching on TV** - Allow switching between user profiles
2. **Voice Search** - Integrate Android TV voice search
3. **Recommendations Row** - Personalized recommendations on TV home
4. **Parental Controls** - PIN-based content restrictions
5. **Playback Resume** - Continue watching from where you left off

## Troubleshooting

### Common Issues

1. **QR Code Not Scanning**
   - Ensure CAMERA permission is granted
   - Check QR code is well-lit and in focus
   - Verify QR content starts with `watchtime://tv-auth`

2. **TV Not Authenticating**
   - Check backend endpoints are running
   - Verify polling is working (check logs)
   - Ensure session hasn't expired (5 min timeout)

3. **Build Errors**
   - Run `./gradlew clean build`
   - Sync Gradle files
   - Check all module dependencies are correct

4. **TV App Not Launching**
   - Verify Android TV/emulator is Android 8.0+ (API 26+)
   - Check manifest has `android.software.leanback` feature
   - Ensure LEANBACK_LAUNCHER intent filter is present

## File Checklist

### Created Files
- ✅ `/app-tv/build.gradle.kts`
- ✅ `/app-tv/src/main/AndroidManifest.xml`
- ✅ `/app-tv/src/main/java/com/app/watchtime/tv/TvMainActivity.kt`
- ✅ `/app-tv/src/main/java/com/app/watchtime/tv/WatchTimeTvApplication.kt`
- ✅ `/app-tv/src/main/java/com/app/watchtime/tv/navigation/TvAppNavigation.kt`
- ✅ `/app-tv/src/main/java/com/app/watchtime/tv/screens/TvHomeScreen.kt`
- ✅ `/app-tv/src/main/java/com/app/watchtime/tv/di/TvAppModule.kt`
- ✅ `/core/tv-ui/build.gradle.kts`
- ✅ `/core/tv-ui/src/main/java/com/app/core/tvui/components/TvFocusableCard.kt`
- ✅ `/auth/tv-ui/build.gradle.kts`
- ✅ `/auth/tv-ui/src/main/java/com/app/auth/tvui/screens/TvAuthScreen.kt`
- ✅ `/auth/tv-ui/src/main/java/com/app/auth/tvui/viewmodels/TvAuthViewModel.kt`
- ✅ `/auth/tv-ui/src/main/java/com/app/auth/tvui/di/AuthTvUiModule.kt`
- ✅ `/auth/ui/src/main/java/com/app/auth/ui/screens/QrScanScreen.kt`
- ✅ `/auth/ui/src/main/java/com/app/auth/ui/viewmodels/QrScanViewModel.kt`
- ✅ `/core/network/src/main/java/com/app/core/network/api/TvAuthApiService.kt`
- ✅ `/core/network/src/main/java/com/app/core/network/model/TvAuthResponse.kt`
- ✅ Feature TV modules: `popular/tv-ui`, `discover/tv-ui`, `media/tv-ui`, `collections/tv-ui`

### Modified Files
- ✅ `/settings.gradle.kts` - Added TV modules
- ✅ `/gradle/libs.versions.toml` - Added TV and QR dependencies
- ✅ `/auth/ui/build.gradle.kts` - Added QR scanning dependencies
- ✅ `/auth/ui/src/main/java/com/app/auth/ui/di/AuthUIModule.kt` - Added QrScanViewModel
- ✅ `/core/network/src/main/java/com/app/core/network/di/NetworkModule.kt` - Added TvAuthApiService
- ✅ `/app/src/main/AndroidManifest.xml` - Added CAMERA permission

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────┐
│                    Mobile App                            │
│  ┌──────────────────────────────────────────────────┐  │
│  │ QrScanScreen → QrScanViewModel → TvAuthApiService│  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                            │
                            │ POST /auth/tv/link
                            ▼
┌─────────────────────────────────────────────────────────┐
│                    Backend Server                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │ TV Auth Endpoints                                 │  │
│  │ - Create Session                                  │  │
│  │ - Check Status (polled)                          │  │
│  │ - Link Mobile to TV                              │  │
│  │                                                    │  │
│  │ Session Storage (Redis):                         │  │
│  │ tv_auth:{sessionId} → { authenticated, token }   │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                            ▲
                            │ GET /auth/tv/check-status (polling)
                            │
┌─────────────────────────────────────────────────────────┐
│                     TV App                               │
│  ┌──────────────────────────────────────────────────┐  │
│  │ TvAuthScreen → TvAuthViewModel                    │  │
│  │ ├─ Generate QR Code (ZXing)                      │  │
│  │ ├─ Display QR on Screen                          │  │
│  │ └─ Poll for Authentication                        │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

## Summary

The Android TV implementation is now structurally complete with:
- ✅ Separate TV app module (`app-tv`)
- ✅ TV-optimized UI components
- ✅ QR code authentication flow (TV displays, mobile scans)
- ✅ Proper multi-module architecture
- ✅ Shared domain/data layers
- ✅ All necessary dependencies

**What remains:**
1. Backend API implementation (3 endpoints)
2. Integration of QR scanner into mobile app navigation
3. Content browsing UI for TV home screen
4. Testing with actual devices/emulators

