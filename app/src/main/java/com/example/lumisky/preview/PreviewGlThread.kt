/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Lumisky v5 Preview katmanı bileşeni.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Lumisky v5 Preview katmanı bileşeni.
 */
package com.example.lumisky.preview

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.PowerManager
import android.view.SurfaceHolder
import android.view.WindowManager
import com.example.lumisky.core.BoundedRenderThreadHandoff
import com.example.lumisky.core.RenderLifecycleGate
import com.example.lumisky.core.WallpaperFrameClock
import com.example.lumisky.data.RuntimeSettingsPolicy
import com.example.lumisky.data.RuntimeSettingsPolicyResult
import com.example.lumisky.data.SettingsRepository
import com.example.lumisky.definition.WallpaperDefinition
import com.example.lumisky.device.ThermalStateController
import com.example.lumisky.engine.RenderContext
import com.example.lumisky.engine.RuntimeMode
import com.example.lumisky.engine.AdaptiveFrameRateGovernor
import com.example.lumisky.engine.PreviewFrameRateCap
import com.example.lumisky.engine.SceneInputSnapshot
import com.example.lumisky.engine.gl.EglManager
import com.example.lumisky.engine.gl.EglSwapResult
import com.example.lumisky.engine.gl.GlResourceManager
import com.example.lumisky.registry.ShaderRegistry
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
    private val sensorDispatcher: com.example.lumisky.device.SensorDispatcher,
    private val onDayProgressChanged: (Float) -> Unit = {},
    private val onFirstFrameRendered: () -> Unit = {}
) : HandlerThread("LumiskyPreviewGlThread"), com.example.lumisky.device.SensorDispatcher.SensorListener {

    override fun onSensorValues(x: Float, y: Float) {
        parallaxX = x
        parallaxY = y
        handler.post {
            if (isVisible && hasSurface) {
                frameClock?.stop()
                frameClock?.start()
            }
        }
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
    private val inputSnapshot = SceneInputSnapshot(
        isVisible = false,
        batterySaver = false,
        parallaxX = 0f,
        parallaxY = 0f,
        touchX = 0f,
        touchY = 0f,
        hasTouch = false
    )
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val shutdownHandoff = BoundedRenderThreadHandoff(SHUTDOWN_TIMEOUT_MILLIS)
    private val frameRateGovernor by lazy {
        if (renderer.runtimeProfile.mode == RuntimeMode.PREVIEW_CARD) {
            AdaptiveFrameRateGovernor(intArrayOf(60, 30, 15, 10), initialFps = 30)
        } else {
            AdaptiveFrameRateGovernor(intArrayOf(120, 60, 30, 15), initialFps = 60)
        }
    }

    @Volatile var isVisible = false
    @Volatile var batterySaver = false
    @Volatile var maxFps = 30
    @Volatile private var policyMaxFps = 30
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
                    sceneMaxFps = renderer.runtimeProfile.maxFps,
                    batterySaverSceneMaxFps = 15
                )
            }.collectLatest { policy ->
                val runtimeMaxFps = PreviewFrameRateCap.resolve(renderer.runtimeProfile.maxFps, displayMaxFps())
                policyMaxFps = policy.maxFps
                maxFps = if (runtimeMaxFps > 0) {
                    minOf(frameRateGovernor.targetFps, policyMaxFps, runtimeMaxFps)
                } else {
                    0
                }
                batterySaver = maxFps <= 15
                minFrameIntervalNanos = if (maxFps > 0) {
                    1_000_000_000L / maxFps
                } else {
                    Long.MAX_VALUE
                }
            }
        }

        frameClock = WallpaperFrameClock { frameTimeNanos ->
            if (RenderLifecycleGate.canRender(isVisible, hasSurface)) {
                val previousFrameNanos = lastRenderedFrameNanos
                val frameIntervalNanos = if (renderer.shouldContinueRendering) {
                    1_000_000_000L / maxFps.coerceAtLeast(1)
                } else {
                    minFrameIntervalNanos
                }
                if (frameTimeNanos - lastRenderedFrameNanos < frameIntervalNanos) {
                    val remainingNanos = frameIntervalNanos - (frameTimeNanos - lastRenderedFrameNanos)
                    frameClock?.postNextFrame((remainingNanos / 1_000_000).coerceAtLeast(0))
                    return@WallpaperFrameClock
                }
                if (previousFrameNanos > 0L) {
                    val deadlineMissed = frameTimeNanos - previousFrameNanos > frameIntervalNanos * 2
                    maxFps = minOf(
                        frameRateGovernor.report(deadlineMissed, batterySaver),
                        policyMaxFps,
                        PreviewFrameRateCap.resolve(renderer.runtimeProfile.maxFps, displayMaxFps())
                    )
                    minFrameIntervalNanos = 1_000_000_000L / maxFps.coerceAtLeast(1)
                }
                lastRenderedFrameNanos = frameTimeNanos
                renderContext.update(frameTimeNanos)
                
                inputSnapshot.isVisible = isVisible
                inputSnapshot.batterySaver = batterySaver
                inputSnapshot.parallaxX = parallaxX
                inputSnapshot.parallaxY = parallaxY
                inputSnapshot.touchX = touchX
                inputSnapshot.touchY = touchY
                inputSnapshot.hasTouch = hasTouch
                inputSnapshot.thermalEmergency = thermalStateController.isThermalThrottling
                
                eglManager?.makeCurrent()
                renderer.renderFrame(renderContext, inputSnapshot)
                when (eglManager?.swapBuffers()) {
                    EglSwapResult.SUCCESS -> renderer.onFramePresented()
                    EglSwapResult.CONTEXT_LOST -> handleContextLost()
                    else -> Unit
                }
                if (renderer.activeScene != null) {
                    publishFirstFrameIfNeeded()
                    publishDayProgressIfDue(frameTimeNanos)
                }
            }
            if (renderer.shouldContinueRendering) {
                frameClock?.postNextFrame()
            }
        }
    }

    fun onSurfaceCreated(holder: SurfaceHolder) {
        handler.post {
            val egl = ensureEglManager()
            egl.createSurface(holder)
            egl.makeCurrent()
            hasSurface = true
            
            val gl = glManager
            if (gl != null && !renderer.isContextCreated) {
                renderer.onContextCreated(gl, renderContext)
            }
            if (isVisible) {
                frameClock?.start()
            }
        }
    }

    fun onSurfaceCreated(surface: android.view.Surface) {
        handler.post {
            val egl = ensureEglManager()
            egl.createSurface(surface)
            egl.makeCurrent()
            hasSurface = true
            
            val gl = glManager
            if (gl != null && !renderer.isContextCreated) {
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
        if (RenderLifecycleGate.canRunSensor(visible)) {
            sensorDispatcher.registerListener(
                listener = this,
                maxUpdatesPerSecond = if (renderer.runtimeProfile.mode == RuntimeMode.PREVIEW_CARD) 30 else 60
            )
        } else {
            sensorDispatcher.unregisterListener(this)
        }
        handler.post {
            if (visible) {
                lastRenderedFrameNanos = 0L
                lastDayProgressCallbackNanos = 0L
                renderer.triggerPreviewAnimation() // GUARANTEE animation starts before first frame!
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
            if (RenderLifecycleGate.canPublishCallback(isVisible)) onFirstFrameRendered()
        }
    }

    private fun ensureEglManager(): EglManager {
        eglManager?.takeIf { it.hasContext }?.let { return it }
        eglManager?.release()
        return EglManager().also {
            it.initialize()
            eglManager = it
        }
    }

    private fun handleContextLost() {
        hasSurface = false
        frameClock?.stop()
        renderer.onContextLost()
    }

    private fun displayMaxFps(): Int {
        @Suppress("DEPRECATION")
        val display = (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)
            ?.defaultDisplay
            ?: return DEFAULT_DISPLAY_MAX_FPS
        return display.supportedModes.maxOfOrNull { it.refreshRate.toInt() }
            ?.coerceAtLeast(DEFAULT_DISPLAY_MAX_FPS)
            ?: DEFAULT_DISPLAY_MAX_FPS
    }

    private fun publishDayProgressIfDue(frameTimeNanos: Long) {
        if (!RenderLifecycleGate.canPublishCallback(isVisible)) return
        if (lastDayProgressCallbackNanos > 0L &&
            frameTimeNanos - lastDayProgressCallbackNanos < DAY_PROGRESS_CALLBACK_INTERVAL_NANOS
        ) {
            return
        }
        lastDayProgressCallbackNanos = frameTimeNanos
        val progress = renderer.frameState.dayProgress
        mainHandler.post {
            if (RenderLifecycleGate.canPublishCallback(isVisible)) onDayProgressChanged(progress)
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
        shutdownHandoff.run(
            isOwnerThread = Thread.currentThread() === this,
            post = { action -> handler.post(action) }
        ) {
            frameClock?.stop()
            renderer.onContextLost()
            glManager?.release()
            glManager = null
            eglManager?.release()
            eglManager = null
        }
        return super.quitSafely()
    }

    private companion object {
        // The GL renderer animates celestial motion; Compose only needs badge updates.
        const val DAY_PROGRESS_CALLBACK_INTERVAL_NANOS = 500_000_000L
        const val SHUTDOWN_TIMEOUT_MILLIS = 1_500L
        const val DEFAULT_DISPLAY_MAX_FPS = 60
    }
}
