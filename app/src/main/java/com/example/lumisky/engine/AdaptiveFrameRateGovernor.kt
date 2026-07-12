package com.example.lumisky.engine

class AdaptiveFrameRateGovernor(
    private val steps: IntArray,
    initialFps: Int
) {
    private var index = steps.indexOf(initialFps).coerceAtLeast(0)
    private var stableFrames = 0

    val targetFps: Int get() = steps[index]

    fun report(deadlineMissed: Boolean, constrained: Boolean): Int {
        if (deadlineMissed || constrained) {
            index = (index + 1).coerceAtMost(steps.lastIndex)
            stableFrames = 0
        } else if (++stableFrames >= PROMOTE_AFTER_STABLE_FRAMES) {
            index = (index - 1).coerceAtLeast(0)
            stableFrames = 0
        }
        return targetFps
    }

    private companion object { const val PROMOTE_AFTER_STABLE_FRAMES = 120 }
}
