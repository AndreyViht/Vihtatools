# Viht Tools Mobile

Android overlay application for Grand Mobile RolePlay administrators. Monitors incoming reports in real-time and provides quick-reply functionality without switching applications.

## Project Structure

```
VihtToolsMobile/
├── app/
│   ├── src/main/
│   │   ├── kotlin/com/vihttools/mobile/
│   │   │   ├── MainActivity.kt              # Main activity with Compose UI
│   │   │   ├── service/
│   │   │   │   ├── OverlayService.kt       # Floating button management
│   │   │   │   └── OCRMonitoringService.kt # Screen capture & OCR
│   │   │   ├── data/
│   │   │   │   ├── Report.kt               # Data models
│   │   │   │   ├── ReportDao.kt            # Database access
│   │   │   │   └── AppDatabase.kt          # Room database
│   │   │   └── ui/theme/
│   │   │       ├── Theme.kt                # Material 3 theme
│   │   │       └── Typography.kt           # Text styles
│   │   ├── res/
│   │   │   ├── values/
│   │   │   │   ├── strings.xml             # String resources
│   │   │   │   ├── colors.xml              # Color palette
│   │   │   │   └── themes.xml              # Theme definitions
│   │   └── AndroidManifest.xml             # App manifest
│   └── build.gradle.kts                    # App build config
├── build.gradle.kts                        # Root build config
├── settings.gradle.kts                     # Gradle settings
├── gradle.properties                       # Gradle properties
├── design.md                               # UI/UX design specification
└── todo.md                                 # Development tasks

```

## Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose + Material 3
- **OCR:** Google ML Kit Text Recognition v2
- **Screen Capture:** MediaProjection API
- **Overlay:** WindowManager TYPE_APPLICATION_OVERLAY
- **Database:** Room DB + SQLite
- **Preferences:** DataStore
- **Build System:** Gradle 8.2.0

## Key Features (Planned)

### Phase 1: Foundation & Permissions ✓
- Permission request system (Overlay + MediaProjection)
- Main screen with "Start Overlay" button
- Basic UI structure

### Phase 2: Floating Button & Overlay
- Draggable floating button "V"
- Button state colors (gray/red/green)
- Report count badge
- Position persistence

### Phase 3: OCR & Game Detection
- Screen capture via MediaProjection
- ML Kit text recognition
- Game detection (welcome message)
- Configurable scan intervals

### Phase 4: Report Detection & Queue
- Report pattern detection (Regex + RGB)
- Circular buffer (max 10 reports)
- Deduplication logic
- Room database storage

### Phase 5: Notifications & UI Panels
- Android notifications
- Reports list panel
- Quick reply panel
- Panel animations

### Phase 6: Template System
- Template management (add/edit/delete)
- Copy-to-clipboard functionality
- Report status tracking
- Answer blocking

### Phase 7: Settings Screen
- Button position selector
- Transparency slider
- OCR interval selector
- Theme toggle
- Template editor

### Phase 8: Polish & Testing
- Haptic feedback
- Battery optimization
- Real device testing
- Documentation

## Building & Running

### Prerequisites
- Android SDK 24+ (API level 24)
- Kotlin 1.9.22+
- Gradle 8.2.0+

### Build
```bash
./gradlew build
```

### Run on Device
```bash
./gradlew installDebug
```

### Debug
```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Permissions Required

- `SYSTEM_ALERT_WINDOW` — Display overlay over other apps
- `FOREGROUND_SERVICE` — Run foreground service
- `FOREGROUND_SERVICE_MEDIA_PROJECTION` — Screen capture service
- `POST_NOTIFICATIONS` — Send notifications
- `INTERNET` — ML Kit connectivity

## Important Notes

- OCR accuracy depends on screen resolution and font
- RGB color tolerance: ±40 per channel
- Android 12+ requires explicit MediaProjection confirmation per session
- Text input not automated (user pastes manually)
- Constant screen capture may impact battery life
- Minimum Android version: 8.0 (API 24)

## Development Workflow

1. Read `design.md` for UI/UX specifications
2. Check `todo.md` for current tasks
3. Follow Kotlin style guide (ktlint)
4. Write unit tests for business logic
5. Test on real device (emulator limitations with overlay)

## Architecture

The app follows a clean architecture pattern with separation of concerns:

- **UI Layer:** Jetpack Compose (MainActivity, Screens)
- **Service Layer:** OverlayService, OCRMonitoringService
- **Data Layer:** Room DB, DataStore preferences
- **Domain Layer:** Report models, business logic

## References

- [Android Overlay Documentation](https://developer.android.com/reference/android/view/WindowManager.LayoutParams#TYPE_APPLICATION_OVERLAY)
- [MediaProjection API](https://developer.android.com/reference/android/media/projection/MediaProjection)
- [ML Kit Text Recognition](https://developers.google.com/ml-kit/vision/text-recognition)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)

## License

Closed distribution. For internal use only.
