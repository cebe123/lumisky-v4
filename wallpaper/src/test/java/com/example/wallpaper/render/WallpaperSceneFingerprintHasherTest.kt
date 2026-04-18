package com.example.wallpaper.render

import com.example.engine.config.WallpaperConfig
import org.junit.Assert.assertNotEquals
import org.junit.Test

class WallpaperSceneFingerprintHasherTest {

	@Test
	fun compute_changes_when_config_changes() {
		val baseline = WallpaperConfig.default(id = "baseline")
		val updated = baseline.copy(
			peakY = 0.72f,
			previewLoopDurationSeconds = 10f
		)

		val baselineHash = WallpaperSceneFingerprintHasher.compute(baseline)
		val updatedHash = WallpaperSceneFingerprintHasher.compute(updated)

		assertNotEquals(baselineHash, updatedHash)
	}
}
