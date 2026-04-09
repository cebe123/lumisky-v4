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
					LegacyWallpaperCatalogSource(),
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
	): WallpaperConfig {
		return repository(context).configById(id = id, daylight = daylight)
	}
}

internal class WallpaperCatalogRepository(
	private val sources: List<WallpaperCatalogSource>
) {
	@Volatile
	private var cachedEntries: List<WallpaperCatalogEntry>? = null

	fun buildConfigs(
		daylight: SunDaylight = SunDaylight.fallback()
	): List<WallpaperConfig> {
		return loadEntries().map { entry -> entry.resolve(daylight) }
	}

	fun configById(
		id: String,
		daylight: SunDaylight = SunDaylight.fallback()
	): WallpaperConfig {
		val entry = loadEntries().firstOrNull { candidate -> candidate.id == id }
		return entry?.resolve(daylight) ?: WallpaperConfig.default(id = id).copy(
			daylight = com.example.engine.config.DaylightConfig(
				sunriseMinute = daylight.sunriseMinute,
				sunsetMinute = daylight.sunsetMinute,
				solarNoonMinute = daylight.solarNoonMinute,
				timeZoneId = daylight.timeZoneId
			)
		)
	}

	private fun loadEntries(): List<WallpaperCatalogEntry> {
		cachedEntries?.let { return it }
		return synchronized(this) {
			cachedEntries ?: mergeEntries().also { merged ->
				cachedEntries = merged
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
}
