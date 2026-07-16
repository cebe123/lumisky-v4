package com.example.lumisky.engine

import com.example.lumisky.definition.LayerDefinition

object LayerParallaxDepthResolver {
    fun resolveDepth(
        layer: LayerDefinition,
        layerIndex: Int,
        layerCount: Int,
        parallaxEnabled: Boolean
    ): Float {
        layer.parallax?.depth?.let { return it.coerceIn(0f, 1f) }
        if (!parallaxEnabled) return 0f
        if (layerCount <= 1) return 1f
        val normalized = layerIndex.coerceIn(0, layerCount - 1).toFloat() / (layerCount - 1).toFloat()
        return MIN_DEPTH + ((MAX_DEPTH - MIN_DEPTH) * normalized)
    }

    private const val MIN_DEPTH = 0.05f
    private const val MAX_DEPTH = 1f
}
