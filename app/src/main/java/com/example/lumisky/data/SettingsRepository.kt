/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - DataStore tabanlı seçili wallpaper, quality, parallax, last successful scene saklama.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: DataStore tabanlı seçili wallpaper, quality, parallax, last successful scene saklama.
 */
package com.example.lumisky.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val selectedWallpaperKey = stringPreferencesKey("selected_wallpaper_id")
    private val previewWallpaperKey = stringPreferencesKey("preview_wallpaper_id")
    private val qualityTierKey = stringPreferencesKey("quality_tier")
    private val locationModeKey = stringPreferencesKey("location_mode")
    private val manualLatitudeKey = doublePreferencesKey("manual_latitude")
    private val manualLongitudeKey = doublePreferencesKey("manual_longitude")
    private val manualTimeZoneKey = stringPreferencesKey("manual_time_zone")
    private val previewTimeSimulationKey = booleanPreferencesKey("preview_time_simulation")
    private val highRefreshEnabledKey = booleanPreferencesKey("high_refresh_enabled")
    private val performanceModeKey = stringPreferencesKey("performance_mode")
    private val appThemeModeKey = stringPreferencesKey("app_theme_mode")
    private val languageTagKey = stringPreferencesKey("language_tag")
    private val deviceLocationLabelKey = stringPreferencesKey("device_location_label")
    private val deviceLatitudeKey = doublePreferencesKey("device_latitude")
    private val deviceLongitudeKey = doublePreferencesKey("device_longitude")
    private val deviceTimeZoneKey = stringPreferencesKey("device_time_zone")
    private val deviceCapturedAtKey = longPreferencesKey("device_captured_at")
    private val lastSuccessfulWallpaperIdKey = stringPreferencesKey("last_successful_wallpaper_id")
    private val lastSuccessfulDefinitionVersionKey = longPreferencesKey("last_successful_definition_version")
    private val lastSuccessfulQualityTierKey = stringPreferencesKey("last_successful_quality_tier")
    private val lastSuccessfulTimestampKey = longPreferencesKey("last_successful_timestamp")

    val selectedWallpaperId: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[selectedWallpaperKey] ?: "starter_gradient"
    }

    val previewWallpaperId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[previewWallpaperKey]
    }

    val qualityTier: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[qualityTierKey] ?: "BALANCED"
    }

    val locationMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[locationModeKey] ?: LOCATION_MODE_MANUAL
    }

    val manualLatitude: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[manualLatitudeKey] ?: DEFAULT_LATITUDE
    }

    val manualLongitude: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[manualLongitudeKey] ?: DEFAULT_LONGITUDE
    }

    val manualTimeZone: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[manualTimeZoneKey] ?: DEFAULT_TIME_ZONE
    }

    val previewTimeSimulation: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[previewTimeSimulationKey] ?: true
    }

    val highRefreshEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[highRefreshEnabledKey] ?: true
    }

    val performanceMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[performanceModeKey] ?: PERFORMANCE_MODE_AUTO
    }

    val appThemeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[appThemeModeKey] ?: THEME_SYSTEM
    }

    val languageTag: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[languageTagKey] ?: LANGUAGE_SYSTEM
    }

    val deviceLocationSnapshot: Flow<DeviceLocationSnapshot?> = context.dataStore.data.map { prefs ->
        val latitude = prefs[deviceLatitudeKey] ?: return@map null
        val longitude = prefs[deviceLongitudeKey] ?: return@map null
        val capturedAt = prefs[deviceCapturedAtKey] ?: return@map null
        DeviceLocationSnapshot(
            label = prefs[deviceLocationLabelKey] ?: "",
            latitude = latitude,
            longitude = longitude,
            timeZoneId = prefs[deviceTimeZoneKey] ?: DEFAULT_TIME_ZONE,
            capturedAtEpochMs = capturedAt
        )
    }

    val lastSuccessfulScene: Flow<LastSuccessfulSceneState?> = context.dataStore.data.map { prefs ->
        val wallpaperId = prefs[lastSuccessfulWallpaperIdKey] ?: return@map null
        val definitionVersion = prefs[lastSuccessfulDefinitionVersionKey] ?: return@map null
        val timestamp = prefs[lastSuccessfulTimestampKey] ?: return@map null
        LastSuccessfulSceneState(
            wallpaperId = wallpaperId,
            definitionVersion = definitionVersion.toInt(),
            qualityTier = try {
                com.example.lumisky.definition.QualityTier.valueOf(
                    prefs[lastSuccessfulQualityTierKey] ?: "BALANCED"
                )
            } catch (e: Throwable) {
                com.example.lumisky.definition.QualityTier.BALANCED
            },
            timestampMillis = timestamp
        )
    }

    suspend fun setSelectedWallpaperId(id: String) {
        context.dataStore.edit { prefs ->
            prefs[selectedWallpaperKey] = id
        }
    }

    suspend fun setPreviewWallpaperId(id: String) {
        context.dataStore.edit { prefs ->
            prefs[previewWallpaperKey] = id
        }
    }

    suspend fun promotePreviewWallpaper(): Boolean {
        var promoted = false
        context.dataStore.edit { prefs ->
            val candidate = prefs[previewWallpaperKey]
            if (candidate != null) {
                prefs[selectedWallpaperKey] = candidate
                prefs.remove(previewWallpaperKey)
                promoted = true
            }
        }
        return promoted
    }

    suspend fun clearPreviewWallpaper() {
        context.dataStore.edit { prefs ->
            prefs.remove(previewWallpaperKey)
        }
    }

    suspend fun setQualityTier(tier: String) {
        context.dataStore.edit { prefs ->
            prefs[qualityTierKey] = tier
        }
    }

    suspend fun setLocationMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[locationModeKey] = if (mode == LOCATION_MODE_DEVICE) {
                LOCATION_MODE_DEVICE
            } else {
                LOCATION_MODE_MANUAL
            }
        }
    }

    suspend fun setManualLocation(latitude: Double, longitude: Double, timeZoneId: String) {
        context.dataStore.edit { prefs ->
            prefs[manualLatitudeKey] = latitude.coerceIn(-89.0, 89.0)
            prefs[manualLongitudeKey] = longitude.coerceIn(-180.0, 180.0)
            prefs[manualTimeZoneKey] = timeZoneId.ifBlank { DEFAULT_TIME_ZONE }
        }
    }

    suspend fun setPreviewTimeSimulation(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[previewTimeSimulationKey] = enabled
        }
    }

    suspend fun setHighRefreshEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[highRefreshEnabledKey] = enabled
        }
    }

    suspend fun setPerformanceMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[performanceModeKey] = when (mode) {
                PERFORMANCE_MODE_BATTERY,
                PERFORMANCE_MODE_SMOOTH -> mode
                else -> PERFORMANCE_MODE_AUTO
            }
        }
    }

    suspend fun setAppThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[appThemeModeKey] = when (mode) {
                THEME_LIGHT,
                THEME_DARK -> mode
                else -> THEME_SYSTEM
            }
        }
    }

    suspend fun setLanguageTag(tag: String) {
        context.dataStore.edit { prefs ->
            prefs[languageTagKey] = when (tag) {
                LANGUAGE_EN,
                LANGUAGE_TR,
                LANGUAGE_ES,
                LANGUAGE_FR,
                LANGUAGE_DE,
                LANGUAGE_IT,
                LANGUAGE_PT,
                LANGUAGE_RU,
                LANGUAGE_JA,
                LANGUAGE_ZH_CN,
                LANGUAGE_HI,
                LANGUAGE_AR -> tag
                else -> LANGUAGE_SYSTEM
            }
        }
    }

    suspend fun setDeviceLocationSnapshot(snapshot: DeviceLocationSnapshot) {
        context.dataStore.edit { prefs ->
            prefs[deviceLocationLabelKey] = snapshot.label
            prefs[deviceLatitudeKey] = snapshot.latitude.coerceIn(-89.0, 89.0)
            prefs[deviceLongitudeKey] = snapshot.longitude.coerceIn(-180.0, 180.0)
            prefs[deviceTimeZoneKey] = snapshot.timeZoneId.ifBlank { DEFAULT_TIME_ZONE }
            prefs[deviceCapturedAtKey] = snapshot.capturedAtEpochMs
        }
    }

    suspend fun markLastSuccessfulScene(state: LastSuccessfulSceneState) {
        context.dataStore.edit { prefs ->
            prefs[lastSuccessfulWallpaperIdKey] = state.wallpaperId
            prefs[lastSuccessfulDefinitionVersionKey] = state.definitionVersion.toLong()
            prefs[lastSuccessfulQualityTierKey] = state.qualityTier.name
            prefs[lastSuccessfulTimestampKey] = state.timestampMillis
        }
    }

    companion object {
        const val LOCATION_MODE_MANUAL = "MANUAL"
        const val LOCATION_MODE_DEVICE = "DEVICE"
        const val PERFORMANCE_MODE_BATTERY = "BATTERY"
        const val PERFORMANCE_MODE_AUTO = "AUTO"
        const val PERFORMANCE_MODE_SMOOTH = "SMOOTH"
        const val THEME_SYSTEM = "SYSTEM"
        const val THEME_LIGHT = "LIGHT"
        const val THEME_DARK = "DARK"
        const val LANGUAGE_SYSTEM = "system"
        const val LANGUAGE_EN = "en"
        const val LANGUAGE_TR = "tr"
        const val LANGUAGE_ES = "es"
        const val LANGUAGE_FR = "fr"
        const val LANGUAGE_DE = "de"
        const val LANGUAGE_IT = "it"
        const val LANGUAGE_PT = "pt"
        const val LANGUAGE_RU = "ru"
        const val LANGUAGE_JA = "ja"
        const val LANGUAGE_ZH_CN = "zh-CN"
        const val LANGUAGE_HI = "hi"
        const val LANGUAGE_AR = "ar"
        const val DEFAULT_LATITUDE = 41.0082
        const val DEFAULT_LONGITUDE = 28.9784
        const val DEFAULT_TIME_ZONE = "Europe/Istanbul"
    }
}
