package com.example.core.location

import com.example.core.api.SunLocation
import java.time.ZoneId
import kotlin.math.abs

fun SunLocation.asGpsApiCandidate(
	deviceTimeZoneId: String = ZoneId.systemDefault().id
): SunLocation {
	val normalizedTimeZoneId = normalizeTimeZoneIdOrNull(timeZoneId)
		?: normalizeTimeZoneIdOrNull(deviceTimeZoneId)
	return if (normalizedTimeZoneId == timeZoneId) {
		this
	} else {
		copy(timeZoneId = normalizedTimeZoneId)
	}
}

fun LocationSnapshot.withResolvedTimeZone(
	timeZoneId: String?
): LocationSnapshot {
	val normalizedTimeZoneId = normalizeTimeZoneIdOrNull(timeZoneId) ?: return this
	if (normalizedTimeZoneId == this.timeZoneId) return this
	return copy(timeZoneId = normalizedTimeZoneId)
}

fun LocationSnapshot.matchesCoordinates(
	location: SunLocation,
	toleranceDegrees: Double = 0.001
): Boolean {
	return abs(latitude - location.latitude) <= toleranceDegrees &&
		abs(longitude - location.longitude) <= toleranceDegrees
}

private fun normalizeTimeZoneIdOrNull(timeZoneId: String?): String? {
	val normalized = timeZoneId?.trim().orEmpty()
	if (normalized.isBlank()) return null
	return runCatching { ZoneId.of(normalized).id }.getOrDefault(normalized)
}
