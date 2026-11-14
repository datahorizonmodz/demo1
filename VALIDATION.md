# Verification — DATZON Glass Lab 0.2.0

Verified 2026-11-14 with Temurin JDK 17.0.21, Android SDK 36 / build tools 35.0.0,
Gradle 8.13 and the dependency pins documented in README.

Command (local proxy/trust configuration was supplied outside the repository):

```sh
./gradlew :app:assembleDebug :app:lintDebug :app:testDebugUnitTest
```

**Result: BUILD SUCCESSFUL.** A fresh debug APK was produced after a clean build.
No release keystore is required. Codemagic itself was not invoked.

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
