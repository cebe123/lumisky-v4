package com.example.lumisky.engine

import com.example.lumisky.definition.CelestialDefinition
import com.example.lumisky.definition.CelestialOrbitDefinition
import com.example.lumisky.definition.DaylightDefinition
import com.example.lumisky.definition.HorizonDefinition
import com.example.lumisky.definition.WallpaperDefinition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CelestialMotionControllerTest {
    private val controller = CelestialMotionController()

    @Test
    fun resolveIntoUpdatesPreallocatedFrameState() {
        val frame = MutableRenderFrameState()

        controller.resolveInto(
            definition = pixelForestDefinition(),
            dayProgress = 0.5f,
            output = frame
        )

        assertTrue(frame.drawSun)
        assertFalse(frame.isNight)
        assertEquals(0.48f, frame.sunX, 0.001f)
        assertEquals(0.10f, frame.sunY, 0.001f)
        assertEquals(720f, frame.minute, 0.001f)
    }

    @Test
    fun verticalSunPathKeepsPixelForestFixedXAndMapsPeakToShaderCoordinates() {
        val result = controller.resolve(
            definition = pixelForestDefinition(),
            dayProgress = 0.5f
        )

        assertTrue(result.drawSun)
        assertFalse(result.isNight)
        assertEquals(0.48f, result.sunX, 0.001f)
        assertEquals(0.10f, result.sunY, 0.001f)
        assertEquals(720f, result.minute, 0.001f)
    }

    @Test
    fun verticalMoonPathUsesPixelForestMoonXAndPeaksAtMidnight() {
        val result = controller.resolve(
            definition = pixelForestDefinition(),
            dayProgress = 0.0f
        )

        assertFalse(result.drawSun)
        assertTrue(result.isNight)
        assertEquals(0.52f, result.moonX, 0.001f)
        assertEquals(0.12f, result.moonY, 0.001f)
        assertEquals(1.0f, result.nightAmount, 0.001f)
    }

    @Test
    fun daylightOverrideReplacesDefinitionDaylightForLocationAwareMotion() {
        val result = controller.resolve(
            definition = pixelForestDefinition(),
            dayProgress = 600f / 1440f,
            daylightOverride = DaylightOverride(
                sunriseMinute = 300,
                sunsetMinute = 900,
                solarNoonMinute = 600
            )
        )

        assertTrue(result.drawSun)
        assertEquals(300, result.sunriseMinute)
        assertEquals(900, result.sunsetMinute)
        assertEquals(600, result.solarNoonMinute)
        assertEquals(0.48f, result.sunX, 0.001f)
        assertEquals(0.10f, result.sunY, 0.001f)
    }

    private fun pixelForestDefinition(): WallpaperDefinition {
        return WallpaperDefinition(
            id = "pixel_forest",
            name = "Pixel Forest 4K Live",
            category = "Premium 4K Wallpapers",
            horizon = HorizonDefinition(offset = 0.45f),
            peakY = 0.9f,
            celestial = CelestialDefinition(
                sunPathType = "VERTICAL",
                moonPathType = "VERTICAL",
                sunOrbit = CelestialOrbitDefinition(
                    pathType = "VERTICAL",
                    startX = 0.48f,
                    endX = 0.48f,
                    peakY = 0.9f
                ),
                moonOrbit = CelestialOrbitDefinition(
                    pathType = "VERTICAL",
                    startX = 0.52f,
                    endX = 0.52f,
                    peakY = 0.88f
                )
            ),
            daylight = DaylightDefinition(
                sunriseMinute = 360,
                sunsetMinute = 1080,
                solarNoonMinute = 720
            )
        )
    }
}
