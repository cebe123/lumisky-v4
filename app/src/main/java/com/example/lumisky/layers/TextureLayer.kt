/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Statik image/texture layer. Foreground/background için.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Statik image/texture layer. Foreground/background için.
 */
package com.example.lumisky.layers

import com.example.lumisky.assets.ShaderSourceLoader
import com.example.lumisky.definition.LayerDefinition
import com.example.lumisky.engine.MutableRenderFrameState
import com.example.lumisky.engine.RenderContext
import com.example.lumisky.engine.gl.GlProgram
import com.example.lumisky.engine.gl.GlResourceManager

open class TextureLayer(
    definition: LayerDefinition,
    private val shaderSourceLoader: ShaderSourceLoader
) : BaseLayer(definition) {
    private var program: GlProgram? = null
    private val asset = TextureAssetHandle.optional(definition.source)

    override fun onCreateGl(gl: GlResourceManager, context: RenderContext) {
        program = gl.programs.get("common.texture2d", shaderSourceLoader)
        asset?.preload(gl)
    }

    override fun render(frame: MutableRenderFrameState) {
        val activeProgram = program ?: return
        val textureAsset = asset ?: return
        
        activeProgram.use()
        
        val texture = textureAsset.resolve(frame)
        texture.bind(0)
        activeProgram.setUniform("u_Texture", 0)
        
        // Apply texture-level parallax offset scaled by depth
        val depth = (definition.parallax?.depth ?: 0.0f).coerceIn(0.0f, MAX_PARALLAX_DEPTH)
        val offsetUvX = frame.parallaxOffsetX * depth
        val offsetUvY = frame.parallaxOffsetY * depth
        activeProgram.setUniform("u_ParallaxOffset", offsetUvX, offsetUvY)
        
        blendMode.apply()
        frame.gl.meshes.drawQuad()
        
        texture.unbind()
    }

    private companion object {
        const val MAX_PARALLAX_DEPTH = 1.0f
    }
}
