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
import android.util.Log
import com.example.lumisky.assets.ShaderSourceLoader
import com.example.lumisky.definition.LayerDefinition
import com.example.lumisky.engine.MutableRenderFrameState
import com.example.lumisky.engine.RenderContext
import com.example.lumisky.engine.gl.GlProgram
import com.example.lumisky.engine.gl.GlResourceManager

open class ShaderLayer(
    definition: LayerDefinition,
    private val shaderSourceLoader: ShaderSourceLoader
) : BaseLayer(definition) {
    private var program: GlProgram? = null
    private val bindings = CompiledShaderBindings.compile(definition)
    private val isOptionalLayer = definition.type == "StarsLayer" ||
        definition.id.contains("stars", ignoreCase = true) ||
        definition.id.contains("particle", ignoreCase = true)

    override fun onCreateGl(gl: GlResourceManager, context: RenderContext) {
        val shaderRef = definition.shaderRef
        if (shaderRef != null) {
            program = runCatching { gl.programs.get(shaderRef, shaderSourceLoader) }
                .onFailure { Log.e(TAG, "Disabled layer ${definition.id}: shader fallback unavailable", it) }
                .getOrNull()
        }
        for (binding in bindings.textures) binding.asset.preload(gl)
    }

    override fun render(frame: MutableRenderFrameState) {
        if (isOptionalLayer) {
            val enabled = frame.particleEffectsEnabled && frame.quality != com.example.lumisky.definition.QualityTier.LOW && !frame.thermalEmergency
            if (!enabled) return
        }

        val activeProgram = program ?: return
        activeProgram.use()

        // Bind standard engine uniforms
        activeProgram.setUniform("u_Time", frame.timeSeconds)
        activeProgram.setUniform("u_Resolution", frame.width.toFloat(), frame.height.toFloat())
        val parallaxX = resolveParallaxX(frame)
        val parallaxY = resolveParallaxY(frame)
        activeProgram.setUniform("u_ParallaxOffset", parallaxX, parallaxY)
        activeProgram.setUniform("u_Parallax", parallaxX, parallaxY)
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

        for (binding in bindings.uniforms) binding.bind(activeProgram)

        for (binding in bindings.textures) {
            val texture = binding.asset.resolve(frame)
            texture.bind(binding.unit)
            activeProgram.setUniform(binding.uniform, binding.unit)
        }

        blendMode.apply()
        frame.gl.meshes.drawQuad()
    }

    private companion object {
        const val TAG = "ShaderLayer"
    }
}
