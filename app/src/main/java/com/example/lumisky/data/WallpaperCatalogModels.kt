package com.example.lumisky.data

import com.example.core.api.SunDaylight
import com.example.engine.config.DaylightConfig
import com.example.engine.config.WallpaperConfig

internal data class WallpaperCatalogEntry(
	val baseConfig: WallpaperConfig
) {
	val id: String
		get() = baseConfig.id

	fun resolve(daylight: SunDaylight): WallpaperConfig {
		return baseConfig.copy(
			daylight = DaylightConfig(
				sunriseMinute = daylight.sunriseMinute,
				sunsetMinute = daylight.sunsetMinute,
				solarNoonMinute = daylight.solarNoonMinute,
				timeZoneId = daylight.timeZoneId
			)
		)
	}
}

internal interface WallpaperCatalogSource {
	fun loadEntries(): List<WallpaperCatalogEntry>
}
