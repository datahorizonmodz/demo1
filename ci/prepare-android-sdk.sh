#!/usr/bin/env bash
# Codemagic does not guarantee that Android command-line tools are on PATH.
# Compatible with the system Bash 3.2 on macOS as well as Linux.
set -euo pipefail

sdk_root=""
for candidate in "${ANDROID_SDK_ROOT:-}" "${ANDROID_HOME:-}"; do
  if [ -n "$candidate" ] && [ -d "$candidate" ]; then
    sdk_root="$candidate"
    break
  fi
done
if [ -z "$sdk_root" ]; then
  echo "Android SDK directory not found. Check ANDROID_SDK_ROOT / ANDROID_HOME." >&2
  exit 1
fi

packages_ready() {
  [ -f "$sdk_root/platforms/android-36/android.jar" ] &&
    [ -f "$sdk_root/build-tools/35.0.0/aapt2" ]
}

if ! packages_ready; then
  sdk_manager=""
  for candidate in "$sdk_root/cmdline-tools/latest/bin/sdkmanager" \
    "$sdk_root"/cmdline-tools/*/bin/sdkmanager \
    "$sdk_root/tools/bin/sdkmanager"; do
    if [ -x "$candidate" ]; then
      sdk_manager="$candidate"
      break
    fi
  done
  if [ -z "$sdk_manager" ]; then
    sdk_manager="$(command -v sdkmanager || true)"
  fi
  if [ -z "$sdk_manager" ] || [ ! -x "$sdk_manager" ]; then
    echo "Required SDK packages are missing and sdkmanager was not found under $sdk_root or on PATH." >&2
    exit 1
  fi
  echo "Installing pinned Android SDK packages using $sdk_manager"
  "$sdk_manager" --sdk_root="$sdk_root" "platforms;android-36" "build-tools;35.0.0"
fi

if ! packages_ready; then
  echo "Android SDK 36 / build-tools 35.0.0 are still incomplete after installation." >&2
  exit 1
fi

printf 'sdk.dir=%s\n' "$sdk_root" > "${CM_BUILD_DIR:-$PWD}/local.properties"
echo "Android SDK ready: $sdk_root (API 36, build-tools 35.0.0)"
