package com.example.wallpaper.render

import com.example.engine.config.WallpaperConfig

internal object WallpaperSceneFingerprintHasher {
	fun compute(config: WallpaperConfig): Int {
		var result = 17
		result = 31 * result + config.id.hashCode()
		result = 31 * result + config.horizon.hashCode()
		result = 31 * result + config.celestial.hashCode()
		result = 31 * result + config.features.hashCode()
		result = 31 * result + config.effects.hashCode()
		result = 31 * result + config.textures.hashCode()
		result = 31 * result + config.shader.hashCode()
		result = 31 * result + config.runtimeRenderPolicy.hashCode()
		result = 31 * result + config.capabilities.hashCode()
		result = 31 * result + config.serviceRenderPolicy.hashCode()
		result = 31 * result + config.daylight.hashCode()
		result = 31 * result + config.previewLoopDurationSeconds.toBits()
		result = 31 * result + config.focusCatchUpDurationSeconds.toBits()
		result = 31 * result + config.peakY.toBits()
		result = 31 * result + config.belowHorizonOffset.toBits()
		result = 31 * result + (config.customSkyColors?.hashCode() ?: 0)
		return result
	}
}
