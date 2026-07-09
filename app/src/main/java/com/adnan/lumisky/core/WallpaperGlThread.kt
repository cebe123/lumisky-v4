/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Gerçek live wallpaper için custom GL thread, Looper, EGL context ve Choreographer lifecycle yönetimi.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Gerçek live wallpaper için custom GL thread, Looper, EGL context ve Choreographer lifecycle yönetimi.
 */
package com.adnan.lumisky.core

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.view.SurfaceHolder
import com.adnan.lumisky.engine.LumiskyRenderer
import com.adnan.lumisky.engine.DaylightOverride
import com.adnan.lumisky.engine.RenderContext
import com.adnan.lumisky.engine.RuntimeScene
import com.adnan.lumisky.engine.SceneInputSnapshot
import com.adnan.lumisky.definition.QualityTier
import com.adnan.lumisky.definition.WallpaperDefinition
import com.adnan.lumisky.engine.gl.EglManager
import com.adnan.lumisky.engine.gl.GlResourceManager
import com.adnan.lumisky.registry.ShaderRegistry
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class WallpaperGlThread(
    private val context: Context,
    private val renderer: LumiskyRenderer,
    private val shaderRegistry: ShaderRegistry
) : HandlerThread("LumiskyGlThread") {

    private var handler: Handler? = null
    private var eglManager: EglManager? = null
    private var glManager: GlResourceManager? = null
    private val renderContext = RenderContext()
    private var frameClock: WallpaperFrameClock? = null
    private val pendingActions = ConcurrentLinkedQueue<() -> Unit>()
    private val eventQueue = EngineEventQueue()
    private val drainedEvents = mutableListOf<WallpaperEvent>()
    @Volatile var hasSurface = false

    @Volatile var isVisible = false
    @Volatile var batterySaver = false
    @Volatile var parallaxX = 0.0f
    @Volatile var parallaxY = 0.0f
    @Volatile var touchX = 0.0f
    @Volatile var touchY = 0.0f
    @Volatile var hasTouch = false
    @Volatile var preferredQualityTier: QualityTier? = null
    @Volatile var maxFps: Int = 30
    @Volatile var renderScale: Float = 1.0f
    @Volatile var postProcessEnabled: Boolean = true
    @Volatile var particleEffectsEnabled: Boolean = true
    @Volatile var videoPlaybackEnabled: Boolean = true
    @Volatile var sensorParallaxEnabled: Boolean = true
    @Volatile var telemetryEnabled: Boolean = true
    @Volatile var thermalEmergency: Boolean = false
    @Volatile var daylightOverride: DaylightOverride? = null
    private var lastRenderedFrameTimeNanos: Long = 0L

    override fun onLooperPrepared() {
        handler = Handler(looper)
        eglManager = EglManager().apply { initialize() }
        glManager = GlResourceManager(context, shaderRegistry)
        
        frameClock = WallpaperFrameClock { frameTimeNanos ->
            if (isVisible && hasSurface) {
                renderFrameIfDue(frameTimeNanos)
            }
        }

        while (true) {
            val action = pendingActions.poll() ?: break
            handler?.post(action)
        }
    }

    fun onSurfaceCreated(holder: SurfaceHolder) {
        postToGl {
            frameClock?.stop()
            eglManager?.destroySurface()
            eglManager?.createSurface(holder)
            eglManager?.makeCurrent()

            val gl = glManager
            if (gl != null) {
                renderer.onContextCreated(gl, renderContext)
            }
            hasSurface = true
            val surfaceFrame = holder.surfaceFrame
            val fallbackSize = SurfaceFrameSizeResolver.resolve(
                left = surfaceFrame.left,
                top = surfaceFrame.top,
                right = surfaceFrame.right,
                bottom = surfaceFrame.bottom
            )
            if (fallbackSize.isValid && (renderContext.width <= 0 || renderContext.height <= 0)) {
                applySurfaceSize(fallbackSize.width, fallbackSize.height)
            }
            renderFrameNow(System.nanoTime(), visibleForFrame = true)
            if (isVisible) {
                frameClock?.start()
            }
        }
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        postToGl {
            if (height == 0) return@postToGl
            applySurfaceSize(width, height)
            renderFrameNow(System.nanoTime(), visibleForFrame = true)
        }
    }

    fun switchScene(definition: WallpaperDefinition, newScene: RuntimeScene) {
        postToGl {
            if (!hasSurface) return@postToGl
            frameClock?.stop()
            renderer.switchWallpaper(definition, newScene, renderContext)
            renderFrameNow(System.nanoTime(), visibleForFrame = true)
            if (isVisible) {
                frameClock?.start()
            }
        }
    }

    fun onSurfaceDestroyed() {
        postToGl {
            hasSurface = false
            frameClock?.stop()
            renderer.onContextLost()
            eglManager?.destroySurface()
        }
    }

    fun setVisibility(visible: Boolean) {
        isVisible = visible
        postToGl {
            postEvent(if (visible) WallpaperEvent.ScreenOn else WallpaperEvent.ScreenOff)
            if (visible && hasSurface) {
                renderer.triggerLiveCatchUp(daylightOverride)
                frameClock?.start()
            } else {
                frameClock?.stop()
            }
        }
    }

    fun triggerLiveCatchUp() {
        postToGl {
            renderer.triggerLiveCatchUp(daylightOverride)
        }
    }

    fun postEvent(event: WallpaperEvent) {
        eventQueue.offer(event)
        postToGl {
            drainEvents()
            if (hasSurface) {
                renderFrameNow(System.nanoTime(), visibleForFrame = isVisible)
            }
        }
    }

    private fun renderFrameNow(frameTimeNanos: Long, visibleForFrame: Boolean) {
        if (!visibleForFrame) return
        if (!hasSurface) return
        if (renderContext.width <= 0 || renderContext.height <= 0) return
        if (renderer.activeScene == null) return
        lastRenderedFrameTimeNanos = frameTimeNanos
        drainEvents()
        renderContext.update(frameTimeNanos)

        val snapshot = SceneInputSnapshot(
            isVisible = visibleForFrame,
            batterySaver = batterySaver,
            parallaxX = parallaxX,
            parallaxY = parallaxY,
            touchX = touchX,
            touchY = touchY,
            hasTouch = hasTouch,
            preferredQualityTier = preferredQualityTier,
            daylightOverride = daylightOverride,
            renderScale = renderScale,
            postProcessEnabled = postProcessEnabled,
            particleEffectsEnabled = particleEffectsEnabled,
            videoPlaybackEnabled = videoPlaybackEnabled,
            sensorParallaxEnabled = sensorParallaxEnabled,
            telemetryEnabled = telemetryEnabled,
            thermalEmergency = thermalEmergency
        )

        eglManager?.makeCurrent()
        renderer.renderFrame(renderContext, snapshot)
        eglManager?.swapBuffers()
    }

    private fun applySurfaceSize(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        renderContext.width = width
        renderContext.height = height
        renderContext.aspect = width.toFloat() / height.toFloat()
        renderer.onSurfaceChanged(renderContext, width, height)
    }

    private fun renderFrameIfDue(frameTimeNanos: Long) {
        if (maxFps <= 0) return
        val resolvedMaxFps = maxFps.coerceIn(1, 120)
        val minFrameIntervalNanos = 1_000_000_000L / resolvedMaxFps
        if (lastRenderedFrameTimeNanos > 0L &&
            frameTimeNanos - lastRenderedFrameTimeNanos < minFrameIntervalNanos
        ) {
            return
        }
        renderFrameNow(frameTimeNanos, visibleForFrame = true)
    }

    override fun quitSafely(): Boolean {
        runOnGlBlocking {
            frameClock?.stop()
            renderer.onContextLost()
            glManager?.release()
            glManager = null
            eglManager?.release()
            eglManager = null
            hasSurface = false
        }
        return super.quitSafely()
    }

    private fun postToGl(action: () -> Unit) {
        val currentHandler = handler
        if (currentHandler != null) {
            currentHandler.post(action)
        } else {
            pendingActions.offer(action)
        }
    }

    private fun runOnGlBlocking(action: () -> Unit) {
        if (Thread.currentThread() === this) {
            action()
            return
        }
        val latch = CountDownLatch(1)
        postToGl {
            try {
                action()
            } finally {
                latch.countDown()
            }
        }
        latch.await(1500, TimeUnit.MILLISECONDS)
    }

    private fun drainEvents() {
        drainedEvents.clear()
        eventQueue.drainTo(drainedEvents)
        drainedEvents.forEach(renderer::onEvent)
    }
}
