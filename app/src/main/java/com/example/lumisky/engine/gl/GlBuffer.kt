/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - VBO/IBO gibi buffer objelerinin wrapper’ı.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: VBO/IBO gibi buffer objelerinin wrapper’ı.
 */
package com.example.lumisky.engine.gl

import android.opengl.GLES30
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class GlBuffer(val bufferId: Int, val target: Int) : GlResource {
    override fun release() {
        if (bufferId != 0) {
            val buffers = intArrayOf(bufferId)
            GLES30.glDeleteBuffers(1, buffers, 0)
        }
    }

    fun bind() {
        GLES30.glBindBuffer(target, bufferId)
    }

    fun unbind() {
        GLES30.glBindBuffer(target, 0)
    }

    fun setData(data: Buffer, usage: Int = GLES30.GL_STATIC_DRAW) {
        bind()
        val byteSize = when (data) {
            is FloatBuffer -> data.remaining() * 4
            is ShortBuffer -> data.remaining() * 2
            is ByteBuffer -> data.remaining()
            else -> data.remaining() * 4
        }
        GLES30.glBufferData(target, byteSize, data, usage)
    }

    companion object {
        fun create(target: Int): GlBuffer {
            val buffers = IntArray(1)
            GLES30.glGenBuffers(1, buffers, 0)
            return GlBuffer(buffers[0], target)
        }
    }
}
