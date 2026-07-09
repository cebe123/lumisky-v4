/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Lumisky v5 Ui katmanı bileşeni.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Lumisky v5 Ui katmanı bileşeni.
 */
package com.adnan.lumisky.ui.components

import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.adnan.lumisky.assets.ShaderSourceLoader
import com.adnan.lumisky.data.WallpaperRepository
import com.adnan.lumisky.engine.*
import com.adnan.lumisky.preview.LumiskyPreviewRenderer
import com.adnan.lumisky.preview.PreviewGlThread
import com.adnan.lumisky.registry.SceneFactory
import com.adnan.lumisky.registry.ShaderRegistry
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

import com.adnan.lumisky.data.SettingsRepository
import com.adnan.lumisky.device.ThermalStateController

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PreviewEntryPoint {
    fun wallpaperRepository(): WallpaperRepository
    fun shaderRegistry(): ShaderRegistry
    fun sceneFactory(): SceneFactory
    fun shaderSourceLoader(): ShaderSourceLoader
    fun sceneScheduler(): SceneScheduler
    fun eventTriggerSystem(): EventTriggerSystem
    fun atmosphereController(): AtmosphereController
    fun parallaxController(): ParallaxController
    fun adaptiveQualityController(): AdaptiveQualityController
    fun settingsRepository(): SettingsRepository
    fun thermalStateController(): ThermalStateController
    fun sensorDispatcher(): com.adnan.lumisky.device.SensorDispatcher
}

@Composable
fun LumiskyWallpaperPreviewView(
    wallpaperId: String,
    modifier: Modifier = Modifier,
    playPlayback: Boolean = true,
    runtimeProfile: RuntimeProfile = RuntimeProfile.fullscreenPreview(),
    triggerAnimationKey: Int = 0,
    onDayProgressChanged: (Float) -> Unit = {}
) {
    val context = LocalContext.current.applicationContext
    var firstFrameRendered by remember(wallpaperId, runtimeProfile) { mutableStateOf(false) }
    var framesRendered by remember(wallpaperId, runtimeProfile) { mutableStateOf(0) }
    
    val entryPoint = remember(context) {
        EntryPointAccessors.fromApplication(
            context,
            PreviewEntryPoint::class.java
        )
    }

    val previewRenderer = remember(wallpaperId, runtimeProfile) {
        LumiskyPreviewRenderer(
            shaderSourceLoader = entryPoint.shaderSourceLoader(),
            scheduler = entryPoint.sceneScheduler(),
            eventTriggerSystem = entryPoint.eventTriggerSystem(),
            atmosphereController = entryPoint.atmosphereController(),
            parallaxController = entryPoint.parallaxController(),
            qualityController = entryPoint.adaptiveQualityController(),
            sceneFactory = entryPoint.sceneFactory(),
            runtimeProfile = runtimeProfile
        )
    }

    val dayProgressCallback = rememberUpdatedState(onDayProgressChanged)
    val firstFrameCallback = rememberUpdatedState {
        if (!firstFrameRendered) {
            firstFrameRendered = true
            framesRendered = 0
        }
    }
    val glThread = remember(wallpaperId, previewRenderer) {
        PreviewGlThread(
            context = context,
            renderer = previewRenderer,
            shaderRegistry = entryPoint.shaderRegistry(),
            settingsRepository = entryPoint.settingsRepository(),
            thermalStateController = entryPoint.thermalStateController(),
            sensorDispatcher = entryPoint.sensorDispatcher(),
            onDayProgressChanged = { progress ->
                if (firstFrameRendered) {
                    framesRendered = (framesRendered + 1).coerceAtMost(999)
                }
                dayProgressCallback.value(progress)
            },
            onFirstFrameRendered = {
                firstFrameCallback.value()
            }
        ).apply {
            start()
        }
    }

    LaunchedEffect(wallpaperId) {
        firstFrameRendered = false
        val def = entryPoint.wallpaperRepository().getDefinition(wallpaperId)
        if (def != null) {
            glThread.loadWallpaper(def)
        }
    }

    LaunchedEffect(playPlayback) {
        glThread.setVisibility(playPlayback)
    }

    LaunchedEffect(triggerAnimationKey) {
        if (triggerAnimationKey > 0) {
            glThread.triggerPreviewAnimation()
        }
    }

    DisposableEffect(glThread) {
        onDispose {
            glThread.quitSafely()
        }
    }

    AndroidView(
        factory = { ctx ->
            SurfaceView(ctx).apply {
                alpha = 0f
                isClickable = false
                isFocusable = false
                isFocusableInTouchMode = false
                holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        glThread.onSurfaceCreated(holder)
                    }

                    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                        glThread.onSurfaceChanged(width, height)
                    }

                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        glThread.onSurfaceDestroyed()
                    }
                })
            }
        },
        update = { view ->
            // Wait for at least 2 rendered frames before making visible.
            // This ensures EGL context has properly rendered to the surface.
            val shouldBeVisible = firstFrameRendered && framesRendered >= 2
            if (shouldBeVisible && view.alpha < 1f) {
                view.animate()
                    .alpha(1f)
                    .setDuration(150L)
                    .start()
            } else if (!shouldBeVisible && view.alpha > 0f) {
                view.alpha = 0f
            }
        },
        modifier = modifier
    )
}
