#!/usr/bin/env bash
# Minimal Gradle 8.13 bootstrap for clean Codemagic Linux machines.
# Uses official distribution; does not need a wrapper JAR.
set -euo pipefail
VERSION=8.13
DIR="${GRADLE_USER_HOME:-$HOME/.gradle}/datzon-boot/gradle-$VERSION"
if [ ! -f "$DIR/bin/gradle" ]; then
  TMP="$(mktemp -d)"
  trap 'rm -rf "$TMP"' EXIT
  mkdir -p "$(dirname "$DIR")"
  curl --fail --location --retry 3 --retry-delay 2 "https://services.gradle.org/distributions/gradle-$VERSION-bin.zip" -o "$TMP/gradle.zip"
  unzip -q "$TMP/gradle.zip" -d "$(dirname "$DIR")"
fi
exec "$DIR/bin/gradle" "$@"
