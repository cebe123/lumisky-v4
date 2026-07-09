package com.adnan.lumisky.engine

import java.time.LocalDate
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
}
