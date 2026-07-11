/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Fullscreen preview runtime scene, simulated time ve quality policy yönetimi.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Fullscreen preview runtime scene, simulated time ve quality policy yönetimi.
 */
package com.example.lumisky.preview

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FullscreenPreviewController @Inject constructor() {
    private var simulatedTimeSeconds = 0.0f

    fun updateSimulatedTime(deltaTime: Float): Float {
        simulatedTimeSeconds += deltaTime * 5.0f // Accelerate preview time by 5x
        return simulatedTimeSeconds
    }

    fun reset() {
        simulatedTimeSeconds = 0.0f
    }
}
