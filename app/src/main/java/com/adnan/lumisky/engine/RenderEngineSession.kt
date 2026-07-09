/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Lumisky v5 Engine katmanı bileşeni.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Lumisky v5 Engine katmanı bileşeni.
 */
package com.adnan.lumisky.engine

import com.adnan.lumisky.assets.ShaderSourceLoader
import com.adnan.lumisky.core.WallpaperEvent
import com.adnan.lumisky.definition.WallpaperDefinition
import com.adnan.lumisky.engine.gl.GlResourceManager
import com.adnan.lumisky.engine.pipeline.CachedLayerRenderer
import com.adnan.lumisky.engine.pipeline.FinalCompositeRenderer
import com.adnan.lumisky.engine.pipeline.LayerComposer
import com.adnan.lumisky.layers.LayerCacheMode

class RenderEngineSession(
    private val runtimeProfile: RuntimeProfile,
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
    private var cachedLayerRenderer: CachedLayerRenderer? = null

    val frameState = MutableRenderFrameState()
    val sceneState = SceneState()

    var activeScene: RuntimeScene? = null
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
        activeScene?.onCreateGl(gl, context)
        isContextCreated = true
    }

    fun onSurfaceChanged(context: RenderContext, width: Int, height: Int) {
        frameState.width = width
        frameState.height = height
        activeScene?.onSurfaceChanged(context, width, height)
    }

    fun switchScene(newScene: RuntimeScene, context: RenderContext) {
        val gl = if (frameState.isGlInitialized()) frameState.gl else return
        val previousScene = activeScene
        previousScene?.onDestroyGl(gl)
        cachedLayerRenderer?.clear()
        activeScene = newScene
        activeScene?.onCreateGl(gl, context)
        if (frameState.width > 0 && frameState.height > 0) {
            activeScene?.onSurfaceChanged(context, frameState.width, frameState.height)
        }
    }

    fun onEvent(event: WallpaperEvent) {
        val scene = activeScene ?: return
        eventTriggerSystem.handleEvent(event, activeDefinition, scene)
        scene.onEvent(event)
    }

    fun renderFrame(
        context: RenderContext,
        inputSnapshot: SceneInputSnapshot
    ) {
        if (!isContextCreated || !frameState.isGlInitialized()) return
        val scene = activeScene ?: return

        sceneState.update(
            deltaTime = context.deltaTimeSeconds,
            timeZoneId = resolveSceneTimeZoneId(inputSnapshot.daylightOverride)
        )
        if (runtimeProfile.mode == RuntimeMode.PREVIEW_CARD) {
            sceneState.dayProgress = previewTimeMotionController.resolveDayProgress(
                definition = activeDefinition,
                deltaTimeSeconds = context.deltaTimeSeconds
            )
        }
        sceneState.isVisible = inputSnapshot.isVisible
        sceneState.batterySaver = inputSnapshot.batterySaver
        sceneState.quality = runtimeProfile.overrideQualityTier
            ?: inputSnapshot.preferredQualityTier
            ?: qualityController.resolveTier(activeDefinition, inputSnapshot.batterySaver, false)

        atmosphereController.update(sceneState)

        frameState.timeSeconds = sceneState.timeSeconds
        frameState.deltaTimeSeconds = context.deltaTimeSeconds
        frameState.quality = sceneState.quality
        frameState.dayProgress = sceneState.dayProgress
        frameState.renderScale = runtimeProfile.renderScale * inputSnapshot.renderScale
        frameState.postProcessEnabled = inputSnapshot.postProcessEnabled
        frameState.particleEffectsEnabled = inputSnapshot.particleEffectsEnabled
        frameState.videoPlaybackEnabled = runtimeProfile.playVideo && inputSnapshot.videoPlaybackEnabled
        frameState.sensorParallaxEnabled = inputSnapshot.sensorParallaxEnabled
        frameState.telemetryEnabled = inputSnapshot.telemetryEnabled
        frameState.thermalEmergency = inputSnapshot.thermalEmergency
        val celestial = celestialMotionController.resolve(
            definition = activeDefinition,
            dayProgress = frameState.dayProgress,
            daylightOverride = inputSnapshot.daylightOverride
        )
        frameState.sunX = celestial.sunX
        frameState.sunY = celestial.sunY
        frameState.moonX = celestial.moonX
        frameState.moonY = celestial.moonY
        frameState.drawSun = celestial.drawSun
        frameState.isNight = celestial.isNight
        frameState.minute = celestial.minute
        frameState.sunriseMinute = celestial.sunriseMinute
        frameState.sunsetMinute = celestial.sunsetMinute
        frameState.solarNoonMinute = celestial.solarNoonMinute
        frameState.nightAmount = celestial.nightAmount
        frameState.horizonY = celestial.horizonY
        frameState.sunColorR = celestial.sunColorR
        frameState.sunColorG = celestial.sunColorG
        frameState.sunColorB = celestial.sunColorB

        parallaxController.update(
            inputSnapshot.parallaxX,
            inputSnapshot.parallaxY,
            activeDefinition,
            sceneState,
            frameState
        )

        scene.layers.forEach { layer ->
            if (scheduler.shouldUpdate(layer, context.frameTimeNanos, frameState.thermalEmergency || sceneState.batterySaver)) {
                layer.update(frameState)
            }
            val cacheMode = layerCacheMode(layer.framePolicy.cacheMode)
            if (cacheMode == LayerCacheMode.FBO_CACHE &&
                scheduler.shouldRefreshCache(layer, context.frameTimeNanos, frameState.thermalEmergency || sceneState.batterySaver)
            ) {
                cachedLayerRenderer?.refresh(layer, frameState)
            }
        }

        finalCompositeRenderer.prepareFrame(frameState.width, frameState.height)

        LayerComposer.compose(scene.layers).forEach { layer ->
            when (layerCacheMode(layer.framePolicy.cacheMode)) {
                LayerCacheMode.FBO_CACHE -> cachedLayerRenderer?.compositeLastTexture(layer, frameState)
                else -> layer.render(frameState)
            }
        }
    }

    fun onContextLost() {
        isContextCreated = false
        if (frameState.isGlInitialized()) {
            activeScene?.onDestroyGl(frameState.gl)
            frameState.gl.onContextLost()
        }
        cachedLayerRenderer?.clear()
        cachedLayerRenderer = null
    }

    fun triggerPreviewAnimation() {
        sceneState.triggerFastForward()
    }

    fun triggerLiveCatchUp(daylightOverride: DaylightOverride?) {
        if (runtimeProfile.mode != RuntimeMode.LIVE_WALLPAPER) return
        val window = liveWallpaperCatchUpController.resolveWindow(
            definition = activeDefinition,
            daylightOverride = daylightOverride
        )
        sceneState.triggerDayProgressCatchUp(
            startProgress = window.startProgress,
            targetProgress = window.targetProgress
        )
    }

    private fun layerCacheMode(value: String): LayerCacheMode {
        return try {
            LayerCacheMode.valueOf(value)
        } catch (e: Throwable) {
            LayerCacheMode.NONE
        }
    }

    private fun resolveSceneTimeZoneId(daylightOverride: DaylightOverride?): String {
        return daylightOverride?.timeZoneId
            ?.takeIf { it.isNotBlank() }
            ?: activeDefinition?.daylight?.timeZoneId
            ?: ""
    }
}
