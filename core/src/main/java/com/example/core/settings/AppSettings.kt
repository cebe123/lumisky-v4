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
	val SUPPORTED_CITIES: List<ManualCity> = listOf(
		DEFAULT_CITY,
		ManualCity("Ankara", 39.9334, 32.8597),
		ManualCity("Izmir", 38.4192, 27.1287),
		ManualCity("Bursa", 40.1885, 29.0610),
		ManualCity("Antalya", 36.8969, 30.7133),
		ManualCity("London", 51.5074, -0.1278),
		ManualCity("New York", 40.7128, -74.0060),
		ManualCity("Tokyo", 35.6762, 139.6503),
		ManualCity("Berlin", 52.5200, 13.4050),
		ManualCity("Paris", 48.8566, 2.3522)
	)
}
