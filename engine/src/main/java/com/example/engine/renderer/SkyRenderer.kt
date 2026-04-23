package com.example.engine.renderer

import com.example.engine.atmosphere.AtmosphereController
import com.example.engine.celestial.CelestialCalculator
import com.example.engine.celestial.MoonController
import com.example.engine.celestial.SunController
import com.example.engine.config.WallpaperConfig
import com.example.engine.config.resolvePeakYForAtmosphere
import com.example.engine.sky.Vec2
import com.example.engine.sky.SkyColorBlender
import com.example.engine.time.TimeManager
import com.example.engine.time.DayCycleState
import com.example.engine.atmosphere.AtmosphereState

class SkyRenderer(
	private val timeManager: TimeManager,
	private val celestialCalculator: CelestialCalculator,
	private val atmosphereController: AtmosphereController
) : BaseRenderer() {

	private val sunController = SunController(celestialCalculator)
	private val moonController = MoonController(celestialCalculator)
	private val colorBlender = SkyColorBlender()
	private val scratchDayCycle = DayCycleState()
	private val scratchSun = Vec2()
	private val scratchMoon = Vec2()
	private val scratchAtmosphere = AtmosphereState()
	private val frameStateBuffers = Array(FRAME_BUFFER_COUNT) { RenderFrameState() }

	private var config: WallpaperConfig = WallpaperConfig.default()
	private var frameStateBufferIndex: Int = 0

	var latestState: RenderFrameState? = null
		private set

	fun bindConfig(value: WallpaperConfig) {
		config = value
	}

	fun renderFrame(frameTimeMillis: Long, mode: RenderMode): RenderFrameState {
		val dayCycle = timeManager.resolveDayCycle(
			atMillis = frameTimeMillis,
			sunriseMinute = config.daylight.sunriseMinute,
			sunsetMinute = config.daylight.sunsetMinute,
			timeZoneId = config.daylight.timeZoneId,
			outState = scratchDayCycle
		)
		val progress = dayCycle.progressDay
		val sun = sunController.resolve(progress, config, scratchSun)
		val moon = moonController.resolve(progress, config, scratchMoon)
		val atmosphere = atmosphereController.resolveState(
			progress = progress,
			sunY = sun.y,
			moonY = moon.y,
			config = config,
			outState = scratchAtmosphere
		)
		val finalColor = colorBlender.blend(atmosphere.skyColor, 0)
		val sunAltitude = normalizedAltitude(y = sun.y)
		val moonAltitude = normalizedAltitude(y = moon.y)
		val nightBlend = atmosphere.nightBlendFactor.coerceIn(0f, 1f)
		val flareIntensity = computeFlareIntensity(sunAltitude = sunAltitude)
		val state = nextFrameStateBuffer()
		state.frameTimeMillis = frameTimeMillis
		state.mode = mode
		state.dayProgress = progress
		state.isNight = dayCycle.isNight
		state.dayLengthMinutes = dayCycle.dayLengthMinutes
		state.nightLengthMinutes = dayCycle.nightLengthMinutes
		state.sun.set(sun.x, sun.y)
		state.moon.set(moon.x, moon.y)
		state.parallax.set(0f, 0f)
		state.skyColor = finalColor
		state.skyTopColor = atmosphere.skyTopColor
		state.skyHorizonColor = atmosphere.skyHorizonColor
		state.sunAltitude = sunAltitude
		state.moonAltitude = moonAltitude
		state.nightBlend = nightBlend
		state.preSunriseGlow = atmosphere.preSunriseGlowFactor
		state.atmosphereEnabled = config.features.atmosphereEnabled
		state.lensFlareEnabled = config.features.lensFlareEnabled
		state.starsEnabled = config.features.starsEnabled
		state.flareIntensity = flareIntensity
		state.sunriseMinute = config.daylight.sunriseMinute
		state.sunsetMinute = config.daylight.sunsetMinute
		state.stateHash = buildStateHash(
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
		var result = 17
		result = 31 * result + config.id.hashCode()
		result = 31 * result + mode.ordinal
		result = 31 * result + quantize(progress)
		result = 31 * result + quantize(sunX)
		result = 31 * result + quantize(sunY)
		result = 31 * result + quantize(moonX)
		result = 31 * result + quantize(moonY)
		result = 31 * result + quantize(nightBlend)
		result = 31 * result + quantize(flareIntensity)
		result = 31 * result + quantize(preSunriseGlow)
		result = 31 * result + config.features.atmosphereEnabled.hashCode()
		result = 31 * result + config.features.lensFlareEnabled.hashCode()
		result = 31 * result + config.features.starsEnabled.hashCode()
		result = 31 * result + skyColor
		return result
	}

	private fun normalizedAltitude(y: Float): Float {
		val horizonY = config.horizon.offset.coerceIn(0f, 1f)
		val peakY = config.resolvePeakYForAtmosphere().coerceIn(horizonY + MIN_PEAK_DELTA, 1f)
		return ((y - horizonY) / (peakY - horizonY)).coerceIn(0f, 1f)
	}

	private fun computeFlareIntensity(sunAltitude: Float): Float {
		if (!config.features.lensFlareEnabled) return 0f
		if (sunAltitude <= 0f || sunAltitude > FLARE_ALTITUDE_THRESHOLD) return 0f
		val proximity = (1f - (sunAltitude / FLARE_ALTITUDE_THRESHOLD)).coerceIn(0f, 1f)
		return (proximity * FLARE_INTENSITY_SCALE).coerceIn(0f, 1f)
	}

	private fun quantize(value: Float): Int {
		return (value * 1000f).toInt()
	}

	private fun nextFrameStateBuffer(): RenderFrameState {
		val index = frameStateBufferIndex
		frameStateBufferIndex = (frameStateBufferIndex + 1) % FRAME_BUFFER_COUNT
		return frameStateBuffers[index]
	}

	companion object {
		private const val FRAME_BUFFER_COUNT = 3
		private const val MIN_PEAK_DELTA = 0.05f
		private const val FLARE_ALTITUDE_THRESHOLD = 0.18f
		private const val FLARE_INTENSITY_SCALE = 0.9f
	}
}
