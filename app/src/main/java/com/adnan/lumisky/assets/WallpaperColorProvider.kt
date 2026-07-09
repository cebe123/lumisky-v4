/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Material You renklerini metadata/thumbnail üzerinden cache’li üretir.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Material You renklerini metadata/thumbnail üzerinden cache’li üretir.
 */
package com.adnan.lumisky.assets

import android.annotation.TargetApi
import android.app.WallpaperColors
import android.graphics.Color
import android.os.Build
import com.adnan.lumisky.definition.WallpaperDefinition
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@TargetApi(Build.VERSION_CODES.O_MR1)
class WallpaperColorProvider @Inject constructor(
    private val thumbnailLoader: ThumbnailLoader
) {
    private val colorsCache = mutableMapOf<String, WallpaperColors>()

    fun getColors(definition: WallpaperDefinition): WallpaperColors? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return null
        
        val cached = colorsCache[definition.id]
        if (cached != null) return cached

        val hints = definition.colorHints
        if (hints?.primary != null) {
            try {
                val primaryColor = Color.parseColor(hints.primary)
                val secondaryColor = hints.secondary?.let { Color.parseColor(it) }
                val tertiaryColor = hints.tertiary?.let { Color.parseColor(it) }
                
                val colors = if (secondaryColor != null && tertiaryColor != null) {
                    WallpaperColors(Color.valueOf(primaryColor), Color.valueOf(secondaryColor), Color.valueOf(tertiaryColor))
                } else if (secondaryColor != null) {
                    WallpaperColors(Color.valueOf(primaryColor), Color.valueOf(secondaryColor), null)
                } else {
                    WallpaperColors(Color.valueOf(primaryColor), null, null)
                }
                
                colorsCache[definition.id] = colors
                return colors
            } catch (e: Throwable) {
                // Fallback to thumbnail analysis.
            }
        }

        val thumbnailBitmap = thumbnailLoader.load(definition.preview.thumbnail)
        if (thumbnailBitmap != null) {
            val colors = WallpaperColors.fromBitmap(thumbnailBitmap)
            colorsCache[definition.id] = colors
            return colors
        }

        val defaultColors = WallpaperColors(Color.valueOf(Color.BLACK), null, null)
        colorsCache[definition.id] = defaultColors
        return defaultColors
    }
}
