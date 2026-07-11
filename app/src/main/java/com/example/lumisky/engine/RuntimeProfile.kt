/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Lumisky v5 Engine katmanı bileşeni.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Lumisky v5 Engine katmanı bileşeni.
 */
package com.example.lumisky.engine

import com.example.lumisky.definition.QualityTier

enum class RuntimeMode {
    PREVIEW_CARD,
    PREVIEW_FULLSCREEN,
    LIVE_WALLPAPER
}

data class RuntimeProfile(
    val mode: RuntimeMode,
    val maxFps: Int = 30,
    val overrideQualityTier: QualityTier? = null,
    val playVideo: Boolean = true,
    val renderScale: Float = 1.0f
) {
    companion object {
        fun liveWallpaper() = RuntimeProfile(mode = RuntimeMode.LIVE_WALLPAPER)
        fun fullscreenPreview() = RuntimeProfile(mode = RuntimeMode.PREVIEW_FULLSCREEN)
        fun catalogCardPreview() = RuntimeProfile(
            mode = RuntimeMode.PREVIEW_CARD,
            maxFps = 15,
            overrideQualityTier = QualityTier.LOW,
            playVideo = false,
            renderScale = 0.25f
        )
        fun thumbnailPreview() = RuntimeProfile(
            mode = RuntimeMode.PREVIEW_CARD,
            maxFps = 0,
            overrideQualityTier = QualityTier.LOW,
            playVideo = false,
            renderScale = 0.25f
        )
    }
}
