package com.example.lumisky.data

import com.example.core.api.SunDaylight
import com.example.engine.config.CelestialConfig
import com.example.engine.config.DaylightConfig
import com.example.engine.config.HorizonConfig
import com.example.engine.config.PathType
import com.example.engine.config.ShaderProfile
import com.example.engine.config.SkyFeatureFlags
import com.example.engine.config.WallpaperConfig
import com.example.engine.config.WallpaperTextures

object WallpaperCatalog {

	fun buildConfigs(
		count: Int = DEFAULT_COUNT,
		daylight: SunDaylight = SunDaylight.fallback()
	): List<WallpaperConfig> {
		if (count <= 0) return emptyList()

		val ordered = ORDERED_PRESETS.map { preset ->
			preset.toConfig(daylight = daylight)
		}
		if (count <= ordered.size) {
			return ordered.take(count)
		}
		return ordered
	}

	fun configById(
		id: String,
		daylight: SunDaylight = SunDaylight.fallback()
	): WallpaperConfig {
		val all = buildConfigs(count = DEFAULT_COUNT, daylight = daylight)
		return all.firstOrNull { it.id == id }
			?: all.first().copy(id = id)
	}

	private fun ThemePreset.toConfig(daylight: SunDaylight): WallpaperConfig {
		return WallpaperConfig(
			id = id,
			name = displayName,
			horizon = HorizonConfig(offset = horizonOffset),
			celestial = CelestialConfig(
				sunPathType = sunPath,
				moonPathType = moonPath
			),
			features = features,
			textures = textures,
			daylight = DaylightConfig(
				sunriseMinute = daylight.sunriseMinute,
				sunsetMinute = daylight.sunsetMinute
			),
			previewLoopDurationSeconds = previewLoopDurationSec,
			focusCatchUpDurationSeconds = focusCatchUpDurationSec,
			peakY = peakY,
			belowHorizonOffset = belowHorizonOffset,
			shader = ShaderProfile(
				fragmentAssetPath = fragmentAssetPath,
				mode = "external_theme"
			)
		)
	}

	private data class ThemePreset(
		val id: String,
		val displayName: String,
		val fragmentAssetPath: String,
		val horizonOffset: Float,
		val peakY: Float = 0.9f,
		val belowHorizonOffset: Float = 0.1f,
		val previewLoopDurationSec: Float = 8f,
		val focusCatchUpDurationSec: Float = 2f,
		val sunPath: PathType = PathType.VERTICAL,
		val moonPath: PathType = PathType.VERTICAL,
		val features: SkyFeatureFlags = SkyFeatureFlags(
			atmosphereEnabled = true,
			lensFlareEnabled = true,
			starsEnabled = true
		),
		val textures: WallpaperTextures = WallpaperTextures()
	)

	// ThemeRepository order from D:\LiveWallpaper-v4
	private val ORDERED_PRESETS = listOf(
		ThemePreset(
				id = "pixel_forest",
				displayName = "Pixel Forest",
				fragmentAssetPath = "shaders/pixelforest/fragment.glsl",
				horizonOffset = 0.45f,
				sunPath = PathType.VERTICAL,
				moonPath = PathType.VERTICAL,
				features = SkyFeatureFlags(
				atmosphereEnabled = true,
				lensFlareEnabled = false,
				starsEnabled = true
			),
			textures = WallpaperTextures(
				backgroundTexture = "pixelforest/bg_forest_16px.webp",
				sunTexture = "common/sun_16px.webp",
				moonTexture = "common/moon_16px.webp"
			)
		),
		ThemePreset(
				id = "classic_sun",
				displayName = "Lighthouse",
				fragmentAssetPath = "shaders/lighthouse/fragment.glsl",
				horizonOffset = 0.25f,
				textures = WallpaperTextures(
					backgroundTexture = "lighthouse/lighthouse.webp",
					sunTexture = "common/sun_only.webp",
				moonTexture = "common/moon_only.webp"
			)
		),
		ThemePreset(
			id = "solar_horizon",
			displayName = "Solar Horizon",
			fragmentAssetPath = "shaders/solar/fragment_shader.glsl",
			horizonOffset = 0.50f
		),
		ThemePreset(
			id = "optical_sunset",
			displayName = "Çöl",
			fragmentAssetPath = "shaders/opticalsunset/fragment.glsl",
			horizonOffset = 0.54f,
			textures = WallpaperTextures(
				backgroundTexture = "opticalsunset/desert.webp"
			)
		),
		ThemePreset(
			id = "mars",
			displayName = "Kızıl Gezegen",
			fragmentAssetPath = "shaders/mars/fragment.glsl",
			horizonOffset = 0.50f,
			sunPath = PathType.VERTICAL,
			moonPath = PathType.VERTICAL,
			features = SkyFeatureFlags(
				atmosphereEnabled = true,
				lensFlareEnabled = false,
				starsEnabled = false
			),
			textures = WallpaperTextures(
				backgroundTexture = "mars/mars.webp"
			)
		),
			ThemePreset(
				id = "warrior",
				displayName = "Savaşçı",
				fragmentAssetPath = "shaders/warrior/fragment.glsl",
				horizonOffset = 0.42f,
			features = SkyFeatureFlags(
				atmosphereEnabled = true,
				lensFlareEnabled = true,
				starsEnabled = false
			),
				textures = WallpaperTextures(
					backgroundTexture = "warrior/warrior1.webp",
					flareTexture = "warrior/warrior2.webp"
				)
			),
		ThemePreset(
				id = "tablo",
				displayName = "Tablo",
				fragmentAssetPath = "shaders/tablo/fragment.glsl",
				horizonOffset = 0.25f,
				features = SkyFeatureFlags(
					atmosphereEnabled = true,
					lensFlareEnabled = false,
				starsEnabled = false
			),
			textures = WallpaperTextures(
				backgroundTexture = "tablo/tablo.webp",
				sunTexture = "tablo/tablo_sun.webp",
				moonTexture = "tablo/tablo_moon.webp"
			)
		),
		ThemePreset(
			id = "city_istanbul",
			displayName = "İstanbul",
			fragmentAssetPath = "shaders/city/fragment_shader.glsl",
			horizonOffset = 0.48f,
			textures = WallpaperTextures(
				backgroundTexture = "backgrounds/city_istanbul.webp"
			)
		),
		ThemePreset(
			id = "city_newyork",
			displayName = "New York",
			fragmentAssetPath = "shaders/city/fragment_shader.glsl",
			horizonOffset = 0.48f,
			textures = WallpaperTextures(
				backgroundTexture = "backgrounds/city_newyork.webp"
			)
		),
		ThemePreset(
			id = "city_tokyo",
			displayName = "Tokyo",
			fragmentAssetPath = "shaders/city/fragment_shader.glsl",
			horizonOffset = 0.48f,
			textures = WallpaperTextures(
				backgroundTexture = "backgrounds/city_tokyo.webp"
			)
		),
		ThemePreset(
			id = "city_paris",
			displayName = "Paris",
			fragmentAssetPath = "shaders/city/fragment_shader.glsl",
			horizonOffset = 0.48f,
			textures = WallpaperTextures(
				backgroundTexture = "backgrounds/city_paris.webp"
			)
		),
		ThemePreset(
				id = "anime_sakura",
				displayName = "Cherry Blossom",
				fragmentAssetPath = "shaders/tablo/fragment.glsl",
				horizonOffset = 0.25f,
				sunPath = PathType.VERTICAL,
				moonPath = PathType.VERTICAL,
			features = SkyFeatureFlags(
				atmosphereEnabled = true,
				lensFlareEnabled = false,
				starsEnabled = true
			),
			textures = WallpaperTextures(
				backgroundTexture = "anime/anime_sakura.webp",
				sunTexture = "anime/anime_sun.webp",
				moonTexture = "anime/anime_moon.webp"
			)
		)
	)

	private const val DEFAULT_COUNT = 120
}
