# DATZON Glass Lab

Native Android material and motion laboratory for DATZON Music. Kotlin + Compose,
Android 13+, AGSL RuntimeShader. Offline demo; no streaming, WebView or backend.

## Explore

- **Preview:** scroll six local music carousels, select a track, play/pause/skip the
  simulation. Scroll down 64 dp to morph to Home orb + player capsule + Plus orb;
  scroll up to restore the floating player and full tab bar.
- Tap or pull the mini player upward to expand the shared cover along DATZON's
  curved path. Pull down to close. Pull a floating mini player down to compact it.
- Tap tabs; hold or drag the active lens to stretch/move it without hiding the nav.
  Orbs rubber-band while dragging and spring back on release/cancellation.
- **Components:** 15 independent targets. Each visible control is connected to
  optics, geometry, a gesture, or a shared animation. Optional linking copies only
  changed optical/appearance values to other glass materials.
- **Animation:** test/replay/loop dock, full player, capsule and Plus actions. Scrub
  the same animation channels at 0%, 30%, 60%, 85%, or any intermediate value.
  Compare easing with spring. Reset cancels every active channel.
- **Presets:** seven factory presets plus Custom. Save/name/delete complete local
  presets. Capture Before, choose another preset, and hold Before to compare.
- **Export:** copy target JSON, Kotlin configuration, complete standalone Compose
  material component, or exact AGSL. Save complete JSON / source ZIP with Android's
  file picker. Import JSON from a file or clipboard with schema/finite/range checks.
- Open the top-right tune control for real-app/isolated mode, artwork/gradient/
  dark/light/lines/text/contrast/photo backdrops, glass toggle, bounds, touch points,
  progress, frame cadence and balanced/high quality optics. Isolated glass drags
  within the visible preview. The editor lifts the preview instead of covering it;
  drag/tap its handle to expand/collapse.

Current component settings and named presets persist locally. No network permission
is requested. Demo album covers are procedural Compose artwork; a selected photo is
decoded once at bounded resolution when changed.

## Build manually with Codemagic

Choose **DATZON Glass Lab - Manual Debug APK** on `main`, then Start new build.
`codemagic.yaml` intentionally has no automatic GitHub trigger. No release signing
key is required. The artifact is `app/build/outputs/apk/debug/app-debug.apk`.

| Tool | Pin |
| --- | --- |
| Android Gradle Plugin | 8.13.2 |
| Gradle wrapper | 8.13 |
| Kotlin and Compose compiler plugin | 2.2.21 |
| Compose BOM | 2025.10.01 (UI/Foundation 1.9.4) |
| Activity Compose | 1.11.0 |
| Compile SDK / Build tools | 36 / 35.0.0 |
| Minimum / Target SDK | 33 / 35 |
| Java bytecode / Codemagic JDK | 17 / 17 |

Local verification with a full JDK 17+ and Android SDK:

```sh
./gradlew :app:assembleDebug :app:lintDebug :app:testDebugUnitTest
```

Unit tests cover preset integrity/invalid import, target independence, artwork-path
continuity, scroll hysteresis, interruption, finger anchor math, and native touch
sequences (tab hold/cancel, player swipe, orb cancellation). Native Skia
(Robolectric API 35) compiles the actual AGSL and checks that enabling refraction
changes sampled pattern pixels. Compose UI tests exercise editor/playground/export
and create review screenshots under `app/build/reports/preview`. A generated test
source compiles the standalone export and its complete supporting engine/types.

## Rendering and integration

`GlassEngine.kt` records a live Home layer into GPU drawing commands and replays
it as shader input. SDF shapes share a smooth optical union near contact. The tab
capsule reads a separate source that includes the dock and its labels; neither
reader captures itself. Pixel distances and moving geometry use one local preview
coordinate space. No per-frame Bitmap / PixelCopy / CPU pixel transfer is used.

The profile's normal and IOR/thickness determine backdrop displacement. RGB samples
separate for dispersion. Blur is a bounded spatial 5/9-tap filter. This is an
experimental screen-space refraction model, not a ray-traced optical simulation.
Balanced mode caps blur at 8 dp; shader creation/draw failures show an explicit
solid-material fallback. RuntimeShader instances are remembered per field.

Exports include the actual material engine and an independently compilable Compose
host, not a placeholder snippet. Full lab gesture/preview reference sources and
integration notes accompany the ZIP. Integrating the full dock into production
still requires wiring its host state to DATZON Music; this lab never modifies that
production repository.

See [SOURCE_PROVENANCE.md](SOURCE_PROVENANCE.md) for exact source version,
transferred parameters, and the limits of the DATZON Original translation.
See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for MIT attribution.

Physical-device touch feel, GPU driver behavior, and sustained 60 FPS require APK
checks on the actual device. The FPS overlay measures UI callback cadence, not
GPU render duration. No Codemagic job is started by this repository update.

The Android 13/14 Robolectric native backend still uses the Android 12 shader
parser and rejects valid `child.eval` AGSL (upstream [#9691](https://github.com/robolectric/robolectric/issues/9691)).
Therefore pixel-refraction tests run with API 35's native backend. Android 13 is
the app minimum, but device-specific shader behavior on API 33 remains an APK check.
