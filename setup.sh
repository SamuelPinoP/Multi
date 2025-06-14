#!/usr/bin/env bash
set -euo pipefail

# Directory where the Android SDK will be installed
SDK_DIR="${ANDROID_SDK_ROOT:-$HOME/android-sdk}"
CMDLINE_TOOLS_DIR="$SDK_DIR/cmdline-tools/latest"

# Extract the command line tools if they aren't already installed
if [ ! -d "$CMDLINE_TOOLS_DIR" ]; then
    mkdir -p "$SDK_DIR"
    rm -rf "$SDK_DIR/cmdline-tools" "$SDK_DIR/tmp-tools" 2>/dev/null || true
    mkdir "$SDK_DIR/tmp-tools"
    unzip -q "$(dirname "$0")/android-commandlinetools.zip" -d "$SDK_DIR/tmp-tools"
    mkdir -p "$CMDLINE_TOOLS_DIR"
    mv "$SDK_DIR/tmp-tools/cmdline-tools"/* "$CMDLINE_TOOLS_DIR"/
    rm -rf "$SDK_DIR/tmp-tools"
fi

export ANDROID_HOME="$SDK_DIR"
export ANDROID_SDK_ROOT="$SDK_DIR"
export PATH="$CMDLINE_TOOLS_DIR/bin:$PATH"

# Accept all Android SDK licenses
yes | sdkmanager --licenses >/dev/null

# Install the SDK packages required for building and testing
sdkmanager \
    "platform-tools" \
    "platforms;android-35" \
    "build-tools;35.0.0"

# Run the unit tests
./gradlew test
