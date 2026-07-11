/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - wallpaper_catalog.json metadata index okuma kaynağı.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: wallpaper_catalog.json metadata index okuma kaynağı.
 */
package com.example.lumisky.data

import com.example.lumisky.definition.CatalogDefinition
import com.example.lumisky.definition.WallpaperDefinitionParser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallpaperCatalogDataSource @Inject constructor(
    private val localDataSource: LocalWallpaperDataSource,
    private val parser: WallpaperDefinitionParser
) {
    fun loadCatalog(): CatalogDefinition {
        val json = localDataSource.loadFileFromAssets("wallpapers/index.json")
        return if (json.isNotEmpty()) {
            parser.parseCatalog(json)
        } else {
            CatalogDefinition()
        }
    }
}
