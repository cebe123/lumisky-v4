/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Catalog, definition, entitlement ve install state’i birleştiren ana repository.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Catalog, definition, entitlement ve install state’i birleştiren ana repository.
 */
package com.example.lumisky.data

import com.example.lumisky.definition.CatalogDefinition
import com.example.lumisky.definition.WallpaperDefinition
import com.example.lumisky.definition.WallpaperDefinitionParser
import com.example.lumisky.definition.WallpaperParseResult
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
    private val cacheLock = Any()
    private val definitionCache = object : LinkedHashMap<String, WallpaperDefinition>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, WallpaperDefinition>?): Boolean {
            return size > DEFINITION_CACHE_SIZE
        }
    }
    private val resolvedPathCache = mutableMapOf<String, String>()
    private val missingDefinitionIds = mutableSetOf<String>()

    suspend fun getCatalog(): CatalogDefinition = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        catalogDataSource.loadCatalog()
    }

    suspend fun getDefinition(wallpaperId: String): WallpaperDefinition? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        synchronized(cacheLock) {
            definitionCache[wallpaperId]?.let { return@withContext it }
            if (wallpaperId in missingDefinitionIds) return@withContext null
        }

        val cachedPath = synchronized(cacheLock) { resolvedPathCache[wallpaperId] }
        var path = cachedPath ?: "wallpapers/${wallpaperId}/manifest.json"
        var json = localDataSource.loadFileFromAssets(path)
        if (json.isEmpty() && cachedPath == null) {
            path = "wallpapers/${wallpaperId}.json"
            json = localDataSource.loadFileFromAssets(path)
        }
        if (json.isEmpty()) {
            synchronized(cacheLock) { missingDefinitionIds += wallpaperId }
            return@withContext null
        }

        when (val result = parser.parseWallpaper(json)) {
            is WallpaperParseResult.Success -> result.definition.also { definition ->
                synchronized(cacheLock) {
                    definitionCache[wallpaperId] = definition
                    resolvedPathCache[wallpaperId] = path
                }
            }
            is WallpaperParseResult.Error -> {
                synchronized(cacheLock) { missingDefinitionIds += wallpaperId }
                null
            }
        }
    }

    private companion object {
        const val DEFINITION_CACHE_SIZE = 12
    }
}
