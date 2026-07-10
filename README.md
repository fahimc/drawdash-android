# DrawDash

DrawDash is an original fast drawing Android game. Players race three simulated opponents, pick one of two prompts, draw quickly, and score when the local recogniser matches the drawing before the match timer expires.

## Gameplay

- Choose a 30, 60, 90, or 120 second match.
- Pick one of two drawable words each round.
- Draw on the responsive canvas using pencil, eraser, undo, redo, clear, colour, and stroke-size controls.
- The local recogniser evaluates stroke geometry and aliases after drawing pauses.
- AI opponents score independently using skill, speed, and error-rate profiles.
- Results show final ranking, score, completed words, skips, accuracy, and high-score status.

## Screenshots

Screenshots should be generated from emulator runs and placed in `screenshots/` before store submission. The app is fully runnable with `./gradlew assembleDebug`.

## Features

- Kotlin, Jetpack Compose, Material 3
- Responsive phone, landscape, and tablet layouts
- Timed match loop with countdown, pause/resume, skip penalties, and results
- Structured stroke model with smoothing, pressure, undo/redo, eraser, and history limits
- Modular `DrawingRecognizer` interface with a deterministic local heuristic recogniser
- DataStore-backed settings and statistics
- Bundled local word database with 150+ easy, 150+ medium, and 100+ hard prompts
- Unit tests and a Compose UI smoke test
- GitHub Actions for CI and tag-based release builds

## Technology Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Coroutines and Flow
- DataStore Preferences
- JUnit and AndroidX Compose UI tests
- Gradle Kotlin DSL

## Architecture

The project uses one Android app module with internal package boundaries:

- `model`: immutable state and domain models
- `data`: preferences and local word database
- `drawing`: stroke editing engine
- `recognition`: recogniser interface and local implementation
- `engine`: match state machine and AI opponent scheduler
- `MainActivity.kt`: Compose screens and adaptive UI

The game loop is owned by `DrawDashViewModel`; composables render state and send user intents back to the view model.

## Local Development

Requirements:

- Android Studio or JDK 17
- Android SDK 34
- Windows, macOS, or Linux shell

Run:

```bash
./gradlew assembleDebug
```

Tests:

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
```

Install debug APK:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Recognition

Recognition is implemented behind `DrawingRecognizer`. The first release uses `HeuristicDrawingRecognizer`, which evaluates stroke count, bounding box, aspect ratio, closed paths, corner changes, ink density, aliases, plurals, and prompt categories. It runs off the main thread and is debounced by the game view model after drawing pauses.

Known limitation: this is not a full object-classification model. The architecture allows swapping in ML Kit Digital Ink or TensorFlow Lite without changing the UI.

## Adding Words

Add prompts to `WordBank.kt` using `Name:category`. The generated `WordEntry` includes a stable id, display name, category, difficulty, aliases, and recognition labels. Avoid abstract or inappropriate prompts and avoid pairing near-identical words.

## Signing

For local release builds:

```bash
set ANDROID_KEYSTORE_PATH=C:\path\to\drawdash-release.jks
set ANDROID_KEYSTORE_PASSWORD=...
set ANDROID_KEY_ALIAS=drawdash
set ANDROID_KEY_PASSWORD=...
./gradlew assembleRelease bundleRelease
```

For GitHub Actions, configure these secrets:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

## Creating a Release

Push a version tag:

```bash
git tag v1.0.0
git push origin v1.0.0
```

The release workflow builds signed APK/AAB artifacts, generates SHA-256 checksums, and publishes a GitHub Release.

APK download link after publishing:

`https://github.com/<owner>/drawdash-android/releases/latest`

## Known Limitations

- Recognition is local and deterministic but not yet a trained object classifier.
- Sound settings are persisted; custom sound assets are reserved for a later pass.
- Screenshots are documented but should be captured from emulator/device runs before marketplace submission.

## Roadmap

- ML Kit or TensorFlow Lite recogniser plugin
- More recogniser-specific word metadata
- Richer sound pack and music controls
- Screenshot automation in CI
- Store-ready adaptive icons and tablet screenshots
