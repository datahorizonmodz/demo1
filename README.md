# DATZON Glass Lab

Android-native liquid-glass experiment app for **DATZON Music**, using **Kotlin, Jetpack Compose, Android 13+ RuntimeShader (AGSL)**.

> This is a separate experimental app. It does not modify DATZON Music production.

## What is in v0.1.0

- Local mock of DATZON Music's Home screen: Dire Straits recommendations, Top 50 Indonesia, dynamic gradient wallpaper, floating mini player and pill-shaped bottom navbar, adapted from a provided screenshot.
- Two independent live-edit glass objects: **Mini player** and **Navbar**. The standalone plus button inherits the navbar preset.
- A GPU shader that refracts a live replay of the Home composable underneath the glass, with optical offset and RGB dispersion. The backdrop is recorded with Compose `GraphicsLayer`, without per-frame CPU bitmap reads.
- Slider editor for refraction, IOR, thickness, blur, aberration, radius, opacity, edge highlight, shadows, RGB tint, spring stiffness/damping and press scale.
- Presets: Crystal, Frosted, Liquid, Dark, Soft; custom named presets stored locally using SharedPreferences.
- Copy JSON (with target and engine version), exact AGSL source, or a Kotlin initialization/use snippet to the clipboard. Import JSON presets from clipboard.
- Glass on/off, mock playback button, navbar interactions, and approximate UI-frame FPS indicator.
- Does not require internet, Firebase, keystore, or music streaming credentials.

### Important implementation notes

The shader renders against the captured Home composition, not arbitrary system windows or other Android apps. Blur/refraction values are pixel-based in the GPU layer; slider output should be validated on the phone where the production app will run. The spring parameters are used by the glass press animation. This is a **prototype**, not a final performance-tested DATZON Music integration.

To transfer an effect to DATZON Music, copy both `GlassConfig.kt` and `GlassEngine.kt`, set up a `rememberGraphicsLayer` around the real Home composable, then apply the exported config to a `GlassPane`. Copying only the Kotlin snippet is **not** sufficient without these source files.

## Project details

- Package: `id.datzon.glasslab`
- Minimum API: Android 13 (API 33)
- Compile SDK: 36
- Kotlin 2.2.21, Android Gradle Plugin 8.13.2, Gradle 8.13
- Compose BOM 2025.10.01
- Build output: `app/build/outputs/apk/debug/app-debug.apk`

## Build manually with Codemagic (no auto triggers)

1. Sign in at [Codemagic](https://codemagic.io/apps).
2. Select **Add application**. Connect the GitHub repository `datahorizonmodz/demo1`.
3. Choose Android native or Other (depending on your Codemagic UI), then finish adding the application.
4. Open the app in Codemagic, choose `main`, then **Check for configuration file** if necessary.
5. Select workflow **DATZON Glass Lab - Manual Debug APK** (`glass-lab-manual-debug`).
6. Click **Start new build**.
7. When it succeeds, open the build and download `app-debug.apk` from **Artifacts**. Install on Android 13+.

The workflow lives in the root `codemagic.yaml`. It uses **mac_mini_m2** and JDK 17, bootstraps Gradle 8.13, runs `assembleDebug`, and collects the resulting unsigned-for-release but **debug-signed installable** APK. No keystore upload is required. It has **no automatic trigger** so GitHub pushes will not consume build time.

The `gradlew` in this repository is a small shell bootstrap that fetches the official Gradle 8.13 distribution when missing. Use `bash ./gradlew :app:assembleDebug`, not `./gradlew`, unless the file is marked executable after clone.

## Known build troubleshooting

First Codemagic run failed at Gradle dependency metadata: Compose 1.12.1 required compileSdk 37 and AGP 9.1. Updated to **Compose BOM 2025.10.01** (Compose 1.9.4) and **Activity Compose 1.11.0** to stay compatible with compileSdk 36 / AGP 8.13.2. Also corrected GraphicsLayer imports. Re-run the manual workflow against the latest main commit. This fix has not yet been compile-verified.

## Current verification status

Source is committed. A Codemagic compile and a real Android run **have not yet been verified**. Check the first build log before treating this as an installable APK. If it fails, fix the reported Kotlin/Gradle errors and rerun the same manual workflow.

## Acknowledgements and licensing

Archis Vaze and rsu-Suba Liquid Glass browser demos were used as **concept references**, not copied source code. This project uses its own native AGSL/Compose implementation.
