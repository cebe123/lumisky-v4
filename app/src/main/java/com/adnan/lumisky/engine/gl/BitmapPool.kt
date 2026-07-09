/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - WebP/PNG/JPG decode için inBitmap reuse havuzu.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: WebP/PNG/JPG decode için inBitmap reuse havuzu.
 */
package com.adnan.lumisky.engine.gl

import android.graphics.Bitmap
import java.util.Collections

class BitmapPool {
    private val pool = Collections.synchronizedList(mutableListOf<Bitmap>())

    fun get(width: Int, height: Int, config: Bitmap.Config): Bitmap {
        synchronized(pool) {
            val iterator = pool.iterator()
            while (iterator.hasNext()) {
                val bitmap = iterator.next()
                if (bitmap.width == width && bitmap.height == height && bitmap.config == config && bitmap.isMutable) {
                    iterator.remove()
                    return bitmap
                }
            }
        }
        return Bitmap.createBitmap(width, height, config)
    }

    fun put(bitmap: Bitmap) {
        if (!bitmap.isRecycled && bitmap.isMutable) {
            pool.add(bitmap)
        }
    }

    fun clear() {
        synchronized(pool) {
            pool.forEach { it.recycle() }
            pool.clear()
        }
    }
}
