/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Tüm çizim bileşenlerinin ortak interface’i.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Tüm çizim bileşenlerinin ortak interface’i.
 */
package com.adnan.lumisky.layers

import com.adnan.lumisky.engine.gl.GlResourceManager
import com.adnan.lumisky.engine.RenderContext
import com.adnan.lumisky.engine.MutableRenderFrameState
import com.adnan.lumisky.core.WallpaperEvent
import com.adnan.lumisky.definition.LayerFramePolicyDefinition
import com.adnan.lumisky.definition.QualityProfile
import com.adnan.lumisky.engine.pipeline.BlendMode
import com.adnan.lumisky.engine.pipeline.RenderPass
import com.adnan.lumisky.engine.pipeline.RenderTargetMode

interface RenderLayer {
    val id: String
    val zIndex: Int
    val renderPass: RenderPass
    val blendMode: BlendMode
    val renderTargetMode: RenderTargetMode
    val framePolicy: LayerFramePolicyDefinition
    val parallaxDepth: Float

    fun onCreateGl(gl: GlResourceManager, context: RenderContext)
    fun onSurfaceChanged(context: RenderContext, width: Int, height: Int)
    fun onEvent(event: WallpaperEvent)
    fun update(frame: MutableRenderFrameState)
    fun render(frame: MutableRenderFrameState)
    fun onQualityChanged(profile: QualityProfile)
    fun onDestroyGl(gl: GlResourceManager)
}

enum class LayerFrameMode {
    STATIC,
    ON_DEMAND,
    MINUTE_TICK,
    ONE_FPS,
    FIXED_FPS,
    MATCH_SCENE,
    CONTINUOUS,
    VIDEO_SYNC,
    EVENT_BASED
}

enum class LayerCacheMode {
    NONE,
    CPU_STATE_ONLY,
    FBO_CACHE,
    STATIC_TEXTURE
}

