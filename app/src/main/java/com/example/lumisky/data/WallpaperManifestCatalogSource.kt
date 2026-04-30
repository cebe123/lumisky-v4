package com.example.lumisky.data

import android.content.Context
import com.example.core.Logger
import com.example.engine.config.CelestialConfig
import com.example.engine.config.CelestialOrbitConfig
import com.example.engine.config.DaylightConfig
import com.example.engine.config.EffectConfig
import com.example.engine.config.HorizonConfig
import com.example.engine.config.OrbitCurve
import com.example.engine.config.PathType
import com.example.engine.config.RenderPolicy
import com.example.engine.config.RuntimeRenderPolicy
import com.example.engine.config.ServiceRenderPolicy
import com.example.engine.config.ShaderProfile
import com.example.engine.config.ShaderUniformOverrides
import com.example.engine.config.SkyFeatureFlags
import com.example.engine.config.WallpaperCapabilities
import com.example.engine.config.WallpaperConfig
import com.example.engine.config.WallpaperEffects
import com.example.engine.config.WallpaperTextures
import org.json.JSONObject

internal class WallpaperManifestCatalogSource(
	private val appContext: Context
) : WallpaperCatalogSource {
	private val parser = WallpaperManifestParser()

	override fun loadEntries(): List<WallpaperCatalogEntry> {
		val indexJson = readJson(INDEX_ASSET_PATH) ?: return emptyList()
		val items = indexJson.optJSONArray(KEY_WALLPAPERS) ?: return emptyList()
		val entries = ArrayList<WallpaperCatalogEntry>(items.length())
		for (index in 0 until items.length()) {
			val item = items.optJSONObject(index) ?: continue
			val fallbackId = item.optString(KEY_ID, "").takeIf { it.isNotBlank() } ?: continue
			val manifestPath = item.optString(KEY_MANIFEST, "").takeIf { it.isNotBlank() }
			if (manifestPath == null) {
				Logger.w(TAG, "manifest index missing path for id=$fallbackId")
				continue
			}
			val manifestJson = readJson(manifestPath) ?: continue
			val entry = parser.parse(
				root = manifestJson,
				fallbackId = fallbackId,
				sourceAssetPath = manifestPath
			) ?: continue
			entries += entry
		}
		return entries
	}

	private fun readJson(assetPath: String): JSONObject? {
		val text = runCatching {
			appContext.assets.open(assetPath).bufferedReader().use { reader ->
				reader.readText()
			}
		}.onFailure { throwable ->
			Logger.w(TAG, "catalog asset load failed path=$assetPath", throwable)
		}.getOrNull() ?: return null
		return runCatching { JSONObject(text) }
			.onFailure { throwable ->
				Logger.w(TAG, "catalog json parse failed path=$assetPath", throwable)
			}
			.getOrNull()
	}

	private companion object {
		const val TAG = "ManifestCatalog"
		const val INDEX_ASSET_PATH = "wallpapers/index.json"
		const val KEY_WALLPAPERS = "wallpapers"
		const val KEY_ID = "id"
		const val KEY_MANIFEST = "manifest"
	}
}

internal class WallpaperManifestParser {
	fun parse(
		root: JSONObject,
		fallbackId: String,
		sourceAssetPath: String
	): WallpaperCatalogEntry? {
		return runCatching {
			parseOrThrow(
				root = root,
				fallbackId = fallbackId,
				sourceAssetPath = sourceAssetPath
			)
		}.getOrNull()
	}

	internal fun parseOrThrow(
		root: JSONObject,
		fallbackId: String,
		sourceAssetPath: String
	): WallpaperCatalogEntry {
		return runCatching {
			val id = root.optString(KEY_ID, fallbackId).ifBlank { fallbackId }
			val defaults = WallpaperConfig.default(id)
			val name = root.optString(KEY_NAME, defaults.name).ifBlank { defaults.name }
			val texturesJson = root.optJSONObject(KEY_TEXTURES)
			val shaderJson = root.optJSONObject(KEY_SHADER)
			val forceLinearOrbitCurve = !isLighthouseWallpaper(
				id = id,
				sourceAssetPath = sourceAssetPath,
				texturesJson = texturesJson,
				shaderJson = shaderJson
			)

			val horizon = root.optJSONObject(KEY_HORIZON)
			val horizonOffset = horizon?.optDouble(KEY_OFFSET, defaults.horizon.offset.toDouble())
				?.toFloat()
				?: defaults.horizon.offset

			val celestial = root.optJSONObject(KEY_CELESTIAL)
			val sunPathType = parsePathType(
				raw = celestial?.optString(KEY_SUN_PATH_TYPE),
				fallback = defaults.celestial.sunPathType
			)
			val moonPathType = parsePathType(
				raw = celestial?.optString(KEY_MOON_PATH_TYPE),
				fallback = defaults.celestial.moonPathType
			)
			val sunOrbit = decodeOrbit(
				json = celestial?.optJSONObject(KEY_SUN_ORBIT),
				fallbackPathType = sunPathType,
				forceLinearCurve = forceLinearOrbitCurve
			)
			val moonOrbit = decodeOrbit(
				json = celestial?.optJSONObject(KEY_MOON_ORBIT),
				fallbackPathType = moonPathType,
				forceLinearCurve = forceLinearOrbitCurve
			)

			val features = root.optJSONObject(KEY_FEATURES)
			val featureFlags = SkyFeatureFlags(
				atmosphereEnabled = features?.optBoolean(
					KEY_ATMOSPHERE_ENABLED,
					defaults.features.atmosphereEnabled
				) ?: defaults.features.atmosphereEnabled,
				lensFlareEnabled = features?.optBoolean(
					KEY_LENS_FLARE_ENABLED,
					defaults.features.lensFlareEnabled
				) ?: defaults.features.lensFlareEnabled,
				starsEnabled = features?.optBoolean(
					KEY_STARS_ENABLED,
					defaults.features.starsEnabled
				) ?: defaults.features.starsEnabled
			)

			val effectsJson = root.optJSONObject(KEY_EFFECTS)
			val effects = WallpaperEffects(
				clouds = decodeEffect(effectsJson?.optJSONObject(KEY_CLOUDS)),
				stars = decodeEffect(effectsJson?.optJSONObject(KEY_STARS)),
				fog = decodeEffect(effectsJson?.optJSONObject(KEY_FOG)),
				rain = decodeEffect(effectsJson?.optJSONObject(KEY_RAIN)),
				snow = decodeEffect(effectsJson?.optJSONObject(KEY_SNOW))
			)

			val textures = WallpaperTextures(
				sunTexture = texturesJson?.optString(KEY_SUN_TEXTURE, defaults.textures.sunTexture)
					?: defaults.textures.sunTexture,
				moonTexture = texturesJson?.optString(KEY_MOON_TEXTURE, defaults.textures.moonTexture)
					?: defaults.textures.moonTexture,
				flareTexture = texturesJson?.optNullableString(KEY_FLARE_TEXTURE),
				backgroundTexture = texturesJson?.optNullableString(KEY_BACKGROUND_TEXTURE)
			)

			val shader = ShaderProfile(
				fragmentAssetPath = shaderJson?.optNullableString(KEY_FRAGMENT_ASSET_PATH)
					?: defaults.shader.fragmentAssetPath,
				mode = shaderJson?.optString(KEY_MODE, defaults.shader.mode)?.ifBlank {
					defaults.shader.mode
				} ?: defaults.shader.mode,
				uniformOverrides = decodeUniformOverrides(shaderJson?.optJSONObject(KEY_UNIFORM_OVERRIDES))
			)

			val runtimeRenderPolicy = decodeRuntimeRenderPolicy(
				json = root.optJSONObject(KEY_RUNTIME_RENDER_POLICY),
				fallback = defaults.runtimeRenderPolicy
			)
			val capabilities = decodeCapabilities(
				json = root.optJSONObject(KEY_CAPABILITIES),
				fallback = defaults.capabilities
			)
			val serviceRenderPolicy = decodeServiceRenderPolicy(
				json = root.optJSONObject(KEY_SERVICE_RENDER_POLICY),
				fallback = defaults.serviceRenderPolicy
			)

			val previewLoopDurationSeconds = root.optDouble(
				KEY_PREVIEW_LOOP_DURATION_SECONDS,
				defaults.previewLoopDurationSeconds.toDouble()
			).toFloat()
			val focusCatchUpDurationSeconds = root.optDouble(
				KEY_FOCUS_CATCH_UP_DURATION_SECONDS,
				defaults.focusCatchUpDurationSeconds.toDouble()
			).toFloat()
			val peakY = root.optDouble(KEY_PEAK_Y, defaults.peakY.toDouble()).toFloat()
			val belowHorizonOffset = root.optDouble(
				KEY_BELOW_HORIZON_OFFSET,
				defaults.belowHorizonOffset.toDouble()
			).toFloat()

			WallpaperCatalogEntry(
				baseConfig = WallpaperConfig(
					id = id,
					name = name,
					horizon = HorizonConfig(offset = horizonOffset),
					celestial = CelestialConfig(
						sunPathType = sunPathType,
						moonPathType = moonPathType,
						sunOrbit = sunOrbit,
						moonOrbit = moonOrbit
					),
					features = featureFlags,
					effects = effects,
					textures = textures,
					daylight = DaylightConfig(),
					previewLoopDurationSeconds = previewLoopDurationSeconds,
					focusCatchUpDurationSeconds = focusCatchUpDurationSeconds,
					peakY = peakY,
					belowHorizonOffset = belowHorizonOffset,
					shader = shader,
					runtimeRenderPolicy = runtimeRenderPolicy,
					capabilities = capabilities,
					serviceRenderPolicy = serviceRenderPolicy
				)
			)
		}.getOrThrow()
	}

	private fun isLighthouseWallpaper(
		id: String,
		sourceAssetPath: String,
		texturesJson: JSONObject?,
		shaderJson: JSONObject?
	): Boolean {
		return listOfNotNull(
			id,
			sourceAssetPath,
			texturesJson?.optString(KEY_BACKGROUND_TEXTURE),
			shaderJson?.optString(KEY_FRAGMENT_ASSET_PATH)
		).any { value ->
			value.contains("lighthouse", ignoreCase = true)
		}
	}

	private fun decodeOrbit(
		json: JSONObject?,
		fallbackPathType: PathType,
		forceLinearCurve: Boolean
	): CelestialOrbitConfig? {
		json ?: return null
		return CelestialOrbitConfig(
			pathType = parsePathType(
				raw = json.optString(KEY_PATH_TYPE, fallbackPathType.name),
				fallback = fallbackPathType
			),
			startX = json.optNullableFloat(KEY_START_X),
			endX = json.optNullableFloat(KEY_END_X),
			peakY = json.optNullableFloat(KEY_PEAK_Y),
			hiddenY = json.optNullableFloat(KEY_HIDDEN_Y),
			curve = if (forceLinearCurve) {
				OrbitCurve.LINEAR
			} else {
				parseOrbitCurve(
					raw = json.optString(KEY_CURVE, OrbitCurve.LINEAR.name),
					fallback = OrbitCurve.LINEAR
				)
			}
		)
	}

	private fun decodeEffect(json: JSONObject?): EffectConfig? {
		json ?: return null
		return EffectConfig(
			enabled = json.optBoolean(KEY_ENABLED, true),
			speed = json.optDouble(KEY_SPEED, 1.0).toFloat(),
			density = json.optDouble(KEY_DENSITY, 1.0).toFloat(),
			intensity = json.optDouble(KEY_INTENSITY, 1.0).toFloat()
		)
	}

	private fun decodeUniformOverrides(json: JSONObject?): ShaderUniformOverrides {
		json ?: return ShaderUniformOverrides()
		return ShaderUniformOverrides(
			cityZoom = json.optNullableFloat(KEY_CITY_ZOOM),
			cityVerticalOffset = json.optNullableFloat(KEY_CITY_VERTICAL_OFFSET),
			cityHorizontalOffset = json.optNullableFloat(KEY_CITY_HORIZONTAL_OFFSET),
			cloudAlpha = json.optNullableFloat(KEY_CLOUD_ALPHA),
			cloudOffset = json.optNullableFloat(KEY_CLOUD_OFFSET)
		)
	}

	private fun decodeRuntimeRenderPolicy(
		json: JSONObject?,
		fallback: RuntimeRenderPolicy
	): RuntimeRenderPolicy {
		json ?: return fallback
		return RuntimeRenderPolicy(
			policy = parseRenderPolicy(
				raw = json.optString(KEY_POLICY, fallback.policy.name),
				fallback = fallback.policy
			) ?: fallback.policy,
			continuousFrameIntervalMs = json.optLong(
				KEY_CONTINUOUS_FRAME_INTERVAL_MS,
				fallback.continuousFrameIntervalMs
			).coerceAtLeast(1L)
		)
	}

	private fun decodeCapabilities(
		json: JSONObject?,
		fallback: WallpaperCapabilities
	): WallpaperCapabilities {
		json ?: return fallback
		return WallpaperCapabilities(
			dynamicMotion = json.optBoolean(KEY_DYNAMIC_MOTION, fallback.dynamicMotion),
			dynamicTextures = json.optBoolean(KEY_DYNAMIC_TEXTURES, fallback.dynamicTextures),
			locationAwareLighting = json.optBoolean(
				KEY_LOCATION_AWARE_LIGHTING,
				fallback.locationAwareLighting
			),
			supportsCloudLayer = json.optBoolean(
				KEY_SUPPORTS_CLOUD_LAYER,
				fallback.supportsCloudLayer
			),
			supportsStarLayer = json.optBoolean(
				KEY_SUPPORTS_STAR_LAYER,
				fallback.supportsStarLayer
			)
		)
	}

	private fun decodeServiceRenderPolicy(
		json: JSONObject?,
		fallback: ServiceRenderPolicy
	): ServiceRenderPolicy {
		json ?: return fallback
		return ServiceRenderPolicy(
			overridePolicy = parseRenderPolicy(
				raw = json.optNullableString(KEY_OVERRIDE_POLICY),
				fallback = fallback.overridePolicy
			),
			overrideFrameIntervalMs = json.optNullableLong(KEY_OVERRIDE_FRAME_INTERVAL_MS)
				?.coerceAtLeast(1L)
				?: fallback.overrideFrameIntervalMs,
			usePowerSaverThrottle = json.optBoolean(
				KEY_USE_POWER_SAVER_THROTTLE,
				fallback.usePowerSaverThrottle
			),
			useThermalThrottle = json.optBoolean(
				KEY_USE_THERMAL_THROTTLE,
				fallback.useThermalThrottle
			),
			powerSaverPolicy = parseRenderPolicy(
				raw = json.optNullableString(KEY_POWER_SAVER_POLICY),
				fallback = fallback.powerSaverPolicy
			),
			powerSaverFrameIntervalMs = json.optNullableLong(KEY_POWER_SAVER_FRAME_INTERVAL_MS)
				?.coerceAtLeast(1L)
				?: fallback.powerSaverFrameIntervalMs,
			thermalThrottleFrameIntervalMs = json.optNullableLong(
				KEY_THERMAL_THROTTLE_FRAME_INTERVAL_MS
			)?.coerceAtLeast(1L) ?: fallback.thermalThrottleFrameIntervalMs
		)
	}

	private fun parsePathType(
		raw: String?,
		fallback: PathType
	): PathType {
		if (raw.isNullOrBlank()) return fallback
		return runCatching { PathType.valueOf(raw) }.getOrElse { fallback }
	}

	private fun parseOrbitCurve(
		raw: String?,
		fallback: OrbitCurve
	): OrbitCurve {
		if (raw.isNullOrBlank()) return fallback
		return runCatching { OrbitCurve.valueOf(raw) }.getOrElse { fallback }
	}

	private fun parseRenderPolicy(
		raw: String?,
		fallback: RenderPolicy?
	): RenderPolicy? {
		if (raw.isNullOrBlank()) return fallback
		return runCatching { RenderPolicy.valueOf(raw) }.getOrElse { fallback }
	}

	private fun JSONObject.optNullableString(key: String): String? {
		if (!has(key) || isNull(key)) return null
		return optString(key, "").takeIf { it.isNotBlank() }
	}

	private fun JSONObject.optNullableFloat(key: String): Float? {
		if (!has(key) || isNull(key)) return null
		return optDouble(key).toFloat()
	}

	private fun JSONObject.optNullableLong(key: String): Long? {
		if (!has(key) || isNull(key)) return null
		return optLong(key)
	}

	private companion object {
		const val TAG = "ManifestParser"
		const val KEY_ID = "id"
		const val KEY_NAME = "name"
		const val KEY_HORIZON = "horizon"
		const val KEY_OFFSET = "offset"
		const val KEY_CELESTIAL = "celestial"
		const val KEY_SUN_PATH_TYPE = "sunPathType"
		const val KEY_MOON_PATH_TYPE = "moonPathType"
		const val KEY_SUN_ORBIT = "sunOrbit"
		const val KEY_MOON_ORBIT = "moonOrbit"
		const val KEY_PATH_TYPE = "pathType"
		const val KEY_START_X = "startX"
		const val KEY_END_X = "endX"
		const val KEY_PEAK_Y = "peakY"
		const val KEY_HIDDEN_Y = "hiddenY"
		const val KEY_CURVE = "curve"
		const val KEY_FEATURES = "features"
		const val KEY_ATMOSPHERE_ENABLED = "atmosphereEnabled"
		const val KEY_LENS_FLARE_ENABLED = "lensFlareEnabled"
		const val KEY_STARS_ENABLED = "starsEnabled"
		const val KEY_EFFECTS = "effects"
		const val KEY_CLOUDS = "clouds"
		const val KEY_STARS = "stars"
		const val KEY_FOG = "fog"
		const val KEY_RAIN = "rain"
		const val KEY_SNOW = "snow"
		const val KEY_ENABLED = "enabled"
		const val KEY_SPEED = "speed"
		const val KEY_DENSITY = "density"
		const val KEY_INTENSITY = "intensity"
		const val KEY_TEXTURES = "textures"
		const val KEY_SUN_TEXTURE = "sunTexture"
		const val KEY_MOON_TEXTURE = "moonTexture"
		const val KEY_FLARE_TEXTURE = "flareTexture"
		const val KEY_BACKGROUND_TEXTURE = "backgroundTexture"
		const val KEY_SHADER = "shader"
		const val KEY_FRAGMENT_ASSET_PATH = "fragmentAssetPath"
		const val KEY_MODE = "mode"
		const val KEY_UNIFORM_OVERRIDES = "uniformOverrides"
		const val KEY_CITY_ZOOM = "cityZoom"
		const val KEY_CITY_VERTICAL_OFFSET = "cityVerticalOffset"
		const val KEY_CITY_HORIZONTAL_OFFSET = "cityHorizontalOffset"
		const val KEY_CLOUD_ALPHA = "cloudAlpha"
		const val KEY_CLOUD_OFFSET = "cloudOffset"
		const val KEY_RUNTIME_RENDER_POLICY = "runtimeRenderPolicy"
		const val KEY_POLICY = "policy"
		const val KEY_CONTINUOUS_FRAME_INTERVAL_MS = "continuousFrameIntervalMs"
		const val KEY_CAPABILITIES = "capabilities"
		const val KEY_DYNAMIC_MOTION = "dynamicMotion"
		const val KEY_DYNAMIC_TEXTURES = "dynamicTextures"
		const val KEY_LOCATION_AWARE_LIGHTING = "locationAwareLighting"
		const val KEY_SUPPORTS_CLOUD_LAYER = "supportsCloudLayer"
		const val KEY_SUPPORTS_STAR_LAYER = "supportsStarLayer"
		const val KEY_SERVICE_RENDER_POLICY = "serviceRenderPolicy"
		const val KEY_OVERRIDE_POLICY = "overridePolicy"
		const val KEY_OVERRIDE_FRAME_INTERVAL_MS = "overrideFrameIntervalMs"
		const val KEY_USE_POWER_SAVER_THROTTLE = "usePowerSaverThrottle"
		const val KEY_USE_THERMAL_THROTTLE = "useThermalThrottle"
		const val KEY_POWER_SAVER_POLICY = "powerSaverPolicy"
		const val KEY_POWER_SAVER_FRAME_INTERVAL_MS = "powerSaverFrameIntervalMs"
		const val KEY_THERMAL_THROTTLE_FRAME_INTERVAL_MS = "thermalThrottleFrameIntervalMs"
		const val KEY_PREVIEW_LOOP_DURATION_SECONDS = "previewLoopDurationSeconds"
		const val KEY_FOCUS_CATCH_UP_DURATION_SECONDS = "focusCatchUpDurationSeconds"
		const val KEY_BELOW_HORIZON_OFFSET = "belowHorizonOffset"
	}
}
