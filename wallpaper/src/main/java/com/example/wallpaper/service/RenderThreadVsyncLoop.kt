package com.example.wallpaper.service

import android.view.Choreographer

internal class RenderThreadVsyncLoop(
	private val onFrame: (Long) -> Unit,
	private val shouldContinue: () -> Boolean
) {
	private val choreographer = Choreographer.getInstance()
	private var frameCallbackPosted: Boolean = false
	private val frameCallback = object : Choreographer.FrameCallback {
		override fun doFrame(frameTimeNanos: Long) {
			frameCallbackPosted = false
			onFrame(frameTimeNanos)
			if (shouldContinue()) {
				postIfNeeded()
			}
		}
	}

	fun postIfNeeded() {
		if (frameCallbackPosted) return
		frameCallbackPosted = true
		choreographer.postFrameCallback(frameCallback)
	}

	fun remove() {
		if (!frameCallbackPosted) return
		frameCallbackPosted = false
		choreographer.removeFrameCallback(frameCallback)
	}
}
