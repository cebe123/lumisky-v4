package com.example.lumisky.device

import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceLocationSnapshotFactoryTest {
    @Test
    fun coordinateTimeZoneIsUsedInsteadOfDeviceDefault() {
        val factory = DeviceLocationSnapshotFactory { latitude, longitude ->
            if (latitude == 40.7128 && longitude == -74.0060) "America/New_York" else null
        }

        val snapshot = factory.create(40.7128, -74.0060, 123L)

        assertEquals("America/New_York", snapshot.timeZoneId)
    }

    @Test
    fun unresolvedCoordinateUsesStableSafeZone() {
        val factory = DeviceLocationSnapshotFactory { _, _ -> null }

        val snapshot = factory.create(0.0, -160.0, 123L)

        assertEquals("Etc/UTC", snapshot.timeZoneId)
    }
}
