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

    override fun onCreateGl(gl: GlResourceManager, context: RenderContext) {
        program = gl.programs.get("common.texture2d", shaderSourceLoader)
    }

    override fun render(frame: MutableRenderFrameState) {
        val activeProgram = program ?: return
        val sourcePath = definition.source ?: return
        
        activeProgram.use()
        
        val texture = frame.gl.textures.get(sourcePath, frame.quality)
        texture.bind(0)
        activeProgram.setUniform("u_Texture", 0)
        
        // Apply texture-level parallax offset scaled by depth
        val depth = definition.parallax?.depth ?: 0.0f
        val offsetUvX = frame.parallaxOffsetX * depth
        val offsetUvY = frame.parallaxOffsetY * depth
        activeProgram.setUniform("u_ParallaxOffset", offsetUvX, offsetUvY)
        
        blendMode.apply()
        frame.gl.meshes.drawQuad()
        
        texture.unbind()
    }
}
