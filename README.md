# Multi

This is a simple Android application using Jetpack Compose.

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


To install the Android SDK and run the unit tests from the command line you can use the `setup.sh` script provided in the project root:

```bash
./setup.sh
```

This script extracts the included command line tools, installs the required SDK packages and then executes the Gradle tests.

