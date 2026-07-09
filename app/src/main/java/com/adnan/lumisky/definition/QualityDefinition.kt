/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - QualityTier ve degradation policy tanımları.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: QualityTier ve degradation policy tanımları.
 */
package com.adnan.lumisky.definition

import kotlinx.serialization.Serializable

enum class QualityTier {
    LOW,
    BALANCED,
    HIGH,
    ULTRA
}

@Serializable
data class QualityProfile(
    val tier: QualityTier,
    val maxTextureSize: Int,
    val particleMultiplier: Float,
    val enablePostProcess: Boolean,
    val fboResolutionScale: Float
)
