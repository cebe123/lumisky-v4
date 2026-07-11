/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Texture ID wrapper. GL_TEXTURE_2D kaynaklarını context-bound temsil eder.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Texture ID wrapper. GL_TEXTURE_2D kaynaklarını context-bound temsil eder.
 */
package com.example.lumisky.engine.gl

import android.opengl.GLES30

class GlTexture(
    val textureId: Int,
    val target: Int = GLES30.GL_TEXTURE_2D
) : GlResource {

    override fun release() {
        if (textureId != 0) {
            val textures = intArrayOf(textureId)
            GLES30.glDeleteTextures(1, textures, 0)
        }
    }

    fun bind(unit: Int) {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + unit)
        GLES30.glBindTexture(target, textureId)
    }

    fun unbind() {
        GLES30.glBindTexture(target, 0)
    }
}
