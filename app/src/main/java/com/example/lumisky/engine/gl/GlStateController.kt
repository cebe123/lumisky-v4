/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Blend/depth/viewport/program state değişimlerini merkezileştirir.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Blend/depth/viewport/program state değişimlerini merkezileştirir.
 */
package com.example.lumisky.engine.gl

import android.opengl.GLES30

class GlStateController {
    private var activeProgramId = 0
    private var isBlendEnabled = false
    private var currentBlendSrc = -1
    private var currentBlendDst = -1
    private var viewportX = 0
    private var viewportY = 0
    private var viewportWidth = 0
    private var viewportHeight = 0

    fun useProgram(programId: Int) {
        if (activeProgramId != programId) {
            GLES30.glUseProgram(programId)
            activeProgramId = programId
        }
    }

    fun setBlendEnabled(enabled: Boolean) {
        if (isBlendEnabled != enabled) {
            if (enabled) {
                GLES30.glEnable(GLES30.GL_BLEND)
            } else {
                GLES30.glDisable(GLES30.GL_BLEND)
            }
            isBlendEnabled = enabled
        }
    }

    fun setBlendFunc(src: Int, dst: Int) {
        if (currentBlendSrc != src || currentBlendDst != dst) {
            GLES30.glBlendFunc(src, dst)
            currentBlendSrc = src
            currentBlendDst = dst
        }
    }

    fun setViewport(x: Int, y: Int, width: Int, height: Int) {
        if (viewportX != x || viewportY != y || viewportWidth != width || viewportHeight != height) {
            GLES30.glViewport(x, y, width, height)
            viewportX = x
            viewportY = y
            viewportWidth = width
            viewportHeight = height
        }
    }

    fun reset() {
        activeProgramId = 0
        isBlendEnabled = false
        currentBlendSrc = -1
        currentBlendDst = -1
        viewportX = 0
        viewportY = 0
        viewportWidth = 0
        viewportHeight = 0
    }
}
