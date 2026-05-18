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

	@Test
	fun resolve_day_cycle_calculates_standard_day_correctly() {
		val zoneId = ZoneId.of("Europe/Istanbul")
		// Noon local time
		val localMoment = ZonedDateTime.of(2026, 2, 28, 12, 0, 0, 0, zoneId)
		val epochMillis = localMoment.toInstant().toEpochMilli()
		val manager = TimeManager(
			timeProvider = object : TimeProvider {
				override fun nowMillis(): Long = epochMillis
			}
		)

		withDefaultTimeZone(zoneId) {
			// Sunrise at 6:00 (360 mins), Sunset at 18:00 (1080 mins)
			val cycle = manager.resolveDayCycle(
				atMillis = epochMillis,
				sunriseMinute = 360,
				sunsetMinute = 1080
			)

			assertEquals(0.5f, cycle.progressDay, 0.0001f)
			assertEquals(false, cycle.isNight)
			assertEquals(720, cycle.dayLengthMinutes)
			assertEquals(720, cycle.nightLengthMinutes)
			assertEquals(0.25f, cycle.sunriseProgress, 0.0001f)
			assertEquals(0.75f, cycle.sunsetProgress, 0.0001f)
		}
	}

	@Test
	fun resolve_day_cycle_calculates_night_for_standard_day_correctly() {
		val zoneId = ZoneId.of("Europe/Istanbul")
		// Midnight local time
		val localMoment = ZonedDateTime.of(2026, 2, 28, 0, 0, 0, 0, zoneId)
		val epochMillis = localMoment.toInstant().toEpochMilli()
		val manager = TimeManager(
			timeProvider = object : TimeProvider {
				override fun nowMillis(): Long = epochMillis
			}
		)

		withDefaultTimeZone(zoneId) {
			// Sunrise at 6:00 (360 mins), Sunset at 18:00 (1080 mins)
			val cycle = manager.resolveDayCycle(
				atMillis = epochMillis,
				sunriseMinute = 360,
				sunsetMinute = 1080
			)

			assertEquals(0.0f, cycle.progressDay, 0.0001f)
			assertEquals(true, cycle.isNight)
		}
	}

	@Test
	fun resolve_day_cycle_calculates_wrap_around_day_correctly() {
		val zoneId = ZoneId.of("Europe/Istanbul")
		// 8:00 PM (20:00) local time
		val localMoment = ZonedDateTime.of(2026, 2, 28, 20, 0, 0, 0, zoneId)
		val epochMillis = localMoment.toInstant().toEpochMilli()
		val manager = TimeManager(
			timeProvider = object : TimeProvider {
				override fun nowMillis(): Long = epochMillis
			}
		)

		withDefaultTimeZone(zoneId) {
			// Sunset < Sunrise
			// Sunrise at 18:00 (1080 mins), Sunset at 6:00 (360 mins)
			val cycle = manager.resolveDayCycle(
				atMillis = epochMillis,
				sunriseMinute = 1080,
				sunsetMinute = 360
			)

			val expectedProgress = (20 * 60) / (24f * 60f)
			assertEquals(expectedProgress, cycle.progressDay, 0.0001f)
			assertEquals(false, cycle.isNight)
			// Day length: from 18:00 to 24:00 (360) + 00:00 to 06:00 (360) = 720
			assertEquals(720, cycle.dayLengthMinutes)
			assertEquals(720, cycle.nightLengthMinutes)
			assertEquals(0.75f, cycle.sunriseProgress, 0.0001f)
			assertEquals(0.25f, cycle.sunsetProgress, 0.0001f)
		}
	}

	@Test
	fun resolve_day_cycle_calculates_night_for_wrap_around_day_correctly() {
		val zoneId = ZoneId.of("Europe/Istanbul")
		// Noon local time
		val localMoment = ZonedDateTime.of(2026, 2, 28, 12, 0, 0, 0, zoneId)
		val epochMillis = localMoment.toInstant().toEpochMilli()
		val manager = TimeManager(
			timeProvider = object : TimeProvider {
				override fun nowMillis(): Long = epochMillis
			}
		)

		withDefaultTimeZone(zoneId) {
			// Sunset < Sunrise
			// Sunrise at 18:00 (1080 mins), Sunset at 6:00 (360 mins)
			val cycle = manager.resolveDayCycle(
				atMillis = epochMillis,
				sunriseMinute = 1080,
				sunsetMinute = 360
			)

			assertEquals(0.5f, cycle.progressDay, 0.0001f)
			assertEquals(true, cycle.isNight)
		}
	}

	@Test
	fun resolve_day_cycle_handles_equal_sunrise_and_sunset() {
		val zoneId = ZoneId.of("Europe/Istanbul")
		val localMoment = ZonedDateTime.of(2026, 2, 28, 12, 0, 0, 0, zoneId)
		val epochMillis = localMoment.toInstant().toEpochMilli()
		val manager = TimeManager(
			timeProvider = object : TimeProvider {
				override fun nowMillis(): Long = epochMillis
			}
		)

		withDefaultTimeZone(zoneId) {
			// Sunrise and Sunset at 12:00 (720 mins)
			val cycle = manager.resolveDayCycle(
				atMillis = epochMillis,
				sunriseMinute = 720,
				sunsetMinute = 720
			)

			// computeDayLengthMinutes coerces to 1..1439
			// sunrise=720, sunset=720 -> duration=0 -> coerced to 1
			assertEquals(1, cycle.dayLengthMinutes)
			assertEquals(1439, cycle.nightLengthMinutes)

			// isNight when sunset >= sunrise: progress < sunrise || progress > sunset
			// progress = 0.5. sunrise = 0.5. sunset = 0.5.
			// progress < sunrise is false (0.5 < 0.5), progress > sunset is false (0.5 > 0.5)
			// wait, sunrise=0.5. sunset=0.5.
			// isNight = (0.5 < 0.5) || (0.5 > 0.5) = false
			assertEquals(false, cycle.isNight)
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
