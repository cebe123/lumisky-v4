package com.example.engine

import com.example.core.TimeProvider
import com.example.engine.time.TimeManager
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.TimeZone

class TimeManagerTest {

	@Test
	fun day_progress_and_sampling_follow_local_time_zone() {
		val zoneId = ZoneId.of("Europe/Istanbul")
		val localMoment = ZonedDateTime.of(2026, 2, 28, 13, 26, 0, 0, zoneId)
		val epochMillis = localMoment.toInstant().toEpochMilli()
		val manager = TimeManager(
			timeProvider = object : TimeProvider {
				override fun nowMillis(): Long = epochMillis
			}
		)

		withDefaultTimeZone(zoneId) {
			val expectedProgress = ((13 * 60) + 26) / (24f * 60f)
			assertEquals(expectedProgress, manager.dayProgress(epochMillis), 0.0001f)

			val sampledMillis = manager.millisFromDayProgress(expectedProgress)
			val sampledLocalTime = Instant.ofEpochMilli(sampledMillis)
				.atZone(zoneId)
				.toLocalTime()

			assertEquals(13, sampledLocalTime.hour)
			assertEquals(26, sampledLocalTime.minute)
		}
	}

	@Test
	fun explicit_render_time_zone_overrides_device_time_zone() {
		val deviceZone = ZoneId.of("Europe/Istanbul")
		val renderZone = ZoneId.of("Europe/Paris")
		val localMoment = ZonedDateTime.of(2026, 2, 28, 15, 16, 0, 0, deviceZone)
		val epochMillis = localMoment.toInstant().toEpochMilli()
		val manager = TimeManager(
			timeProvider = object : TimeProvider {
				override fun nowMillis(): Long = epochMillis
			}
		)

		withDefaultTimeZone(deviceZone) {
			val expectedProgress = ((13 * 60) + 16) / (24f * 60f)
			assertEquals(
				expectedProgress,
				manager.dayProgress(epochMillis, timeZoneId = renderZone.id),
				0.0001f
			)

			val sampledMillis = manager.millisFromDayProgress(
				progress = expectedProgress,
				timeZoneId = renderZone.id
			)
			val sampledRenderTime = Instant.ofEpochMilli(sampledMillis)
				.atZone(renderZone)
				.toLocalTime()

			assertEquals(13, sampledRenderTime.hour)
			assertEquals(16, sampledRenderTime.minute)
		}
	}

	private fun withDefaultTimeZone(zoneId: ZoneId, block: () -> Unit) {
		val previous = TimeZone.getDefault()
		TimeZone.setDefault(TimeZone.getTimeZone(zoneId))
		try {
			block()
		} finally {
			TimeZone.setDefault(previous)
		}
	}
}
