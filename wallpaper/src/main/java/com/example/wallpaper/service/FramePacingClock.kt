package com.example.wallpaper.service

internal class FramePacingClock(
	private val jitterAllowanceNanos: Long = DEFAULT_JITTER_ALLOWANCE_NS
) {
	private var nextFrameDeadlineNanos: Long = UNSET_FRAME_DEADLINE_NS
	private var activeIntervalNanos: Long = 0L

	fun reset() {
		nextFrameDeadlineNanos = UNSET_FRAME_DEADLINE_NS
		activeIntervalNanos = 0L
	}

	fun shouldRender(
		frameTimeNanos: Long,
		minIntervalNanos: Long,
		force: Boolean = false
	): Boolean {
		val intervalNanos = minIntervalNanos.coerceAtLeast(1L)
		if (force || nextFrameDeadlineNanos == UNSET_FRAME_DEADLINE_NS || activeIntervalNanos != intervalNanos) {
			activeIntervalNanos = intervalNanos
			nextFrameDeadlineNanos = frameTimeNanos + intervalNanos
			return true
		}
		if (frameTimeNanos + jitterAllowanceNanos < nextFrameDeadlineNanos) {
			return false
		}
		val overdueNanos = (frameTimeNanos - nextFrameDeadlineNanos).coerceAtLeast(0L)
		val skippedIntervals = (overdueNanos / activeIntervalNanos) + 1L
		nextFrameDeadlineNanos += skippedIntervals * activeIntervalNanos
		return true
	}

	companion object {
		private const val DEFAULT_JITTER_ALLOWANCE_NS = 250_000L
		private const val UNSET_FRAME_DEADLINE_NS = Long.MIN_VALUE
	}
}
