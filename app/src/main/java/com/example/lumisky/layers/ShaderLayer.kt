/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Fullscreen veya mesh tabanlı generic shader layer.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Fullscreen veya mesh tabanlı generic shader layer.
 */
package com.example.lumisky.layers

import android.opengl.GLES30
import com.example.lumisky.assets.ShaderSourceLoader
import com.example.lumisky.definition.LayerDefinition
import com.example.lumisky.definition.UniformValueDefinition
import com.example.lumisky.engine.MutableRenderFrameState
import com.example.lumisky.engine.RenderContext
import com.example.lumisky.engine.gl.GlProgram
import com.example.lumisky.engine.gl.GlResourceManager
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.float
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive

open class ShaderLayer(
    definition: LayerDefinition,
    private val shaderSourceLoader: ShaderSourceLoader
) : BaseLayer(definition) {
    private var program: GlProgram? = null

    override fun onCreateGl(gl: GlResourceManager, context: RenderContext) {
        val shaderRef = definition.shaderRef
        if (shaderRef != null) {
            program = gl.programs.get(shaderRef, shaderSourceLoader)
        }
    }

    override fun render(frame: MutableRenderFrameState) {
        val isOptionalLayer = definition.type == "StarsLayer" || 
                              definition.id.contains("stars", ignoreCase = true) ||
                              definition.id.contains("particle", ignoreCase = true)
        
        if (isOptionalLayer) {
            val enabled = frame.particleEffectsEnabled && frame.quality != com.example.lumisky.definition.QualityTier.LOW && !frame.thermalEmergency
            if (!enabled) return
        }

        val activeProgram = program ?: return
        activeProgram.use()

        // Bind standard engine uniforms
        activeProgram.setUniform("u_Time", frame.timeSeconds)
        activeProgram.setUniform("u_Resolution", frame.width.toFloat(), frame.height.toFloat())
        activeProgram.setUniform("u_ParallaxOffset", frame.parallaxOffsetX, frame.parallaxOffsetY)
        activeProgram.setUniform("u_Parallax", frame.parallaxOffsetX, frame.parallaxOffsetY)
        activeProgram.setUniform("u_DayProgress", frame.dayProgress)

        // Aspect ratio
        val aspect = frame.width.toFloat() / frame.height.toFloat()
        activeProgram.setUniform("u_AspectRatio", aspect)

        val hasStars = if (frame.particleEffectsEnabled && frame.quality != com.example.lumisky.definition.QualityTier.LOW && !frame.thermalEmergency) 1f else 0f
        activeProgram.setUniform("u_HasStars", hasStars)

        activeProgram.setUniform("u_SunPos", frame.sunX, frame.sunY)
        activeProgram.setUniform("u_MoonPos", frame.moonX, frame.moonY)
        activeProgram.setUniform("u_DrawSun", if (frame.drawSun) 1f else 0f)
        activeProgram.setUniform("u_IsNight", if (frame.isNight) 1f else 0f)
        activeProgram.setUniform("u_Minute", frame.minute)
        activeProgram.setUniform("u_Sunrise", frame.sunriseMinute.toFloat())
        activeProgram.setUniform("u_Sunset", frame.sunsetMinute.toFloat())
        activeProgram.setUniform("u_SolarNoon", frame.solarNoonMinute.toFloat())
        activeProgram.setUniform("u_NightAmount", frame.nightAmount)
        activeProgram.setUniform("u_HorizonY", frame.horizonY)
        activeProgram.setUniform("u_SunColor", frame.sunColorR, frame.sunColorG, frame.sunColorB)

        // Cloud Offset and Alpha
        activeProgram.setUniform("u_CloudOffset", frame.timeSeconds * 0.006f)
        activeProgram.setUniform("u_CloudAlpha", 0.0f)

        // Bind custom uniforms from layer definition
        definition.uniforms.forEach { (name, def) ->
            bindUniform(activeProgram, name, def)
        }

        // Bind textures configured in definition
        definition.textures.forEachIndexed { index, texDef ->
            val texture = frame.gl.textures.get(texDef.path, frame.quality)
            texture.bind(index)
            activeProgram.setUniform(texDef.uniform, index)
        }

        blendMode.apply()
        frame.gl.meshes.drawQuad()
    }

    private fun bindUniform(program: GlProgram, name: String, def: UniformValueDefinition) {
        try {
            when (def.type) {
                "float" -> {
                    val v = (def.value as? JsonPrimitive)?.float ?: 0f
                    program.setUniform(name, v)
                }
                "int" -> {
                    val v = (def.value as? JsonPrimitive)?.int ?: 0
                    program.setUniform(name, v)
                }
                "vec2" -> {
                    val arr = def.value as? JsonArray
                    if (arr != null && arr.size >= 2) {
                        program.setUniform(name, arr[0].jsonPrimitive.float, arr[1].jsonPrimitive.float)
                    }
                }
                "vec3" -> {
                    val arr = def.value as? JsonArray
                    if (arr != null && arr.size >= 3) {
                        program.setUniform(
                            name,
                            arr[0].jsonPrimitive.float,
                            arr[1].jsonPrimitive.float,
                            arr[2].jsonPrimitive.float
                        )
                    }
                }
                "vec4" -> {
                    val arr = def.value as? JsonArray
                    if (arr != null && arr.size >= 4) {
                        program.setUniform(
                            name,
                            arr[0].jsonPrimitive.float,
                            arr[1].jsonPrimitive.float,
                            arr[2].jsonPrimitive.float,
                            arr[3].jsonPrimitive.float
                        )
                    }
                }
            }
        } catch (e: Throwable) {
            // Ignore malformed uniform values
        }
    }
}
