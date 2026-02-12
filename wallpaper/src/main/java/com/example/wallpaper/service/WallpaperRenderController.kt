package com.example.wallpaper.service

import android.os.Handler
import android.os.HandlerThread
import android.view.SurfaceHolder
import com.example.wallpaper.engine.WallpaperRenderEngine
import com.example.wallpaper.render.SceneStateHasher
import com.example.wallpaper.render.SceneStateInput
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class WallpaperRenderController(
	private val renderEngine: WallpaperRenderEngine,
	private val scheduler: MinuteTickScheduler,
	private val hasher: SceneStateHasher
) {
	private var lastStateHash: Int? = null
	private var visible: Boolean = false
	private var surfaceAttached: Boolean = false
	private var renderThread: HandlerThread? = null
	private var renderHandler: Handler? = null

	fun onCreate() {
		if (renderThread != null) return
		val thread = HandlerThread(RENDER_THREAD_NAME).apply { start() }
		renderThread = thread
		renderHandler = Handler(thread.looper)
		postRenderTask {
			renderEngine.init()
		}
	}

	fun onSurfaceCreated(holder: SurfaceHolder) {
		surfaceAttached = true
		postRenderTask {
			renderEngine.attachSurface(holder)
			if (visible) {
				renderEngine.renderFrame(force = true)
			}
		}
	}

	fun onVisibilityChanged(value: Boolean) {
		visible = value
		if (value) {
			scheduler.start { onMinuteTick() }
			if (surfaceAttached) {
				postRenderTask {
					renderEngine.renderFrame(force = true)
				}
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
		visible = false
		surfaceAttached = false
	}

	private fun onMinuteTick() {
		if (!visible || !surfaceAttached) return
		val hash = hasher.compute(
			SceneStateInput(
				minute = System.currentTimeMillis() / 60_000L,
				visible = visible,
				surfaceAttached = surfaceAttached,
				configFingerprint = renderEngine.sceneFingerprint(),
				renderMode = renderEngine.renderModeName()
			)
		)
		if (hash == lastStateHash) return
		lastStateHash = hash
		postRenderTask {
			renderEngine.renderFrame(force = false)
		}
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
