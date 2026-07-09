/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - VSync uyumlu frame clock. frameTimeNanos ile delta hesaplanmasını sağlar.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: VSync uyumlu frame clock. frameTimeNanos ile delta hesaplanmasını sağlar.
 */
package com.adnan.lumisky.core

import android.view.Choreographer

class WallpaperFrameClock(
    private val onFrame: (Long) -> Unit
) : Choreographer.FrameCallback {

    private var running = false

    fun start() {
        if (running) return
        running = true
        Choreographer.getInstance().postFrameCallback(this)
    }

    fun stop() {
        running = false
        Choreographer.getInstance().removeFrameCallback(this)
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!running) return
        onFrame(frameTimeNanos)
        Choreographer.getInstance().postFrameCallback(this)
    }
}
