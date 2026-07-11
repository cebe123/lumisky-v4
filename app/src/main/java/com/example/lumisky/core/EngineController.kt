/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - StateFlow/SharedFlow akışlarını engine command/event kuyruğuna bağlar.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: StateFlow/SharedFlow akışlarını engine command/event kuyruğuna bağlar.
 */
package com.example.lumisky.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EngineController @Inject constructor() {
    private val eventQueue = EngineEventQueue()
    private val _engineEvents = MutableSharedFlow<WallpaperEvent>(extraBufferCapacity = 16)
    val engineEvents: SharedFlow<WallpaperEvent> = _engineEvents.asSharedFlow()

    fun getQueue(): EngineEventQueue {
        return eventQueue
    }

    fun postEvent(event: WallpaperEvent) {
        eventQueue.offer(event)
        _engineEvents.tryEmit(event)
    }
}
