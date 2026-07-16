package com.example.lumisky.layers

import com.example.lumisky.definition.LayerDefinition
import com.example.lumisky.definition.LayerParallaxDefinition
import com.example.lumisky.engine.MutableRenderFrameState
import org.junit.Assert.assertEquals
import org.junit.Test

class BaseLayerParallaxTest {
    @Test
    fun scalesFrameOffsetsByResolvedDepth() {
        val layer = TestLayer(
            LayerDefinition(
                id = "layer",
                type = "TextureLayer",
                parallax = LayerParallaxDefinition(depth = 0.25f)
            )
        )
        val frame = MutableRenderFrameState(parallaxOffsetX = 0.04f, parallaxOffsetY = -0.02f)

        assertEquals(0.01f, layer.x(frame), 0.0001f)
        assertEquals(-0.005f, layer.y(frame), 0.0001f)
    }

    private class TestLayer(definition: LayerDefinition) : BaseLayer(definition) {
        fun x(frame: MutableRenderFrameState): Float = resolveParallaxX(frame)
        fun y(frame: MutableRenderFrameState): Float = resolveParallaxY(frame)
    }
}
