# Multi

This is a simple Android application using Jetpack Compose.

The home screen now shows a Material Design date picker dialog when the
Calendar segment is tapped. Ensure your Gradle configuration includes the
Compose Material 3 dependency:

```kotlin
implementation("androidx.compose.material3:material3")
```

## Prerequisites

- Android Studio Hedgehog or later with the Android SDK configured
- JDK 11 or later

## Building with Android Studio

1. Open the project in Android Studio.
2. Let the IDE download any required dependencies.
3. Select **Run \u2192 Run 'app'** to build and deploy to a connected device or emulator.

## Building from the command line

Use the provided Gradle wrapper to build the debug APK:

```bash
./gradlew assembleDebug
```

The generated APK can be found in `app/build/outputs/apk/debug/`.

To run unit tests:

```bash
./gradlew test
```

## Note deletion policy

When a note is deleted it is moved to the **Trash Bin** rather than
being removed permanently. Notes remain there for 30 days and can be
restored at any time during that period. After 30 days they are
automatically purged.

