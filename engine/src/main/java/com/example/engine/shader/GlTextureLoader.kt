package com.example.engine.shader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import kotlin.math.roundToInt

internal class GlTextureLoader(
	private val preferredTextureResolver: PreferredTextureResolver
) {

	fun loadTexture(
		path: String?,
		textureBytesLoader: ((String) -> ByteArray?)?,
		qualityScale: Float
	): Int {
		val normalized = path?.takeIf { it.isNotBlank() } ?: return 0
		val loader = textureBytesLoader ?: return 0
		val resolvedTexture = preferredTextureResolver.resolve(normalized, loader) ?: return 0
		val resolvedPath = resolvedTexture.path
		val bytes = resolvedTexture.bytes

		val decodedBitmap = BitmapFactory.decodeByteArray(
			bytes,
			0,
			bytes.size,
			BitmapFactory.Options().apply {
				inPreferredConfig = Bitmap.Config.ARGB_8888
				inDither = true
				inSampleSize = resolveTextureDecodeSampleSize(resolvedPath, qualityScale)
			}
		) ?: return 0
		val bitmap = preprocessTextureBitmap(
			assetPath = resolvedPath,
			bitmap = decodedBitmap
		)
		return try {
			val handles = IntArray(1)
			GLES20.glGenTextures(1, handles, 0)
			val handle = handles[0]
			if (handle == 0) return 0

			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, handle)
			val isPixelArt = isPixelArtTexture(resolvedPath)
			val isPot = isPowerOfTwo(bitmap.width) && isPowerOfTwo(bitmap.height)
			val minFilter = if (isPixelArt) {
				GLES20.GL_NEAREST
			} else if (isPot) {
				GLES20.GL_LINEAR_MIPMAP_LINEAR
			} else {
				GLES20.GL_LINEAR
			}
			val magFilter = if (isPixelArt) GLES20.GL_NEAREST else GLES20.GL_LINEAR
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, minFilter)
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, magFilter)
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
			if (!isPixelArt && isPot) {
				GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
			}
			handle
		} finally {
			if (bitmap !== decodedBitmap) {
				bitmap.recycle()
			}
			decodedBitmap.recycle()
		}
	}

	private fun preprocessTextureBitmap(
		assetPath: String,
		bitmap: Bitmap
	): Bitmap {
		var processed = bitmap
		if (shouldTrimTransparentTop(assetPath)) {
			val trimmed = trimTransparentTop(
				bitmap = processed,
				minAlpha = ALPHA_TRIM_MIN_VISIBLE_ALPHA
			)
			if (trimmed != null) {
				processed = trimmed
			}
		}
		if (shouldBleedTransparentEdgeColors(assetPath)) {
			val bled = bleedTransparentEdgeColors(
				bitmap = processed,
				minOpaqueAlpha = ALPHA_BLEED_MIN_OPAQUE_ALPHA,
				passes = ALPHA_BLEED_PASSES
			)
			if (bled != null) {
				if (processed !== bitmap) {
					processed.recycle()
				}
				processed = bled
			}
		}
		if (shouldFeatherTransparentTopBoundary(assetPath)) {
			val feathered = featherTransparentTopBoundary(
				bitmap = processed,
				minVisibleAlpha = ALPHA_TRIM_MIN_VISIBLE_ALPHA,
				colorCarryPixels = TEEMO_TOP_BOUNDARY_COLOR_CARRY_PIXELS,
				fadePixels = TEEMO_TOP_BOUNDARY_ALPHA_FADE_PIXELS
			)
			if (feathered != null) {
				if (processed !== bitmap) {
					processed.recycle()
				}
				processed = feathered
			}
		}
		return processed
	}

	private fun resolveTextureDecodeSampleSize(assetPath: String, qualityScale: Float): Int {
		if (qualityScale >= PREVIEW_TEXTURE_FULL_QUALITY_THRESHOLD) {
			return 1
		}
		return if (shouldUsePreviewTextureSampling(assetPath)) {
			PREVIEW_TEXTURE_SAMPLE_SIZE
		} else {
			1
		}
	}

	private fun shouldTrimTransparentTop(assetPath: String): Boolean {
		val normalized = assetPath.lowercase()
		return normalized == ANIME_SAKURA_TEXTURE_PATH
	}

	private fun shouldBleedTransparentEdgeColors(assetPath: String): Boolean {
		return false
	}

	private fun shouldUsePreviewTextureSampling(assetPath: String): Boolean {
		val normalized = assetPath.lowercase()
		return normalized == TEEMO_BACKGROUND_TEXTURE_PATH ||
			normalized == TEEMO_SUN_TEXTURE_PATH ||
			normalized == TEEMO_MOON_TEXTURE_PATH
	}

	private fun shouldFeatherTransparentTopBoundary(assetPath: String): Boolean {
		return false
	}

	private fun trimTransparentTop(
		bitmap: Bitmap,
		minAlpha: Int
	): Bitmap? {
		val width = bitmap.width
		val height = bitmap.height
		if (width <= 0 || height <= 1) return null

		val rowPixels = IntArray(width)
		var firstVisibleRow = 0
		while (firstVisibleRow < height - 1) {
			bitmap.getPixels(rowPixels, 0, width, 0, firstVisibleRow, width, 1)
			var hasVisiblePixel = false
			for (pixel in rowPixels) {
				val alpha = (pixel ushr 24) and 0xFF
				if (alpha >= minAlpha) {
					hasVisiblePixel = true
					break
				}
			}
			if (hasVisiblePixel) break
			firstVisibleRow++
		}

		if (firstVisibleRow < ALPHA_TRIM_MIN_TOP_PIXELS) return null
		val maxAllowedTrim = (height * ALPHA_TRIM_MAX_RATIO).toInt().coerceAtLeast(ALPHA_TRIM_MIN_TOP_PIXELS)
		if (firstVisibleRow > maxAllowedTrim) return null
		if (firstVisibleRow >= height - 1) return null

		return Bitmap.createBitmap(
			bitmap,
			0,
			firstVisibleRow,
			width,
			height - firstVisibleRow
		)
	}

	private fun bleedTransparentEdgeColors(
		bitmap: Bitmap,
		minOpaqueAlpha: Int,
		passes: Int
	): Bitmap? {
		val width = bitmap.width
		val height = bitmap.height
		if (width <= 1 || height <= 1 || passes <= 0) return null

		val sourcePixels = IntArray(width * height)
		bitmap.getPixels(sourcePixels, 0, width, 0, 0, width, height)
		if (sourcePixels.none { ((it ushr 24) and 0xFF) in 1 until minOpaqueAlpha }) {
			return null
		}

		var current = sourcePixels
		var working = IntArray(sourcePixels.size)
		var changed = false

		repeat(passes) {
			System.arraycopy(current, 0, working, 0, current.size)
			var passChanged = false
			for (y in 0 until height) {
				val rowOffset = y * width
				for (x in 0 until width) {
					val index = rowOffset + x
					val pixel = current[index]
					val alpha = (pixel ushr 24) and 0xFF
					if (alpha >= minOpaqueAlpha) continue

					var redTotal = 0
					var greenTotal = 0
					var blueTotal = 0
					var neighborCount = 0
					for (offsetY in -1..1) {
						val neighborY = y + offsetY
						if (neighborY !in 0 until height) continue
						val neighborRow = neighborY * width
						for (offsetX in -1..1) {
							if (offsetX == 0 && offsetY == 0) continue
							val neighborX = x + offsetX
							if (neighborX !in 0 until width) continue
							val neighbor = current[neighborRow + neighborX]
							val neighborAlpha = (neighbor ushr 24) and 0xFF
							if (neighborAlpha < minOpaqueAlpha) continue
							redTotal += (neighbor ushr 16) and 0xFF
							greenTotal += (neighbor ushr 8) and 0xFF
							blueTotal += neighbor and 0xFF
							neighborCount++
						}
					}

					if (neighborCount == 0) continue
					val red = redTotal / neighborCount
					val green = greenTotal / neighborCount
					val blue = blueTotal / neighborCount
					working[index] = (alpha shl 24) or (red shl 16) or (green shl 8) or blue
					passChanged = true
				}
			}

			if (!passChanged) return@repeat
			changed = true
			val swap = current
			current = working
			working = swap
		}

		if (!changed) return null
		return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
			setPixels(current, 0, width, 0, 0, width, height)
		}
	}

	private fun featherTransparentTopBoundary(
		bitmap: Bitmap,
		minVisibleAlpha: Int,
		colorCarryPixels: Int,
		fadePixels: Int
	): Bitmap? {
		val width = bitmap.width
		val height = bitmap.height
		if (width <= 1 || height <= 1 || fadePixels <= 0) return null

		val sourcePixels = IntArray(width * height)
		bitmap.getPixels(sourcePixels, 0, width, 0, 0, width, height)
		val result = sourcePixels.copyOf()
		var changed = false

		for (x in 0 until width) {
			var boundaryY = -1
			for (y in 0 until height) {
				val alpha = (sourcePixels[(y * width) + x] ushr 24) and 0xFF
				if (alpha >= minVisibleAlpha) {
					boundaryY = y
					break
				}
			}
			if (boundaryY <= 0) continue

			var redTotal = 0
			var greenTotal = 0
			var blueTotal = 0
			var weightTotal = 0
			val sampleEnd = (boundaryY + TEEMO_TOP_BOUNDARY_COLOR_SAMPLE_PIXELS).coerceAtMost(height - 1)
			for (sampleY in boundaryY..sampleEnd) {
				val pixel = sourcePixels[(sampleY * width) + x]
				val alpha = (pixel ushr 24) and 0xFF
				if (alpha < minVisibleAlpha) continue
				redTotal += ((pixel ushr 16) and 0xFF) * alpha
				greenTotal += ((pixel ushr 8) and 0xFF) * alpha
				blueTotal += (pixel and 0xFF) * alpha
				weightTotal += alpha
			}
			if (weightTotal == 0) continue

			val boundaryRed = (redTotal / weightTotal).coerceIn(0, 255)
			val boundaryGreen = (greenTotal / weightTotal).coerceIn(0, 255)
			val boundaryBlue = (blueTotal / weightTotal).coerceIn(0, 255)

			val carryStart = (boundaryY - colorCarryPixels).coerceAtLeast(0)
			for (carryY in carryStart until boundaryY) {
				val index = (carryY * width) + x
				val pixel = result[index]
				val alpha = (pixel ushr 24) and 0xFF
				val carriedPixel = (alpha shl 24) or (boundaryRed shl 16) or (boundaryGreen shl 8) or boundaryBlue
				if (carriedPixel != pixel) {
					result[index] = carriedPixel
					changed = true
				}
			}

			val fadeEnd = (boundaryY + fadePixels).coerceAtMost(height - 1)
			for (fadeY in boundaryY..fadeEnd) {
				val index = (fadeY * width) + x
				val pixel = sourcePixels[index]
				val alpha = (pixel ushr 24) and 0xFF
				if (alpha == 0) continue

				val progress = smoothStep01((fadeY - boundaryY).toFloat() / fadePixels.toFloat())
				val sourceRed = (pixel ushr 16) and 0xFF
				val sourceGreen = (pixel ushr 8) and 0xFF
				val sourceBlue = pixel and 0xFF
				val fadedAlpha = (alpha * progress).roundToInt().coerceIn(0, 255)
				val mixedRed = lerpChannel(boundaryRed, sourceRed, progress)
				val mixedGreen = lerpChannel(boundaryGreen, sourceGreen, progress)
				val mixedBlue = lerpChannel(boundaryBlue, sourceBlue, progress)
				val featheredPixel = (fadedAlpha shl 24) or (mixedRed shl 16) or (mixedGreen shl 8) or mixedBlue
				if (featheredPixel != result[index]) {
					result[index] = featheredPixel
					changed = true
				}
			}
		}

		if (!changed) return null
		return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
			setPixels(result, 0, width, 0, 0, width, height)
		}
	}

	private fun smoothStep01(value: Float): Float {
		val clamped = value.coerceIn(0f, 1f)
		return clamped * clamped * (3f - (2f * clamped))
	}

	private fun lerpChannel(start: Int, end: Int, progress: Float): Int {
		return (start + ((end - start) * progress)).roundToInt().coerceIn(0, 255)
	}

	private fun isPixelArtTexture(path: String): Boolean {
		val normalized = path.lowercase()
		return normalized.contains("16px")
	}

	private fun isPowerOfTwo(value: Int): Boolean {
		return value > 0 && (value and (value - 1)) == 0
	}

	companion object {
		private const val ANIME_SAKURA_TEXTURE_PATH = "anime/anime_sakura.webp"
		private const val TEEMO_BACKGROUND_TEXTURE_PATH = "teemo/teemo.webp"
		private const val TEEMO_SUN_TEXTURE_PATH = "teemo/teemo_sun.webp"
		private const val TEEMO_MOON_TEXTURE_PATH = "teemo/teemo_moon.webp"
		private const val ALPHA_TRIM_MIN_VISIBLE_ALPHA = 8
		private const val ALPHA_TRIM_MIN_TOP_PIXELS = 24
		private const val ALPHA_TRIM_MAX_RATIO = 0.45f
		private const val ALPHA_BLEED_MIN_OPAQUE_ALPHA = 96
		private const val ALPHA_BLEED_PASSES = 8
		private const val PREVIEW_TEXTURE_FULL_QUALITY_THRESHOLD = 0.95f
		private const val PREVIEW_TEXTURE_SAMPLE_SIZE = 2
		private const val TEEMO_TOP_BOUNDARY_COLOR_CARRY_PIXELS = 24
		private const val TEEMO_TOP_BOUNDARY_ALPHA_FADE_PIXELS = 40
		private const val TEEMO_TOP_BOUNDARY_COLOR_SAMPLE_PIXELS = 12
	}
}
