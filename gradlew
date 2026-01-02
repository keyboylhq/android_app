#!/usr/bin/env sh

# Set environment variables
export GRADLE_USER_HOME="$(pwd)/.gradle"
export JAVA_HOME="$(which java | xargs readlink -f | xargs dirname | xargs dirname)"
export ANDROID_HOME="/root/Android/Sdk"
export PATH="$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools/33.0.0:$ANDROID_HOME/cmdline-tools/latest/bin"

echo "Using Gradle with Android SDK: $ANDROID_HOME"
echo "Java Home: $JAVA_HOME"

# Use the downloaded Gradle 7.5 directly
gradle_bin="/tmp/gradle/gradle-7.5/bin/gradle"
if [ -f "$gradle_bin" ]; then
    echo "Using downloaded Gradle 7.5..."
    "$gradle_bin" "$@"
else
    echo "Error: Gradle 7.5 not found at $gradle_bin"
    exit 1
fi