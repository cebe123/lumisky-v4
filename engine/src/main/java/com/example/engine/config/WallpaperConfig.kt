package com.example.engine.config

enum class PathType {
	ARC,
	VERTICAL
}

data class HorizonConfig(
	val offset: Float = 0.2f
)

data class CelestialConfig(
	val sunPathType: PathType = PathType.ARC,
	val moonPathType: PathType = PathType.ARC
)

data class SkyFeatureFlags(
	val atmosphereEnabled: Boolean = true,
	val lensFlareEnabled: Boolean = true,
	val starsEnabled: Boolean = true
)

data class WallpaperTextures(
	val sunTexture: String = "",
	val moonTexture: String = "",
	val flareTexture: String? = null,
	val backgroundTexture: String? = null
)

data class SkyColors(
	val sunriseColor: Int,
	val dayColor: Int,
	val sunsetColor: Int,
	val nightColor: Int
)

data class ShaderProfile(
	val fragmentAssetPath: String? = null,
	val mode: String = "builtin"
)

data class DaylightConfig(
	val sunriseMinute: Int = 6 * 60,
	val sunsetMinute: Int = 18 * 60,
	val solarNoonMinute: Int = 12 * 60,
	val timeZoneId: String? = null
)

enum class RenderPolicy {
	STATIC,
	MINUTE_TICK,
	CONTINUOUS
}

data class RuntimeRenderPolicy(
	val policy: RenderPolicy = RenderPolicy.MINUTE_TICK,
	val continuousFrameIntervalMs: Long = 16L
)

data class WallpaperConfig(
	val id: String,
	val name: String,
	val horizon: HorizonConfig = HorizonConfig(),
	val celestial: CelestialConfig = CelestialConfig(),
	val features: SkyFeatureFlags = SkyFeatureFlags(),
	val textures: WallpaperTextures = WallpaperTextures(),
	val customSkyColors: SkyColors? = null,
	val previewLoopDurationSeconds: Float = 8f,
	val focusCatchUpDurationSeconds: Float = 2f,
	val daylight: DaylightConfig = DaylightConfig(),
	val peakY: Float = 0.9f,
	val belowHorizonOffset: Float = 0.1f,
	val shader: ShaderProfile = ShaderProfile(),
	val runtimeRenderPolicy: RuntimeRenderPolicy = RuntimeRenderPolicy()
) {
	companion object {
		fun default(id: String = "default"): WallpaperConfig {
			return WallpaperConfig(
				id = id,
				name = "Default Sky",
				runtimeRenderPolicy = legacyRuntimeRenderPolicy(id)
			)
		}

		fun legacyRuntimeRenderPolicy(configId: String): RuntimeRenderPolicy {
			val normalized = configId.lowercase()
			return if (normalized.contains("warrior")) {
				RuntimeRenderPolicy(
					policy = RenderPolicy.CONTINUOUS,
					continuousFrameIntervalMs = 100L
				)
			} else {
				RuntimeRenderPolicy(policy = RenderPolicy.MINUTE_TICK, continuousFrameIntervalMs = 16L)
			}
		}
	}
}
