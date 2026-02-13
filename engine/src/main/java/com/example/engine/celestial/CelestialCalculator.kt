package com.example.engine.celestial

import com.example.engine.config.PathType
import com.example.engine.config.WallpaperConfig
import com.example.engine.sky.Vec2
import kotlin.math.PI
import kotlin.math.sin

class CelestialCalculator {

	fun computeSunPosition(progress: Float, config: WallpaperConfig): Vec2 {
		val minute = minuteOfDay(progress)
		val sunrise = config.daylight.sunriseMinute.coerceIn(0, MINUTES_PER_DAY)
		val sunset = config.daylight.sunsetMinute.coerceIn(0, MINUTES_PER_DAY)
		val horizonY = config.horizon.offset.coerceIn(0f, 1f)
		val peakY = config.peakY.coerceIn(horizonY + MIN_PEAK_DELTA, 1f)
		val hiddenY = hiddenY(horizonY)

		if (sunset <= sunrise) {
			val fallbackProgress = progress.coerceIn(0f, 1f)
			return Vec2(
				x = resolveX(config.celestial.sunPathType, fallbackProgress),
				y = arcY(horizonY, peakY, fallbackProgress)
			)
		}

		return if (minute in sunrise..sunset) {
			val dayDuration = (sunset - sunrise).coerceAtLeast(1).toFloat()
			val dayProgress = ((minute - sunrise) / dayDuration).coerceIn(0f, 1f)
			Vec2(
				x = resolveX(config.celestial.sunPathType, dayProgress),
				y = arcY(horizonY, peakY, dayProgress)
			)
		} else {
			Vec2(
				x = resolveX(config.celestial.sunPathType, 0.5f),
				y = hiddenY
			)
		}
	}

	fun computeMoonPosition(progress: Float, config: WallpaperConfig): Vec2 {
		val minute = minuteOfDay(progress)
		val sunrise = config.daylight.sunriseMinute.coerceIn(0, MINUTES_PER_DAY)
		val sunset = config.daylight.sunsetMinute.coerceIn(0, MINUTES_PER_DAY)
		val horizonY = config.horizon.offset.coerceIn(0f, 1f)
		val peakY = config.peakY.coerceIn(horizonY + MIN_PEAK_DELTA, 1f)
		val hiddenY = hiddenY(horizonY)

		if (sunset <= sunrise) {
			val fallbackProgress = progress.coerceIn(0f, 1f)
			return Vec2(
				x = resolveX(config.celestial.moonPathType, fallbackProgress),
				y = arcY(horizonY, peakY, fallbackProgress)
			)
		}

		return if (minute in sunrise..sunset) {
			Vec2(
				x = resolveX(config.celestial.moonPathType, 0.5f),
				y = hiddenY
			)
		} else {
			var minutesAfterSunset = minute - sunset
			if (minutesAfterSunset < 0) minutesAfterSunset += MINUTES_PER_DAY
			val nightDuration = (MINUTES_PER_DAY - sunset + sunrise).coerceAtLeast(1).toFloat()
			val nightProgress = (minutesAfterSunset / nightDuration).coerceIn(0f, 1f)
			Vec2(
				x = resolveX(config.celestial.moonPathType, nightProgress),
				y = arcY(horizonY, peakY, nightProgress)
			)
		}
	}

	private fun resolveX(pathType: PathType, phaseProgress: Float): Float {
		return when (pathType) {
			PathType.VERTICAL -> VERTICAL_PATH_X
			PathType.ARC -> phaseProgress.coerceIn(0f, 1f)
		}
	}

	private fun arcY(horizonY: Float, peakY: Float, phaseProgress: Float): Float {
		val amplitude = (peakY - horizonY).coerceAtLeast(0f)
		return horizonY + (sin(phaseProgress.coerceIn(0f, 1f) * PI.toFloat()) * amplitude)
	}

	private fun hiddenY(horizonY: Float): Float {
		return (horizonY - HIDDEN_DEPTH).coerceAtMost(HIDDEN_Y_MAX)
	}

	private fun minuteOfDay(progress: Float): Int {
		val wrapped = ((progress % 1f) + 1f) % 1f
		return (wrapped * MINUTES_PER_DAY).toInt().coerceIn(0, MINUTES_PER_DAY)
	}

	companion object {
		private const val MINUTES_PER_DAY = 24 * 60
		private const val VERTICAL_PATH_X = 0.5f
		private const val MIN_PEAK_DELTA = 0.05f
		private const val HIDDEN_DEPTH = 0.75f
		private const val HIDDEN_Y_MAX = -0.15f
	}
}

class SunController(private val calculator: CelestialCalculator) {
	fun resolve(progress: Float, config: WallpaperConfig): Vec2 {
		return calculator.computeSunPosition(progress, config)
	}
}

class MoonController(private val calculator: CelestialCalculator) {
	fun resolve(progress: Float, config: WallpaperConfig): Vec2 {
		return calculator.computeMoonPosition(progress, config)
	}
}

class OrbitPathMapper {
	fun mapToArc(progress: Float, horizonOffset: Float): Float {
		return (progress.coerceIn(0f, 1f) - horizonOffset).coerceIn(0f, 1f)
	}
}
