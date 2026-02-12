package com.example.lumisky.data

import com.example.core.api.SunDaylight
import com.example.engine.config.CelestialConfig
import com.example.engine.config.DaylightConfig
import com.example.engine.config.HorizonConfig
import com.example.engine.config.PathType
import com.example.engine.config.ShaderProfile
import com.example.engine.config.ShaderDefaults
import com.example.engine.config.WallpaperConfig

object WallpaperCatalog {

	fun buildConfigs(
		count: Int = DEFAULT_COUNT,
		daylight: SunDaylight = SunDaylight.fallback()
	): List<WallpaperConfig> {
		return (0 until count).map { index ->
			val id = "wallpaper_$index"
			val horizon = 0.12f + ((index % 9) * 0.04f)
			val sunPath = if (index % 3 == 0) PathType.VERTICAL else PathType.ARC
			val moonPath = if (index % 2 == 0) PathType.ARC else PathType.VERTICAL
			WallpaperConfig(
				id = id,
				name = "Wallpaper ${index + 1}",
				horizon = HorizonConfig(offset = horizon.coerceIn(0.08f, 0.65f)),
				celestial = CelestialConfig(
					sunPathType = sunPath,
					moonPathType = moonPath
				),
				daylight = DaylightConfig(
					sunriseMinute = daylight.sunriseMinute,
					sunsetMinute = daylight.sunsetMinute
				),
				shader = ShaderProfile(
					fragmentAssetPath = DEFAULT_FRAGMENT_ASSET,
					mode = ShaderDefaults.DEFAULT_SHADER_MODE
				)
			)
		}
	}

	fun configById(
		id: String,
		daylight: SunDaylight = SunDaylight.fallback()
	): WallpaperConfig {
		return buildConfigs(count = DEFAULT_COUNT, daylight = daylight)
			.firstOrNull { it.id == id }
			?: WallpaperConfig.default(id = id).copy(
				daylight = DaylightConfig(
					sunriseMinute = daylight.sunriseMinute,
					sunsetMinute = daylight.sunsetMinute
				),
				shader = ShaderProfile(
					fragmentAssetPath = DEFAULT_FRAGMENT_ASSET,
					mode = ShaderDefaults.DEFAULT_SHADER_MODE
				)
			)
	}

	private const val DEFAULT_COUNT = 120
	private const val DEFAULT_FRAGMENT_ASSET = ShaderDefaults.DEFAULT_FRAGMENT_ASSET_PATH
}
