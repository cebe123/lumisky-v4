/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Layer type, shaderRef, texture path, capability matrix doğrulaması.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Layer type, shaderRef, texture path, capability matrix doğrulaması.
 */
package com.example.lumisky.definition

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefinitionValidator @Inject constructor() {
    private val supportedLayerTypes = setOf(
        "AnimationLayer",
        "CloudLayer",
        "FogLayer",
        "ForegroundLayer",
        "MoonLayer",
        "RainLayer",
        "ShaderLayer",
        "ShaderEffectLayer",
        "ParticleLayer",
        "StarsLayer",
        "SunLayer",
        "TimeSliceTextureLayer",
        "TextureLayer",
        "VideoOesLayer"
    )
    private val supportedRenderPasses = setOf(
        "BACKGROUND",
        "OPAQUE",
        "ALPHA_TESTED",
        "TRANSPARENT",
        "POST_PROCESS",
        "OVERLAY"
    )
    private val supportedBlendModes = setOf("NONE", "ALPHA", "ADDITIVE", "MULTIPLY", "SCREEN")
    private val supportedRenderTargets = setOf("DIRECT", "OFFSCREEN_FBO", "CACHED_TEXTURE", "POST_PROCESS")
    private val supportedFrameModes = setOf(
        "STATIC",
        "ON_DEMAND",
        "MINUTE_TICK",
        "ONE_FPS",
        "FIXED_FPS",
        "MATCH_SCENE",
        "CONTINUOUS",
        "VIDEO_SYNC",
        "EVENT_BASED"
    )
    private val supportedCacheModes = setOf("NONE", "CPU_STATE_ONLY", "FBO_CACHE", "STATIC_TEXTURE")
    private val supportedUniformTypes = setOf("float", "int", "vec2", "vec3", "vec4", "mat4")
    private val builtInShaderRefs = mapOf(
        "common.gradient.v1" to "shaders/common/gradient.frag",
        "common.video.oes" to "shaders/common/video_oes.frag",
        "common.texture2d" to "shaders/common/texture2d.frag"
    )

    fun validate(definition: WallpaperDefinition): ValidationResult {
        return validate(definition) { true }
    }

    fun validate(
        definition: WallpaperDefinition,
        assetExists: (String) -> Boolean
    ): ValidationResult {
        val errors = mutableListOf<String>()
        if (definition.id.isBlank()) errors.add("Wallpaper ID is blank")
        if (definition.name.isBlank()) errors.add("Wallpaper name is blank")
        if (definition.category.isBlank()) errors.add("Wallpaper category is blank")
        if (definition.schemaVersion != 5) errors.add("Unsupported schemaVersion: ${definition.schemaVersion}")
        if (definition.sourceKind == WallpaperSourceKind.VIDEO &&
            definition.layers.none { it.enabled && it.type == "VideoOesLayer" }
        ) {
            errors.add("VIDEO wallpaper requires an enabled VideoOesLayer")
        }
        if (definition.sourceKind != WallpaperSourceKind.PROCEDURAL &&
            definition.layers.none { it.enabled }
        ) {
            errors.add("${definition.sourceKind} wallpaper requires at least one enabled layer")
        }
        if (definition.preview.thumbnail.isNotBlank() && !assetExists(definition.preview.thumbnail)) {
            errors.add("Missing preview thumbnail: ${definition.preview.thumbnail}")
        }
        
        definition.layers.forEach { layer ->
            if (layer.id.isBlank()) {
                errors.add("Layer ID is blank")
            }
            if (layer.type.isBlank()) {
                errors.add("Layer type is blank for layer ID: ${layer.id}")
            } else if (layer.type !in supportedLayerTypes) {
                errors.add("Unsupported layer type '${layer.type}' for layer ID: ${layer.id}")
            }
            if (layer.renderPass !in supportedRenderPasses) {
                errors.add("Unsupported renderPass '${layer.renderPass}' for layer ID: ${layer.id}")
            }
            if (layer.blendMode !in supportedBlendModes) {
                errors.add("Unsupported blendMode '${layer.blendMode}' for layer ID: ${layer.id}")
            }
            if (layer.renderTarget !in supportedRenderTargets) {
                errors.add("Unsupported renderTarget '${layer.renderTarget}' for layer ID: ${layer.id}")
            }
            val framePolicy = layer.framePolicy
            if (framePolicy != null) {
                if (framePolicy.mode !in supportedFrameModes) {
                    errors.add("Unsupported framePolicy mode '${framePolicy.mode}' for layer ID: ${layer.id}")
                }
                if (framePolicy.cacheMode !in supportedCacheModes) {
                    errors.add("Unsupported framePolicy cacheMode '${framePolicy.cacheMode}' for layer ID: ${layer.id}")
                }
                if (framePolicy.fps != null && framePolicy.fps <= 0) {
                    errors.add("Invalid framePolicy fps '${framePolicy.fps}' for layer ID: ${layer.id}")
                }
            }
            layer.uniforms.forEach { (name, uniform) ->
                if (uniform.type !in supportedUniformTypes) {
                    errors.add("Unsupported uniform type '${uniform.type}' for uniform '$name' in layer ID: ${layer.id}")
                }
            }
            layer.shaderRef?.let { shaderRef ->
                val shaderPath = builtInShaderRefs[shaderRef] ?: shaderRef
                if (!assetExists(shaderPath)) {
                    errors.add("Missing shader asset: $shaderRef for layer ID: ${layer.id}")
                }
            }
            layer.source?.let { source ->
                if (!assetExists(source)) {
                    errors.add("Missing source asset: $source for layer ID: ${layer.id}")
                }
            }
            if (layer.type == "TimeSliceTextureLayer" && layer.timeSlices.isEmpty()) {
                errors.add("TimeSliceTextureLayer requires time slices for layer ID: ${layer.id}")
            }
            layer.timeSlices.forEach { slice ->
                if (slice.minute !in 0..1439) {
                    errors.add("Invalid time slice minute '${slice.minute}' for layer ID: ${layer.id}")
                }
                if (slice.path.isBlank()) {
                    errors.add("Time slice path is blank for layer ID: ${layer.id}")
                } else if (!assetExists(slice.path)) {
                    errors.add("Missing time slice asset: ${slice.path} for layer ID: ${layer.id}")
                }
            }
            layer.textures.forEach { texture ->
                if (texture.uniform.isBlank()) {
                    errors.add("Texture uniform is blank for layer ID: ${layer.id}")
                }
                if (texture.path.isBlank()) {
                    errors.add("Texture path is blank for layer ID: ${layer.id}")
                } else if (!assetExists(texture.path)) {
                    errors.add("Missing texture asset: ${texture.path} for layer ID: ${layer.id}")
                }
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}

sealed interface ValidationResult {
    object Valid : ValidationResult
    data class Invalid(val errors: List<String>) : ValidationResult
}

typealias DefinitionValidationResult = ValidationResult
