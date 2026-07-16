/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - CompiledSceneDefinition ve LayerRegistry ile RuntimeScene üretir.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: CompiledSceneDefinition ve LayerRegistry ile RuntimeScene üretir.
 */
package com.example.lumisky.registry

import com.example.lumisky.definition.LayerParallaxDefinition
import com.example.lumisky.definition.WallpaperDefinition
import com.example.lumisky.engine.CompiledLayerGraph
import com.example.lumisky.engine.LayerParallaxDepthResolver
import com.example.lumisky.engine.RuntimeScene
import com.example.lumisky.layers.RenderLayer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SceneFactory @Inject constructor(
    private val layerRegistry: LayerRegistry
) {
    fun create(
        definition: WallpaperDefinition,
        layerGraph: CompiledLayerGraph = CompiledLayerGraph.compile(definition)
    ): RuntimeScene {
        val layers = mutableListOf<RenderLayer>()
        val parallaxEnabled = definition.parallax?.enabled == true
        val layerCount = layerGraph.layersByIndex.size

        layerGraph.layersByIndex.forEachIndexed { layerIndex, compiledLayer ->
            val sourceDefinition = compiledLayer.definition
            val resolvedDefinition = if (parallaxEnabled) {
                val depth = LayerParallaxDepthResolver.resolveDepth(
                    layer = sourceDefinition,
                    layerIndex = layerIndex,
                    layerCount = layerCount,
                    parallaxEnabled = true
                )
                sourceDefinition.copy(
                    parallax = (sourceDefinition.parallax ?: LayerParallaxDefinition()).copy(depth = depth)
                )
            } else {
                sourceDefinition
            }
            when (val result = layerRegistry.create(resolvedDefinition, required = false)) {
                is LayerCreateResult.Created -> {
                    layers.add(result.layer)
                }
                is LayerCreateResult.UnknownType -> {
                    // Fallback handling or log telemetry
                }
                is LayerCreateResult.CreateFailed -> {
                    // Fallback handling or log telemetry
                }
            }
        }

        return RuntimeScene(definition.id, layers)
    }
}
