package com.example.lumisky.engine.gl

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextureUploadBudgetTest {
    @Test
    fun limitsUploadsUntilNextFrame() {
        val budget = TextureUploadBudget(maxUploadsPerFrame = 2)

        assertTrue(budget.tryAcquireUpload())
        assertTrue(budget.tryAcquireUpload())
        assertFalse(budget.tryAcquireUpload())

        budget.beginFrame()

        assertTrue(budget.tryAcquireUpload())
    }
}
