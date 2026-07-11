/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - QualityTier ve layer maliyetine göre FBO çözünürlüğü seçer.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: QualityTier ve layer maliyetine göre FBO çözünürlüğü seçer.
 */
package com.example.lumisky.engine.pipeline

import com.example.lumisky.definition.QualityTier

object FboResolutionPolicy {
    fun getResolutionScale(tier: QualityTier, layerType: String): Float {
        return when (tier) {
            QualityTier.LOW -> 0.25f
            QualityTier.BALANCED -> 0.5f
            QualityTier.HIGH -> 0.75f
            QualityTier.ULTRA -> 1.0f
        }
    }
}
