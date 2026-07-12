package com.example.lumisky.layers

import com.example.lumisky.assets.ShaderSourceLoader
import com.example.lumisky.definition.LayerDefinition
import com.example.lumisky.engine.MutableRenderFrameState
import com.example.lumisky.engine.RenderContext
import com.example.lumisky.engine.RuntimeMode
import com.example.lumisky.engine.gl.GlProgram
import com.example.lumisky.engine.gl.GlResourceManager

class TimeSliceTextureLayer(
    definition: LayerDefinition,
    private val shaderSourceLoader: ShaderSourceLoader
) : BaseLayer(definition) {
    private val plan = TimeSliceTexturePlan(definition.timeSlices)
    private var program: GlProgram? = null
    private var previewTexturesPreloaded = false

    override fun onCreateGl(gl: GlResourceManager, context: RenderContext) {
        program = gl.programs.get("common.texture2d", shaderSourceLoader)
        previewTexturesPreloaded = false
    }

    override fun render(frame: MutableRenderFrameState) {
        if (frame.runtimeMode != RuntimeMode.LIVE_WALLPAPER && !previewTexturesPreloaded) {
            plan.preload(frame.gl)
            previewTexturesPreloaded = true
        }
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
