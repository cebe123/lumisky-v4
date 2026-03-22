package com.example.core.location

import com.example.core.api.SunLocation
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.util.TimeZone

class SunLocationExtensionsTest {

	@Test
	fun gpsApiCandidateFallsBackToDeviceTimeZone() {
		withDefaultTimeZone("Europe/Istanbul") {
			val candidate = SunLocation(
				label = "gps",
				latitude = 40.7128,
				longitude = -74.0060,
				timeZoneId = null
			)

			assertEquals("Europe/Istanbul", candidate.asGpsApiCandidate().timeZoneId)
		}
	}

	@Test
	fun gpsApiCandidateKeepsResolvedNonDeviceTimeZone() {
		withDefaultTimeZone("Europe/Istanbul") {
			val candidate = SunLocation(
				label = "gps",
				latitude = 40.7128,
				longitude = -74.0060,
				timeZoneId = "America/New_York"
			)

			assertEquals("America/New_York", candidate.asGpsApiCandidate().timeZoneId)
		}
	}

	@Test
	fun gpsApiCandidateNormalizesProvidedDeviceTimeZone() {
		val candidate = SunLocation(
			label = "gps",
			latitude = 40.7128,
			longitude = -74.0060,
			timeZoneId = null
		)

		assertEquals(
			"America/New_York",
			candidate.asGpsApiCandidate(deviceTimeZoneId = "America/New_York").timeZoneId
		)
	}

	@Test
	fun locationSnapshotUpdatesToResolvedTimeZone() {
		val snapshot = LocationSnapshot(
			latitude = 40.7128,
			longitude = -74.0060,
			timeZoneId = "Europe/Istanbul",
			label = "gps",
			accuracyMeters = 12f,
			capturedAtEpochMs = 1_000L,
			accessLevel = LocationAccessLevel.PRECISE,
			source = LocationSource.CURRENT
		)

		assertEquals("America/New_York", snapshot.withResolvedTimeZone("America/New_York").timeZoneId)
	}

	private fun withDefaultTimeZone(
		timeZoneId: String,
		block: () -> Unit
	) {
		val previous = TimeZone.getDefault()
		TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of(timeZoneId)))
		try {
			block()
		} finally {
			TimeZone.setDefault(previous)
		}
	}
}
