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
    private val durationSeconds = (definition.animation?.durationMs ?: DEFAULT_DURATION_MS).coerceAtLeast(1L) / 1_000f
    private val loop = definition.animation?.loop ?: false
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
            animationTime += frame.deltaTimeSeconds.coerceAtLeast(0f)
            if (animationTime >= durationSeconds) {
                if (loop) {
                    animationTime %= durationSeconds
                } else {
                    isPlaying = false
                    animationTime = 0.0f
                }
            }
        }
    }

    override fun render(frame: MutableRenderFrameState) {
        if (isPlaying) {
            super.render(frame)
        }
    }

    private companion object {
        const val DEFAULT_DURATION_MS = 1_000L
    }
}
