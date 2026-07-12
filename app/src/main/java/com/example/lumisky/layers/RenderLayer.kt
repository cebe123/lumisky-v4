/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Tüm çizim bileşenlerinin ortak interface’i.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Tüm çizim bileşenlerinin ortak interface’i.
 */
package com.example.lumisky.layers

import com.example.lumisky.engine.gl.GlResourceManager
import com.example.lumisky.engine.RenderContext
import com.example.lumisky.engine.MutableRenderFrameState
import com.example.lumisky.core.WallpaperEvent
import com.example.lumisky.definition.LayerFramePolicyDefinition
import com.example.lumisky.definition.QualityProfile
import com.example.lumisky.engine.pipeline.BlendMode
import com.example.lumisky.engine.pipeline.RenderPass
import com.example.lumisky.engine.pipeline.RenderTargetMode
import com.example.lumisky.engine.FrameDemandReason

interface RenderLayer {
    val id: String
    val zIndex: Int
    val renderPass: RenderPass
    val blendMode: BlendMode
    val renderTargetMode: RenderTargetMode
    val framePolicy: LayerFramePolicyDefinition
    val frameMode: LayerFrameMode
    val cacheMode: LayerCacheMode
    val parallaxDepth: Float

    fun onCreateGl(gl: GlResourceManager, context: RenderContext)
    fun onSurfaceChanged(context: RenderContext, width: Int, height: Int)
    fun onEvent(event: WallpaperEvent)
    fun update(frame: MutableRenderFrameState)
    fun render(frame: MutableRenderFrameState)
    fun onQualityChanged(profile: QualityProfile)
    fun onDestroyGl(gl: GlResourceManager)
    fun onContextLost() {}
    val hasPendingFrameDemand: Boolean get() = false
    fun pendingFrameDemandReason(): FrameDemandReason? = null
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

