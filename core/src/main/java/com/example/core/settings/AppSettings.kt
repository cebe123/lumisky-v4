package com.example.core.settings

enum class AppThemeMode {
	SYSTEM,
	LIGHT,
	DARK
}

enum class LocationMode {
	GPS,
	MANUAL
}

enum class PerformanceMode {
	AUTO,
	SMOOTH,
	BATTERY
}

data class ManualCity(
	val name: String,
	val latitude: Double,
	val longitude: Double
)

data class AppSettingsSnapshot(
	val appThemeMode: AppThemeMode,
	val languageTag: String,
	val highRefreshEnabled: Boolean,
	val performanceMode: PerformanceMode,
	val locationMode: LocationMode,
	val manualCity: ManualCity
)

object AppSettingsDefaults {
	const val LANGUAGE_SYSTEM = "system"
	val DEFAULT_CITY = ManualCity(
		name = "Istanbul",
		latitude = 41.0082,
		longitude = 28.9784
	)
}
