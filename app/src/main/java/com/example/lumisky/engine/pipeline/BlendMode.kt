/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - NONE, ALPHA, ADDITIVE, MULTIPLY, SCREEN blend davranışlarını tanımlar.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: NONE, ALPHA, ADDITIVE, MULTIPLY, SCREEN blend davranışlarını tanımlar.
 */
package com.example.lumisky.engine.pipeline

import android.opengl.GLES30

enum class BlendMode {
    NONE,
    ALPHA,
    ADDITIVE,
    MULTIPLY,
    SCREEN;

    fun apply() {
        when (this) {
            NONE -> {
                GLES30.glDisable(GLES30.GL_BLEND)
            }
            ALPHA -> {
                GLES30.glEnable(GLES30.GL_BLEND)
                GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
            }
            ADDITIVE -> {
                GLES30.glEnable(GLES30.GL_BLEND)
                GLES30.glBlendFunc(GLES30.GL_ONE, GLES30.GL_ONE)
            }
            MULTIPLY -> {
                GLES30.glEnable(GLES30.GL_BLEND)
                GLES30.glBlendFunc(GLES30.GL_DST_COLOR, GLES30.GL_ONE_MINUS_SRC_ALPHA)
            }
            SCREEN -> {
                GLES30.glEnable(GLES30.GL_BLEND)
                GLES30.glBlendFunc(GLES30.GL_ONE, GLES30.GL_ONE_MINUS_SRC_COLOR)
            }
        }
    }
}
