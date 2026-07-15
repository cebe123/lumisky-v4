/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Gerçek live wallpaper için custom GL thread, Looper, EGL context ve Choreographer lifecycle yönetimi.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Gerçek live wallpaper için custom GL thread, Looper, EGL context ve Choreographer lifecycle yönetimi.
 */
package com.example.lumisky.core

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.view.SurfaceHolder
import com.example.lumisky.engine.LumiskyRenderer
import com.example.lumisky.engine.DaylightOverride
import com.example.lumisky.engine.RenderContext
import com.example.lumisky.engine.RuntimeScene
import com.example.lumisky.engine.SceneInputSnapshot
import com.example.lumisky.engine.SceneFramePacingPolicy
import com.example.lumisky.definition.QualityTier
import com.example.lumisky.definition.WallpaperDefinition
import com.example.lumisky.engine.gl.EglManager
import com.example.lumisky.engine.gl.EglSwapResult
import com.example.lumisky.engine.gl.GlResourceManager
import com.example.lumisky.registry.ShaderRegistry
import java.util.concurrent.ConcurrentLinkedQueue

class WallpaperGlThread(
    private val context: Context,
    private val renderer: LumiskyRenderer,
    private val shaderRegistry: ShaderRegistry,
    private val isPreview: Boolean = false,
    private val onSceneCommitted: (WallpaperDefinition, RuntimeScene) -> Unit = { _, _ -> }
) : HandlerThread("LumiskyGlThread") {

    private var handler: Handler? = null
    private var eglManager: EglManager? = null
    private var glManager: GlResourceManager? = null
    private val renderContext = RenderContext()
    private var frameClock: WallpaperFrameClock? = null
    private val renderThreadGuard = RenderThreadGuard()
    private val shutdownHandoff = BoundedRenderThreadHandoff(SHUTDOWN_TIMEOUT_MILLIS)

    init {
        if (isPreview) {
            renderer.runtimeProfile = com.example.lumisky.engine.RuntimeProfile.fullscreenPreview()
        }
    }
    private val pendingActions = ConcurrentLinkedQueue<() -> Unit>()
    private val eventQueue = EngineEventQueue()
    private val commandMailbox = RenderCommandMailbox()
    private val drainedEvents = mutableListOf<WallpaperEvent>()
    private val drainedCommands = mutableListOf<RenderCommand>()
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
    private val sceneCommitTransaction = SceneCommitTransaction<Pair<WallpaperDefinition, RuntimeScene>>()
    private val inputSnapshot = SceneInputSnapshot(
        isVisible = false,
        batterySaver = false,
        parallaxX = 0f,
        parallaxY = 0f,
        touchX = 0f,
        touchY = 0f,
        hasTouch = false
    )

    override fun onLooperPrepared() {
        renderThreadGuard.bindCurrentThread()
        handler = Handler(looper)
        eglManager = EglManager().apply { initialize() }
        glManager = GlResourceManager(context, shaderRegistry)
        
        frameClock = WallpaperFrameClock { frameTimeNanos ->
            if (RenderLifecycleGate.canRender(isVisible, hasSurface)) {
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
            val egl = ensureEglManager()
            egl.destroySurface()
            egl.createSurface(holder)
            egl.makeCurrent()

            val gl = glManager
            if (gl != null && !renderer.isContextCreated) {
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
            renderFrameNow(System.nanoTime(), visibleForFrame = isVisible)
            if (isVisible) {
                frameClock?.start()
            }
        }
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        postToGl {
            if (height == 0) return@postToGl
            applySurfaceSize(width, height)
            renderFrameNow(System.nanoTime(), visibleForFrame = isVisible)
        }
    }

    fun switchScene(definition: WallpaperDefinition, newScene: RuntimeScene) {
        postToGl {
            sceneCommitTransaction.stage(definition to newScene)
            frameClock?.stop()
            renderer.switchWallpaper(definition, newScene, renderContext)
            if (isPreview) {
                renderer.triggerPreviewAnimation()
            }
            renderFrameNow(System.nanoTime(), visibleForFrame = isVisible)
            if (isVisible) {
                frameClock?.start()
            }
        }
    }

    fun onSurfaceDestroyed() {
        postToGl {
            hasSurface = false
            frameClock?.stop()
            eglManager?.destroySurface()
        }
    }

    fun setVisibility(visible: Boolean) {
        submit(RenderCommand.SetVisibility(visible))
    }

    fun triggerLiveCatchUp() {
        postToGl {
            if (isPreview) return@postToGl
            renderer.triggerLiveCatchUp(daylightOverride)
            if (isVisible && hasSurface) frameClock?.start()
        }
    }

    fun stageSceneCommit(definition: WallpaperDefinition, scene: RuntimeScene) {
        postToGl { sceneCommitTransaction.stage(definition to scene) }
    }

    fun submit(command: RenderCommand) {
        commandMailbox.offer(command)
        postToGl {
            drainCommands()
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
        renderThreadGuard.checkCurrentThread()
        if (!RenderLifecycleGate.canRender(visibleForFrame, hasSurface)) return
        if (renderContext.width <= 0 || renderContext.height <= 0) return
        if (renderer.activeScene == null) return
        lastRenderedFrameTimeNanos = frameTimeNanos
        drainEvents()
        renderContext.update(frameTimeNanos)

        inputSnapshot.isVisible = visibleForFrame
        inputSnapshot.batterySaver = batterySaver
        inputSnapshot.parallaxX = parallaxX
        inputSnapshot.parallaxY = parallaxY
        inputSnapshot.touchX = touchX
        inputSnapshot.touchY = touchY
        inputSnapshot.hasTouch = hasTouch
        inputSnapshot.preferredQualityTier = preferredQualityTier
        inputSnapshot.daylightOverride = daylightOverride
        inputSnapshot.renderScale = renderScale
        inputSnapshot.postProcessEnabled = postProcessEnabled
        inputSnapshot.particleEffectsEnabled = particleEffectsEnabled
        inputSnapshot.videoPlaybackEnabled = videoPlaybackEnabled
        inputSnapshot.sensorParallaxEnabled = sensorParallaxEnabled
        inputSnapshot.telemetryEnabled = telemetryEnabled
        inputSnapshot.thermalEmergency = thermalEmergency

        eglManager?.makeCurrent()
        renderer.renderFrame(renderContext, inputSnapshot)
        when (eglManager?.swapBuffers()) {
            EglSwapResult.SUCCESS -> {
                renderer.onFramePresented()
                sceneCommitTransaction.takeAfterSwap(succeeded = true)?.let { (definition, scene) ->
                    onSceneCommitted(definition, scene)
                }
            }
            EglSwapResult.CONTEXT_LOST -> handleContextLost()
            else -> Unit
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

    private fun applySurfaceSize(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        renderContext.width = width
        renderContext.height = height
        renderContext.aspect = width.toFloat() / height.toFloat()
        renderer.onSurfaceChanged(renderContext, width, height)
    }

    private fun renderFrameIfDue(frameTimeNanos: Long) {
        val minFrameIntervalNanos = SceneFramePacingPolicy.frameIntervalNanos(
            layers = renderer.activeScene?.layers.orEmpty(),
            maxFps = maxFps,
            batterySaver = batterySaver,
            forceContinuous = renderer.isCatchUpAnimating ||
                renderer.isPreviewAnimationRunning ||
                renderer.hasFrameDemand ||
                renderer.hasPendingTextureWork
        ) ?: return
        if (lastRenderedFrameTimeNanos > 0L &&
            frameTimeNanos - lastRenderedFrameTimeNanos < minFrameIntervalNanos
        ) {
            val remainingNanos = minFrameIntervalNanos - (frameTimeNanos - lastRenderedFrameTimeNanos)
            frameClock?.postNextFrame((remainingNanos / 1_000_000).coerceAtLeast(0))
            return
        }
        renderFrameNow(frameTimeNanos, visibleForFrame = true)
        frameClock?.postNextFrame()
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
        shutdownHandoff.run(
            isOwnerThread = Thread.currentThread() === this,
            post = ::postToGl,
            action = action
        )
    }

    private fun drainEvents() {
        drainedEvents.clear()
        eventQueue.drainTo(drainedEvents)
        drainedEvents.forEach(renderer::onEvent)
    }

    private fun drainCommands() {
        drainedCommands.clear()
        commandMailbox.drainTo(drainedCommands)
        for (command in drainedCommands) {
            when (command) {
                is RenderCommand.SetVisibility -> {
                    isVisible = command.visible
                    eventQueue.offer(if (command.visible) WallpaperEvent.ScreenOn else WallpaperEvent.ScreenOff)
                    drainEvents()
                    if (command.visible && hasSurface) frameClock?.start() else frameClock?.stop()
                }
                is RenderCommand.SetParallax -> {
                    parallaxX = command.x
                    parallaxY = command.y
                    eventQueue.offer(WallpaperEvent.ParallaxChanged(command.x, command.y))
                }
                is RenderCommand.SetTouch -> {
                    touchX = command.x
                    touchY = command.y
                    hasTouch = command.active
                    if (command.active) eventQueue.offer(WallpaperEvent.Touch(command.x, command.y))
                }
                is RenderCommand.SetRuntimePolicy -> {
                    preferredQualityTier = command.qualityTier
                    maxFps = command.maxFps
                    renderScale = command.renderScale
                    postProcessEnabled = command.postProcessEnabled
                    particleEffectsEnabled = command.particleEffectsEnabled
                    videoPlaybackEnabled = command.videoPlaybackEnabled
                    sensorParallaxEnabled = command.sensorParallaxEnabled
                    telemetryEnabled = command.telemetryEnabled
                }
                is RenderCommand.SetPowerPolicy -> {
                    batterySaver = command.batterySaver
                    thermalEmergency = command.thermalEmergency
                }
                is RenderCommand.SetDaylight -> daylightOverride = command.daylight
                else -> Unit
            }
        }
        if (hasSurface && isVisible && drainedCommands.isNotEmpty()) renderFrameNow(System.nanoTime(), true)
    }

    private companion object {
        const val SHUTDOWN_TIMEOUT_MILLIS = 1_500L
    }
}
