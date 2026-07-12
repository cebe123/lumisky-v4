package com.example.lumisky.layers

import com.example.lumisky.definition.TimeSliceDefinition
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeSliceTexturePlanTest {

    private val plan = TimeSliceTexturePlan(
        listOf(
            TimeSliceDefinition(minute = 0, path = "midnight"),
            TimeSliceDefinition(minute = 360, path = "dawn"),
            TimeSliceDefinition(minute = 720, path = "noon"),
            TimeSliceDefinition(minute = 1080, path = "sunset")
        )
    )

    @Test
    fun selectsLatestSliceAtOrBeforeCurrentMinute() {
        assertEquals("midnight", plan.assetFor(0)?.path)
        assertEquals("dawn", plan.assetFor(400)?.path)
        assertEquals("noon", plan.assetFor(900)?.path)
    }

    @Test
    fun wrapsBeforeFirstSliceToPreviousDaysFinalSlice() {
        val offsetPlan = TimeSliceTexturePlan(
            listOf(
                TimeSliceDefinition(minute = 120, path = "dawn"),
                TimeSliceDefinition(minute = 900, path = "night")
            )
        )

        assertEquals("night", offsetPlan.assetFor(60)?.path)
        assertEquals("dawn", offsetPlan.assetFor(1560)?.path)
    }
}
