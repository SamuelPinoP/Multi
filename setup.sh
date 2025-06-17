#!/usr/bin/env bash
set -euo pipefail

# Install required packages
sudo apt-get update
sudo apt-get install -y openjdk-17-jdk unzip

# Determine SDK directory
ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/android-sdk}"
CMDLINE_DIR="$ANDROID_SDK_ROOT/cmdline-tools/latest"

mkdir -p "$ANDROID_SDK_ROOT/cmdline-tools"

# Install Android command line tools if not already present
if [ ! -d "$CMDLINE_DIR" ]; then
  unzip -q android-commandlinetools.zip -d "$ANDROID_SDK_ROOT/cmdline-tools"
  mv "$ANDROID_SDK_ROOT/cmdline-tools/cmdline-tools" "$CMDLINE_DIR"
fi

export ANDROID_HOME="$ANDROID_SDK_ROOT"
export PATH="$PATH:$CMDLINE_DIR/bin:$ANDROID_HOME/platform-tools"

# Accept licenses and install required platforms and build tools
yes | "$CMDLINE_DIR/bin/sdkmanager" --licenses > /dev/null
"$CMDLINE_DIR/bin/sdkmanager" \
    "platform-tools" \
    "build-tools;35.0.0" \
    "platforms;android-35"

# Ensure JAVA_HOME is set
export JAVA_HOME="$(dirname $(dirname $(readlink -f $(which javac))))"

# Run unit tests
./gradlew --no-daemon test
