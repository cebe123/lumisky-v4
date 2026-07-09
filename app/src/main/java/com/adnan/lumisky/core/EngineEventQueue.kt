/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Main/UI thread eventlerini GL thread’de güvenli işlemek için Concurrent queue.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Main/UI thread eventlerini GL thread’de güvenli işlemek için Concurrent queue.
 */
package com.adnan.lumisky.core

import java.util.concurrent.ConcurrentLinkedQueue

sealed interface WallpaperEvent {
    object ScreenOn : WallpaperEvent
    object ScreenOff : WallpaperEvent
    object UserPresent : WallpaperEvent
    data class VideoFrameAvailable(val layerId: String) : WallpaperEvent
    data class Touch(val x: Float, val y: Float) : WallpaperEvent
    data class ParallaxChanged(val offsetX: Float, val offsetY: Float) : WallpaperEvent
    data class TimeChanged(val hour: Int, val minute: Int) : WallpaperEvent
}

class EngineEventQueue {
    private val queue = ConcurrentLinkedQueue<WallpaperEvent>()

    fun offer(event: WallpaperEvent) {
        queue.offer(event)
    }

    fun drainTo(target: MutableList<WallpaperEvent>) {
        while (true) {
            val event = queue.poll() ?: break
            target.add(event)
        }
    }
}
