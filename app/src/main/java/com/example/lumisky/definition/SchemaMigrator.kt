/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Raw JSON’u desteklenen son schemaVersion’a taşır.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Raw JSON’u desteklenen son schemaVersion’a taşır.
 */
package com.example.lumisky.definition

import kotlinx.serialization.json.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SchemaMigrator @Inject constructor() {
    private val latestVersion: Int = 5

    fun migrateToLatest(rawJson: JsonObject): SchemaMigrationResult {
        val version = rawJson["schemaVersion"]?.jsonPrimitive?.intOrNull ?: 1

        if (version > latestVersion) {
            return SchemaMigrationResult.UnsupportedVersion(version)
        }

        var current = rawJson
        if (version < 5) {
            current = migrateOldSchemaToV5(current)
        }

        return SchemaMigrationResult.Migrated(current)
    }

    private fun migrateOldSchemaToV5(oldJson: JsonObject): JsonObject {
        return buildJsonObject {
            put("schemaVersion", JsonPrimitive(5))
            put("id", oldJson["id"] ?: JsonPrimitive("unknown"))
            put("name", oldJson["name"] ?: JsonPrimitive("Unknown"))
            put("category", oldJson["category"] ?: JsonPrimitive("Classic"))
            put("assetPack", oldJson["assetPack"] ?: JsonNull)
            
            val oldCaps = oldJson["capabilities"] as? JsonObject
            put("capabilities", buildJsonObject {
                put("parallax", JsonPrimitive(oldCaps?.get("dynamicMotion")?.jsonPrimitive?.booleanOrNull ?: false))
                put("touch", JsonPrimitive(false))
                put("locationAware", JsonPrimitive(oldCaps?.get("locationAwareLighting")?.jsonPrimitive?.booleanOrNull ?: false))
                put("supportsPreviewTimeSimulation", JsonPrimitive(true))
            })

            val layersList = buildJsonArray {
                val oldShader = oldJson["shader"] as? JsonObject
                val fragmentPath = oldShader?.get("fragmentAssetPath")?.jsonPrimitive?.content
                
                if (!fragmentPath.isNullOrBlank()) {
                    add(buildJsonObject {
                        put("id", JsonPrimitive("main_shader"))
                        put("type", JsonPrimitive("ShaderLayer"))
                        put("zIndex", JsonPrimitive(0))
                        put("renderPass", JsonPrimitive("BACKGROUND"))
                        put("blendMode", JsonPrimitive("NONE"))
                        put("shaderRef", JsonPrimitive(fragmentPath))
                        
                        val oldTextures = oldJson["textures"] as? JsonObject
                        val textureSlots = buildJsonArray {
                            oldTextures?.forEach { (key, value) ->
                                val texturePath = value.jsonPrimitive.content
                                add(buildJsonObject {
                                    put("uniform", JsonPrimitive(key))
                                    put("path", JsonPrimitive(texturePath))
                                })
                            }
                        }
                        put("textures", textureSlots)
                    })
                }
            }
            put("layers", layersList)
            
            put("preview", buildJsonObject {
                val previewThumbnail = oldJson["preview"]?.jsonObject?.get("thumbnail")
                    ?: JsonPrimitive("wallpapers/${oldJson["id"]?.jsonPrimitive?.contentOrNull ?: "unknown"}/thumbnail.webp")
                put("thumbnail", previewThumbnail)
                put("cardMode", JsonPrimitive("THUMBNAIL"))
                put("fullscreenMode", JsonPrimitive("FAST_TIME_SIMULATION"))
            })
        }
    }
}
