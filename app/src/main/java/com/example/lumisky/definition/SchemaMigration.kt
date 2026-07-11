/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - schemaVersion migration arayüzü ve migration result modelleri.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: schemaVersion migration arayüzü ve migration result modelleri.
 */
package com.example.lumisky.definition

import kotlinx.serialization.json.JsonObject

interface WallpaperSchemaMigration {
    val fromVersion: Int
    val toVersion: Int
    fun migrate(rawJson: JsonObject): JsonObject
}

sealed interface SchemaMigrationResult {
    data class Migrated(val json: JsonObject) : SchemaMigrationResult
    data class UnsupportedVersion(val fromVersion: Int) : SchemaMigrationResult
    data class InvalidJson(val reason: String) : SchemaMigrationResult
}
