package com.example.lumisky.layers

import com.example.lumisky.definition.TimeSliceDefinition

internal class TimeSliceTexturePlan(timeSlices: List<TimeSliceDefinition>) {
    private val slices = timeSlices
        .sortedBy { it.minute }
        .map { TimeSliceAsset(it.minute, TextureAssetHandle.required(it.path)) }
        .toTypedArray()

    fun assetFor(minute: Int): TextureAssetHandle? {
        if (slices.isEmpty()) return null
        val normalizedMinute = ((minute % MINUTES_PER_DAY) + MINUTES_PER_DAY) % MINUTES_PER_DAY
        for (index in slices.indices.reversed()) {
            if (normalizedMinute >= slices[index].minute) return slices[index].asset
        }
        return slices.last().asset
    }

    fun preload(gl: com.example.lumisky.engine.gl.GlResourceManager) {
        for (slice in slices) slice.asset.preload(gl)
    }

    private data class TimeSliceAsset(val minute: Int, val asset: TextureAssetHandle)

    private companion object {
        const val MINUTES_PER_DAY = 24 * 60
    }
}
