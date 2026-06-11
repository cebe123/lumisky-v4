# Lumisky Startup And Render Optimization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Improve app startup responsiveness and reduce wallpaper engine/render wakeups without changing the public architecture or wallpaper lifecycle.

**Architecture:** This is a two-track plan. Track A is app-only: decouple Home first render from expensive startup cache warmup. Track B is wallpaper/render-only: make continuous rendering more selective while preserving visible/surface gating, preview behavior, and low-battery throttles.

**Tech Stack:** Android, Kotlin, Compose, OpenGL ES, Gradle, JUnit4.

---

## Scope

Implement the tracks independently. Track A can ship without Track B. Track B must not change preview-mode behavior and must keep animated wallpapers continuous unless a test proves they are safe to demote.

## File Map

- Modify: `app/src/main/java/com/example/lumisky/MainActivity.kt`
  - Decouple `startupCacheReady` from full `warmHomeStartupCaches()` completion.
  - Warm a small visible-first subset before idle/deferred cache warming.
- Create: `app/src/main/java/com/example/lumisky/HomeStartupWarmupPlanner.kt`
  - Pure helper for selecting immediate and deferred warmup items.
- Create: `app/src/test/java/com/example/lumisky/HomeStartupWarmupPlannerTest.kt`
  - Unit tests for warmup ordering and limits.
- Modify: `wallpaper/src/main/java/com/example/wallpaper/service/ServiceRenderPolicyResolver.kt`
  - Keep `CONTINUOUS` only for configs that really require per-frame motion.
  - Preserve power saver and thermal floors.
- Modify: `wallpaper/src/test/java/com/example/wallpaper/service/ServiceRenderPolicyResolverTest.kt`
  - Add regression tests for dynamic texture, dynamic motion, preview, and static/minute-tick behavior.
- Inspect only if needed: `app/src/main/assets/wallpapers/*/manifest.json`
  - Confirm which wallpapers are currently `CONTINUOUS` and why.

---

### Task 1: Baseline And Guardrails

**Files:**
- Read: `app/src/main/java/com/example/lumisky/MainActivity.kt`
- Read: `wallpaper/src/main/java/com/example/wallpaper/service/ServiceRenderPolicyResolver.kt`
- Read: `wallpaper/src/test/java/com/example/wallpaper/service/ServiceRenderPolicyResolverTest.kt`

- [ ] **Step 1: Capture current continuous wallpapers**

Run:

```powershell
rg -n '"policy": "CONTINUOUS"|"dynamicMotion"|"dynamicTextures"|continuousFrameIntervalMs' app\src\main\assets\wallpapers -S
```

Expected: list includes current continuous targets such as `sky`, `flower`, and `warrior`.

- [ ] **Step 2: Run current fast validation**

Run:

```powershell
.\gradlew.bat :app:lintDebugLocal
.\gradlew.bat :wallpaper:testDebugUnitTest
```

Expected: both pass before code changes. If either fails, record the existing failure and do not mix unrelated fixes into this plan.

---

### Task 2: Startup Warmup Planner

**Files:**
- Create: `app/src/main/java/com/example/lumisky/HomeStartupWarmupPlanner.kt`
- Create: `app/src/test/java/com/example/lumisky/HomeStartupWarmupPlannerTest.kt`

- [ ] **Step 1: Write failing planner tests**

Create `app/src/test/java/com/example/lumisky/HomeStartupWarmupPlannerTest.kt`:

```kotlin
package com.example.lumisky

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeStartupWarmupPlannerTest {
	@Test
	fun keepsImmediateItemsAtFrontAndDefersTheRest() {
		val plan = splitStartupWarmupItems(
			items = listOf("a", "b", "c", "d"),
			immediateSnapshotLimit = 2,
			renderAssetLimit = 1
		)

		assertEquals(listOf("a", "b"), plan.immediateSnapshotItems)
		assertEquals(listOf("c", "d"), plan.deferredSnapshotItems)
		assertEquals(listOf("a"), plan.renderAssetItems)
	}

	@Test
	fun handlesEmptyItems() {
		val plan = splitStartupWarmupItems(
			items = emptyList<String>(),
			immediateSnapshotLimit = 2,
			renderAssetLimit = 1
		)

		assertEquals(emptyList<String>(), plan.immediateSnapshotItems)
		assertEquals(emptyList<String>(), plan.deferredSnapshotItems)
		assertEquals(emptyList<String>(), plan.renderAssetItems)
	}
}
```

- [ ] **Step 2: Run test and verify it fails**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.example.lumisky.HomeStartupWarmupPlannerTest
```

Expected: fail because `splitStartupWarmupItems` does not exist.

- [ ] **Step 3: Add minimal planner**

Create `app/src/main/java/com/example/lumisky/HomeStartupWarmupPlanner.kt`:

```kotlin
package com.example.lumisky

internal data class HomeStartupWarmupPlan<T>(
	val immediateSnapshotItems: List<T>,
	val deferredSnapshotItems: List<T>,
	val renderAssetItems: List<T>
)

internal fun <T> splitStartupWarmupItems(
	items: List<T>,
	immediateSnapshotLimit: Int,
	renderAssetLimit: Int
): HomeStartupWarmupPlan<T> {
	val immediateLimit = immediateSnapshotLimit.coerceAtLeast(0)
	val renderLimit = renderAssetLimit.coerceAtLeast(0)
	return HomeStartupWarmupPlan(
		immediateSnapshotItems = items.take(immediateLimit),
		deferredSnapshotItems = items.drop(immediateLimit),
		renderAssetItems = items.take(renderLimit)
	)
}
```

- [ ] **Step 4: Run planner tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.example.lumisky.HomeStartupWarmupPlannerTest
```

Expected: PASS.

---

### Task 3: Decouple Home First Render From Full Warmup

**Files:**
- Modify: `app/src/main/java/com/example/lumisky/MainActivity.kt`

- [ ] **Step 1: Replace all-items blocking warmup with visible-first warmup**

In `LaunchedEffect(homeViewModel)`, keep `startupCacheReady = true` after only the immediate subset is warmed. Then defer the rest after first Home frame.

Implementation shape:

```kotlin
val warmupPlan = splitStartupWarmupItems(
	items = homeViewModel.items,
	immediateSnapshotLimit = STARTUP_IMMEDIATE_SNAPSHOT_WARM_LIMIT,
	renderAssetLimit = STARTUP_RENDER_ASSET_WARM_LIMIT
)
warmHomeStartupCaches(
	snapshotItems = warmupPlan.immediateSnapshotItems,
	renderAssetItems = warmupPlan.renderAssetItems
)
startupCacheReady = true
startupAnimationsEnabled = true
reportFullyDrawn()
withFrameNanos { }
warmHomeDeferredSnapshotCaches(warmupPlan.deferredSnapshotItems)
```

- [ ] **Step 2: Split warmup function parameters**

Change the existing private function from one list to two explicit lists:

```kotlin
private suspend fun warmHomeStartupCaches(
	snapshotItems: List<HomeWallpaperItem>,
	renderAssetItems: List<HomeWallpaperItem>
)
```

Snapshot loop uses `snapshotItems`; render prewarm loop uses `renderAssetItems`.

- [ ] **Step 3: Add deferred snapshot-only warmup**

Add a private helper in `MainActivity.kt`:

```kotlin
private suspend fun warmHomeDeferredSnapshotCaches(items: List<HomeWallpaperItem>) {
	if (items.isEmpty()) return
	withContext(Dispatchers.IO) {
		val snapshotLoader = SnapshotPreviewAssetLoader(applicationContext)
		val (previewWidthPx, previewHeightPx) = resolveHomeStartupPreviewSizePx()
		items.forEach { item ->
			runCatching {
				snapshotLoader.loadBitmap(
					configId = item.config.id,
					targetWidthPx = previewWidthPx,
					targetHeightPx = previewHeightPx
				)
			}.onFailure {
				Logger.w(TAG, "failed to warm deferred startup snapshot configId=${item.config.id}", it)
			}
		}
	}
}
```

- [ ] **Step 4: Add constants**

Near existing startup constants in `MainActivity.kt`, add:

```kotlin
private const val STARTUP_IMMEDIATE_SNAPSHOT_WARM_LIMIT = 3
```

Keep `STARTUP_RENDER_ASSET_WARM_LIMIT` unchanged.

- [ ] **Step 5: Validate app track**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.example.lumisky.HomeStartupWarmupPlannerTest
.\gradlew.bat :app:lintDebugLocal
```

Expected: both pass. Manual phone check should confirm first Home paint appears before full preview cache warmup completes.

---

### Task 4: Render Policy Regression Tests

**Files:**
- Modify: `wallpaper/src/test/java/com/example/wallpaper/service/ServiceRenderPolicyResolverTest.kt`

- [ ] **Step 1: Add tests for selective continuous rendering**

Append tests:

```kotlin
@Test
fun dynamic_textures_keep_continuous_loop() {
	val resolver = ServiceRenderPolicyResolver()
	val config = WallpaperConfig.default(id = "dynamic_texture").copy(
		runtimeRenderPolicy = RuntimeRenderPolicy(
			policy = RenderPolicy.MINUTE_TICK,
			continuousFrameIntervalMs = 33L
		),
		capabilities = WallpaperCapabilities(
			dynamicMotion = false,
			dynamicTextures = true,
			locationAwareLighting = true,
			supportsCloudLayer = false,
			supportsStarLayer = true
		)
	)

	val resolved = resolver.resolve(
		config = config,
		previewMode = false,
		visible = true,
		surfaceAttached = true
	)

	assertEquals(WallpaperLoopMode.VSYNC, resolved.loopMode)
	assertEquals(33L, resolved.frameIntervalMs)
}

@Test
fun dynamic_motion_without_dynamic_textures_can_remain_minute_tick() {
	val resolver = ServiceRenderPolicyResolver()
	val config = WallpaperConfig.default(id = "dynamic_motion_only").copy(
		runtimeRenderPolicy = RuntimeRenderPolicy(
			policy = RenderPolicy.MINUTE_TICK,
			continuousFrameIntervalMs = 33L
		),
		capabilities = WallpaperCapabilities(
			dynamicMotion = true,
			dynamicTextures = false,
			locationAwareLighting = true,
			supportsCloudLayer = false,
			supportsStarLayer = true
		)
	)

	val resolved = resolver.resolve(
		config = config,
		previewMode = false,
		visible = true,
		surfaceAttached = true
	)

	assertEquals(WallpaperLoopMode.MINUTE_TICK, resolved.loopMode)
}
```

- [ ] **Step 2: Run tests and verify targeted failure**

Run:

```powershell
.\gradlew.bat :wallpaper:testDebugUnitTest --tests com.example.wallpaper.service.ServiceRenderPolicyResolverTest
```

Expected: `dynamic_motion_without_dynamic_textures_can_remain_minute_tick` fails before resolver change.

---

### Task 5: Selective Continuous Render Policy

**Files:**
- Modify: `wallpaper/src/main/java/com/example/wallpaper/service/ServiceRenderPolicyResolver.kt`

- [ ] **Step 1: Add helper for promotion decision**

Add private helper:

```kotlin
private fun shouldPromoteDynamicContentToContinuous(config: WallpaperConfig): Boolean {
	return config.capabilities.dynamicTextures
}
```

- [ ] **Step 2: Use helper in existing promotion block**

Replace:

```kotlin
val hasDynamicContent = config.capabilities.dynamicMotion || config.capabilities.dynamicTextures
if (hasDynamicContent && policy != RenderPolicy.CONTINUOUS) {
	policy = RenderPolicy.CONTINUOUS
}
```

with:

```kotlin
if (policy != RenderPolicy.CONTINUOUS && shouldPromoteDynamicContentToContinuous(config)) {
	policy = RenderPolicy.CONTINUOUS
}
```

- [ ] **Step 3: Preserve explicit continuous configs**

Do not change configs whose `runtimeRenderPolicy.policy` or service override is already `CONTINUOUS`. This keeps `flower`, `warrior`, and any explicit manifest continuous policy stable until each manifest is audited separately.

- [ ] **Step 4: Validate render policy**

Run:

```powershell
.\gradlew.bat :wallpaper:testDebugUnitTest --tests com.example.wallpaper.service.ServiceRenderPolicyResolverTest
.\gradlew.bat :app:validateShaderCelestialMotionContinuity
```

Expected: policy tests pass; shader continuity check passes.

---

### Task 6: Manifest-Level Continuous Audit

**Files:**
- Inspect: `app/src/main/assets/wallpapers/*/manifest.json`
- Modify only if proven safe: specific `manifest.json`

- [ ] **Step 1: List explicit continuous manifests**

Run:

```powershell
rg -n '"policy": "CONTINUOUS"|"overridePolicy": "CONTINUOUS"|"continuousFrameIntervalMs"' app\src\main\assets\wallpapers -S
```

- [ ] **Step 2: Classify each continuous wallpaper**

Use this rule:

```text
Keep CONTINUOUS: animated textures, shader time animation visible on home, particle motion, moving creator layers.
Candidate for MINUTE_TICK: sun/moon/daylight-only changes, static background, static creator layers, location-aware lighting only.
```

- [ ] **Step 3: Change one manifest at a time**

For a safe candidate, change:

```json
"runtimeRenderPolicy": {
  "policy": "MINUTE_TICK",
  "continuousFrameIntervalMs": 16
}
```

Remove `"overridePolicy": "CONTINUOUS"` only in the same manifest and only when the visual audit confirms no home-screen animation is lost.

- [ ] **Step 4: Validate after each manifest**

Run:

```powershell
.\gradlew.bat :app:validateShaderCelestialMotionContinuity
.\gradlew.bat :app:lintDebugLocal
```

Expected: both pass. Manual phone check should confirm no lost intended animation.

---

### Task 7: Phone-Side Performance Verification

**Files:**
- No source changes.

- [ ] **Step 1: Deploy debug build**

Run:

```powershell
.\gradlew.bat :app:deployDebugToConnectedDevice
```

- [ ] **Step 2: Collect quick frame evidence**

Run after interacting with Home and returning to launcher:

```powershell
adb shell dumpsys gfxinfo com.example.lumisky framestats
adb shell dumpsys meminfo com.example.lumisky
```

Expected targets:

```text
Home FPS >= 30
Preview FPS >= 60
Frame P95 < 33ms
Memory delta < 10MB after startup warmup
Battery CPU increase < 5% during home wallpaper idle
```

- [ ] **Step 3: Final validation**

Run:

```powershell
.\gradlew.bat :app:lintDebugLocal
.\gradlew.bat :wallpaper:testDebugUnitTest
.\gradlew.bat :app:validateShaderCelestialMotionContinuity
```

Expected: all pass.

---

## Rollback Plan

- Startup track rollback: revert `MainActivity.kt`, `HomeStartupWarmupPlanner.kt`, and `HomeStartupWarmupPlannerTest.kt`.
- Render track rollback: revert `ServiceRenderPolicyResolver.kt`, `ServiceRenderPolicyResolverTest.kt`, and any touched manifest.
- If phone visual behavior regresses, keep startup track and rollback only render policy/manifest changes.
