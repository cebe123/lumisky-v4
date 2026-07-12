/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Lumisky v5 Data katmanı bileşeni.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Lumisky v5 Data katmanı bileşeni.
 */
package com.example.lumisky.data

import com.example.lumisky.definition.QualityTier
import com.example.lumisky.engine.RenderFeatureFlags

data class RuntimeSettingsPolicyResult(
    val qualityTier: QualityTier,
    val maxFps: Int,
    val renderScale: Float,
    val postProcessEnabled: Boolean,
    val particleEffectsEnabled: Boolean,
    val videoPlaybackEnabled: Boolean,
    val sensorParallaxEnabled: Boolean,
    val telemetryEnabled: Boolean,
    val thermalEmergency: Boolean
)

object RuntimeSettingsPolicy {
    const val THERMAL_STATUS_SEVERE = 3

    fun resolve(
        qualityTier: String,
        performanceMode: String,
        highRefreshEnabled: Boolean,
        batterySaver: Boolean = false,
        thermalStatus: Int = 0,
        ambientMode: Boolean = false,
        sceneMaxFps: Int = 30,
        batterySaverSceneMaxFps: Int = 15,
        featureFlags: RenderFeatureFlags = RenderFeatureFlags.safeDefaults()
    ): RuntimeSettingsPolicyResult {
        val thermalEmergency = thermalStatus >= THERMAL_STATUS_SEVERE
        val baseMaxFps = if (highRefreshEnabled) sceneMaxFps else sceneMaxFps.coerceAtMost(60)

        return when {
            ambientMode -> RuntimeSettingsPolicyResult(
                qualityTier = QualityTier.LOW,
                maxFps = 0,
                renderScale = 0.25f,
                postProcessEnabled = false,
                particleEffectsEnabled = false,
                videoPlaybackEnabled = false,
                sensorParallaxEnabled = false,
                telemetryEnabled = featureFlags.renderTelemetryEnabled,
                thermalEmergency = false
            )
            thermalEmergency -> RuntimeSettingsPolicyResult(
                qualityTier = QualityTier.LOW,
                maxFps = batterySaverSceneMaxFps,
                renderScale = 0.35f,
                postProcessEnabled = false,
                particleEffectsEnabled = false,
                videoPlaybackEnabled = false,
                sensorParallaxEnabled = featureFlags.sensorParallaxEnabled,
                telemetryEnabled = featureFlags.renderTelemetryEnabled,
                thermalEmergency = true
            )
            batterySaver || performanceMode == SettingsRepository.PERFORMANCE_MODE_BATTERY -> RuntimeSettingsPolicyResult(
                qualityTier = QualityTier.LOW,
                maxFps = batterySaverSceneMaxFps,
                renderScale = 0.5f,
                postProcessEnabled = false,
                particleEffectsEnabled = false,
                videoPlaybackEnabled = false,
                sensorParallaxEnabled = featureFlags.sensorParallaxEnabled,
                telemetryEnabled = featureFlags.renderTelemetryEnabled,
                thermalEmergency = false
            )
            performanceMode == SettingsRepository.PERFORMANCE_MODE_SMOOTH -> RuntimeSettingsPolicyResult(
                qualityTier = QualityTier.HIGH,
                maxFps = baseMaxFps,
                renderScale = 1.0f,
                postProcessEnabled = featureFlags.postProcessEnabled,
                particleEffectsEnabled = true,
                videoPlaybackEnabled = featureFlags.videoOesEnabled,
                sensorParallaxEnabled = featureFlags.sensorParallaxEnabled,
                telemetryEnabled = featureFlags.renderTelemetryEnabled,
                thermalEmergency = false
            )
            else -> RuntimeSettingsPolicyResult(
                qualityTier = parseQualityTier(qualityTier),
                maxFps = baseMaxFps,
                renderScale = 1.0f,
                postProcessEnabled = featureFlags.postProcessEnabled,
                particleEffectsEnabled = true,
                videoPlaybackEnabled = featureFlags.videoOesEnabled,
                sensorParallaxEnabled = featureFlags.sensorParallaxEnabled,
                telemetryEnabled = featureFlags.renderTelemetryEnabled,
                thermalEmergency = false
            )
        }
    }

    private fun parseQualityTier(value: String): QualityTier {
        return try {
            QualityTier.valueOf(value)
        } catch (e: Throwable) {
            QualityTier.BALANCED
        }
    }
}
