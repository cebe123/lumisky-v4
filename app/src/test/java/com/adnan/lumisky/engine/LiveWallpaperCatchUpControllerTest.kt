package com.example.lumisky.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class LiveWallpaperCatchUpControllerTest {

    @Test
    fun daytimeCatchUpStartsAtSunriseAndTargetsCurrentTime() {
        val window = LiveWallpaperCatchUpController().resolveWindow(
            nowProgress = 0.50f,
            sunriseMinute = 360,
            sunsetMinute = 1080
        )

        assertEquals(0.25f, window.startProgress, 0.0001f)
        assertEquals(0.50f, window.targetProgress, 0.0001f)
    }

    @Test
    fun nightCatchUpAfterSunsetStartsAtMoonriseAndTargetsCurrentTime() {
        val window = LiveWallpaperCatchUpController().resolveWindow(
            nowProgress = 0.90f,
            sunriseMinute = 360,
            sunsetMinute = 1080
        )

        assertEquals(0.75f, window.startProgress, 0.0001f)
        assertEquals(0.90f, window.targetProgress, 0.0001f)
    }

    @Test
    fun nightCatchUpBeforeSunriseWrapsFromPreviousMoonrise() {
        val window = LiveWallpaperCatchUpController().resolveWindow(
            nowProgress = 0.10f,
            sunriseMinute = 360,
            sunsetMinute = 1080
        )

        assertEquals(0.75f, window.startProgress, 0.0001f)
        assertEquals(1.10f, window.targetProgress, 0.0001f)
    }
}
