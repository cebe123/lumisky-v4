package com.example.lumisky.engine

import com.example.lumisky.definition.WallpaperDefinition
import com.example.lumisky.registry.SceneFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HybridSceneRendererBackend @Inject constructor(
    private val sceneFactory: SceneFactory
) {
    fun applyCompiledGraph(
        definition: WallpaperDefinition,
        graph: CompiledLayerGraph
    ): RuntimeScene = sceneFactory.create(definition, graph)
}
