/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Riskli renderer/layer özellikleri için kill switch ve feature flag kaynağı.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Riskli renderer/layer özellikleri için kill switch ve feature flag kaynağı.
 */
package com.adnan.lumisky.telemetry

import com.adnan.lumisky.engine.RenderFeatureFlags
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeatureFlagRepository @Inject constructor() {
    fun flags(): Flow<RenderFeatureFlags> {
        return flowOf(RenderFeatureFlags.safeDefaults())
    }

    fun isFeatureEnabled(name: String): Flow<Boolean> {
        val flags = RenderFeatureFlags.safeDefaults()
        return flowOf(
            when (name) {
                "shaderBinaryCache" -> flags.shaderBinaryCacheEnabled
                "videoOes" -> flags.videoOesEnabled
                "postProcess" -> flags.postProcessEnabled
                "fboCache" -> flags.fboCacheEnabled
                "astc" -> flags.astcEnabled
                "sensorParallax" -> flags.sensorParallaxEnabled
                "bitmapPool" -> flags.bitmapPoolEnabled
                "renderTelemetry" -> flags.renderTelemetryEnabled
                else -> false
            }
        )
    }
}
