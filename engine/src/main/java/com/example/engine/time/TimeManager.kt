package com.example.engine.time

import com.example.core.SystemTimeProvider
import com.example.core.TimeProvider

class TimeManager(
	private val timeProvider: TimeProvider = SystemTimeProvider()
) {
	fun nowMillis(): Long = timeProvider.nowMillis()

	fun dayProgress(atMillis: Long = nowMillis()): Float {
		val dayMillis = atMillis % MILLIS_PER_DAY
		return dayMillis.toFloat() / MILLIS_PER_DAY.toFloat()
	}

	fun millisFromDayProgress(progress: Float): Long {
		return (progress.coerceIn(0f, 1f) * MILLIS_PER_DAY).toLong()
	}

	companion object {
		const val MILLIS_PER_DAY: Long = 24L * 60L * 60L * 1000L
		const val NOON_PROGRESS: Float = 0.5f
	}
}

class DayCycleCalculator {
	fun normalize(progress: Float): Float = progress.coerceIn(0f, 1f)
}

class TimeInterpolator {
	fun lerp(start: Float, end: Float, t: Float): Float {
		val value = t.coerceIn(0f, 1f)
		return start + (end - start) * value
	}
}
