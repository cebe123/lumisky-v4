package com.example.engine.time

import com.example.core.SystemTimeProvider
import com.example.core.TimeProvider
import java.util.TimeZone
import kotlin.math.roundToLong

class TimeManager(
	private val timeProvider: TimeProvider = SystemTimeProvider()
) {
	@Volatile
	private var cachedTimeZoneKey: String? = null
	@Volatile
	private var cachedTimeZone: TimeZone? = null
	@Volatile
	private var cachedDefaultTimeZoneId: String? = null
	@Volatile
	private var cachedDefaultTimeZone: TimeZone? = null

	fun nowMillis(): Long = timeProvider.nowMillis()

	fun dayProgress(
		atMillis: Long = nowMillis(),
		timeZoneId: String? = null
	): Float {
		val timeZone = resolveTimeZone(timeZoneId)
		val localMillis = localMillisOfDay(
			atMillis = atMillis,
			timeZone = timeZone
		)
		return localMillis.toDouble()
			.div(MILLIS_PER_DAY.toDouble())
			.toFloat()
	}

	fun millisFromDayProgress(
		progress: Float,
		timeZoneId: String? = null
	): Long {
		val timeZone = resolveTimeZone(timeZoneId)
		val nowMillis = nowMillis()
		val targetLocalMillis = (progress.coerceIn(0f, 1f) * SECONDS_PER_DAY.toDouble())
			.roundToLong()
			.times(1_000L)
			.coerceIn(0L, MILLIS_PER_DAY - 1L)
		val dayStartLocalMillis = localDayStartMillis(
			atMillis = nowMillis,
			timeZone = timeZone
		)
		return resolveUtcMillis(
			targetLocalMillis = dayStartLocalMillis + targetLocalMillis,
			timeZone = timeZone,
			referenceMillis = nowMillis
		)
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

	private fun resolveTimeZone(timeZoneId: String?): TimeZone {
		val normalized = timeZoneId
			?.trim()
			?.takeIf { it.isNotBlank() }
		if (normalized == null) {
			val defaultTimeZone = TimeZone.getDefault()
			if (cachedDefaultTimeZoneId == defaultTimeZone.id) {
				cachedDefaultTimeZone?.let { return it }
			}
			cachedDefaultTimeZoneId = defaultTimeZone.id
			cachedDefaultTimeZone = defaultTimeZone
			return defaultTimeZone
		}

		if (cachedTimeZoneKey == normalized) {
			cachedTimeZone?.let { return it }
		}

		val resolved = TimeZone.getTimeZone(normalized)
		val timeZone = if (resolved.id == "GMT" && !isGmtLikeTimeZoneId(normalized)) {
			resolveTimeZone(null)
		} else {
			resolved
		}
		cachedTimeZoneKey = normalized
		cachedTimeZone = timeZone
		return timeZone
	}

	private fun localMillisOfDay(
		atMillis: Long,
		timeZone: TimeZone
	): Long {
		val localMillis = atMillis + timeZone.getOffset(atMillis).toLong()
		return floorMod(localMillis, MILLIS_PER_DAY)
	}

	private fun localDayStartMillis(
		atMillis: Long,
		timeZone: TimeZone
	): Long {
		val localMillis = atMillis + timeZone.getOffset(atMillis).toLong()
		return localMillis - floorMod(localMillis, MILLIS_PER_DAY)
	}

	private fun resolveUtcMillis(
		targetLocalMillis: Long,
		timeZone: TimeZone,
		referenceMillis: Long
	): Long {
		var targetUtcMillis = targetLocalMillis - timeZone.getOffset(referenceMillis).toLong()
		repeat(2) {
			val offsetMillis = timeZone.getOffset(targetUtcMillis).toLong()
			val adjustedUtcMillis = targetLocalMillis - offsetMillis
			if (adjustedUtcMillis == targetUtcMillis) {
				return targetUtcMillis
			}
			targetUtcMillis = adjustedUtcMillis
		}
		return targetUtcMillis
	}

	private fun isGmtLikeTimeZoneId(value: String): Boolean {
		return value.equals("GMT", ignoreCase = true) ||
			value.startsWith("GMT", ignoreCase = true) ||
			value.equals("UTC", ignoreCase = true) ||
			value.startsWith("UTC", ignoreCase = true)
	}

	private fun floorMod(
		value: Long,
		modulus: Long
	): Long {
		val remainder = value % modulus
		return if (remainder >= 0L) remainder else remainder + modulus
	}

	companion object {
		const val MILLIS_PER_DAY: Long = 24L * 60L * 60L * 1000L
		private const val SECONDS_PER_DAY: Int = 24 * 60 * 60
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
