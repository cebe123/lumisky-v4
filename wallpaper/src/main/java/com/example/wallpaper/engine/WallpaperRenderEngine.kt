package com.example.wallpaper.engine

import android.content.Context
import android.util.LruCache
import android.view.SurfaceHolder
import com.example.core.Logger
import com.example.core.perf.RenderStatsTracker
import com.example.engine.SkyEngine
import com.example.engine.config.RenderPolicy
import com.example.engine.config.ShaderProfile
import com.example.engine.config.WallpaperConfig
import com.example.engine.renderer.RenderMode
import com.example.engine.renderer.RenderFrameState
import com.example.wallpaper.render.WallpaperEglSession
import com.example.wallpaper.render.SceneStateHasher
import com.example.wallpaper.render.WallpaperSceneFingerprintHasher
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
			fragmentAssetPath = "shaders/opticalsunset/fragment.glsl",
			mode = "lighthouse_like"
		)
	)
	private var sceneFingerprintHash: Int = WallpaperSceneFingerprintHasher.compute(config)
	private var renderMode: RenderMode = RenderMode.WALLPAPER_SERVICE
	private var fragmentShaderOverride: String? = null
	private var previewLoopStartNanos: Long = 0L
	private var previewLoopConfigId: String = config.id
	@Volatile
	private var parallaxX: Float = 0f
	@Volatile
	private var parallaxY: Float = 0f
	private val textureLoadLocks = HashMap<String, Any>()
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

	@Synchronized
	fun init() {
		skyEngine.init(config)
		skyEngine.setRenderMode(renderMode)
		fragmentShaderOverride = loadFragmentShaderFor(config)
		stats.reset()
	}

	@Synchronized
	fun setConfig(value: WallpaperConfig) {
		config = value
		sceneFingerprintHash = WallpaperSceneFingerprintHasher.compute(value)
		skyEngine.setConfig(value)
		previewLoopStartNanos = 0L
		previewLoopConfigId = value.id
		fragmentShaderOverride = loadFragmentShaderFor(config)
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
					holder = null
					Logger.e(TAG, "EGL reattach failed on config change")
				}
			}
		}
	}

	@Synchronized
	fun attachSurface(surfaceHolder: SurfaceHolder): Boolean {
		val attached = eglSession.attach(
			holder = surfaceHolder,
			config = config,
			fragmentShaderOverride = fragmentShaderOverride,
			textureBytesLoader = textureBytesLoader
		)
		if (attached) {
			holder = surfaceHolder
		} else {
			holder = null
			Logger.e(TAG, "EGL attach failed")
		}
		return attached
	}

	@Synchronized
	fun detachSurface() {
		holder = null
		eglSession.detachSurface()
	}

	@Synchronized
	fun renderFrame(
		force: Boolean = false,
		previewLoop: Boolean = false,
		frameTimeNanos: Long? = null
	): RenderFrameState? {
		if (holder == null) {
			stats.onSkip("holder_null")
			return null
		}
		val frameStartNs = System.nanoTime()
		val state = if (previewLoop) {
			val now = frameTimeNanos ?: System.nanoTime()
			if (previewLoopStartNanos == 0L || previewLoopConfigId != config.id) {
				previewLoopStartNanos = now
				previewLoopConfigId = config.id
			}
			val loopDurationNs = max(
				(config.previewLoopDurationSeconds * NANOS_PER_SECOND.toFloat()).toLong(),
				MIN_PREVIEW_LOOP_NS
			)
			val elapsedNs = (now - previewLoopStartNanos).coerceAtLeast(0L)
			val loopProgress = (elapsedNs % loopDurationNs).toFloat() / loopDurationNs.toFloat()
			skyEngine.sampleAtDayProgress(
				dayProgress = loopProgress,
				mode = renderMode,
				force = true
			)
		} else {
			skyEngine.renderNow(force = force)
		}
		if (state == null) {
			stats.onSkip("engine_state_skip")
			return null
		}
		state.parallax.set(parallaxX, parallaxY)
		val drawn = eglSession.draw(state)
		if (!drawn) {
			Logger.e(TAG, "EGL draw failed")
			stats.onSkip("egl_draw_failed")
			return null
		}
		stats.onDraw(System.nanoTime() - frameStartNs)
		return state
	}

	@Synchronized
	fun computeSceneHash(
		visible: Boolean,
		surfaceAttached: Boolean,
		hasher: SceneStateHasher,
		snapshot: RenderFrameState? = null
	): Int {
		val resolvedSnapshot = snapshot ?: skyEngine.peekState(mode = renderMode)
		return hasher.compute(
			visible = visible,
			surfaceAttached = surfaceAttached,
			configFingerprintHash = sceneFingerprintHash,
			renderModeOrdinal = renderMode.ordinal,
			sunX = quantize(resolvedSnapshot?.sun?.x),
			sunY = quantize(resolvedSnapshot?.sun?.y),
			moonX = quantize(resolvedSnapshot?.moon?.x),
			moonY = quantize(resolvedSnapshot?.moon?.y),
			nightBlend = quantize(resolvedSnapshot?.nightBlend),
			skyColor = resolvedSnapshot?.skyColor ?: 0,
			flareActive = isFlareActive(resolvedSnapshot)
		)
	}

	@Synchronized
	fun sceneFingerprint(): Int {
		return sceneFingerprintHash
	}

	fun setParallaxOffset(
		x: Float,
		y: Float
	) {
		parallaxX = x.coerceIn(-1f, 1f)
		parallaxY = y.coerceIn(-1f, 1f)
	}

	fun renderModeName(): String = renderMode.name

	fun requiresContinuousRendering(): Boolean {
		return config.runtimeRenderPolicy.policy == RenderPolicy.CONTINUOUS
	}

	fun continuousFrameIntervalMs(): Long {
		return config.runtimeRenderPolicy.continuousFrameIntervalMs.coerceAtLeast(1L)
	}

	fun previewFrameIntervalNanos(displayRefreshRateHz: Int): Long {
		return displayVsyncPeriodNanos(displayRefreshRateHz)
	}

	fun continuousFrameIntervalNanos(displayRefreshRateHz: Int): Long {
		return frameIntervalNanos(
			frameIntervalMs = continuousFrameIntervalMs(),
			displayRefreshRateHz = displayRefreshRateHz
		)
	}

	fun frameIntervalNanos(
		frameIntervalMs: Long,
		displayRefreshRateHz: Int
	): Long {
		val requestedNanos = frameIntervalMs.coerceAtLeast(1L) * NANOS_PER_MILLISECOND
		return quantizeToDisplayVsync(
			requestedNanos = requestedNanos,
			displayRefreshRateHz = displayRefreshRateHz
		)
	}

	@Synchronized
	fun release() {
		holder = null
		eglSession.release()
		skyEngine.release()
		synchronized(textureBytesCache) {
			textureBytesCache.evictAll()
		}
		synchronized(textureLoadLocks) {
			textureLoadLocks.clear()
		}
		fragmentShaderOverride = null
		previewLoopStartNanos = 0L
		previewLoopConfigId = config.id
	}

	companion object {
		private const val TAG = "WallpaperRenderEngine"
		private const val QUANTIZE_SCALE = 1000f
		private const val ACTIVE_FLARE_THRESHOLD = 0.02f
		private const val MIN_PREVIEW_LOOP_NS = 1_000_000_000L
		private const val MAX_TEXTURE_CACHE_BYTES = 12 * 1024 * 1024
		private const val NANOS_PER_MILLISECOND = 1_000_000L
		private const val NANOS_PER_SECOND = 1_000_000_000L
		private const val SIXTY_HZ_INTERVAL_NS = 16_666_667L
		private const val DEFAULT_REFRESH_RATE_HZ = 60
		private const val MIN_REFRESH_RATE_HZ = 30
		private const val MAX_REFRESH_RATE_HZ = 120
	}

	private fun quantize(value: Float?): Int {
		return ((value ?: 0f) * QUANTIZE_SCALE).toInt()
	}

	private fun loadTextureBytes(assetPath: String): ByteArray? {
		synchronized(textureBytesCache) {
			textureBytesCache.get(assetPath)?.let {
				Logger.v(TAG, "texture byte cache hit path=$assetPath size=${it.size}")
				return it
			}
		}
		Logger.v(TAG, "texture byte cache miss path=$assetPath")
		return withTextureLoadLock(assetPath) {
			synchronized(textureBytesCache) {
				textureBytesCache.get(assetPath)?.let {
					Logger.v(TAG, "texture byte cache hit path=$assetPath size=${it.size}")
					return@withTextureLoadLock it
				}
			}
			val loaded = runCatching {
				appContext.assets.open(assetPath).use { it.readBytes() }
			}.getOrNull() ?: return@withTextureLoadLock null
			Logger.v(TAG, "texture asset read path=$assetPath size=${loaded.size}")
			synchronized(textureBytesCache) {
				textureBytesCache.put(assetPath, loaded)
			}
			loaded
		}
	}

	private fun withTextureLoadLock(
		assetPath: String,
		block: () -> ByteArray?
	): ByteArray? {
		val lock = synchronized(textureLoadLocks) {
			textureLoadLocks.getOrPut(assetPath) { Any() }
		}
		return synchronized(lock) {
			block()
		}
	}

	private fun loadFragmentShaderFor(config: WallpaperConfig): String? {
		return WallpaperShaderAssetLoader.loadFragment(
			context = appContext,
			assetPath = config.shader.fragmentAssetPath
		)
	}

	private fun isFlareActive(state: RenderFrameState?): Boolean {
		if (state == null) return false
		return state.lensFlareEnabled && state.flareIntensity > ACTIVE_FLARE_THRESHOLD
	}

	private fun quantizeToDisplayVsync(
		requestedNanos: Long,
		displayRefreshRateHz: Int
	): Long {
		val displayVsyncNanos = displayVsyncPeriodNanos(displayRefreshRateHz)
		if (requestedNanos <= SIXTY_HZ_INTERVAL_NS) {
			return displayVsyncNanos
		}
		val multiples = ((requestedNanos + displayVsyncNanos - 1L) / displayVsyncNanos)
			.coerceAtLeast(1L)
		return multiples * displayVsyncNanos
	}

	private fun displayVsyncPeriodNanos(displayRefreshRateHz: Int): Long {
		val refreshRate = if (displayRefreshRateHz > 0) {
			displayRefreshRateHz.coerceIn(MIN_REFRESH_RATE_HZ, MAX_REFRESH_RATE_HZ)
		} else {
			DEFAULT_REFRESH_RATE_HZ
		}
		return (NANOS_PER_SECOND / refreshRate.toLong()).coerceAtLeast(1L)
	}
}
