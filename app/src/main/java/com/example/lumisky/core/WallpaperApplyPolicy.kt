/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Lumisky v5 Core katmanı bileşeni.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Lumisky v5 Core katmanı bileşeni.
 */
package com.example.lumisky.core

enum class WallpaperApplyAction {
    SKIP,
    SWITCH_SCENE,
    CREATE_SURFACE,
    STORE_PENDING_SCENE
}

object WallpaperApplyPolicy {
    fun resolve(
        hasEngineSurface: Boolean,
        hasGlSurface: Boolean,
        sameWallpaperAlreadyApplied: Boolean
    ): WallpaperApplyAction {
        return when {
            sameWallpaperAlreadyApplied -> WallpaperApplyAction.SKIP
            !hasEngineSurface -> WallpaperApplyAction.STORE_PENDING_SCENE
            else -> WallpaperApplyAction.SWITCH_SCENE
        }
    }
}
