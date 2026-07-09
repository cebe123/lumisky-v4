/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Lumisky v5 Preview katmanı bileşeni.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Lumisky v5 Preview katmanı bileşeni.
 */
package com.adnan.lumisky.preview

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.PowerManager
import android.view.SurfaceHolder
import com.adnan.lumisky.core.WallpaperFrameClock
import com.adnan.lumisky.data.RuntimeSettingsPolicy
import com.adnan.lumisky.data.RuntimeSettingsPolicyResult
import com.adnan.lumisky.data.SettingsRepository
import com.adnan.lumisky.definition.WallpaperDefinition
import com.adnan.lumisky.device.ThermalStateController
import com.adnan.lumisky.engine.RenderContext
import com.adnan.lumisky.engine.SceneInputSnapshot
import com.adnan.lumisky.engine.gl.EglManager
import com.adnan.lumisky.engine.gl.GlResourceManager
import com.adnan.lumisky.registry.ShaderRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class PreviewGlThread(
    private val context: Context,
    private val renderer: LumiskyPreviewRenderer,
    private val shaderRegistry: ShaderRegistry,
    private val settingsRepository: SettingsRepository,
    private val thermalStateController: ThermalStateController,
    private val sensorDispatcher: com.adnan.lumisky.device.SensorDispatcher,
    private val onDayProgressChanged: (Float) -> Unit = {},
    private val onFirstFrameRendered: () -> Unit = {}
) : HandlerThread("LumiskyPreviewGlThread"), com.adnan.lumisky.device.SensorDispatcher.SensorListener {

    override fun onSensorValues(x: Float, y: Float) {
        parallaxX = x
        parallaxY = y
    }

    private val handler by lazy { Handler(looper) }
    private val mainHandler = Handler(Looper.getMainLooper())
    private var eglManager: EglManager? = null
    private var glManager: GlResourceManager? = null
    private val renderContext = RenderContext()
    private var frameClock: WallpaperFrameClock? = null
    private var lastRenderedFrameNanos = 0L
    private var lastDayProgressCallbackNanos = 0L
    private var hasRenderedFirstFrame = false
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Volatile var isVisible = false
    @Volatile var batterySaver = false
    @Volatile var maxFps = 30
    @Volatile var minFrameIntervalNanos: Long = 1_000_000_000L / 30
    @Volatile var parallaxX = 0.0f
    @Volatile var parallaxY = 0.0f
    @Volatile var touchX = 0.0f
    @Volatile var touchY = 0.0f
    @Volatile var hasTouch = false
    @Volatile private var hasSurface = false

    override fun onLooperPrepared() {
        eglManager = EglManager().apply { initialize() }
        glManager = GlResourceManager(context, shaderRegistry)
        
        scope.launch {
            combine(
                settingsRepository.qualityTier,
                settingsRepository.performanceMode,
                settingsRepository.highRefreshEnabled,
                thermalStateController.thermalStatus
            ) { qualityTier, performanceMode, highRefreshEnabled, thermalStatus ->
                val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                val isPowerSave = pm?.isPowerSaveMode == true
                RuntimeSettingsPolicy.resolve(
                    qualityTier = qualityTier,
                    performanceMode = performanceMode,
                    highRefreshEnabled = highRefreshEnabled,
                    batterySaver = isPowerSave,
                    thermalStatus = thermalStatus,
                    sceneMaxFps = 60,
                    batterySaverSceneMaxFps = 15
                )
            }.collectLatest { policy ->
                maxFps = policy.maxFps
                batterySaver = policy.maxFps <= 15
                minFrameIntervalNanos = if (policy.maxFps > 0) {
                    1_000_000_000L / policy.maxFps.coerceAtLeast(1)
                } else {
                    Long.MAX_VALUE
                }
            }
        }

        frameClock = WallpaperFrameClock { frameTimeNanos ->
            if (isVisible && hasSurface) {
                if (frameTimeNanos - lastRenderedFrameNanos < minFrameIntervalNanos) {
                    return@WallpaperFrameClock
                }
                lastRenderedFrameNanos = frameTimeNanos
                renderContext.update(frameTimeNanos)
                
                val snapshot = SceneInputSnapshot(
                    isVisible = isVisible,
                    batterySaver = batterySaver,
                    parallaxX = parallaxX,
                    parallaxY = parallaxY,
                    touchX = touchX,
                    touchY = touchY,
                    hasTouch = hasTouch
                )
                
                eglManager?.makeCurrent()
                renderer.renderFrame(renderContext, snapshot)
                eglManager?.swapBuffers()
                publishFirstFrameIfNeeded()
                publishDayProgressIfDue(frameTimeNanos)
            }
        }
    }

    fun onSurfaceCreated(holder: SurfaceHolder) {
        handler.post {
            eglManager?.createSurface(holder)
            eglManager?.makeCurrent()
            hasSurface = true
            
            val gl = glManager
            if (gl != null) {
                renderer.onContextCreated(gl, renderContext)
            }
            if (isVisible) {
                frameClock?.start()
            }
        }
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        handler.post {
            renderContext.width = width
            renderContext.height = height
            renderContext.aspect = width.toFloat() / height.toFloat()
            renderer.onSurfaceChanged(renderContext, width, height)
        }
    }

    fun onSurfaceDestroyed() {
        hasSurface = false
        handler.post {
            frameClock?.stop()
            eglManager?.destroySurface()
        }
    }

    fun setVisibility(visible: Boolean) {
        isVisible = visible
        if (visible) {
            sensorDispatcher.registerListener(this)
        } else {
            sensorDispatcher.unregisterListener(this)
        }
        handler.post {
            if (visible) {
                lastRenderedFrameNanos = 0L
                lastDayProgressCallbackNanos = 0L
                if (hasSurface) {
                    frameClock?.start()
                }
            } else {
                frameClock?.stop()
            }
        }
    }

    private fun publishFirstFrameIfNeeded() {
        if (hasRenderedFirstFrame) return
        hasRenderedFirstFrame = true
        mainHandler.post {
            onFirstFrameRendered()
        }
    }

    private fun publishDayProgressIfDue(frameTimeNanos: Long) {
        if (lastDayProgressCallbackNanos > 0L &&
            frameTimeNanos - lastDayProgressCallbackNanos < DAY_PROGRESS_CALLBACK_INTERVAL_NANOS
        ) {
            return
        }
        lastDayProgressCallbackNanos = frameTimeNanos
        val progress = renderer.frameState.dayProgress
        mainHandler.post {
            onDayProgressChanged(progress)
        }
    }

    fun loadWallpaper(definition: WallpaperDefinition) {
        handler.post {
            hasRenderedFirstFrame = false
            renderer.loadWallpaper(definition)
        }
    }

    fun triggerPreviewAnimation() {
        handler.post {
            renderer.triggerPreviewAnimation()
        }
    }

    override fun quitSafely(): Boolean {
        sensorDispatcher.unregisterListener(this)
        scope.cancel()
        handler.post {
            frameClock?.stop()
            renderer.onContextLost()
            glManager = null
            eglManager?.release()
            eglManager = null
        }
        return super.quitSafely()
    }

    private companion object {
        // Optimize day progress callback for smooth sun/moon animation
        // Changed from 1 second to 50ms for fluid motion
        const val DAY_PROGRESS_CALLBACK_INTERVAL_NANOS = 50_000_000L
    }
}
