/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Lumisky v5 Ui katmanı bileşeni.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Lumisky v5 Ui katmanı bileşeni.
 */
package com.example.lumisky.ui.catalog

object CatalogThumbnailDecodePolicy {
    fun calculateInSampleSize(
        sourceWidth: Int,
        sourceHeight: Int,
        targetWidth: Int,
        targetHeight: Int
    ): Int {
        if (sourceWidth <= 0 || sourceHeight <= 0 || targetWidth <= 0 || targetHeight <= 0) {
            return 1
        }
        val boundedTargetWidth = targetWidth.coerceAtMost(MAX_DECODE_WIDTH_PX)
        val boundedTargetHeight = targetHeight.coerceAtMost(MAX_DECODE_HEIGHT_PX)
        var sampleSize = 1
        while (
            sourceWidth / (sampleSize * 2) >= boundedTargetWidth &&
            sourceHeight / (sampleSize * 2) >= boundedTargetHeight
        ) {
            sampleSize *= 2
        }
        return sampleSize.coerceAtLeast(2)
    }

    private const val MAX_DECODE_WIDTH_PX = 540
    private const val MAX_DECODE_HEIGHT_PX = 960
}
