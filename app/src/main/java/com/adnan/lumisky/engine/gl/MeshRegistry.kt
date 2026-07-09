/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Fullscreen quad ve ortak mesh/VBO kaynaklarını context scope içinde tutar.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Fullscreen quad ve ortak mesh/VBO kaynaklarını context scope içinde tutar.
 */
package com.adnan.lumisky.engine.gl

import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MeshRegistry(private val manager: GlResourceManager) {
    private var quadVbo: GlBuffer? = null

    fun getQuadVbo(): GlBuffer {
        val current = quadVbo
        if (current != null) return current

        // Vertex layout: 2D Position (x, y), 2D UV (u, v)
        val vertices = floatArrayOf(
            -1.0f, -1.0f,  0.0f, 0.0f,
             1.0f, -1.0f,  1.0f, 0.0f,
            -1.0f,  1.0f,  0.0f, 1.0f,
             1.0f,  1.0f,  1.0f, 1.0f
        )
        val byteBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
        val floatBuffer = byteBuffer.asFloatBuffer().apply {
            put(vertices)
            position(0)
        }

        val vbo = GlBuffer.create(GLES30.GL_ARRAY_BUFFER)
        vbo.setData(floatBuffer)
        quadVbo = vbo
        return vbo
    }

    fun drawQuad() {
        val vbo = getQuadVbo()
        vbo.bind()
        
        // Position: attribute 0, size 2, stride 16 bytes, offset 0
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 16, 0)
        
        // UV Coord: attribute 1, size 2, stride 16 bytes, offset 8 bytes (2 floats)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 16, 8)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
        vbo.unbind()
    }

    fun clear() {
        quadVbo?.release()
        quadVbo = null
    }
}
