/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Foldable/tablet/rotation surface değişimlerinde projection/parallax clamp hesaplar.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Foldable/tablet/rotation surface değişimlerinde projection/parallax clamp hesaplar.
 */
package com.example.lumisky.device

import com.example.lumisky.definition.WallpaperDefinition
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DisplayMetricsController @Inject constructor() {
    fun calculateParallaxBounds(width: Int, height: Int, definition: WallpaperDefinition?): ParallaxBounds {
        val policy = definition?.displayPolicy
        val isFoldable = policy?.foldableAware ?: true
        val aspect = width.toFloat() / height.toFloat()
        
        return if (aspect > 0.7f && isFoldable) {
            ParallaxBounds(
                policy?.maxOffsetXExpanded ?: 0.022f,
                policy?.maxOffsetY ?: 0.018f,
                policy?.fovExpanded ?: 38.0f
            )
        } else {
            ParallaxBounds(
                policy?.maxOffsetXPhone ?: 0.035f,
                policy?.maxOffsetY ?: 0.018f,
                policy?.fovPhone ?: 45.0f
            )
        }
    }
}

data class ParallaxBounds(
    val maxX: Float,
    val maxY: Float,
    val fov: Float
)
