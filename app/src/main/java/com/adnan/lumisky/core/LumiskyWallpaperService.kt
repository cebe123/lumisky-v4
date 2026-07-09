/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Android canlı wallpaper servisidir. Sadece WallpaperService.Engine üretir; render detaylarını bilmez.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Android canlı wallpaper servisidir. Sadece WallpaperService.Engine üretir; render detaylarını bilmez.
 */
package com.adnan.lumisky.core

import android.annotation.TargetApi
import android.app.WallpaperColors
import android.os.Build
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.adnan.lumisky.assets.WallpaperColorProvider
import com.adnan.lumisky.data.WallpaperRepository
import com.adnan.lumisky.device.SensorDispatcher
import com.adnan.lumisky.device.ThermalStateController
import com.adnan.lumisky.engine.LumiskyRenderer
import com.adnan.lumisky.registry.SceneFactory
import com.adnan.lumisky.registry.ShaderRegistry
import com.adnan.lumisky.telemetry.FeatureFlagRepository
import com.adnan.lumisky.telemetry.RenderTelemetryLogger
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
        entryPoint: EngineEntryPoint
    ) : Engine() {
        private val delegate = LumiskyWallpaperEngine(this@LumiskyWallpaperService, entryPoint) {
            notifyColorsChangedIfAvailable()
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(true)
            setOffsetNotificationsEnabled(true)
            delegate.onCreate(surfaceHolder)
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
