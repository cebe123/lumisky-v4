package com.example.engine

import com.example.engine.atmosphere.AtmosphereController
import com.example.engine.celestial.CelestialCalculator
import com.example.engine.config.CelestialConfig
import com.example.engine.config.HorizonConfig
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

	private fun luminance(color: Int): Int {
		val r = (color shr 16) and 0xFF
		val g = (color shr 8) and 0xFF
		val b = color and 0xFF
		return (r * 299 + g * 587 + b * 114) / 1000
	}
}
