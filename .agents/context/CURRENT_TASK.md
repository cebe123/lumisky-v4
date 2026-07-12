# Current task: Lumisky V4 V8 production implementation

## Objective
Implement `C:\Users\adnan\Downloads\lumisky_v4_production_gelistirme_plani_v8.md` incrementally in `D:\Lumisky v4` without rewrites or unrelated changes.

## Decisions and invariants
- Fuji package is the selected content source; do not use `lumisky_samurai_sunset_package`.
- License/source metadata validation is explicitly out of scope for every wallpaper.
- Keep GL/EGL work on the render thread; external callers submit immutable commands.
- User accepts one connected device for device validation.

## Completed changes
- Session-local `SceneScheduler`/`ParallaxController`; Hilt no longer shares their mutable state.
- `RenderThreadGuard`, `RenderCommand`, `RenderCommandMailbox`, event latest-wins coalescing, and `FrameDemandController` added.
- `WallpaperGlThread` consumes visibility/parallax/touch/daylight commands; `LumiskyWallpaperEngine` submits touch/parallax/daylight commands.
- Scene activation commits last-successful state only after a successful EGL swap.
- Sensor delivery deduplicates listeners and applies epsilon/rate limits.
- Preview FPS profiles/governor: fullscreen starts 60/max 120; card starts 30/max 60; frame deadline feedback is wired in `PreviewGlThread`.
- Location resolution records selected mode and resolution source; polar day/night is typed in `DaylightOverride`.
- Video OES layer skips update/draw when playback policy is disabled and clears pending frame state on GL destroy.
- `WallpaperSourceKind`, `CompiledWallpaperScene`, and source-kind semantic validation added.
- Catalog surface controller now denies a second active preview lease.
- Repaired shader smoothstep contract and stale renderer scope test paths.
- First-swap scene commit is transaction-tested; location source selection and polar day/night outcomes are covered.
- Runtime and power policies now reach the GL thread only through latest-wins render commands.
- `CompiledLayerGraph` and `HybridSceneRendererBackend` now compile and apply pass/zIndex/declaration ordering before runtime layer creation.
- Fuji time-slice assets are bound through `TimeSliceTextureLayer` with minute-tick selection and build-time asset validation.
- `ShaderLayer` custom uniform ve texture-slot JSON tanımlarını layer oluşturulurken typed binding array’lerine derliyor.
- `TextureAssetHandle`, static texture, shader slot ve time-slice asset yollarını render dışı bağlayıp GL yaratımında preload istiyor.
- `CompiledLayerGraph` layer fallback kararlarını typed `SceneFallbackPlan` olarak üretiyor.
- `CompiledLayerGraph` supported animation tanımlarını layer-indexed typed `CompiledAnimationPlan` olarak üretiyor.
- `WallpaperGlThread` command drain buffer’ı thread-owned ve yeniden kullanılabilir; her drain’de liste tahsisi yapmıyor.
- `CelestialMotionController` render sonucu için kalıcı scratch/orbit durumunu kullanıyor; `RenderEngineSession` sonucu preallocated frame state’e doğrudan yazıyor.
- `EglLifecycleState` surface ve context yaşam döngüsünü ayırıyor; surface detach renderer context-loss üretmiyor ve aynı context’te kaynaklar yeniden yaratılmıyor.
- `TexturePool` hazır bitmap upload’larını frame başına iki içerik texture’ı ile sınırlıyor; bütçe aşımı fallback ile sonraki kareye erteleniyor.
- `ShaderProgramPool` geçersiz `programId=0` cache’lemiyor; safe fallback de başarısızsa `ShaderLayer` yalnız o layer’ı devre dışı bırakıyor.
- Context loss sırasında program/texture/FBO/mesh cache’leri `glDelete*` çağrısı olmadan invalidate ediliyor; video OES eski texture ID’sini sıfırlıyor.
- Live/preview share an explicit visibility+surface render gate; render-thread shutdown handoff is bounded.
- Session-local scheduler/parallax isolation and 10,000-input mailbox/event backlog behavior are unit-tested.
- Fuji assets copied to `app/src/main/assets/wallpapers/samurai_fuji_twilight/`; added `samurai_fuji_twilight.json` and catalog item.

## Validation performed
- Repeated `:app:compileDebugKotlin` success after changes.
- `:app:testDebugUnitTest` passed after shader/scope fixes.
- Targeted tests passed: command mailbox, event queue, thread guard, frame demand, governor, source-kind validator.
- `:app:validateWallpaperDefinitions :app:compileDebugKotlin` passed after Fuji import.
- `CompiledLayerGraphTest`, `TimeSliceTexturePlanTest` and time-slice validator coverage passed.
- Lifecycle gate, bounded handoff, runtime-session isolation and high-frequency backlog tests passed.
- `CompiledShaderBindingsTest` passed after typed shader binding compilation.
- `CompiledShaderBindingsTest` and `TimeSliceTexturePlanTest` passed after typed asset handle coverage.
- `CompiledLayerGraphTest` passed with typed fallback plan coverage.
- `CompiledLayerGraphTest` passed with typed animation plan coverage.
- `RenderCommandMailboxTest` passed after reusable command-drain buffer change.
- `CelestialMotionControllerTest` passed after preallocated celestial frame-state output change.
- `RenderLifecycleInstrumentationTest` and its Android test APK compile successfully; device execution is currently blocked by `INSTALL_FAILED_USER_RESTRICTED` while installing the test APK.
- `RenderLifecycleInstrumentationTest` M2101K9AG (`6563c026`) üzerinde geçti; debug APK kuruldu ve uygulama açıldı.
- `EglLifecycleStateTest` passed after surface/context lifecycle separation.
- `TextureUploadBudgetTest` passed after per-frame texture upload budget.
- `ShaderProgramFallbackPolicyTest` passed after invalid-program/fallback handling.
- `RuntimeSceneContextLossTest` passed after context-loss callback/invalidation path.
- `EglLifecycleStateTest` now covers forced context loss followed by fresh context/surface restore contract.
- `EglSwapErrorPolicyTest` covers `EGL_CONTEXT_LOST` classification separately from ordinary surface failure.
- Debug APK installed/launched on ADB device `6563c026`; no crash observed.
- Lifecycle gate/handoff patch installed on `6563c026`; cold launch completed in 2887 ms and the app process remained running.
- Perfetto baseline: `artifacts/device-smoke/lumisky-v8-baseline.perfetto-trace`.

## Important files
- Runtime: `app/src/main/java/com/example/lumisky/core/WallpaperGlThread.kt`, `LumiskyWallpaperEngine.kt`, `RenderCommand*.kt`, `RenderThreadGuard.kt`.
- Engine: `RenderEngineSession.kt`, `FrameDemandController.kt`, `AdaptiveFrameRateGovernor.kt`, `SceneCompiler.kt`, `CompiledWallpaperScene.kt`.
- Hybrid/content runtime: `CompiledLayerGraph.kt`, `HybridSceneRendererBackend.kt`, `TimeSliceTextureLayer.kt`, `TimeSliceTexturePlan.kt`.
- Preview: `preview/PreviewGlThread.kt`, `engine/RuntimeProfile.kt`, `ui/catalog/PreviewSurfaceController.kt`.
- Location: `data/SettingsLocationPlanner.kt`, `engine/LocationDaylightController.kt`, `engine/SceneInputSnapshot.kt`.
- Content: `definition/WallpaperDefinition.kt`, `DefinitionValidator.kt`, `assets/wallpapers/samurai_fuji_twilight.json`, `assets/wallpapers/index.json`.

## Current gaps / exact next steps
1. Finish Phase 3 compiled runtime: device allocation profile evidence (celestial output, orbit ve renk ara nesneleri statik incelemede hot path’ten çıkarıldı).
2. Finish Phase 4 EGL/resource lifecycle: gerçek forced context-loss cihaz testi.
4. Close Phase 5–6 device/parity gates: fullscreen/catalog FPS behavior, thermal/power and GPS-off/timezone scenarios.
5. Finish Phase 7 production layers and Fuji visual/parallax parity evidence.
6. Implement Phase 8 Media3 controller, poster/variant fallback, context restore and long-run video tests.
7. Complete Phase 9 catalog scroll/lease lifecycle, thumbnail LRU and Macrobenchmark coverage.
8. Complete Phase 10 deterministic `WallpaperPackCompiler`, generated index/content hash and CI/release gates.

## Commands
```powershell
$env:ANDROID_HOME='C:\Users\adnan\AppData\Local\Android\Sdk'
.\gradlew.bat :app:compileDebugKotlin --console=plain
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat :app:validateWallpaperDefinitions --console=plain
& "$env:ANDROID_HOME\platform-tools\adb.exe" devices -l
```
