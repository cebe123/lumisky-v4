package com.example.lumisky.engine

import com.example.lumisky.definition.LayerDefinition
import com.example.lumisky.definition.WallpaperDefinition
import com.example.lumisky.engine.pipeline.RenderPass

data class CompiledLayerGraph(
    val orderedPasses: Array<CompiledRenderPass>,
    val layersByIndex: Array<CompiledLayer>,
    val fallbackPlan: SceneFallbackPlan,
    val animationPlan: CompiledAnimationPlan
) {
    companion object {
        fun compile(definition: WallpaperDefinition): CompiledLayerGraph {
            val layers = definition.layers.withIndex()
                .asSequence()
                .filter { it.value.enabled }
                .map { indexed ->
                    CompiledLayer(
                        definition = indexed.value,
                        declarationIndex = indexed.index,
                        renderPass = indexed.value.renderPass.toRenderPass()
                    )
                }
                .sortedWith(
                    compareBy<CompiledLayer> { it.renderPass.ordinal }
                        .thenBy { it.definition.zIndex }
                        .thenBy { it.declarationIndex }
                )
                .toList()
                .toTypedArray()

            val passes = RenderPass.entries.mapNotNull { pass ->
                val firstLayerIndex = layers.indexOfFirst { it.renderPass == pass }
                if (firstLayerIndex < 0) {
                    null
                } else {
                    CompiledRenderPass(
                        pass = pass,
                        firstLayerIndex = firstLayerIndex,
                        layerCount = layers.drop(firstLayerIndex).takeWhile { it.renderPass == pass }.size
                    )
                }
            }.toTypedArray()
            return CompiledLayerGraph(
                orderedPasses = passes,
                layersByIndex = layers,
                fallbackPlan = SceneFallbackPlan.compile(layers),
                animationPlan = CompiledAnimationPlan.compile(layers)
            )
        }

        private fun String.toRenderPass(): RenderPass =
            RenderPass.entries.firstOrNull { it.name == this } ?: RenderPass.BACKGROUND
    }
}

data class CompiledRenderPass(
    val pass: RenderPass,
    val firstLayerIndex: Int,
    val layerCount: Int
)

data class CompiledLayer(
    val definition: LayerDefinition,
    val declarationIndex: Int,
    val renderPass: RenderPass
)

data class SceneFallbackPlan(
    val layersByIndex: Array<CompiledLayerFallback>
) {
    companion object {
        fun compile(layers: Array<CompiledLayer>): SceneFallbackPlan = SceneFallbackPlan(
            layers.map { layer ->
                val fallback = layer.definition.fallback
                CompiledLayerFallback(
                    onShaderError = ShaderFallbackAction.fromDefinition(fallback?.onShaderError),
                    onMissingTexture = TextureFallbackAction.fromDefinition(fallback?.onMissingTexture)
                )
            }.toTypedArray()
        )
    }
}

data class CompiledLayerFallback(
    val onShaderError: ShaderFallbackAction,
    val onMissingTexture: TextureFallbackAction
)

enum class ShaderFallbackAction {
    DISABLE_LAYER,
    USE_SAFE_SHADER;

    companion object {
        fun fromDefinition(value: String?): ShaderFallbackAction = when (value) {
            "use_safe_shader" -> USE_SAFE_SHADER
            else -> DISABLE_LAYER
        }
    }
}

enum class TextureFallbackAction {
    DISABLE_LAYER,
    USE_TRANSPARENT_TEXTURE;

    companion object {
        fun fromDefinition(value: String?): TextureFallbackAction = when (value) {
            "disable_layer" -> DISABLE_LAYER
            else -> USE_TRANSPARENT_TEXTURE
        }
    }
}

data class CompiledAnimationPlan(
    val tracksByLayerIndex: Array<CompiledAnimationTrack?>
) {
    companion object {
        fun compile(layers: Array<CompiledLayer>): CompiledAnimationPlan = CompiledAnimationPlan(
            layers.map { layer ->
                val animation = layer.definition.animation ?: return@map null
                val type = CompiledAnimationType.fromDefinition(animation.type) ?: return@map null
                CompiledAnimationTrack(
                    type = type,
                    durationNanos = animation.durationMs.coerceIn(1L, MAX_DURATION_MILLIS) * NANOS_PER_MILLI,
                    loop = animation.loop,
                    parameters = animation.parameters.map { (name, value) ->
                        CompiledAnimationParameter(name, value)
                    }.toTypedArray()
                )
            }.toTypedArray()
        )

        private const val NANOS_PER_MILLI = 1_000_000L
        private const val MAX_DURATION_MILLIS = Long.MAX_VALUE / NANOS_PER_MILLI
    }
}

data class CompiledAnimationTrack(
    val type: CompiledAnimationType,
    val durationNanos: Long,
    val loop: Boolean,
    val parameters: Array<CompiledAnimationParameter>
)

data class CompiledAnimationParameter(
    val name: String,
    val value: Float
)

enum class CompiledAnimationType {
    UV_SCROLL,
    PULSE,
    PATH_FOLLOW;

    companion object {
        fun fromDefinition(value: String): CompiledAnimationType? = when (value) {
            "UV_SCROLL" -> UV_SCROLL
            "PULSE" -> PULSE
            "PATH_FOLLOW" -> PATH_FOLLOW
            else -> null
        }
    }
}
