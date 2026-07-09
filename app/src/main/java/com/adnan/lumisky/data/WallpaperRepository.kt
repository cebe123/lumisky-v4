/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Catalog, definition, entitlement ve install state’i birleştiren ana repository.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Catalog, definition, entitlement ve install state’i birleştiren ana repository.
 */
package com.adnan.lumisky.data

import com.adnan.lumisky.definition.CatalogDefinition
import com.adnan.lumisky.definition.WallpaperDefinition
import com.adnan.lumisky.definition.WallpaperDefinitionParser
import com.adnan.lumisky.definition.WallpaperParseResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallpaperRepository @Inject constructor(
    private val catalogDataSource: WallpaperCatalogDataSource,
    private val localDataSource: LocalWallpaperDataSource,
    private val parser: WallpaperDefinitionParser,
    val settings: SettingsRepository,
    val entitlement: EntitlementRepository,
    val downloads: AssetDownloadRepository
) {
    suspend fun getCatalog(): CatalogDefinition = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        catalogDataSource.loadCatalog()
    }

    suspend fun getDefinition(wallpaperId: String): WallpaperDefinition? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        var path = "wallpapers/${wallpaperId}/manifest.json"
        var json = localDataSource.loadFileFromAssets(path)
        if (json.isEmpty()) {
            path = "wallpapers/${wallpaperId}.json"
            json = localDataSource.loadFileFromAssets(path)
        }
        if (json.isEmpty()) return@withContext null
        
        return@withContext when (val result = parser.parseWallpaper(json)) {
            is WallpaperParseResult.Success -> result.definition
            is WallpaperParseResult.Error -> null
        }
    }
}
