/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Compiled OpenGL shader program wrapper. Uniform location cache ve use/delete işlemleri.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Compiled OpenGL shader program wrapper. Uniform location cache ve use/delete işlemleri.
 */
package com.adnan.lumisky.engine.gl

import android.opengl.GLES30

class GlProgram(val programId: Int) : GlResource {
    private val uniformLocations = mutableMapOf<String, Int>()

    override fun release() {
        if (programId != 0) {
            GLES30.glDeleteProgram(programId)
        }
    }

    fun use() {
        GLES30.glUseProgram(programId)
    }

    fun getUniformLocation(name: String): Int {
        return uniformLocations.getOrPut(name) {
            GLES30.glGetUniformLocation(programId, name)
        }
    }

    fun setUniform(name: String, value: Float) {
        val loc = getUniformLocation(name)
        if (loc >= 0) GLES30.glUniform1f(loc, value)
    }

    fun setUniform(name: String, value: Int) {
        val loc = getUniformLocation(name)
        if (loc >= 0) GLES30.glUniform1i(loc, value)
    }

    fun setUniform(name: String, x: Float, y: Float) {
        val loc = getUniformLocation(name)
        if (loc >= 0) GLES30.glUniform2f(loc, x, y)
    }

    fun setUniform(name: String, x: Float, y: Float, z: Float) {
        val loc = getUniformLocation(name)
        if (loc >= 0) GLES30.glUniform3f(loc, x, y, z)
    }

    fun setUniform(name: String, x: Float, y: Float, z: Float, w: Float) {
        val loc = getUniformLocation(name)
        if (loc >= 0) GLES30.glUniform4f(loc, x, y, z, w)
    }

    fun setUniformMatrix(name: String, matrix: FloatArray) {
        val loc = getUniformLocation(name)
        if (loc >= 0) GLES30.glUniformMatrix4fv(loc, 1, false, matrix, 0)
    }
}
