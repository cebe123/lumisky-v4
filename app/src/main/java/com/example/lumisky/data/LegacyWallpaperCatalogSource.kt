package com.example.lumisky.data

import com.example.engine.config.CelestialConfig
import com.example.engine.config.DaylightConfig
import com.example.engine.config.HorizonConfig
import com.example.engine.config.PathType
import com.example.engine.config.RenderPolicy
import com.example.engine.config.RuntimeRenderPolicy
import com.example.engine.config.ServiceRenderPolicy
import com.example.engine.config.ShaderProfile
import com.example.engine.config.SkyFeatureFlags
import com.example.engine.config.WallpaperConfig
import com.example.engine.config.WallpaperEffects
import com.example.engine.config.WallpaperTextures

internal class LegacyWallpaperCatalogSource : WallpaperCatalogSource {

	override fun loadEntries(): List<WallpaperCatalogEntry> {
		return ORDERED_PRESETS.map { preset ->
			WallpaperCatalogEntry(
				baseConfig = WallpaperConfig(
					id = preset.id,
					name = preset.displayName,
					horizon = HorizonConfig(offset = preset.horizonOffset),
					celestial = CelestialConfig(
						sunPathType = preset.sunPath,
						moonPathType = preset.moonPath
					),
					features = preset.features,
					effects = WallpaperEffects(),
					textures = preset.textures,
					daylight = DaylightConfig(),
					previewLoopDurationSeconds = preset.previewLoopDurationSec,
					focusCatchUpDurationSeconds = preset.focusCatchUpDurationSec,
					peakY = preset.peakY,
					belowHorizonOffset = preset.belowHorizonOffset,
					shader = ShaderProfile(
						fragmentAssetPath = preset.fragmentAssetPath,
						mode = "external_theme"
					),
					runtimeRenderPolicy = preset.runtimeRenderPolicy,
					capabilities = WallpaperConfig.legacyCapabilities(preset.id),
					serviceRenderPolicy = WallpaperConfig.legacyServiceRenderPolicy(preset.id)
				)
			)
		}
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
		val textures: WallpaperTextures = WallpaperTextures(),
		val runtimeRenderPolicy: RuntimeRenderPolicy = RuntimeRenderPolicy(
			policy = RenderPolicy.MINUTE_TICK,
			continuousFrameIntervalMs = 16L
		)
	)

	private companion object {
		val ORDERED_PRESETS = listOf(
			ThemePreset(
				id = "pixel_forest",
				displayName = "Pixel Forest 4K Live",
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
				displayName = "Lighthouse 3D Scenery",
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
				displayName = "Solar Horizon HD",
				fragmentAssetPath = "shaders/solar/fragment_shader.glsl",
				horizonOffset = 0.50f
			),
			ThemePreset(
				id = "optical_sunset",
				displayName = "Desert Sunset 4K",
				fragmentAssetPath = "shaders/opticalsunset/fragment.glsl",
				horizonOffset = 0.54f,
				textures = WallpaperTextures(
					backgroundTexture = "opticalsunset/desert.webp"
				)
			),
			ThemePreset(
				id = "mars",
				displayName = "Mars Space 3D",
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
				displayName = "Warrior Fantasy HD",
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
				),
				runtimeRenderPolicy = RuntimeRenderPolicy(
					policy = RenderPolicy.CONTINUOUS,
					continuousFrameIntervalMs = 100L
				)
			),
			ThemePreset(
				id = "tablo",
				displayName = "Artistic Canvas Live",
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
				id = "flower",
				displayName = "Spring Flower 3D",
				fragmentAssetPath = "shaders/flower/fragment.glsl",
				horizonOffset = 0.02f,
				peakY = 0.94f,
				features = SkyFeatureFlags(
					atmosphereEnabled = true,
					lensFlareEnabled = false,
					starsEnabled = true
				),
				textures = WallpaperTextures(
					backgroundTexture = "flower/flower.webp",
					moonTexture = "flower/flower_moon.webp"
				),
				runtimeRenderPolicy = RuntimeRenderPolicy(
					policy = RenderPolicy.CONTINUOUS,
					continuousFrameIntervalMs = 50L
				)
			),
			ThemePreset(
				id = "city_lisbon",
				displayName = "Lisbon Cityscape 4K",
				fragmentAssetPath = "shaders/lisbon/fragment.glsl",
				horizonOffset = 0.425f,
				peakY = 0.89f,
				sunPath = PathType.VERTICAL,
				moonPath = PathType.VERTICAL,
				textures = WallpaperTextures(
					backgroundTexture = "backgrounds/city_lisbon.png"
				)
			),
			ThemePreset(
				id = "city_istanbul",
				displayName = "Istanbul Skyline HD",
				fragmentAssetPath = "shaders/city/fragment_shader.glsl",
				horizonOffset = 0.48f,
				textures = WallpaperTextures(
					backgroundTexture = "backgrounds/city_istanbul.webp"
				)
			),
			ThemePreset(
				id = "city_newyork",
				displayName = "New York Cyberpunk 4K",
				fragmentAssetPath = "shaders/city/fragment_shader.glsl",
				horizonOffset = 0.48f,
				textures = WallpaperTextures(
					backgroundTexture = "backgrounds/city_newyork.webp"
				)
			),
			ThemePreset(
				id = "city_tokyo",
				displayName = "Tokyo Night Live",
				fragmentAssetPath = "shaders/city/fragment_shader.glsl",
				horizonOffset = 0.48f,
				textures = WallpaperTextures(
					backgroundTexture = "backgrounds/city_tokyo.webp"
				)
			),
			ThemePreset(
				id = "city_paris",
				displayName = "Paris Romance 3D",
				fragmentAssetPath = "shaders/city/fragment_shader.glsl",
				horizonOffset = 0.48f,
				textures = WallpaperTextures(
					backgroundTexture = "backgrounds/city_paris.webp"
				)
			),
			ThemePreset(
				id = "anime_sakura",
				displayName = "Anime Sakura Live",
				fragmentAssetPath = "shaders/anime_sakura/fragment.glsl",
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
			),
			ThemePreset(
				id = "game_teemo",
				displayName = "Teemo Gaming HD",
				fragmentAssetPath = "shaders/teemo/fragment.glsl",
				horizonOffset = 0.30f,
				peakY = 0.94f,
				features = SkyFeatureFlags(
					atmosphereEnabled = true,
					lensFlareEnabled = false,
					starsEnabled = false
				),
				textures = WallpaperTextures(
					backgroundTexture = "teemo/teemo.webp",
					sunTexture = "teemo/teemo_sun.webp",
					moonTexture = "teemo/teemo_moon.webp"
				)
			)
		)
	}
}
