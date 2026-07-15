package com.example.lumisky.ui.catalog

import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogStartupWarmupPolicyTest {
    @Test
    fun resolvesTheSameCardPixelSizeUsedByTheCatalog() {
        val size = CatalogStartupWarmupPolicy.resolveTargetSize(360, 800, 3f)

        assertEquals(828, size.widthPx)
        assertEquals(1656, size.heightPx)
    }

    @Test
    fun warmsEachThumbnailPathOnlyOnce() {
        assertEquals(
            listOf("a.webp", "b.webp"),
            CatalogStartupWarmupPolicy.uniqueThumbnailPaths(listOf("a.webp", "a.webp", "", "b.webp"))
        )
    }

    @Test
    fun limitsParallelThumbnailWarmupToFourItemsPerBatch() {
        assertEquals(
            listOf(listOf("a", "b", "c", "d"), listOf("e")),
            CatalogStartupWarmupPolicy.thumbnailBatches(
                paths = listOf("a", "b", "c", "d", "e"),
                maxParallelism = 4
            )
        )
    }
}
