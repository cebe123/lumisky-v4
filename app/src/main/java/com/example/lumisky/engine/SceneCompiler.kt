/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - WallpaperDefinition’ı validate/normalize edilmiş CompiledSceneDefinition’a çevirir.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: WallpaperDefinition’ı validate/normalize edilmiş CompiledSceneDefinition’a çevirir.
 */
package com.example.lumisky.engine

import com.example.lumisky.definition.DefinitionValidator
import com.example.lumisky.definition.ValidationResult
import com.example.lumisky.definition.WallpaperDefinitionParser
import com.example.lumisky.definition.WallpaperParseResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SceneCompiler @Inject constructor(
    private val parser: WallpaperDefinitionParser,
    private val validator: DefinitionValidator,
    private val hybridRendererBackend: HybridSceneRendererBackend
) {
    fun compile(jsonString: String): CompileResult {
        return when (val parseResult = parser.parseWallpaper(jsonString)) {
            is WallpaperParseResult.Success -> {
                val definition = parseResult.definition
                when (val valResult = validator.validate(definition)) {
                    is ValidationResult.Valid -> {
                        val layerGraph = CompiledLayerGraph.compile(definition)
                        CompileResult.Success(
                            CompiledWallpaperScene(
                                id = definition.id,
                                sourceKind = definition.sourceKind,
                                layerGraph = layerGraph,
                                runtimeScene = hybridRendererBackend.applyCompiledGraph(definition, layerGraph)
                            )
                        )
                    }
                    is ValidationResult.Invalid -> {
                        CompileResult.Error("Validation errors: ${valResult.errors.joinToString()}")
                    }
                }
            }
            is WallpaperParseResult.Error -> {
                CompileResult.Error("Parsing error: ${parseResult.message}")
            }
        }
    }
}

sealed interface CompileResult {
    data class Success(val scene: CompiledWallpaperScene) : CompileResult
    data class Error(val message: String) : CompileResult
}
