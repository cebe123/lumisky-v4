/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Android canlı wallpaper servisidir. Sadece WallpaperService.Engine üretir; render detaylarını bilmez.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Android canlı wallpaper servisidir. Sadece WallpaperService.Engine üretir; render detaylarını bilmez.
 */
package com.example.lumisky.core

import android.annotation.TargetApi
import android.app.WallpaperColors
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.example.lumisky.assets.WallpaperColorProvider
import com.example.lumisky.data.WallpaperRepository
import com.example.lumisky.device.SensorDispatcher
import com.example.lumisky.device.ThermalStateController
import com.example.lumisky.engine.LumiskyRenderer
import com.example.lumisky.registry.SceneFactory
import com.example.lumisky.registry.ShaderRegistry
import com.example.lumisky.telemetry.FeatureFlagRepository
import com.example.lumisky.telemetry.RenderTelemetryLogger
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface EngineEntryPoint {
    fun wallpaperRepository(): WallpaperRepository
    fun lumiskyRenderer(): LumiskyRenderer
    fun shaderRegistry(): ShaderRegistry
    fun sensorDispatcher(): SensorDispatcher
    fun sceneFactory(): SceneFactory
    fun wallpaperColorProvider(): WallpaperColorProvider
    fun thermalStateController(): ThermalStateController
    fun featureFlagRepository(): FeatureFlagRepository
    fun renderTelemetryLogger(): RenderTelemetryLogger
}

@AndroidEntryPoint
class LumiskyWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            EngineEntryPoint::class.java
        )
        return EngineAdapter(entryPoint)
    }

    private inner class EngineAdapter(
        private val entryPoint: EngineEntryPoint
    ) : Engine() {
        private val delegate: LumiskyWallpaperEngine by lazy {
            LumiskyWallpaperEngine(
                service = this@LumiskyWallpaperService,
                entryPoint = entryPoint,
                notifyColorsChanged = { notifyColorsChangedIfAvailable() }
            )
        }

        private val userPresentReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_OFF -> delegate.onDisplayInteractiveChanged(false)
                    Intent.ACTION_SCREEN_ON -> delegate.onDisplayInteractiveChanged(true)
                }
            }
        }

        private val temporalContextReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_TIME_TICK,
                    Intent.ACTION_TIME_CHANGED,
                    Intent.ACTION_TIMEZONE_CHANGED,
                    Intent.ACTION_DATE_CHANGED,
                    LocationManager.PROVIDERS_CHANGED_ACTION -> delegate.onTemporalContextChanged()
                }
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            
            setTouchEventsEnabled(true)
            setOffsetNotificationsEnabled(true)
            delegate.onCreate(surfaceHolder, isPreview)
            
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
            }
            this@LumiskyWallpaperService.registerReceiver(userPresentReceiver, filter)
            val temporalFilter = IntentFilter().apply {
                addAction(Intent.ACTION_TIME_TICK)
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
                addAction(Intent.ACTION_DATE_CHANGED)
                addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            }
            this@LumiskyWallpaperService.registerReceiver(temporalContextReceiver, temporalFilter)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            delegate.onVisibilityChanged(visible)
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            delegate.onSurfaceCreated(holder)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            delegate.onSurfaceChanged(holder, format, width, height)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            delegate.onSurfaceDestroyed(holder)
        }

        override fun onTouchEvent(event: MotionEvent) {
            delegate.onTouchEvent(event)
        }

        override fun onOffsetsChanged(
            xOffset: Float,
            yOffset: Float,
            xOffsetStep: Float,
            yOffsetStep: Float,
            xPixelOffset: Int,
            yPixelOffset: Int
        ) {
            delegate.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset)
        }

        @TargetApi(Build.VERSION_CODES.O_MR1)
        override fun onComputeColors(): WallpaperColors? {
            return delegate.onComputeColors()
        }

        override fun onDestroy() {
            try {
                this@LumiskyWallpaperService.unregisterReceiver(userPresentReceiver)
            } catch (e: Exception) {
                // Ignore if not registered
            }
            try {
                this@LumiskyWallpaperService.unregisterReceiver(temporalContextReceiver)
            } catch (e: Exception) {
                // Ignore if not registered
            }
            delegate.onDestroy()
            super.onDestroy()
        }

        @TargetApi(Build.VERSION_CODES.O_MR1)
        private fun notifyColorsChangedIfAvailable() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                notifyColorsChanged()
            }
        }
    }
}
