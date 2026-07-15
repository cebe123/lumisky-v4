package com.example.lumisky.engine.gl

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TexturePendingWorkPolicyTest {
    @Test
    fun onlyRequestedTextureWorkBlocksAFrame() {
        assertFalse(TexturePendingWorkPolicy.hasRenderBlockingWork(emptySet(), setOf("unused"), setOf("unused")))
        assertTrue(TexturePendingWorkPolicy.hasRenderBlockingWork(setOf("visible"), setOf("visible"), setOf("unused")))
        assertTrue(TexturePendingWorkPolicy.hasRenderBlockingWork(setOf("visible"), emptySet(), setOf("visible")))
    }
}
