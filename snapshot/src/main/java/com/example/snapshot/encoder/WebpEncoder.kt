package com.example.snapshot.encoder

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.util.Base64
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
		baseWidth: Int = BASE_WIDTH,
		baseHeight: Int = BASE_HEIGHT
	): Bitmap {
		val width = scaler.scale(baseWidth)
		val height = scaler.scale(baseHeight)
		val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
		val canvas = Canvas(bitmap)
		canvas.drawColor(state.skyColor)

		val celestialRadius = max(6f, width * CELESTIAL_RADIUS_RATIO)
		drawCelestial(
			canvas = canvas,
			normalizedX = state.sun.x,
			normalizedY = state.sun.y,
			radius = celestialRadius,
			paint = sunPaint
		)
		drawCelestial(
			canvas = canvas,
			normalizedX = state.moon.x,
			normalizedY = state.moon.y,
			radius = celestialRadius * MOON_RADIUS_MULTIPLIER,
			paint = moonPaint
		)
		return bitmap
	}

	private fun drawCelestial(
		canvas: Canvas,
		normalizedX: Float,
		normalizedY: Float,
		radius: Float,
		paint: Paint
	) {
		val x = normalizedX.coerceIn(0f, 1f) * canvas.width
		val y = (1f - normalizedY.coerceIn(0f, 1f)) * canvas.height
		canvas.drawCircle(x, y, radius, paint)
	}

	companion object {
		private const val BASE_WIDTH = 732
		private const val BASE_HEIGHT = 412
		private const val CELESTIAL_RADIUS_RATIO = 0.055f
		private const val MOON_RADIUS_MULTIPLIER = 0.9f
	}
}
