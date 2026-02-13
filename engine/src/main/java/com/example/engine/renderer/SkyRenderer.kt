package com.example.engine.renderer

import com.example.engine.atmosphere.AtmosphereController
import com.example.engine.celestial.CelestialCalculator
import com.example.engine.celestial.MoonController
import com.example.engine.celestial.SunController
import com.example.engine.config.WallpaperConfig
import com.example.engine.sky.SkyColorBlender
import com.example.engine.texture.TexturePool
import com.example.engine.time.TimeManager
import kotlin.math.abs

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
		val dayCycle = timeManager.resolveDayCycle(
			atMillis = frameTimeMillis,
			sunriseMinute = config.daylight.sunriseMinute,
			sunsetMinute = config.daylight.sunsetMinute
		)
		val progress = dayCycle.progressDay
		val sun = sunController.resolve(progress, config)
		val moon = moonController.resolve(progress, config)
		val atmosphere = atmosphereController.resolveState(
			progress = progress,
			sunY = sun.y,
			moonY = moon.y,
			config = config
		)
		val finalColor = colorBlender.blend(atmosphere.skyColor, 0)
		texturePool.touch(config.id)
		val sunAltitude = normalizedAltitude(y = sun.y)
		val moonAltitude = normalizedAltitude(y = moon.y)
		val nightBlend = atmosphere.nightBlendFactor.coerceIn(0f, 1f)
		val flareIntensity = computeFlareIntensity(sunY = sun.y)

		val state = RenderFrameState(
			frameTimeMillis = frameTimeMillis,
			mode = mode,
			dayProgress = progress,
			isNight = dayCycle.isNight,
			dayLengthMinutes = dayCycle.dayLengthMinutes,
			nightLengthMinutes = dayCycle.nightLengthMinutes,
			sun = sun,
			moon = moon,
			skyColor = finalColor,
			skyTopColor = atmosphere.skyTopColor,
			skyHorizonColor = atmosphere.skyHorizonColor,
			sunAltitude = sunAltitude,
			moonAltitude = moonAltitude,
			nightBlend = nightBlend,
			preSunriseGlow = atmosphere.preSunriseGlowFactor,
			atmosphereEnabled = config.features.atmosphereEnabled,
			lensFlareEnabled = config.features.lensFlareEnabled,
			starsEnabled = config.features.starsEnabled,
			flareIntensity = flareIntensity,
			sunriseMinute = config.daylight.sunriseMinute,
			sunsetMinute = config.daylight.sunsetMinute,
			stateHash = buildStateHash(
				mode = mode,
				progress = progress,
				sunX = sun.x,
				sunY = sun.y,
				moonX = moon.x,
				moonY = moon.y,
				skyColor = finalColor,
				nightBlend = nightBlend,
				flareIntensity = flareIntensity,
				preSunriseGlow = atmosphere.preSunriseGlowFactor
			)
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
		skyColor: Int,
		nightBlend: Float,
		flareIntensity: Float,
		preSunriseGlow: Float
	): Int {
		return listOf(
			config.id,
			mode.ordinal,
			quantize(progress),
			quantize(sunX),
			quantize(sunY),
			quantize(moonX),
			quantize(moonY),
			quantize(nightBlend),
			quantize(flareIntensity),
			quantize(preSunriseGlow),
			config.features.atmosphereEnabled,
			config.features.lensFlareEnabled,
			config.features.starsEnabled,
			skyColor
		).hashCode()
	}

	private fun normalizedAltitude(y: Float): Float {
		val horizonY = config.horizon.offset.coerceIn(0f, 1f)
		val peakY = config.peakY.coerceIn(horizonY + MIN_PEAK_DELTA, 1f)
		return ((y - horizonY) / (peakY - horizonY)).coerceIn(0f, 1f)
	}

	private fun computeFlareIntensity(sunY: Float): Float {
		if (!config.features.lensFlareEnabled) return 0f
		val horizonY = config.horizon.offset.coerceIn(0f, 1f)
		val horizonDistance = abs(sunY - horizonY)
		val proximity = (1f - (horizonDistance / FLARE_HORIZON_BAND)).coerceIn(0f, 1f)
		return (proximity * FLARE_INTENSITY_SCALE).coerceIn(0f, 1f)
	}

	private fun quantize(value: Float): Int {
		return (value * 1000f).toInt()
	}

	companion object {
		private const val MIN_PEAK_DELTA = 0.05f
		private const val FLARE_HORIZON_BAND = 0.16f
		private const val FLARE_INTENSITY_SCALE = 0.9f
	}
}
