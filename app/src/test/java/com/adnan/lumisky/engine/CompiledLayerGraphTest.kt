package com.example.lumisky.engine

import com.example.lumisky.definition.LayerDefinition
import com.example.lumisky.definition.LayerFallbackDefinition
import com.example.lumisky.definition.AnimationDefinition
import com.example.lumisky.definition.WallpaperDefinition
import com.example.lumisky.engine.pipeline.RenderPass
import org.junit.Assert.assertEquals
import org.junit.Test

class CompiledLayerGraphTest {

    @Test
    fun ordersEnabledLayersByPassThenZIndexThenDeclarationOrder() {
        val graph = CompiledLayerGraph.compile(
            WallpaperDefinition(
                id = "fuji",
                name = "Fuji",
                category = "test",
                layers = listOf(
                    layer("foreground", zIndex = 1, renderPass = "TRANSPARENT"),
                    layer("second-background", zIndex = 10, renderPass = "BACKGROUND"),
                    layer("first-background", zIndex = 10, renderPass = "BACKGROUND"),
                    layer("disabled", zIndex = 0, renderPass = "BACKGROUND", enabled = false)
                )
            )
        )

        assertEquals(
            listOf("second-background", "first-background", "foreground"),
            graph.layersByIndex.map { it.definition.id }
        )
        assertEquals(
            listOf(RenderPass.BACKGROUND, RenderPass.TRANSPARENT),
            graph.orderedPasses.map { it.pass }
        )
        assertEquals(2, graph.orderedPasses.first().layerCount)
    }

    @Test
    fun compilesFallbackPoliciesIntoTypedPlan() {
        val graph = CompiledLayerGraph.compile(
            WallpaperDefinition(
                id = "fallback",
                name = "Fallback",
                category = "test",
                layers = listOf(
                    layer("safe", zIndex = 0, renderPass = "BACKGROUND").copy(
                        fallback = LayerFallbackDefinition(
                            onShaderError = "use_safe_shader",
                            onMissingTexture = "disable_layer"
                        )
                    ),
                    layer("default", zIndex = 1, renderPass = "BACKGROUND")
                )
            )
        )

        assertEquals(ShaderFallbackAction.USE_SAFE_SHADER, graph.fallbackPlan.layersByIndex[0].onShaderError)
        assertEquals(TextureFallbackAction.DISABLE_LAYER, graph.fallbackPlan.layersByIndex[0].onMissingTexture)
        assertEquals(ShaderFallbackAction.DISABLE_LAYER, graph.fallbackPlan.layersByIndex[1].onShaderError)
        assertEquals(TextureFallbackAction.USE_TRANSPARENT_TEXTURE, graph.fallbackPlan.layersByIndex[1].onMissingTexture)
    }

    @Test
    fun compilesSupportedAnimationIntoLayerIndexedTrack() {
        val graph = CompiledLayerGraph.compile(
            WallpaperDefinition(
                id = "animated",
                name = "Animated",
                category = "test",
                layers = listOf(
                    layer("clouds", zIndex = 0, renderPass = "BACKGROUND").copy(
                        animation = AnimationDefinition(
                            type = "UV_SCROLL",
                            durationMs = 800L,
                            loop = false,
                            parameters = linkedMapOf("speed" to 0.25f)
                        )
                    ),
                    layer("static", zIndex = 1, renderPass = "BACKGROUND")
                )
            )
        )

        val track = graph.animationPlan.tracksByLayerIndex[0]
        assertEquals(CompiledAnimationType.UV_SCROLL, track?.type)
        assertEquals(800_000_000L, track?.durationNanos)
        assertEquals(false, track?.loop)
        assertEquals("speed", track?.parameters?.single()?.name)
        assertEquals(null, graph.animationPlan.tracksByLayerIndex[1])
    }

    private fun layer(
        id: String,
        zIndex: Int,
        renderPass: String,
        enabled: Boolean = true
    ) = LayerDefinition(
        id = id,
        type = "TextureLayer",
        enabled = enabled,
        zIndex = zIndex,
        renderPass = renderPass
    )
}
