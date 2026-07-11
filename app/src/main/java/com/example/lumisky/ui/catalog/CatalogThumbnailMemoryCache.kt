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

import android.graphics.Bitmap
import android.util.LruCache

object CatalogThumbnailMemoryCache {
    private val cache = object : LruCache<String, Bitmap>(24 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    fun get(key: String): Bitmap? = synchronized(cache) {
        cache.get(key)
    }

    fun put(key: String, bitmap: Bitmap) {
        synchronized(cache) {
            cache.put(key, bitmap)
        }
    }

    fun key(path: String, targetWidthPx: Int, targetHeightPx: Int): String {
        return "$path@$targetWidthPx:$targetHeightPx"
    }
}
