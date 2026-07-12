package com.example.lumisky.layers

import com.example.lumisky.assets.ShaderSourceLoader
import com.example.lumisky.definition.LayerDefinition
import com.example.lumisky.engine.MutableRenderFrameState
import com.example.lumisky.engine.RenderContext
import com.example.lumisky.engine.gl.GlProgram
import com.example.lumisky.engine.gl.GlResourceManager

class TimeSliceTextureLayer(
    definition: LayerDefinition,
    private val shaderSourceLoader: ShaderSourceLoader
) : BaseLayer(definition) {
    private val plan = TimeSliceTexturePlan(definition.timeSlices)
    private var program: GlProgram? = null

    override fun onCreateGl(gl: GlResourceManager, context: RenderContext) {
        program = gl.programs.get("common.texture2d", shaderSourceLoader)
        plan.preload(gl)
    }

    override fun render(frame: MutableRenderFrameState) {
        val asset = plan.assetFor(frame.minute.toInt()) ?: return
        val activeProgram = program ?: return

        activeProgram.use()
        val texture = asset.resolve(frame)
        texture.bind(0)
        activeProgram.setUniform("u_Texture", 0)
        activeProgram.setUniform("u_ParallaxOffset", 0f, 0f)
        blendMode.apply()
        frame.gl.meshes.drawQuad()
        texture.unbind()
    }
}
