package com.example.lumisky.viewmodel

import com.example.core.location.LocationSnapshot
import com.example.core.settings.ManualCity
import java.util.Locale

internal const val LAST_KNOWN_CITY_ID = "gps_last_known"

internal fun LocationSnapshot.toLastKnownManualCity(): ManualCity {
	val resolvedName = label
		?.takeIf { it.isNotBlank() }
		?: formatGpsCoordinatesLabel(latitude, longitude)
	return ManualCity(
		id = LAST_KNOWN_CITY_ID,
		name = resolvedName,
		countryCode = "GPS",
		latitude = latitude,
		longitude = longitude,
		timeZoneId = timeZoneId
	)
}

internal fun formatGpsCoordinatesLabel(latitude: Double, longitude: Double): String {
	return "(${latitude.toDisplay()}, ${longitude.toDisplay()})"
}

private fun Double.toDisplay(): String = String.format(Locale.US, "%.2f", this)
