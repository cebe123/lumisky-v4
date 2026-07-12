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

import com.example.lumisky.definition.WallpaperDefinition
import com.example.lumisky.engine.CompiledLayerGraph
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
        
        layerGraph.layersByIndex.forEach { compiledLayer ->
            when (val result = layerRegistry.create(compiledLayer.definition, required = false)) {
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
