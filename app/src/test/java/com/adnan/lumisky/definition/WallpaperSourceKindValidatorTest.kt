package com.example.lumisky.definition

import org.junit.Assert.assertTrue
import org.junit.Test

class WallpaperSourceKindValidatorTest {
    @Test
    fun videoSourceWithoutVideoLayerIsRejected() {
        val definition = WallpaperDefinition(
            id = "video",
            name = "Video",
            category = "test",
            sourceKind = WallpaperSourceKind.VIDEO
        )

        val result = DefinitionValidator().validate(definition)

        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun timeSliceLayerRequiresItsDeclaredAssets() {
        val definition = WallpaperDefinition(
            id = "time-slices",
            name = "Time slices",
            category = "test",
            layers = listOf(
                LayerDefinition(
                    id = "sky",
                    type = "TimeSliceTextureLayer",
                    timeSlices = listOf(TimeSliceDefinition(minute = 0, path = "slices/night.png"))
                )
            )
        )

        val result = DefinitionValidator().validate(definition) { it != "slices/night.png" }

        assertTrue(result is ValidationResult.Invalid)
    }
}
