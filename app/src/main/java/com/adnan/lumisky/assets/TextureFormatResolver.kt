/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - ASTC/ETC2/WebP fallback kararlarını cihaz desteğine göre verir.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: ASTC/ETC2/WebP fallback kararlarını cihaz desteğine göre verir.
 */
package com.adnan.lumisky.assets

object TextureFormatResolver {
    var isAstcSupported: Boolean = false
        private set
    var isEtc2Supported: Boolean = true // Standard in OpenGL ES 3.0
        private set

    fun initialize(extensions: String) {
        isAstcSupported = extensions.contains("GL_KHR_texture_compression_astc_ldr") ||
                extensions.contains("GL_OES_texture_compression_astc")
    }

    fun selectBestFormat(preferredAstcPath: String, fallbackPath: String): String {
        return if (isAstcSupported) preferredAstcPath else fallbackPath
    }
}
