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
			val dayProgress = resolvePeakAlignedPhaseProgress(
				currentMinute = minute,
				startMinute = sunrise,
				peakMinute = resolveSunZenithMinute(
					sunriseMinute = sunrise,
					sunsetMinute = sunset,
					config = config
				),
				endMinute = sunset
			)
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
			val sunZenithMinute = resolveSunZenithMinute(
				sunriseMinute = sunrise,
				sunsetMinute = sunset,
				config = config
			)
			val nightProgress = resolvePeakAlignedPhaseProgress(
				currentMinute = minute,
				startMinute = sunset,
				peakMinute = (sunZenithMinute + HALF_DAY_MINUTES) % MINUTES_PER_DAY,
				endMinute = sunrise
			)
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

	private fun resolveSunZenithMinute(
		sunriseMinute: Int,
		sunsetMinute: Int,
		config: WallpaperConfig
	): Int {
		val normalizedSunrise = sunriseMinute.coerceIn(0, MINUTES_PER_DAY)
		val normalizedSunset = sunsetMinute.coerceIn(0, MINUTES_PER_DAY)
		val configuredZenith = config.daylight.solarNoonMinute.coerceIn(0, MINUTES_PER_DAY)
		return if (configuredZenith in normalizedSunrise..normalizedSunset) {
			configuredZenith
		} else {
			normalizedSunrise + ((normalizedSunset - normalizedSunrise).coerceAtLeast(1) / 2)
		}
	}

	private fun resolvePeakAlignedPhaseProgress(
		currentMinute: Int,
		startMinute: Int,
		peakMinute: Int,
		endMinute: Int
	): Float {
		val start = normalizeMinute(startMinute)
		val peak = normalizeMinuteForward(peakMinute, anchorMinute = start)
		val end = normalizeMinuteForward(endMinute, anchorMinute = start)
		val current = normalizeMinuteForward(currentMinute, anchorMinute = start)
		if (current <= peak) {
			val firstHalfDuration = (peak - start).coerceAtLeast(1)
			val firstHalfProgress = (current - start).toFloat() / firstHalfDuration.toFloat()
			return (firstHalfProgress * 0.5f).coerceIn(0f, 0.5f)
		}
		val secondHalfDuration = (end - peak).coerceAtLeast(1)
		val secondHalfProgress = (current - peak).toFloat() / secondHalfDuration.toFloat()
		return (0.5f + (secondHalfProgress * 0.5f)).coerceIn(0.5f, 1f)
	}

	private fun minuteOfDay(progress: Float): Int {
		val wrapped = ((progress % 1f) + 1f) % 1f
		return (wrapped * MINUTES_PER_DAY).toInt().coerceIn(0, MINUTES_PER_DAY)
	}

	private fun normalizeMinute(minute: Int): Int {
		return minute.coerceIn(0, MINUTES_PER_DAY)
	}

	private fun normalizeMinuteForward(
		minute: Int,
		anchorMinute: Int
	): Int {
		var normalized = normalizeMinute(minute)
		while (normalized < anchorMinute) {
			normalized += MINUTES_PER_DAY
		}
		return normalized
	}

	companion object {
		private const val MINUTES_PER_DAY = 24 * 60
		private const val HALF_DAY_MINUTES = 12 * 60
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
