package com.example.wallpaper.service

import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.view.SurfaceHolder
import com.example.engine.config.WallpaperConfig
import com.example.engine.renderer.RenderFrameState
import com.example.wallpaper.engine.WallpaperRenderEngine
import com.example.wallpaper.render.SceneStateHasher
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class WallpaperRenderController(
	private val renderEngine: WallpaperRenderEngine,
	private val scheduler: MinuteTickScheduler,
	private val hasher: SceneStateHasher,
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
		if (previewMode || shouldUseContinuousRendering()) return
		postRenderTask {
			renderMinuteTickIfNeeded()
		}
	}

	private fun renderMinuteTickIfNeeded() {
		if (!visible || !surfaceAttached) return
		if (previewMode || shouldUseContinuousRendering()) return
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
		frameTimeNanos: Long? = null
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

		val targetHash = expectedHash ?: computeSceneHash()
		if (shouldSkipRender(force = force, hash = targetHash)) {
			if (expectedHash == null) {
				pendingStateHash = null
			}
			return
		}

		renderEngine.renderFrame(force = force, frameTimeNanos = frameTimeNanos) ?: return
		lastStateHash = targetHash
		lastRenderElapsedMs = SystemClock.elapsedRealtime()
		pendingFullRedraw = false
		if (expectedHash == null) {
			pendingStateHash = null
		}
	}

	private fun onVsyncFrame(frameTimeNanos: Long) {
		if (!shouldDriveVsyncLoop()) return
		val targetIntervalNanos = when {
			previewMode -> renderEngine.previewFrameIntervalNanos(displayRefreshRateHz)
			renderEngine.requiresContinuousRendering() -> renderEngine.continuousFrameIntervalNanos(displayRefreshRateHz)
			else -> return
		}
		if (!framePacingClock.shouldRender(frameTimeNanos, targetIntervalNanos)) {
			return
		}
		renderCurrentScene(
			force = true,
			frameTimeNanos = frameTimeNanos
		)
	}

	private fun shouldSkipRender(force: Boolean, hash: Int): Boolean {
		if (!force) {
			return hash == lastStateHash
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
		if (previewMode || shouldUseContinuousRendering()) {
			scheduler.stop()
		} else {
			scheduler.start { onMinuteTick() }
		}
	}

	private fun shouldUseContinuousRendering(): Boolean {
		return renderEngine.requiresContinuousRendering()
	}

	private fun updateRenderLoopsLocked() {
		if (!shouldDriveVsyncLoop()) {
			stopRenderLoopsLocked()
			return
		}
		framePacingClock.reset()
		renderThreadVsyncLoop?.postIfNeeded()
	}

	private fun shouldDriveVsyncLoop(): Boolean {
		if (!visible || !surfaceAttached) return false
		return previewMode || renderEngine.requiresContinuousRendering()
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
	}
}
