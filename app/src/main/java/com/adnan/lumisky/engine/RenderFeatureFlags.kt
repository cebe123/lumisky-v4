/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Lumisky v5 Engine katmanı bileşeni.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Lumisky v5 Engine katmanı bileşeni.
 */
package com.adnan.lumisky.engine

data class RenderFeatureFlags(
    val shaderBinaryCacheEnabled: Boolean = false,
    val videoOesEnabled: Boolean = false,
    val postProcessEnabled: Boolean = true,
    val fboCacheEnabled: Boolean = true,
    val astcEnabled: Boolean = false,
    val sensorParallaxEnabled: Boolean = true,
    val bitmapPoolEnabled: Boolean = true,
    val renderTelemetryEnabled: Boolean = true
) {
    companion object {
        fun safeDefaults(): RenderFeatureFlags = RenderFeatureFlags()
    }
}
