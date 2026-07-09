# Lumisky v5 Preview And Celestial Parity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bring catalog preview, set-preview flow, settings, and pixel forest celestial rendering back to v2 behavior on top of the v5 data-driven architecture.

**Architecture:** Keep the single `:app` module. Fix behavior through v5 definitions, shared render-session profiles, repository-backed settings, and validated shader/texture contracts rather than restoring old modules.

**Tech Stack:** Android Kotlin, Compose, Hilt, GLES 3.0, kotlinx.serialization, JUnit4.

---

### Task 1: Shader Contract Parity

**Files:**
- Modify: `app/src/test/java/com/adnan/lumisky/definition/WallpaperAssetCatalogValidationTest.kt`
- Modify: `app/src/main/assets/wallpapers/*.json`
- Modify: `app/src/main/java/com/adnan/lumisky/layers/ShaderLayer.kt`

- [ ] Add a failing test that every `sampler2D` declared by each wallpaper shader is either bound by the layer texture list or explicitly disabled by a safe uniform such as `u_CloudAlpha = 0`.
- [ ] Run `.\gradlew.bat :app:testDebugUnitTest --tests "com.adnan.lumisky.definition.WallpaperAssetCatalogValidationTest" --stacktrace` and confirm it fails on the current texture uniform mismatch.
- [ ] Update generated v5 definitions so `backgroundTexture`, `sunTexture`, `moonTexture`, and secondary texture keys map to actual shader sampler uniforms.
- [ ] Make missing optional cloud samplers safe by setting `u_CloudAlpha = 0` when no `u_CloudTexture` is declared in the texture list.

### Task 2: Celestial Motion Parity

**Files:**
- Create: `app/src/main/java/com/adnan/lumisky/engine/CelestialMotionController.kt`
- Test: `app/src/test/java/com/adnan/lumisky/engine/CelestialMotionControllerTest.kt`
- Modify: `app/src/main/java/com/adnan/lumisky/layers/ShaderLayer.kt`
- Modify: `app/src/main/assets/wallpapers/pixel_forest.json`

- [ ] Add tests for vertical sun/moon paths from the old pixel forest manifest: sun x near `0.48`, moon x near `0.52`, and peak y near `0.9`.
- [ ] Implement `CelestialMotionController` to resolve `u_SunPos`, `u_MoonPos`, `u_DrawSun`, `u_IsNight`, `u_Minute`, `u_Sunrise`, `u_Sunset`, and `u_NightAmount` from v5 metadata and location-aware day phase.
- [ ] Wire `ShaderLayer` to use the controller instead of hard-coded 0.25/0.75 day windows.

### Task 3: Catalog Live Preview And Smoothness

**Files:**
- Modify: `app/src/main/java/com/adnan/lumisky/engine/RuntimeProfile.kt`
- Modify: `app/src/main/java/com/adnan/lumisky/preview/LumiskyPreviewRenderer.kt`
- Modify: `app/src/main/java/com/adnan/lumisky/preview/PreviewGlThread.kt`
- Modify: `app/src/main/java/com/adnan/lumisky/ui/components/LumiskyWallpaperPreviewView.kt`
- Modify: `app/src/main/java/com/adnan/lumisky/ui/catalog/WallpaperCatalogScreen.kt`

- [ ] Reintroduce live preview only for the focused/active catalog card.
- [ ] Use a catalog preview runtime profile with half render quality, capped FPS, video disabled, and immediate stop when not visible.
- [ ] Keep thumbnails for non-focused cards to avoid multiple active GL loops.

### Task 4: Set Screen And Settings Flow

**Files:**
- Modify: `app/src/main/java/com/adnan/lumisky/ui/preview/WallpaperPreviewScreen.kt`
- Modify: `app/src/main/java/com/adnan/lumisky/ui/settings/SettingsScreen.kt`
- Modify: `app/src/main/java/com/adnan/lumisky/data/SettingsRepository.kt`

- [ ] Ensure catalog item click opens the set/fullscreen preview path with one active GL surface.
- [ ] Add settings controls for quality mode, location-aware lighting, and preview playback behavior.
- [ ] Persist settings through repository APIs used by preview and live wallpaper.

### Task 5: Verification

- [ ] Run `.\gradlew.bat :app:validateWallpaperDefinitions :app:testDebugUnitTest :app:lintDebug :app:assembleDebug --stacktrace`.
- [ ] Install the debug APK on the connected device.
- [ ] Verify catalog scroll smoothness, preview open/set path, active live wallpaper service, no black screen, and no shader/logcat fatal errors.
