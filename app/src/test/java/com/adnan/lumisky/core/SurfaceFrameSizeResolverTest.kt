package com.adnan.lumisky.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SurfaceFrameSizeResolverTest {
    @Test
    fun resolvesPositiveSizeFromSurfaceFrameBounds() {
        val size = SurfaceFrameSizeResolver.resolve(left = 0, top = 10, right = 1080, bottom = 2410)

        assertEquals(1080, size.width)
        assertEquals(2400, size.height)
        assertTrue(size.isValid)
    }

    @Test
    fun clampsInvalidSurfaceFrameToZeroSize() {
        val size = SurfaceFrameSizeResolver.resolve(left = 100, top = 100, right = 50, bottom = 75)

        assertEquals(0, size.width)
        assertEquals(0, size.height)
        assertFalse(size.isValid)
    }
}
