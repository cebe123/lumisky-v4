package com.example.lumisky.ui.settings

import com.example.core.api.SunDaylight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsCelestialTimelineTest {

	@Test
	fun sun_progress_clamps_to_day_bounds() {
		val daylight = SunDaylight(
			sunriseMinute = 6 * 60 + 34,
			sunsetMinute = 19 * 60 + 51,
			solarNoonMinute = 13 * 60 + 13
		)

		val beforeSunrise = resolveCelestialTimeline(daylight = daylight, currentMinute = 5 * 60 + 45)
		val atSunrise = resolveCelestialTimeline(daylight = daylight, currentMinute = daylight.sunriseMinute)
		val atSunset = resolveCelestialTimeline(daylight = daylight, currentMinute = daylight.sunsetMinute)
		val afterSunset = resolveCelestialTimeline(daylight = daylight, currentMinute = 22 * 60)

		assertEquals(0f, beforeSunrise.sunProgress, 0.0001f)
		assertFalse(beforeSunrise.sunActive)
		assertEquals(0f, atSunrise.sunProgress, 0.0001f)
		assertTrue(atSunrise.sunActive)
		assertEquals(1f, atSunset.sunProgress, 0.0001f)
		assertTrue(atSunset.sunActive)
		assertEquals(1f, afterSunset.sunProgress, 0.0001f)
		assertFalse(afterSunset.sunActive)
	}

	@Test
	fun moon_window_covers_full_night_without_gap() {
		val daylight = SunDaylight(
			sunriseMinute = 6 * 60 + 34,
			sunsetMinute = 19 * 60 + 51,
			solarNoonMinute = 13 * 60 + 13
		)

		val justAfterSunset = resolveCelestialTimeline(daylight = daylight, currentMinute = daylight.sunsetMinute + 1)
		val middleOfNight = resolveCelestialTimeline(daylight = daylight, currentMinute = 1 * 60 + 12)
		val justBeforeSunrise = resolveCelestialTimeline(daylight = daylight, currentMinute = daylight.sunriseMinute - 1)

		assertEquals(daylight.sunsetMinute, justAfterSunset.moonriseMinute)
		assertEquals(daylight.sunriseMinute, justAfterSunset.moonsetMinute)
		assertTrue(justAfterSunset.moonActive)
		assertTrue(justAfterSunset.moonProgress < 1f)
		assertEquals(0.5008f, middleOfNight.moonProgress, 0.001f)
		assertTrue(middleOfNight.moonActive)
		assertTrue(justBeforeSunrise.moonActive)
		assertTrue(justBeforeSunrise.moonProgress > 0f)
	}

	@Test
	fun moon_window_uses_full_night_even_when_night_is_short() {
		val daylight = SunDaylight(
			sunriseMinute = 5 * 60,
			sunsetMinute = 21 * 60,
			solarNoonMinute = 13 * 60
		)

		val snapshot = resolveCelestialTimeline(daylight = daylight, currentMinute = 1 * 60)

		assertEquals(daylight.sunsetMinute, snapshot.moonriseMinute)
		assertEquals(daylight.sunriseMinute, snapshot.moonsetMinute)
		assertEquals(0.5f, snapshot.moonProgress, 0.0001f)
		assertTrue(snapshot.moonActive)
	}

	@Test
	fun markers_are_mutually_exclusive_between_day_and_night() {
		val daylight = SunDaylight(
			sunriseMinute = 6 * 60 + 34,
			sunsetMinute = 19 * 60 + 51,
			solarNoonMinute = 13 * 60 + 13
		)

		val daytime = resolveCelestialTimeline(daylight = daylight, currentMinute = 12 * 60)
		val nighttime = resolveCelestialTimeline(daylight = daylight, currentMinute = 23 * 60)

		assertTrue(daytime.sunActive)
		assertFalse(daytime.moonActive)
		assertFalse(nighttime.sunActive)
		assertTrue(nighttime.moonActive)
	}
}
