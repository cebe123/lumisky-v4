/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - wallpaper_catalog.json için hafif metadata modeli.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: wallpaper_catalog.json için hafif metadata modeli.
 */
package com.example.lumisky.definition

import kotlinx.serialization.Serializable

@Serializable
data class CatalogDefinition(
    val schemaVersion: Int = 5,
    val wallpapers: List<CatalogWallpaperItem> = emptyList()
)

@Serializable
data class CatalogWallpaperItem(
    val id: String,
    val name: String,
    val category: String,
    val thumbnail: String,
    val definitionPath: String,
    val assetPack: String? = null,
    val isPremium: Boolean = false,
    val estimatedCost: EstimatedCost = EstimatedCost()
)

@Serializable
data class EstimatedCost(
    val gpu: String = "low",
    val battery: String = "low",
    val memory: String = "low"
)
