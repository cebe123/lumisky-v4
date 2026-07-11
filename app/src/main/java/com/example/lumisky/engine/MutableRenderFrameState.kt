/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Her frame yeniden allocate edilmeden güncellenen preallocated frame state.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Her frame yeniden allocate edilmeden güncellenen preallocated frame state.
 */
package com.example.lumisky.engine

import com.example.lumisky.definition.QualityTier
import com.example.lumisky.engine.gl.GlResourceManager

data class MutableRenderFrameState(
    var width: Int = 0,
    var height: Int = 0,
    var timeSeconds: Float = 0.0f,
    var deltaTimeSeconds: Float = 0.0f,
    var parallaxOffsetX: Float = 0.0f,
    var parallaxOffsetY: Float = 0.0f,
    var quality: QualityTier = QualityTier.BALANCED,
    var dayProgress: Float = 0.0f,
    var sunX: Float = 0.5f,
    var sunY: Float = 1.2f,
    var moonX: Float = 0.5f,
    var moonY: Float = 1.2f,
    var drawSun: Boolean = false,
    var isNight: Boolean = false,
    var minute: Float = 0.0f,
    var sunriseMinute: Int = 360,
    var sunsetMinute: Int = 1080,
    var solarNoonMinute: Int = 720,
    var nightAmount: Float = 0.0f,
    var horizonY: Float = 0.8f,
    var sunColorR: Float = 1.0f,
    var sunColorG: Float = 0.9f,
    var sunColorB: Float = 0.8f,
    var renderScale: Float = 1.0f,
    var postProcessEnabled: Boolean = true,
    var particleEffectsEnabled: Boolean = true,
    var videoPlaybackEnabled: Boolean = true,
    var sensorParallaxEnabled: Boolean = true,
    var telemetryEnabled: Boolean = true,
    var thermalEmergency: Boolean = false
) {
    private var isGlInitialized = false
    private lateinit var _gl: GlResourceManager
    
    var gl: GlResourceManager
        get() = _gl
        set(value) {
            _gl = value
            isGlInitialized = true
        }

    fun isGlInitialized(): Boolean = isGlInitialized
}
