package com.example.wallpaper.render

import org.junit.Assert.assertNotEquals
import org.junit.Test

class SceneStateHasherTest {

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
