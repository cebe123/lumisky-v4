/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Tek universal renderer. RuntimeScene, scheduler ve GL resource sistemini kullanarak frame çizer.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Tek universal renderer. RuntimeScene, scheduler ve GL resource sistemini kullanarak frame çizer.
 */
package com.adnan.lumisky.engine

import com.adnan.lumisky.assets.ShaderSourceLoader
import com.adnan.lumisky.core.WallpaperEvent
import com.adnan.lumisky.definition.WallpaperDefinition
import com.adnan.lumisky.engine.gl.GlResourceManager
import javax.inject.Inject

class LumiskyRenderer @Inject constructor(
    private val shaderSourceLoader: ShaderSourceLoader,
    private val scheduler: SceneScheduler,
    private val eventTriggerSystem: EventTriggerSystem,
    private val atmosphereController: AtmosphereController,
    private val parallaxController: ParallaxController,
    private val qualityController: AdaptiveQualityController
) {
    private val session = RenderEngineSession(
        runtimeProfile = RuntimeProfile.liveWallpaper(),
        shaderSourceLoader = shaderSourceLoader,
        scheduler = scheduler,
        eventTriggerSystem = eventTriggerSystem,
        atmosphereController = atmosphereController,
        parallaxController = parallaxController,
        qualityController = qualityController
    )

    var activeScene: RuntimeScene?
        get() = session.activeScene
        set(value) {
            session.activeScene = value
        }
    var activeDefinition: WallpaperDefinition?
        get() = session.activeDefinition
        set(value) {
            session.activeDefinition = value
        }
    val isContextCreated: Boolean
        get() = session.isContextCreated

    fun onContextCreated(gl: GlResourceManager, context: RenderContext) {
        session.onContextCreated(gl, context)
    }

    fun onSurfaceChanged(context: RenderContext, width: Int, height: Int) {
        session.onSurfaceChanged(context, width, height)
    }

    fun switchScene(newScene: RuntimeScene, context: RenderContext) {
        session.switchScene(newScene, context)
    }

    fun switchWallpaper(
        definition: WallpaperDefinition,
        newScene: RuntimeScene,
        context: RenderContext
    ) {
        session.activeDefinition = definition
        session.switchScene(newScene, context)
    }

    fun onEvent(event: WallpaperEvent) {
        session.onEvent(event)
    }

    fun renderFrame(
        context: RenderContext,
        inputSnapshot: SceneInputSnapshot
    ) {
        session.renderFrame(context, inputSnapshot)
    }

    fun onContextLost() {
        session.onContextLost()
    }

    fun triggerPreviewAnimation() {
        session.triggerPreviewAnimation()
    }

    fun triggerLiveCatchUp(daylightOverride: DaylightOverride?) {
        session.triggerLiveCatchUp(daylightOverride)
    }
}
