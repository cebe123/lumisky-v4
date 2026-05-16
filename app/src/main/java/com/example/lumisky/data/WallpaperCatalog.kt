package com.example.lumisky.data

import android.content.Context
import com.example.core.api.SunDaylight
import com.example.engine.config.WallpaperConfig

object WallpaperCatalog {
	@Volatile
	private var repository: WallpaperCatalogRepository? = null

	internal fun repository(context: Context): WallpaperCatalogRepository {
		repository?.let { return it }
		return synchronized(this) {
			repository ?: WallpaperCatalogRepository(
				sources = listOf(
					WallpaperManifestCatalogSource(context.applicationContext)
				)
			).also { created ->
				repository = created
			}
		}
	}

	fun buildConfigs(
		context: Context,
		daylight: SunDaylight = SunDaylight.fallback()
	): List<WallpaperConfig> {
		return repository(context).buildConfigs(daylight)
	}

	fun configById(
		context: Context,
		id: String,
		daylight: SunDaylight = SunDaylight.fallback()
	): WallpaperConfig? {
		return repository(context).configById(id = id, daylight = daylight)
	}
}

internal class WallpaperCatalogRepository(
	private val sources: List<WallpaperCatalogSource>
) {
	@Volatile
	private var cachedEntries: List<WallpaperCatalogEntry>? = null
	@Volatile
	private var cachedEntriesById: Map<String, WallpaperCatalogEntry>? = null
	private val configCache = object : LinkedHashMap<ConfigCacheKey, WallpaperConfig>(
		MAX_CONFIG_CACHE_ENTRIES,
		0.75f,
		true
	) {
		override fun removeEldestEntry(
			eldest: MutableMap.MutableEntry<ConfigCacheKey, WallpaperConfig>?
		): Boolean {
			return size > MAX_CONFIG_CACHE_ENTRIES
		}
	}
	private var configCacheDaylightHash: Int? = null

	fun buildConfigs(
		daylight: SunDaylight = SunDaylight.fallback()
	): List<WallpaperConfig> {
		resetConfigCacheIfDaylightChanged(daylight.hashCode())
		return loadEntries().map { entry -> entry.resolve(daylight) }
	}

	fun configById(
		id: String,
		daylight: SunDaylight = SunDaylight.fallback()
	): WallpaperConfig? {
		val daylightHash = daylight.hashCode()
		val cacheKey = ConfigCacheKey(id = id, daylightHash = daylightHash)
		synchronized(configCache) {
			resetConfigCacheIfDaylightChangedLocked(daylightHash)
			configCache[cacheKey]?.let { return it }
		}
		val entry = loadEntriesById()[id] ?: return null
		val config = entry.resolve(daylight)
		synchronized(configCache) {
			resetConfigCacheIfDaylightChangedLocked(daylightHash)
			configCache[cacheKey] = config
		}
		return config
	}

	private fun loadEntries(): List<WallpaperCatalogEntry> {
		cachedEntries?.let { return it }
		return synchronized(this) {
			cachedEntries ?: mergeEntries().also { merged ->
				cachedEntries = merged
				cachedEntriesById = merged.associateBy { entry -> entry.id }
			}
		}
	}

	private fun loadEntriesById(): Map<String, WallpaperCatalogEntry> {
		cachedEntriesById?.let { return it }
		val entries = loadEntries()
		return synchronized(this) {
			cachedEntriesById ?: entries.associateBy { entry -> entry.id }.also { indexed ->
				cachedEntriesById = indexed
			}
		}
	}

	private fun mergeEntries(): List<WallpaperCatalogEntry> {
		val merged = LinkedHashMap<String, WallpaperCatalogEntry>()
		sources.forEach { source ->
			source.loadEntries().forEach { entry ->
				merged[entry.id] = entry
			}
		}
		return merged.values.toList()
	}

	private fun resetConfigCacheIfDaylightChanged(daylightHash: Int) {
		synchronized(configCache) {
			resetConfigCacheIfDaylightChangedLocked(daylightHash)
		}
	}

	private fun resetConfigCacheIfDaylightChangedLocked(daylightHash: Int) {
		if (configCacheDaylightHash == daylightHash) return
		configCache.clear()
		configCacheDaylightHash = daylightHash
	}

	private data class ConfigCacheKey(
		val id: String,
		val daylightHash: Int
	)

	private companion object {
		private const val MAX_CONFIG_CACHE_ENTRIES = 128
	}
}
