package com.example.core.perf

class RenderStatsTracker(
	tag: String,
	logEvery: Int
) {
	fun onDraw(drawDurationNanos: Long) = Unit

	fun onSkip(reason: String) = Unit

	fun reset() = Unit
}
