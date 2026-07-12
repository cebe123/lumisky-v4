/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Layer type, shaderRef, uniforms, framePolicy, parallax tanımı.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Layer type, shaderRef, uniforms, framePolicy, parallax tanımı.
 */
package com.example.lumisky.definition

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class LayerDefinition(
    val id: String,
    val type: String,
    val enabled: Boolean = true,
    val zIndex: Int = 0,
    val renderPass: String = "BACKGROUND",
    val blendMode: String = "NONE",
    val renderTarget: String = "DIRECT",
    val source: String? = null,
    val timeSlices: List<TimeSliceDefinition> = emptyList(),
    val shaderRef: String? = null,
    val uniforms: Map<String, UniformValueDefinition> = emptyMap(),
    val textures: List<TextureSlotDefinition> = emptyList(),
    val animation: AnimationDefinition? = null,
    val framePolicy: LayerFramePolicyDefinition? = null,
    val parallax: LayerParallaxDefinition? = null,
    val fallback: LayerFallbackDefinition? = null
)

@Serializable
data class TimeSliceDefinition(
    val minute: Int,
    val path: String
)

@Serializable
data class UniformValueDefinition(
    val type: String, // float, int, vec3, vec4, mat4, etc.
    val value: JsonElement
)

@Serializable
data class LayerFramePolicyDefinition(
    val mode: String = "MATCH_SCENE",
    val fps: Int? = null,
    val cacheMode: String = "NONE",
    val updateWhenInvisible: Boolean = false,
    val degradeInBatterySaver: Boolean = true,
    val batterySaverFps: Int? = null,
    val idleFps: Int? = null
)

@Serializable
data class LayerParallaxDefinition(
    val factorX: Float = 0.0f,
    val factorY: Float = 0.0f,
    val depth: Float = 0.0f
)

@Serializable
data class LayerFallbackDefinition(
    val onShaderError: String = "disable_layer",
    val onMissingTexture: String = "use_transparent_texture"
)
