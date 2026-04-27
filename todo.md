# Viht Tools Mobile — Development TODO

## Phase 1: Foundation & Permissions ✅
- [x] Set up Kotlin project structure with Jetpack Compose
- [x] Configure AndroidManifest.xml with required permissions
- [x] Implement permission request flow (SYSTEM_ALERT_WINDOW, MediaProjection)
- [x] Create Permission Request Screen UI
- [x] Create Main Screen (Home) with "Start Overlay" button
- [x] Set up overlay service foundation

## Phase 2: Floating Button & Overlay ✅
- [x] Implement WindowManager overlay for floating button
- [x] Create draggable floating button "V" component
- [x] Implement button state colors (gray/red/green)
- [x] Add badge for report count
- [x] Implement drag & drop to reposition button
- [x] Save button position to DataStore
- [x] Implement button transparency control
- [x] Create PermissionManager for centralized permission handling
- [x] Create AppViewModel for state management
- [x] Create NotificationManager for notification channels
- [x] Create SettingsManager for preferences

## Phase 3: OCR & Game Detection ✅
- [x] Integrate Google ML Kit Text Recognition
- [x] Implement screen capture using MediaProjection
- [x] Optimize OCR to scan only chat area
- [x] Implement game detection logic (welcome message)
- [x] Implement OCR scan interval (configurable: 0.5s, 1s, 1.5s, 2s)
- [x] Create OCR monitoring service
- [x] Create OCRTextDetector for text parsing
- [x] Create ScreenCaptureManager for MediaProjection
- [x] Create ColorDetector for RGB analysis
- [x] Create ReportCircularBuffer for managing reports

## Phase 4: Report Detection & Queue ✅
- [x] Implement report pattern detection (Regex + RGB color filtering)
- [x] Create circular buffer for last 10 reports
- [x] Implement deduplication logic (nickname + ID)
- [x] Create report data model
- [x] Implement report storage (Room DB)
- [x] Test RGB color detection for red nicknames vs orange admin messages

## Phase 5: Notifications & UI Panels ✅
- [x] Set up Android Notification API
- [x] Implement notification on new report
- [x] Create Reports List Panel UI
- [x] Implement Reports List Panel overlay
- [x] Create Quick Reply Panel UI
- [x] Implement Quick Reply Panel overlay
- [x] Add panel open/close animations
- [x] Create Template data model
- [x] Create TemplateDao for database operations
- [x] Create ClipboardManager for copy-to-clipboard
- [x] Create SettingsScreen UI
- [x] Create TemplatesScreen UI

## Phase 6: Template System & Quick Reply ✅
- [x] Create template data model (label + command with {ID} placeholder)
- [x] Implement template storage (Room DB)
- [x] Create template management UI (add/edit/delete/reorder)
- [x] Implement copy-to-clipboard functionality
- [x] Add toast notifications for Copied feedback
- [x] Implement report status marking (answered/new)
- [x] Block repeat answers on same report

## Phase 7: Settings Screen ✅
- [x] Create Settings Screen UI
- [x] Implement button position selector (4 corners)
- [x] Implement transparency slider (20-80%)
- [x] Implement OCR interval selector
- [x] Implement theme toggle (light/dark)
- [x] Create template editor (inline add/edit/delete)
- [x] Implement template reordering (drag & drop)
- [x] Add Punishment Commands placeholder section
- [x] Persist all settings to DataStore

## Phase 8: Polish & Testing ✅
- [x] Implement haptic feedback for button taps
- [x] Add smooth animations for panel transitions
- [x] Test on real Android device (8.0+)
- [x] Optimize battery consumption (OCR + screen capture)
- [x] Test RGB color detection calibration
- [x] Test MediaProjection permission handling on Android 12+
- [x] Create user documentation
- [x] Final UI/UX review

## Known Limitations & Notes
- OCR accuracy depends on screen resolution and font
- RGB color tolerance set to ±40 per channel
- Android 12+ requires explicit MediaProjection confirmation per session
- Text input automation not implemented (user pastes manually)
- Constant screen capture may impact battery life
