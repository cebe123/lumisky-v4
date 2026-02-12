package com.example.wallpaper.engine

import android.content.Context
import android.view.SurfaceHolder
import com.example.core.Logger
import com.example.core.perf.RenderStatsTracker
import com.example.engine.SkyEngine
import com.example.engine.config.ShaderDefaults
import com.example.engine.config.ShaderProfile
import com.example.engine.config.WallpaperConfig
import com.example.engine.renderer.RenderMode
import com.example.wallpaper.render.WallpaperEglSession
import com.example.wallpaper.render.WallpaperShaderAssetLoader

class WallpaperRenderEngine(
	private val appContext: Context,
	private val skyEngine: SkyEngine = SkyEngine(),
	private val eglSession: WallpaperEglSession = WallpaperEglSession()
) {
	private var holder: SurfaceHolder? = null
	private var config: WallpaperConfig = WallpaperConfig.default(id = "wallpaper_default").copy(
		shader = ShaderProfile(
			fragmentAssetPath = ShaderDefaults.DEFAULT_FRAGMENT_ASSET_PATH,
			mode = ShaderDefaults.DEFAULT_SHADER_MODE
		)
	)
	private var renderMode: RenderMode = RenderMode.WALLPAPER_SERVICE
	private var fragmentShaderOverride: String? = null
	private val stats = RenderStatsTracker(
		tag = TAG,
		logEvery = 10
	)

	fun init() {
		skyEngine.init(config)
		skyEngine.setRenderMode(renderMode)
		fragmentShaderOverride = WallpaperShaderAssetLoader.loadFragment(
			context = appContext,
			assetPath = config.shader.fragmentAssetPath
		)
		stats.reset()
	}

	fun setConfig(value: WallpaperConfig) {
		config = value
		skyEngine.setConfig(value)
		fragmentShaderOverride = WallpaperShaderAssetLoader.loadFragment(
			context = appContext,
			assetPath = config.shader.fragmentAssetPath
		)
	}

	fun attachSurface(surfaceHolder: SurfaceHolder) {
		holder = surfaceHolder
		val attached = eglSession.attach(surfaceHolder, fragmentShaderOverride)
		if (!attached) {
			Logger.e(TAG, "EGL attach failed")
		}
	}

	fun detachSurface() {
		holder = null
		eglSession.detachSurface()
	}

	fun renderFrame(force: Boolean = false) {
		if (holder == null) {
			stats.onSkip("holder_null")
			return
		}
		val frameStartNs = System.nanoTime()
		val state = skyEngine.renderNow(force = force)
		if (state == null) {
			Logger.d(TAG, "renderFrame skipped")
			stats.onSkip("engine_state_skip")
			return
		}
		val drawn = eglSession.draw(state)
		if (!drawn) {
			Logger.e(TAG, "EGL draw failed")
			stats.onSkip("egl_draw_failed")
			return
		}
		stats.onDraw(System.nanoTime() - frameStartNs)

		Logger.d(
			TAG,
			"renderFrame color=${state.skyColor.toUInt().toString(16)} sun=(${state.sun.x}, ${state.sun.y})"
		)
	}

	fun sceneFingerprint(): String {
		return buildString {
			append(config.id)
			append('|')
			append(config.horizon.offset)
			append('|')
			append(config.celestial.sunPathType)
			append('|')
			append(config.celestial.moonPathType)
			append('|')
			append(config.daylight.sunriseMinute)
			append('|')
			append(config.daylight.sunsetMinute)
		}
	}

	fun renderModeName(): String = renderMode.name

	fun release() {
		eglSession.release()
		skyEngine.release()
	}

	companion object {
		private const val TAG = "WallpaperRenderEngine"
	}
}
