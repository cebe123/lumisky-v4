package com.example.lumisky.core

object WallpaperEngineSelectionPolicy {
    fun resolve(
        isPreview: Boolean,
        selectedWallpaperId: String,
        previewWallpaperId: String?
    ): String = if (isPreview) {
        previewWallpaperId ?: selectedWallpaperId
    } else {
        selectedWallpaperId
    }
}
