package com.example.lumisky.engine

import com.example.lumisky.core.WallpaperEvent
import com.example.lumisky.definition.LayerFramePolicyDefinition
import com.example.lumisky.definition.ParallaxDefinition
import com.example.lumisky.definition.QualityProfile
import com.example.lumisky.definition.WallpaperDefinition
import com.example.lumisky.engine.gl.GlResourceManager
import com.example.lumisky.engine.pipeline.BlendMode
import com.example.lumisky.engine.pipeline.RenderPass
import com.example.lumisky.engine.pipeline.RenderTargetMode
import com.example.lumisky.layers.LayerCacheMode
import com.example.lumisky.layers.LayerFrameMode
import com.example.lumisky.layers.RenderLayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimeSessionIsolationTest {
    @Test
    fun schedulersDoNotShareCadenceState() {
        val liveScheduler = SceneScheduler()
        val previewScheduler = SceneScheduler()
        val layer = OneFpsLayer()

        assertTrue(liveScheduler.shouldUpdate(layer, 1_000_000_000L, sceneId = "scene"))
        assertFalse(liveScheduler.shouldUpdate(layer, 1_000_000_000L, sceneId = "scene"))
        assertTrue(previewScheduler.shouldUpdate(layer, 1_000_000_000L, sceneId = "scene"))
    }

    @Test
    fun parallaxControllersDoNotShareSmoothingState() {
        val definition = WallpaperDefinition(
            id = "scene",
            name = "Scene",
            category = "test",
            parallax = ParallaxDefinition(smoothing = 0.5f, maxOffsetX = 1f, maxOffsetY = 1f)
        )
        val liveFrame = MutableRenderFrameState()
        val previewFrame = MutableRenderFrameState()

        ParallaxController().update(0.8f, 0f, definition, SceneState(), liveFrame)
        ParallaxController().update(-0.8f, 0f, definition, SceneState(), previewFrame)

        assertEquals(0.4f, liveFrame.parallaxOffsetX, 0.0001f)
        assertEquals(-0.4f, previewFrame.parallaxOffsetX, 0.0001f)
    }

    private class OneFpsLayer : RenderLayer {
        override val id = "one-fps"
        override val zIndex = 0
        override val renderPass = RenderPass.BACKGROUND
        override val blendMode = BlendMode.NONE
        override val renderTargetMode = RenderTargetMode.DIRECT
        override val framePolicy = LayerFramePolicyDefinition(mode = "ONE_FPS")
        override val frameMode = LayerFrameMode.ONE_FPS
        override val cacheMode = LayerCacheMode.NONE
        override val parallaxDepth = 0f
        override fun onCreateGl(gl: GlResourceManager, context: RenderContext) = Unit
        override fun onSurfaceChanged(context: RenderContext, width: Int, height: Int) = Unit
        override fun onEvent(event: WallpaperEvent) = Unit
        override fun update(frame: MutableRenderFrameState) = Unit
        override fun render(frame: MutableRenderFrameState) = Unit
        override fun onQualityChanged(profile: QualityProfile) = Unit
        override fun onDestroyGl(gl: GlResourceManager) = Unit
    }
}
