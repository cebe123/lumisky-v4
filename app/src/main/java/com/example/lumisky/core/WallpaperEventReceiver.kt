/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Screen on/off, user present ve battery gibi sistem eventlerini yakalayıp EngineEventQueue’ya iletir.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Screen on/off, user present ve battery gibi sistem eventlerini yakalayıp EngineEventQueue’ya iletir.
 */
package com.example.lumisky.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WallpaperEventReceiver : BroadcastReceiver() {

    @Inject
    lateinit var engineController: EngineController

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            Intent.ACTION_SCREEN_ON -> engineController.postEvent(WallpaperEvent.ScreenOn)
            Intent.ACTION_SCREEN_OFF -> engineController.postEvent(WallpaperEvent.ScreenOff)
            Intent.ACTION_USER_PRESENT -> engineController.postEvent(WallpaperEvent.UserPresent)
        }
    }

    fun register(context: Context) {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        context.registerReceiver(this, filter)
    }

    fun unregister(context: Context) {
        try {
            context.unregisterReceiver(this)
        } catch (e: Throwable) {
            // Suppress errors if already unregistered
        }
    }
}
