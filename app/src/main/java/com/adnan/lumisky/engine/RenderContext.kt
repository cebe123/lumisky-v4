/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Hot-path için allocation yapmadan kullanılan teknik render state: viewport, matrices, frame time.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Hot-path için allocation yapmadan kullanılan teknik render state: viewport, matrices, frame time.
 */
package com.adnan.lumisky.engine

class RenderContext {
    var width: Int = 0
    var height: Int = 0
    var aspect: Float = 1.0f
    val projectionMatrix = FloatArray(16) { if (it % 5 == 0) 1.0f else 0.0f }
    var frameTimeNanos: Long = 0L
    var deltaTimeSeconds: Float = 0.0f

    fun update(timeNanos: Long) {
        if (frameTimeNanos != 0L) {
            deltaTimeSeconds = (timeNanos - frameTimeNanos) / 1_000_000_000.0f
        }
        frameTimeNanos = timeNanos
    }
}
