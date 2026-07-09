package com.adnan.lumisky.definition

import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertTrue
import org.junit.Test

class DefinitionValidatorTest {
    private val validator = DefinitionValidator()

    @Test
    fun rejectsUnknownLayerTypes() {
        val definition = baseDefinition(
            layers = listOf(
                LayerDefinition(
                    id = "bad_layer",
                    type = "UnknownLayer",
                    shaderRef = "wallpapers/sky/fragment.glsl"
                )
            )
        )

        val result = validator.validate(definition) { true }

        assertInvalidContains(result, "Unsupported layer type")
    }

    @Test
    fun rejectsMissingShaderAndTextureAssets() {
        val definition = baseDefinition(
            layers = listOf(
                LayerDefinition(
                    id = "main_shader",
                    type = "ShaderLayer",
                    shaderRef = "wallpapers/missing/fragment.glsl",
                    textures = listOf(
                        TextureSlotDefinition(
                            uniform = "backgroundTexture",
                            path = "wallpapers/missing/background.webp"
                        )
                    )
                )
            ),
            preview = PreviewDefinition(thumbnail = "wallpapers/missing/thumbnail.webp")
        )

        val result = validator.validate(definition) { false }

        assertInvalidContains(result, "Missing shader asset")
        assertInvalidContains(result, "Missing texture asset")
        assertInvalidContains(result, "Missing preview thumbnail")
    }

    @Test
    fun rejectsUnsupportedEnumValuesAndUniformTypes() {
        val definition = baseDefinition(
            layers = listOf(
                LayerDefinition(
                    id = "main_shader",
                    type = "ShaderLayer",
                    renderPass = "BAD_PASS",
                    blendMode = "BAD_BLEND",
                    renderTarget = "BAD_TARGET",
                    shaderRef = "wallpapers/sky/fragment.glsl",
                    framePolicy = LayerFramePolicyDefinition(mode = "BAD_MODE", cacheMode = "BAD_CACHE"),
                    uniforms = mapOf(
                        "u_Bad" to UniformValueDefinition(type = "bad_type", value = JsonPrimitive(1))
                    )
                )
            )
        )

        val result = validator.validate(definition) { true }

        assertInvalidContains(result, "Unsupported renderPass")
        assertInvalidContains(result, "Unsupported blendMode")
        assertInvalidContains(result, "Unsupported renderTarget")
        assertInvalidContains(result, "Unsupported framePolicy mode")
        assertInvalidContains(result, "Unsupported framePolicy cacheMode")
        assertInvalidContains(result, "Unsupported uniform type")
    }

    private fun baseDefinition(
        layers: List<LayerDefinition>,
        preview: PreviewDefinition = PreviewDefinition(thumbnail = "wallpapers/sky/thumbnail.webp")
    ): WallpaperDefinition {
        return WallpaperDefinition(
            id = "sky",
            name = "Sky",
            category = "Classic",
            layers = layers,
            preview = preview
        )
    }

    private fun assertInvalidContains(result: ValidationResult, expected: String) {
        assertTrue("Expected invalid result, got $result", result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(
            "Expected one validation error to contain '$expected', got ${invalid.errors}",
            invalid.errors.any { it.contains(expected) }
        )
    }
}
