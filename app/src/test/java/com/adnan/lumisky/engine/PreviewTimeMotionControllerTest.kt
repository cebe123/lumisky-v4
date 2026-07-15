package com.example.lumisky.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class PreviewTimeMotionControllerTest {

    @Test
    fun daytimeFocusStartsAtConfiguredSunriseAndWrapsToCurrentTime() {
        val window = PreviewTimeMotionController().resolveFocusCatchUpWindow(
            nowProgress = 0.50f,
            sunriseMinute = 360,
            sunsetMinute = 1080
        )

        assertEquals(0.25f, window.startProgress, 0.0001f)
        assertEquals(1.50f, window.targetProgress, 0.0001f)
    }

    @Test
    fun afterSunsetFocusStartsAtConfiguredSunriseAndStopsAtCurrentTime() {
        val window = PreviewTimeMotionController().resolveFocusCatchUpWindow(
            nowProgress = 0.90f,
            sunriseMinute = 360,
            sunsetMinute = 1080
        )

        assertEquals(0.25f, window.startProgress, 0.0001f)
        assertEquals(0.90f, window.targetProgress, 0.0001f)
    }

    @Test
    fun focusCatchUpWrapsBeforeSunriseToNextDayNightProgress() {
        val window = PreviewTimeMotionController().resolveFocusCatchUpWindow(
            nowProgress = 0.10f,
            sunriseMinute = 360,
            sunsetMinute = 1080
        )

        assertEquals(0.25f, window.startProgress, 0.0001f)
        assertEquals(1.10f, window.targetProgress, 0.0001f)
    }

    @Test
    fun catalogFocusDurationMatchesV2() {
        assertEquals(4f, PreviewTimeMotionController.CATALOG_FOCUS_DURATION_SECONDS, 0.0001f)
    }

    @Test
    fun fullscreenPickerRunsAContinuousEightSecondFullDayLoop() {
        val controller = PreviewTimeMotionController()
        controller.startFullDayLoop(wallpaperId = null)

        assertEquals(0f, controller.resolveDayProgress(null, 0f), 0.0001f)
        assertEquals(0.25f, controller.resolveDayProgress(null, 2f), 0.0001f)
        assertEquals(0f, controller.resolveDayProgress(null, 6f), 0.0001f)
        assertEquals(true, controller.isAnimating)
    }
}
