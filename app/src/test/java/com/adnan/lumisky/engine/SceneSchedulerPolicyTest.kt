package com.adnan.lumisky.engine

import com.adnan.lumisky.core.WallpaperEvent
import com.adnan.lumisky.definition.LayerFramePolicyDefinition
import com.adnan.lumisky.definition.QualityProfile
import com.adnan.lumisky.engine.gl.GlResourceManager
import com.adnan.lumisky.engine.pipeline.BlendMode
import com.adnan.lumisky.engine.pipeline.RenderPass
import com.adnan.lumisky.engine.pipeline.RenderTargetMode
import com.adnan.lumisky.layers.RenderLayer
import org.junit.Assert.assertFalse
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

    private class TestLayer(
        override val framePolicy: LayerFramePolicyDefinition
    ) : RenderLayer {
        override val id: String = "test"
        override val zIndex: Int = 0
        override val renderPass: RenderPass = RenderPass.BACKGROUND
        override val blendMode: BlendMode = BlendMode.NONE
        override val renderTargetMode: RenderTargetMode = RenderTargetMode.DIRECT
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
