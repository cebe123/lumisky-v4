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

data class SurfaceFrameSize(
    val width: Int,
    val height: Int
) {
    val isValid: Boolean
        get() = width > 0 && height > 0
}

object SurfaceFrameSizeResolver {
    fun resolve(left: Int, top: Int, right: Int, bottom: Int): SurfaceFrameSize {
        return SurfaceFrameSize(
            width = (right - left).coerceAtLeast(0),
            height = (bottom - top).coerceAtLeast(0)
        )
    }
}
