package com.example.lumisky.ui.catalog

import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogThumbnailDecodePolicyTest {

    @Test
    fun catalogThumbnailsPreferHalfQualityEvenWhenNearCardSize() {
        assertEquals(
            2,
            CatalogThumbnailDecodePolicy.calculateInSampleSize(
                sourceWidth = 990,
                sourceHeight = 1858,
                targetWidth = 760,
                targetHeight = 1440
            )
        )
    }

    @Test
    fun downsamplesOversizedThumbnailsByPowersOfTwo() {
        assertEquals(
            4,
            CatalogThumbnailDecodePolicy.calculateInSampleSize(
                sourceWidth = 4000,
                sourceHeight = 8000,
                targetWidth = 760,
                targetHeight = 1440
            )
        )
    }

    @Test
    fun capsHighDensityCardThumbnailsBeforeChoosingTheSampleSize() {
        assertEquals(
            4,
            CatalogThumbnailDecodePolicy.calculateInSampleSize(
                sourceWidth = 2_160,
                sourceHeight = 3_840,
                targetWidth = 760,
                targetHeight = 1_440
            )
        )
    }
}
