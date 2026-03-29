package com.example.engine.preview

import org.junit.Assert.assertEquals
import org.junit.Test

class PreviewGlRendererFocusCatchUpTest {

	@Test
	fun daytime_wraps_to_next_day_current_time() {
		val window = resolveFocusCatchUpWindow(
			nowProgress = 13f / 24f,
			sunriseMinute = 7 * 60,
			sunsetMinute = 19 * 60
		)

		assertEquals(7f / 24f, window.startProgress, 0.0001f)
		assertEquals(1f + (13f / 24f), window.targetProgress, 0.0001f)
	}

	@Test
	fun after_sunset_stops_at_same_day_current_time() {
		val window = resolveFocusCatchUpWindow(
			nowProgress = 21f / 24f,
			sunriseMinute = 7 * 60,
			sunsetMinute = 19 * 60
		)

		assertEquals(7f / 24f, window.startProgress, 0.0001f)
		assertEquals(21f / 24f, window.targetProgress, 0.0001f)
	}

	@Test
	fun before_sunrise_wraps_night_cycle_to_current_time() {
		val window = resolveFocusCatchUpWindow(
			nowProgress = 5f / 24f,
			sunriseMinute = 7 * 60,
			sunsetMinute = 19 * 60
		)

		assertEquals(7f / 24f, window.startProgress, 0.0001f)
		assertEquals(1f + (5f / 24f), window.targetProgress, 0.0001f)
	}
}
