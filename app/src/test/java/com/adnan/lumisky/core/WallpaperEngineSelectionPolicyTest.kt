package com.example.lumisky.core

import org.junit.Assert.assertEquals
import org.junit.Test

class WallpaperEngineSelectionPolicyTest {
    @Test
    fun previewEngineUsesCandidateWithoutChangingLiveEngineSelection() {
        assertEquals(
            "candidate",
            WallpaperEngineSelectionPolicy.resolve(
                isPreview = true,
                selectedWallpaperId = "active",
                previewWallpaperId = "candidate"
            )
        )
        assertEquals(
            "active",
            WallpaperEngineSelectionPolicy.resolve(
                isPreview = false,
                selectedWallpaperId = "active",
                previewWallpaperId = "candidate"
            )
        )
    }

    @Test
    fun previewEngineFallsBackToActiveSelectionWithoutCandidate() {
        assertEquals(
            "active",
            WallpaperEngineSelectionPolicy.resolve(
                isPreview = true,
                selectedWallpaperId = "active",
                previewWallpaperId = null
            )
        )
    }
}
