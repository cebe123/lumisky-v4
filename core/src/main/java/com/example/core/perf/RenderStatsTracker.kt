package com.example.core.perf

import android.os.SystemClock
import com.example.core.Logger
import java.util.Locale
import kotlin.math.max

class RenderStatsTracker(
	private val tag: String,
	private val logEvery: Int
) {
	private var attempts: Long = 0
	private var draws: Long = 0
	private var skips: Long = 0

	private var windowStartMs: Long = SystemClock.elapsedRealtime()
	private var windowDraws: Long = 0
	private var windowDrawNanos: Long = 0

	@Synchronized
	fun onDraw(drawDurationNanos: Long) {
		attempts += 1
		draws += 1
		windowDraws += 1
		windowDrawNanos += drawDurationNanos
		maybeLog()
	}

	@Synchronized
	fun onSkip(reason: String) {
		attempts += 1
		skips += 1
		if (attempts % logEvery == 0L) {
			Logger.d(tag, "skip reason=$reason")
		}
		maybeLog()
	}

	@Synchronized
	fun reset() {
		attempts = 0
		draws = 0
		skips = 0
		windowStartMs = SystemClock.elapsedRealtime()
		windowDraws = 0
		windowDrawNanos = 0
	}

	@Synchronized
	private fun maybeLog() {
		if (attempts == 0L || attempts % logEvery != 0L) return

		val nowMs = SystemClock.elapsedRealtime()
		val windowElapsedMs = max(1L, nowMs - windowStartMs)
		val fps = (windowDraws * 1000f) / windowElapsedMs.toFloat()
		val avgDrawMs = if (windowDraws > 0) {
			(windowDrawNanos.toDouble() / windowDraws.toDouble()) / 1_000_000.0
		} else {
			0.0
		}
		val drawRatio = if (attempts > 0) {
			(draws.toDouble() / attempts.toDouble()) * 100.0
		} else {
			0.0
		}

		Logger.d(
			tag,
			"stats attempts=$attempts draws=$draws skips=$skips drawRatio=${
				format(drawRatio)
			}% windowFps=${format(fps.toDouble())} avgDrawMs=${format(avgDrawMs)}"
		)
		TemporaryDebugMetrics.publish(
			DebugMetricLine(
				tag = tag,
				summary = "A:$attempts D:$draws S:$skips FPS:${format(fps.toDouble())} Avg:${format(avgDrawMs)}ms",
				updatedAtMs = nowMs
			)
		)

		windowStartMs = nowMs
		windowDraws = 0
		windowDrawNanos = 0
	}

	private fun format(value: Double): String {
		return String.format(Locale.US, "%.2f", value)
	}
}
