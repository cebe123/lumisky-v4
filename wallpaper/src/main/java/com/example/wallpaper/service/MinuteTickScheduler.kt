package com.example.wallpaper.service

import android.os.Handler
import android.os.Looper
import com.example.core.Logger
import kotlin.math.max

class MinuteTickScheduler(
	private val handler: Handler = Handler(Looper.getMainLooper()),
	private val nowProvider: () -> Long = { System.currentTimeMillis() }
) {
	private var callback: (() -> Unit)? = null
	private var started: Boolean = false

	private val runnable = object : Runnable {
		override fun run() {
			val currentCallback = callback ?: run {
				started = false
				return
			}
			currentCallback.invoke()
			if (!started) return
			postAtNextMinuteBoundary()
		}
	}

	fun start(onTick: () -> Unit) {
		callback = onTick
		if (started) return
		started = true
		postAtNextMinuteBoundary()
	}

	fun stop() {
		if (!started && callback == null) return
		started = false
		handler.removeCallbacks(runnable)
		callback = null
	}

	private fun postAtNextMinuteBoundary() {
		val now = nowProvider()
		val msIntoMinute = now % ONE_MINUTE_MS
		val delay = max(1L, ONE_MINUTE_MS - msIntoMinute)
		handler.postDelayed(runnable, delay)
		Logger.d("MinuteTickScheduler", "nextTickInMs=$delay")
	}

	companion object {
		private const val ONE_MINUTE_MS = 60_000L
	}
}
