package com.example.core.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class SunTimesRepositoryTest {

	@Test
	fun refreshesWhenCandidateLocalDayChanges() {
		val firstDaylight = SunDaylight(
			sunriseMinute = 6 * 60 + 41,
			sunsetMinute = 19 * 60 + 8,
			solarNoonMinute = 12 * 60 + 54,
			timeZoneId = "America/New_York"
		)
		val secondDaylight = SunDaylight(
			sunriseMinute = 6 * 60 + 39,
			sunsetMinute = 19 * 60 + 9,
			solarNoonMinute = 12 * 60 + 54,
			timeZoneId = "America/New_York"
		)
		val apiClient = FakeSunTimesApiClient(
			responses = listOf(firstDaylight, secondDaylight)
		)
		var nowMs = Instant.parse("2026-03-23T03:00:00Z").toEpochMilli()
		val repository = SunTimesRepository(
			apiClient = apiClient,
			nowProvider = { nowMs }
		)
		val candidate = SunLocation(
			label = "new_york_gps",
			latitude = 40.7128,
			longitude = -74.0060,
			timeZoneId = "America/New_York"
		)

		try {
			assertEquals(firstDaylight, awaitRefresh(repository, candidate))

			nowMs = Instant.parse("2026-03-23T05:30:00Z").toEpochMilli()

			assertEquals(secondDaylight, awaitRefresh(repository, candidate))
			assertEquals(2, apiClient.fetchCount)
		} finally {
			repository.release()
		}
	}

	@Test
	fun doesNotReusePreviousDayLocationCacheAsFallback() {
		val cachedDaylight = SunDaylight(
			sunriseMinute = 7 * 60 + 2,
			sunsetMinute = 19 * 60 + 14,
			solarNoonMinute = 13 * 60 + 8,
			timeZoneId = "Europe/Paris"
		)
		val apiClient = FakeSunTimesApiClient(
			responses = listOf(cachedDaylight)
		)
		var nowMs = Instant.parse("2026-03-22T09:00:00Z").toEpochMilli()
		val repository = SunTimesRepository(
			apiClient = apiClient,
			nowProvider = { nowMs }
		)
		val candidate = SunLocation(
			label = "paris_manual",
			latitude = 48.8566,
			longitude = 2.3522,
			timeZoneId = "Europe/Paris"
		)

		try {
			assertEquals(cachedDaylight, awaitRefresh(repository, candidate))

			nowMs = Instant.parse("2026-03-23T09:00:00Z").toEpochMilli()

			assertEquals(SunDaylight.fallback(), repository.currentOrFallback())
			assertEquals(1, apiClient.fetchCount)
		} finally {
			repository.release()
		}
	}

	private fun awaitRefresh(
		repository: SunTimesRepository,
		candidate: SunLocation
	): SunDaylight {
		val latch = CountDownLatch(1)
		var resolved: SunDaylight? = null
		repository.refreshAsyncWithCandidates(listOf(candidate)) { daylight ->
			resolved = daylight
			latch.countDown()
		}
		assertTrue("refreshAsyncWithCandidates timed out", latch.await(2, TimeUnit.SECONDS))
		return resolved ?: error("refreshAsyncWithCandidates completed without daylight result")
	}

	private class FakeSunTimesApiClient(
		private val responses: List<SunDaylight?>
	) : SunTimesApiClient() {
		var fetchCount: Int = 0
			private set

		override fun fetchDaylight(
			latitude: Double,
			longitude: Double,
			timeZoneId: String?
		): SunDaylight? {
			val responseIndex = fetchCount
			fetchCount += 1
			return responses.getOrNull(responseIndex)
		}
	}
}
