package com.example.core.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.UserManager
import com.example.core.location.LocationAccessLevel
import com.example.core.location.LocationSnapshot
import com.example.core.location.LocationSource

class AppSettingsRepository(
	context: Context
) {
	private val appContext = context.applicationContext
	private val prefs by lazy {
		appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
	}
	private val deviceProtectedPrefs by lazy {
		runCatching {
			appContext.createDeviceProtectedStorageContext()
				.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
		}.getOrNull()
	}

	fun snapshot(): AppSettingsSnapshot {
		return AppSettingsSnapshot(
			appThemeMode = getAppThemeMode(),
			languageTag = getLanguageTag(),
			highRefreshEnabled = isHighRefreshEnabled(),
			performanceMode = getPerformanceMode(),
			locationMode = getLocationMode(),
			manualCity = getManualCity(),
			automaticLocation = getAutomaticLocation()
		)
	}

	fun addChangeListener(
		listener: (AppSettingsSnapshot) -> Unit
	): AutoCloseable {
		synchronized(changeListeners) {
			changeListeners.add(listener)
		}
		return AutoCloseable {
			synchronized(changeListeners) {
				changeListeners.remove(listener)
			}
		}
	}

	fun getAppThemeMode(): AppThemeMode {
		val raw = prefs.getString(KEY_APP_THEME_MODE, AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
		return runCatching { AppThemeMode.valueOf(raw) }.getOrElse { AppThemeMode.SYSTEM }
	}

	fun setAppThemeMode(mode: AppThemeMode) {
		if (getAppThemeMode() == mode) return
		prefs.edit()
			.putString(KEY_APP_THEME_MODE, mode.name)
			.apply()
		dispatchSnapshotChanged()
	}

	fun getLanguageTag(): String {
		return prefs.getString(KEY_LANGUAGE_TAG, AppSettingsDefaults.LANGUAGE_SYSTEM)
			?: AppSettingsDefaults.LANGUAGE_SYSTEM
	}

	fun setLanguageTag(tag: String) {
		val normalized = tag.ifBlank { AppSettingsDefaults.LANGUAGE_SYSTEM }
		if (getLanguageTag() == normalized) return
		prefs.edit()
			.putString(KEY_LANGUAGE_TAG, normalized)
			.apply()
		dispatchSnapshotChanged()
	}

	fun isHighRefreshEnabled(): Boolean {
		return prefs.getBoolean(KEY_HIGH_REFRESH_ENABLED, DEFAULT_HIGH_REFRESH_ENABLED)
	}

	fun setHighRefreshEnabled(enabled: Boolean) {
		if (isHighRefreshEnabled() == enabled) return
		prefs.edit()
			.putBoolean(KEY_HIGH_REFRESH_ENABLED, enabled)
			.apply()
		dispatchSnapshotChanged()
	}

	fun getPerformanceMode(): PerformanceMode {
		val raw = prefs.getString(KEY_PERFORMANCE_MODE, DEFAULT_PERFORMANCE_MODE.name)
			?: DEFAULT_PERFORMANCE_MODE.name
		return runCatching { PerformanceMode.valueOf(raw) }.getOrElse { DEFAULT_PERFORMANCE_MODE }
	}

	fun setPerformanceMode(mode: PerformanceMode) {
		if (getPerformanceMode() == mode) return
		prefs.edit()
			.putString(KEY_PERFORMANCE_MODE, mode.name)
			.apply()
		dispatchSnapshotChanged()
	}

	fun getLocationMode(): LocationMode {
		val raw = prefs.getString(KEY_LOCATION_MODE, DEFAULT_LOCATION_MODE.name) ?: DEFAULT_LOCATION_MODE.name
		return runCatching { LocationMode.valueOf(raw) }.getOrElse { DEFAULT_LOCATION_MODE }
	}

	fun setLocationMode(mode: LocationMode) {
		if (getLocationMode() == mode) return
		prefs.edit()
			.putString(KEY_LOCATION_MODE, mode.name)
			.apply()
		dispatchSnapshotChanged()
	}

	fun getManualCity(): ManualCity {
		val languageTag = getLanguageTag()
		val defaultCity = AppSettingsDefaults.defaultCity(languageTag)
		val storedId = prefs.getString(KEY_MANUAL_CITY_ID, null)
		if (!storedId.isNullOrBlank()) {
			return AppSettingsDefaults.resolveCityById(storedId, languageTag)
		}

		val legacyName = prefs.getString(KEY_MANUAL_CITY_NAME, defaultCity.name)
		val latitude = readDouble(KEY_MANUAL_CITY_LAT, defaultCity.latitude)
		val longitude = readDouble(KEY_MANUAL_CITY_LNG, defaultCity.longitude)
		val migrated = AppSettingsDefaults.resolveCityByLegacy(
			legacyName = legacyName,
			latitude = latitude,
			longitude = longitude,
			languageTag = languageTag
		)
		setManualCity(migrated)
		return migrated
	}

	fun setManualCity(city: ManualCity) {
		val storedCityId = prefs.getString(KEY_MANUAL_CITY_ID, null)
		val storedCityName = prefs.getString(KEY_MANUAL_CITY_NAME, null)
		val storedCountryCode = prefs.getString(KEY_MANUAL_CITY_COUNTRY, null)
		val storedLatitude = readDouble(KEY_MANUAL_CITY_LAT, Double.NaN)
		val storedLongitude = readDouble(KEY_MANUAL_CITY_LNG, Double.NaN)
		val sameStoredCity = storedCityId == city.id &&
			storedCityName == city.name &&
			storedCountryCode == city.countryCode &&
			storedLatitude == city.latitude &&
			storedLongitude == city.longitude
		if (sameStoredCity) return
		prefs.edit()
			.putString(KEY_MANUAL_CITY_ID, city.id)
			.putString(KEY_MANUAL_CITY_NAME, city.name)
			.putString(KEY_MANUAL_CITY_COUNTRY, city.countryCode)
			.putLong(KEY_MANUAL_CITY_LAT, city.latitude.toRawBits())
			.putLong(KEY_MANUAL_CITY_LNG, city.longitude.toRawBits())
			.apply()
		dispatchSnapshotChanged()
	}

	fun getAutomaticLocation(): LocationSnapshot? {
		val capturedAt = prefs.getLong(KEY_AUTO_LOCATION_CAPTURED_AT_MS, 0L)
		if (capturedAt <= 0L) return null

		val latitude = readDouble(KEY_AUTO_LOCATION_LAT, Double.NaN)
		val longitude = readDouble(KEY_AUTO_LOCATION_LNG, Double.NaN)
		if (!latitude.isFinite() || !longitude.isFinite()) return null

		val timeZoneId = prefs.getString(KEY_AUTO_LOCATION_TIME_ZONE_ID, null)
			?.takeIf { it.isNotBlank() }
			?: return null

		val accessLevel = prefs.getString(
			KEY_AUTO_LOCATION_ACCESS_LEVEL,
			LocationAccessLevel.APPROXIMATE.name
		)?.let { raw ->
			runCatching { LocationAccessLevel.valueOf(raw) }.getOrDefault(LocationAccessLevel.APPROXIMATE)
		} ?: LocationAccessLevel.APPROXIMATE

		val source = prefs.getString(KEY_AUTO_LOCATION_SOURCE, LocationSource.STORED.name)
			?.let { raw ->
				runCatching { LocationSource.valueOf(raw) }.getOrDefault(LocationSource.STORED)
			} ?: LocationSource.STORED

		val accuracyMeters = if (prefs.contains(KEY_AUTO_LOCATION_ACCURACY_METERS)) {
			prefs.getFloat(KEY_AUTO_LOCATION_ACCURACY_METERS, 0f)
		} else {
			null
		}

		return LocationSnapshot(
			latitude = latitude,
			longitude = longitude,
			timeZoneId = timeZoneId,
			label = prefs.getString(KEY_AUTO_LOCATION_LABEL, null)?.takeIf { it.isNotBlank() },
			accuracyMeters = accuracyMeters,
			capturedAtEpochMs = capturedAt,
			accessLevel = accessLevel,
			source = source
		)
	}

	fun setAutomaticLocation(location: LocationSnapshot) {
		if (getAutomaticLocation() == location) return
		prefs.edit()
			.putLong(KEY_AUTO_LOCATION_LAT, location.latitude.toRawBits())
			.putLong(KEY_AUTO_LOCATION_LNG, location.longitude.toRawBits())
			.putString(KEY_AUTO_LOCATION_TIME_ZONE_ID, location.timeZoneId)
			.putString(KEY_AUTO_LOCATION_LABEL, location.label)
			.putLong(KEY_AUTO_LOCATION_CAPTURED_AT_MS, location.capturedAtEpochMs)
			.putString(KEY_AUTO_LOCATION_ACCESS_LEVEL, location.accessLevel.name)
			.putString(KEY_AUTO_LOCATION_SOURCE, location.source.name)
			.apply {
				val accuracy = location.accuracyMeters
				if (accuracy != null) {
					putFloat(KEY_AUTO_LOCATION_ACCURACY_METERS, accuracy)
				} else {
					remove(KEY_AUTO_LOCATION_ACCURACY_METERS)
				}
			}
			.apply()
		dispatchSnapshotChanged()
	}

	fun clearAutomaticLocation() {
		if (getAutomaticLocation() == null) return
		prefs.edit()
			.remove(KEY_AUTO_LOCATION_LAT)
			.remove(KEY_AUTO_LOCATION_LNG)
			.remove(KEY_AUTO_LOCATION_TIME_ZONE_ID)
			.remove(KEY_AUTO_LOCATION_LABEL)
			.remove(KEY_AUTO_LOCATION_ACCURACY_METERS)
			.remove(KEY_AUTO_LOCATION_CAPTURED_AT_MS)
			.remove(KEY_AUTO_LOCATION_ACCESS_LEVEL)
			.remove(KEY_AUTO_LOCATION_SOURCE)
			.apply()
		dispatchSnapshotChanged()
	}

	fun getRestoreLiveWallpaperOnLockScreen(): Boolean? {
		readOptionalBoolean(
			preferences = deviceProtectedPrefs,
			key = KEY_RESTORE_LIVE_WALLPAPER_ON_LOCK_SCREEN
		)?.let { return it }
		if (!canAccessCredentialProtectedStorage()) return null
		return readOptionalBoolean(
			preferences = prefs,
			key = KEY_RESTORE_LIVE_WALLPAPER_ON_LOCK_SCREEN
		)
	}

	fun setRestoreLiveWallpaperOnLockScreen(enabled: Boolean?) {
		val currentValue = getRestoreLiveWallpaperOnLockScreen()
		if (currentValue == enabled) return
		writeRestoreLiveWallpaperOnLockScreenFlag(prefs, enabled)
		writeRestoreLiveWallpaperOnLockScreenFlag(deviceProtectedPrefs, enabled)
	}

	private fun dispatchSnapshotChanged() {
		val snapshot = snapshot()
		val listeners = synchronized(changeListeners) { changeListeners.toList() }
		listeners.forEach { listener ->
			runCatching { listener(snapshot) }
		}
	}

	private fun readDouble(key: String, defaultValue: Double): Double {
		if (!prefs.contains(key)) return defaultValue
		return Double.fromBits(prefs.getLong(key, 0L))
	}

	private fun writeRestoreLiveWallpaperOnLockScreenFlag(
		targetPrefs: SharedPreferences?,
		enabled: Boolean?
	) {
		targetPrefs ?: return
		runCatching {
			targetPrefs.edit().apply {
				if (enabled == null) {
					remove(KEY_RESTORE_LIVE_WALLPAPER_ON_LOCK_SCREEN)
				} else {
					putBoolean(KEY_RESTORE_LIVE_WALLPAPER_ON_LOCK_SCREEN, enabled)
				}
			}.apply()
		}
	}

	private fun canAccessCredentialProtectedStorage(): Boolean {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return true
		val userManager = appContext.getSystemService(UserManager::class.java) ?: return true
		return runCatching { userManager.isUserUnlocked }.getOrDefault(false)
	}

	private fun readOptionalBoolean(
		preferences: SharedPreferences?,
		key: String
	): Boolean? {
		preferences ?: return null
		return runCatching {
			if (!preferences.contains(key)) {
				null
			} else {
				preferences.getBoolean(key, false)
			}
		}.getOrNull()
	}

	private fun Double.toRawBits(): Long = java.lang.Double.doubleToRawLongBits(this)

	private companion object {
		private const val PREFERENCES_NAME = "lumisky_app_settings"
		private const val KEY_APP_THEME_MODE = "app_theme_mode"
		private const val KEY_LANGUAGE_TAG = "language_tag"
		private const val KEY_HIGH_REFRESH_ENABLED = "high_refresh_enabled"
		private const val KEY_PERFORMANCE_MODE = "performance_mode"
		private const val KEY_LOCATION_MODE = "location_mode"
		private const val KEY_MANUAL_CITY_ID = "manual_city_id"
		private const val KEY_MANUAL_CITY_NAME = "manual_city_name"
		private const val KEY_MANUAL_CITY_COUNTRY = "manual_city_country"
		private const val KEY_MANUAL_CITY_LAT = "manual_city_lat"
		private const val KEY_MANUAL_CITY_LNG = "manual_city_lng"
		private const val KEY_AUTO_LOCATION_LAT = "auto_location_lat"
		private const val KEY_AUTO_LOCATION_LNG = "auto_location_lng"
		private const val KEY_AUTO_LOCATION_TIME_ZONE_ID = "auto_location_time_zone_id"
		private const val KEY_AUTO_LOCATION_LABEL = "auto_location_label"
		private const val KEY_AUTO_LOCATION_ACCURACY_METERS = "auto_location_accuracy_meters"
		private const val KEY_AUTO_LOCATION_CAPTURED_AT_MS = "auto_location_captured_at_ms"
		private const val KEY_AUTO_LOCATION_ACCESS_LEVEL = "auto_location_access_level"
		private const val KEY_AUTO_LOCATION_SOURCE = "auto_location_source"
		private const val KEY_RESTORE_LIVE_WALLPAPER_ON_LOCK_SCREEN =
			"restore_live_wallpaper_on_lock_screen"

		private const val DEFAULT_HIGH_REFRESH_ENABLED = false
		private val DEFAULT_PERFORMANCE_MODE = PerformanceMode.AUTO
		private val DEFAULT_LOCATION_MODE = LocationMode.GPS
		private val changeListeners = LinkedHashSet<(AppSettingsSnapshot) -> Unit>()
	}
}
