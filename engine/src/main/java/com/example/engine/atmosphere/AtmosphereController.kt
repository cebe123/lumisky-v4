package com.example.engine.atmosphere

import com.example.engine.config.SkyColors
import com.example.engine.config.WallpaperConfig
import kotlin.math.abs

class AtmosphereController {
	fun resolveSkyColor(progress: Float, sunY: Float, config: WallpaperConfig): Int {
		return resolveState(progress = progress, sunY = sunY, moonY = 0f, config = config).skyColor
	}

	fun resolveState(
		progress: Float,
		sunY: Float,
		moonY: Float,
		config: WallpaperConfig,
		outState: AtmosphereState = AtmosphereState()
	): AtmosphereState {
		val palette = config.customSkyColors ?: DEFAULT_COLORS
		val horizonY = config.horizon.offset.coerceIn(0f, 1f)
		val peakY = config.peakY.coerceIn(horizonY + MIN_PEAK_DELTA, 1f)

		val sunAltitude = ((sunY - horizonY) / (peakY - horizonY)).coerceIn(0f, 1f)
		val moonAltitude = ((moonY - horizonY) / (peakY - horizonY)).coerceIn(0f, 1f)
		val twilightFactor = (1f - abs(sunAltitude - 0.5f) * 2f).coerceIn(0f, 1f)
		val twilightColor = if (progress < 0.5f) palette.sunriseColor else palette.sunsetColor
		val preSunriseGlow = preSunriseGlow(progress = progress, config = config)

		val dayBlend = lerpColor(palette.nightColor, palette.dayColor, sunAltitude)
		if (!config.features.atmosphereEnabled) {
			return outState.set(
				skyTopColor = dayBlend,
				skyHorizonColor = dayBlend,
				skyColor = dayBlend,
				nightBlendFactor = (1f - sunAltitude).coerceIn(0f, 1f),
				preSunriseGlowFactor = 0f
			)
		}

		val horizonWarm = (twilightFactor * TWILIGHT_BLEND_WEIGHT + preSunriseGlow * PRE_SUNRISE_WEIGHT)
			.coerceIn(0f, 1f)
		val skyTopColor = lerpColor(dayBlend, twilightColor, horizonWarm * TOP_BLEND_SCALE)
		val skyHorizonColor = lerpColor(dayBlend, twilightColor, horizonWarm)
		val skyColor = lerpColor(skyTopColor, skyHorizonColor, HORIZON_BIAS)
		val nightBlend = (1f - maxOf(sunAltitude, moonAltitude * MOON_LIFT_SCALE)).coerceIn(0f, 1f)

		return outState.set(
			skyTopColor = skyTopColor,
			skyHorizonColor = skyHorizonColor,
			skyColor = skyColor,
			nightBlendFactor = nightBlend,
			preSunriseGlowFactor = preSunriseGlow
		)
	}

	private fun preSunriseGlow(
		progress: Float,
		config: WallpaperConfig
	): Float {
		val sunrise = (config.daylight.sunriseMinute / MINUTES_PER_DAY.toFloat()).coerceIn(0f, 1f)
		val delta = (sunrise - progress).normalizeUnitDistance()
		if (delta <= 0f || delta > PRE_SUNRISE_WINDOW) return 0f
		return (1f - (delta / PRE_SUNRISE_WINDOW)).coerceIn(0f, 1f)
	}

	private fun Float.normalizeUnitDistance(): Float {
		val wrapped = (this + 1f) % 1f
		return wrapped
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
		private const val HORIZON_BIAS = 0.65f
		private const val TOP_BLEND_SCALE = 0.5f
		private const val MOON_LIFT_SCALE = 0.4f
		private const val PRE_SUNRISE_WINDOW = 0.12f
		private const val PRE_SUNRISE_WEIGHT = 0.75f
		private const val MINUTES_PER_DAY = 24 * 60
		private val DEFAULT_COLORS = SkyColors(
			sunriseColor = 0xFFFFB266.toInt(),
			dayColor = 0xFF78B8FF.toInt(),
			sunsetColor = 0xFFFF7B54.toInt(),
			nightColor = 0xFF0E1A2B.toInt()
		)
	}
}

class AtmosphereState(
	var skyTopColor: Int = 0,
	var skyHorizonColor: Int = 0,
	var skyColor: Int = 0,
	var nightBlendFactor: Float = 0f,
	var preSunriseGlowFactor: Float = 0f
) {
	fun set(
		skyTopColor: Int,
		skyHorizonColor: Int,
		skyColor: Int,
		nightBlendFactor: Float,
		preSunriseGlowFactor: Float
	): AtmosphereState {
		this.skyTopColor = skyTopColor
		this.skyHorizonColor = skyHorizonColor
		this.skyColor = skyColor
		this.nightBlendFactor = nightBlendFactor
		this.preSunriseGlowFactor = preSunriseGlowFactor
		return this
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
