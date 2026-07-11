/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Shader source compile/link, loglama, fallback shader ve validation görevleri.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Shader source compile/link, loglama, fallback shader ve validation görevleri.
 */
package com.example.lumisky.engine.gl

import android.opengl.GLES30
import android.util.Log

object ShaderCompiler {
    private const val TAG = "ShaderCompiler"

    fun compileAndLink(vertexSource: String, fragmentSource: String): Int {
        val hasVersion300 = fragmentSource.contains("#version 300 es")
        
        val resolvedVertexSource = if (hasVersion300) {
            """
            #version 300 es
            layout(location = 0) in vec2 a_Position;
            layout(location = 1) in vec2 a_Uv;
            out vec2 v_Uv;
            out vec2 v_TexCoord;
            void main() {
                v_Uv = a_Position.xy * 0.5 + 0.5;
                v_TexCoord = vec2(v_Uv.x, 1.0 - v_Uv.y);
                gl_Position = vec4(a_Position, 0.0, 1.0);
            }
            """.trimIndent()
        } else {
            """
            attribute vec2 a_Position;
            attribute vec2 a_Uv;
            varying vec2 v_Uv;
            varying vec2 v_TexCoord;
            void main() {
                v_Uv = a_Position.xy * 0.5 + 0.5;
                v_TexCoord = vec2(v_Uv.x, 1.0 - v_Uv.y);
                gl_Position = vec4(a_Position, 0.0, 1.0);
            }
            """.trimIndent()
        }

        val vertexShader = compileShader(GLES30.GL_VERTEX_SHADER, resolvedVertexSource)
        if (vertexShader == 0) {
            return 0
        }

        val fragmentShader = compileShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource)
        if (fragmentShader == 0) {
            GLES30.glDeleteShader(vertexShader)
            return 0
        }

        val program = GLES30.glCreateProgram()
        if (program == 0) {
            GLES30.glDeleteShader(vertexShader)
            GLES30.glDeleteShader(fragmentShader)
            return 0
        }

        GLES30.glAttachShader(program, vertexShader)
        GLES30.glAttachShader(program, fragmentShader)
        
        // Explicitly bind attributes for ES 100/300 linkage consistency
        GLES30.glBindAttribLocation(program, 0, "a_Position")
        GLES30.glBindAttribLocation(program, 1, "a_Uv")
        
        GLES30.glLinkProgram(program)

        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val log = GLES30.glGetProgramInfoLog(program)
            Log.e(TAG, "Error linking program: $log")
            GLES30.glDeleteProgram(program)
            GLES30.glDeleteShader(vertexShader)
            GLES30.glDeleteShader(fragmentShader)
            return 0
        }

        GLES30.glDetachShader(program, vertexShader)
        GLES30.glDetachShader(program, fragmentShader)
        GLES30.glDeleteShader(vertexShader)
        GLES30.glDeleteShader(fragmentShader)

        return program
    }

    private fun compileShader(type: Int, source: String): Int {
        val shader = GLES30.glCreateShader(type)
        if (shader == 0) {
            return 0
        }

        GLES30.glShaderSource(shader, source)
        GLES30.glCompileShader(shader)

        val compileStatus = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val log = GLES30.glGetShaderInfoLog(shader)
            val typeStr = if (type == GLES30.GL_VERTEX_SHADER) "VERTEX" else "FRAGMENT"
            Log.e(TAG, "Error compiling $typeStr shader: $log")
            GLES30.glDeleteShader(shader)
            return 0
        }

        return shader
    }
}
