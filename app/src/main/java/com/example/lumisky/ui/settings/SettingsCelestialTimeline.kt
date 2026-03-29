package com.example.lumisky.ui.settings

import com.example.core.api.SunDaylight

internal data class CelestialTimelineSnapshot(
	val sunriseMinute: Int,
	val sunsetMinute: Int,
	val moonriseMinute: Int,
	val moonsetMinute: Int,
	val sunProgress: Float,
	val moonProgress: Float,
	val sunActive: Boolean,
	val moonActive: Boolean
)

internal fun resolveCelestialTimeline(
	daylight: SunDaylight,
	currentMinute: Int
): CelestialTimelineSnapshot {
	val sunrise = normalizeMinute(daylight.sunriseMinute)
	val sunset = normalizeMinute(daylight.sunsetMinute)
	val normalizedCurrentMinute = normalizeMinute(currentMinute)
	val moonWindow = deriveMoonWindow(daylight)
	val sunActive = sunset > sunrise && normalizedCurrentMinute in sunrise..sunset
	val moonActive = isInWrappedRange(
		minute = normalizedCurrentMinute,
		startMinute = moonWindow.startMinute,
		endMinute = moonWindow.endMinute
	)

	return CelestialTimelineSnapshot(
		sunriseMinute = sunrise,
		sunsetMinute = sunset,
		moonriseMinute = moonWindow.startMinute,
		moonsetMinute = moonWindow.endMinute,
		sunProgress = resolveSunProgress(
			currentMinute = normalizedCurrentMinute,
			sunriseMinute = sunrise,
			sunsetMinute = sunset
		),
		moonProgress = resolveMoonProgress(
			currentMinute = normalizedCurrentMinute,
			moonriseMinute = moonWindow.startMinute,
			moonsetMinute = moonWindow.endMinute,
			isMoonActive = moonActive
		),
		sunActive = sunActive,
		moonActive = moonActive
	)
}

private data class MoonWindow(
	val startMinute: Int,
	val endMinute: Int
)

private fun deriveMoonWindow(daylight: SunDaylight): MoonWindow {
	val sunrise = normalizeMinute(daylight.sunriseMinute)
	val sunset = normalizeMinute(daylight.sunsetMinute)
	val solarNoon = normalizeMinute(daylight.solarNoonMinute)
	val nightDuration = minutesForward(
		startMinute = sunset,
		endMinute = sunrise
	).coerceAtLeast(1)
	// Settings currently receives daylight only, so lunar rise/set stay as a stable UI approximation around moon zenith.
	val visibleDuration = nightDuration.coerceAtMost(DEFAULT_MOON_VISIBILITY_WINDOW_MINUTES)
	val moonZenithForward = normalizeMinuteForward(
		minute = (solarNoon + HALF_DAY_MINUTES) % MINUTES_PER_DAY,
		anchorMinute = sunset
	)
	val latestStart = sunset + (nightDuration - visibleDuration)
	val startForward = (moonZenithForward - (visibleDuration / 2)).coerceIn(sunset, latestStart)
	val endForward = startForward + visibleDuration
	return MoonWindow(
		startMinute = normalizeMinute(startForward),
		endMinute = normalizeMinute(endForward)
	)
}

private fun resolveSunProgress(
	currentMinute: Int,
	sunriseMinute: Int,
	sunsetMinute: Int
): Float {
	if (sunsetMinute <= sunriseMinute) return 0.5f
	if (currentMinute <= sunriseMinute) return 0f
	if (currentMinute >= sunsetMinute) return 1f
	return ((currentMinute - sunriseMinute).toFloat() / (sunsetMinute - sunriseMinute).toFloat())
		.coerceIn(0f, 1f)
}

private fun resolveMoonProgress(
	currentMinute: Int,
	moonriseMinute: Int,
	moonsetMinute: Int,
	isMoonActive: Boolean
): Float {
	val duration = minutesForward(
		startMinute = moonriseMinute,
		endMinute = moonsetMinute
	).coerceAtLeast(1)
	if (!isMoonActive) return 0f
	val elapsed = minutesForward(
		startMinute = moonriseMinute,
		endMinute = currentMinute
	).coerceAtMost(duration)
	return (1f - (elapsed.toFloat() / duration.toFloat())).coerceIn(0f, 1f)
}

private fun isInWrappedRange(
	minute: Int,
	startMinute: Int,
	endMinute: Int
): Boolean {
	return if (startMinute <= endMinute) {
		minute in startMinute..endMinute
	} else {
		minute >= startMinute || minute <= endMinute
	}
}

private fun minutesForward(
	startMinute: Int,
	endMinute: Int
): Int {
	val normalizedStart = normalizeMinute(startMinute)
	val normalizedEnd = normalizeMinute(endMinute)
	return if (normalizedEnd >= normalizedStart) {
		normalizedEnd - normalizedStart
	} else {
		(MINUTES_PER_DAY - normalizedStart) + normalizedEnd
	}
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

private fun normalizeMinute(minute: Int): Int {
	return ((minute % MINUTES_PER_DAY) + MINUTES_PER_DAY) % MINUTES_PER_DAY
}

private const val MINUTES_PER_DAY = 24 * 60
private const val HALF_DAY_MINUTES = 12 * 60
private const val DEFAULT_MOON_VISIBILITY_WINDOW_MINUTES = 10 * 60
