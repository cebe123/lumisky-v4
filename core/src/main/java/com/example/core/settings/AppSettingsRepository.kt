package com.example.core.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.UserManager
import com.example.core.Logger
import com.example.core.location.LocationAccessLevel
import com.example.core.location.LocationSnapshot
import com.example.core.location.LocationSource
import kotlin.math.abs

class AppSettingsRepository(
	context: Context
) {
	private val appContext = context.applicationContext
	private val settingsLock = Any()
	private val prefs by lazy {
		appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
	}
	private val deviceProtectedPrefs: SharedPreferences? by lazy {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
			null
		} else {
			runCatching {
				appContext.createDeviceProtectedStorageContext()
					.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
			}.onFailure { throwable ->
				Logger.w(TAG, "device protected preferences unavailable", throwable)
			}.getOrNull()
		}
	}

	fun snapshot(): AppSettingsSnapshot {
		return synchronized(settingsLock) {
			AppSettingsSnapshot(
				appThemeMode = getAppThemeMode(),
				languageTag = getLanguageTag(),
				highRefreshEnabled = isHighRefreshEnabled(),
				performanceMode = getPerformanceMode(),
				locationMode = getLocationMode(),
				manualCity = getManualCity(),
				automaticLocation = getAutomaticLocation()
			)
		}
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
		return synchronized(settingsLock) {
			readEnum(KEY_APP_THEME_MODE, AppThemeMode.SYSTEM)
		}
	}

	fun setAppThemeMode(mode: AppThemeMode) {
		val changed = synchronized(settingsLock) {
			if (getAppThemeMode() == mode) return
			commitPreference(
				prefs.edit().putString(KEY_APP_THEME_MODE, mode.name),
				"app theme mode"
			)
		}
		if (changed) dispatchSnapshotChanged()
	}

	fun getLanguageTag(): String {
		return synchronized(settingsLock) {
			prefs.getString(KEY_LANGUAGE_TAG, AppSettingsDefaults.LANGUAGE_SYSTEM)
				?: AppSettingsDefaults.LANGUAGE_SYSTEM
		}
	}

	fun setLanguageTag(tag: String) {
		val normalized = tag.ifBlank { AppSettingsDefaults.LANGUAGE_SYSTEM }
		val changed = synchronized(settingsLock) {
			if (getLanguageTag() == normalized) return
			commitPreference(
				prefs.edit().putString(KEY_LANGUAGE_TAG, normalized),
				"language tag"
			)
		}
		if (changed) dispatchSnapshotChanged()
	}

	fun isHighRefreshEnabled(): Boolean {
		return synchronized(settingsLock) {
			prefs.getBoolean(KEY_HIGH_REFRESH_ENABLED, DEFAULT_HIGH_REFRESH_ENABLED)
		}
	}

	fun setHighRefreshEnabled(enabled: Boolean) {
		val changed = synchronized(settingsLock) {
			if (isHighRefreshEnabled() == enabled) return
			commitPreference(
				prefs.edit().putBoolean(KEY_HIGH_REFRESH_ENABLED, enabled),
				"high refresh enabled"
			)
		}
		if (changed) dispatchSnapshotChanged()
	}

	fun getPerformanceMode(): PerformanceMode {
		return synchronized(settingsLock) {
			readEnum(KEY_PERFORMANCE_MODE, DEFAULT_PERFORMANCE_MODE)
		}
	}

	fun setPerformanceMode(mode: PerformanceMode) {
		val changed = synchronized(settingsLock) {
			if (getPerformanceMode() == mode) return
			commitPreference(
				prefs.edit().putString(KEY_PERFORMANCE_MODE, mode.name),
				"performance mode"
			)
		}
		if (changed) dispatchSnapshotChanged()
	}

	fun getLocationMode(): LocationMode {
		return synchronized(settingsLock) {
			readEnum(KEY_LOCATION_MODE, DEFAULT_LOCATION_MODE)
		}
	}

	fun setLocationMode(mode: LocationMode) {
		val changed = synchronized(settingsLock) {
			if (getLocationMode() == mode) return
			commitPreference(
				prefs.edit().putString(KEY_LOCATION_MODE, mode.name),
				"location mode"
			)
		}
		if (changed) dispatchSnapshotChanged()
	}

	fun getManualCity(): ManualCity {
		return synchronized(settingsLock) {
			val languageTag = prefs.getString(KEY_LANGUAGE_TAG, AppSettingsDefaults.LANGUAGE_SYSTEM)
				?: AppSettingsDefaults.LANGUAGE_SYSTEM
			val defaultCity = AppSettingsDefaults.defaultCity(languageTag)
			val storedId = prefs.getString(KEY_MANUAL_CITY_ID, null)
			if (!storedId.isNullOrBlank()) {
				AppSettingsDefaults.supportedCities(languageTag)
					.firstOrNull { city -> city.id == storedId }
					?.let { city -> return@synchronized city }

				val storedName = prefs.getString(KEY_MANUAL_CITY_NAME, null)?.takeIf { it.isNotBlank() }
				val latitude = readDouble(KEY_MANUAL_CITY_LAT, Double.NaN)
				val longitude = readDouble(KEY_MANUAL_CITY_LNG, Double.NaN)
				val timeZoneId = prefs.getString(KEY_MANUAL_CITY_TIME_ZONE_ID, null)
					?.takeIf { it.isNotBlank() }
				if (
					storedName != null &&
					latitude.isFinite() &&
					longitude.isFinite() &&
					!timeZoneId.isNullOrBlank()
				) {
					return@synchronized ManualCity(
						id = storedId,
						name = storedName,
						countryCode = prefs.getString(KEY_MANUAL_CITY_COUNTRY, null)
							?.takeIf { it.isNotBlank() }
							?: "GPS",
						latitude = latitude,
						longitude = longitude,
						timeZoneId = timeZoneId
					)
				}

				return@synchronized defaultCity
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
			writeManualCity(migrated)
			migrated
		}
	}

	fun setManualCity(city: ManualCity) {
		val changed = synchronized(settingsLock) {
			val storedCityId = prefs.getString(KEY_MANUAL_CITY_ID, null)
			val storedCityName = prefs.getString(KEY_MANUAL_CITY_NAME, null)
			val storedCountryCode = prefs.getString(KEY_MANUAL_CITY_COUNTRY, null)
			val storedLatitude = readDouble(KEY_MANUAL_CITY_LAT, Double.NaN)
			val storedLongitude = readDouble(KEY_MANUAL_CITY_LNG, Double.NaN)
			val storedTimeZoneId = prefs.getString(KEY_MANUAL_CITY_TIME_ZONE_ID, null)
			val sameStoredCity = storedCityId == city.id &&
				storedCityName == city.name &&
				storedCountryCode == city.countryCode &&
				sameCoordinate(storedLatitude, city.latitude) &&
				sameCoordinate(storedLongitude, city.longitude) &&
				storedTimeZoneId == city.timeZoneId
			if (sameStoredCity) return
			writeManualCity(city)
		}
		if (changed) dispatchSnapshotChanged()
	}

	fun getAutomaticLocation(): LocationSnapshot? {
		return synchronized(settingsLock) {
			val capturedAt = prefs.getLong(KEY_AUTO_LOCATION_CAPTURED_AT_MS, 0L)
			if (capturedAt <= 0L) return@synchronized null

			val latitude = readDouble(KEY_AUTO_LOCATION_LAT, Double.NaN)
			val longitude = readDouble(KEY_AUTO_LOCATION_LNG, Double.NaN)
			if (!latitude.isFinite() || !longitude.isFinite()) return@synchronized null

			val timeZoneId = prefs.getString(KEY_AUTO_LOCATION_TIME_ZONE_ID, null)
				?.takeIf { it.isNotBlank() }
				?: return@synchronized null

			val accessLevel = readEnum(
				key = KEY_AUTO_LOCATION_ACCESS_LEVEL,
				defaultValue = LocationAccessLevel.APPROXIMATE
			)
			val source = readEnum(
				key = KEY_AUTO_LOCATION_SOURCE,
				defaultValue = LocationSource.STORED
			)

			val accuracyMeters = if (prefs.contains(KEY_AUTO_LOCATION_ACCURACY_METERS)) {
				prefs.getFloat(KEY_AUTO_LOCATION_ACCURACY_METERS, 0f)
			} else {
				null
			}

			LocationSnapshot(
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
	}

	fun setAutomaticLocation(location: LocationSnapshot) {
		val changed = synchronized(settingsLock) {
			if (getAutomaticLocation() == location) return
			commitPreference(
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
					},
				"automatic location"
			)
		}
		if (changed) dispatchSnapshotChanged()
	}

	fun clearAutomaticLocation() {
		val changed = synchronized(settingsLock) {
			if (getAutomaticLocation() == null) return
			commitPreference(
				prefs.edit()
					.remove(KEY_AUTO_LOCATION_LAT)
					.remove(KEY_AUTO_LOCATION_LNG)
					.remove(KEY_AUTO_LOCATION_TIME_ZONE_ID)
					.remove(KEY_AUTO_LOCATION_LABEL)
					.remove(KEY_AUTO_LOCATION_ACCURACY_METERS)
					.remove(KEY_AUTO_LOCATION_CAPTURED_AT_MS)
					.remove(KEY_AUTO_LOCATION_ACCESS_LEVEL)
					.remove(KEY_AUTO_LOCATION_SOURCE),
				"clear automatic location"
			)
		}
		if (changed) dispatchSnapshotChanged()
	}

	fun getHasRequestedLocationPermission(): Boolean {
		return synchronized(settingsLock) {
			prefs.getBoolean(KEY_REQUESTED_LOCATION_PERMISSION, false)
		}
	}

	fun setHasRequestedLocationPermission(requested: Boolean) {
		synchronized(settingsLock) {
			commitPreference(
				prefs.edit().putBoolean(KEY_REQUESTED_LOCATION_PERMISSION, requested),
				"requested location permission"
			)
		}
	}

	fun getRestoreLiveWallpaperOnLockScreen(): Boolean? {
		return synchronized(settingsLock) {
			readOptionalBoolean(
				preferences = deviceProtectedPrefs,
				key = KEY_RESTORE_LIVE_WALLPAPER_ON_LOCK_SCREEN
			)?.let { return@synchronized it }
			if (!canAccessCredentialProtectedStorage()) return@synchronized null
			readOptionalBoolean(
				preferences = prefs,
				key = KEY_RESTORE_LIVE_WALLPAPER_ON_LOCK_SCREEN
			)
		}
	}

	fun setRestoreLiveWallpaperOnLockScreen(enabled: Boolean?) {
		synchronized(settingsLock) {
			val currentValue = getRestoreLiveWallpaperOnLockScreen()
			if (currentValue == enabled) return
			writeRestoreLiveWallpaperOnLockScreenFlag(prefs, enabled)
			writeRestoreLiveWallpaperOnLockScreenFlag(deviceProtectedPrefs, enabled)
		}
	}

	private fun dispatchSnapshotChanged() {
		val snapshot = snapshot()
		val listeners = synchronized(changeListeners) { changeListeners.toList() }
		listeners.forEach { listener ->
			runCatching { listener(snapshot) }
				.onFailure { throwable ->
					Logger.w(TAG, "settings change listener failed", throwable)
				}
		}
	}

	private fun readDouble(key: String, defaultValue: Double): Double {
		return runCatching {
			if (!prefs.contains(key)) return defaultValue
			val value = Double.fromBits(prefs.getLong(key, defaultValue.toRawBits()))
			if (value.isFinite() || defaultValue.isNaN()) {
				value
			} else {
				Logger.w(TAG, "invalid double preference key=$key, using default")
				defaultValue
			}
		}.onFailure { throwable ->
			Logger.w(TAG, "failed to read double preference key=$key", throwable)
		}.getOrDefault(defaultValue)
	}

	private fun writeManualCity(city: ManualCity): Boolean {
		return commitPreference(
			prefs.edit()
				.putString(KEY_MANUAL_CITY_ID, city.id)
				.putString(KEY_MANUAL_CITY_NAME, city.name)
				.putString(KEY_MANUAL_CITY_COUNTRY, city.countryCode)
				.putLong(KEY_MANUAL_CITY_LAT, city.latitude.toRawBits())
				.putLong(KEY_MANUAL_CITY_LNG, city.longitude.toRawBits())
				.putString(KEY_MANUAL_CITY_TIME_ZONE_ID, city.timeZoneId),
			"manual city"
		)
	}

	private fun writeRestoreLiveWallpaperOnLockScreenFlag(
		targetPrefs: SharedPreferences?,
		enabled: Boolean?
	) {
		targetPrefs ?: return
		commitPreference(
			targetPrefs.edit().apply {
				if (enabled == null) {
					remove(KEY_RESTORE_LIVE_WALLPAPER_ON_LOCK_SCREEN)
				} else {
					putBoolean(KEY_RESTORE_LIVE_WALLPAPER_ON_LOCK_SCREEN, enabled)
				}
			},
			"restore live wallpaper on lock screen"
		)
	}

	private fun canAccessCredentialProtectedStorage(): Boolean {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return true
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
		val userManager = appContext.getSystemService(UserManager::class.java) ?: return true
		return runCatching { userManager.isUserUnlocked }.getOrDefault(false)
	}

	private inline fun <reified T : Enum<T>> readEnum(
		key: String,
		defaultValue: T
	): T {
		val raw = prefs.getString(key, defaultValue.name) ?: defaultValue.name
		return runCatching {
			enumValueOf<T>(raw)
		}.onFailure { throwable ->
			Logger.w(TAG, "invalid enum preference key=$key value=$raw", throwable)
			commitPreference(prefs.edit().remove(key), "clear invalid enum $key")
		}.getOrElse {
			defaultValue
		}
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

	private fun commitPreference(
		editor: SharedPreferences.Editor,
		label: String
	): Boolean {
		return runCatching {
			editor.commit()
		}.onFailure { throwable ->
			Logger.w(TAG, "failed to commit preference change: $label", throwable)
		}.getOrElse {
			false
		}.also { committed ->
			if (!committed) {
				Logger.w(TAG, "preference commit returned false: $label")
			}
		}
	}

	private fun sameCoordinate(
		first: Double,
		second: Double
	): Boolean {
		return first.isFinite() &&
			second.isFinite() &&
			abs(first - second) < DOUBLE_COMPARISON_EPSILON
	}

	private fun Double.toRawBits(): Long = java.lang.Double.doubleToRawLongBits(this)

	private companion object {
		private const val TAG = "AppSettingsRepository"
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
		private const val KEY_MANUAL_CITY_TIME_ZONE_ID = "manual_city_time_zone_id"
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
		private const val KEY_REQUESTED_LOCATION_PERMISSION = "requested_location_permission"

		private const val DEFAULT_HIGH_REFRESH_ENABLED = false
		private const val DOUBLE_COMPARISON_EPSILON = 1e-9
		private val DEFAULT_PERFORMANCE_MODE = PerformanceMode.AUTO
		private val DEFAULT_LOCATION_MODE = LocationMode.GPS
		private val changeListeners = LinkedHashSet<(AppSettingsSnapshot) -> Unit>()
	}
}
