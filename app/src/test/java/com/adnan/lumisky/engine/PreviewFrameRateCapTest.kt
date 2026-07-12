package com.example.lumisky.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class PreviewFrameRateCapTest {
    @Test
    fun capsFullscreenToPhysicalDisplayAndKeepsCatalogAtSixty() {
        assertEquals(90, PreviewFrameRateCap.resolve(profileMaxFps = 120, displayMaxFps = 90))
        assertEquals(60, PreviewFrameRateCap.resolve(profileMaxFps = 60, displayMaxFps = 90))
    }
}
