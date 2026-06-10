package com.example.wallpaper.service

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.SystemClock
import android.view.SurfaceHolder
import com.example.core.Logger
import com.example.core.report.CrashDiagnostics
import com.example.core.settings.PerformanceMode
import com.example.engine.config.WallpaperConfig
import com.example.engine.renderer.RenderFrameState
import com.example.wallpaper.engine.WallpaperRenderEngine
import com.example.wallpaper.render.SceneStateHasher
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Coordinates live wallpaper lifecycle calls on the service thread with render work on
 * [RENDER_THREAD_NAME]. Surface/config/render state that can be observed across both threads must
 * stay volatile, atomic, or guarded before it is used to skip/execute queued render work.
 */
internal class WallpaperRenderController(
	private val renderEngine: WallpaperRenderEngine,
	private val scheduler: MinuteTickScheduler,
	private val hasher: SceneStateHasher,
	private val policyResolver: ServiceRenderPolicyResolver = ServiceRenderPolicyResolver(),
	private val displayRefreshRateProvider: () -> Int = { DEFAULT_DISPLAY_REFRESH_RATE_HZ }
) {
	@Volatile
	private var lastStateHash: Int? = null
	@Volatile
	private var pendingStateHash: Int? = null
	private val stateHashLock = Any()

	@Volatile
	private var visible: Boolean = false
	@Volatile
	private var surfaceAttached: Boolean = false
	@Volatile
	private var previewMode: Boolean = false
	@Volatile
	private var displayRefreshRateHz: Int = DEFAULT_DISPLAY_REFRESH_RATE_HZ
	@Volatile
	private var performanceMode: PerformanceMode = PerformanceMode.AUTO
	private var renderThread: HandlerThread? = null
	private var renderHandler: Handler? = null
	private var renderThreadVsyncLoop: RenderThreadVsyncLoop? = null
	private var pendingConfig: WallpaperConfig? = null
	private var currentConfig: WallpaperConfig? = null
	private var pendingFullRedraw: Boolean = true
	private var lastRenderElapsedMs: Long = 0L
	private var lastParallaxRenderElapsedMs: Long = 0L
	private val surfaceGeneration = AtomicInteger(0)
	private val framePacingClock = FramePacingClock()

	fun onCreate() {
		if (renderThread != null) {
			Logger.w(TAG, "onCreate ignored, render thread already exists")
			return
		}
		CrashDiagnostics.setCustomKey("fps_mode", performanceMode.name)
		val thread = HandlerThread(RENDER_THREAD_NAME).apply { start() }
		renderThread = thread
		renderHandler = Handler(thread.looper)
		postRenderTask {
			renderThreadVsyncLoop = RenderThreadVsyncLoop(
				onFrame = ::onVsyncFrame,
				shouldContinue = ::shouldDriveVsyncLoop
			)
			pendingConfig?.let { config ->
				Logger.d(TAG, "applying pending config on render thread id=${config.id}")
				renderEngine.setConfig(config)
				pendingConfig = null
			}
			renderEngine.init()
		}
	}

	fun onSurfaceCreated(holder: SurfaceHolder) {
		surfaceAttached = true
		val generation = surfaceGeneration.incrementAndGet()
		displayRefreshRateHz = displayRefreshRateProvider()
		pendingFullRedraw = true
		updateSchedulerState()
		postRenderTask {
			if (!isCurrentSurfaceGeneration(generation)) return@postRenderTask
			val attached = renderEngine.attachSurface(holder)
			if (!attached) {
				if (isCurrentSurfaceGeneration(generation)) {
					surfaceAttached = false
					pendingFullRedraw = true
					clearStateHashes()
					updateSchedulerState()
				}
				return@postRenderTask
			}
			if (visible && isCurrentSurfaceGeneration(generation)) {
				updateRenderLoopsLocked()
				renderCurrentScene(force = true)
			}
		}
	}

	fun onVisibilityChanged(value: Boolean) {
		visible = value
		if (value) {
			displayRefreshRateHz = displayRefreshRateProvider()
		}
		updateSchedulerState()
		if (value) {
			if (surfaceAttached) {
				postRenderTask {
					updateRenderLoopsLocked()
					renderCurrentScene(force = true)
				}
			}
		} else {
			postRenderTask {
				stopRenderLoopsLocked()
			}
		}
	}

	fun onSurfaceDestroyed() {
		surfaceAttached = false
		surfaceGeneration.incrementAndGet()
		pendingFullRedraw = true
		updateSchedulerState()
		val detached = postRenderTaskBlocking(SURFACE_DETACH_WAIT_TIMEOUT_MS) {
			stopRenderLoopsLocked()
			renderEngine.detachSurface()
		}
		if (!detached) {
			Logger.w(TAG, "surface detach did not complete within ${SURFACE_DETACH_WAIT_TIMEOUT_MS}ms")
		}
	}

	fun setPreviewMode(enabled: Boolean) {
		previewMode = enabled
		CrashDiagnostics.setCustomKey("preview_mode", enabled)
		CrashDiagnostics.log("Wallpaper preview mode: $enabled")
		updateSchedulerState()
		postRenderTask {
			updateRenderLoopsLocked()
		}
	}

	fun setPerformanceMode(mode: PerformanceMode) {
		if (performanceMode == mode) return
		performanceMode = mode
		CrashDiagnostics.setCustomKey("fps_mode", mode.name)
		CrashDiagnostics.log("FPS mode: ${mode.name}")
		updateSchedulerState()
		postRenderTask {
			updateRenderLoopsLocked()
		}
	}

	fun setParallaxOffset(
		x: Float,
		y: Float
	) {
		renderEngine.setParallaxOffset(x, y)
		if (!visible || !surfaceAttached || resolvedLoopMode() == WallpaperLoopMode.VSYNC) return
		postRenderTask {
			renderParallaxFrameIfNeeded()
		}
	}

	fun setConfig(config: WallpaperConfig) {
		if (config == currentConfig) return
		currentConfig = config
		CrashDiagnostics.setCustomKey("wallpaper", config.name)
		CrashDiagnostics.setCustomKey("wallpaper_id", config.id)
		CrashDiagnostics.log("Current wallpaper: ${config.id}")
		clearStateHashes()
		pendingFullRedraw = true
		val handler = renderHandler
		if (handler == null) {
			pendingConfig = config
			Logger.d(TAG, "setConfig queued before render handler is ready id=${config.id}")
			return
		}
		updateSchedulerState()
		val posted = handler.post {
			renderEngine.setConfig(config)
			pendingConfig = null
			if (visible && surfaceAttached) {
				updateRenderLoopsLocked()
				renderCurrentScene(force = true)
			}
		}
		if (!posted) {
			pendingConfig = config
			Logger.w(TAG, "setConfig post failed, config kept pending id=${config.id}")
		}
	}

	fun onDestroy() {
		Logger.d(TAG, "onDestroy called")
		scheduler.stop()
		surfaceGeneration.incrementAndGet()
		val released = postRenderTaskBlocking {
			Logger.d(TAG, "releasing render engine")
			stopRenderLoopsLocked()
			renderThreadVsyncLoop = null
			renderEngine.release()
		}
		if (!released) {
			Logger.w(TAG, "render engine release did not complete before timeout")
		}
		renderHandler?.removeCallbacksAndMessages(null)
		renderHandler = null

		val thread = renderThread
		if (thread != null) {
			thread.quitSafely()
			try {
				thread.join(THREAD_JOIN_TIMEOUT_MS)
				if (thread.isAlive) {
					Logger.w(TAG, "render thread did not terminate within ${THREAD_JOIN_TIMEOUT_MS}ms")
				}
			} catch (e: InterruptedException) {
				Thread.currentThread().interrupt()
				Logger.w(TAG, "interrupted while joining render thread", e)
			}
		}
		renderThread = null
		clearStateHashes()
		pendingConfig = null
		currentConfig = null
		pendingFullRedraw = true
		lastRenderElapsedMs = 0L
		visible = false
		surfaceAttached = false
	}

	private fun onMinuteTick() {
		if (!visible || !surfaceAttached) return
		if (resolvedLoopMode() != WallpaperLoopMode.MINUTE_TICK) return
		postRenderTask {
			renderMinuteTickIfNeeded()
		}
	}

	private fun renderMinuteTickIfNeeded() {
		if (!visible || !surfaceAttached) return
		if (resolvedLoopMode() != WallpaperLoopMode.MINUTE_TICK) return
		val hash = synchronized(stateHashLock) {
			val candidate = computeSceneHash()
			if (candidate == lastStateHash || candidate == pendingStateHash) return
			pendingStateHash = candidate
			candidate
		}
		try {
			renderCurrentScene(force = false, expectedHash = hash)
		} finally {
			synchronized(stateHashLock) {
				if (pendingStateHash == hash) {
					pendingStateHash = null
				}
			}
		}
	}

	private fun renderParallaxFrameIfNeeded() {
		if (!visible || !surfaceAttached) return
		if (resolvedLoopMode() == WallpaperLoopMode.VSYNC) return
		val nowElapsedMs = SystemClock.elapsedRealtime()
		if (nowElapsedMs - lastParallaxRenderElapsedMs < PARALLAX_RENDER_INTERVAL_MS) return
		lastParallaxRenderElapsedMs = nowElapsedMs
		renderCurrentScene(force = true, debounceForcedRender = false)
	}

	private fun renderCurrentScene(
		force: Boolean,
		expectedHash: Int? = null,
		frameTimeNanos: Long? = null,
		debounceForcedRender: Boolean = true
	) {
		if (!visible || !surfaceAttached) return
		if (previewMode) {
			val renderedState = renderEngine.renderFrame(
				force = true,
				previewLoop = true,
				frameTimeNanos = frameTimeNanos
			) ?: return
			lastRenderElapsedMs = SystemClock.elapsedRealtime()
			pendingFullRedraw = false
			setLastStateHash(computeSceneHash(renderedState))
			if (expectedHash == null) {
				clearPendingStateHash()
			}
			return
		}

		val shouldBypassHashSkip = force && !debounceForcedRender && expectedHash == null
		val targetHash = if (shouldBypassHashSkip) {
			null
		} else {
			expectedHash ?: computeSceneHash()
		}
		if (targetHash != null && shouldSkipRender(
				force = force,
				hash = targetHash,
				debounceForcedRender = debounceForcedRender
			)
		) {
			if (expectedHash == null) {
				clearPendingStateHash()
			}
			return
		}

		val renderedState = renderEngine.renderFrame(force = force, frameTimeNanos = frameTimeNanos) ?: return
		setLastStateHash(targetHash ?: computeSceneHash(renderedState))
		lastRenderElapsedMs = SystemClock.elapsedRealtime()
		pendingFullRedraw = false
		if (expectedHash == null) {
			clearPendingStateHash()
		}
	}

	private fun onVsyncFrame(frameTimeNanos: Long) {
		val resolvedPolicy = resolveServiceRenderPolicy()
		if (resolvedPolicy.loopMode != WallpaperLoopMode.VSYNC) return
		val targetIntervalNanos = when {
			previewMode -> renderEngine.previewFrameIntervalNanos(displayRefreshRateHz)
			resolvedPolicy.targetFrameRateFps != null -> frameIntervalNanosForTargetFps(
				targetFps = resolvedPolicy.targetFrameRateFps,
				displayRefreshRateHz = displayRefreshRateHz
			).coerceAtLeast(
				resolvedPolicy.frameIntervalMs?.let { frameIntervalMs ->
					renderEngine.frameIntervalNanos(
						frameIntervalMs = frameIntervalMs,
						displayRefreshRateHz = displayRefreshRateHz
					)
				} ?: MIN_FRAME_INTERVAL_NANOS
			)
			resolvedPolicy.frameIntervalMs != null -> renderEngine.frameIntervalNanos(
				frameIntervalMs = resolvedPolicy.frameIntervalMs,
				displayRefreshRateHz = displayRefreshRateHz
			)
			else -> renderEngine.previewFrameIntervalNanos(displayRefreshRateHz)
		}
		if (!framePacingClock.shouldRender(frameTimeNanos, targetIntervalNanos)) {
			return
		}
		renderCurrentScene(
			force = true,
			frameTimeNanos = frameTimeNanos,
			debounceForcedRender = false
		)
	}

	private fun shouldSkipRender(
		force: Boolean,
		hash: Int,
		debounceForcedRender: Boolean
	): Boolean {
		if (!force) {
			return isLastStateHash(hash)
		}
		if (!debounceForcedRender) {
			return false
		}
		if (pendingFullRedraw) {
			return false
		}
		if (!isLastStateHash(hash)) {
			return false
		}
		val elapsedSinceLastRenderMs = SystemClock.elapsedRealtime() - lastRenderElapsedMs
		return elapsedSinceLastRenderMs < FORCE_RENDER_DEBOUNCE_MS
	}

	private fun computeSceneHash(snapshot: RenderFrameState? = null): Int {
		return renderEngine.computeSceneHash(
			visible = visible,
			surfaceAttached = surfaceAttached,
			hasher = hasher,
			snapshot = snapshot
		)
	}

	private fun postRenderTask(task: () -> Unit) {
		val handler = renderHandler
		if (handler == null) {
			Logger.w(TAG, "postRenderTask dropped, render handler is null")
			return
		}
		if (!handler.post { runRenderTask(task) }) {
			Logger.w(TAG, "postRenderTask rejected by render handler")
		}
	}

	private fun runRenderTask(task: () -> Unit) {
		try {
			task()
		} catch (e: Exception) {
			CrashDiagnostics.recordException(e)
			Logger.e(TAG, "render task failed", e)
		}
	}

	private fun isCurrentSurfaceGeneration(generation: Int): Boolean {
		return surfaceAttached && surfaceGeneration.get() == generation
	}

	private fun updateSchedulerState() {
		if (!visible || !surfaceAttached) {
			scheduler.stop()
			return
		}
		when (resolvedLoopMode()) {
			WallpaperLoopMode.MINUTE_TICK -> {
				scheduler.start { onMinuteTick() }
			}
			else -> {
				scheduler.stop()
			}
		}
	}

	private fun resolvedLoopMode(): WallpaperLoopMode {
		return resolveServiceRenderPolicy().loopMode
	}

	private fun resolveServiceRenderPolicy(): ResolvedServiceRenderPolicy {
		val config = currentConfig ?: pendingConfig ?: WallpaperConfig.default(id = "wallpaper_default")
		return policyResolver.resolve(
			config = config,
			previewMode = previewMode,
			visible = visible,
			surfaceAttached = surfaceAttached,
			performanceMode = performanceMode,
			displayRefreshRateHz = displayRefreshRateHz
		)
	}

	private fun frameIntervalNanosForTargetFps(
		targetFps: Int,
		displayRefreshRateHz: Int
	): Long {
		val safeDisplayRefreshRate = displayRefreshRateHz.coerceIn(
			MIN_DISPLAY_REFRESH_RATE_HZ,
			MAX_DISPLAY_REFRESH_RATE_HZ
		)
		val displayVsyncNanos = NANOS_PER_SECOND / safeDisplayRefreshRate.toLong()
		val safeTargetFps = targetFps.coerceIn(
			MIN_SETTINGS_FRAME_RATE_FPS,
			MAX_SETTINGS_FRAME_RATE_FPS
		)
		val targetIntervalNanos = NANOS_PER_SECOND / safeTargetFps.toLong()
		return targetIntervalNanos.coerceAtLeast(displayVsyncNanos)
	}

	private fun updateRenderLoopsLocked() {
		if (resolvedLoopMode() != WallpaperLoopMode.VSYNC) {
			stopRenderLoopsLocked()
			return
		}
		framePacingClock.reset()
		renderThreadVsyncLoop?.postIfNeeded()
	}

	private fun shouldDriveVsyncLoop(): Boolean {
		return resolvedLoopMode() == WallpaperLoopMode.VSYNC
	}

	private fun stopRenderLoopsLocked() {
		renderThreadVsyncLoop?.remove()
		framePacingClock.reset()
	}

	private fun postRenderTaskBlocking(
		timeoutMs: Long = RELEASE_WAIT_TIMEOUT_MS,
		task: () -> Unit
	): Boolean {
		val handler = renderHandler
		if (handler == null) {
			Logger.w(TAG, "postRenderTaskBlocking dropped, render handler is null")
			return false
		}
		if (handler.looper == Looper.myLooper()) {
			runRenderTask(task)
			return true
		}
		val latch = CountDownLatch(1)
		val posted = handler.post {
			try {
				runRenderTask(task)
			} finally {
				latch.countDown()
			}
		}
		if (!posted) {
			Logger.w(TAG, "postRenderTaskBlocking rejected by render handler")
			return false
		}
		return try {
			val completed = latch.await(timeoutMs, TimeUnit.MILLISECONDS)
			if (!completed) {
				Logger.w(TAG, "postRenderTaskBlocking timeout after ${timeoutMs}ms")
			}
			completed
		} catch (e: InterruptedException) {
			Thread.currentThread().interrupt()
			Logger.w(TAG, "postRenderTaskBlocking interrupted", e)
			false
		}
	}

	private fun clearStateHashes() {
		synchronized(stateHashLock) {
			lastStateHash = null
			pendingStateHash = null
		}
	}

	private fun clearPendingStateHash() {
		synchronized(stateHashLock) {
			pendingStateHash = null
		}
	}

	private fun setLastStateHash(hash: Int) {
		synchronized(stateHashLock) {
			lastStateHash = hash
		}
	}

	private fun isLastStateHash(hash: Int): Boolean {
		return synchronized(stateHashLock) {
			lastStateHash == hash
		}
	}

	companion object {
		private const val TAG = "WallpaperRenderController"
		private const val RENDER_THREAD_NAME = "WallpaperRenderThread"
		private const val RELEASE_WAIT_TIMEOUT_MS = 1500L
		private const val SURFACE_DETACH_WAIT_TIMEOUT_MS = 500L
		private const val THREAD_JOIN_TIMEOUT_MS = 1500L
		private const val FORCE_RENDER_DEBOUNCE_MS = 500L
		private const val DEFAULT_DISPLAY_REFRESH_RATE_HZ = 60
		private const val MIN_DISPLAY_REFRESH_RATE_HZ = 30
		private const val MAX_DISPLAY_REFRESH_RATE_HZ = 120
		private const val MIN_SETTINGS_FRAME_RATE_FPS = 30
		private const val MAX_SETTINGS_FRAME_RATE_FPS = 90
		private const val NANOS_PER_SECOND = 1_000_000_000L
		private const val MIN_FRAME_INTERVAL_NANOS = 1L
		private const val PARALLAX_RENDER_INTERVAL_MS = 33L
	}
}
