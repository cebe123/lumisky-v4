package com.example.lumisky.engine.gl

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EglLifecycleStateTest {
    @Test
    fun surfaceDetachPreservesContextUntilExplicitContextLoss() {
        val state = EglLifecycleState()

        state.onContextCreated()
        state.onSurfaceCreated()
        state.onSurfaceDestroyed()

        assertTrue(state.hasContext)
        assertFalse(state.hasSurface)

        state.onContextLost()

        assertFalse(state.hasContext)
        assertFalse(state.hasSurface)
    }

    @Test
    fun forcedContextLossRequiresFreshContextBeforeSurfaceRestore() {
        val state = EglLifecycleState()
        state.onContextCreated()
        state.onSurfaceCreated()
        state.onContextLost()

        assertFalse(state.hasContext)
        assertFalse(state.hasSurface)

        state.onContextCreated()
        state.onSurfaceCreated()

        assertTrue(state.hasContext)
        assertTrue(state.hasSurface)
    }
}
