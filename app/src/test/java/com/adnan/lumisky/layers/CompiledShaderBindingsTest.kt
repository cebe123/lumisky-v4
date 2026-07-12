package com.example.lumisky.layers

import com.example.lumisky.definition.LayerDefinition
import com.example.lumisky.definition.TextureSlotDefinition
import com.example.lumisky.definition.UniformValueDefinition
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompiledShaderBindingsTest {
    @Test
    fun compilesJsonUniformsAndTextureSlotsOnce() {
        val bindings = CompiledShaderBindings.compile(
            LayerDefinition(
                id = "shader",
                type = "ShaderLayer",
                uniforms = linkedMapOf(
                    "u_Amount" to UniformValueDefinition("float", JsonPrimitive(0.5f)),
                    "u_Tint" to UniformValueDefinition(
                        "vec3",
                        buildJsonArray {
                            add(JsonPrimitive(0.1f))
                            add(JsonPrimitive(0.2f))
                            add(JsonPrimitive(0.3f))
                        }
                    )
                ),
                textures = listOf(
                    TextureSlotDefinition("u_Base", "textures/base.png"),
                    TextureSlotDefinition("u_Noise", "textures/noise.png")
                )
            )
        )

        assertEquals(2, bindings.uniforms.size)
        assertTrue(bindings.uniforms[0] is CompiledUniformBinding.Float1)
        assertTrue(bindings.uniforms[1] is CompiledUniformBinding.Float3)
        assertEquals("u_Base", bindings.textures[0].uniform)
        assertEquals("textures/noise.png", bindings.textures[1].asset.path)
        assertEquals(1, bindings.textures[1].unit)
    }
}
