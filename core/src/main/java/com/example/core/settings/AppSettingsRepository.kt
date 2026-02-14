package com.example.core.settings

import android.content.Context

class AppSettingsRepository(
	context: Context
) {
	private val appContext = context.applicationContext
	private val prefs by lazy {
		appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
	}

	fun snapshot(): AppSettingsSnapshot {
		return AppSettingsSnapshot(
			appThemeMode = getAppThemeMode(),
			languageTag = getLanguageTag(),
			highRefreshEnabled = isHighRefreshEnabled(),
			performanceMode = getPerformanceMode(),
			locationMode = getLocationMode(),
			manualCity = getManualCity()
		)
	}

	fun getAppThemeMode(): AppThemeMode {
		val raw = prefs.getString(KEY_APP_THEME_MODE, AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
		return runCatching { AppThemeMode.valueOf(raw) }.getOrElse { AppThemeMode.SYSTEM }
	}

	fun setAppThemeMode(mode: AppThemeMode) {
		prefs.edit()
			.putString(KEY_APP_THEME_MODE, mode.name)
			.apply()
	}

	fun getLanguageTag(): String {
		return prefs.getString(KEY_LANGUAGE_TAG, AppSettingsDefaults.LANGUAGE_SYSTEM)
			?: AppSettingsDefaults.LANGUAGE_SYSTEM
	}

	fun setLanguageTag(tag: String) {
		val normalized = tag.ifBlank { AppSettingsDefaults.LANGUAGE_SYSTEM }
		prefs.edit()
			.putString(KEY_LANGUAGE_TAG, normalized)
			.apply()
	}

	fun isHighRefreshEnabled(): Boolean {
		return prefs.getBoolean(KEY_HIGH_REFRESH_ENABLED, DEFAULT_HIGH_REFRESH_ENABLED)
	}

	fun setHighRefreshEnabled(enabled: Boolean) {
		prefs.edit()
			.putBoolean(KEY_HIGH_REFRESH_ENABLED, enabled)
			.apply()
	}

	fun getPerformanceMode(): PerformanceMode {
		val raw = prefs.getString(KEY_PERFORMANCE_MODE, DEFAULT_PERFORMANCE_MODE.name)
			?: DEFAULT_PERFORMANCE_MODE.name
		return runCatching { PerformanceMode.valueOf(raw) }.getOrElse { DEFAULT_PERFORMANCE_MODE }
	}

	fun setPerformanceMode(mode: PerformanceMode) {
		prefs.edit()
			.putString(KEY_PERFORMANCE_MODE, mode.name)
			.apply()
	}

	fun getLocationMode(): LocationMode {
		val raw = prefs.getString(KEY_LOCATION_MODE, DEFAULT_LOCATION_MODE.name) ?: DEFAULT_LOCATION_MODE.name
		return runCatching { LocationMode.valueOf(raw) }.getOrElse { DEFAULT_LOCATION_MODE }
	}

	fun setLocationMode(mode: LocationMode) {
		prefs.edit()
			.putString(KEY_LOCATION_MODE, mode.name)
			.apply()
	}

	fun getManualCity(): ManualCity {
		val defaults = AppSettingsDefaults.DEFAULT_CITY
		val name = prefs.getString(KEY_MANUAL_CITY_NAME, defaults.name) ?: defaults.name
		val latitude = readDouble(KEY_MANUAL_CITY_LAT, defaults.latitude)
		val longitude = readDouble(KEY_MANUAL_CITY_LNG, defaults.longitude)
		return ManualCity(
			name = name,
			latitude = latitude,
			longitude = longitude
		)
	}

	fun setManualCity(city: ManualCity) {
		prefs.edit()
			.putString(KEY_MANUAL_CITY_NAME, city.name)
			.putLong(KEY_MANUAL_CITY_LAT, city.latitude.toRawBits())
			.putLong(KEY_MANUAL_CITY_LNG, city.longitude.toRawBits())
			.apply()
	}

	private fun readDouble(key: String, defaultValue: Double): Double {
		if (!prefs.contains(key)) return defaultValue
		return Double.fromBits(prefs.getLong(key, 0L))
	}

	private fun Double.toRawBits(): Long = java.lang.Double.doubleToRawLongBits(this)

	private companion object {
		private const val PREFERENCES_NAME = "lumisky_app_settings"
		private const val KEY_APP_THEME_MODE = "app_theme_mode"
		private const val KEY_LANGUAGE_TAG = "language_tag"
		private const val KEY_HIGH_REFRESH_ENABLED = "high_refresh_enabled"
		private const val KEY_PERFORMANCE_MODE = "performance_mode"
		private const val KEY_LOCATION_MODE = "location_mode"
		private const val KEY_MANUAL_CITY_NAME = "manual_city_name"
		private const val KEY_MANUAL_CITY_LAT = "manual_city_lat"
		private const val KEY_MANUAL_CITY_LNG = "manual_city_lng"

		private const val DEFAULT_HIGH_REFRESH_ENABLED = false
		private val DEFAULT_PERFORMANCE_MODE = PerformanceMode.AUTO
		private val DEFAULT_LOCATION_MODE = LocationMode.GPS
	}
}
