/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - FBO ve bağlı texture/renderbuffer kaynaklarını temsil eder.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: FBO ve bağlı texture/renderbuffer kaynaklarını temsil eder.
 */
package com.example.lumisky.engine.gl

import android.opengl.GLES30

class GlFramebuffer(
    val framebufferId: Int,
    val texture: GlTexture?,
    val width: Int,
    val height: Int
) : GlResource {

    override fun release() {
        if (framebufferId != 0) {
            val fbos = intArrayOf(framebufferId)
            GLES30.glDeleteFramebuffers(1, fbos, 0)
        }
        texture?.release()
    }

    fun bind() {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, framebufferId)
        GLES30.glViewport(0, 0, width, height)
    }

    companion object {
        fun create(width: Int, height: Int): GlFramebuffer {
            val fbos = IntArray(1)
            GLES30.glGenFramebuffers(1, fbos, 0)
            val fboId = fbos[0]

            val texs = IntArray(1)
            GLES30.glGenTextures(1, texs, 0)
            val texId = texs[0]

            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texId)
            GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA,
                width, height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null
            )
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fboId)
            GLES30.glFramebufferTexture2D(
                GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
                GLES30.GL_TEXTURE_2D, texId, 0
            )

            val status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER)
            if (status != GLES30.GL_FRAMEBUFFER_COMPLETE) {
                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
                GLES30.glDeleteTextures(1, texs, 0)
                GLES30.glDeleteFramebuffers(1, fbos, 0)
                throw RuntimeException("Framebuffer setup incomplete: status $status")
            }

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
            return GlFramebuffer(fboId, GlTexture(texId), width, height)
        }
    }
}
