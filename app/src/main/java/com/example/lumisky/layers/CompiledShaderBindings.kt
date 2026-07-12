package com.example.lumisky.layers

import com.example.lumisky.definition.LayerDefinition
import com.example.lumisky.engine.gl.GlProgram
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.float
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive

internal data class CompiledShaderBindings(
    val uniforms: Array<CompiledUniformBinding>,
    val textures: Array<CompiledTextureBinding>
) {
    companion object {
        fun compile(definition: LayerDefinition): CompiledShaderBindings {
            val uniforms = definition.uniforms.mapNotNull { (name, value) ->
                compileUniform(name, value.type, value.value)
            }.toTypedArray()
            val textures = definition.textures.mapIndexed { index, texture ->
                CompiledTextureBinding(texture.uniform, TextureAssetHandle.required(texture.path), index)
            }.toTypedArray()
            return CompiledShaderBindings(uniforms, textures)
        }

        private fun compileUniform(
            name: String,
            type: String,
            value: kotlinx.serialization.json.JsonElement
        ): CompiledUniformBinding? = try {
            when (type) {
                "float" -> CompiledUniformBinding.Float1(name, (value as? JsonPrimitive)?.float ?: 0f)
                "int" -> CompiledUniformBinding.Int1(name, (value as? JsonPrimitive)?.int ?: 0)
                "vec2" -> (value as? JsonArray)?.takeIf { it.size >= 2 }?.let {
                    CompiledUniformBinding.Float2(name, it[0].jsonPrimitive.float, it[1].jsonPrimitive.float)
                }
                "vec3" -> (value as? JsonArray)?.takeIf { it.size >= 3 }?.let {
                    CompiledUniformBinding.Float3(name, it[0].jsonPrimitive.float, it[1].jsonPrimitive.float, it[2].jsonPrimitive.float)
                }
                "vec4" -> (value as? JsonArray)?.takeIf { it.size >= 4 }?.let {
                    CompiledUniformBinding.Float4(
                        name,
                        it[0].jsonPrimitive.float,
                        it[1].jsonPrimitive.float,
                        it[2].jsonPrimitive.float,
                        it[3].jsonPrimitive.float
                    )
                }
                else -> null
            }
        } catch (_: Throwable) {
            null
        }
    }
}

internal sealed class CompiledUniformBinding {
    abstract fun bind(program: GlProgram)

    data class Float1(private val name: String, private val value: Float) : CompiledUniformBinding() {
        override fun bind(program: GlProgram) = program.setUniform(name, value)
    }

    data class Int1(private val name: String, private val value: Int) : CompiledUniformBinding() {
        override fun bind(program: GlProgram) = program.setUniform(name, value)
    }

    data class Float2(private val name: String, private val x: Float, private val y: Float) : CompiledUniformBinding() {
        override fun bind(program: GlProgram) = program.setUniform(name, x, y)
    }

    data class Float3(private val name: String, private val x: Float, private val y: Float, private val z: Float) : CompiledUniformBinding() {
        override fun bind(program: GlProgram) = program.setUniform(name, x, y, z)
    }

    data class Float4(
        private val name: String,
        private val x: Float,
        private val y: Float,
        private val z: Float,
        private val w: Float
    ) : CompiledUniformBinding() {
        override fun bind(program: GlProgram) = program.setUniform(name, x, y, z, w)
    }
}

internal data class CompiledTextureBinding(
    val uniform: String,
    val asset: TextureAssetHandle,
    val unit: Int
)
