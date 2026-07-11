/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Lumisky v5 Data katmanı bileşeni.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Lumisky v5 Data katmanı bileşeni.
 */
package com.example.lumisky.data

import java.util.Locale

enum class LocationLightingMode {
    MANUAL,
    DEVICE
}

data class ManualLocationPreset(
    val id: String,
    val label: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String
)

data class DeviceLocationSnapshot(
    val label: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String,
    val capturedAtEpochMs: Long
)

data class ResolvedLocationLighting(
    val label: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String,
    val usesDeviceLocation: Boolean
)

data class CelestialTimelineLabels(
    val sunriseLabel: String,
    val sunsetLabel: String,
    val solarNoonLabel: String
)

object SettingsLocationPlanner {
    const val DEVICE_LOCATION_MAX_AGE_MS = 24L * 60L * 60L * 1000L

    fun defaultManualLocation(): ManualLocationPreset = supportedManualLocations().first()

    fun supportedManualLocations(): List<ManualLocationPreset> = listOf(
        ManualLocationPreset(
            id = "istanbul",
            label = "Istanbul",
            country = "Turkey",
            latitude = 41.0082,
            longitude = 28.9784,
            timeZoneId = "Europe/Istanbul"
        ),
        ManualLocationPreset(
            id = "new_york",
            label = "New York",
            country = "United States",
            latitude = 40.7128,
            longitude = -74.0060,
            timeZoneId = "America/New_York"
        ),
        ManualLocationPreset(
            id = "tokyo",
            label = "Tokyo",
            country = "Japan",
            latitude = 35.6762,
            longitude = 139.6503,
            timeZoneId = "Asia/Tokyo"
        ),
        ManualLocationPreset(
            id = "paris",
            label = "Paris",
            country = "France",
            latitude = 48.8566,
            longitude = 2.3522,
            timeZoneId = "Europe/Paris"
        ),
        ManualLocationPreset(
            id = "lisbon",
            label = "Lisbon",
            country = "Portugal",
            latitude = 38.7223,
            longitude = -9.1393,
            timeZoneId = "Europe/Lisbon"
        )
    )

    fun resolveManualLocation(
        latitude: Double,
        longitude: Double,
        timeZoneId: String
    ): ManualLocationPreset {
        return supportedManualLocations().firstOrNull { preset ->
            preset.timeZoneId == timeZoneId &&
                nearlySame(preset.latitude, latitude) &&
                nearlySame(preset.longitude, longitude)
        } ?: ManualLocationPreset(
            id = "custom",
            label = formatCoordinates(latitude, longitude),
            country = "Manual",
            latitude = latitude.coerceIn(-89.0, 89.0),
            longitude = longitude.coerceIn(-180.0, 180.0),
            timeZoneId = timeZoneId.ifBlank { defaultManualLocation().timeZoneId }
        )
    }

    fun resolve(
        mode: LocationLightingMode,
        manualLocation: ManualLocationPreset,
        deviceSnapshot: DeviceLocationSnapshot?,
        nowEpochMs: Long
    ): ResolvedLocationLighting {
        val device = deviceSnapshot?.takeIf { snapshot ->
            snapshot.capturedAtEpochMs > 0L &&
                kotlin.math.abs(nowEpochMs - snapshot.capturedAtEpochMs) <= DEVICE_LOCATION_MAX_AGE_MS
        }
        if (mode == LocationLightingMode.DEVICE && device != null) {
            return ResolvedLocationLighting(
                label = device.label.ifBlank { formatCoordinates(device.latitude, device.longitude) },
                latitude = device.latitude.coerceIn(-89.0, 89.0),
                longitude = device.longitude.coerceIn(-180.0, 180.0),
                timeZoneId = device.timeZoneId.ifBlank { manualLocation.timeZoneId },
                usesDeviceLocation = true
            )
        }
        return ResolvedLocationLighting(
            label = manualLocation.label,
            latitude = manualLocation.latitude.coerceIn(-89.0, 89.0),
            longitude = manualLocation.longitude.coerceIn(-180.0, 180.0),
            timeZoneId = manualLocation.timeZoneId,
            usesDeviceLocation = false
        )
    }

    fun timeline(
        sunriseMinute: Int,
        sunsetMinute: Int,
        solarNoonMinute: Int
    ): CelestialTimelineLabels {
        return CelestialTimelineLabels(
            sunriseLabel = formatMinute(sunriseMinute),
            sunsetLabel = formatMinute(sunsetMinute),
            solarNoonLabel = formatMinute(solarNoonMinute)
        )
    }

    fun modeFromStorage(value: String): LocationLightingMode {
        return if (value == "DEVICE") LocationLightingMode.DEVICE else LocationLightingMode.MANUAL
    }

    fun formatCoordinates(latitude: Double, longitude: Double): String {
        return "(${String.format(Locale.US, "%.2f", latitude)}, ${String.format(Locale.US, "%.2f", longitude)})"
    }

    private fun formatMinute(minute: Int): String {
        val normalized = minute.coerceIn(0, 1439)
        return String.format(Locale.US, "%02d:%02d", normalized / 60, normalized % 60)
    }

    private fun nearlySame(left: Double, right: Double): Boolean {
        return kotlin.math.abs(left - right) < 0.0001
    }
}
