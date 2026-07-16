# Central Layer Parallax Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Parallax-enabled wallpapers automatically receive back-to-front layer depth from `0.05` to `1.0`, while explicit layer depth and wallpaper-level motion settings remain overrideable.

**Architecture:** A pure resolver computes depth once during `SceneFactory` preparation from `CompiledLayerGraph.layersByIndex`. `SceneFactory` copies the resolved depth into the definition passed to `LayerRegistry`; allocation-free `BaseLayer` helpers feed it to every texture/shader render path. Samurai uses automatic depth, while Castle feeds full input into its existing internal shader factors.

**Tech Stack:** Kotlin, JUnit4, kotlinx serialization, OpenGL ES 3, Gradle wallpaper validators.

## Global Constraints

- Automatic depth runs only when `WallpaperDefinition.parallax.enabled == true`.
- Multiple active layers map linearly from `0.05f` to `1.0f` in `layersByIndex` order; a single active layer maps to `1.0f`.
- Existing `LayerDefinition.parallax.depth` overrides the automatic value and is clamped to `0f..1f`.
- `WallpaperDefinition.parallax.maxOffsetX`, `maxOffsetY`, and `smoothing` remain unchanged and overrideable per wallpaper.
- No JSON, collection building, allocation, or index calculation enters render/update hot paths.
- GL ownership, visibility gating, mailbox coalescing, and scheduler behavior remain unchanged.
- Do not stage, commit, or discard changes unless the user explicitly requests it.

---

### Task 1: Pure depth resolver

**Files:**
- Create: `app/src/main/java/com/example/lumisky/engine/LayerParallaxDepthResolver.kt`
- Create: `app/src/test/java/com/adnan/lumisky/engine/LayerParallaxDepthResolverTest.kt`

**Interfaces:**
- Produces: `LayerParallaxDepthResolver.resolveDepth(layer: LayerDefinition, layerIndex: Int, layerCount: Int, parallaxEnabled: Boolean): Float`.

- [ ] **Step 1: Write the failing tests**

```kotlin
package com.example.lumisky.engine

import com.example.lumisky.definition.LayerDefinition
import com.example.lumisky.definition.LayerParallaxDefinition
import org.junit.Assert.assertEquals
import org.junit.Test

class LayerParallaxDepthResolverTest {
    private val layer = LayerDefinition(id = "layer", type = "TextureLayer")

    @Test fun distributesBackMiddleAndFrontByIndex() {
        assertEquals(0.05f, LayerParallaxDepthResolver.resolveDepth(layer, 0, 3, true), 0.0001f)
        assertEquals(0.525f, LayerParallaxDepthResolver.resolveDepth(layer, 1, 3, true), 0.0001f)
        assertEquals(1f, LayerParallaxDepthResolver.resolveDepth(layer, 2, 3, true), 0.0001f)
    }

    @Test fun singleLayerReceivesFullDepth() {
        assertEquals(1f, LayerParallaxDepthResolver.resolveDepth(layer, 0, 1, true), 0.0001f)
    }

    @Test fun explicitDepthOverridesAndClampsAutomaticDepth() {
        val explicit = layer.copy(parallax = LayerParallaxDefinition(depth = 1.4f))
        assertEquals(1f, LayerParallaxDepthResolver.resolveDepth(explicit, 0, 4, true), 0.0001f)
    }

    @Test fun disabledWallpaperDoesNotAddAutomaticDepth() {
        assertEquals(0f, LayerParallaxDepthResolver.resolveDepth(layer, 2, 3, false), 0.0001f)
    }
}
```

- [ ] **Step 2: Verify RED**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.example.lumisky.engine.LayerParallaxDepthResolverTest" --console=plain
```

Expected: compilation failure because `LayerParallaxDepthResolver` does not exist.

- [ ] **Step 3: Implement the resolver**

```kotlin
package com.example.lumisky.engine

import com.example.lumisky.definition.LayerDefinition

object LayerParallaxDepthResolver {
    fun resolveDepth(layer: LayerDefinition, layerIndex: Int, layerCount: Int, parallaxEnabled: Boolean): Float {
        layer.parallax?.let { return it.depth.coerceIn(0f, 1f) }
        if (!parallaxEnabled) return 0f
        if (layerCount <= 1) return 1f
        val normalized = layerIndex.coerceIn(0, layerCount - 1).toFloat() / (layerCount - 1).toFloat()
        return MIN_DEPTH + ((MAX_DEPTH - MIN_DEPTH) * normalized)
    }

    private const val MIN_DEPTH = 0.05f
    private const val MAX_DEPTH = 1f
}
```

- [ ] **Step 4: Verify GREEN and review scope**

Run the Step 2 command; expect `BUILD SUCCESSFUL`, then run:

```powershell
git diff --check
git diff -- app/src/main/java/com/example/lumisky/engine/LayerParallaxDepthResolver.kt app/src/test/java/com/adnan/lumisky/engine/LayerParallaxDepthResolverTest.kt
```

---

### Task 2: Scene preparation integration

**Files:**
- Modify: `app/src/main/java/com/example/lumisky/registry/SceneFactory.kt`
- Create: `app/src/test/java/com/adnan/lumisky/registry/SceneFactoryParallaxTest.kt`

**Interfaces:**
- Consumes: Task 1 resolver.
- Produces: final `RenderLayer.parallaxDepth` before GL/render work begins.

- [ ] **Step 1: Write the failing integration test**

```kotlin
package com.example.lumisky.registry

import com.example.lumisky.definition.LayerDefinition
import com.example.lumisky.definition.LayerParallaxDefinition
import com.example.lumisky.definition.ParallaxDefinition
import com.example.lumisky.definition.WallpaperDefinition
import com.example.lumisky.layers.BaseLayer
import com.example.lumisky.layers.RenderLayer
import javax.inject.Provider
import org.junit.Assert.assertEquals
import org.junit.Test

class SceneFactoryParallaxTest {
@Test fun passesAutomaticAndExplicitDepthsToLayerFactories() {
    val factory: LayerFactory = object : LayerFactory {
        override fun create(definition: LayerDefinition): RenderLayer = object : BaseLayer(definition) {}
    }
    val registry = LayerRegistry(mapOf("TextureLayer" to Provider { factory }))
    val scene = SceneFactory(registry).create(
        WallpaperDefinition(
            id = "layers",
            name = "Layers",
            category = "test",
            parallax = ParallaxDefinition(enabled = true),
            layers = listOf(
                LayerDefinition(id = "back", type = "TextureLayer", zIndex = 0),
                LayerDefinition(id = "middle", type = "TextureLayer", zIndex = 1),
                LayerDefinition(id = "front", type = "TextureLayer", zIndex = 2,
                    parallax = LayerParallaxDefinition(depth = 0.8f))
            )
        )
    )
    assertEquals(listOf(0.05f, 0.525f, 0.8f), scene.layers.map { it.parallaxDepth })
}
}
```

- [ ] **Step 2: Verify RED**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.example.lumisky.registry.SceneFactoryParallaxTest" --console=plain
```

Expected: assertion failure because absent layer depths are still `0f`.

- [ ] **Step 3: Resolve definitions once in `SceneFactory.create`**

Use `forEachIndexed` over `layerGraph.layersByIndex`. When wallpaper parallax is enabled, call the Task 1 resolver and pass this copy to `LayerRegistry`:

```kotlin
val depth = LayerParallaxDepthResolver.resolveDepth(sourceDefinition, layerIndex, layerCount, true)
val resolvedDefinition = sourceDefinition.copy(
    parallax = (sourceDefinition.parallax ?: LayerParallaxDefinition()).copy(depth = depth)
)
```

When wallpaper parallax is disabled, pass `sourceDefinition` unchanged. Add only the required `LayerParallaxDefinition` and `LayerParallaxDepthResolver` imports.

- [ ] **Step 4: Verify GREEN and review scope**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.example.lumisky.engine.LayerParallaxDepthResolverTest" --tests "com.example.lumisky.registry.SceneFactoryParallaxTest" --console=plain
git diff --check
git diff -- app/src/main/java/com/example/lumisky/registry/SceneFactory.kt app/src/test/java/com/adnan/lumisky/registry/SceneFactoryParallaxTest.kt
```

Expected: `BUILD SUCCESSFUL`.

---

### Task 3: Shared allocation-free render offsets

**Files:**
- Modify: `app/src/main/java/com/example/lumisky/layers/BaseLayer.kt`
- Modify: `app/src/main/java/com/example/lumisky/layers/TextureLayer.kt`
- Modify: `app/src/main/java/com/example/lumisky/layers/ShaderLayer.kt`
- Modify: `app/src/main/java/com/example/lumisky/layers/TimeSliceTextureLayer.kt`
- Create: `app/src/test/java/com/adnan/lumisky/layers/BaseLayerParallaxTest.kt`

**Interfaces:**
- Produces: protected `resolveParallaxX(frame)` and `resolveParallaxY(frame)` functions shared by all texture/shader paths.

- [ ] **Step 1: Write the failing BaseLayer test**

```kotlin
package com.example.lumisky.layers

import com.example.lumisky.definition.LayerDefinition
import com.example.lumisky.definition.LayerParallaxDefinition
import com.example.lumisky.engine.MutableRenderFrameState
import org.junit.Assert.assertEquals
import org.junit.Test

class BaseLayerParallaxTest {
@Test fun scalesFrameOffsetsByResolvedDepth() {
    val layer = TestLayer(LayerDefinition(id = "layer", type = "TextureLayer",
        parallax = LayerParallaxDefinition(depth = 0.25f)))
    val frame = MutableRenderFrameState(parallaxOffsetX = 0.04f, parallaxOffsetY = -0.02f)
    assertEquals(0.01f, layer.x(frame), 0.0001f)
    assertEquals(-0.005f, layer.y(frame), 0.0001f)
}

private class TestLayer(definition: LayerDefinition) : BaseLayer(definition) {
    fun x(frame: MutableRenderFrameState) = resolveParallaxX(frame)
    fun y(frame: MutableRenderFrameState) = resolveParallaxY(frame)
}
}
```

- [ ] **Step 2: Verify RED**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.example.lumisky.layers.BaseLayerParallaxTest" --console=plain
```

Expected: compilation failure because both helpers are absent.

- [ ] **Step 3: Implement and consume the helpers**

Add to `BaseLayer`:

```kotlin
protected fun resolveParallaxX(frame: MutableRenderFrameState): Float =
    frame.parallaxOffsetX * parallaxDepth.coerceIn(0f, 1f)

protected fun resolveParallaxY(frame: MutableRenderFrameState): Float =
    frame.parallaxOffsetY * parallaxDepth.coerceIn(0f, 1f)
```

Use these values for `u_ParallaxOffset` in `TextureLayer` and `TimeSliceTextureLayer`. In `ShaderLayer`, use the same values for both `u_ParallaxOffset` and `u_Parallax`, removing its duplicated depth calculation. Do not allocate a pair/data object per frame.

- [ ] **Step 4: Verify GREEN and review scope**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.example.lumisky.layers.BaseLayerParallaxTest" --tests "com.example.lumisky.engine.LayerParallaxDepthResolverTest" --tests "com.example.lumisky.registry.SceneFactoryParallaxTest" --console=plain
git diff --check
git diff -- app/src/main/java/com/example/lumisky/layers/BaseLayer.kt app/src/main/java/com/example/lumisky/layers/TextureLayer.kt app/src/main/java/com/example/lumisky/layers/ShaderLayer.kt app/src/main/java/com/example/lumisky/layers/TimeSliceTextureLayer.kt app/src/test/java/com/adnan/lumisky/layers/BaseLayerParallaxTest.kt
```

Expected: `BUILD SUCCESSFUL`.

---

### Task 4: Castle override and Samurai contract

**Files:**
- Modify: `app/src/main/assets/wallpapers/sky.json`
- Create: `app/src/test/java/com/adnan/lumisky/engine/CentralParallaxWallpaperContractTest.kt`

- [ ] **Step 1: Write failing content contracts**

```kotlin
package com.example.lumisky.engine

import com.example.lumisky.definition.WallpaperDefinition
import java.io.File
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CentralParallaxWallpaperContractTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun castleUsesFullLayerOverrideAndKeepsInternalDepthFactors() {
        val definition = json.decodeFromString<WallpaperDefinition>(
            File("src/main/assets/wallpapers/sky.json").readText()
        )
        val shader = File("src/main/assets/wallpapers/sky/fragment.glsl").readText()

        assertTrue(definition.parallax?.enabled == true)
        assertEquals(1f, definition.layers.single().parallax?.depth ?: -1f, 0.0001f)
        assertTrue(shader.contains("ustBulutParallax"))
        assertTrue(shader.contains("altBulutParallax"))
        assertTrue(shader.contains("castleParallax"))
    }

    @Test fun samuraiLayersRemainAutomaticAndOrderedBackToFront() {
        val definition = json.decodeFromString<WallpaperDefinition>(
            File("src/main/assets/wallpapers/samurai_fuji_twilight.json").readText()
        )

        assertTrue(definition.parallax?.enabled == true)
        assertTrue(definition.layers.zipWithNext().all { (back, front) -> back.zIndex < front.zIndex })
        definition.layers.forEach { assertNull(it.parallax) }
    }
}
```

- [ ] **Step 2: Verify RED**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.example.lumisky.engine.CentralParallaxWallpaperContractTest" --console=plain
```

Expected: Castle depth assertion fails with actual `0f`.

- [ ] **Step 3: Change only Castle's layer override**

In `app/src/main/assets/wallpapers/sky.json`, change the existing layer parallax block's `"depth": 0.0` to `"depth": 1.0`. Do not alter Castle shader factors or Samurai manifest.

- [ ] **Step 4: Verify GREEN and content validity**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.example.lumisky.engine.CentralParallaxWallpaperContractTest" --console=plain
.\gradlew.bat :app:validateWallpaperDefinitions --console=plain
git diff --check
git diff -- app/src/main/assets/wallpapers/sky.json app/src/test/java/com/adnan/lumisky/engine/CentralParallaxWallpaperContractTest.kt
```

Expected: both Gradle commands report `BUILD SUCCESSFUL`.

---

### Task 5: Final verification

**Files:** Verify only; no planned production edits.

- [ ] **Step 1: Run affected module gates**

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:validateWallpaperDefinitions :app:assembleDebug --console=plain
```

Expected: `BUILD SUCCESSFUL` with no failing unit test or wallpaper validator.

- [ ] **Step 2: Install if a device is connected**

```powershell
$apk = (Resolve-Path 'app\build\outputs\apk\debug\app-debug.apk').Path
adb devices -l
adb install -r $apk
```

If no device is listed, report device verification as blocked without changing code.

- [ ] **Step 3: Visually verify both wallpapers**

Set Samurai and then Castle through the Android picker. Tilt the device and confirm Samurai's sky moves least and samurai most; Castle's upper cloud moves least, lower cloud more, and castle most. Confirm no exposed texture edge, invisible rendering, crash, ANR, EGL, or GL error.

- [ ] **Step 4: Inspect focused logs and final diff**

```powershell
adb logcat -d -t 3000 | Select-String -Pattern 'FATAL EXCEPTION|ANR in com\.example\.lumisky|OutOfMemoryError|EGL_BAD|GL_INVALID'
git diff --check
git status --short
git diff --stat -- app/src/main app/src/test docs/superpowers
```

Report changed files, index mapping, overrides, validation evidence, device status, and remaining overscan risk.
