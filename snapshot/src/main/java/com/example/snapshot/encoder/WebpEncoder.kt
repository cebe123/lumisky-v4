package com.example.snapshot.encoder

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.util.Base64
import android.util.LruCache
import com.example.engine.config.WallpaperConfig
import com.example.engine.renderer.RenderFrameState
import java.io.ByteArrayOutputStream
import kotlin.math.max

data class EncodedSnapshot(
	val bytes: ByteArray,
	val mimeType: String = "image/webp"
) {
	fun asDataUri(): String {
		return "data:$mimeType;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
	}
}

class WebpEncoder {
	fun encode(bitmap: Bitmap, quality: Int = DEFAULT_QUALITY): EncodedSnapshot? {
		val outputStream = ByteArrayOutputStream()
		val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			Bitmap.CompressFormat.WEBP_LOSSY
		} else {
			@Suppress("DEPRECATION")
			Bitmap.CompressFormat.WEBP
		}

		val compressed = bitmap.compress(format, quality.coerceIn(0, 100), outputStream)
		if (!compressed) return null
		return EncodedSnapshot(outputStream.toByteArray())
	}

	companion object {
		private const val DEFAULT_QUALITY = 80
	}
}

class BitmapScaler {
	fun scaleRatio(): Float = SCALE_RATIO

	fun scale(value: Int): Int {
		return (value * scaleRatio()).toInt().coerceAtLeast(1)
	}

	companion object {
		private const val SCALE_RATIO = 0.7f
	}
}

class SnapshotBitmapFactory {
	private val scaler = BitmapScaler()
	private val texturePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
		isFilterBitmap = true
		isDither = true
	}
	private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
		isFilterBitmap = true
		isDither = true
		alpha = 255
	}
	private val textureCache = object : LruCache<String, Bitmap>(MAX_TEXTURE_CACHE_KB) {
		override fun sizeOf(key: String, value: Bitmap): Int {
			return (value.byteCount / 1024).coerceAtLeast(1)
		}
	}

	private val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
		color = 0xFFFFD176.toInt()
		style = Paint.Style.FILL
	}

	private val moonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
		color = 0xFFE6EDFF.toInt()
		style = Paint.Style.FILL
	}

	fun create(
		state: RenderFrameState,
		config: WallpaperConfig,
		textureBytesLoader: ((String) -> ByteArray?)? = null,
		baseWidth: Int = BASE_WIDTH,
		baseHeight: Int = BASE_HEIGHT
	): Bitmap {
		val width = scaler.scale(baseWidth)
		val height = scaler.scale(baseHeight)
		val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
		val canvas = Canvas(bitmap)
		drawBackground(
			canvas = canvas,
			state = state,
			config = config,
			textureBytesLoader = textureBytesLoader
		)

		val celestialRadius = max(6f, width * CELESTIAL_RADIUS_RATIO)
		drawCelestial(
			canvas = canvas,
			normalizedX = state.sun.x,
			normalizedY = state.sun.y,
			radius = celestialRadius,
			paint = sunPaint,
			textureBitmap = loadTextureBitmap(
				path = config.textures.sunTexture,
				textureBytesLoader = textureBytesLoader
			)
		)
		drawCelestial(
			canvas = canvas,
			normalizedX = state.moon.x,
			normalizedY = state.moon.y,
			radius = celestialRadius * MOON_RADIUS_MULTIPLIER,
			paint = moonPaint,
			textureBitmap = loadTextureBitmap(
				path = config.textures.moonTexture,
				textureBytesLoader = textureBytesLoader
			)
		)
		return bitmap
	}

	private fun drawBackground(
		canvas: Canvas,
		state: RenderFrameState,
		config: WallpaperConfig,
		textureBytesLoader: ((String) -> ByteArray?)?
	) {
		val backgroundBitmap = loadTextureBitmap(
			path = config.textures.backgroundTexture,
			textureBytesLoader = textureBytesLoader
		)
		if (backgroundBitmap == null) {
			canvas.drawColor(state.skyColor)
		} else {
			drawCoverBitmap(
				canvas = canvas,
				bitmap = backgroundBitmap,
				paint = texturePaint
			)
		}

		if (!config.id.lowercase().contains("warrior")) return
		val overlayBitmap = loadTextureBitmap(
			path = config.textures.flareTexture,
			textureBytesLoader = textureBytesLoader
		) ?: return
		overlayPaint.alpha = (255f * WARRIOR_OVERLAY_ALPHA).toInt().coerceIn(0, 255)
		drawCoverBitmap(
			canvas = canvas,
			bitmap = overlayBitmap,
			paint = overlayPaint
		)
		overlayPaint.alpha = 255
	}

	private fun drawCoverBitmap(
		canvas: Canvas,
		bitmap: Bitmap,
		paint: Paint
	) {
		val dstWidth = canvas.width
		val dstHeight = canvas.height
		if (dstWidth <= 0 || dstHeight <= 0 || bitmap.width <= 0 || bitmap.height <= 0) return
		val dstAspect = dstWidth.toFloat() / dstHeight.toFloat()
		val srcAspect = bitmap.width.toFloat() / bitmap.height.toFloat()

		val srcRect = if (srcAspect > dstAspect) {
			val cropWidth = (bitmap.height * dstAspect).toInt().coerceAtLeast(1)
			val left = ((bitmap.width - cropWidth) / 2).coerceAtLeast(0)
			Rect(left, 0, (left + cropWidth).coerceAtMost(bitmap.width), bitmap.height)
		} else {
			val cropHeight = (bitmap.width / dstAspect).toInt().coerceAtLeast(1)
			val top = ((bitmap.height - cropHeight) / 2).coerceAtLeast(0)
			Rect(0, top, bitmap.width, (top + cropHeight).coerceAtMost(bitmap.height))
		}
		val dstRect = Rect(0, 0, dstWidth, dstHeight)
		canvas.drawBitmap(bitmap, srcRect, dstRect, paint)
	}

	private fun drawCelestial(
		canvas: Canvas,
		normalizedX: Float,
		normalizedY: Float,
		radius: Float,
		paint: Paint,
		textureBitmap: Bitmap?
	) {
		val x = normalizedX.coerceIn(0f, 1f) * canvas.width
		val y = (1f - normalizedY.coerceIn(0f, 1f)) * canvas.height
		if (textureBitmap == null) {
			canvas.drawCircle(x, y, radius, paint)
			return
		}
		val left = (x - radius).toInt()
		val top = (y - radius).toInt()
		val right = (x + radius).toInt()
		val bottom = (y + radius).toInt()
		canvas.drawBitmap(
			textureBitmap,
			null,
			Rect(left, top, right, bottom),
			texturePaint
		)
	}

	private fun loadTextureBitmap(
		path: String?,
		textureBytesLoader: ((String) -> ByteArray?)?
	): Bitmap? {
		val normalized = path?.takeIf { it.isNotBlank() } ?: return null
		val loader = textureBytesLoader ?: return null
		val resolved = resolvePreferredTexturePath(normalized, loader)
		textureCache.get(resolved)?.let { cached ->
			if (!cached.isRecycled) return cached
		}
		val bytes = runCatching { loader(resolved) }.getOrNull()
			?.takeIf { it.isNotEmpty() }
			?: return null
		val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
		textureCache.put(resolved, decoded)
		return decoded
	}

	private fun resolvePreferredTexturePath(
		originalPath: String,
		loader: (String) -> ByteArray?
	): String {
		val lower = originalPath.lowercase()
		if (lower.endsWith(".webp")) return originalPath
		val extIndex = originalPath.lastIndexOf('.')
		if (extIndex == -1) return originalPath
		val webpCandidate = "${originalPath.substring(0, extIndex)}.webp"
		val bytes = runCatching { loader(webpCandidate) }.getOrNull()
		return if (bytes != null && bytes.isNotEmpty()) webpCandidate else originalPath
	}

	fun release() {
		textureCache.snapshot().values.forEach { bitmap ->
			if (!bitmap.isRecycled) {
				bitmap.recycle()
			}
		}
		textureCache.evictAll()
	}

	companion object {
		private const val BASE_WIDTH = 732
		private const val BASE_HEIGHT = 412
		private const val CELESTIAL_RADIUS_RATIO = 0.055f
		private const val MOON_RADIUS_MULTIPLIER = 0.9f
		private const val WARRIOR_OVERLAY_ALPHA = 0.45f
		private const val MAX_TEXTURE_CACHE_KB = 40 * 1024
	}
}
