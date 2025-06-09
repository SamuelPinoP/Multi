#!/usr/bin/env bash
set -euo pipefail

# Display basic environment information
java_version=$(java -version 2>&1 | head -n 1)
gradle_version=$(gradle -v | head -n 1)
echo "Java: $java_version"
echo "Gradle: $gradle_version"

# Detect network access to Gradle services
if curl -I https://services.gradle.org >/dev/null 2>&1; then
  echo "Network access detected. Running Gradle normally."
  OFFLINE=""
else
  echo "No network access to services.gradle.org. Running Gradle in offline mode."
  OFFLINE="--offline"
fi

# Use system Gradle if available; fall back to wrapper
if command -v gradle >/dev/null 2>&1; then
  GRADLE_CMD=gradle
else
  GRADLE_CMD=./gradlew
fi

# Run the tests
if ! $GRADLE_CMD test --no-daemon $OFFLINE; then
  echo
  echo "Gradle build failed. The environment may be missing cached dependencies." >&2
  exit 1
fi
