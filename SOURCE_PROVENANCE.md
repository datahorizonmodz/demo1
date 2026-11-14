# DATZON Glass Lab 0.2 — source provenance

Audited 2026-11-14. Production repository was read only. No backend/configuration,
keys, authentication, media provider code, keystores, or private artwork was copied.

## DATZON Music

Source: datahorizonmodz/musik, main commit
`e80c557ef4b9e4913c76bdf9ace1608b6b867404` (v0.6.52, merged PR #41).
The active MainActivity wires MiniPlayerMotion, GlassNavigationState and PlayerExpansion.

| Production source | Lab implementation / source behavior |
| --- | --- |
| SpotizonHome.kt | NativePreview Home: horizontal artwork carousels, section headings, Top 50, dominant-artwork atmosphere; safe local data |
| MiniPlayerMotion.kt | LabMotion.scroll: directional accumulation, reset on reversal, 64 dp threshold; dock morph 420 ms |
| GlassNavigationBar.kt | NativePreview: 67→60 dp player, 12→30 dp corners, 6 dp artwork inset, bar shrinks to 60 dp Home orb; plus remains separate |
| GlassNavigationState.kt, DockGestures.kt | LabMotion / labNavigationGesture: immediate finger ownership, grab offset, long press, horizontal takeover, cancel to committed tab |
| NavigationOrbState.kt | labOrbGesture: bounded exponential rubber band, spring return; parameterized for experiments |
| PlayerExpansion.kt | playerArtworkRect: exact curved-center interpolation; staged backdrop .02–.76, gradient .02–.94; 650 ms cubic easing (.30,0,.22,1) |
| NativeTrackScreen.kt | Full-player closing drag / same shared progress / velocity release |
| NavigationDockGlass.kt | GlassEngine: SDF rounded geometry + smooth union, edge normal refraction, shared optical surface |
| LiveSortGlass.kt, NavigationLabelBackdrop.kt | GPU GraphicsLayer command replay; separate label-containing source for active capsule |
| SpotizonMiniPlayer.kt | Continuous artwork size/corner, title/artist scale, controls occupying interpolated widths |

Production contains several material/rendering paths. DATZON Original translates
source geometry, timings and representative optical settings to the lab's new
surface-profile shader. It is NOT a claim of pixel-identical production rendering.
Specifically the production dock uses edge bend 12 dp / falloff 4.5 dp, 8 dp blur,
15% dark tint, rim width 1.15 dp and highlight .15 + .34×lighting. This lab also
exposes IOR / thickness / chromatic dispersion, which are new experiments.
Production's active tab lens uses zero blur and .08 tint, .07 source refraction
strength in a different shader. That .07 cannot be transferred numerically into
this engine. Production's spring release and dock gestures are parameterized here.

The lab intentionally omits playback/backend functionality. Track names and covers
are local fictional demo data / procedural Compose artwork. Photo picker processes
one selected image when changed, never a bitmap readback per frame.

## External references

- https://github.com/rsu-Suba/Liquid-Glass-React, inspected Glass.tsx,
  GlassProcessor.ts, GlassFC.ts. No license file present in audited checkout:
  no source copied. Studied capsule spine/radial edge displacement, RGB channel
  separation and cached geometry. No React/CPU pixel loop used in Android.
- https://github.com/archisvaze/liquid-glass, inspected index.html (SVG map
  refraction profiles), webgl.html (SDF / superellipse profile / Snell displacement),
  ui.js and bg-picker.js (editor persistence / gestures). MIT attribution retained
  in THIRD_PARTY_NOTICES.md. No bundled reference background photographs.

## Renderer / export contract

Home is recorded into GPU drawing commands, outside all reader nodes. Dock reads
Home, the tab lens reads a second layer containing Home + dock + labels. Neither
reader records itself. Source and effect occupy the same preview-local viewport,
so scrolling and moving geometry never rely on screen-to-window guesses.
A cached RuntimeShader is created once per field; parameters/effect snapshots update
without shader recompilation. Refraction displaces real backdrop samples. Blur is
a bounded 5/9-tap spatial filter, not a claim of exact Gaussian convolution.

ExportedGlass.kt contains exact engine + configuration types + one reusable Compose
host. The ZIP also includes lab motion/preview reference files and integration notes.
The standalone host is a material primitive. It does not promise to replace the
production MusicModel or insert the full dock into that app automatically.
