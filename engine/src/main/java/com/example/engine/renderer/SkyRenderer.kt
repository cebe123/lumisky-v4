package com.example.engine.renderer

import com.example.engine.atmosphere.AtmosphereController
import com.example.engine.celestial.CelestialCalculator
import com.example.engine.celestial.MoonController
import com.example.engine.celestial.SunController
import com.example.engine.config.WallpaperConfig
import com.example.engine.sky.SkyColorBlender
import com.example.engine.texture.TexturePool
import com.example.engine.time.TimeManager

class SkyRenderer(
	private val timeManager: TimeManager,
	private val celestialCalculator: CelestialCalculator,
	private val atmosphereController: AtmosphereController,
	private val texturePool: TexturePool
) : BaseRenderer() {

	private val sunController = SunController(celestialCalculator)
	private val moonController = MoonController(celestialCalculator)
	private val colorBlender = SkyColorBlender()

	private var config: WallpaperConfig = WallpaperConfig.default()

	var latestState: RenderFrameState? = null
		private set

	fun bindConfig(value: WallpaperConfig) {
		config = value
	}

	fun renderFrame(frameTimeMillis: Long, mode: RenderMode): RenderFrameState {
		val progress = timeManager.dayProgress(frameTimeMillis)
		val sun = sunController.resolve(progress, config)
		val moon = moonController.resolve(progress, config)
		val skyColor = atmosphereController.resolveSkyColor(progress, sun.y, config)
		val finalColor = colorBlender.blend(skyColor, 0)
		texturePool.touch(config.id)

		val state = RenderFrameState(
			frameTimeMillis = frameTimeMillis,
			mode = mode,
			dayProgress = progress,
			sun = sun,
			moon = moon,
			skyColor = finalColor,
			stateHash = buildStateHash(mode, progress, sun.x, sun.y, moon.x, moon.y, finalColor)
		)
		latestState = state
		return state
	}

	override fun drawFrame(frameTimeMillis: Long) {
		renderFrame(frameTimeMillis, RenderMode.PREVIEW)
	}

	private fun buildStateHash(
		mode: RenderMode,
		progress: Float,
		sunX: Float,
		sunY: Float,
		moonX: Float,
		moonY: Float,
		skyColor: Int
	): Int {
		return listOf(
			config.id,
			mode.ordinal,
			quantize(progress),
			quantize(sunX),
			quantize(sunY),
			quantize(moonX),
			quantize(moonY),
			skyColor
		).hashCode()
	}

	private fun quantize(value: Float): Int {
		return (value * 1000f).toInt()
	}
}
