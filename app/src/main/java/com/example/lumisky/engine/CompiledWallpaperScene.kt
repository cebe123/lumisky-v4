package com.example.lumisky.engine

import com.example.lumisky.definition.WallpaperSourceKind

data class CompiledWallpaperScene(
    val id: String,
    val sourceKind: WallpaperSourceKind,
    val layerGraph: CompiledLayerGraph,
    val runtimeScene: RuntimeScene
)
