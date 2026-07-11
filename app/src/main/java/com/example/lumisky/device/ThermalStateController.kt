/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - OS thermal listener ile kalite degradation sinyali verir.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: OS thermal listener ile kalite degradation sinyali verir.
 */
package com.example.lumisky.device

import android.content.Context
import android.os.Build
import android.os.PowerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThermalStateController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    private var isRegistered = false
    private val _thermalStatus = MutableStateFlow(PowerManager.THERMAL_STATUS_NONE)

    val thermalStatus: StateFlow<Int> = _thermalStatus.asStateFlow()

    var isThermalThrottling = false
        private set

    fun register() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !isRegistered && powerManager != null) {
            try {
                updateStatus(powerManager.currentThermalStatus)
                powerManager.addThermalStatusListener(::updateStatus)
                isRegistered = true
            } catch (e: Throwable) {
                // Suppress registration failures on customized ROMs or emulators
            }
        }
    }

    private fun updateStatus(status: Int) {
        _thermalStatus.value = status
        isThermalThrottling = status >= PowerManager.THERMAL_STATUS_SEVERE
    }
}
