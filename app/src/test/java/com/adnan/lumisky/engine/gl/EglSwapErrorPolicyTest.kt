package com.example.lumisky.engine.gl

import org.junit.Assert.assertEquals
import org.junit.Test

class EglSwapErrorPolicyTest {
    @Test
    fun classifiesContextLossSeparatelyFromSurfaceFailure() {
        assertEquals(EglSwapResult.SUCCESS, EglSwapErrorPolicy.classify(true, 0, 0x300E))
        assertEquals(EglSwapResult.CONTEXT_LOST, EglSwapErrorPolicy.classify(false, 0x300E, 0x300E))
        assertEquals(EglSwapResult.SURFACE_LOST, EglSwapErrorPolicy.classify(false, 0x300D, 0x300E))
    }
}
