package com.example.engine

import com.example.engine.atmosphere.AtmosphereController
import com.example.engine.celestial.CelestialCalculator
import com.example.engine.config.CelestialConfig
import com.example.engine.config.CelestialOrbitConfig
import com.example.engine.config.DaylightConfig
import com.example.engine.config.HorizonConfig
import com.example.engine.config.OrbitCurve
import com.example.engine.config.PathType
import com.example.engine.config.WallpaperConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CelestialCalculatorTest {

	private val calculator = CelestialCalculator()

	@Test
	fun sun_is_above_horizon_at_noon_and_below_at_midnight() {
		val config = WallpaperConfig.default().copy(
			horizon = HorizonConfig(offset = 0.30f)
		)

		val noon = calculator.computeSunPosition(progress = 0.5f, config = config)
		val midnight = calculator.computeSunPosition(progress = 0.0f, config = config)

		assertTrue(noon.y > config.horizon.offset)
		assertTrue(midnight.y < config.horizon.offset)
	}

	@Test
	fun vertical_path_keeps_constant_x() {
		val config = WallpaperConfig.default().copy(
			celestial = CelestialConfig(
				sunPathType = PathType.VERTICAL,
				moonPathType = PathType.VERTICAL
			)
		)

		val morning = calculator.computeSunPosition(progress = 0.30f, config = config)
		val evening = calculator.computeSunPosition(progress = 0.70f, config = config)

		assertEquals(0.5f, morning.x, 0.0001f)
		assertEquals(0.5f, evening.x, 0.0001f)
	}

	@Test
	fun atmosphere_is_brighter_at_noon() {
		val controller = AtmosphereController()
		val config = WallpaperConfig.default()

		val noon = controller.resolveSkyColor(progress = 0.5f, sunY = 0.9f, config = config)
		val night = controller.resolveSkyColor(progress = 0.0f, sunY = 0.1f, config = config)

		assertTrue(luminance(noon) > luminance(night))
	}

	@Test
	fun sun_reaches_peak_at_configured_solar_noon() {
		val config = WallpaperConfig.default().copy(
			horizon = HorizonConfig(offset = 0.25f),
			peakY = 0.88f,
			daylight = DaylightConfig(
				sunriseMinute = 7 * 60,
				sunsetMinute = 19 * 60,
				solarNoonMinute = 13 * 60 + 12
			)
		)

		val solarNoonProgress = (config.daylight.solarNoonMinute / (24f * 60f)).coerceIn(0f, 1f)
		val solarNoon = calculator.computeSunPosition(progress = solarNoonProgress, config = config)

		assertEquals(config.peakY, solarNoon.y, 0.0001f)
	}

	@Test
	fun moon_reaches_peak_opposite_configured_solar_noon() {
		val config = WallpaperConfig.default().copy(
			horizon = HorizonConfig(offset = 0.22f),
			peakY = 0.91f,
			daylight = DaylightConfig(
				sunriseMinute = 7 * 60,
				sunsetMinute = 19 * 60,
				solarNoonMinute = 13 * 60 + 12
			)
		)

		val moonZenithMinute = (config.daylight.solarNoonMinute + (12 * 60)) % (24 * 60)
		val moonZenithProgress = (moonZenithMinute / (24f * 60f)).coerceIn(0f, 1f)
		val moonZenith = calculator.computeMoonPosition(progress = moonZenithProgress, config = config)

		assertEquals(config.peakY, moonZenith.y, 0.0001f)
	}

	@Test
	fun custom_arc_orbit_uses_manifest_like_start_and_end_offsets() {
		val config = WallpaperConfig.default().copy(
			celestial = CelestialConfig(
				sunPathType = PathType.ARC,
				moonPathType = PathType.ARC,
				sunOrbit = CelestialOrbitConfig(
					pathType = PathType.ARC,
					startX = 0.18f,
					endX = 0.82f,
					peakY = 0.86f,
					curve = OrbitCurve.EASE_IN_OUT
				)
			),
			daylight = DaylightConfig(
				sunriseMinute = 6 * 60,
				sunsetMinute = 18 * 60,
				solarNoonMinute = 12 * 60
			)
		)

		val sunrise = calculator.computeSunPosition(progress = 0.25f, config = config)
		val sunset = calculator.computeSunPosition(progress = 0.75f, config = config)
		val solarNoon = calculator.computeSunPosition(progress = 0.5f, config = config)

		assertEquals(0.18f, sunrise.x, 0.02f)
		assertEquals(0.82f, sunset.x, 0.02f)
		assertEquals(0.86f, solarNoon.y, 0.0001f)
	}

	@Test
	fun vertical_orbit_can_be_shifted_off_center_without_new_path_type() {
		val config = WallpaperConfig.default().copy(
			celestial = CelestialConfig(
				sunPathType = PathType.VERTICAL,
				moonPathType = PathType.VERTICAL,
				sunOrbit = CelestialOrbitConfig(
					pathType = PathType.VERTICAL,
					startX = 0.68f,
					endX = 0.68f
				)
			)
		)

		val morning = calculator.computeSunPosition(progress = 0.30f, config = config)
		val evening = calculator.computeSunPosition(progress = 0.70f, config = config)

		assertEquals(0.68f, morning.x, 0.0001f)
		assertEquals(0.68f, evening.x, 0.0001f)
	}

	private fun luminance(color: Int): Int {
		val r = (color shr 16) and 0xFF
		val g = (color shr 8) and 0xFF
		val b = color and 0xFF
		return (r * 299 + g * 587 + b * 114) / 1000
	}
}
