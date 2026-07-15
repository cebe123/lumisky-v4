/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Lumisky v5 Engine katmanı bileşeni.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Lumisky v5 Engine katmanı bileşeni.
 */
package com.example.lumisky.engine

import com.example.lumisky.assets.ShaderSourceLoader
import com.example.lumisky.core.WallpaperEvent
import com.example.lumisky.definition.WallpaperDefinition
import com.example.lumisky.engine.gl.GlResourceManager
import com.example.lumisky.engine.pipeline.CachedLayerRenderer
import com.example.lumisky.engine.pipeline.FinalCompositeRenderer
import com.example.lumisky.layers.LayerCacheMode

class RenderEngineSession(
    var runtimeProfile: RuntimeProfile,
    private val shaderSourceLoader: ShaderSourceLoader,
    private val scheduler: SceneScheduler,
    private val eventTriggerSystem: EventTriggerSystem,
    private val atmosphereController: AtmosphereController,
    private val parallaxController: ParallaxController,
    private val qualityController: AdaptiveQualityController
) {
    private val finalCompositeRenderer = FinalCompositeRenderer()
    private val celestialMotionController = CelestialMotionController()
    private val previewTimeMotionController = PreviewTimeMotionController()
    private val liveWallpaperCatchUpController = LiveWallpaperCatchUpController()
    private val frameDemandController = FrameDemandController()
    private var cachedLayerRenderer: CachedLayerRenderer? = null

    val frameState = MutableRenderFrameState()
    val sceneState = SceneState()

    var activeScene: RuntimeScene? = null
        set(value) {
            field = value
            if (sceneState.isCatchUpAnimating) {
                sceneState.resetCatchUpTimer()
            }
        }
    var activeDefinition: WallpaperDefinition? = null
        set(value) {
            if (field?.id != value?.id) {
                previewTimeMotionController.reset(value?.id)
            }
            field = value
            sceneState.wallpaperId = value?.id.orEmpty()
        }
    var isContextCreated = false
        private set

    fun onContextCreated(gl: GlResourceManager, context: RenderContext) {
        frameState.gl = gl
        cachedLayerRenderer = CachedLayerRenderer(gl, shaderSourceLoader)
        preloadTextures(gl)
        activeScene?.onCreateGl(gl, context)
        isContextCreated = true
        frameDemandController.request(FrameDemandReason.INITIAL_FRAME)
    }

    fun onSurfaceChanged(context: RenderContext, width: Int, height: Int) {
        frameState.width = width
        frameState.height = height
        activeScene?.onSurfaceChanged(context, width, height)
        frameDemandController.request(FrameDemandReason.SURFACE_CHANGED)
    }

    fun switchScene(newScene: RuntimeScene, context: RenderContext) {
        if (!isContextCreated) {
            activeScene = newScene
            frameDemandController.request(FrameDemandReason.SCENE_SWITCH)
            return
        }
        val gl = frameState.gl
        val previousScene = activeScene
        previousScene?.onDestroyGl(gl)
        cachedLayerRenderer?.clear()
        gl.textures.clear()
        activeScene = newScene
        preloadTextures(gl)
        activeScene?.onCreateGl(gl, context)
        if (frameState.width > 0 && frameState.height > 0) {
            activeScene?.onSurfaceChanged(context, frameState.width, frameState.height)
        }
        frameDemandController.request(FrameDemandReason.SCENE_SWITCH)
    }

    fun onEvent(event: WallpaperEvent) {
        val scene = activeScene ?: return
        eventTriggerSystem.handleEvent(event, activeDefinition, scene)
        scene.onEvent(event)
        frameDemandController.request(FrameDemandReason.EVENT_ANIMATION)
    }

    fun renderFrame(
        context: RenderContext,
        inputSnapshot: SceneInputSnapshot
    ) {
        if (!isContextCreated || !frameState.isGlInitialized()) return
        val scene = activeScene ?: return
        frameState.gl.textures.beginFrame()
        scene.layers.forEach { layer ->
            layer.pendingFrameDemandReason()?.let(frameDemandController::request)
        }

        sceneState.update(
            deltaTime = context.deltaTimeSeconds,
            timeZoneId = resolveSceneTimeZoneId(inputSnapshot.daylightOverride)
        )
        sceneState.isVisible = inputSnapshot.isVisible
        sceneState.batterySaver = inputSnapshot.batterySaver

        if (runtimeProfile.mode != RuntimeMode.LIVE_WALLPAPER) {
            sceneState.dayProgress = previewTimeMotionController.resolveDayProgress(
                definition = activeDefinition,
                deltaTimeSeconds = context.deltaTimeSeconds,
                durationSeconds = if (runtimeProfile.mode == RuntimeMode.PREVIEW_CARD) {
                    PreviewTimeMotionController.CATALOG_FOCUS_DURATION_SECONDS
                } else {
                    PreviewTimeMotionController.FULL_DAY_LOOP_DURATION_SECONDS
                }
            )
        }

        sceneState.quality = runtimeProfile.overrideQualityTier
            ?: inputSnapshot.preferredQualityTier
            ?: qualityController.resolveTier(
                activeDefinition,
                inputSnapshot.batterySaver,
                inputSnapshot.thermalEmergency
            )

        atmosphereController.update(sceneState)

        frameState.timeSeconds = sceneState.timeSeconds
        frameState.deltaTimeSeconds = context.deltaTimeSeconds
        frameState.isVisible = inputSnapshot.isVisible
        frameState.runtimeMode = runtimeProfile.mode
        frameState.quality = sceneState.quality
        frameState.dayProgress = sceneState.dayProgress
        frameState.renderScale = runtimeProfile.renderScale * inputSnapshot.renderScale
        frameState.postProcessEnabled = inputSnapshot.postProcessEnabled
        frameState.particleEffectsEnabled = inputSnapshot.particleEffectsEnabled
        frameState.videoPlaybackEnabled = runtimeProfile.playVideo && inputSnapshot.videoPlaybackEnabled
        frameState.sensorParallaxEnabled = inputSnapshot.sensorParallaxEnabled
        frameState.telemetryEnabled = inputSnapshot.telemetryEnabled
        frameState.thermalEmergency = inputSnapshot.thermalEmergency
        celestialMotionController.resolveInto(
            definition = activeDefinition,
            dayProgress = frameState.dayProgress,
            daylightOverride = inputSnapshot.daylightOverride,
            output = frameState
        )

        parallaxController.update(
            inputSnapshot.parallaxX,
            inputSnapshot.parallaxY,
            activeDefinition,
            sceneState,
            frameState
        )

        scene.layers.forEach { layer ->
            if (scheduler.shouldUpdate(
                    layer,
                    context.frameTimeNanos,
                    frameState.thermalEmergency || sceneState.batterySaver,
                    sceneId = scene.id
                )
            ) {
                layer.update(frameState)
            }
            val cacheMode = layer.cacheMode
            if (cacheMode == LayerCacheMode.FBO_CACHE &&
                scheduler.shouldRefreshCache(
                    layer,
                    context.frameTimeNanos,
                    frameState.thermalEmergency || sceneState.batterySaver,
                    sceneId = scene.id
                )
            ) {
                cachedLayerRenderer?.refresh(layer, frameState)
            }
        }

        finalCompositeRenderer.prepareFrame(frameState.width, frameState.height)

        scene.orderedLayers.forEach { layer ->
            when (layer.cacheMode) {
                LayerCacheMode.FBO_CACHE -> cachedLayerRenderer?.compositeLastTexture(layer, frameState)
                else -> layer.render(frameState)
            }
        }
    }

    fun onContextLost() {
        isContextCreated = false
        if (frameState.isGlInitialized()) {
            activeScene?.onContextLost()
            frameState.gl.onContextLost()
        }
        cachedLayerRenderer?.invalidate()
        cachedLayerRenderer = null
    }

    fun onFramePresented() {
        frameDemandController.onFramePresented()
    }

    fun triggerPreviewAnimation() {
        if (runtimeProfile.mode == RuntimeMode.PREVIEW_FULLSCREEN) {
            previewTimeMotionController.startFullDayLoop(activeDefinition?.id)
        } else {
            previewTimeMotionController.startFocusAnimation(activeDefinition)
        }
    }

    fun triggerLiveCatchUp(daylightOverride: DaylightOverride?) {
        val window = liveWallpaperCatchUpController.resolveWindow(
            definition = activeDefinition,
            daylightOverride = daylightOverride
        )
        sceneState.triggerDayProgressCatchUp(
            startProgress = window.startProgress,
            targetProgress = window.targetProgress
        )
    }

    private fun preloadTextures(gl: GlResourceManager) {
        val paths = activeDefinition?.layers.orEmpty().flatMap { layer ->
            buildList {
                layer.source?.let(::add)
                addAll(layer.textures.map { it.path })
            }
        }
        gl.textures.preload(paths, frameState.quality)
    }

    val hasPendingTextureWork: Boolean
        get() = frameState.isGlInitialized() && frameState.gl.textures.hasPendingWork

    val isPreviewAnimationRunning: Boolean
        get() = previewTimeMotionController.isAnimating

    val hasFrameDemand: Boolean
        get() = frameDemandController.hasDemand(System.nanoTime()) ||
            activeScene?.layers?.any { it.hasPendingFrameDemand } == true

    private fun resolveSceneTimeZoneId(daylightOverride: DaylightOverride?): String {
        return daylightOverride?.timeZoneId
            ?.takeIf { it.isNotBlank() }
            ?: activeDefinition?.daylight?.timeZoneId
            ?: ""
    }
}
