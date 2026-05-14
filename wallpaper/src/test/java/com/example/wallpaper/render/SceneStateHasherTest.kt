package com.example.wallpaper.render

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SceneStateHasherTest {

	@Test
	fun compute_matches_manual_rolling_hash() {
		val hash = SceneStateHasher().compute(
			visible = true,
			surfaceAttached = false,
			configFingerprintHash = 101,
			renderModeOrdinal = 2,
			sunX = 100,
			sunY = 200,
			moonX = 300,
			moonY = 400,
			nightBlend = 500,
			skyColor = 0x123456,
			flareActive = true
		)

		var expected = 17
		expected = 31 * expected + 1
		expected = 31 * expected + 0
		expected = 31 * expected + 101
		expected = 31 * expected + 2
		expected = 31 * expected + 100
		expected = 31 * expected + 200
		expected = 31 * expected + 300
		expected = 31 * expected + 400
		expected = 31 * expected + 500
		expected = 31 * expected + 0x123456
		expected = 31 * expected + 1

		assertEquals(expected, hash)
	}

	@Test
	fun compute_changes_when_render_state_changes() {
		val hasher = SceneStateHasher()

		val baseline = hasher.compute(
			visible = true,
			surfaceAttached = true,
			configFingerprintHash = 101,
			renderModeOrdinal = 0,
			sunX = 100,
			sunY = 200,
			moonX = 300,
			moonY = 400,
			nightBlend = 500,
			skyColor = 0x123456,
			flareActive = false
		)
		val changed = hasher.compute(
			visible = true,
			surfaceAttached = true,
			configFingerprintHash = 101,
			renderModeOrdinal = 1,
			sunX = 100,
			sunY = 200,
			moonX = 300,
			moonY = 401,
			nightBlend = 500,
			skyColor = 0x123456,
			flareActive = false
		)

		assertNotEquals(baseline, changed)
	}
}
