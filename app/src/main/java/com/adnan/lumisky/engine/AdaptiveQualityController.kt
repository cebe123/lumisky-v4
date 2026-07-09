/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Battery saver, thermal, frame pacing ve cihaz profiline göre kalite kararı verir.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Battery saver, thermal, frame pacing ve cihaz profiline göre kalite kararı verir.
 */
package com.adnan.lumisky.engine

import com.adnan.lumisky.definition.QualityTier
import com.adnan.lumisky.definition.WallpaperDefinition
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdaptiveQualityController @Inject constructor() {
    fun resolveTier(
        definition: WallpaperDefinition?,
        batterySaver: Boolean,
        thermalThrottle: Boolean
    ): QualityTier {
        val policy = definition?.qualityPolicy
        val allowAdaptive = policy?.allowAdaptiveQuality ?: true
        val defaultTier = try { QualityTier.valueOf(policy?.defaultTier ?: "BALANCED") } catch (e: Throwable) { QualityTier.BALANCED }

        if (!allowAdaptive) return defaultTier

        return when {
            batterySaver -> QualityTier.LOW
            thermalThrottle -> QualityTier.LOW
            else -> defaultTier
        }
    }
}
