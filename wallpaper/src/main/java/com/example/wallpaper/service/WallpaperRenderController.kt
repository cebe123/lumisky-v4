package com.example.wallpaper.service

import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.view.SurfaceHolder
import com.example.core.settings.PerformanceMode
import com.example.engine.config.WallpaperConfig
import com.example.engine.renderer.RenderFrameState
import com.example.wallpaper.engine.WallpaperRenderEngine
import com.example.wallpaper.render.SceneStateHasher
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

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
	private val framePacingClock = FramePacingClock()

	fun onCreate() {
		if (renderThread != null) return
		val thread = HandlerThread(RENDER_THREAD_NAME).apply { start() }
		renderThread = thread
		renderHandler = Handler(thread.looper)
		postRenderTask {
			renderThreadVsyncLoop = RenderThreadVsyncLoop(
				onFrame = ::onVsyncFrame,
				shouldContinue = ::shouldDriveVsyncLoop
			)
			pendingConfig?.let { config ->
				renderEngine.setConfig(config)
				pendingConfig = null
			}
			renderEngine.init()
		}
	}

	fun onSurfaceCreated(holder: SurfaceHolder) {
		surfaceAttached = true
		displayRefreshRateHz = displayRefreshRateProvider()
		pendingFullRedraw = true
		updateSchedulerState()
		postRenderTask {
			renderEngine.attachSurface(holder)
			if (visible) {
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
		pendingFullRedraw = true
		updateSchedulerState()
		postRenderTask {
			stopRenderLoopsLocked()
			renderEngine.detachSurface()
		}
	}

	fun setPreviewMode(enabled: Boolean) {
		previewMode = enabled
		updateSchedulerState()
		postRenderTask {
			updateRenderLoopsLocked()
		}
	}

	fun setPerformanceMode(mode: PerformanceMode) {
		if (performanceMode == mode) return
		performanceMode = mode
		updateSchedulerState()
		postRenderTask {
			updateRenderLoopsLocked()
		}
	}

	fun setConfig(config: WallpaperConfig) {
		if (config == currentConfig) return
		currentConfig = config
		lastStateHash = null
		pendingStateHash = null
		pendingFullRedraw = true
		val handler = renderHandler
		if (handler == null) {
			pendingConfig = config
			return
		}
		updateSchedulerState()
		handler.post {
			renderEngine.setConfig(config)
			pendingConfig = null
			if (visible && surfaceAttached) {
				updateRenderLoopsLocked()
				renderCurrentScene(force = true)
			}
		}
	}

	fun onDestroy() {
		scheduler.stop()
		postRenderTaskBlocking {
			stopRenderLoopsLocked()
			renderThreadVsyncLoop = null
			renderEngine.release()
		}
		renderHandler?.removeCallbacksAndMessages(null)
		renderHandler = null

		val thread = renderThread
		if (thread != null) {
			thread.quitSafely()
			try {
				thread.join(THREAD_JOIN_TIMEOUT_MS)
			} catch (_: InterruptedException) {
				Thread.currentThread().interrupt()
			}
		}
		renderThread = null
		lastStateHash = null
		pendingStateHash = null
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
		val hash = computeSceneHash()
		if (hash == lastStateHash || hash == pendingStateHash) return
		pendingStateHash = hash
		try {
			renderCurrentScene(force = false, expectedHash = hash)
		} finally {
			if (pendingStateHash == hash) {
				pendingStateHash = null
			}
		}
	}

	private fun renderCurrentScene(
		force: Boolean,
		expectedHash: Int? = null,
		frameTimeNanos: Long? = null,
		debounceForcedRender: Boolean = true
	) {
		if (previewMode) {
			val renderedState = renderEngine.renderFrame(
				force = true,
				previewLoop = true,
				frameTimeNanos = frameTimeNanos
			) ?: return
			lastRenderElapsedMs = SystemClock.elapsedRealtime()
			pendingFullRedraw = false
			lastStateHash = computeSceneHash(renderedState)
			if (expectedHash == null) {
				pendingStateHash = null
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
				pendingStateHash = null
			}
			return
		}

		val renderedState = renderEngine.renderFrame(force = force, frameTimeNanos = frameTimeNanos) ?: return
		lastStateHash = targetHash ?: computeSceneHash(renderedState)
		lastRenderElapsedMs = SystemClock.elapsedRealtime()
		pendingFullRedraw = false
		if (expectedHash == null) {
			pendingStateHash = null
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
				} ?: 1L
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
			return hash == lastStateHash
		}
		if (!debounceForcedRender) {
			return false
		}
		if (pendingFullRedraw) {
			return false
		}
		if (hash != lastStateHash) {
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
		renderHandler?.post(task)
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
		val displayVsyncNanos = NANOS_PER_SECOND / displayRefreshRateHz
			.coerceIn(MIN_DISPLAY_REFRESH_RATE_HZ, MAX_DISPLAY_REFRESH_RATE_HZ)
			.toLong()
		val targetIntervalNanos = NANOS_PER_SECOND / targetFps
			.coerceIn(MIN_SETTINGS_FRAME_RATE_FPS, MAX_SETTINGS_FRAME_RATE_FPS)
			.toLong()
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

	private fun postRenderTaskBlocking(task: () -> Unit) {
		val handler = renderHandler ?: return
		val latch = CountDownLatch(1)
		handler.post {
			try {
				task()
			} finally {
				latch.countDown()
			}
		}
		latch.await(RELEASE_WAIT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
	}

	companion object {
		private const val RENDER_THREAD_NAME = "WallpaperRenderThread"
		private const val RELEASE_WAIT_TIMEOUT_MS = 1500L
		private const val THREAD_JOIN_TIMEOUT_MS = 1500L
		private const val FORCE_RENDER_DEBOUNCE_MS = 500L
		private const val DEFAULT_DISPLAY_REFRESH_RATE_HZ = 60
		private const val MIN_DISPLAY_REFRESH_RATE_HZ = 30
		private const val MAX_DISPLAY_REFRESH_RATE_HZ = 120
		private const val MIN_SETTINGS_FRAME_RATE_FPS = 30
		private const val MAX_SETTINGS_FRAME_RATE_FPS = 90
		private const val NANOS_PER_SECOND = 1_000_000_000L
	}
}
