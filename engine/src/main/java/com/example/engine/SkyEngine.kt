package com.example.engine

import com.example.core.Logger
import com.example.engine.atmosphere.AtmosphereController
import com.example.engine.celestial.CelestialCalculator
import com.example.engine.config.WallpaperConfig
import com.example.engine.renderer.FrameScheduler
import com.example.engine.renderer.RenderFrameState
import com.example.engine.renderer.RenderMode
import com.example.engine.renderer.RenderModeController
import com.example.engine.renderer.SkyRenderer
import com.example.engine.texture.TexturePool
import com.example.engine.time.TimeManager

class SkyEngine(
	private val timeManager: TimeManager = TimeManager(),
	private val celestialCalculator: CelestialCalculator = CelestialCalculator(),
	private val atmosphereController: AtmosphereController = AtmosphereController(),
	private val texturePool: TexturePool = TexturePool(),
	private val modeController: RenderModeController = RenderModeController(),
	private val frameScheduler: FrameScheduler = FrameScheduler()
) {
	private val renderer = SkyRenderer(
		timeManager = timeManager,
		celestialCalculator = celestialCalculator,
		atmosphereController = atmosphereController,
		texturePool = texturePool
	)

	private var initialized = false
	private var activeConfig: WallpaperConfig = WallpaperConfig.default()
	private var lastRenderedHash: Int? = null

	fun init(config: WallpaperConfig = activeConfig) {
		if (initialized) return
		initialized = true
		setConfig(config)
		Logger.d(TAG, "init(config=${config.id})")
	}

	fun setConfig(config: WallpaperConfig) {
		activeConfig = config
		renderer.bindConfig(config)
		lastRenderedHash = null
	}

	fun setRenderMode(mode: RenderMode) {
		modeController.switchTo(mode)
	}

	fun getRenderMode(): RenderMode = modeController.mode

	fun renderNow(
		atMillis: Long = timeManager.nowMillis(),
		force: Boolean = false
	): RenderFrameState? {
		if (!initialized) return null

		val mode = modeController.mode
		val candidate = renderer.renderFrame(atMillis, mode)
		if (!frameScheduler.shouldRender(mode, candidate.stateHash, lastRenderedHash, force)) {
			return null
		}

		lastRenderedHash = candidate.stateHash
		return candidate
	}

	fun peekState(
		atMillis: Long = timeManager.nowMillis(),
		mode: RenderMode = modeController.mode
	): RenderFrameState? {
		if (!initialized) return null
		return renderer.renderFrame(atMillis, mode)
	}

	fun sampleAtDayProgress(
		dayProgress: Float,
		mode: RenderMode = modeController.mode,
		force: Boolean = true
	): RenderFrameState? {
		if (!initialized) return null
		val previousMode = modeController.mode
		modeController.switchTo(mode)
		val atMillis = timeManager.millisFromDayProgress(dayProgress)
		val state = renderNow(atMillis = atMillis, force = force)
		modeController.switchTo(previousMode)
		return state
	}

	fun release() {
		if (!initialized) return
		Logger.d(TAG, "release(config=${activeConfig.id})")
		initialized = false
		lastRenderedHash = null
	}

	companion object {
		private const val TAG = "SkyEngine"
	}
}
