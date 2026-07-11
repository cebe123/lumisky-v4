/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Unlock flash gibi event-based/loop animasyon layer’ı.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Unlock flash gibi event-based/loop animasyon layer’ı.
 */
package com.example.lumisky.layers

import com.example.lumisky.assets.ShaderSourceLoader
import com.example.lumisky.core.WallpaperEvent
import com.example.lumisky.definition.LayerDefinition
import com.example.lumisky.engine.MutableRenderFrameState

class AnimationLayer(
    definition: LayerDefinition,
    shaderSourceLoader: ShaderSourceLoader
) : ShaderLayer(definition, shaderSourceLoader) {
    private var animationTime = 0.0f
    private var isPlaying = false

    override fun onEvent(event: WallpaperEvent) {
        if (event is WallpaperEvent.UserPresent) {
            isPlaying = true
            animationTime = 0.0f
        }
    }

    override fun update(frame: MutableRenderFrameState) {
        super.update(frame)
        if (isPlaying) {
            animationTime += 0.016f
            if (animationTime > 1.0f) {
                isPlaying = false
                animationTime = 0.0f
            }
        }
    }

    override fun render(frame: MutableRenderFrameState) {
        if (isPlaying) {
            super.render(frame)
        }
    }
}
