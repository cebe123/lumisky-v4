/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Main thread’den GL thread’e aktarılan immutable input snapshot modeli.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Main thread’den GL thread’e aktarılan immutable input snapshot modeli.
 */
package com.example.lumisky.engine

import com.example.lumisky.definition.QualityTier

enum class DaylightMode { NORMAL, POLAR_DAY, POLAR_NIGHT }

data class DaylightOverride(
    val sunriseMinute: Int,
    val sunsetMinute: Int,
    val solarNoonMinute: Int,
    val timeZoneId: String = "",
    val mode: DaylightMode = DaylightMode.NORMAL
)

class SceneInputSnapshot(
    var isVisible: Boolean,
    var batterySaver: Boolean,
    var parallaxX: Float,
    var parallaxY: Float,
    var touchX: Float,
    var touchY: Float,
    var hasTouch: Boolean,
    var preferredQualityTier: QualityTier? = null,
    var daylightOverride: DaylightOverride? = null,
    var renderScale: Float = 1.0f,
    var postProcessEnabled: Boolean = true,
    var particleEffectsEnabled: Boolean = true,
    var videoPlaybackEnabled: Boolean = true,
    var sensorParallaxEnabled: Boolean = true,
    var telemetryEnabled: Boolean = true,
    var thermalEmergency: Boolean = false
)
