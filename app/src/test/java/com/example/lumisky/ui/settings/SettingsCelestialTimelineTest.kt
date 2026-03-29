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
	fun moon_window_centers_on_zenith_and_moves_right_to_left() {
		val daylight = SunDaylight(
			sunriseMinute = 6 * 60 + 34,
			sunsetMinute = 19 * 60 + 51,
			solarNoonMinute = 13 * 60 + 13
		)

		val moonriseSnapshot = resolveCelestialTimeline(daylight = daylight, currentMinute = 20 * 60 + 13)
		val moonZenithSnapshot = resolveCelestialTimeline(daylight = daylight, currentMinute = 1 * 60 + 13)
		val moonsetSnapshot = resolveCelestialTimeline(daylight = daylight, currentMinute = 6 * 60 + 13)

		assertEquals(20 * 60 + 13, moonriseSnapshot.moonriseMinute)
		assertEquals(6 * 60 + 13, moonriseSnapshot.moonsetMinute)
		assertEquals(1f, moonriseSnapshot.moonProgress, 0.0001f)
		assertTrue(moonriseSnapshot.moonActive)
		assertEquals(0.5f, moonZenithSnapshot.moonProgress, 0.0001f)
		assertTrue(moonZenithSnapshot.moonActive)
		assertEquals(0f, moonsetSnapshot.moonProgress, 0.0001f)
		assertTrue(moonsetSnapshot.moonActive)
	}

	@Test
	fun short_nights_use_full_night_for_moon_window() {
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
}
