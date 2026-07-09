/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Main thread’den GL thread’e aktarılan immutable input snapshot modeli.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Main thread’den GL thread’e aktarılan immutable input snapshot modeli.
 */
package com.adnan.lumisky.engine

import com.adnan.lumisky.definition.QualityTier

data class DaylightOverride(
    val sunriseMinute: Int,
    val sunsetMinute: Int,
    val solarNoonMinute: Int,
    val timeZoneId: String = ""
)

class SceneInputSnapshot(
    val isVisible: Boolean,
    val batterySaver: Boolean,
    val parallaxX: Float,
    val parallaxY: Float,
    val touchX: Float,
    val touchY: Float,
    val hasTouch: Boolean,
    val preferredQualityTier: QualityTier? = null,
    val daylightOverride: DaylightOverride? = null,
    val renderScale: Float = 1.0f,
    val postProcessEnabled: Boolean = true,
    val particleEffectsEnabled: Boolean = true,
    val videoPlaybackEnabled: Boolean = true,
    val sensorParallaxEnabled: Boolean = true,
    val telemetryEnabled: Boolean = true,
    val thermalEmergency: Boolean = false
)
