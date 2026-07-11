package com.example.lumisky.engine

import com.example.lumisky.definition.QualityTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class RuntimeProfileTest {

    @Test
    fun catalogCardPreviewRunsAtQuarterLiveWallpaperScale() {
        val live = RuntimeProfile.liveWallpaper()
        val catalog = RuntimeProfile.catalogCardPreview()

        assertEquals(live.maxFps / 2, catalog.maxFps)
        assertEquals(QualityTier.LOW, catalog.overrideQualityTier)
        assertEquals(0.25f, catalog.renderScale, 0.001f)
        assertFalse(catalog.playVideo)
    }
}
