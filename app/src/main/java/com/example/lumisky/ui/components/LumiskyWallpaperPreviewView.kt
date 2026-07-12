package com.example.lumisky.ui.components

import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.lumisky.R
import com.example.lumisky.assets.ShaderSourceLoader
import com.example.lumisky.data.SettingsRepository
import com.example.lumisky.data.WallpaperRepository
import com.example.lumisky.device.SensorDispatcher
import com.example.lumisky.device.ThermalStateController
import com.example.lumisky.engine.*
import com.example.lumisky.preview.LumiskyPreviewRenderer
import com.example.lumisky.preview.PreviewGlThread
import com.example.lumisky.registry.SceneFactory
import com.example.lumisky.registry.ShaderRegistry
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PreviewEntryPoint {
    fun wallpaperRepository(): WallpaperRepository
    fun shaderRegistry(): ShaderRegistry
    fun sceneFactory(): SceneFactory
    fun shaderSourceLoader(): ShaderSourceLoader
    fun eventTriggerSystem(): EventTriggerSystem
    fun atmosphereController(): AtmosphereController
    fun adaptiveQualityController(): AdaptiveQualityController
    fun settingsRepository(): SettingsRepository
    fun thermalStateController(): ThermalStateController
    fun sensorDispatcher(): SensorDispatcher
}

@Composable
fun LumiskyWallpaperPreviewView(
    wallpaperId: String,
    modifier: Modifier = Modifier,
    playPlayback: Boolean = true,
    runtimeProfile: RuntimeProfile = RuntimeProfile.fullscreenPreview(),
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
            eventTriggerSystem = entryPoint.eventTriggerSystem(),
            atmosphereController = entryPoint.atmosphereController(),
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

    var isLoaded by remember(wallpaperId) { mutableStateOf(false) }

    LaunchedEffect(wallpaperId) {
        firstFrameRendered = false
        isLoaded = false
        val def = entryPoint.wallpaperRepository().getDefinition(wallpaperId)
        if (def != null) {
            glThread.loadWallpaper(def)
            isLoaded = true
        }
    }

    LaunchedEffect(playPlayback, isLoaded) {
        if (isLoaded) {
            glThread.setVisibility(playPlayback)
        }
    }

    DisposableEffect(glThread) {
        onDispose {
            glThread.quitSafely()
        }
    }

    val viewAlpha by animateFloatAsState(
        targetValue = if (firstFrameRendered && framesRendered >= 2) 1f else 0f,
        animationSpec = tween(150),
        label = "preview_gl_alpha"
    )

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                android.view.TextureView(ctx).apply {
                    isClickable = false
                    isFocusable = false
                    isFocusableInTouchMode = false
                    surfaceTextureListener = object : android.view.TextureView.SurfaceTextureListener {
                        private var surface: android.view.Surface? = null
                        override fun onSurfaceTextureAvailable(st: android.graphics.SurfaceTexture, width: Int, height: Int) {
                            val s = android.view.Surface(st)
                            surface = s
                            glThread.onSurfaceCreated(s)
                            glThread.onSurfaceChanged(width, height)
                        }

                        override fun onSurfaceTextureSizeChanged(st: android.graphics.SurfaceTexture, width: Int, height: Int) {
                            glThread.onSurfaceChanged(width, height)
                        }

                        override fun onSurfaceTextureDestroyed(st: android.graphics.SurfaceTexture): Boolean {
                            glThread.onSurfaceDestroyed()
                            surface?.release()
                            surface = null
                            return true
                        }

                        override fun onSurfaceTextureUpdated(st: android.graphics.SurfaceTexture) {}
                    }
                }
            },
            update = { view ->
                // Handled via Compose modifier
            },
            modifier = Modifier.fillMaxSize().alpha(viewAlpha)
        )

        if (!(firstFrameRendered && framesRendered >= 2) && playPlayback) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_splash_logo),
                    contentDescription = "Loading",
                    tint = Color(0xFF81ECFF),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
