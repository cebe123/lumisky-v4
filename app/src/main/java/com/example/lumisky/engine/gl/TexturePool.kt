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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class TexturePool(private val manager: GlResourceManager) {
    private val pool = mutableMapOf<String, GlTexture>()
    private val prepared = ConcurrentHashMap<String, Bitmap>()
    private val pending = ConcurrentHashMap<String, Long>()
    private val ioExecutor = Executors.newFixedThreadPool(2)
    @Volatile private var generation = 0L
    private var fallbackTexture: GlTexture? = null

    val hasPendingWork: Boolean
        get() = pending.isNotEmpty() || prepared.isNotEmpty()

    fun preload(paths: Collection<String>, quality: QualityTier = QualityTier.BALANCED) {
        paths.filter(String::isNotBlank).forEach { path ->
            val key = "$path#${quality.name}"
            val requestGeneration = generation
            if (pool.containsKey(key) || prepared.containsKey(key) ||
                pending.putIfAbsent(key, requestGeneration) != null
            ) return@forEach
            ioExecutor.execute {
                var bitmap: Bitmap? = null
                try {
                    bitmap = loadBitmap(path, quality)
                    if (requestGeneration == generation) {
                        prepared[key] = bitmap
                        bitmap = null
                    }
                } finally {
                    bitmap?.recycle()
                    pending.remove(key, requestGeneration)
                }
            }
        }
    }

    fun get(path: String, quality: QualityTier = QualityTier.BALANCED): GlTexture {
        val key = "$path#${quality.name}"
        pool[key]?.let { return it }
        val bitmap = prepared.remove(key)
        if (bitmap == null) {
            preload(listOf(path), quality)
            return fallbackTexture()
        }
        return GlTexture(uploadTexture(bitmap)).also {
            bitmap.recycle()
            pool[key] = it
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
        generation++
        pool.values.forEach { it.release() }
        pool.clear()
        fallbackTexture?.release()
        fallbackTexture = null
        prepared.values.forEach { it.recycle() }
        prepared.clear()
        pending.clear()
    }

    fun release() {
        clear()
        ioExecutor.shutdownNow()
    }

    private fun fallbackTexture(): GlTexture {
        return fallbackTexture ?: run {
            val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
            GlTexture(uploadTexture(bitmap)).also {
                bitmap.recycle()
                fallbackTexture = it
            }
        }
    }
}
