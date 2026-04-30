package com.example.lumisky.viewmodel

import com.example.core.location.LocationAccessLevel
import com.example.core.location.LocationSnapshot
import com.example.core.location.LocationSource
import org.junit.Assert.assertEquals
import org.junit.Test

class LastKnownCityMapperTest {

	@Test
	fun lastKnownCityMapperKeepsUnsupportedGpsLocationCoordinates() {
		val snapshot = LocationSnapshot(
			latitude = 37.3318,
			longitude = -122.0312,
			timeZoneId = "America/Los_Angeles",
			label = "Cupertino",
			accuracyMeters = 18f,
			capturedAtEpochMs = 1_000L,
			accessLevel = LocationAccessLevel.PRECISE,
			source = LocationSource.CURRENT
		)

		val city = snapshot.toLastKnownManualCity()

		assertEquals(LAST_KNOWN_CITY_ID, city.id)
		assertEquals("Cupertino", city.name)
		assertEquals("GPS", city.countryCode)
		assertEquals(37.3318, city.latitude, 0.0)
		assertEquals(-122.0312, city.longitude, 0.0)
		assertEquals("America/Los_Angeles", city.timeZoneId)
	}

	@Test
	fun lastKnownCityMapperUsesCoordinateLabelWhenPlaceNameIsMissing() {
		val snapshot = LocationSnapshot(
			latitude = 37.3318,
			longitude = -122.0312,
			timeZoneId = "America/Los_Angeles",
			label = null,
			accuracyMeters = null,
			capturedAtEpochMs = 1_000L,
			accessLevel = LocationAccessLevel.APPROXIMATE,
			source = LocationSource.PASSIVE
		)

		val city = snapshot.toLastKnownManualCity()

		assertEquals(LAST_KNOWN_CITY_ID, city.id)
		assertEquals("(37.33, -122.03)", city.name)
		assertEquals(37.3318, city.latitude, 0.0)
		assertEquals(-122.0312, city.longitude, 0.0)
	}
}
