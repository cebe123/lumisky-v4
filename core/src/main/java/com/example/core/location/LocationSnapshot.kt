package com.example.core.location

import com.example.core.api.SunLocation
import java.time.ZoneId

enum class LocationAccessLevel {
	NONE,
	APPROXIMATE,
	PRECISE
}

enum class LocationSource {
	STORED,
	LAST_KNOWN,
	CURRENT,
	PASSIVE
}

data class LocationSnapshot(
	val latitude: Double,
	val longitude: Double,
	val timeZoneId: String = ZoneId.systemDefault().id,
	val label: String? = null,
	val accuracyMeters: Float? = null,
	val capturedAtEpochMs: Long,
	val accessLevel: LocationAccessLevel,
	val source: LocationSource
) {
	fun toSunLocation(
		labelFallback: String = label ?: source.name.lowercase()
	): SunLocation {
		return SunLocation(
			label = label ?: labelFallback,
			latitude = latitude,
			longitude = longitude,
			timeZoneId = timeZoneId
		)
	}

	fun isFreshWithin(
		maxAgeMs: Long,
		nowMs: Long = System.currentTimeMillis()
	): Boolean {
		if (maxAgeMs < 0L) return false
		return (nowMs - capturedAtEpochMs) in 0..maxAgeMs
	}
}
