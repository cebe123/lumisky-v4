/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Catalog thumbnail decode, target size ve memory cache işlemleri.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Catalog thumbnail decode, target size ve memory cache işlemleri.
 */
package com.adnan.lumisky.assets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThumbnailLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val memoryCache = object : LruCache<String, Bitmap>(AssetCachePolicy.MAX_MEMORY_CACHE_BYTES / 4) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount
        }
    }

    fun load(path: String): Bitmap? {
        val cached = memoryCache.get(path)
        if (cached != null) return cached

        return try {
            val inputStream = context.assets.open(path)
            val options = BitmapFactory.Options().apply {
                inSampleSize = 2 // downsample to save RAM in listings
            }
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            if (bitmap != null) {
                memoryCache.put(path, bitmap)
            }
            bitmap
        } catch (e: Throwable) {
            null
        }
    }
}
