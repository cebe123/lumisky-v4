/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Raw JSON’u WallpaperDefinition modeline parse eder.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Raw JSON’u WallpaperDefinition modeline parse eder.
 */
package com.adnan.lumisky.definition

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallpaperDefinitionParser @Inject constructor(
    private val schemaMigrator: SchemaMigrator
) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        prettyPrint = true
    }

    fun parseCatalog(jsonString: String): CatalogDefinition {
        return json.decodeFromString(CatalogDefinition.serializer(), jsonString)
    }

    fun parseWallpaper(jsonString: String): WallpaperParseResult {
        return try {
            val rawElement = json.parseToJsonElement(jsonString)
            if (rawElement !is JsonObject) {
                return WallpaperParseResult.Error("JSON root is not an object")
            }
            when (val migrationResult = schemaMigrator.migrateToLatest(rawElement)) {
                is SchemaMigrationResult.Migrated -> {
                    val definition = json.decodeFromJsonElement<WallpaperDefinition>(migrationResult.json)
                    WallpaperParseResult.Success(definition)
                }
                is SchemaMigrationResult.UnsupportedVersion -> {
                    WallpaperParseResult.Error("Unsupported schema version: ${migrationResult.fromVersion}")
                }
                is SchemaMigrationResult.InvalidJson -> {
                    WallpaperParseResult.Error("Invalid json format: ${migrationResult.reason}")
                }
            }
        } catch (e: Exception) {
            WallpaperParseResult.Error("Parsing error: ${e.message}")
        }
    }
}

sealed interface WallpaperParseResult {
    data class Success(val definition: WallpaperDefinition) : WallpaperParseResult
    data class Error(val message: String) : WallpaperParseResult
}
