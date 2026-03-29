package com.example.wallpaper.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FramePacingClockTest {

	@Test
	fun firstFrameRendersImmediately() {
		val clock = FramePacingClock()

		assertTrue(clock.shouldRender(frameTimeNanos = 0L, minIntervalNanos = EIGHT_HZ_INTERVAL_NS))
	}

	@Test
	fun skipsFramesBeforeDeadlineAndRendersOnNextEligibleVsync() {
		val clock = FramePacingClock()
		val startNanos = 1_000_000_000L

		assertTrue(clock.shouldRender(frameTimeNanos = startNanos, minIntervalNanos = VSYNC_120HZ_NS))
		assertFalse(
			clock.shouldRender(
				frameTimeNanos = startNanos + 4_000_000L,
				minIntervalNanos = VSYNC_120HZ_NS
			)
		)
		assertTrue(
			clock.shouldRender(
				frameTimeNanos = startNanos + VSYNC_120HZ_NS,
				minIntervalNanos = VSYNC_120HZ_NS
			)
		)
	}

	@Test
	fun catchesUpAfterLongStallWithoutTriggeringBurstFrames() {
		val clock = FramePacingClock()
		val startNanos = 500L

		assertTrue(clock.shouldRender(frameTimeNanos = startNanos, minIntervalNanos = VSYNC_60HZ_NS))
		assertTrue(
			clock.shouldRender(
				frameTimeNanos = startNanos + (VSYNC_60HZ_NS * 4L),
				minIntervalNanos = VSYNC_60HZ_NS
			)
		)
		assertFalse(
			clock.shouldRender(
				frameTimeNanos = startNanos + (VSYNC_60HZ_NS * 4L) + (VSYNC_60HZ_NS / 2L),
				minIntervalNanos = VSYNC_60HZ_NS
			)
		)
	}

	@Test
	fun intervalChangeRearmsPacingImmediately() {
		val clock = FramePacingClock()

		assertTrue(clock.shouldRender(frameTimeNanos = 0L, minIntervalNanos = VSYNC_60HZ_NS))
		assertTrue(clock.shouldRender(frameTimeNanos = 1_000_000L, minIntervalNanos = VSYNC_120HZ_NS))
	}

	companion object {
		private const val VSYNC_60HZ_NS = 16_666_667L
		private const val VSYNC_120HZ_NS = 8_333_333L
		private const val EIGHT_HZ_INTERVAL_NS = 125_000_000L
	}
}
