/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Liste/kart preview’da thumbnail-only veya tek seçili low-cost preview kararı.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Liste/kart preview’da thumbnail-only veya tek seçili low-cost preview kararı.
 */
package com.example.lumisky.preview

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardPreviewController @Inject constructor(
    private val surfaceController: PreviewSurfaceController
) {
    fun shouldRenderLive(): Boolean {
        return surfaceController.hasActiveSurface()
    }
}
