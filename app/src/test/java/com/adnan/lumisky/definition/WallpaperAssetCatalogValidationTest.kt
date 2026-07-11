package com.example.lumisky.definition

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class WallpaperAssetCatalogValidationTest {
    private val assetsDir = File("src/main/assets")
    private val parser = WallpaperDefinitionParser(SchemaMigrator())
    private val validator = DefinitionValidator()
    private val samplerRegex = Regex("""uniform\s+sampler2D\s+([A-Za-z_][A-Za-z0-9_]*)\s*;""")

    @Test
    fun everyCatalogWallpaperDefinitionAndReferencedAssetIsValid() {
        val catalogJson = File(assetsDir, "wallpapers/index.json").readText()
        val catalog = parser.parseCatalog(catalogJson)

        assertTrue("Catalog should contain wallpapers", catalog.wallpapers.isNotEmpty())

        val errors = mutableListOf<String>()
        catalog.wallpapers.forEach { item ->
            val definitionFile = File(assetsDir, item.definitionPath)
            if (!definitionFile.isFile) {
                errors.add("${item.id}: missing definition ${item.definitionPath}")
                return@forEach
            }

            validateDefinition(item.id, definitionFile, errors)
        }

        assertTrue(errors.joinToString(separator = "\n"), errors.isEmpty())
    }

    @Test
    fun starterGradientFallbackDefinitionAndReferencedAssetsAreValid() {
        val errors = mutableListOf<String>()
        val definitionFile = File(assetsDir, "wallpapers/starter_gradient.json")

        if (!definitionFile.isFile) {
            errors.add("starter_gradient: missing fallback definition")
        } else {
            validateDefinition("starter_gradient", definitionFile, errors)
        }

        assertTrue(errors.joinToString(separator = "\n"), errors.isEmpty())
    }

    @Test
    fun shaderLayerTextureUniformsMatchDeclaredSamplerUniforms() {
        val catalogJson = File(assetsDir, "wallpapers/index.json").readText()
        val catalog = parser.parseCatalog(catalogJson)
        val errors = mutableListOf<String>()

        catalog.wallpapers.forEach { item ->
            val definitionFile = File(assetsDir, item.definitionPath)
            when (val parseResult = parser.parseWallpaper(definitionFile.readText())) {
                is WallpaperParseResult.Error -> errors.add("${item.id}: ${parseResult.message}")
                is WallpaperParseResult.Success -> {
                    parseResult.definition.layers
                        .filter { it.type == "ShaderLayer" }
                        .forEach { layer ->
                            val shaderPath = layer.shaderRef ?: layer.source ?: return@forEach
                            val shaderFile = File(assetsDir, shaderPath)
                            if (!shaderFile.isFile) {
                                errors.add("${item.id}:${layer.id}: missing shader $shaderPath")
                                return@forEach
                            }

                            val declaredSamplers = samplerRegex.findAll(shaderFile.readText())
                                .map { it.groupValues[1] }
                                .toSet()
                            val boundSamplers = layer.textures.map { it.uniform }.toSet()
                            val safeCloudDisabled = layer.uniforms["u_CloudAlpha"]?.value?.toString() == "0.0" ||
                                layer.uniforms["u_CloudAlpha"]?.value?.toString() == "0"
                            val allowedMissing = if (safeCloudDisabled) setOf("u_CloudTexture") else emptySet()
                            val missing = declaredSamplers - boundSamplers - allowedMissing
                            val unknown = boundSamplers - declaredSamplers

                            if (missing.isNotEmpty()) {
                                errors.add("${item.id}:${layer.id}: missing sampler bindings ${missing.sorted()}")
                            }
                            if (unknown.isNotEmpty()) {
                                errors.add("${item.id}:${layer.id}: texture uniforms not declared by shader ${unknown.sorted()}")
                            }
                        }
                }
            }
        }

        assertTrue(errors.joinToString(separator = "\n"), errors.isEmpty())
    }

    private fun validateDefinition(
        expectedId: String,
        definitionFile: File,
        errors: MutableList<String>
    ) {
        when (val parseResult = parser.parseWallpaper(definitionFile.readText())) {
            is WallpaperParseResult.Error -> errors.add("$expectedId: ${parseResult.message}")
            is WallpaperParseResult.Success -> {
                val definition = parseResult.definition
                if (definition.id != expectedId) {
                    errors.add("$expectedId: definition id mismatch ${definition.id}")
                }
                when (val validation = validator.validate(definition) { path ->
                    File(assetsDir, path).isFile
                }) {
                    is ValidationResult.Valid -> Unit
                    is ValidationResult.Invalid -> validation.errors.forEach { error ->
                        errors.add("$expectedId: $error")
                    }
                }
            }
        }
    }
}
