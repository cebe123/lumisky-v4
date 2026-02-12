package com.example.engine.celestial

import com.example.engine.config.PathType
import com.example.engine.config.WallpaperConfig
import com.example.engine.sky.Vec2
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin

private enum class DayPhase {
	DAYLIGHT,
	NIGHT
}

class CelestialCalculator {
	fun computeSunPosition(progress: Float, config: WallpaperConfig): Vec2 {
		return computePosition(progress, config, config.celestial.sunPathType, DayPhase.DAYLIGHT)
	}

	fun computeMoonPosition(progress: Float, config: WallpaperConfig): Vec2 {
		return computePosition(progress, config, config.celestial.moonPathType, DayPhase.NIGHT)
	}

	private fun computePosition(
		dayProgress: Float,
		config: WallpaperConfig,
		pathType: PathType,
		phase: DayPhase
	): Vec2 {
		val progress = dayProgress.coerceIn(0f, 1f)
		val sunrise = normalizeMinute(config.daylight.sunriseMinute)
		val sunset = normalizeMinute(config.daylight.sunsetMinute)

		val phaseProgress = when (phase) {
			DayPhase.DAYLIGHT -> daylightProgress(progress, sunrise, sunset)
			DayPhase.NIGHT -> nightProgress(progress, sunrise, sunset)
		}

		val horizonY = config.horizon.offset.coerceIn(0f, 1f)
		val peakY = config.peakY.coerceIn(horizonY + MIN_PEAK_DELTA, 1f)
		val belowHorizonY = (horizonY - config.belowHorizonOffset).coerceIn(0f, 1f)

		val pathProgress = phaseProgress ?: when (phase) {
			DayPhase.DAYLIGHT -> if (progress < sunrise) 0f else 1f
			DayPhase.NIGHT -> if (progress < sunrise) 1f else 0f
		}

		val altitude = if (phaseProgress == null) {
			0f
		} else {
			sin(PI.toFloat() * pathProgress).coerceIn(0f, 1f)
		}

		val x = when (pathType) {
			PathType.ARC -> pathProgress
			PathType.VERTICAL -> VERTICAL_PATH_X
		}
		val y = belowHorizonY + ((peakY - belowHorizonY) * altitude)
		return Vec2(x.coerceIn(0f, 1f), y.coerceIn(0f, 1f))
	}

	private fun normalizeMinute(minute: Int): Float {
		val clamped = minute.coerceIn(0, MINUTES_PER_DAY)
		return clamped / MINUTES_PER_DAY.toFloat()
	}

	private fun daylightProgress(progress: Float, sunrise: Float, sunset: Float): Float? {
		if (sunset <= sunrise) {
			return fallbackPhaseProgress(progress)
		}
		if (progress < sunrise || progress > sunset) {
			return null
		}
		val daylightWindow = max(sunset - sunrise, EPSILON)
		return ((progress - sunrise) / daylightWindow).coerceIn(0f, 1f)
	}

	private fun nightProgress(progress: Float, sunrise: Float, sunset: Float): Float? {
		if (sunset <= sunrise) {
			return fallbackPhaseProgress(progress)
		}
		if (progress in sunrise..sunset) {
			return null
		}

		val nightDuration = max((1f - sunset) + sunrise, EPSILON)
		val elapsed = if (progress >= sunset) {
			progress - sunset
		} else {
			(1f - sunset) + progress
		}
		return (elapsed / nightDuration).coerceIn(0f, 1f)
	}

	private fun fallbackPhaseProgress(progress: Float): Float {
		return progress.coerceIn(0f, 1f)
	}

	companion object {
		private const val MINUTES_PER_DAY = 24 * 60
		private const val VERTICAL_PATH_X = 0.5f
		private const val MIN_PEAK_DELTA = 0.05f
		private const val EPSILON = 0.0001f
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
