/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - App preview surface lifecycle ve tek aktif GL preview kuralı.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: App preview surface lifecycle ve tek aktif GL preview kuralı.
 */
package com.example.lumisky.preview

import android.view.SurfaceHolder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreviewSurfaceController @Inject constructor() {
    private var activeHolder: SurfaceHolder? = null

    @Synchronized
    fun claimSurface(holder: SurfaceHolder): Boolean {
        if (activeHolder != null && activeHolder != holder) return false
        activeHolder = holder
        return true
    }

    @Synchronized
    fun releaseSurface(holder: SurfaceHolder) {
        if (activeHolder == holder) {
            activeHolder = null
        }
    }

    @Synchronized
    fun hasActiveSurface(): Boolean {
        return activeHolder != null
    }
}
