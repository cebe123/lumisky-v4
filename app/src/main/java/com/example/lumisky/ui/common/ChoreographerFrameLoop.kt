package com.example.lumisky.ui.common

import android.view.Choreographer

class ChoreographerFrameLoop(
	private val onFrame: (Long) -> Unit,
	private val shouldContinue: () -> Boolean
) {
	private var frameCallbackPosted: Boolean = false
	private val frameTicker = object : Choreographer.FrameCallback {
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
		Choreographer.getInstance().postFrameCallback(frameTicker)
	}

	fun remove() {
		if (!frameCallbackPosted) return
		frameCallbackPosted = false
		Choreographer.getInstance().removeFrameCallback(frameTicker)
	}
}
