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

## Images and Links

Notes can include an attached image. Use the overflow menu in the note editor to
attach or remove a picture. Image attachments appear above the note text.

Web links written in a note become clickable when the note is viewed in read
only mode. Links starting with `note://<id>` open another note inside the app.

## Trash Bin

Deleted notes are moved to a temporary trash bin. Items remain there for 30 days
unless restored or permanently removed earlier.

