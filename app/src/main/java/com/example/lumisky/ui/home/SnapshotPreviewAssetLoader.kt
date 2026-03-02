package com.example.lumisky.ui.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import com.example.core.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException
import java.util.Locale
import kotlin.math.max

class SnapshotPreviewAssetLoader(context: Context) {
	private val assetManager = context.applicationContext.assets
	private val missingAssetPaths = HashSet<String>()
	private val failedAssetPaths = HashSet<String>()
	private val bitmapCache = object : LruCache<String, Bitmap>(MAX_CACHE_BYTES) {
		override fun sizeOf(
			key: String,
			value: Bitmap
		): Int = value.allocationByteCount
	}

	fun cachedBitmap(
		configId: String,
		targetWidthPx: Int,
		targetHeightPx: Int
	): Bitmap? {
		if (targetWidthPx <= 0 || targetHeightPx <= 0) return null
		val cacheKey = cacheKey(configId, targetWidthPx, targetHeightPx)
		return synchronized(bitmapCache) { bitmapCache.get(cacheKey) }
	}

	suspend fun loadBitmap(
		configId: String,
		targetWidthPx: Int,
		targetHeightPx: Int
	): Bitmap? = withContext(Dispatchers.IO) {
		loadBitmapInternal(configId, targetWidthPx, targetHeightPx)
	}

	private fun loadBitmapInternal(
		configId: String,
		targetWidthPx: Int,
		targetHeightPx: Int
	): Bitmap? {
		if (targetWidthPx <= 0 || targetHeightPx <= 0) return null
		val cacheKey = cacheKey(configId, targetWidthPx, targetHeightPx)
		synchronized(bitmapCache) {
			bitmapCache.get(cacheKey)
		}?.let { cached ->
			return cached
		}

		val assetPath = assetPathFor(configId)
		val bounds = BitmapFactory.Options().apply {
			inJustDecodeBounds = true
		}

		try {
			assetManager.open(assetPath).use { input ->
				BitmapFactory.decodeStream(input, null, bounds)
			}
		} catch (_: FileNotFoundException) {
			logMissingAssetOnce(assetPath)
			return null
		} catch (error: IOException) {
			logDecodeFailureOnce(assetPath, "bounds_decode", error)
			return null
		}

		if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
			logDecodeFailureOnce(assetPath, "invalid_bounds", null)
			return null
		}

		val bitmap = try {
			assetManager.open(assetPath).use { input ->
				BitmapFactory.decodeStream(
					input,
					null,
					BitmapFactory.Options().apply {
						inPreferredConfig = Bitmap.Config.ARGB_8888
						inDither = true
						inSampleSize = calculateInSampleSize(
							sourceWidth = bounds.outWidth,
							sourceHeight = bounds.outHeight,
							targetWidthPx = targetWidthPx,
							targetHeightPx = targetHeightPx
						)
					}
				)
			}
		} catch (_: FileNotFoundException) {
			logMissingAssetOnce(assetPath)
			null
		} catch (error: IOException) {
			logDecodeFailureOnce(assetPath, "bitmap_decode", error)
			null
		}

		if (bitmap == null) {
			logDecodeFailureOnce(assetPath, "bitmap_decode_null", null)
			return null
		}

		synchronized(bitmapCache) {
			bitmapCache.put(cacheKey, bitmap)
		}
		return bitmap
	}

	private fun assetPathFor(configId: String): String {
		return "$ASSET_DIRECTORY/${normalizeConfigId(configId)}.webp"
	}

	private fun cacheKey(
		configId: String,
		targetWidthPx: Int,
		targetHeightPx: Int
	): String {
		val normalizedId = normalizeConfigId(configId)
		return "${normalizedId}_${max(targetWidthPx, 1)}x${max(targetHeightPx, 1)}"
	}

	private fun normalizeConfigId(configId: String): String {
		val normalized = configId
			.trim()
			.lowercase(Locale.US)
			.replace(Regex("[^a-z0-9_-]+"), "_")
			.trim('_')
		return normalized.ifBlank { "wallpaper" }
	}

	private fun calculateInSampleSize(
		sourceWidth: Int,
		sourceHeight: Int,
		targetWidthPx: Int,
		targetHeightPx: Int
	): Int {
		var sampleSize = 1
		if (sourceWidth <= targetWidthPx && sourceHeight <= targetHeightPx) {
			return sampleSize
		}

		val nextWidth = sourceWidth / 2
		val nextHeight = sourceHeight / 2
		while (
			nextWidth / sampleSize >= targetWidthPx &&
			nextHeight / sampleSize >= targetHeightPx
		) {
			sampleSize *= 2
		}
		return sampleSize.coerceAtLeast(1)
	}

	private fun logMissingAssetOnce(assetPath: String) {
		val shouldLog = synchronized(missingAssetPaths) { missingAssetPaths.add(assetPath) }
		if (shouldLog) {
			Logger.d(TAG, "snapshot asset missing path=$assetPath")
		}
	}

	private fun logDecodeFailureOnce(
		assetPath: String,
		reason: String,
		error: Throwable?
	) {
		val shouldLog = synchronized(failedAssetPaths) { failedAssetPaths.add(assetPath) }
		if (shouldLog) {
			Logger.w(TAG, "snapshot asset decode failed path=$assetPath reason=$reason", error)
		}
	}

	companion object {
		private const val TAG = "SnapshotPreviewAssetLoader"
		private const val ASSET_DIRECTORY = "previews/zenith"
		private const val MAX_CACHE_BYTES = 24 * 1024 * 1024
	}
}
