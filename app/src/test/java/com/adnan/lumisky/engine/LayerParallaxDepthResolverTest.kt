package com.example.lumisky.engine

import com.example.lumisky.definition.LayerDefinition
import com.example.lumisky.definition.LayerParallaxDefinition
import org.junit.Assert.assertEquals
import org.junit.Test

class LayerParallaxDepthResolverTest {
    private val layer = LayerDefinition(id = "layer", type = "TextureLayer")

    @Test
    fun distributesBackMiddleAndFrontByIndex() {
        assertEquals(0.05f, LayerParallaxDepthResolver.resolveDepth(layer, 0, 3, true), 0.0001f)
        assertEquals(0.525f, LayerParallaxDepthResolver.resolveDepth(layer, 1, 3, true), 0.0001f)
        assertEquals(1f, LayerParallaxDepthResolver.resolveDepth(layer, 2, 3, true), 0.0001f)
    }

    @Test
    fun singleLayerReceivesFullDepth() {
        assertEquals(1f, LayerParallaxDepthResolver.resolveDepth(layer, 0, 1, true), 0.0001f)
    }

    @Test
    fun explicitDepthOverridesAndClampsAutomaticDepth() {
        val explicit = layer.copy(parallax = LayerParallaxDefinition(depth = 1.4f))

        assertEquals(1f, LayerParallaxDepthResolver.resolveDepth(explicit, 0, 4, true), 0.0001f)
    }

    @Test
    fun parallaxFactorsWithoutDepthKeepAutomaticDistribution() {
        val factorsOnly = layer.copy(parallax = LayerParallaxDefinition(factorX = 0.2f, factorY = 0.1f))

        assertEquals(0.525f, LayerParallaxDepthResolver.resolveDepth(factorsOnly, 1, 3, true), 0.0001f)
    }

    @Test
    fun disabledWallpaperDoesNotAddAutomaticDepth() {
        assertEquals(0f, LayerParallaxDepthResolver.resolveDepth(layer, 2, 3, false), 0.0001f)
    }
}
