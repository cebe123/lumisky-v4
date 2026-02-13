package com.example.wallpaper.service

import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.view.SurfaceHolder
import com.example.engine.config.WallpaperConfig
import com.example.wallpaper.engine.WallpaperRenderEngine
import com.example.wallpaper.render.SceneStateHasher
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class WallpaperRenderController(
	private val renderEngine: WallpaperRenderEngine,
	private val scheduler: MinuteTickScheduler,
	private val hasher: SceneStateHasher
) {
	@Volatile
	private var lastStateHash: Int? = null
	@Volatile
	private var pendingStateHash: Int? = null

	private var visible: Boolean = false
	private var surfaceAttached: Boolean = false
	private var previewMode: Boolean = false
	private var renderThread: HandlerThread? = null
	private var renderHandler: Handler? = null
	private var pendingConfig: WallpaperConfig? = null
	private var currentConfig: WallpaperConfig? = null
	private var pendingFullRedraw: Boolean = true
	private var lastRenderElapsedMs: Long = 0L
	private val previewFrameTicker = object : Runnable {
		override fun run() {
			if (!visible || !surfaceAttached || !previewMode) return
			renderCurrentScene(force = true)
			renderHandler?.postDelayed(this, PREVIEW_FRAME_INTERVAL_MS)
		}
	}

	fun onCreate() {
		if (renderThread != null) return
		val thread = HandlerThread(RENDER_THREAD_NAME).apply { start() }
		renderThread = thread
		renderHandler = Handler(thread.looper)
		postRenderTask {
			pendingConfig?.let { config ->
				renderEngine.setConfig(config)
				pendingConfig = null
			}
			renderEngine.init()
		}
	}

	fun onSurfaceCreated(holder: SurfaceHolder) {
		surfaceAttached = true
		pendingFullRedraw = true
		postRenderTask {
			renderEngine.attachSurface(holder)
			if (visible) {
				updatePreviewLoopLocked()
				renderCurrentScene(force = true)
			}
		}
	}

	fun onVisibilityChanged(value: Boolean) {
		visible = value
		if (value) {
			if (previewMode) {
				scheduler.stop()
			} else {
				scheduler.start { onMinuteTick() }
			}
			if (surfaceAttached) {
				postRenderTask {
					updatePreviewLoopLocked()
					renderCurrentScene(force = true)
				}
			}
		} else {
			scheduler.stop()
			postRenderTask {
				stopPreviewLoopLocked()
			}
		}
	}

	fun onSurfaceDestroyed() {
		scheduler.stop()
		surfaceAttached = false
		pendingFullRedraw = true
		postRenderTask {
			stopPreviewLoopLocked()
			renderEngine.detachSurface()
		}
	}

	fun setPreviewMode(enabled: Boolean) {
		previewMode = enabled
		postRenderTask {
			updatePreviewLoopLocked()
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
		handler.post {
			renderEngine.setConfig(config)
			pendingConfig = null
			if (visible && surfaceAttached) {
				renderCurrentScene(force = true)
			}
		}
	}

	fun onDestroy() {
		scheduler.stop()
		postRenderTaskBlocking {
			stopPreviewLoopLocked()
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
		val hash = computeSceneHash()
		if (hash == lastStateHash || hash == pendingStateHash) return
		pendingStateHash = hash
		postRenderTask {
			try {
				renderCurrentScene(force = false, expectedHash = hash)
			} finally {
				if (pendingStateHash == hash) {
					pendingStateHash = null
				}
			}
		}
	}

	private fun renderCurrentScene(
		force: Boolean,
		expectedHash: Int? = null
	) {
		if (previewMode) {
			renderEngine.renderFrame(force = true, previewLoop = true)
			lastRenderElapsedMs = SystemClock.elapsedRealtime()
			pendingFullRedraw = false
			lastStateHash = computeSceneHash()
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

		renderEngine.renderFrame(force = force)
		lastStateHash = targetHash
		lastRenderElapsedMs = SystemClock.elapsedRealtime()
		pendingFullRedraw = false
		if (expectedHash == null) {
			pendingStateHash = null
		}
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

	private fun computeSceneHash(): Int {
		return hasher.compute(
			renderEngine.snapshotSceneStateInput(
				visible = visible,
				surfaceAttached = surfaceAttached
			)
		)
	}

	private fun postRenderTask(task: () -> Unit) {
		renderHandler?.post(task)
	}

	private fun updatePreviewLoopLocked() {
		if (visible && surfaceAttached && previewMode) {
			startPreviewLoopLocked()
		} else {
			stopPreviewLoopLocked()
		}
	}

	private fun startPreviewLoopLocked() {
		val handler = renderHandler ?: return
		handler.removeCallbacks(previewFrameTicker)
		handler.post(previewFrameTicker)
	}

	private fun stopPreviewLoopLocked() {
		renderHandler?.removeCallbacks(previewFrameTicker)
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
		private const val PREVIEW_FRAME_INTERVAL_MS = 16L
	}
}
