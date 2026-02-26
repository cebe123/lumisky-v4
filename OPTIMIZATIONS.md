# Lumisky Full Optimization Audit

### 1) Optimization Summary

* Brief summary of current optimization health: Runtime rendering code is relatively efficient on the GL side (renderer logs show low average draw times in previews/wallpaper loops), but app startup and interaction smoothness are heavily affected by non-render work on/around startup (network prefetch, location refresh cascades, UI orchestration), plus a few avoidable asset I/O and per-frame allocation patterns.
* Top 3 highest-impact improvements:
* 1. Defer and throttle startup backup sun-times prefetch (`25` candidates currently starts ~2s after launch and runs during UI warm-up).
* 2. Coalesce `HomeViewModel` location/sun-times refresh flows to avoid duplicate synchronous location reads and repeated refresh chains on main-thread-triggered paths.
* 3. Remove duplicate texture asset reads in wallpaper texture loading path (`PreviewSkyProgram` probe + load, combined with `WallpaperRenderEngine` raw `readBytes()` loader lambdas).
* Biggest risk if no changes are made: As wallpaper count/features grow, startup jank, API/network cost, and maintenance drift will increase faster than rendering cost, causing a poor UX despite a reasonably fast GL pipeline.
* Validation note (GL thread count): Re-tested after user feedback. Cold launch + idle still shows multiple `GLThread` rows in `ps -T`, but unique GL thread names stayed at `2` before and after interaction in the controlled run. This matches expected live+prepared preview behavior and is not treated as a confirmed leak finding.

### 2) Findings (Prioritized)

#### Finding 1

* **Title** Startup backup prefetch performs a large burst of network calls during UI warm-up
* **Category** Network
* **Severity** High
* **Impact** Improves startup latency, jank, network/API cost, battery use, and failure resilience under weak networks
* **Evidence**
* Code schedules startup prefetch very early (`2_000ms`) in `app/src/main/java/com/example/lumisky/viewmodel/HomeViewModel.kt:515` and `app/src/main/java/com/example/lumisky/viewmodel/HomeViewModel.kt:561`
* Startup path triggers it from `init` in `app/src/main/java/com/example/lumisky/viewmodel/HomeViewModel.kt:114`
* Prefetch builds a wide candidate set and calls `prefetchBackupAsync` in `app/src/main/java/com/example/lumisky/viewmodel/HomeViewModel.kt:520` and `app/src/main/java/com/example/lumisky/viewmodel/HomeViewModel.kt:547`
* Repository iterates due candidates sequentially and fetches each via API in `core/src/main/java/com/example/core/api/SunTimesRepository.kt:199`, `core/src/main/java/com/example/core/api/SunTimesRepository.kt:225`, `core/src/main/java/com/example/core/api/SunTimesRepository.kt:226`, `core/src/main/java/com/example/core/api/SunTimesRepository.kt:236`
* Runtime logs (2026-02-26 cold launch) show `BACKUP_PREFETCH_START ... candidates=25 due=25` around app startup, followed by many `BACKUP_PREFETCH_SUCCESS` entries over several seconds while UI is active
* `gfxinfo` during cold launch showed high jank (example run: `70` frames, `64.29%` janky, 50p `34ms`, 90p `101ms`)
* **Why it’s inefficient**
* Backup prefetch is background work, but it starts during the app’s most latency-sensitive phase and competes for CPU/network resources while previews and UI composition are initializing.
* The candidate count scales with supported cities and can grow significantly as the catalog/localization list expands.
* **Recommended fix**
* Move backup prefetch off startup-critical path (defer until app idle, post-first-interaction, or background `WorkManager` with constraints).
* Limit initial session prefetch batch size (for example top N cities or staggered batches).
* Add backoff/jitter and budget caps per session.
* **Tradeoffs / Risks**
* Backup cache freshness may be delayed after first install or language/city changes.
* Requires explicit freshness policy and user-visible fallback expectations.
* **Expected impact estimate** High for startup smoothness and API load (qualitative: noticeable jank reduction on cold launch; substantial reduction in startup network burst)
* **Removal Safety** Needs Verification
* **Reuse Scope** service-wide
* **Optimization Hygiene Classification** Reuse Opportunity (background scheduling policy can be shared across prefetch jobs)

#### Finding 2

* **Title** `HomeViewModel` triggers duplicate location reads and cascaded refreshes across UI events
* **Category** I/O
* **Severity** High
* **Impact** Improves UI responsiveness, reduces redundant location manager calls and downstream sun-times refreshes
* **Evidence**
* Repeated refresh chains in `HomeViewModel` (`refreshLocationState()`, `requestImmediateGpsLocation()`, `refreshSunTimes()`) are triggered in multiple user paths: `app/src/main/java/com/example/lumisky/viewmodel/HomeViewModel.kt:186`, `app/src/main/java/com/example/lumisky/viewmodel/HomeViewModel.kt:209`, `app/src/main/java/com/example/lumisky/viewmodel/HomeViewModel.kt:222`, `app/src/main/java/com/example/lumisky/viewmodel/HomeViewModel.kt:327`
* `refreshLocationState()` performs sync location checks and last-known reads via `LastKnownLocationProvider` in `app/src/main/java/com/example/lumisky/viewmodel/HomeViewModel.kt:227`, `app/src/main/java/com/example/lumisky/viewmodel/HomeViewModel.kt:257`, `app/src/main/java/com/example/lumisky/viewmodel/HomeViewModel.kt:263`
* `resolveLocationCandidates()` can call `getLastKnownLocation()` again during `refreshSunTimes()` in `app/src/main/java/com/example/lumisky/viewmodel/HomeViewModel.kt:354`, `app/src/main/java/com/example/lumisky/viewmodel/HomeViewModel.kt:413`, `app/src/main/java/com/example/lumisky/viewmodel/HomeViewModel.kt:431`, `app/src/main/java/com/example/lumisky/viewmodel/HomeViewModel.kt:434`
* `LastKnownLocationProvider.getLastKnownLocation()` scans providers each call in `core/src/main/java/com/example/core/location/LastKnownLocationProvider.kt:43`, `core/src/main/java/com/example/core/location/LastKnownLocationProvider.kt:51`, `core/src/main/java/com/example/core/location/LastKnownLocationProvider.kt:58`
* **Why it’s inefficient**
* The same UI event often causes multiple location-state and sun-times refreshes in short succession.
* Duplicate provider scans and system service calls increase main-thread-adjacent work and can amplify jank when combined with startup prefetch and preview initialization.
* **Recommended fix**
* Introduce a coalesced refresh pipeline (single `refreshLocationAndSunTimes(reason)` entry point with debounce/coalesce window).
* Cache location candidate resolution for a short TTL or until GPS callback/manual city changes.
* Avoid calling `getLastKnownLocation()` again inside `resolveLocationCandidates()` if `refreshLocationState()` already resolved values in the same cycle.
* **Tradeoffs / Risks**
* Slightly more complex state machine; stale data bugs are possible if invalidation is incomplete.
* Requires careful handling of permission/system-location state changes.
* **Expected impact estimate** Medium-High (fewer redundant system calls and refreshes; smoother mode/city changes)
* **Removal Safety** Needs Verification
* **Reuse Scope** local file / module

#### Finding 3

* **Title** Wallpaper texture loading path can read the same asset twice per texture because WebP probing consumes loader bytes
* **Category** I/O
* **Severity** High
* **Impact** Reduces config-change latency, wallpaper attach/reconfigure time, memory churn, and I/O spikes
* **Evidence**
* `PreviewSkyProgram.loadTexture()` loads bytes from loader in `engine/src/main/java/com/example/engine/shader/PreviewSkyProgram.kt:371`
* `PreviewSkyProgram.resolvePreferredTexturePath()` probes WebP candidate by calling the same loader in `engine/src/main/java/com/example/engine/shader/PreviewSkyProgram.kt:468` and `engine/src/main/java/com/example/engine/shader/PreviewSkyProgram.kt:477`
* In wallpaper module, loader lambdas use raw `assets.open(...).readBytes()` (no cache) at `wallpaper/src/main/java/com/example/wallpaper/engine/WallpaperRenderEngine.kt:72`, `wallpaper/src/main/java/com/example/wallpaper/engine/WallpaperRenderEngine.kt:84`, `wallpaper/src/main/java/com/example/wallpaper/engine/WallpaperRenderEngine.kt:103`
* Texture decode/upload then occurs immediately after bytes load in `engine/src/main/java/com/example/engine/shader/PreviewSkyProgram.kt:374`, `engine/src/main/java/com/example/engine/shader/PreviewSkyProgram.kt:400`, `engine/src/main/java/com/example/engine/shader/PreviewSkyProgram.kt:402`
* **Why it’s inefficient**
* When a `.webp` candidate exists, the probe path can fully load bytes once just to detect existence, then load again for the actual decode.
* This is especially expensive during `attach`/`reconfigure` when several textures may be reloaded together.
* **Recommended fix**
* Split loader contract into `exists(path)` + `read(path)` or return `(resolvedPath, bytes)` from the probe stage to reuse bytes.
* Add a small in-session wallpaper asset byte cache (or reuse `RenderAssetCache`-like cache in wallpaper module).
* **Tradeoffs / Risks**
* Memory usage increases if byte caching is unbounded.
* Must ensure cache invalidation on config changes and avoid leaking large arrays.
* **Expected impact estimate** Medium-High on wallpaper config switch/attach latency; moderate memory churn reduction
* **Removal Safety** Safe
* **Reuse Scope** module / service-wide
* **Optimization Hygiene Classification** Reuse Opportunity

#### Finding 4

* **Title** Per-frame state hashing allocates temporary collections/strings in render hot paths
* **Category** Memory
* **Severity** High
* **Impact** Reduces GC pressure and CPU overhead in preview and wallpaper render loops
* **Evidence**
* `SkyRenderer` computes `stateHash` using `listOf(...).hashCode()` each frame in `engine/src/main/java/com/example/engine/renderer/SkyRenderer.kt:97` and `engine/src/main/java/com/example/engine/renderer/SkyRenderer.kt:109`
* `SceneStateHasher.compute()` also uses `listOf(...).hashCode()` in `wallpaper/src/main/java/com/example/wallpaper/render/SceneStateHasher.kt:19`
* Wallpaper controller computes scene hash repeatedly, including continuous path in `wallpaper/src/main/java/com/example/wallpaper/service/WallpaperRenderController.kt:213`, `wallpaper/src/main/java/com/example/wallpaper/service/WallpaperRenderController.kt:217`, `wallpaper/src/main/java/com/example/wallpaper/service/WallpaperRenderController.kt:235`
* `WallpaperRenderEngine.snapshotSceneStateInput()` builds a fingerprint string every hash request via `sceneFingerprint()` and `buildString` in `wallpaper/src/main/java/com/example/wallpaper/engine/WallpaperRenderEngine.kt:162`, `wallpaper/src/main/java/com/example/wallpaper/engine/WallpaperRenderEngine.kt:182`, `wallpaper/src/main/java/com/example/wallpaper/engine/WallpaperRenderEngine.kt:183`
* **Why it’s inefficient**
* `listOf()` and `buildString()` allocate objects on every frame or hash computation.
* These allocations are small individually but occur in high-frequency paths, creating avoidable GC churn.
* **Recommended fix**
* Replace `listOf(...).hashCode()` with manual rolling hash accumulation on primitives/booleans/strings.
* Cache config fingerprint and recompute only on `setConfig`.
* Reuse a lightweight mutable hasher or inline hash function for `SceneStateInput`.
* **Tradeoffs / Risks**
* Manual hash code logic is easier to get wrong than standard collection hash.
* Must preserve collision characteristics sufficiently for redraw suppression logic.
* **Expected impact estimate** Medium (steady CPU/GC improvement under continuous render and preview loops)
* **Removal Safety** Safe
* **Reuse Scope** module / service-wide

#### Finding 5

* **Title** `HomeScreen` performs repeated collection scans and transformations that scale poorly with wallpaper count
* **Category** Algorithm
* **Severity** Medium
* **Impact** Improves list recomposition cost, focus calculation overhead, and scalability as wallpaper count grows
* **Evidence**
* Grouping scans `wallpapers` per category using `orderedCategories.mapNotNull` + `filter` in `app/src/main/java/com/example/lumisky/ui/home/HomeScreen.kt:143` and `app/src/main/java/com/example/lumisky/ui/home/HomeScreen.kt:144`
* Focus bootstrap flattens IDs via `flatMap` in `app/src/main/java/com/example/lumisky/ui/home/HomeScreen.kt:177`
* Category prewarm flow runs additional candidate computation and background prewarm dispatch in `app/src/main/java/com/example/lumisky/ui/home/HomeScreen.kt:377` and `app/src/main/java/com/example/lumisky/ui/home/HomeScreen.kt:379`
* `WallpaperCatalog.configById()` rebuilds configs list for lookup in `app/src/main/java/com/example/lumisky/data/WallpaperCatalog.kt:32`, `app/src/main/java/com/example/lumisky/data/WallpaperCatalog.kt:37`, `app/src/main/java/com/example/lumisky/data/WallpaperCatalog.kt:38`
* **Why it’s inefficient**
* Current complexity is acceptable for small lists but grows unnecessarily with more wallpapers/categories.
* Full-list transformations occur in UI-centric paths where responsiveness matters.
* **Recommended fix**
* Pre-group wallpapers once (`groupBy`) and derive ordered categories from map lookups instead of repeated `filter`.
* Maintain a `Set<String>`/index map for ID membership checks rather than `flatMap + contains`.
* Cache `configById` results or build an ID map alongside `buildConfigs`.
* **Tradeoffs / Risks**
* Slightly more memory for maps/sets.
* Additional state synchronization if caches are kept mutable.
* **Expected impact estimate** Medium for future scaling (120+ wallpapers), Low-Medium at current catalog size
* **Removal Safety** Safe
* **Reuse Scope** module

#### Finding 6

* **Title** `RenderAssetCache` WebP path resolution performs extra asset opens and lacks path-resolution memoization
* **Category** Caching
* **Severity** Medium
* **Impact** Reduces asset I/O during preview prewarm and repeated texture loads; lowers startup/scroll churn
* **Evidence**
* Texture byte cache exists (`LruCache<String, ByteArray>`) in `app/src/main/java/com/example/lumisky/shader/RenderAssetCache.kt:9`, `app/src/main/java/com/example/lumisky/shader/RenderAssetCache.kt:10`
* Before loading bytes, code resolves preferred path and may probe asset existence by opening the asset in `app/src/main/java/com/example/lumisky/shader/RenderAssetCache.kt:35`, `app/src/main/java/com/example/lumisky/shader/RenderAssetCache.kt:59`, `app/src/main/java/com/example/lumisky/shader/RenderAssetCache.kt:68`, `app/src/main/java/com/example/lumisky/shader/RenderAssetCache.kt:71`, `app/src/main/java/com/example/lumisky/shader/RenderAssetCache.kt:77`
* `HomeScreen` triggers prewarm for focused and neighbor cards in `app/src/main/java/com/example/lumisky/ui/home/HomeScreen.kt:377`, `app/src/main/java/com/example/lumisky/ui/home/HomeScreen.kt:379`
* **Why it’s inefficient**
* Existence checks add extra asset-open calls before actual byte reads.
* Preferred-path resolution is recalculated repeatedly per request instead of cached by original path.
* **Recommended fix**
* Add a small `LruCache<String, String>` for resolved texture path (`png -> webp` mapping).
* Replace `assetExists(open+read)` with `AssetManager.list`-based directory cache or resolver cache.
* Batch prewarm requests by category and skip duplicate paths across neighboring cards.
* **Tradeoffs / Risks**
* Slightly more cache memory and invalidation complexity if assets change during debug sessions.
* `AssetManager.list` behavior differs by path structure; must handle nested directories correctly.
* **Expected impact estimate** Medium on first-scroll/prewarm responsiveness, Low-Medium on steady state
* **Removal Safety** Safe
* **Reuse Scope** module

#### Finding 7

* **Title** Continuous wallpaper rendering policy is string-hint based (`config.id`) and will not scale safely with new dynamic themes
* **Category** Cost
* **Severity** Medium
* **Impact** Prevents accidental over-rendering (battery/CPU cost) or under-rendering (visual bugs) as catalog grows
* **Evidence**
* Continuous rendering decision uses `config.id` substring hints in `wallpaper/src/main/java/com/example/wallpaper/engine/WallpaperRenderEngine.kt:234`, `wallpaper/src/main/java/com/example/wallpaper/engine/WallpaperRenderEngine.kt:236`
* Frame interval policy is also ID-specific (`warrior`) in `wallpaper/src/main/java/com/example/wallpaper/engine/WallpaperRenderEngine.kt:239` and `wallpaper/src/main/java/com/example/wallpaper/engine/WallpaperRenderEngine.kt:258`
* Controller render loops depend on this policy in `wallpaper/src/main/java/com/example/wallpaper/service/WallpaperRenderController.kt:42`, `wallpaper/src/main/java/com/example/wallpaper/service/WallpaperRenderController.kt:46`, `wallpaper/src/main/java/com/example/wallpaper/service/WallpaperRenderController.kt:275`
* **Why it’s inefficient**
* String matching makes performance behavior data-blind and fragile.
* New dynamic wallpapers may accidentally run too fast/too slow unless code is manually updated.
* **Recommended fix**
* Add explicit runtime policy fields to `WallpaperConfig` (for example `renderPolicy = STATIC | MINUTE_TICK | CONTINUOUS` + `targetFrameIntervalMs`).
* Keep a compatibility fallback for legacy configs, then migrate presets.
* **Tradeoffs / Risks**
* Requires config schema changes and preset migration.
* Must preserve behavior for current themes during transition.
* **Expected impact estimate** Medium (battery/cpu predictability and lower maintenance cost)
* **Removal Safety** Needs Verification
* **Reuse Scope** service-wide
* **Optimization Hygiene Classification** Over-Abstracted Code (string hints create hidden policy coupling)

#### Finding 8

* **Title** Asset conversion pipeline is tied to `preBuild` and writes into source assets, reducing build reproducibility and optimization headroom
* **Category** Build
* **Severity** Medium
* **Impact** Improves local build consistency, CI reproducibility, and future build-cache/config-cache adoption
* **Evidence**
* Conversion task declares source assets directory as output in `app/build.gradle.kts:92`
* Packaging sync depends on conversion in `app/build.gradle.kts:172`
* Main assets source is redirected to generated filtered dir in `app/build.gradle.kts:179`
* `preBuild` always depends on filtered asset prep in `app/build.gradle.kts:181`
* Build profile (`:app:assembleDebug`) shows asset tasks are currently `UP-TO-DATE`, but the design still mutates `src/main/assets` on build path
* **Why it’s inefficient**
* Build tasks mutating source directories reduce determinism and complicate cacheability.
* It increases the chance of unnecessary work, VCS noise, and harder CI optimization.
* **Recommended fix**
* Move conversion outputs entirely under `build/` and treat source raster files as inputs only.
* Keep a separate explicit maintenance task for generating checked-in `.webp` files when desired.
* If source mutation remains, isolate it from `preBuild` and make it opt-in locally.
* **Tradeoffs / Risks**
* Changes current developer workflow and may require docs updates.
* Initial migration may expose missing `.webp` assets or path assumptions.
* **Expected impact estimate** Medium for build hygiene and future build optimization; low runtime impact
* **Removal Safety** Needs Verification
* **Reuse Scope** module

#### Finding 9

* **Title** Build feedback loop optimization headroom remains unused (configuration cache / parallel builds / lint scope)
* **Category** Build
* **Severity** Medium
* **Impact** Shorter local iteration time and CI duration, especially for repeated lint/test commands
* **Evidence**
* `gradle.properties` leaves `org.gradle.parallel` disabled/commented in `gradle.properties:13`
* Gradle repeatedly suggests configuration cache in successful runs
* Profile summary examples:
* `:app:assembleDebug` total `22.117s` with startup+config time > `5s` (`build/reports/profile/profile-2026-02-26-22-25-02.html`)
* `:app:lintDebug` total `1m2.94s`, task execution dominated by lint analysis (`build/reports/profile/profile-2026-02-26-22-25-33.html:46`, `build/reports/profile/profile-2026-02-26-22-25-33.html:70`, `build/reports/profile/profile-2026-02-26-22-25-33.html:852`, `build/reports/profile/profile-2026-02-26-22-25-33.html:857`, `build/reports/profile/profile-2026-02-26-22-25-33.html:862`)
* **Why it’s inefficient**
* Repeated local builds/tests re-pay startup/configuration costs.
* `lintDebug` also analyzes debug AndroidTest and UnitTest variants, which may be excessive for every developer loop.
* **Recommended fix**
* Trial-enable configuration cache and validate AGP/Compose compatibility.
* Evaluate `org.gradle.parallel=true` after checking project decoupling and memory pressure.
* Split lint commands by use case (fast local lint vs full CI lint).
* **Tradeoffs / Risks**
* Some plugins/tasks may not be configuration-cache compatible.
* Parallel builds can increase memory usage and occasionally expose task dependency bugs.
* **Expected impact estimate** Medium (developer/CI throughput), low runtime impact
* **Removal Safety** Likely Safe
* **Reuse Scope** service-wide

#### Finding 10

* **Title** Duplicate preview GL scheduling logic and duplicate shader-asset loaders increase optimization drift risk
* **Category** Cost
* **Severity** Medium
* **Impact** Improves maintainability and makes future performance fixes cheaper and more consistent
* **Evidence**
* Duplicate shader fragment asset loaders:
* `app/src/main/java/com/example/lumisky/shader/ShaderAssetLoader.kt:5`
* `wallpaper/src/main/java/com/example/wallpaper/render/WallpaperShaderAssetLoader.kt:5`
* Duplicate GLSurfaceView/Choreographer scheduling patterns:
* Home preview view in `app/src/main/java/com/example/lumisky/ui/home/HomeScreen.kt:537`, `app/src/main/java/com/example/lumisky/ui/home/HomeScreen.kt:542`, `app/src/main/java/com/example/lumisky/ui/home/HomeScreen.kt:616`
* Preview screen anonymous GLSurfaceView in `app/src/main/java/com/example/lumisky/ui/preview/PreviewScreen.kt:75`, `app/src/main/java/com/example/lumisky/ui/preview/PreviewScreen.kt:78`, `app/src/main/java/com/example/lumisky/ui/preview/PreviewScreen.kt:110`
* Duplicate refresh-rate helpers:
* `app/src/main/java/com/example/lumisky/ui/home/HomeScreen.kt:753`
* `app/src/main/java/com/example/lumisky/ui/preview/PreviewScreen.kt:156`
* **Why it’s inefficient**
* Performance fixes (frame pacing, lifecycle release, power-save behavior) must be duplicated and can drift.
* Duplicate loaders/caches increase the chance that one path gets optimized while another remains slow.
* **Recommended fix**
* Extract a shared preview view component or scheduler helper (parameterized for focus vs full-preview behavior).
* Consolidate shader asset loading into a shared loader/cache utility in `:core` or a dedicated shared render asset module.
* **Tradeoffs / Risks**
* Refactor may touch multiple UI paths and lifecycle behavior.
* Over-generalization is a risk; keep shared API small.
* **Expected impact estimate** Medium maintenance ROI, indirect runtime gains
* **Removal Safety** Likely Safe
* **Reuse Scope** module / service-wide
* **Optimization Hygiene Classification** Reuse Opportunity

#### Finding 11

* **Title** Stale/unused state and helper classes add maintenance cost and distract optimization efforts
* **Category** Cost
* **Severity** Low
* **Impact** Reduces code surface, review noise, and future optimization confusion
* **Evidence**
* `startupLoading` and `startupProgress` are defined in `HomeViewModel` (`app/src/main/java/com/example/lumisky/viewmodel/HomeViewModel.kt:88`, `app/src/main/java/com/example/lumisky/viewmodel/HomeViewModel.kt:91`) and read by `HomeScreen`, but no assignments were found beyond initial values (search-based verification)
* `TextureLoader`, `DeviceTier`, and `DpiTextureSelector` exist in `engine/src/main/java/com/example/engine/texture/TexturePool.kt:40`, `engine/src/main/java/com/example/engine/texture/TexturePool.kt:46`, `engine/src/main/java/com/example/engine/texture/TexturePool.kt:52` with no references found outside that file
* `TexturePool` is currently only “touched” from `SkyRenderer` (`engine/src/main/java/com/example/engine/renderer/SkyRenderer.kt:48`) and does not appear to participate in actual GL texture handle reuse
* **Why it’s inefficient**
* Dead/stale code increases cognitive load and can mislead future optimization work.
* Unused abstractions encourage false assumptions about existing caching or loading behavior.
* **Recommended fix**
* Remove or clearly annotate unused/stub classes and stale UI loading states.
* If planned for future use, move to a draft/experimental package or add TODOs with intended ownership.
* **Tradeoffs / Risks**
* Some code may be intentionally reserved for upcoming work; confirm before deletion.
* **Expected impact estimate** Low runtime impact, Medium maintenance impact
* **Removal Safety** Needs Verification
* **Reuse Scope** local file / module
* **Optimization Hygiene Classification** Dead Code / Over-Abstracted Code

### 3) Quick Wins (Do First)

* Defer `HomeViewModel` startup backup prefetch (`prefetchBackupCityCache`) until after first meaningful interaction or schedule it via background work instead of `2s` post-launch.
* Coalesce `refreshLocationState()` + `refreshSunTimes()` into one debounced pipeline and reuse already-resolved GPS values within a single refresh cycle.
* Fix duplicate asset reads in `PreviewSkyProgram` by returning probed bytes/path together or splitting `exists` vs `load`.
* Replace `listOf(...).hashCode()` in `SkyRenderer` and `SceneStateHasher` with manual hash accumulation and cache `sceneFingerprint`.
* Add resolved texture path cache in `RenderAssetCache` to avoid repeated `.webp` probe opens.
* Split local lint commands (fast local lint vs full lint) to avoid paying `lintAnalyzeDebugAndroidTest`/`lintAnalyzeDebugUnitTest` on every run.

### 4) Deeper Optimizations (Do Next)

* Introduce a data-driven wallpaper render policy in `WallpaperConfig` (`STATIC`, `MINUTE_TICK`, `CONTINUOUS`, target frame interval) and remove string-hint policy coupling.
* Consolidate preview rendering infrastructure (`GLSurfaceView` + Choreographer loop + refresh-rate resolver + release lifecycle) into a shared component used by Home and Preview screens.
* Build a shared render-asset service (fragment + texture bytes + preferred-path resolution) usable by both app previews and wallpaper runtime.
* Rework asset conversion pipeline so build tasks never mutate source assets and can participate in stronger incremental/caching behavior.
* Add explicit performance telemetry (unique active preview views, preview GL thread count, prewarm queue depth, backup prefetch duration/count) to detect regressions as catalog size grows.

### 5) Validation Plan

* Benchmarks
* Build:
* Re-run profiled commands before/after changes and compare `Total Build Time`, `Configuring Projects`, and top task durations:
* `:app:assembleDebug --profile`
* `:app:lintDebug --profile`
* `:engine:testDebugUnitTest --profile`, `:core:testDebugUnitTest --profile`, `:wallpaper:testDebugUnitTest --profile`, `:app:testDebugUnitTest --profile`
* Runtime:
* Cold launch (`force-stop` -> launch -> 10-15s idle)
* Home scroll stress (vertical + horizontal)
* Preview open/close loop (5x)
* Wallpaper service preview/apply/visibility changes (if live wallpaper active)
* Profiling strategy
* `adb shell dumpsys gfxinfo com.example.lumisky reset` before each scenario and collect `framestats` after
* `adb shell dumpsys meminfo com.example.lumisky` after each scenario
* `adb shell top -H -n 1 -b` snapshots during idle and active scenarios
* `adb logcat` filtered for `PreviewGlRenderer`, `WallpaperRenderEngine`, `SunTimesRepository`, `LocationProvider`, `MinuteTickScheduler`
* Optional Perfetto/Android Studio trace if jank remains after top fixes
* Metrics to compare before/after
* UI smoothness: janky frame %, p50/p90/p95 frame times, high input latency count, slow UI thread count
* Renderer efficiency: `PreviewGlRenderer` and `WallpaperRenderEngine` logged `avgDrawMs`, `fps`, draw/skip ratio
* Startup/network load: time to first `SunTimesRepository FETCH_SUCCESS`, time and count of backup prefetch calls, total startup network requests
* Memory: Total PSS, Graphics PSS, `EGL mtrack`, `GL mtrack`, native heap
* Build: total time, configuration time, lint analysis task durations
* Test cases to ensure correctness is preserved
* GPS/manual location mode switching still updates labels/daylight correctly
* Sunrise/sunset fallback behavior still works when network fails/timeouts occur
* Preview and wallpaper visuals remain unchanged for representative themes (`tablo`, `warrior`, `game_teemo`, city themes)
* Continuous/minute-tick rendering behavior remains correct after render-policy changes
* Asset loading still prefers `.webp` and no missing textures/shaders occur
* Wallpaper service wrapper manifest binding remains intact (`com.example.wallpaper.SkyWallpaperService`)

### 6) Optimized Code / Patch (when possible)

* Revised snippets below are suggestions only (not applied in this audit).

```kotlin
// Suggestion: coalesce startup prefetch and defer it off the launch-critical path.
// HomeViewModel.kt (concept)
private fun scheduleStartupBackupPrefetch() {
	mainHandler.removeCallbacks(startupBackupPrefetchRunnable)
	// Delay longer and skip if app is still busy; better: move to WorkManager with constraints.
	mainHandler.postDelayed(startupBackupPrefetchRunnable, 30_000L)
}
```

```kotlin
// Suggestion: reuse resolved GPS values in one refresh cycle to avoid duplicate provider scans.
// HomeViewModel.kt (concept)
private data class ResolvedLocationState(
	val systemEnabled: Boolean,
	val liveGps: SunLocation?,
	val lastGps: SunLocation?
)

private fun resolveLocationStateOnce(): ResolvedLocationState { /* single provider scan pass */ }
private fun refreshLocationAndSunTimesCoalesced(reason: String) { /* debounce + single pipeline */ }
```

```kotlin
// Suggestion: avoid double read during texture path resolution.
// PreviewSkyProgram.kt (concept)
private data class ResolvedTexture(val path: String, val bytes: ByteArray)

private fun resolveTextureBytes(
	originalPath: String,
	loader: (String) -> ByteArray?
): ResolvedTexture? {
	val webpCandidate = /* build path */
	val webpBytes = loader(webpCandidate)
	if (webpBytes != null && webpBytes.isNotEmpty()) return ResolvedTexture(webpCandidate, webpBytes)
	val bytes = loader(originalPath) ?: return null
	return ResolvedTexture(originalPath, bytes)
}
```

```kotlin
// Suggestion: replace list allocations in per-frame hash paths.
// SkyRenderer.kt / SceneStateHasher.kt (concept)
private fun hashState(...): Int {
	var result = 17
	result = 31 * result + mode.ordinal
	result = 31 * result + quantize(progress)
	result = 31 * result + quantize(sunX)
	// ...
	result = 31 * result + skyColor
	return result
}
```

```kotlin
// Suggestion: make wallpaper render policy data-driven.
// WallpaperConfig.kt (concept)
enum class RenderPolicy { STATIC, MINUTE_TICK, CONTINUOUS }
data class RuntimeRenderPolicy(
	val policy: RenderPolicy = RenderPolicy.MINUTE_TICK,
	val continuousFrameIntervalMs: Long = 16L
)
```

* Additional assumptions / confidence notes for this audit:
* Measured evidence was collected from local Gradle profiles and `adb` runs on device `M2101K9AG` (Android 13).
* Wallpaper-service-specific runtime observations are partial (logs indicate active `WallpaperRenderEngine` in process during app runs, but not all service lifecycle scenarios were manually validated in this pass).
* Build findings about asset conversion are based on static script review plus profiled warm-build behavior (asset tasks were `UP-TO-DATE` in measured runs).
