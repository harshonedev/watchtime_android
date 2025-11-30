# TV Authentication Testing Checklist

## Pre-Testing Setup

- [ ] Backend API is running and accessible
- [ ] Base URL is correctly configured in `Constants.kt`
- [ ] TV app installed on Android TV device/emulator
- [ ] Mobile app installed on Android phone with camera
- [ ] Mobile app user is signed in with valid account

## TV App Testing

### ✅ Initial Authentication Flow

#### Test Case 1: First Launch - QR Code Display
- [ ] Launch TV app for the first time
- [ ] Verify app shows authentication screen
- [ ] Verify QR code is displayed clearly
- [ ] Verify QR code is readable and well-sized
- [ ] Verify instructions are displayed:
  - "Scan QR Code to Sign In"
  - Step-by-step instructions
- [ ] Check logs for successful session creation
  - Look for: `"Creating TV auth session with ID: ..."`
  - Look for: `"Auth session created successfully"`

#### Test Case 2: QR Code Contains Correct Data
- [ ] Use mobile QR scanner app (not WatchTime app yet)
- [ ] Scan QR code
- [ ] Verify format: `watchtime://tv-auth?sessionId={uuid}`
- [ ] Verify sessionId is a valid UUID
- [ ] Try scanning multiple times (should be same session)

#### Test Case 3: Polling Starts Automatically
- [ ] Check logs for polling messages
  - Look for: `"Starting to poll for authentication status..."`
- [ ] Verify polling happens every ~5 seconds
- [ ] Leave for 1-2 minutes, verify continued polling
- [ ] Verify no crashes or memory leaks

#### Test Case 4: Error Handling - Network Issues
- [ ] Disconnect network before launching app
- [ ] Launch TV app
- [ ] Verify error message is displayed
- [ ] Verify message is user-friendly (not technical error)
- [ ] Reconnect network
- [ ] Click "Retry" button
- [ ] Verify QR code loads successfully

#### Test Case 5: Session Expiry
- [ ] Launch TV app and display QR code
- [ ] Wait 10 minutes without scanning
- [ ] Verify timeout message appears
- [ ] Verify retry option is available
- [ ] Click retry
- [ ] Verify new QR code is generated

### ✅ Post-Authentication Flow

#### Test Case 6: Successful Authentication
- [ ] Display QR code on TV
- [ ] Scan with authenticated mobile app
- [ ] Verify TV shows "Authentication Successful!" message
- [ ] Verify TV navigates to home screen automatically
- [ ] Check logs for token save:
  - Look for: `"Authentication successful! Saving token..."`
  - Look for: `"Token saved successfully"`

#### Test Case 7: Home Screen Display
- [ ] Verify home screen loads
- [ ] Verify app title displayed: "WatchTime TV"
- [ ] Verify placeholder content sections visible
- [ ] Verify "Sign Out" button is present
- [ ] Verify UI is responsive to D-pad navigation

#### Test Case 8: Persistent Authentication
- [ ] After successful auth, close TV app completely
- [ ] Relaunch TV app
- [ ] Verify app goes directly to home screen
- [ ] Verify NO QR code is shown
- [ ] Verify authentication is remembered

#### Test Case 9: Sign Out
- [ ] On home screen, navigate to "Sign Out" button
- [ ] Click "Sign Out"
- [ ] Verify app navigates to auth screen
- [ ] Verify new QR code is generated
- [ ] Close and relaunch app
- [ ] Verify app shows auth screen (not home)

## Mobile App Testing

### ✅ QR Scanning Flow

#### Test Case 10: Camera Permission
- [ ] Launch QR scan feature (first time)
- [ ] Verify camera permission request appears
- [ ] Deny permission
- [ ] Verify message: "Camera permission is required..."
- [ ] Verify "Grant Permission" button shown
- [ ] Click "Grant Permission"
- [ ] Grant permission
- [ ] Verify camera preview appears

#### Test Case 11: QR Code Scanning
- [ ] Open QR scan feature
- [ ] Point camera at TV QR code
- [ ] Verify scan happens automatically
- [ ] Verify no manual trigger needed
- [ ] Verify scan success vibration/feedback (if implemented)
- [ ] Verify processing state shown
- [ ] Verify success message displayed

#### Test Case 12: Invalid QR Code
- [ ] Scan a random QR code (not WatchTime)
- [ ] Verify error message: "Invalid QR code"
- [ ] Verify can retry scanning
- [ ] Scan a valid WatchTime QR code
- [ ] Verify linking proceeds

#### Test Case 13: User Not Signed In
- [ ] Sign out of mobile app
- [ ] Try to scan TV QR code
- [ ] Verify error: "You must be signed in to link TV"
- [ ] Sign in to mobile app
- [ ] Retry scan
- [ ] Verify linking succeeds

#### Test Case 14: Network Issues During Link
- [ ] Disconnect mobile network
- [ ] Scan TV QR code
- [ ] Verify error: "Network error. Please check..."
- [ ] Reconnect network
- [ ] Click "Try Again"
- [ ] Verify linking succeeds

#### Test Case 15: Already Linked Session
- [ ] Successfully link a TV session
- [ ] On another mobile device, scan same QR code
- [ ] Verify appropriate error message
- [ ] Verify first TV still works

## Integration Testing

### ✅ End-to-End Flow

#### Test Case 16: Complete Happy Path
1. [ ] Fresh install of TV app
2. [ ] Launch TV app
3. [ ] QR code appears
4. [ ] Launch mobile app (signed in)
5. [ ] Open QR scanner
6. [ ] Scan TV QR code
7. [ ] Mobile shows "TV Linked Successfully!"
8. [ ] TV shows "Authentication Successful!"
9. [ ] TV navigates to home
10. [ ] Close TV app
11. [ ] Reopen TV app
12. [ ] TV goes directly to home

#### Test Case 17: Multiple TVs
- [ ] Set up TV #1, scan and link
- [ ] Set up TV #2, scan and link
- [ ] Verify both TVs work independently
- [ ] Verify each has its own token
- [ ] Sign out of TV #1
- [ ] Verify TV #2 still authenticated

#### Test Case 18: Multiple Users
- [ ] User A links TV
- [ ] Sign out on TV
- [ ] User B links same TV
- [ ] Verify User B's account is active
- [ ] Verify User A's session invalidated

### ✅ API Testing

#### Test Case 19: Create Session Endpoint
```bash
SESSION_ID=$(uuidgen)
curl -X POST "https://boc4cgg8sgkkgw84wk8gk80c.harshone.dev/api/auth/tv/create-session?sessionId=$SESSION_ID"
```
- [ ] Returns 200 OK
- [ ] Response has `success: true`
- [ ] Response has `data.authUrl`
- [ ] Response has `data.expiresAt`
- [ ] Session ID in response matches request

#### Test Case 20: Check Status Endpoint (Unauthenticated)
```bash
curl "https://boc4cgg8sgkkgw84wk8gk80c.harshone.dev/api/auth/tv/check-status?sessionId=$SESSION_ID"
```
- [ ] Returns 200 OK
- [ ] Response has `authenticated: false`
- [ ] Response has `token: null`
- [ ] Response has `userId: null`

#### Test Case 21: Link Endpoint
```bash
JWT_TOKEN="your_actual_jwt_token"
curl -X POST "https://boc4cgg8sgkkgw84wk8gk80c.harshone.dev/api/auth/tv/link" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d "{\"sessionId\": \"$SESSION_ID\"}"
```
- [ ] Returns 200 OK
- [ ] Response has `success: true`
- [ ] Response has success message

#### Test Case 22: Check Status Endpoint (Authenticated)
```bash
curl "https://boc4cgg8sgkkgw84wk8gk80c.harshone.dev/api/auth/tv/check-status?sessionId=$SESSION_ID"
```
- [ ] Returns 200 OK
- [ ] Response has `authenticated: true`
- [ ] Response has valid `token`
- [ ] Response has valid `userId`

## Performance Testing

### ✅ Performance Metrics

#### Test Case 23: QR Code Generation Speed
- [ ] Launch TV app
- [ ] Time from app start to QR display
- [ ] Should be < 3 seconds
- [ ] Monitor memory usage
- [ ] No memory leaks

#### Test Case 24: Polling Efficiency
- [ ] Monitor network requests
- [ ] Verify polling interval is 5 seconds
- [ ] Verify no duplicate requests
- [ ] Verify proper request cancellation on auth
- [ ] Check battery usage (should be minimal)

#### Test Case 25: Authentication Speed
- [ ] Start timer when QR is scanned
- [ ] Stop when TV navigates to home
- [ ] Total time should be < 10 seconds
- [ ] Most time should be network latency

## Edge Cases & Error Testing

### ✅ Edge Cases

#### Test Case 26: Rapid Retry
- [ ] Get error on TV
- [ ] Click "Retry" rapidly multiple times
- [ ] Verify no crashes
- [ ] Verify only one session created
- [ ] Verify proper cleanup

#### Test Case 27: App Backgrounding
- [ ] Display QR code on TV
- [ ] Press home button (background app)
- [ ] Wait 2 minutes
- [ ] Return to app
- [ ] Verify polling resumes
- [ ] Scan QR and verify link works

#### Test Case 28: Screen Rotation (Mobile)
- [ ] Start QR scan on mobile
- [ ] Rotate device
- [ ] Verify camera preview adjusts
- [ ] Scan QR code
- [ ] Verify link still works

#### Test Case 29: Low Memory
- [ ] Open many apps on TV
- [ ] Launch WatchTime TV app
- [ ] Verify QR code still loads
- [ ] Complete authentication
- [ ] Verify no crashes

#### Test Case 30: Slow Network
- [ ] Use network throttling (if available)
- [ ] Set to slow 3G speed
- [ ] Launch TV app
- [ ] Verify loading states shown
- [ ] Verify eventual success
- [ ] Complete authentication flow

## Security Testing

### ✅ Security Checks

#### Test Case 31: Token Storage
- [ ] Authenticate TV
- [ ] Use ADB to inspect SharedPreferences
  ```bash
  adb shell run-as com.app.watchtime.tv
  cat shared_prefs/watchtime_auth_prefs.xml
  ```
- [ ] Verify token is stored
- [ ] Verify it's not in logcat output
- [ ] Sign out
- [ ] Verify token is removed

#### Test Case 32: HTTPS Usage
- [ ] Monitor network traffic
- [ ] Verify all API calls use HTTPS
- [ ] Verify no plaintext sensitive data
- [ ] Verify proper TLS version

#### Test Case 33: Session Isolation
- [ ] Create session on TV #1
- [ ] Try to use session ID on TV #2
- [ ] Verify it doesn't work
- [ ] Verify proper error message

## Logging Verification

### ✅ Log Checks

Check for these logs during testing:

**TV App:**
```
"TvAuthViewModel" - Session creation
"TvAuthViewModel" - QR code generation  
"TvAuthViewModel" - Polling status
"TvAuthViewModel" - Token save
"AuthRepositoryImpl" - Token storage
```

**Mobile App:**
```
"QrScanViewModel" - QR scan detection
"QrScanViewModel" - Session ID extraction
"QrScanViewModel" - Link request
"QrScanViewModel" - Success/error
```

## Sign-Off Criteria

All tests must pass before production release:

- [ ] All TV app test cases pass (1-9)
- [ ] All mobile app test cases pass (10-15)
- [ ] All integration test cases pass (16-18)
- [ ] All API test cases pass (19-22)
- [ ] All performance test cases pass (23-25)
- [ ] All edge case test cases pass (26-30)
- [ ] All security test cases pass (31-33)
- [ ] No critical bugs found
- [ ] No memory leaks detected
- [ ] Logs are clean (no errors in happy path)

## Known Issues / Notes

(Document any known issues discovered during testing)

---

**Tester Name:** _________________  
**Date:** _________________  
**Build Version:** _________________  
**Test Environment:** _________________  


