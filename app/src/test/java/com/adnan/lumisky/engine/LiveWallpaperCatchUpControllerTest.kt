package com.example.lumisky.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class LiveWallpaperCatchUpControllerTest {

    @Test
    fun daytimeCatchUpStaysInsideDayModeAndTargetsCurrentTime() {
        val window = LiveWallpaperCatchUpController().resolveWindow(
            nowProgress = 0.50f,
            sunriseMinute = 360,
            sunsetMinute = 1080
        )

        assertEquals(0.375f, window.startProgress, 0.0001f)
        assertEquals(0.50f, window.targetProgress, 0.0001f)
    }

    @Test
    fun nightCatchUpAfterSunsetStartsInsideNightMode() {
        val window = LiveWallpaperCatchUpController().resolveWindow(
            nowProgress = 0.90f,
            sunriseMinute = 360,
            sunsetMinute = 1080
        )

        assertEquals(0.825f, window.startProgress, 0.0001f)
        assertEquals(0.90f, window.targetProgress, 0.0001f)
    }

    @Test
    fun nightCatchUpBeforeSunriseStaysInsideWrappedNightMode() {
        val window = LiveWallpaperCatchUpController().resolveWindow(
            nowProgress = 0.10f,
            sunriseMinute = 360,
            sunsetMinute = 1080
        )

        assertEquals(0.925f, window.startProgress, 0.0001f)
        assertEquals(1.10f, window.targetProgress, 0.0001f)
    }

    @Test
    fun equalSunriseAndSunsetUsesTheAllDayMode() {
        val window = LiveWallpaperCatchUpController().resolveWindow(
            nowProgress = 0.50f,
            sunriseMinute = 360,
            sunsetMinute = 360
        )

        assertEquals(0.375f, window.startProgress, 0.0001f)
        assertEquals(0.50f, window.targetProgress, 0.0001f)
    }
}
