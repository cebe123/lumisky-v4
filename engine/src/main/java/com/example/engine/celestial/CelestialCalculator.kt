package com.example.engine.celestial

import com.example.engine.config.CelestialOrbitConfig
import com.example.engine.config.OrbitCurve
import com.example.engine.config.PathType
import com.example.engine.config.WallpaperConfig
import com.example.engine.config.resolveMoonOrbit
import com.example.engine.config.resolveSunOrbit
import com.example.engine.sky.Vec2
import kotlin.math.PI
import kotlin.math.sin

class CelestialCalculator {

	fun computeSunPosition(
		progress: Float,
		config: WallpaperConfig,
		out: Vec2 = Vec2()
	): Vec2 {
		val minute = minuteOfDay(progress)
		val sunrise = config.daylight.sunriseMinute.coerceIn(0, MINUTES_PER_DAY)
		val sunset = config.daylight.sunsetMinute.coerceIn(0, MINUTES_PER_DAY)
		val horizonY = config.horizon.offset.coerceIn(0f, 1f)
		val orbit = config.resolveSunOrbit()
		val peakY = resolvePeakY(horizonY, orbit, config)
		val hiddenY = resolveHiddenY(horizonY, orbit)

		if (sunset <= sunrise) {
			val fallbackProgress = progress.coerceIn(0f, 1f)
			return resolveVisiblePosition(
				orbit = orbit,
				horizonY = horizonY,
				peakY = peakY,
				phaseProgress = fallbackProgress,
				out = out
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
			resolveVisiblePosition(
				orbit = orbit,
				horizonY = horizonY,
				peakY = peakY,
				phaseProgress = dayProgress,
				out = out
			)
		} else {
			out.set(
				x = resolveHiddenX(orbit),
				y = hiddenY
			)
		}
	}

	fun computeMoonPosition(
		progress: Float,
		config: WallpaperConfig,
		out: Vec2 = Vec2()
	): Vec2 {
		val minute = minuteOfDay(progress)
		val sunrise = config.daylight.sunriseMinute.coerceIn(0, MINUTES_PER_DAY)
		val sunset = config.daylight.sunsetMinute.coerceIn(0, MINUTES_PER_DAY)
		val horizonY = config.horizon.offset.coerceIn(0f, 1f)
		val orbit = config.resolveMoonOrbit()
		val peakY = resolvePeakY(horizonY, orbit, config)
		val hiddenY = resolveHiddenY(horizonY, orbit)

		if (sunset <= sunrise) {
			val fallbackProgress = progress.coerceIn(0f, 1f)
			return resolveVisiblePosition(
				orbit = orbit,
				horizonY = horizonY,
				peakY = peakY,
				phaseProgress = fallbackProgress,
				out = out
			)
		}

		return if (minute in sunrise..sunset) {
			out.set(
				x = resolveHiddenX(orbit),
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
			resolveVisiblePosition(
				orbit = orbit,
				horizonY = horizonY,
				peakY = peakY,
				phaseProgress = nightProgress,
				out = out
			)
		}
	}

	private fun resolveVisiblePosition(
		orbit: CelestialOrbitConfig,
		horizonY: Float,
		peakY: Float,
		phaseProgress: Float,
		out: Vec2
	): Vec2 {
		val shapedProgress = applyCurve(phaseProgress, orbit.curve)
		return out.set(
			x = resolveVisibleX(orbit, shapedProgress),
			y = arcY(horizonY, peakY, shapedProgress)
		)
	}

	private fun resolveVisibleX(
		orbit: CelestialOrbitConfig,
		phaseProgress: Float
	): Float {
		return when (orbit.pathType) {
			PathType.VERTICAL -> {
				val fixedX = orbit.startX ?: orbit.endX ?: VERTICAL_PATH_X
				fixedX.coerceIn(0f, 1f)
			}
			PathType.ARC -> {
				val startX = orbit.startX ?: ARC_PATH_START_X
				val endX = orbit.endX ?: ARC_PATH_END_X
				lerp(startX, endX, phaseProgress.coerceIn(0f, 1f)).coerceIn(0f, 1f)
			}
		}
	}

	private fun resolveHiddenX(orbit: CelestialOrbitConfig): Float {
		return when (orbit.pathType) {
			PathType.VERTICAL -> (orbit.startX ?: orbit.endX ?: VERTICAL_PATH_X).coerceIn(0f, 1f)
			PathType.ARC -> {
				val startX = orbit.startX ?: ARC_PATH_START_X
				val endX = orbit.endX ?: ARC_PATH_END_X
				((startX + endX) * 0.5f).coerceIn(0f, 1f)
			}
		}
	}

	private fun resolvePeakY(
		horizonY: Float,
		orbit: CelestialOrbitConfig,
		config: WallpaperConfig
	): Float {
		return (orbit.peakY ?: config.peakY).coerceIn(horizonY + MIN_PEAK_DELTA, 1f)
	}

	private fun resolveHiddenY(
		horizonY: Float,
		orbit: CelestialOrbitConfig
	): Float {
		return orbit.hiddenY ?: hiddenY(horizonY)
	}

	private fun applyCurve(
		phaseProgress: Float,
		curve: OrbitCurve
	): Float {
		val clamped = phaseProgress.coerceIn(0f, 1f)
		return when (curve) {
			OrbitCurve.LINEAR -> clamped
			OrbitCurve.EASE_IN_OUT -> clamped * clamped * (3f - (2f * clamped))
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

	private fun lerp(start: Float, end: Float, t: Float): Float {
		return start + ((end - start) * t.coerceIn(0f, 1f))
	}

	companion object {
		private const val MINUTES_PER_DAY = 24 * 60
		private const val HALF_DAY_MINUTES = 12 * 60
		private const val VERTICAL_PATH_X = 0.5f
		private const val ARC_PATH_START_X = 0f
		private const val ARC_PATH_END_X = 1f
		private const val MIN_PEAK_DELTA = 0.05f
		private const val HIDDEN_DEPTH = 0.75f
		private const val HIDDEN_Y_MAX = -0.15f
	}
}

class SunController(private val calculator: CelestialCalculator) {
	fun resolve(
		progress: Float,
		config: WallpaperConfig,
		out: Vec2 = Vec2()
	): Vec2 {
		return calculator.computeSunPosition(progress, config, out)
	}
}

class MoonController(private val calculator: CelestialCalculator) {
	fun resolve(
		progress: Float,
		config: WallpaperConfig,
		out: Vec2 = Vec2()
	): Vec2 {
		return calculator.computeMoonPosition(progress, config, out)
	}
}

class OrbitPathMapper {
	fun mapToArc(progress: Float, horizonOffset: Float): Float {
		return (progress.coerceIn(0f, 1f) - horizonOffset).coerceIn(0f, 1f)
	}
}
