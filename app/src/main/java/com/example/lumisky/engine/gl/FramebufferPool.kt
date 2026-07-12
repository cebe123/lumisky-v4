/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - FBO reuse, half-res/quarter-res policy ve release işlemleri.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: FBO reuse, half-res/quarter-res policy ve release işlemleri.
 */
package com.example.lumisky.engine.gl

class FramebufferPool(private val manager: GlResourceManager) {
    private val pool = mutableListOf<GlFramebuffer>()

    fun obtain(width: Int, height: Int): GlFramebuffer {
        val index = pool.indexOfFirst { it.width == width && it.height == height }
        return if (index >= 0) {
            pool.removeAt(index)
        } else {
            GlFramebuffer.create(width, height)
        }
    }

    fun recycle(framebuffer: GlFramebuffer) {
        pool.add(framebuffer)
    }

    fun clear() {
        pool.forEach { it.release() }
        pool.clear()
    }

    fun invalidate() {
        pool.clear()
    }
}
