package com.example.lumisky.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RenderLifecycleGateTest {
    @Test
    fun requiresBothVisibilityAndSurface() {
        assertFalse(RenderLifecycleGate.canRender(visible = false, hasSurface = true))
        assertFalse(RenderLifecycleGate.canRender(visible = true, hasSurface = false))
        assertTrue(RenderLifecycleGate.canRender(visible = true, hasSurface = true))
    }

    @Test
    fun invisibleStateDisablesSensorVideoAndCallbacks() {
        assertFalse(RenderLifecycleGate.canRunSensor(visible = false))
        assertFalse(RenderLifecycleGate.canRunVideo(visible = false, playbackEnabled = true))
        assertFalse(RenderLifecycleGate.canPublishCallback(visible = false))
        assertTrue(RenderLifecycleGate.canRunVideo(visible = true, playbackEnabled = true))
    }
}
