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

import com.adnan.lumisky.assets.ShaderSourceLoader
import com.adnan.lumisky.definition.WallpaperDefinition
import com.adnan.lumisky.engine.AdaptiveQualityController
import com.adnan.lumisky.engine.AtmosphereController
import com.adnan.lumisky.engine.EventTriggerSystem
import com.adnan.lumisky.engine.MutableRenderFrameState
import com.adnan.lumisky.engine.ParallaxController
import com.adnan.lumisky.engine.RenderEngineSession
import com.adnan.lumisky.engine.RenderContext
import com.adnan.lumisky.engine.RuntimeProfile
import com.adnan.lumisky.engine.RuntimeScene
import com.adnan.lumisky.engine.SceneInputSnapshot
import com.adnan.lumisky.engine.SceneScheduler
import com.adnan.lumisky.engine.SceneState
import com.adnan.lumisky.engine.gl.GlResourceManager
import com.adnan.lumisky.registry.SceneFactory

class LumiskyPreviewRenderer(
    private val shaderSourceLoader: ShaderSourceLoader,
    private val scheduler: SceneScheduler,
    private val eventTriggerSystem: EventTriggerSystem,
    private val atmosphereController: AtmosphereController,
    private val parallaxController: ParallaxController,
    private val qualityController: AdaptiveQualityController,
    private val sceneFactory: SceneFactory,
    val runtimeProfile: RuntimeProfile = RuntimeProfile.fullscreenPreview()
) {
    private val session = RenderEngineSession(
        runtimeProfile = runtimeProfile,
        shaderSourceLoader = shaderSourceLoader,
        scheduler = scheduler,
        eventTriggerSystem = eventTriggerSystem,
        atmosphereController = atmosphereController,
        parallaxController = parallaxController,
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

    fun loadWallpaper(definition: WallpaperDefinition) {
        val previousScene = session.activeScene
        session.activeDefinition = definition
        val nextScene = sceneFactory.create(definition)
        if (session.isContextCreated && session.frameState.isGlInitialized()) {
            previousScene?.onDestroyGl(session.frameState.gl)
            val context = currentContext
            if (context != null) {
                nextScene.onCreateGl(session.frameState.gl, context)
                if (session.frameState.width > 0 && session.frameState.height > 0) {
                    nextScene.onSurfaceChanged(context, session.frameState.width, session.frameState.height)
                }
            }
        }
        session.activeScene = nextScene
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

    fun onContextLost() {
        currentContext = null
        session.onContextLost()
    }
    
    fun triggerPreviewAnimation() {
        session.triggerPreviewAnimation()
    }
}
