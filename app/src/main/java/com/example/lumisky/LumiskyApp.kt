/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Uygulama sınıfı. Hilt, global telemetry ve app-level initialization için giriş noktası.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Uygulama sınıfı. Hilt, global telemetry ve app-level initialization için giriş noktası.
 */
package com.example.lumisky

import android.app.Application
import com.example.lumisky.device.ThermalStateController
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LumiskyApp : Application() {

    @Inject
    lateinit var thermalStateController: ThermalStateController

    override fun onCreate() {
        super.onCreate()
        thermalStateController.register()
    }
}
