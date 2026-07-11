package com.example.lumisky.ui.settings

import com.example.lumisky.data.DeviceLocationSnapshot
import com.example.lumisky.data.LocationLightingMode
import com.example.lumisky.data.ManualLocationPreset
import com.example.lumisky.data.SettingsLocationPlanner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsLocationPlannerTest {

    @Test
    fun deviceModeUsesFreshDeviceSnapshotWhenAvailable() {
        val manual = ManualLocationPreset(
            id = "istanbul",
            label = "Istanbul",
            country = "Turkey",
            latitude = 41.0082,
            longitude = 28.9784,
            timeZoneId = "Europe/Istanbul"
        )
        val device = DeviceLocationSnapshot(
            label = "Kadikoy",
            latitude = 40.992,
            longitude = 29.027,
            timeZoneId = "Europe/Istanbul",
            capturedAtEpochMs = 2_000L
        )

        val resolved = SettingsLocationPlanner.resolve(
            mode = LocationLightingMode.DEVICE,
            manualLocation = manual,
            deviceSnapshot = device,
            nowEpochMs = 3_000L
        )

        assertEquals("Kadikoy", resolved.label)
        assertEquals(40.992, resolved.latitude, 0.0001)
        assertTrue(resolved.usesDeviceLocation)
    }

    @Test
    fun deviceModeFallsBackToManualWhenSnapshotIsMissing() {
        val manual = SettingsLocationPlanner.defaultManualLocation()

        val resolved = SettingsLocationPlanner.resolve(
            mode = LocationLightingMode.DEVICE,
            manualLocation = manual,
            deviceSnapshot = null,
            nowEpochMs = 3_000L
        )

        assertEquals(manual.label, resolved.label)
        assertFalse(resolved.usesDeviceLocation)
    }

    @Test
    fun staleDeviceSnapshotFallsBackToManualLocation() {
        val manual = SettingsLocationPlanner.defaultManualLocation()
        val stale = DeviceLocationSnapshot(
            label = "Old location",
            latitude = 48.8566,
            longitude = 2.3522,
            timeZoneId = "Europe/Paris",
            capturedAtEpochMs = 0L
        )

        val resolved = SettingsLocationPlanner.resolve(
            mode = LocationLightingMode.DEVICE,
            manualLocation = manual,
            deviceSnapshot = stale,
            nowEpochMs = SettingsLocationPlanner.DEVICE_LOCATION_MAX_AGE_MS + 1L
        )

        assertEquals(manual.label, resolved.label)
        assertFalse(resolved.usesDeviceLocation)
    }

    @Test
    fun timelineLabelsAreFormattedAsClockValues() {
        val timeline = SettingsLocationPlanner.timeline(
            sunriseMinute = 360,
            sunsetMinute = 1110,
            solarNoonMinute = 735
        )

        assertEquals("06:00", timeline.sunriseLabel)
        assertEquals("18:30", timeline.sunsetLabel)
        assertEquals("12:15", timeline.solarNoonLabel)
    }
}
