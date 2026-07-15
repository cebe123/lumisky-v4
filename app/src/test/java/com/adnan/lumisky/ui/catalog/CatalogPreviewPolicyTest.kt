package com.example.lumisky.ui.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogPreviewPolicyTest {

    @Test
    fun onlyCenteredItemInActiveSectionCanRenderLivePreview() {
        assertTrue(
            CatalogPreviewPolicy.shouldRenderLivePreview(
                sectionIndex = 1,
                activeSectionIndex = 1,
                itemIndex = 2,
                centeredItemIndex = 2,
                parentScrollInProgress = false,
                rowScrollInProgress = false
            )
        )
        assertFalse(
            CatalogPreviewPolicy.shouldRenderLivePreview(
                sectionIndex = 0,
                activeSectionIndex = 1,
                itemIndex = 2,
                centeredItemIndex = 2,
                parentScrollInProgress = false,
                rowScrollInProgress = false
            )
        )
        assertFalse(
            CatalogPreviewPolicy.shouldRenderLivePreview(
                sectionIndex = 1,
                activeSectionIndex = 1,
                itemIndex = 0,
                centeredItemIndex = 2,
                parentScrollInProgress = false,
                rowScrollInProgress = false
            )
        )
    }

    @Test
    fun livePreviewIsPausedWhileParentOrRowIsScrolling() {
        assertFalse(
            CatalogPreviewPolicy.shouldRenderLivePreview(
                sectionIndex = 0,
                activeSectionIndex = 0,
                itemIndex = 0,
                centeredItemIndex = 0,
                parentScrollInProgress = true,
                rowScrollInProgress = false
            )
        )
        assertFalse(
            CatalogPreviewPolicy.shouldRenderLivePreview(
                sectionIndex = 0,
                activeSectionIndex = 0,
                itemIndex = 0,
                centeredItemIndex = 0,
                parentScrollInProgress = false,
                rowScrollInProgress = true
            )
        )
    }

    @Test
    fun livePreviewSurfaceMountingFollowsFocusAndScrollState() {
        assertTrue(
            CatalogPreviewPolicy.shouldMountLivePreview(
                sectionIndex = 1,
                activeSectionIndex = 1,
                itemIndex = 2,
                centeredItemIndex = 2,
                parentScrollInProgress = false,
                rowScrollInProgress = false
            )
        )
        assertFalse(
            CatalogPreviewPolicy.shouldMountLivePreview(
                sectionIndex = 1,
                activeSectionIndex = 1,
                itemIndex = 2,
                centeredItemIndex = 2,
                parentScrollInProgress = false,
                rowScrollInProgress = true
            )
        )
        assertFalse(
            CatalogPreviewPolicy.shouldMountLivePreview(
                sectionIndex = 1,
                activeSectionIndex = 1,
                itemIndex = 0,
                centeredItemIndex = 2,
                parentScrollInProgress = false,
                rowScrollInProgress = false
            )
        )
    }

    @Test
    fun unresolvedActiveSectionDisablesLivePreviewUntilCentered() {
        assertEquals(-1, CatalogPreviewPolicy.resolveActiveSectionIndex(-1, 4))
        assertEquals(2, CatalogPreviewPolicy.resolveActiveSectionIndex(2, 4))
        assertEquals(0, CatalogPreviewPolicy.resolveActiveSectionIndex(7, 4))
    }

    @Test
    fun formatsLoopProgressAsTwentyFourHourClock() {
        assertEquals("00:00", CatalogPreviewPolicy.formatLoopTime(0.0f))
        assertEquals("06:00", CatalogPreviewPolicy.formatLoopTime(0.25f))
        assertEquals("12:00", CatalogPreviewPolicy.formatLoopTime(0.5f))
        assertEquals("23:59", CatalogPreviewPolicy.formatLoopTime(0.9999f))
    }

    @Test
    fun formatsBadgeTimeFromRendererProgressWhenAvailable() {
        assertEquals(
            "06:00",
            CatalogPreviewPolicy.formatBadgeTime(
                rendererDayProgress = 0.25f,
                fallbackHour = 14,
                fallbackMinute = 45
            )
        )
    }

    @Test
    fun formatsBadgeTimeFromFallbackWhenRendererHasNotRenderedYet() {
        assertEquals(
            "14:05",
            CatalogPreviewPolicy.formatBadgeTime(
                rendererDayProgress = null,
                fallbackHour = 14,
                fallbackMinute = 5
            )
        )
    }

    @Test
    fun livePreviewBadgeUsesCoarseUiTicksInsteadOfFrameTicks() {
        assertEquals(1000L, CatalogPreviewPolicy.livePreviewBadgeTickMillis())
        assertEquals(0.0f, CatalogPreviewPolicy.loopProgressForElapsedMillis(0), 0.001f)
        assertEquals(0.5f, CatalogPreviewPolicy.loopProgressForElapsedMillis(6_000), 0.001f)
    }

    @Test
    fun cardChromeStaysVisibleWhileScrolling() {
        assertTrue(CatalogPreviewPolicy.shouldRenderCardChrome(parentScrollInProgress = true, rowScrollInProgress = false))
        assertTrue(CatalogPreviewPolicy.shouldRenderCardChrome(parentScrollInProgress = false, rowScrollInProgress = true))
        assertTrue(CatalogPreviewPolicy.shouldRenderCardChrome(parentScrollInProgress = false, rowScrollInProgress = false))
    }

    @Test
    fun livePreviewStartsOnlyAfterWarmupIsReady() {
        assertFalse(CatalogPreviewPolicy.shouldStartLivePreview(showLivePreview = true, warmupReady = false))
        assertFalse(CatalogPreviewPolicy.shouldStartLivePreview(showLivePreview = false, warmupReady = true))
        assertTrue(CatalogPreviewPolicy.shouldStartLivePreview(showLivePreview = true, warmupReady = true))
    }



    @Test
    fun previewFocusDelayMatchesTheFastV2Interaction() {
        assertEquals(0L, CatalogPreviewPolicy.previewFocusDelayMillis())
    }

    @Test
    fun initialPreviewStartsWithoutAnArtificialDelay() {
        assertEquals(0L, CatalogPreviewPolicy.initialPreviewDelayMillis())
    }

}
