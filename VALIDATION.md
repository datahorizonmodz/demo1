# Verification — DATZON Glass Lab 0.2.0

Verified 2026-11-14 with Temurin JDK 17.0.21, Android SDK 36 / build tools 35.0.0,
Gradle 8.13 and the dependency pins documented in README.

Command (local proxy/trust configuration was supplied outside the repository):

```sh
./gradlew :app:assembleDebug :app:lintDebug :app:testDebugUnitTest
```

**Result: BUILD SUCCESSFUL.** A fresh debug APK was produced after a clean build.
No release keystore is required. This original local verification did not invoke
Codemagic. The subsequent SDK setup repair is recorded below.

| Check | Result |
| --- | --- |
| Android dependency/AAR metadata and Kotlin/Compose compilation | Passed |
| Debug APK assembly | Passed |
| Android lint | Passed; see generated report for informational warnings |
| Standalone Compose material export + full supporting engine/types | Compiled as generated test source |
| Preset JSON integrity, invalid data, independent target values | Passed |
| Artwork continuity, scroll hysteresis, interruption and finger anchor | Passed |
| Actual AGSL compile + native HWUI refraction pixel comparison | Passed on Robolectric API 35 |
| Editor/playground/full-player/export UI sequence | Passed |
| Native touch tab selection, long-press cancellation, player open/close swipe, orb cancel | Passed |

Nine automated tests completed, zero failures/errors. Native HWUI screenshots of
Home, editor, compact dock and full player were rendered and visually reviewed.
The test-only readback helper does not ship in the APK and is never used by the
live backdrop engine.

## Remaining device verification

No physical Android device or Codemagic run was available in this session. Check
actual touch feel, OEM GPU behavior, sustained frame cadence and Android file-picker
export/import after installing the APK. Real-device 60 FPS is a target, not a
measured claim. The frame overlay reports UI callback cadence, not GPU timing.

Robolectric API 33/34 uses an Android 12 native shader parser which rejects valid
child.eval AGSL, as documented in upstream issue #9691. An attempted API 33 pixel
test hit that framework limitation. Production AGSL was not replaced with an old
parser workaround; pixel tests use API 35's native backend. Android 13 remains the
minimum supported platform and needs a device APK check.

https://github.com/robolectric/robolectric/issues/9691
https://developer.android.com/reference/android/graphics/RuntimeShader

DATZON Original is a documented translation of production geometry/motion and
representative optics. Pixel-identical production rendering and automatic wiring
to the production music model are not claimed. Full export integration notes and
private-source provenance are in SOURCE_PROVENANCE.md.

## Codemagic SDK setup repair — 2026-11-14

The subsequent manual Codemagic attempt stopped before Gradle: the setup step
reported `sdkmanager: command not found` (exit 127). Calling the command by its
bare name incorrectly assumed the Android command-line tools were on PATH.

The workflow now invokes `ci/prepare-android-sdk.sh`. It resolves the SDK from
ANDROID_SDK_ROOT / ANDROID_HOME, skips installation when API 36 and build-tools
35.0.0 are present, and otherwise resolves sdkmanager under cmdline-tools/latest,
a versioned command-line tools directory, the legacy tools directory, or PATH.
It invokes the resolved executable with an explicit --sdk_root, checks the
installed packages and writes local.properties only after successful preparation.

Verification of this repair:

- Bash syntax check passed; script uses macOS Bash 3.2-compatible syntax.
- Eleven isolated setup checks passed, including no SDK tools on PATH, latest /
  versioned / legacy layouts, PATH fallback, ANDROID_HOME fallback, preinstalled
  packages without a manager, paths containing spaces, missing SDK/tools,
  installer failure propagation and incomplete-install rejection.
- The actual preparation script passed against the local SDK with SDK tools
  excluded from PATH.
- `:app:assembleDebug :app:lintDebug` completed with BUILD SUCCESSFUL after setup.
- Codemagic's new remote run has not been started or claimed successful here.

Official environment / tool layout references:
https://docs.codemagic.io/yaml-basic-configuration/environment-variables/
https://docs.codemagic.io/specs-macos/xcode-16-4/
