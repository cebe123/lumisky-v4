package com.example.lumisky.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class AdaptiveFrameRateGovernorTest {
    @Test
    fun degradesImmediatelyAndPromotesOnlyAfterStableWindow() {
        val governor = AdaptiveFrameRateGovernor(intArrayOf(120, 60, 30), initialFps = 60)
        assertEquals(30, governor.report(deadlineMissed = true, constrained = false))
        repeat(119) { governor.report(deadlineMissed = false, constrained = false) }
        assertEquals(30, governor.targetFps)
        assertEquals(60, governor.report(deadlineMissed = false, constrained = false))
    }
}
