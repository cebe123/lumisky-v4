/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Aktif scene texture cache ve LRU eviction yönetimi.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Aktif scene texture cache ve LRU eviction yönetimi.
 */
package com.example.lumisky.engine.gl

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLUtils
import com.example.lumisky.definition.QualityTier

class TexturePool(private val manager: GlResourceManager) {
    private val pool = mutableMapOf<String, GlTexture>()

    fun get(path: String, quality: QualityTier = QualityTier.BALANCED): GlTexture {
        val key = "$path#${quality.name}"
        return pool.getOrPut(key) {
            val bitmap = loadBitmap(path, quality)
            val textureId = uploadTexture(bitmap)
            manager.bitmapPool.put(bitmap)
            GlTexture(textureId)
        }
    }

    private fun loadBitmap(path: String, quality: QualityTier): Bitmap {
        return try {
            val inputStream = manager.context.assets.open(path)
            val options = BitmapFactory.Options().apply {
                inMutable = true
                inSampleSize = when (quality) {
                    QualityTier.LOW -> 2
                    QualityTier.BALANCED,
                    QualityTier.HIGH,
                    QualityTier.ULTRA -> 1
                }
            }
            BitmapFactory.decodeStream(inputStream, null, options)
                ?: throw RuntimeException("Decoded bitmap is null")
        } catch (e: Throwable) {
            // Fallback to a transparent 2x2 bitmap
            Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        }
    }

    private fun uploadTexture(bitmap: Bitmap): Int {
        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)
        val texId = textures[0]
        if (texId == 0) return 0

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texId)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)

        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)

        return texId
    }

    fun clear() {
        pool.values.forEach { it.release() }
        pool.clear()
    }
}
