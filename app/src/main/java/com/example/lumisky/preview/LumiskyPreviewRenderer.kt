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

import com.example.lumisky.assets.ShaderSourceLoader
import com.example.lumisky.definition.WallpaperDefinition
import com.example.lumisky.engine.AdaptiveQualityController
import com.example.lumisky.engine.AtmosphereController
import com.example.lumisky.engine.EventTriggerSystem
import com.example.lumisky.engine.MutableRenderFrameState
import com.example.lumisky.engine.ParallaxController
import com.example.lumisky.engine.RenderEngineSession
import com.example.lumisky.engine.RenderContext
import com.example.lumisky.engine.RuntimeProfile
import com.example.lumisky.engine.RuntimeScene
import com.example.lumisky.engine.SceneInputSnapshot
import com.example.lumisky.engine.SceneScheduler
import com.example.lumisky.engine.SceneState
import com.example.lumisky.engine.gl.GlResourceManager
import com.example.lumisky.registry.SceneFactory

class LumiskyPreviewRenderer(
    private val shaderSourceLoader: ShaderSourceLoader,
    private val eventTriggerSystem: EventTriggerSystem,
    private val atmosphereController: AtmosphereController,
    private val qualityController: AdaptiveQualityController,
    private val sceneFactory: SceneFactory,
    val runtimeProfile: RuntimeProfile = RuntimeProfile.fullscreenPreview()
) {
    private val session = RenderEngineSession(
        runtimeProfile = runtimeProfile,
        shaderSourceLoader = shaderSourceLoader,
        scheduler = SceneScheduler(),
        eventTriggerSystem = eventTriggerSystem,
        atmosphereController = atmosphereController,
        parallaxController = ParallaxController(),
        qualityController = qualityController
    )
    private var currentContext: RenderContext? = null

    val frameState: MutableRenderFrameState
        get() = session.frameState
    val sceneState: SceneState
        get() = session.sceneState

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
    val hasPendingTextureWork: Boolean
        get() = session.hasPendingTextureWork
    val shouldContinueRendering: Boolean
        get() = session.hasPendingTextureWork || session.isPreviewAnimationRunning

    fun loadWallpaper(definition: WallpaperDefinition) {
        session.activeDefinition = definition
        val nextScene = sceneFactory.create(definition)
        currentContext?.let { session.switchScene(nextScene, it) } ?: run { session.activeScene = nextScene }
    }

    fun onContextCreated(gl: GlResourceManager, context: RenderContext) {
        currentContext = context
        session.onContextCreated(gl, context)
    }

    fun onSurfaceChanged(context: RenderContext, width: Int, height: Int) {
        currentContext = context
        session.onSurfaceChanged(context, width, height)
    }

    fun renderFrame(
        context: RenderContext,
        inputSnapshot: SceneInputSnapshot
    ) {
        session.renderFrame(context, inputSnapshot)
    }

    fun onFramePresented() {
        session.onFramePresented()
    }

    fun onContextLost() {
        currentContext = null
        session.onContextLost()
    }
    
    fun triggerPreviewAnimation() {
        session.triggerPreviewAnimation()
    }
}
