/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - SensorDispatcher’dan gelen parallax inputunu smoothing ve clamp ile SceneState’e işler.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: SensorDispatcher’dan gelen parallax inputunu smoothing ve clamp ile SceneState’e işler.
 */
package com.example.lumisky.engine

import com.example.lumisky.definition.WallpaperDefinition
class ParallaxController {
    private var currentX = 0.0f
    private var currentY = 0.0f

    fun update(
        targetX: Float,
        targetY: Float,
        definition: WallpaperDefinition?,
        state: SceneState,
        frameState: MutableRenderFrameState
    ) {
        val policy = definition?.parallax
        val enabled = policy?.enabled ?: true
        if (!enabled) {
            frameState.parallaxOffsetX = 0.0f
            frameState.parallaxOffsetY = 0.0f
            return
        }

        val smoothing = policy?.smoothing ?: 0.12f
        val maxX = policy?.maxOffsetX ?: 0.035f
        val maxY = policy?.maxOffsetY ?: 0.018f

        // Apply linear interpolation (smoothing)
        currentX += (targetX - currentX) * smoothing
        currentY += (targetY - currentY) * smoothing

        // Clamp to max bounds
        frameState.parallaxOffsetX = currentX.coerceIn(-maxX, maxX)
        frameState.parallaxOffsetY = currentY.coerceIn(-maxY, maxY)
    }
}
