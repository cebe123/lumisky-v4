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
    @Test
    fun passesAutomaticAndExplicitDepthsToLayerFactories() {
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
                    LayerDefinition(
                        id = "front",
                        type = "TextureLayer",
                        zIndex = 2,
                        parallax = LayerParallaxDefinition(depth = 0.8f)
                    )
                )
            )
        )

        assertEquals(listOf(0.05f, 0.525f, 0.8f), scene.layers.map { it.parallaxDepth })
    }
}
