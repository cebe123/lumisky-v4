package com.example.lumisky.device

import com.example.lumisky.data.DeviceLocationSnapshot
import com.example.lumisky.data.SettingsLocationPlanner
import javax.inject.Inject

class DeviceLocationSnapshotFactory @Inject constructor(
    private val timeZoneResolver: CoordinateTimeZoneResolver
) {
    fun create(
        latitude: Double,
        longitude: Double,
        capturedAtEpochMs: Long
    ): DeviceLocationSnapshot = DeviceLocationSnapshot(
        label = SettingsLocationPlanner.formatCoordinates(latitude, longitude),
        latitude = latitude,
        longitude = longitude,
        timeZoneId = timeZoneResolver.resolve(latitude, longitude) ?: SAFE_TIME_ZONE,
        capturedAtEpochMs = capturedAtEpochMs
    )

    private companion object {
        const val SAFE_TIME_ZONE = "Etc/UTC"
    }
}
