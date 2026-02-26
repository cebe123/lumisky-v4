package com.example.wallpaper.engine

import android.content.Context
import android.util.LruCache
import android.view.SurfaceHolder
import com.example.core.Logger
import com.example.core.perf.RenderStatsTracker
import com.example.engine.SkyEngine
import com.example.engine.config.RenderPolicy
import com.example.engine.config.ShaderDefaults
import com.example.engine.config.ShaderProfile
import com.example.engine.config.WallpaperConfig
import com.example.engine.renderer.RenderMode
import com.example.engine.renderer.RenderFrameState
import com.example.wallpaper.render.WallpaperEglSession
import com.example.wallpaper.render.SceneStateInput
import com.example.wallpaper.render.WallpaperShaderAssetLoader
import kotlin.math.max

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
	private var sceneFingerprintCache: String = buildSceneFingerprint(config)
	private var renderMode: RenderMode = RenderMode.WALLPAPER_SERVICE
	private var fragmentShaderOverride: String? = null
	private var previewLoopStartMillis: Long = 0L
	private var previewLoopConfigId: String = config.id
	private val textureBytesCache = object : LruCache<String, ByteArray>(MAX_TEXTURE_CACHE_BYTES) {
		override fun sizeOf(key: String, value: ByteArray): Int = value.size
	}
	private val textureBytesLoader: (String) -> ByteArray? = { assetPath ->
		loadTextureBytes(assetPath)
	}
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
		Logger.event(
			TAG,
			"set_config",
			"id" to value.id,
			"sunrise" to value.daylight.sunriseMinute,
			"sunset" to value.daylight.sunsetMinute
		)
		config = value
		sceneFingerprintCache = buildSceneFingerprint(value)
		skyEngine.setConfig(value)
		previewLoopStartMillis = 0L
		previewLoopConfigId = value.id
		fragmentShaderOverride = WallpaperShaderAssetLoader.loadFragment(
			context = appContext,
			assetPath = config.shader.fragmentAssetPath
		)
		holder?.let { surfaceHolder ->
			val reconfigured = eglSession.reconfigure(
				config = config,
				fragmentShaderOverride = fragmentShaderOverride,
				textureBytesLoader = textureBytesLoader
			)
			if (!reconfigured) {
				Logger.w(TAG, "EGL reconfigure failed on config change, attempting reattach")
				val attached = eglSession.attach(
					holder = surfaceHolder,
					config = config,
					fragmentShaderOverride = fragmentShaderOverride,
					textureBytesLoader = textureBytesLoader
				)
				if (!attached) {
					Logger.e(TAG, "EGL reattach failed on config change")
				}
			}
		}
	}

	fun attachSurface(surfaceHolder: SurfaceHolder) {
		holder = surfaceHolder
		val attached = eglSession.attach(
			holder = surfaceHolder,
			config = config,
			fragmentShaderOverride = fragmentShaderOverride,
			textureBytesLoader = textureBytesLoader
		)
		if (!attached) {
			Logger.e(TAG, "EGL attach failed")
		}
	}

	fun detachSurface() {
		holder = null
		eglSession.detachSurface()
	}

	fun renderFrame(
		force: Boolean = false,
		previewLoop: Boolean = false
	) {
		if (holder == null) {
			stats.onSkip("holder_null")
			return
		}
		val frameStartNs = System.nanoTime()
		val state = if (previewLoop) {
			val now = System.currentTimeMillis()
			if (previewLoopStartMillis == 0L || previewLoopConfigId != config.id) {
				previewLoopStartMillis = now
				previewLoopConfigId = config.id
			}
			val loopDurationMs = max((config.previewLoopDurationSeconds * 1000f).toLong(), MIN_PREVIEW_LOOP_MS)
			val elapsedMs = (now - previewLoopStartMillis).coerceAtLeast(0L)
			val loopProgress = (elapsedMs % loopDurationMs).toFloat() / loopDurationMs.toFloat()
			skyEngine.sampleAtDayProgress(
				dayProgress = loopProgress,
				mode = renderMode,
				force = true
			)
		} else {
			skyEngine.renderNow(force = force)
		}
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

	fun snapshotSceneStateInput(
		visible: Boolean,
		surfaceAttached: Boolean
	): SceneStateInput {
		val snapshot = skyEngine.peekState(mode = renderMode)
		return SceneStateInput(
			visible = visible,
			surfaceAttached = surfaceAttached,
			configFingerprint = sceneFingerprint(),
			renderMode = renderMode.name,
			sunX = quantize(snapshot?.sun?.x),
			sunY = quantize(snapshot?.sun?.y),
			moonX = quantize(snapshot?.moon?.x),
			moonY = quantize(snapshot?.moon?.y),
			nightBlend = quantize(snapshot?.nightBlend),
			skyColor = snapshot?.skyColor ?: 0,
			flareActive = isFlareActive(snapshot)
		)
	}

	fun sceneFingerprint(): String {
		return sceneFingerprintCache
	}

	private fun buildSceneFingerprint(config: WallpaperConfig): String {
		return buildString {
			append(config.id)
			append('|')
			append(config.horizon.offset)
			append('|')
			append(config.peakY)
			append('|')
			append(config.belowHorizonOffset)
			append('|')
			append(config.celestial.sunPathType)
			append('|')
			append(config.celestial.moonPathType)
			append('|')
			append(config.features.atmosphereEnabled)
			append('|')
			append(config.features.lensFlareEnabled)
			append('|')
			append(config.features.starsEnabled)
			append('|')
			append(config.textures.sunTexture)
			append('|')
			append(config.textures.moonTexture)
			append('|')
			append(config.textures.flareTexture ?: "")
			append('|')
			append(config.textures.backgroundTexture ?: "")
			append('|')
			append(config.shader.fragmentAssetPath ?: "")
			append('|')
			append(config.shader.mode)
			append('|')
			append(config.runtimeRenderPolicy.policy)
			append('|')
			append(config.runtimeRenderPolicy.continuousFrameIntervalMs)
			append('|')
			append(config.daylight.sunriseMinute)
			append('|')
			append(config.daylight.sunsetMinute)
			append('|')
			append(config.previewLoopDurationSeconds)
			append('|')
			append(config.focusCatchUpDurationSeconds)
			append('|')
			append(config.customSkyColors?.sunriseColor ?: 0)
			append('|')
			append(config.customSkyColors?.dayColor ?: 0)
			append('|')
			append(config.customSkyColors?.sunsetColor ?: 0)
			append('|')
			append(config.customSkyColors?.nightColor ?: 0)
		}
	}

	fun renderModeName(): String = renderMode.name

	fun requiresContinuousRendering(): Boolean {
		return config.runtimeRenderPolicy.policy == RenderPolicy.CONTINUOUS
	}

	fun continuousFrameIntervalMs(): Long {
		return config.runtimeRenderPolicy.continuousFrameIntervalMs.coerceAtLeast(1L)
	}

	fun release() {
		eglSession.release()
		skyEngine.release()
		synchronized(textureBytesCache) {
			textureBytesCache.evictAll()
		}
	}

	companion object {
		private const val TAG = "WallpaperRenderEngine"
		private const val QUANTIZE_SCALE = 1000f
		private const val ACTIVE_FLARE_THRESHOLD = 0.02f
		private const val MIN_PREVIEW_LOOP_MS = 1000L
		private const val MAX_TEXTURE_CACHE_BYTES = 12 * 1024 * 1024
	}

	private fun quantize(value: Float?): Int {
		return ((value ?: 0f) * QUANTIZE_SCALE).toInt()
	}

	private fun loadTextureBytes(assetPath: String): ByteArray? {
		synchronized(textureBytesCache) {
			textureBytesCache.get(assetPath)?.let { return it }
		}
		val loaded = runCatching {
			appContext.assets.open(assetPath).use { it.readBytes() }
		}.getOrNull() ?: return null
		synchronized(textureBytesCache) {
			textureBytesCache.put(assetPath, loaded)
		}
		return loaded
	}

	private fun isFlareActive(state: RenderFrameState?): Boolean {
		if (state == null) return false
		return state.lensFlareEnabled && state.flareIntensity > ACTIVE_FLARE_THRESHOLD
	}
}
