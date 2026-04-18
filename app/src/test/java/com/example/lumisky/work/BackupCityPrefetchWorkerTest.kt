package com.example.lumisky.work

import com.example.core.settings.ManualCity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCityPrefetchWorkerTest {

	@Test
	fun buildBackupCityPrefetchCandidates_keepsManualCityAndDeduplicatesDefault() {
		val manualCity = ManualCity(
			id = "tr_istanbul",
			name = "Istanbul",
			countryCode = "TR",
			latitude = 41.0082,
			longitude = 28.9784,
			timeZoneId = "Europe/Istanbul"
		)

		val candidates = buildBackupCityPrefetchCandidates(
			languageTag = "en",
			manualCity = manualCity
		)

		assertTrue(candidates.isNotEmpty())
		assertEquals("Istanbul", candidates.first().label)
		assertEquals(1, candidates.count { it.latitude == manualCity.latitude && it.longitude == manualCity.longitude })
	}

	@Test
	fun buildBackupCityPrefetchCandidates_appliesCandidateLimit() {
		val manualCity = ManualCity(
			id = "us_new_york",
			name = "New York",
			countryCode = "US",
			latitude = 40.7128,
			longitude = -74.0060,
			timeZoneId = "America/New_York"
		)

		val candidates = buildBackupCityPrefetchCandidates(
			languageTag = "en",
			manualCity = manualCity,
			maxCandidateCount = 3
		)

		assertEquals(3, candidates.size)
	}
}
