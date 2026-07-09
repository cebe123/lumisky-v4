/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - SettingsRepository ile UI state ve engine command bridge.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: SettingsRepository ile UI state ve engine command bridge.
 */
package com.adnan.lumisky.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adnan.lumisky.data.SettingsRepository
import com.adnan.lumisky.device.DeviceLocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val deviceLocationProvider: DeviceLocationProvider
) : ViewModel() {

    // Coalesce location refresh requests to avoid redundant system calls
    private var pendingLocationRefreshJob: Job? = null
    private var lastLocationRefreshAtMs: Long = 0L

    val selectedWallpaperId: StateFlow<String> = settingsRepository.selectedWallpaperId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "starter_gradient")

    val qualityTier: StateFlow<String> = settingsRepository.qualityTier
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "BALANCED")

    val locationMode: StateFlow<String> = settingsRepository.locationMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.LOCATION_MODE_MANUAL)

    val manualLatitude: StateFlow<Double> = settingsRepository.manualLatitude
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_LATITUDE)

    val manualLongitude: StateFlow<Double> = settingsRepository.manualLongitude
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_LONGITUDE)

    val manualTimeZone: StateFlow<String> = settingsRepository.manualTimeZone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_TIME_ZONE)

    val previewTimeSimulation: StateFlow<Boolean> = settingsRepository.previewTimeSimulation
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val highRefreshEnabled: StateFlow<Boolean> = settingsRepository.highRefreshEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val performanceMode: StateFlow<String> = settingsRepository.performanceMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.PERFORMANCE_MODE_AUTO)

    val appThemeMode: StateFlow<String> = settingsRepository.appThemeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.THEME_SYSTEM)

    val languageTag: StateFlow<String> = settingsRepository.languageTag
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.LANGUAGE_SYSTEM)

    val deviceLocationSnapshot = settingsRepository.deviceLocationSnapshot
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setQualityTier(tier: String) {
        viewModelScope.launch {
            settingsRepository.setQualityTier(tier)
        }
    }

    fun setLocationMode(mode: String) {
        // Coalesce location mode changes to avoid cascading refreshes
        coalesceLocationRefresh {
            viewModelScope.launch {
                settingsRepository.setLocationMode(mode)
                if (mode == SettingsRepository.LOCATION_MODE_DEVICE) {
                    performDeviceLocationRefresh()
                }
            }
        }
    }

    fun setManualLocation(latitude: Double, longitude: Double, timeZoneId: String) {
        viewModelScope.launch {
            settingsRepository.setManualLocation(latitude, longitude, timeZoneId)
            settingsRepository.setLocationMode(SettingsRepository.LOCATION_MODE_MANUAL)
        }
    }

    fun setPreviewTimeSimulation(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setPreviewTimeSimulation(enabled)
        }
    }

    fun setHighRefreshEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setHighRefreshEnabled(enabled)
        }
    }

    fun setPerformanceMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setPerformanceMode(mode)
        }
    }

    fun setAppThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setAppThemeMode(mode)
        }
    }

    fun setLanguageTag(tag: String) {
        viewModelScope.launch {
            settingsRepository.setLanguageTag(tag)
        }
    }

    fun refreshDeviceLocation() {
        // Debounce rapid location refresh calls
        coalesceLocationRefresh {
            performDeviceLocationRefresh()
        }
    }

    private fun performDeviceLocationRefresh() {
        viewModelScope.launch {
            val snapshot = deviceLocationProvider.readLastKnownSnapshot()
            if (snapshot != null) {
                settingsRepository.setDeviceLocationSnapshot(snapshot)
            }
            settingsRepository.setLocationMode(SettingsRepository.LOCATION_MODE_DEVICE)
        }
    }

    /**
     * Coalesce location refresh requests within LOCATION_COALESCE_DELAY_MS window.
     * This prevents redundant system location calls when multiple refresh triggers occur in quick succession.
     */
    private fun coalesceLocationRefresh(action: suspend () -> Unit) {
        val now = System.currentTimeMillis()
        
        // Cancel pending job if within coalesce window
        if (pendingLocationRefreshJob?.isActive == true && 
            now - lastLocationRefreshAtMs < LOCATION_COALESCE_DELAY_MS) {
            return
        }
        
        // Schedule new refresh
        pendingLocationRefreshJob = viewModelScope.launch {
            kotlinx.coroutines.delay(LOCATION_COALESCE_DELAY_MS)
            lastLocationRefreshAtMs = System.currentTimeMillis()
            action()
        }
    }

    fun isDeviceLocationAvailable(): Boolean {
        return deviceLocationProvider.hasLocationPermission() && deviceLocationProvider.isLocationEnabled()
    }

    companion object {
       // Debounce window for coalescing location refresh requests (ms).
       // Prevents redundant LocationManager calls when multiple triggers occur rapidly.
       // Value tuned from V2's LocationSunTimesCoordinator pattern.
       private const val LOCATION_COALESCE_DELAY_MS = 200L
    }
}
