package com.example.wallpaper.service

import android.os.Handler
import android.os.HandlerThread
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
	private var renderThread: HandlerThread? = null
	private var renderHandler: Handler? = null
	private var pendingConfig: WallpaperConfig? = null

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
		postRenderTask {
			renderEngine.attachSurface(holder)
			if (visible) {
				renderCurrentScene(force = true)
			}
		}
	}

	fun onVisibilityChanged(value: Boolean) {
		visible = value
		if (value) {
			scheduler.start { onMinuteTick() }
			if (surfaceAttached) {
				onMinuteTick()
			}
		} else {
			scheduler.stop()
		}
	}

	fun onSurfaceDestroyed() {
		scheduler.stop()
		surfaceAttached = false
		postRenderTask {
			renderEngine.detachSurface()
		}
	}

	fun setConfig(config: WallpaperConfig) {
		lastStateHash = null
		pendingStateHash = null
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
		renderEngine.renderFrame(force = force)
		lastStateHash = expectedHash ?: computeSceneHash()
		if (expectedHash == null) {
			pendingStateHash = null
		}
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
	}
}
