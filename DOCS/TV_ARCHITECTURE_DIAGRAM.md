# WatchTime Android TV - Architecture Diagram

## Module Structure

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          WatchTime Project                               │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌──────────────────────┐              ┌──────────────────────┐        │
│  │    Mobile App        │              │      TV App          │        │
│  │    (:app)            │              │    (:app-tv)         │        │
│  │                      │              │                      │        │
│  │  - MainActivity      │              │  - TvMainActivity    │        │
│  │  - AppNavigation     │              │  - TvAppNavigation   │        │
│  │  - WatchTimeApp      │              │  - WatchTimeTvApp    │        │
│  └──────────┬───────────┘              └──────────┬───────────┘        │
│             │                                     │                     │
│             │                                     │                     │
│  ┌──────────▼───────────────────────────────────▼───────────┐         │
│  │              UI Layer (Platform Specific)                 │         │
│  ├───────────────────────────────────────────────────────────┤         │
│  │                                                            │         │
│  │  Mobile UI               │             TV UI              │         │
│  │  ┌───────────────┐      │       ┌───────────────┐       │         │
│  │  │ auth:ui       │      │       │ auth:tv-ui    │       │         │
│  │  │ - AuthScreen  │      │       │ - TvAuthScreen│       │         │
│  │  │ - QrScan      │      │       │ - QR Display  │       │         │
│  │  └───────────────┘      │       └───────────────┘       │         │
│  │                          │                                │         │
│  │  ┌───────────────┐      │       ┌───────────────┐       │         │
│  │  │ popular:ui    │      │       │ popular:tv-ui │       │         │
│  │  │ discover:ui   │      │       │ discover:tv-ui│       │         │
│  │  │ media:ui      │      │       │ media:tv-ui   │       │         │
│  │  │ collections:ui│      │       │ collections:  │       │         │
│  │  │ profile:ui    │      │       │   tv-ui       │       │         │
│  │  └───────────────┘      │       └───────────────┘       │         │
│  │                          │                                │         │
│  │  ┌───────────────┐      │       ┌───────────────┐       │         │
│  │  │ core:ui       │      │       │ core:tv-ui    │       │         │
│  │  │ - Theme       │      │       │ - TvCards     │       │         │
│  │  │ - Components  │      │       │ - TvTheme     │       │         │
│  │  └───────────────┘      │       └───────────────┘       │         │
│  └────────────────────────────────────────────────────────────┘       │
│                             │                                          │
│                             ▼                                          │
│  ┌────────────────────────────────────────────────────────────┐       │
│  │              Domain Layer (Shared Business Logic)          │       │
│  ├────────────────────────────────────────────────────────────┤       │
│  │                                                             │       │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐          │       │
│  │  │ auth:      │  │ popular:   │  │ discover:  │          │       │
│  │  │ domain     │  │ domain     │  │ domain     │          │       │
│  │  │            │  │            │  │            │          │       │
│  │  │ - User     │  │ - Media    │  │ - Genre    │          │       │
│  │  │ - Auth     │  │ - Popular  │  │ - Discover │          │       │
│  │  │   Repo     │  │   Repo     │  │   Repo     │          │       │
│  │  └────────────┘  └────────────┘  └────────────┘          │       │
│  │                                                             │       │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐          │       │
│  │  │ media:     │  │collections:│  │ profile:   │          │       │
│  │  │ domain     │  │ domain     │  │ domain     │          │       │
│  │  └────────────┘  └────────────┘  └────────────┘          │       │
│  └────────────────────────────────────────────────────────────┘       │
│                             │                                          │
│                             ▼                                          │
│  ┌────────────────────────────────────────────────────────────┐       │
│  │              Data Layer (Shared Implementations)           │       │
│  ├────────────────────────────────────────────────────────────┤       │
│  │                                                             │       │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐          │       │
│  │  │ auth:data  │  │ popular:   │  │ discover:  │          │       │
│  │  │            │  │ data       │  │ data       │          │       │
│  │  │ - Firebase │  │            │  │            │          │       │
│  │  │ - Supabase │  │ - API      │  │ - API      │          │       │
│  │  │ - Repo     │  │ - Repo     │  │ - Repo     │          │       │
│  │  │   Impl     │  │   Impl     │  │   Impl     │          │       │
│  │  └────────────┘  └────────────┘  └────────────┘          │       │
│  │                                                             │       │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐          │       │
│  │  │ media:data │  │collections:│  │ profile:   │          │       │
│  │  │            │  │ data       │  │ data       │          │       │
│  │  └────────────┘  └────────────┘  └────────────┘          │       │
│  └────────────────────────────────────────────────────────────┘       │
│                             │                                          │
│                             ▼                                          │
│  ┌────────────────────────────────────────────────────────────┐       │
│  │              Core Layer (Shared Infrastructure)            │       │
│  ├────────────────────────────────────────────────────────────┤       │
│  │                                                             │       │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐          │       │
│  │  │ core:      │  │ core:room  │  │ core:utils │          │       │
│  │  │ network    │  │            │  │            │          │       │
│  │  │            │  │ - Database │  │ - Constants│          │       │
│  │  │ - Retrofit │  │ - DAOs     │  │ - Helpers  │          │       │
│  │  │ - API      │  │ - Entities │  │ - Failures │          │       │
│  │  │ - TV Auth  │  │            │  │            │          │       │
│  │  │   API      │  │            │  │            │          │       │
│  │  └────────────┘  └────────────┘  └────────────┘          │       │
│  │                                                             │       │
│  │  ┌���───────────┐                                            │       │
│  │  │ core:      │                                            │       │
│  │  │ navigation │                                            │       │
│  │  │            │                                            │       │
│  │  │ - Routes   │                                            │       │
│  │  │ - NavGraph │                                            │       │
│  │  └────────────┘                                            │       │
│  └────────────────────────��───────────────────────────────────┘       │
└─────────────────────────────────────────────────────────────────────────┘
```

## Data Flow - QR Authentication

```
┌────────────────────┐
│   TV App Launch    │
└─────────┬──────────┘
          │
          ▼
┌────────────────────────────────┐
│ TvAuthViewModel.initiateQRAuth │
└─────────┬──────────────────────┘
          │
          ▼
┌────────────────────────────────┐
│ Generate Session ID (UUID)     │
└─────────┬──────────────────────┘
          │
          ▼
┌─────────────────────────────────────────┐
│ TvAuthApiService.createTvAuthSession()  │
│                                          │
│ POST /auth/tv/create-session            │
│   ?sessionId={uuid}                     │
└─────────┬───────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────┐
│ Backend Creates Session                 │
│                                          │
│ Redis: tv_auth:{sessionId} = {          │
│   authenticated: false,                 │
│   token: null,                          │
│   expiresAt: +5min                      │
│ }                                        │
└─────────┬───────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────┐
│ Return Auth URL                         │
│ "watchtime://tv-auth?sessionId={uuid}"  │
└─────────┬───────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────┐
│ Generate QR Code (ZXing)                │
│ 512x512 Bitmap                          │
└─────────┬───────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────┐
│ Display QR Code on TV (400dp)           │
│ + Instructions                          │
└─────────┬───────────────────────────────┘
          │
          ├──── Start Polling ────┐
          │                        │
          │                        ▼
          │              ┌──────────────────────┐
          │              │ Every 5 seconds:     │
          │              │ Check Auth Status    │
          │              │                      │
          │              │ GET /auth/tv/        │
          │              │   check-status       │
          │              └──────────────────────┘
          │                        │
          │                        │ (Not authenticated yet)
          │                        │
          │                        ▼
          │              ┌──────────────────────┐
          │              │ Backend Returns:     │
          │              │ { authenticated:     │
          │              │   false }            │
          │              └──────────────────────┘
          │                        │
          │                        │ (Continue polling)
          │                        │
┌─────────▼────────────────────────▼─────────────┐
│        User Opens Mobile App                   │
│        Navigates to QR Scanner                 │
└─────────┬──────────────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────┐
│ QrScanScreen - Request Camera Permission│
└─────────┬───────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────┐
│ CameraX Preview + ML Kit Scanner        │
└─────────┬───────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────┐
│ Detect QR Code                          │
│ Extract: sessionId from URL             │
└─────────┬───────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────┐
│ QrScanViewModel.onQrCodeScanned()       │
└─────────┬───────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────┐
│ TvAuthApiService.linkMobileToTv()       │
│                                          │
│ POST /auth/tv/link                      │
│ Headers: Authorization: Bearer {token}  │
│ Body: { sessionId }                     │
└─────────┬───────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────┐
│ Backend Validates User Token            │
│ Updates Session:                        │
│ Redis: tv_auth:{sessionId} = {          │
│   authenticated: true,                  │
│   token: {user_token},                  │
│   userId: {user_id}                     │
│ }                                        │
└─────────┬───────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────┐
│ Mobile Shows Success Message            │
└─────────────────────────────────────────┘


          ┌─────────────────────────────────────┐
          │ TV Polling Continues...             │
          │                                      │
          │ GET /auth/tv/check-status            │
          └─────────┬───────────────────────────┘
                    │
                    ▼
          ┌─────────────────────────────────────┐
          │ Backend Returns:                    │
          │ {                                    │
          │   authenticated: true,               │
          │   token: {user_token},               │
          │   userId: {user_id}                  │
          │ }                                    │
          └─────────┬───────────────────────────┘
                    │
                    ▼
          ┌─────────────────────────────────────┐
          │ TvAuthViewModel Receives Auth       │
          │ Stores Token                        │
          │ Updates State to Authenticated      │
          └─────────┬───────────────────────────┘
                    │
                    ▼
          ┌─────────────────────────────────────┐
          │ TvAuthScreen Observes State         │
          │ Triggers onAuthSuccess()            │
          └─────────┬───────────────────────────┘
                    │
                    ▼
          ┌─────────────────────────────────────┐
          │ Navigate to TvHomeScreen            │
          │ User is now logged in!              │
          └─────────────────────────────────────┘
```

## Dependency Graph

```
app-tv
  ├─> core:tv-ui
  ├─> auth:tv-ui
  │     ├─> auth:domain
  │     ├─> core:utils
  │     └─> core:network
  ├─> popular:tv-ui
  │     ├─> popular:domain
  │     └─> core:tv-ui
  ├─> discover:tv-ui
  ├─> media:tv-ui
  ├─> collections:tv-ui
  ├─> auth:data
  │     ├─> auth:domain
  │     └─> core:network
  ├─> popular:data
  ├─> discover:data
  ├─> media:data
  ├─> collections:data
  └─> core modules
        ├─> core:ui
        ├─> core:network
        ├─> core:room
        └─> core:utils

app (mobile)
  ├─> auth:ui (updated with QR scanner)
  │     ├─> auth:domain
  │     ├─> core:utils
  │     └─> core:network
  ├─> popular:ui
  ├─> discover:ui
  ├─> media:ui
  ├─> collections:ui
  ├─> auth:data
  └─> feature data modules
```

## Technology Stack

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
├──────────────────┬──────────────────────────────────────┤
│   Mobile UI      │            TV UI                     │
│                  │                                       │
│ • Jetpack        │ • Jetpack Compose for TV            │
│   Compose        │ • TV Material Design                 │
│ • Material 3     │ • Leanback                           │
│ • Navigation     │ • D-pad Navigation                   │
│   Compose        │                                       │
│ • CameraX        │ • QR Code Display (ZXing)            │
│ • ML Kit         │                                       │
│   Barcode        │                                       │
└──────────────────┴──────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                   ViewModel Layer                        │
├─────────────────────────────────────────────────────────┤
│ • Kotlin Coroutines                                     │
│ • StateFlow / MutableState                              │
│ • Lifecycle-aware                                       │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                    Domain Layer                          │
├─────────────────────────────────────────────────────────┤
│ • Pure Kotlin Modules                                   │
│ • Repository Interfaces                                 │
│ • Entity Models                                         │
│ • Use Cases (if needed)                                 │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                     Data Layer                           │
├─────────────────────────────────────────────────────────┤
│ • Retrofit (REST API)                                   │
│ • OkHttp (Networking)                                   │
│ • Kotlin Serialization (JSON)                           │
│ • Room (Local Database)                                 │
│ • Firebase Auth                                         │
│ • Supabase                                              │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                  Infrastructure                          │
├─────────────────────────────────────────────────────────┤
│ • Koin (Dependency Injection)                           │
│ • Coil (Image Loading)                                  │
│ • Desugar (Java 8+ APIs)                                │
└─────────────────────────────────────────────────────────┘
```

## File Count Summary

```
New Modules:        8
New Files:         30+
Modified Files:     5
Configuration:      3
Documentation:      4

Total Impact:      50+ files
```

## Build Time Estimate

```
Module Compilation (Parallel):
├─ core modules:           ~30s
├─ domain modules:         ~20s
├─ data modules:           ~40s
├─ tv-ui modules:          ~50s
├─ app-tv:                 ~45s
└─ app (mobile):           ~45s

Total Build Time:         ~3-4 minutes (clean build)
Incremental Build:        ~30-60 seconds
```

---

*Architecture Diagram - WatchTime Android TV*
*Multi-Module Clean Architecture*

