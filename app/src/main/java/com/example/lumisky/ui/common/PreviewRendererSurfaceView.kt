package com.example.lumisky.ui.common

import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Build
import android.view.View
import com.example.core.motion.TiltParallaxTracker
import com.example.engine.preview.PreviewGlRenderer

class PreviewRendererSurfaceView(
	context: Context,
	private val previewRenderer: PreviewGlRenderer,
	initialPlaybackEnabled: Boolean = true,
	private val parallaxEnabled: Boolean = true,
	private val warmupFramesOnEnable: Int = 0,
	private val requestRenderOnAttach: Boolean = false,
	private val onPlaybackStateChanged: ((enabled: Boolean, enteringEnabled: Boolean) -> Unit)? = null
) : GLSurfaceView(context) {

	private var playbackEnabled: Boolean = initialPlaybackEnabled
	private var lastRenderFrameNs: Long = 0L
	private var lastParallaxRenderNs: Long = 0L
	private var warmupFramesRemaining: Int = 0
	private val tiltParallaxTracker = TiltParallaxTracker(context) { x, y ->
		previewRenderer.setParallaxOffset(x, y)
		if (parallaxEnabled &&
			windowVisibility == View.VISIBLE &&
			!previewRenderer.shouldContinueRendering()
		) {
			requestParallaxRenderIfNeeded()
		}
	}
	private val frameLoop = ChoreographerFrameLoop(
		onFrame = { frameTimeNanos ->
			val minIntervalNs = previewRenderer.nextFrameDelayMs() * 1_000_000L
			if (frameTimeNanos - lastRenderFrameNs >= minIntervalNs) {
				requestRender()
				lastRenderFrameNs = frameTimeNanos
				if (warmupFramesRemaining > 0) {
					warmupFramesRemaining--
				}
			}
		},
		shouldContinue = { shouldScheduleFrame() }
	)

	init {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
			setSurfaceLifecycle(SURFACE_LIFECYCLE_FOLLOWS_ATTACHMENT)
		}
		setEGLContextClientVersion(2)
		setRenderer(previewRenderer)
		renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
	}

	fun setPlaybackEnabled(enabled: Boolean) {
		if (playbackEnabled == enabled) {
			if (enabled && windowVisibility == View.VISIBLE) {
				armWarmupFrames()
				frameLoop.postIfNeeded()
			}
			return
		}
		val enteringEnabled = !playbackEnabled && enabled
		playbackEnabled = enabled
		onPlaybackStateChanged?.let { callback ->
			runCatching {
				queueEvent { callback(enabled, enteringEnabled) }
			}
		}
		if (enabled && windowVisibility == View.VISIBLE) {
			armWarmupFrames()
			lastRenderFrameNs = 0L
			frameLoop.postIfNeeded()
		} else {
			warmupFramesRemaining = 0
			frameLoop.remove()
		}
	}

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		lastRenderFrameNs = 0L
		if (parallaxEnabled) {
			tiltParallaxTracker.start()
		}
		if (requestRenderOnAttach) {
			requestRender()
		}
		if (shouldScheduleFrame()) {
			frameLoop.postIfNeeded()
		}
	}

	override fun onWindowVisibilityChanged(visibility: Int) {
		super.onWindowVisibilityChanged(visibility)
		if (visibility == View.VISIBLE && shouldScheduleFrame()) {
			frameLoop.postIfNeeded()
		} else {
			frameLoop.remove()
		}
		if (!parallaxEnabled) return
		if (visibility == View.VISIBLE) {
			tiltParallaxTracker.start()
		} else {
			tiltParallaxTracker.stop()
		}
	}

	override fun onDetachedFromWindow() {
		frameLoop.remove()
		if (parallaxEnabled) {
			tiltParallaxTracker.close()
		}
		runCatching {
			queueEvent { previewRenderer.release() }
		}
		super.onDetachedFromWindow()
	}

	private fun armWarmupFrames() {
		if (warmupFramesOnEnable <= 0) return
		if (warmupFramesRemaining < warmupFramesOnEnable) {
			warmupFramesRemaining = warmupFramesOnEnable
		}
	}

	private fun shouldScheduleFrame(): Boolean {
		if (windowVisibility != View.VISIBLE || !playbackEnabled) return false
		return previewRenderer.shouldContinueRendering() || warmupFramesRemaining > 0
	}

	private fun requestParallaxRenderIfNeeded() {
		val nowNs = System.nanoTime()
		if (nowNs - lastParallaxRenderNs < PARALLAX_RENDER_INTERVAL_NS) return
		lastParallaxRenderNs = nowNs
		requestRender()
	}

	companion object {
		private const val PARALLAX_RENDER_INTERVAL_NS = 16_000_000L
	}
}
