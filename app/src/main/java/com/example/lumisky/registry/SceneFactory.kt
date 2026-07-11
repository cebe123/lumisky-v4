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
import com.example.lumisky.engine.RuntimeScene
import com.example.lumisky.layers.RenderLayer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SceneFactory @Inject constructor(
    private val layerRegistry: LayerRegistry
) {
    fun create(definition: WallpaperDefinition): RuntimeScene {
        val layers = mutableListOf<RenderLayer>()
        
        definition.layers.forEach { layerDef ->
            if (layerDef.enabled) {
                when (val result = layerRegistry.create(layerDef, required = false)) {
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
        }
        
        // Sort layers by zIndex for rendering order
        layers.sortBy { it.zIndex }
        
        return RuntimeScene(definition.id, layers)
    }
}
