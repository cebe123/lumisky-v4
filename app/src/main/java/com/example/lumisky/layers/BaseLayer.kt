/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - RenderLayer için ortak default davranışları sağlayan soyut temel sınıf.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: RenderLayer için ortak default davranışları sağlayan soyut temel sınıf.
 */
package com.example.lumisky.layers

import com.example.lumisky.core.WallpaperEvent
import com.example.lumisky.definition.LayerDefinition
import com.example.lumisky.definition.LayerFramePolicyDefinition
import com.example.lumisky.definition.QualityProfile
import com.example.lumisky.engine.MutableRenderFrameState
import com.example.lumisky.engine.RenderContext
import com.example.lumisky.engine.gl.GlResourceManager
import com.example.lumisky.engine.pipeline.BlendMode
import com.example.lumisky.engine.pipeline.RenderPass
import com.example.lumisky.engine.pipeline.RenderTargetMode

abstract class BaseLayer(
    protected val definition: LayerDefinition
) : RenderLayer {
    override val id: String get() = definition.id
    override val zIndex: Int get() = definition.zIndex
    override val renderPass: RenderPass get() = try { RenderPass.valueOf(definition.renderPass) } catch (e: Throwable) { RenderPass.BACKGROUND }
    override val blendMode: BlendMode get() = try { BlendMode.valueOf(definition.blendMode) } catch (e: Throwable) { BlendMode.NONE }
    override val renderTargetMode: RenderTargetMode get() = try { RenderTargetMode.valueOf(definition.renderTarget) } catch (e: Throwable) { RenderTargetMode.DIRECT }
    override val framePolicy: LayerFramePolicyDefinition get() = definition.framePolicy ?: LayerFramePolicyDefinition()
    override val parallaxDepth: Float get() = definition.parallax?.depth ?: 0.0f

    override fun onCreateGl(gl: GlResourceManager, context: RenderContext) {}
    override fun onSurfaceChanged(context: RenderContext, width: Int, height: Int) {}
    override fun onEvent(event: WallpaperEvent) {}
    override fun update(frame: MutableRenderFrameState) {}
    override fun render(frame: MutableRenderFrameState) {}
    override fun onQualityChanged(profile: QualityProfile) {}
    override fun onDestroyGl(gl: GlResourceManager) {}
}
