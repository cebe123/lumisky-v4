package com.example.engine.time

import com.example.core.SystemTimeProvider
import com.example.core.TimeProvider
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

class TimeManager(
	private val timeProvider: TimeProvider = SystemTimeProvider()
) {
	fun nowMillis(): Long = timeProvider.nowMillis()

	fun dayProgress(
		atMillis: Long = nowMillis(),
		timeZoneId: String? = null
	): Float {
		val localTime = Instant.ofEpochMilli(atMillis)
			.atZone(resolveZone(timeZoneId))
			.toLocalTime()
		return localTime.toNanoOfDay().toDouble()
			.div(NANOS_PER_DAY.toDouble())
			.toFloat()
	}

	fun millisFromDayProgress(
		progress: Float,
		timeZoneId: String? = null
	): Long {
		val zone = resolveZone(timeZoneId)
		val localDate = Instant.ofEpochMilli(nowMillis())
			.atZone(zone)
			.toLocalDate()
		val targetTime = LocalTime.ofNanoOfDay(
			(progress.coerceIn(0f, 1f) * NANOS_PER_DAY.toDouble()).toLong()
				.coerceIn(0L, NANOS_PER_DAY - 1L)
		)
		return localDate
			.atTime(targetTime)
			.atZone(zone)
			.toInstant()
			.toEpochMilli()
	}

	fun resolveDayCycle(
		atMillis: Long = nowMillis(),
		sunriseMinute: Int,
		sunsetMinute: Int,
		timeZoneId: String? = null
	): DayCycleState {
		val progress = dayProgress(atMillis, timeZoneId)
		val sunrise = normalizeMinute(sunriseMinute)
		val sunset = normalizeMinute(sunsetMinute)
		val dayLength = computeDayLengthMinutes(
			sunriseMinute = sunriseMinute,
			sunsetMinute = sunsetMinute
		)
		val nightLength = (MINUTES_PER_DAY - dayLength).coerceIn(0, MINUTES_PER_DAY)

		val isNight = if (sunset >= sunrise) {
			progress < sunrise || progress > sunset
		} else {
			// Wrap-around fallback for edge-case daylight windows.
			progress > sunset && progress < sunrise
		}

		return DayCycleState(
			progressDay = progress,
			isNight = isNight,
			dayLengthMinutes = dayLength,
			nightLengthMinutes = nightLength,
			sunriseProgress = sunrise,
			sunsetProgress = sunset
		)
	}

	private fun normalizeMinute(minute: Int): Float {
		return minute.coerceIn(0, MINUTES_PER_DAY) / MINUTES_PER_DAY.toFloat()
	}

	private fun computeDayLengthMinutes(
		sunriseMinute: Int,
		sunsetMinute: Int
	): Int {
		val sunrise = sunriseMinute.coerceIn(0, MINUTES_PER_DAY)
		val sunset = sunsetMinute.coerceIn(0, MINUTES_PER_DAY)
		val duration = if (sunset >= sunrise) {
			sunset - sunrise
		} else {
			(MINUTES_PER_DAY - sunrise) + sunset
		}
		return duration.coerceIn(1, MINUTES_PER_DAY - 1)
	}

	private fun resolveZone(timeZoneId: String?): ZoneId {
		val configured = timeZoneId
			?.trim()
			?.takeIf { it.isNotBlank() }
			?.let { runCatching { ZoneId.of(it) }.getOrNull() }
		return configured ?: ZoneId.systemDefault()
	}

	companion object {
		const val MILLIS_PER_DAY: Long = 24L * 60L * 60L * 1000L
		private const val NANOS_PER_DAY: Long = 24L * 60L * 60L * 1_000_000_000L
		const val NOON_PROGRESS: Float = 0.5f
		const val MINUTES_PER_DAY: Int = 24 * 60
	}
}

data class DayCycleState(
	val progressDay: Float,
	val isNight: Boolean,
	val dayLengthMinutes: Int,
	val nightLengthMinutes: Int,
	val sunriseProgress: Float,
	val sunsetProgress: Float
)

class DayCycleCalculator {
	fun normalize(progress: Float): Float = progress.coerceIn(0f, 1f)
}

class TimeInterpolator {
	fun lerp(start: Float, end: Float, t: Float): Float {
		val value = t.coerceIn(0f, 1f)
		return start + (end - start) * value
	}
}
