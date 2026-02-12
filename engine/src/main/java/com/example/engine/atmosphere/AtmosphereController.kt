package com.example.engine.atmosphere

import com.example.engine.config.SkyColors
import com.example.engine.config.WallpaperConfig
import kotlin.math.abs

class AtmosphereController {
	fun resolveSkyColor(progress: Float, sunY: Float, config: WallpaperConfig): Int {
		val palette = config.customSkyColors ?: DEFAULT_COLORS
		val horizonY = config.horizon.offset.coerceIn(0f, 1f)
		val peakY = config.peakY.coerceIn(horizonY + MIN_PEAK_DELTA, 1f)

		val dayFactor = ((sunY - horizonY) / (peakY - horizonY)).coerceIn(0f, 1f)
		val twilightFactor = (1f - abs(dayFactor - 0.5f) * 2f).coerceIn(0f, 1f)
		val twilightColor = if (progress < 0.5f) palette.sunriseColor else palette.sunsetColor

		val dayBlend = lerpColor(palette.nightColor, palette.dayColor, dayFactor)
		if (!config.features.atmosphereEnabled) {
			return dayBlend
		}

		return lerpColor(dayBlend, twilightColor, twilightFactor * TWILIGHT_BLEND_WEIGHT)
	}

	private fun lerpColor(start: Int, end: Int, t: Float): Int {
		val ratio = t.coerceIn(0f, 1f)
		val a = lerp(channel(start, 24), channel(end, 24), ratio)
		val r = lerp(channel(start, 16), channel(end, 16), ratio)
		val g = lerp(channel(start, 8), channel(end, 8), ratio)
		val b = lerp(channel(start, 0), channel(end, 0), ratio)
		return (a shl 24) or (r shl 16) or (g shl 8) or b
	}

	private fun channel(value: Int, shift: Int): Int {
		return (value shr shift) and 0xFF
	}

	private fun lerp(start: Int, end: Int, t: Float): Int {
		return (start + ((end - start) * t)).toInt().coerceIn(0, 255)
	}

	companion object {
		private const val TWILIGHT_BLEND_WEIGHT = 0.65f
		private const val MIN_PEAK_DELTA = 0.05f
		private val DEFAULT_COLORS = SkyColors(
			sunriseColor = 0xFFFFB266.toInt(),
			dayColor = 0xFF78B8FF.toInt(),
			sunsetColor = 0xFFFF7B54.toInt(),
			nightColor = 0xFF0E1A2B.toInt()
		)
	}
}

class HorizonLight {
	fun intensity(progress: Float): Float {
		return (1f - abs(progress - 0.5f) * 2f).coerceIn(0f, 1f)
	}
}

class PreSunriseGlow {
	fun amount(progress: Float): Float {
		return (0.5f - progress).coerceAtLeast(0f)
	}
}
