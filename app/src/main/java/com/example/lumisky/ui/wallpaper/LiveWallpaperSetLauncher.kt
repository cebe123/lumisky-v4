/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Lumisky v5 Ui katmanı bileşeni.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Lumisky v5 Ui katmanı bileşeni.
 */
package com.example.lumisky.ui.wallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.lumisky.core.LumiskyWallpaperService

object LiveWallpaperSetLauncher {
    fun open(context: Context): Boolean {
        return try {
            context.startActivity(changeLiveWallpaperIntent(context))
            true
        } catch (primary: Exception) {
            try {
                context.startActivity(liveWallpaperChooserIntent())
                true
            } catch (fallback: Exception) {
                false
            }
        }
    }

    private fun changeLiveWallpaperIntent(context: Context): Intent {
        return Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(context, LumiskyWallpaperService::class.java)
            )
        }
    }

    private fun liveWallpaperChooserIntent(): Intent {
        return Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
    }
}
