package com.example.engine.config

enum class PathType {
	ARC,
	VERTICAL
}

enum class OrbitCurve {
	LINEAR,
	EASE_IN_OUT
}

data class HorizonConfig(
	val offset: Float = 0.2f
)

data class CelestialOrbitConfig(
	val pathType: PathType = PathType.ARC,
	val startX: Float? = null,
	val endX: Float? = null,
	val peakY: Float? = null,
	val hiddenY: Float? = null,
	val curve: OrbitCurve = OrbitCurve.LINEAR
)

data class CelestialConfig(
	val sunPathType: PathType = PathType.VERTICAL,
	val moonPathType: PathType = PathType.VERTICAL,
	val sunOrbit: CelestialOrbitConfig? = null,
	val moonOrbit: CelestialOrbitConfig? = null
)

data class SkyFeatureFlags(
	val atmosphereEnabled: Boolean = true,
	val lensFlareEnabled: Boolean = true,
	val starsEnabled: Boolean = true
)

data class EffectConfig(
	val enabled: Boolean = true,
	val speed: Float = 1f,
	val density: Float = 1f,
	val intensity: Float = 1f
)

data class WallpaperEffects(
	val clouds: EffectConfig? = null,
	val stars: EffectConfig? = null,
	val fog: EffectConfig? = null,
	val rain: EffectConfig? = null,
	val snow: EffectConfig? = null
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

data class ShaderUniformOverrides(
	val cityZoom: Float? = null,
	val cityVerticalOffset: Float? = null,
	val cityHorizontalOffset: Float? = null,
	val cloudAlpha: Float? = null,
	val cloudOffset: Float? = null
)

data class ShaderProfile(
	val fragmentAssetPath: String? = null,
	val mode: String = "builtin",
	val uniformOverrides: ShaderUniformOverrides = ShaderUniformOverrides()
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

data class WallpaperCapabilities(
	val dynamicMotion: Boolean = false,
	val dynamicTextures: Boolean = false,
	val locationAwareLighting: Boolean = true,
	val supportsCloudLayer: Boolean = false,
	val supportsStarLayer: Boolean = true
)

data class ServiceRenderPolicy(
	val overridePolicy: RenderPolicy? = null,
	val overrideFrameIntervalMs: Long? = null,
	val usePowerSaverThrottle: Boolean = true,
	val useThermalThrottle: Boolean = true,
	val powerSaverPolicy: RenderPolicy? = null,
	val powerSaverFrameIntervalMs: Long? = null,
	val thermalThrottleFrameIntervalMs: Long? = null
)

data class WallpaperConfig(
	val id: String,
	val name: String,
	val horizon: HorizonConfig = HorizonConfig(),
	val celestial: CelestialConfig = CelestialConfig(),
	val features: SkyFeatureFlags = SkyFeatureFlags(),
	val effects: WallpaperEffects = WallpaperEffects(),
	val textures: WallpaperTextures = WallpaperTextures(),
	val customSkyColors: SkyColors? = null,
	val previewLoopDurationSeconds: Float = 8f,
	val focusCatchUpDurationSeconds: Float = 2f,
	val daylight: DaylightConfig = DaylightConfig(),
	val peakY: Float = 0.9f,
	val belowHorizonOffset: Float = 0.1f,
	val shader: ShaderProfile = ShaderProfile(),
	val runtimeRenderPolicy: RuntimeRenderPolicy = RuntimeRenderPolicy(),
	val capabilities: WallpaperCapabilities = WallpaperCapabilities(),
	val serviceRenderPolicy: ServiceRenderPolicy = ServiceRenderPolicy()
) {
	companion object {
		fun default(id: String = "default"): WallpaperConfig {
			return WallpaperConfig(
				id = id,
				name = "Default Sky",
				runtimeRenderPolicy = legacyRuntimeRenderPolicy(id),
				capabilities = legacyCapabilities(id),
				serviceRenderPolicy = legacyServiceRenderPolicy(id)
			)
		}

		fun legacyRuntimeRenderPolicy(configId: String): RuntimeRenderPolicy {
			val normalized = configId.lowercase()
			return when {
				normalized.contains("warrior") -> RuntimeRenderPolicy(
					policy = RenderPolicy.CONTINUOUS,
					continuousFrameIntervalMs = 100L
				)
				normalized.contains("flower") -> RuntimeRenderPolicy(
					policy = RenderPolicy.CONTINUOUS,
					continuousFrameIntervalMs = 50L
				)
				else -> RuntimeRenderPolicy(
					policy = RenderPolicy.MINUTE_TICK,
					continuousFrameIntervalMs = 16L
				)
			}
		}

		fun legacyCapabilities(configId: String): WallpaperCapabilities {
			val normalized = configId.lowercase()
			val isDynamic = normalized.contains("warrior") || normalized.contains("flower")
			return WallpaperCapabilities(
				dynamicMotion = isDynamic,
				dynamicTextures = isDynamic,
				locationAwareLighting = true,
				supportsCloudLayer = false,
				supportsStarLayer = true
			)
		}

		fun legacyServiceRenderPolicy(configId: String): ServiceRenderPolicy {
			val runtimePolicy = legacyRuntimeRenderPolicy(configId)
			return if (runtimePolicy.policy == RenderPolicy.CONTINUOUS) {
				ServiceRenderPolicy(
					overridePolicy = runtimePolicy.policy,
					overrideFrameIntervalMs = runtimePolicy.continuousFrameIntervalMs,
					powerSaverFrameIntervalMs = (runtimePolicy.continuousFrameIntervalMs * 2L)
						.coerceAtLeast(runtimePolicy.continuousFrameIntervalMs),
					thermalThrottleFrameIntervalMs = (runtimePolicy.continuousFrameIntervalMs * 3L)
						.coerceAtLeast(runtimePolicy.continuousFrameIntervalMs)
				)
			} else {
				ServiceRenderPolicy()
			}
		}
	}
}

fun WallpaperConfig.resolveSunOrbit(): CelestialOrbitConfig {
	return celestial.sunOrbit ?: CelestialOrbitConfig(
		pathType = celestial.sunPathType,
		peakY = peakY
	)
}
fun WallpaperConfig.resolveMoonOrbit(): CelestialOrbitConfig {
	return celestial.moonOrbit ?: CelestialOrbitConfig(
		pathType = celestial.moonPathType,
		peakY = peakY
	)
}

fun WallpaperConfig.resolvePeakYForAtmosphere(): Float {
	return maxOf(
		peakY,
		resolveSunOrbit().peakY ?: peakY,
		resolveMoonOrbit().peakY ?: peakY
	)
}
