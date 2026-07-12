/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Main/UI thread eventlerini GL thread’de güvenli işlemek için Concurrent queue.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Main/UI thread eventlerini GL thread’de güvenli işlemek için Concurrent queue.
 */
package com.example.lumisky.core

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicReference

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
    private val latestTouch = AtomicReference<WallpaperEvent.Touch?>(null)
    private val latestParallax = AtomicReference<WallpaperEvent.ParallaxChanged?>(null)

    fun offer(event: WallpaperEvent) {
        when (event) {
            is WallpaperEvent.Touch -> latestTouch.set(event)
            is WallpaperEvent.ParallaxChanged -> latestParallax.set(event)
            else -> queue.offer(event)
        }
    }

    fun drainTo(target: MutableList<WallpaperEvent>) {
        while (true) {
            val event = queue.poll() ?: break
            target.add(event)
        }
        latestParallax.getAndSet(null)?.let(target::add)
        latestTouch.getAndSet(null)?.let(target::add)
    }
}
