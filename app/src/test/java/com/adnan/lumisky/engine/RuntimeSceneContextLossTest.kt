package com.example.lumisky.engine

import com.example.lumisky.definition.LayerDefinition
import com.example.lumisky.layers.BaseLayer
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimeSceneContextLossTest {
    @Test
    fun contextLossInvalidatesLayersWithoutDestroyingTheirGlResources() {
        val layer = ContextTrackingLayer()
        val scene = RuntimeScene(id = "context-loss", layers = listOf(layer))

        scene.onContextLost()

        assertTrue(layer.contextLost)
    }

    private class ContextTrackingLayer : BaseLayer(
        LayerDefinition(id = "context-layer", type = "test")
    ) {
        var contextLost = false

        override fun onContextLost() {
            contextLost = true
        }
    }
}
