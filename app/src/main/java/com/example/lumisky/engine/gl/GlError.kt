/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - GL hata tipleri ve telemetry’ye aktarılacak hata modelleri.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: GL hata tipleri ve telemetry’ye aktarılacak hata modelleri.
 */
package com.example.lumisky.engine.gl

import android.opengl.GLES30

object GlError {
    fun check(operation: String) {
        val error = GLES30.glGetError()
        if (error != GLES30.GL_NO_ERROR) {
            val msg = when (error) {
                GLES30.GL_INVALID_ENUM -> "GL_INVALID_ENUM"
                GLES30.GL_INVALID_VALUE -> "GL_INVALID_VALUE"
                GLES30.GL_INVALID_OPERATION -> "GL_INVALID_OPERATION"
                GLES30.GL_OUT_OF_MEMORY -> "GL_OUT_OF_MEMORY"
                GLES30.GL_INVALID_FRAMEBUFFER_OPERATION -> "GL_INVALID_FRAMEBUFFER_OPERATION"
                else -> "0x${Integer.toHexString(error)}"
            }
            throw RuntimeException("$operation: OpenGL ES error: $msg")
        }
    }
}
