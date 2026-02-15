package com.example.lumisky.ui.debug

import android.app.Activity
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.SparseIntArray
import androidx.core.app.FrameMetricsAggregator
import com.example.core.Logger
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

object FrameJankTelemetry {
	private val trackers = ConcurrentHashMap<String, Tracker>()

	fun start(
		activity: Activity,
		label: String
	) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
		stop(label)
		val tracker = Tracker(activity = activity, label = label)
		trackers[label] = tracker
		tracker.start()
	}

	fun stop(label: String) {
		trackers.remove(label)?.stop()
	}

	private class Tracker(
		private val activity: Activity,
		private val label: String
	) {
		private val handler = Handler(Looper.getMainLooper())
		private val aggregator = FrameMetricsAggregator(FrameMetricsAggregator.TOTAL_DURATION)
		private var lastPublishedTotalFrames: Long = 0L
		private val ticker = object : Runnable {
			override fun run() {
				publishCurrent()
				handler.postDelayed(this, PUBLISH_INTERVAL_MS)
			}
		}

		fun start() {
			aggregator.add(activity)
			handler.postDelayed(ticker, PUBLISH_INTERVAL_MS)
		}

		fun stop() {
			handler.removeCallbacks(ticker)
			val removed = aggregator.remove(activity)
			publishCurrent(removed)
		}

		private fun publishCurrent(metricsArray: Array<SparseIntArray>? = aggregator.metrics) {
			val totals = computeTotals(metricsArray)
			// Suppress telemetry spam when there are no new frames since last publish.
			if (!hasNewFrames(totals.totalFrames)) return
			lastPublishedTotalFrames = totals.totalFrames
			Logger.event(
				tag = "FrameJankTelemetry",
				name = "jank_sample",
				"label" to label,
				"frames" to totals.totalFrames,
				"jankyFrames" to totals.jankyFrames,
				"jankPct" to "${format(totals.jankPercent)}%",
				"droppedFramesApprox" to totals.droppedFramesApprox,
				"droppedPerFrame" to format(totals.droppedPerFrame),
				"avgFrameMs" to format(totals.avgFrameMs)
			)
		}

		private fun computeTotals(metricsArray: Array<SparseIntArray>?): Totals {
			val totalDurations = metricsArray?.getOrNull(FrameMetricsAggregator.TOTAL_INDEX)
				?: return Totals()
			var totalFrames = 0L
			var jankyFrames = 0L
			var droppedFramesApprox = 0L
			var weightedDurationMs = 0L
			for (i in 0 until totalDurations.size()) {
				val frameDurationMs = totalDurations.keyAt(i).toLong()
				val frameCount = totalDurations.valueAt(i).toLong()
				if (frameCount <= 0L) continue
				totalFrames += frameCount
				weightedDurationMs += frameDurationMs * frameCount
				if (frameDurationMs >= JANK_FRAME_MS) {
					jankyFrames += frameCount
				}
				val droppedPerFrame = ((frameDurationMs.toDouble() / TARGET_FRAME_MS) - 1.0)
					.roundToInt()
					.coerceAtLeast(0)
				droppedFramesApprox += droppedPerFrame.toLong() * frameCount
			}
			val avgFrameMs = if (totalFrames > 0L) {
				weightedDurationMs.toDouble() / totalFrames.toDouble()
			} else {
				0.0
			}
			val jankPercent = if (totalFrames > 0L) {
				(jankyFrames.toDouble() * 100.0) / totalFrames.toDouble()
			} else {
				0.0
			}
			val droppedPerFrame = if (totalFrames > 0L) {
				droppedFramesApprox.toDouble() / totalFrames.toDouble()
			} else {
				0.0
			}
			return Totals(
				totalFrames = totalFrames,
				jankyFrames = jankyFrames,
				droppedFramesApprox = droppedFramesApprox,
				jankPercent = jankPercent,
				droppedPerFrame = droppedPerFrame,
				avgFrameMs = avgFrameMs
			)
		}

		private fun format(value: Double): String {
			return String.format(Locale.US, "%.2f", value)
		}

		private fun hasNewFrames(currentTotalFrames: Long): Boolean {
			if (currentTotalFrames <= 0L) return false
			if (currentTotalFrames < lastPublishedTotalFrames) {
				// Aggregator reset edge-case.
				lastPublishedTotalFrames = 0L
			}
			return currentTotalFrames > lastPublishedTotalFrames
		}
	}

	private data class Totals(
		val totalFrames: Long = 0L,
		val jankyFrames: Long = 0L,
		val droppedFramesApprox: Long = 0L,
		val jankPercent: Double = 0.0,
		val droppedPerFrame: Double = 0.0,
		val avgFrameMs: Double = 0.0
	)

	private const val PUBLISH_INTERVAL_MS = 1_000L
	private const val JANK_FRAME_MS = 24L
	private const val TARGET_FRAME_MS = 16.6667
}
