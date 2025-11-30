# Android TV Quick Reference

## 🎯 Project Status

✅ **COMPLETED:**
- All module structure
- QR authentication flow (TV + Mobile)
- Network API interfaces
- Basic TV home screen
- Dependency injection setup

⚠️ **PENDING:**
- Backend API implementation (CRITICAL)
- Mobile navigation integration
- TV content browsing UI

## 📁 Key Files Location

### TV App
```
app-tv/src/main/java/com/app/watchtime/tv/
├── TvMainActivity.kt          # Main TV activity
├── WatchTimeTvApplication.kt  # Application class
├── navigation/
│   └── TvAppNavigation.kt     # TV navigation graph
└── screens/
    └── TvHomeScreen.kt         # TV home screen
```

### TV Authentication
```
auth/tv-ui/src/main/java/com/app/auth/tvui/
├── screens/
│   └── TvAuthScreen.kt         # QR display screen
└── viewmodels/
    └── TvAuthViewModel.kt      # QR generation & polling
```

### Mobile QR Scanner
```
auth/ui/src/main/java/com/app/auth/ui/
├── screens/
│   └── QrScanScreen.kt         # Camera scanner screen
└── viewmodels/
    └── QrScanViewModel.kt      # Scanning logic
```

### Network API
```
core/network/src/main/java/com/app/core/network/
├── api/
│   └── TvAuthApiService.kt     # TV auth endpoints
└── model/
    └── TvAuthResponse.kt       # Response models
```

## 🔧 Build Commands

```bash
# Build TV App
./gradlew :app-tv:assembleDebug

# Install TV App
./gradlew :app-tv:installDebug

# Build Mobile App
./gradlew :app:assembleDebug

# Install Mobile App
./gradlew :app:installDebug

# Clean build
./gradlew clean build

# Sync Gradle
./gradlew sync
```

## 🌐 Backend Endpoints Needed

### 1. Create TV Session
```http
POST /auth/tv/create-session?sessionId={uuid}

Response:
{
  "success": true,
  "data": {
    "sessionId": "uuid-here",
    "authUrl": "watchtime://tv-auth?sessionId=uuid-here",
    "expiresAt": 1234567890
  }
}
```

### 2. Check TV Status (Polled by TV)
```http
GET /auth/tv/check-status?sessionId={uuid}

Response:
{
  "success": true,
  "data": {
    "authenticated": false,  // or true
    "token": "auth-token",   // if authenticated
    "userId": "user-id"      // if authenticated
  }
}
```

### 3. Link Mobile to TV
```http
POST /auth/tv/link
Headers: Authorization: Bearer {mobile-token}
Body: {
  "sessionId": "uuid-here"
}

Response:
{
  "success": true,
  "message": "TV linked successfully"
}
```

## 🔄 Authentication Flow

```
TV App                    Backend                    Mobile App
   │                         │                           │
   │─── Create Session ─────>│                           │
   │<──── QR Code URL ────────│                           │
   │                         │                           │
   │ [Display QR]            │                           │
   │                         │                           │
   │                         │<──── Scan QR ─────────────│
   │                         │                           │
   │                         │<──── Link Request ────────│
   │                         │   (with user token)       │
   │                         │                           │
   │                         │───── Success ─────────────>│
   │                         │                           │
   │─── Check Status ───────>│                           │
   │<──── Authenticated ─────│                           │
   │    (with token)         │                           │
   │                         │                           │
   │ [Navigate to Home]      │                           │
```

## 📝 Quick Integration Checklist

### Backend Setup
- [ ] Install Redis (or use in-memory storage)
- [ ] Create session storage logic
- [ ] Implement 3 endpoints
- [ ] Add rate limiting
- [ ] Test with Postman

### Mobile Integration
- [ ] Update `Screen.kt` with QR scan route
- [ ] Add composable in `AppNavigation.kt`
- [ ] Add button in Profile screen
- [ ] Test camera permissions
- [ ] Test QR scanning

### TV Content
- [ ] Create content rows in TvHomeScreen
- [ ] Connect to PopularViewModel
- [ ] Implement D-pad navigation
- [ ] Add media cards
- [ ] Test focus handling

## 🧪 Testing Checklist

### QR Authentication
- [ ] TV displays QR code
- [ ] QR code is scannable
- [ ] Mobile camera works
- [ ] Permission request appears
- [ ] QR detected correctly
- [ ] Session linked successfully
- [ ] TV receives authentication
- [ ] TV navigates to home
- [ ] Timeout works (5 minutes)

### TV Navigation
- [ ] D-pad up/down/left/right works
- [ ] Focus visible on cards
- [ ] Select button opens details
- [ ] Back button works
- [ ] Home button works

### Edge Cases
- [ ] Expired session
- [ ] Invalid QR code
- [ ] Network error
- [ ] Permission denied
- [ ] Multiple scans

## 🐛 Common Issues

### QR Not Scanning
- Check camera permission granted
- Ensure good lighting
- Hold phone steady
- QR code should be `watchtime://tv-auth?sessionId=xxx`

### TV Not Authenticating
- Verify backend is running
- Check polling interval (5 seconds)
- Verify session not expired
- Check logs for errors

### Build Errors
```bash
# Clean and rebuild
./gradlew clean
./gradlew build --refresh-dependencies

# Invalidate caches (in IDE)
File → Invalidate Caches → Invalidate and Restart
```

### Module Not Found
- Check `settings.gradle.kts` includes module
- Sync Gradle files
- Rebuild project

## 📦 Dependencies Quick List

```toml
# TV
androidx-tv-foundation = "1.1.0"
androidx-tv-material = "1.1.0"
androidx-leanback = "1.2.0-alpha06"

# QR Generation
zxing-core = "3.5.3"

# QR Scanning
zxing-android = "4.3.0"
camerax = "1.4.1"
mlkit-barcode = "17.3.0"
accompanist-permissions = "0.36.0"
```

## 🎨 UI Components

### TV Card (Focusable)
```kotlin
TvFocusableCard(
    title = "Movie Title",
    subtitle = "2024",
    onClick = { /* navigate */ }
)
```

### Mobile QR Scanner
```kotlin
QrScanScreen(
    onScanComplete = { /* navigate back */ }
)
```

### TV Auth Screen
```kotlin
TvAuthScreen(
    onAuthSuccess = { /* navigate to home */ }
)
```

## 🔗 URL Scheme

**TV Auth URL Format:**
```
watchtime://tv-auth?sessionId={uuid-v4}
```

**Example:**
```
watchtime://tv-auth?sessionId=550e8400-e29b-41d4-a716-446655440000
```

## ⚙️ Configuration

### Polling Settings
- Interval: 5 seconds
- Max attempts: 60
- Timeout: 5 minutes

### QR Code Settings
- Size: 512x512 pixels (generates), 400dp (displays)
- Format: QR_CODE
- Error correction: Medium

### Session Settings
- Expiration: 5-10 minutes (backend)
- One-time use: Yes
- Storage: Redis recommended

## 📖 Documentation Files

1. **TV_SUMMARY.md** - This file (quick reference)
2. **TV_IMPLEMENTATION.md** - Full technical docs
3. **TV_IMPLEMENTATION_STEPS.md** - Step-by-step plan

## 🚀 Next Steps Priority

1. **Backend API** (2-3 days) ⚠️ CRITICAL
2. **Mobile Navigation** (1 hour)
3. **TV Home Content** (2-3 days)
4. **Testing** (1-2 days)
5. **Polish** (1-2 days)

**Total MVP Time: ~7-10 days**

---

*Quick Reference Guide - WatchTime Android TV*
*Last Updated: November 30, 2025*

