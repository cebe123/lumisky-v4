package com.example.lumisky.engine

import com.example.lumisky.core.WallpaperEvent
import com.example.lumisky.definition.LayerFramePolicyDefinition
import com.example.lumisky.definition.QualityProfile
import com.example.lumisky.engine.gl.GlResourceManager
import com.example.lumisky.engine.pipeline.BlendMode
import com.example.lumisky.engine.pipeline.RenderPass
import com.example.lumisky.engine.pipeline.RenderTargetMode
import com.example.lumisky.layers.RenderLayer
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SceneSchedulerPolicyTest {

    @Test
    fun batterySaverFpsOverridesLayerFixedFps() {
        val scheduler = SceneScheduler()
        val layer = TestLayer(
            framePolicy = LayerFramePolicyDefinition(
                mode = "FIXED_FPS",
                fps = 60,
                batterySaverFps = 10
            )
        )

        assertTrue(scheduler.shouldUpdate(layer, frameTimeNanos = 100_000_000L, batterySaver = true))
        assertFalse(scheduler.shouldUpdate(layer, frameTimeNanos = 150_000_000L, batterySaver = true))
        assertTrue(scheduler.shouldUpdate(layer, frameTimeNanos = 200_000_000L, batterySaver = true))
    }

    @Test
    fun fixedFpsCacheRefreshDoesNotConsumeUpdateTimestamp() {
        val scheduler = SceneScheduler()
        val layer = TestLayer(
            framePolicy = LayerFramePolicyDefinition(mode = "FIXED_FPS", fps = 10)
        )

        assertTrue(scheduler.shouldUpdate(layer, frameTimeNanos = 100_000_000L))
        assertTrue(scheduler.shouldRefreshCache(layer, frameTimeNanos = 100_000_000L))
        assertFalse(scheduler.shouldUpdate(layer, frameTimeNanos = 150_000_000L))
        assertFalse(scheduler.shouldRefreshCache(layer, frameTimeNanos = 150_000_000L))
        assertTrue(scheduler.shouldUpdate(layer, frameTimeNanos = 200_000_000L))
        assertTrue(scheduler.shouldRefreshCache(layer, frameTimeNanos = 200_000_000L))
    }

    @Test
    fun minuteTickSceneDoesNotUseContinuousRendering() {
        val interval = SceneFramePacingPolicy.frameIntervalNanos(
            layers = listOf(TestLayer(LayerFramePolicyDefinition(mode = "MINUTE_TICK"))),
            maxFps = 30,
            batterySaver = false
        )

        assertEquals(60_000_000_000L, interval)
    }

    @Test
    fun minuteTickLayerUpdatesOnlyOncePerMinute() {
        val scheduler = SceneScheduler()
        val layer = TestLayer(LayerFramePolicyDefinition(mode = "MINUTE_TICK"))

        assertTrue(scheduler.shouldUpdate(layer, frameTimeNanos = 60_000_000_000L))
        assertFalse(scheduler.shouldUpdate(layer, frameTimeNanos = 60_500_000_000L))
        assertTrue(scheduler.shouldUpdate(layer, frameTimeNanos = 120_000_000_000L))
    }

    @Test
    fun identicalLayerIdsInDifferentScenesHaveIndependentSchedules() {
        val scheduler = SceneScheduler()
        val layer = TestLayer(LayerFramePolicyDefinition(mode = "FIXED_FPS", fps = 1))

        assertTrue(scheduler.shouldUpdate(layer, frameTimeNanos = 1_000_000_000L, sceneId = "first"))
        assertTrue(scheduler.shouldUpdate(layer, frameTimeNanos = 1_000_000_000L, sceneId = "second"))
    }

    @Test
    fun separateSessionsDoNotShareScheduleState() {
        val firstSessionScheduler = SceneScheduler()
        val secondSessionScheduler = SceneScheduler()
        val layer = TestLayer(LayerFramePolicyDefinition(mode = "FIXED_FPS", fps = 1))

        assertTrue(firstSessionScheduler.shouldUpdate(layer, frameTimeNanos = 1_000_000_000L, sceneId = "sky"))
        assertTrue(secondSessionScheduler.shouldUpdate(layer, frameTimeNanos = 1_000_000_000L, sceneId = "sky"))
    }

    @Test
    fun onDemandSceneHasNoRecurringFrame() {
        val interval = SceneFramePacingPolicy.frameIntervalNanos(
            layers = listOf(TestLayer(LayerFramePolicyDefinition(mode = "ON_DEMAND"))),
            maxFps = 30,
            batterySaver = false
        )

        assertNull(interval)
    }

    @Test
    fun catchUpTemporarilyUsesSceneFps() {
        val interval = SceneFramePacingPolicy.frameIntervalNanos(
            layers = listOf(TestLayer(LayerFramePolicyDefinition(mode = "MINUTE_TICK"))),
            maxFps = 30,
            batterySaver = false,
            forceContinuous = true
        )

        assertEquals(33_333_333L, interval)
    }

    private class TestLayer(
        override val framePolicy: LayerFramePolicyDefinition
    ) : RenderLayer {
        override val id: String = "test"
        override val zIndex: Int = 0
        override val renderPass: RenderPass = RenderPass.BACKGROUND
        override val blendMode: BlendMode = BlendMode.NONE
        override val renderTargetMode: RenderTargetMode = RenderTargetMode.DIRECT
        override val frameMode: com.example.lumisky.layers.LayerFrameMode =
            com.example.lumisky.layers.LayerFrameMode.valueOf(framePolicy.mode)
        override val cacheMode: com.example.lumisky.layers.LayerCacheMode =
            com.example.lumisky.layers.LayerCacheMode.valueOf(framePolicy.cacheMode)
        override val parallaxDepth: Float = 0f

        override fun onCreateGl(gl: GlResourceManager, context: RenderContext) = Unit
        override fun onSurfaceChanged(context: RenderContext, width: Int, height: Int) = Unit
        override fun onEvent(event: WallpaperEvent) = Unit
        override fun update(frame: MutableRenderFrameState) = Unit
        override fun render(frame: MutableRenderFrameState) = Unit
        override fun onQualityChanged(profile: QualityProfile) = Unit
        override fun onDestroyGl(gl: GlResourceManager) = Unit
    }
}
