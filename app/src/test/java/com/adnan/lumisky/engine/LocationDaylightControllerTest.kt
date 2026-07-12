package com.example.lumisky.engine

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationDaylightControllerTest {

    @Test
    fun istanbulSummerSolsticeProducesUsableDaylightWindow() {
        val daylight = LocationDaylightController().resolve(
            latitude = 41.0082,
            longitude = 28.9784,
            timeZoneId = "Europe/Istanbul",
            date = LocalDate.of(2026, 6, 21)
        )

        assertTrue(daylight.sunriseMinute in 250..420)
        assertTrue(daylight.sunsetMinute in 1120..1300)
        assertTrue(daylight.solarNoonMinute in daylight.sunriseMinute..daylight.sunsetMinute)
    }

    @Test
    fun arcticSummerSolsticeIsPolarDay() {
        val daylight = LocationDaylightController().resolve(
            latitude = 78.2232,
            longitude = 15.6469,
            timeZoneId = "Arctic/Longyearbyen",
            date = LocalDate.of(2026, 6, 21)
        )

        assertEquals(DaylightMode.POLAR_DAY, daylight.mode)
        assertEquals(0, daylight.sunriseMinute)
        assertEquals(1440, daylight.sunsetMinute)
    }

    @Test
    fun arcticWinterSolsticeIsPolarNight() {
        val daylight = LocationDaylightController().resolve(
            latitude = 78.2232,
            longitude = 15.6469,
            timeZoneId = "Arctic/Longyearbyen",
            date = LocalDate.of(2026, 12, 21)
        )

        assertEquals(DaylightMode.POLAR_NIGHT, daylight.mode)
        assertEquals(1440, daylight.sunriseMinute)
        assertEquals(0, daylight.sunsetMinute)
    }
}
