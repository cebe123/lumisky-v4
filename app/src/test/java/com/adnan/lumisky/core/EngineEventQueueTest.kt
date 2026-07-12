package com.example.lumisky.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EngineEventQueueTest {
    @Test
    fun retainsOnlyLatestHighFrequencyInputsAndPreservesLifecycleEvents() {
        val queue = EngineEventQueue()
        val drained = mutableListOf<WallpaperEvent>()

        queue.offer(WallpaperEvent.ScreenOn)
        queue.offer(WallpaperEvent.ParallaxChanged(0.1f, 0.2f))
        queue.offer(WallpaperEvent.ParallaxChanged(0.3f, 0.4f))
        queue.offer(WallpaperEvent.Touch(1f, 2f))
        queue.offer(WallpaperEvent.Touch(3f, 4f))
        queue.drainTo(drained)

        assertTrue(drained.first() is WallpaperEvent.ScreenOn)
        assertEquals(WallpaperEvent.ParallaxChanged(0.3f, 0.4f), drained[1])
        assertEquals(WallpaperEvent.Touch(3f, 4f), drained[2])
    }

    @Test
    fun highFrequencyEventsHaveBoundedBacklog() {
        val queue = EngineEventQueue()
        val drained = mutableListOf<WallpaperEvent>()

        repeat(10_000) { index ->
            queue.offer(WallpaperEvent.ParallaxChanged(index.toFloat(), -index.toFloat()))
            queue.offer(WallpaperEvent.Touch(index.toFloat(), index.toFloat()))
        }

        queue.drainTo(drained)

        assertEquals(2, drained.size)
        assertEquals(WallpaperEvent.ParallaxChanged(9_999f, -9_999f), drained[0])
        assertEquals(WallpaperEvent.Touch(9_999f, 9_999f), drained[1])
    }
}
