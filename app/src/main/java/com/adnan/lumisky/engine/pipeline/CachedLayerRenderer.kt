/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Low-FPS/FBO cached layer output’unu gerektiğinde refresh eder, her frame composite ettirir.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Low-FPS/FBO cached layer output’unu gerektiğinde refresh eder, her frame composite ettirir.
 */
package com.adnan.lumisky.engine.pipeline

import android.opengl.GLES30
import com.adnan.lumisky.assets.ShaderSourceLoader
import com.adnan.lumisky.engine.MutableRenderFrameState
import com.adnan.lumisky.engine.gl.GlFramebuffer
import com.adnan.lumisky.engine.gl.GlResourceManager
import com.adnan.lumisky.layers.RenderLayer

class CachedLayerRenderer(
    private val gl: GlResourceManager,
    private val shaderSourceLoader: ShaderSourceLoader
) {
    private val fboCache = mutableMapOf<String, GlFramebuffer>()

    fun refresh(layer: RenderLayer, frame: MutableRenderFrameState) {
        val scale = FboResolutionPolicy.getResolutionScale(frame.quality, layer.id)
        val fboWidth = (frame.width * scale).toInt().coerceAtLeast(1)
        val fboHeight = (frame.height * scale).toInt().coerceAtLeast(1)

        val fbo = fboCache.getOrPut(layer.id) {
            GlFramebuffer.create(fboWidth, fboHeight)
        }
        
        fbo.bind()
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        
        layer.render(frame)
        
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
    }

    fun compositeLastTexture(layer: RenderLayer, frame: MutableRenderFrameState) {
        val fbo = fboCache[layer.id] ?: return
        val texture = fbo.texture ?: return
        
        layer.blendMode.apply()
        
        val program = gl.programs.get("common.texture2d", shaderSourceLoader)
        program.use()
        
        texture.bind(0)
        program.setUniform("u_Texture", 0)
        
        gl.meshes.drawQuad()
        
        texture.unbind()
    }

    fun clear() {
        fboCache.values.forEach { it.release() }
        fboCache.clear()
    }
}
