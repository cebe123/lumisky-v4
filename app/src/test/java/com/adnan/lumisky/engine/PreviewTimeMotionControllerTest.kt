package com.example.lumisky.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class PreviewTimeMotionControllerTest {

    @Test
    fun focusCatchUpStartsAtNoonAndCompletesTheV2StyleLoopAtCurrentTime() {
        val window = PreviewTimeMotionController().resolveFocusCatchUpWindow(
            nowProgress = 0.50f,
            sunriseMinute = 360,
            sunsetMinute = 1080
        )

        assertEquals(0.50f, window.startProgress, 0.0001f)
        assertEquals(1.50f, window.targetProgress, 0.0001f)
    }

    @Test
    fun focusCatchUpUsesNoonForNightFocusToo() {
        val window = PreviewTimeMotionController().resolveFocusCatchUpWindow(
            nowProgress = 0.90f,
            sunriseMinute = 360,
            sunsetMinute = 1080
        )

        assertEquals(0.50f, window.startProgress, 0.0001f)
        assertEquals(1.90f, window.targetProgress, 0.0001f)
    }

    @Test
    fun focusCatchUpWrapsBeforeSunriseToNextDayNightProgress() {
        val window = PreviewTimeMotionController().resolveFocusCatchUpWindow(
            nowProgress = 0.10f,
            sunriseMinute = 360,
            sunsetMinute = 1080
        )

        assertEquals(0.50f, window.startProgress, 0.0001f)
        assertEquals(1.10f, window.targetProgress, 0.0001f)
    }
}
