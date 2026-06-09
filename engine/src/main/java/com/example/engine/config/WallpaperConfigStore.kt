package com.example.engine.config

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class WallpaperConfigStore(
	context: Context
) {
	private val appContext = context.applicationContext
	private val credentialPreferences by lazy {
		runCatching {
			appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
		}.getOrNull()
	}
	private val deviceProtectedPreferences by lazy {
		runCatching {
			appContext.createDeviceProtectedStorageContext()
				.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
		}.getOrNull()
	}

	fun saveSelected(config: WallpaperConfig) {
		val encoded = WallpaperConfigJsonCodec.encode(config)
		writeEncoded(
			preferences = credentialPreferences,
			key = KEY_SELECTED_CONFIG_JSON,
			encoded = encoded
		)
		writeEncoded(
			preferences = deviceProtectedPreferences,
			key = KEY_SELECTED_CONFIG_JSON,
			encoded = encoded
		)
	}

	fun loadSelected(): WallpaperConfig? {
		val deviceEncoded = readEncoded(
			preferences = deviceProtectedPreferences,
			key = KEY_SELECTED_CONFIG_JSON
		)
		if (deviceEncoded != null) {
			WallpaperConfigJsonCodec.decode(deviceEncoded)?.let { return it }
			removeEncoded(
				preferences = deviceProtectedPreferences,
				key = KEY_SELECTED_CONFIG_JSON
			)
		}

		val credentialEncoded = readEncoded(
			preferences = credentialPreferences,
			key = KEY_SELECTED_CONFIG_JSON
		)
		if (credentialEncoded != null) {
			val decoded = WallpaperConfigJsonCodec.decode(credentialEncoded)
			if (decoded != null) {
				writeEncoded(
					preferences = deviceProtectedPreferences,
					key = KEY_SELECTED_CONFIG_JSON,
					encoded = credentialEncoded
				)
				return decoded
			}
			removeEncoded(
				preferences = credentialPreferences,
				key = KEY_SELECTED_CONFIG_JSON
			)
		}
		return null
	}

	fun clearSelected() {
		removeEncoded(
			preferences = credentialPreferences,
			key = KEY_SELECTED_CONFIG_JSON
		)
		removeEncoded(
			preferences = deviceProtectedPreferences,
			key = KEY_SELECTED_CONFIG_JSON
		)
	}

	fun savePreview(config: WallpaperConfig) {
		val encoded = WallpaperConfigJsonCodec.encode(config)
		writeEncoded(
			preferences = credentialPreferences,
			key = KEY_PREVIEW_CONFIG_JSON,
			encoded = encoded
		)
		writeEncoded(
			preferences = deviceProtectedPreferences,
			key = KEY_PREVIEW_CONFIG_JSON,
			encoded = encoded
		)
	}

	fun loadPreview(): WallpaperConfig? {
		val deviceEncoded = readEncoded(
			preferences = deviceProtectedPreferences,
			key = KEY_PREVIEW_CONFIG_JSON
		)
		if (deviceEncoded != null) {
			WallpaperConfigJsonCodec.decode(deviceEncoded)?.let { return it }
			removeEncoded(
				preferences = deviceProtectedPreferences,
				key = KEY_PREVIEW_CONFIG_JSON
			)
		}

		val credentialEncoded = readEncoded(
			preferences = credentialPreferences,
			key = KEY_PREVIEW_CONFIG_JSON
		)
		if (credentialEncoded != null) {
			val decoded = WallpaperConfigJsonCodec.decode(credentialEncoded)
			if (decoded != null) {
				writeEncoded(
					preferences = deviceProtectedPreferences,
					key = KEY_PREVIEW_CONFIG_JSON,
					encoded = credentialEncoded
				)
				return decoded
			}
			removeEncoded(
				preferences = credentialPreferences,
				key = KEY_PREVIEW_CONFIG_JSON
			)
		}
		return null
	}

	fun clearPreview() {
		removeEncoded(
			preferences = credentialPreferences,
			key = KEY_PREVIEW_CONFIG_JSON
		)
		removeEncoded(
			preferences = deviceProtectedPreferences,
			key = KEY_PREVIEW_CONFIG_JSON
		)
	}

	fun promotePreviewToSelected(): WallpaperConfig? {
		val preview = loadPreview() ?: return null
		saveSelected(preview)
		clearPreview()
		return preview
	}

	private fun readEncoded(
		preferences: SharedPreferences?,
		key: String
	): String? {
		if (preferences == null) return null
		return runCatching {
			preferences.getString(key, null)
		}.getOrNull()
	}

	private fun writeEncoded(
		preferences: SharedPreferences?,
		key: String,
		encoded: String
	) {
		if (preferences == null) return
		runCatching {
			val editor = preferences.edit()
				.putString(key, encoded)
			if (!editor.commit()) {
				editor.apply()
			}
		}
	}

	private fun removeEncoded(
		preferences: SharedPreferences?,
		key: String
	) {
		if (preferences == null) return
		runCatching {
			val editor = preferences.edit()
				.remove(key)
			if (!editor.commit()) {
				editor.apply()
			}
		}
	}

	companion object {
		private const val PREFERENCES_NAME = "lumisky_wallpaper_config_store"
		private const val KEY_SELECTED_CONFIG_JSON = "selected_config_json"
		private const val KEY_PREVIEW_CONFIG_JSON = "preview_config_json"
	}
}

internal object WallpaperConfigJsonCodec {
	private const val KEY_ID = "id"
	private const val KEY_NAME = "name"
	private const val KEY_HORIZON = "horizon"
	private const val KEY_OFFSET = "offset"
	private const val KEY_CELESTIAL = "celestial"
	private const val KEY_SUN_PATH_TYPE = "sunPathType"
	private const val KEY_MOON_PATH_TYPE = "moonPathType"
	private const val KEY_SUN_ORBIT = "sunOrbit"
	private const val KEY_MOON_ORBIT = "moonOrbit"
	private const val KEY_START_X = "startX"
	private const val KEY_END_X = "endX"
	private const val KEY_HIDDEN_Y = "hiddenY"
	private const val KEY_CURVE = "curve"
	private const val KEY_FEATURES = "features"
	private const val KEY_ATMOSPHERE_ENABLED = "atmosphereEnabled"
	private const val KEY_LENS_FLARE_ENABLED = "lensFlareEnabled"
	private const val KEY_STARS_ENABLED = "starsEnabled"
	private const val KEY_EFFECTS = "effects"
	private const val KEY_CLOUDS = "clouds"
	private const val KEY_STARS = "stars"
	private const val KEY_FOG = "fog"
	private const val KEY_RAIN = "rain"
	private const val KEY_SNOW = "snow"
	private const val KEY_ENABLED = "enabled"
	private const val KEY_SPEED = "speed"
	private const val KEY_DENSITY = "density"
	private const val KEY_INTENSITY = "intensity"
	private const val KEY_TEXTURES = "textures"
	private const val KEY_SUN_TEXTURE = "sunTexture"
	private const val KEY_MOON_TEXTURE = "moonTexture"
	private const val KEY_FLARE_TEXTURE = "flareTexture"
	private const val KEY_BACKGROUND_TEXTURE = "backgroundTexture"
	private const val KEY_CUSTOM_SKY_COLORS = "customSkyColors"
	private const val KEY_SUNRISE_COLOR = "sunriseColor"
	private const val KEY_DAY_COLOR = "dayColor"
	private const val KEY_SUNSET_COLOR = "sunsetColor"
	private const val KEY_NIGHT_COLOR = "nightColor"
	private const val KEY_PREVIEW_LOOP_DURATION_SECONDS = "previewLoopDurationSeconds"
	private const val KEY_FOCUS_CATCH_UP_DURATION_SECONDS = "focusCatchUpDurationSeconds"
	// Backward compatibility for previously persisted configs.
	private const val KEY_ANIMATION_DURATION_SECONDS = "animationDurationSeconds"
	private const val KEY_DAYLIGHT = "daylight"
	private const val KEY_SUNRISE_MINUTE = "sunriseMinute"
	private const val KEY_SUNSET_MINUTE = "sunsetMinute"
	private const val KEY_SOLAR_NOON_MINUTE = "solarNoonMinute"
	private const val KEY_TIME_ZONE_ID = "timeZoneId"
	private const val KEY_PEAK_Y = "peakY"
	private const val KEY_BELOW_HORIZON_OFFSET = "belowHorizonOffset"
	private const val KEY_SHADER = "shader"
	private const val KEY_FRAGMENT_ASSET_PATH = "fragmentAssetPath"
	private const val KEY_MODE = "mode"
	private const val KEY_UNIFORM_OVERRIDES = "uniformOverrides"
	private const val KEY_CITY_ZOOM = "cityZoom"
	private const val KEY_CITY_VERTICAL_OFFSET = "cityVerticalOffset"
	private const val KEY_CITY_HORIZONTAL_OFFSET = "cityHorizontalOffset"
	private const val KEY_CLOUD_ALPHA = "cloudAlpha"
	private const val KEY_CLOUD_OFFSET = "cloudOffset"
	private const val KEY_RUNTIME_RENDER_POLICY = "runtimeRenderPolicy"
	private const val KEY_POLICY = "policy"
	private const val KEY_CONTINUOUS_FRAME_INTERVAL_MS = "continuousFrameIntervalMs"
	private const val KEY_CAPABILITIES = "capabilities"
	private const val KEY_DYNAMIC_MOTION = "dynamicMotion"
	private const val KEY_DYNAMIC_TEXTURES = "dynamicTextures"
	private const val KEY_LOCATION_AWARE_LIGHTING = "locationAwareLighting"
	private const val KEY_SUPPORTS_CLOUD_LAYER = "supportsCloudLayer"
	private const val KEY_SUPPORTS_STAR_LAYER = "supportsStarLayer"
	private const val KEY_SERVICE_RENDER_POLICY = "serviceRenderPolicy"
	private const val KEY_CREATOR = "creator"
	private const val KEY_SCHEMA_VERSION = "schemaVersion"
	private const val KEY_LAYERS = "layers"
	private const val KEY_TEXTURE_PATH = "texturePath"
	private const val KEY_MEDIA_TYPE = "mediaType"
	private const val KEY_MIME_TYPE = "mimeType"
	private const val KEY_MOTION_TYPE = "motionType"
	private const val KEY_MOTION_SPEED = "motionSpeed"
	private const val KEY_MOTION_AMPLITUDE = "motionAmplitude"
	private const val KEY_MOTION_DIRECTION = "motionDirection"
	private const val KEY_MOTION_DURATION = "motionDuration"
	private const val KEY_MOTION_START_X = "motionStartX"
	private const val KEY_MOTION_START_Y = "motionStartY"
	private const val KEY_MOTION_END_X = "motionEndX"
	private const val KEY_MOTION_END_Y = "motionEndY"
	private const val KEY_KEYFRAME_START_X = "keyframeStartX"
	private const val KEY_KEYFRAME_START_Y = "keyframeStartY"
	private const val KEY_KEYFRAME_END_X = "keyframeEndX"
	private const val KEY_KEYFRAME_END_Y = "keyframeEndY"
	private const val KEY_KEYFRAME_START_SCALE = "keyframeStartScale"
	private const val KEY_KEYFRAME_END_SCALE = "keyframeEndScale"
	private const val KEY_KEYFRAME_START_OPACITY = "keyframeStartOpacity"
	private const val KEY_KEYFRAME_END_OPACITY = "keyframeEndOpacity"
	private const val KEY_KEYFRAME_START_ROTATION = "keyframeStartRotation"
	private const val KEY_KEYFRAME_END_ROTATION = "keyframeEndRotation"
	private const val KEY_KEYFRAME_EASING = "keyframeEasing"
	private const val KEY_KEYFRAME_PING_PONG = "keyframePingPong"
	private const val KEY_OFFSET_X = "offsetX"
	private const val KEY_OFFSET_Y = "offsetY"
	private const val KEY_SCALE_X = "scaleX"
	private const val KEY_SCALE_Y = "scaleY"
	private const val KEY_OPACITY = "opacity"
	private const val KEY_BLEND_MODE = "blendMode"
	private const val KEY_MASK_ENABLED = "maskEnabled"
	private const val KEY_MASK_SHAPE = "maskShape"
	private const val KEY_MASK_X = "maskX"
	private const val KEY_MASK_Y = "maskY"
	private const val KEY_MASK_RADIUS = "maskRadius"
	private const val KEY_MASK_SOFTNESS = "maskSoftness"
	private const val KEY_MASK_ANGLE = "maskAngle"
	private const val KEY_PARTICLE_PRESET = "particlePreset"
	private const val KEY_PARTICLE_COUNT = "particleCount"
	private const val KEY_PARTICLE_SIZE = "particleSize"
	private const val KEY_PARTICLE_SPEED = "particleSpeed"
	private const val KEY_PARTICLE_SPREAD = "particleSpread"
	private const val KEY_PARTICLE_OPACITY = "particleOpacity"
	private const val KEY_FIT_MODE = "fitMode"
	private const val KEY_PHOTO_ROLE = "photoRole"
	private const val KEY_VISIBLE = "visible"
	private const val KEY_OVERRIDE_POLICY = "overridePolicy"
	private const val KEY_OVERRIDE_FRAME_INTERVAL_MS = "overrideFrameIntervalMs"
	private const val KEY_USE_POWER_SAVER_THROTTLE = "usePowerSaverThrottle"
	private const val KEY_USE_THERMAL_THROTTLE = "useThermalThrottle"
	private const val KEY_POWER_SAVER_POLICY = "powerSaverPolicy"
	private const val KEY_POWER_SAVER_FRAME_INTERVAL_MS = "powerSaverFrameIntervalMs"
	private const val KEY_THERMAL_THROTTLE_FRAME_INTERVAL_MS = "thermalThrottleFrameIntervalMs"

	fun encode(config: WallpaperConfig): String {
		return JSONObject().apply {
			put(KEY_ID, config.id)
			put(KEY_NAME, config.name)
			put(KEY_HORIZON, JSONObject().apply {
				put(KEY_OFFSET, config.horizon.offset.toDouble())
			})
			put(KEY_CELESTIAL, JSONObject().apply {
				put(KEY_SUN_PATH_TYPE, config.celestial.sunPathType.name)
				put(KEY_MOON_PATH_TYPE, config.celestial.moonPathType.name)
				put(KEY_SUN_ORBIT, encodeOrbit(config.celestial.sunOrbit) ?: JSONObject.NULL)
				put(KEY_MOON_ORBIT, encodeOrbit(config.celestial.moonOrbit) ?: JSONObject.NULL)
			})
			put(KEY_FEATURES, JSONObject().apply {
				put(KEY_ATMOSPHERE_ENABLED, config.features.atmosphereEnabled)
				put(KEY_LENS_FLARE_ENABLED, config.features.lensFlareEnabled)
				put(KEY_STARS_ENABLED, config.features.starsEnabled)
			})
			put(KEY_EFFECTS, JSONObject().apply {
				put(KEY_CLOUDS, encodeEffect(config.effects.clouds) ?: JSONObject.NULL)
				put(KEY_STARS, encodeEffect(config.effects.stars) ?: JSONObject.NULL)
				put(KEY_FOG, encodeEffect(config.effects.fog) ?: JSONObject.NULL)
				put(KEY_RAIN, encodeEffect(config.effects.rain) ?: JSONObject.NULL)
				put(KEY_SNOW, encodeEffect(config.effects.snow) ?: JSONObject.NULL)
			})
			put(KEY_TEXTURES, JSONObject().apply {
				put(KEY_SUN_TEXTURE, config.textures.sunTexture)
				put(KEY_MOON_TEXTURE, config.textures.moonTexture)
				put(KEY_FLARE_TEXTURE, config.textures.flareTexture ?: JSONObject.NULL)
				put(KEY_BACKGROUND_TEXTURE, config.textures.backgroundTexture ?: JSONObject.NULL)
			})
			put(KEY_CUSTOM_SKY_COLORS, config.customSkyColors?.let { colors ->
				JSONObject().apply {
					put(KEY_SUNRISE_COLOR, colors.sunriseColor)
					put(KEY_DAY_COLOR, colors.dayColor)
					put(KEY_SUNSET_COLOR, colors.sunsetColor)
					put(KEY_NIGHT_COLOR, colors.nightColor)
				}
			} ?: JSONObject.NULL)
			put(KEY_PREVIEW_LOOP_DURATION_SECONDS, config.previewLoopDurationSeconds.toDouble())
			put(KEY_FOCUS_CATCH_UP_DURATION_SECONDS, config.focusCatchUpDurationSeconds.toDouble())
			put(KEY_DAYLIGHT, JSONObject().apply {
				put(KEY_SUNRISE_MINUTE, config.daylight.sunriseMinute)
				put(KEY_SUNSET_MINUTE, config.daylight.sunsetMinute)
				put(KEY_SOLAR_NOON_MINUTE, config.daylight.solarNoonMinute)
				put(KEY_TIME_ZONE_ID, config.daylight.timeZoneId ?: JSONObject.NULL)
			})
			put(KEY_PEAK_Y, config.peakY.toDouble())
			put(KEY_BELOW_HORIZON_OFFSET, config.belowHorizonOffset.toDouble())
			put(KEY_SHADER, JSONObject().apply {
				put(KEY_FRAGMENT_ASSET_PATH, config.shader.fragmentAssetPath ?: JSONObject.NULL)
				put(KEY_MODE, config.shader.mode)
				put(
					KEY_UNIFORM_OVERRIDES,
					encodeShaderUniformOverrides(config.shader.uniformOverrides) ?: JSONObject.NULL
				)
			})
			put(KEY_RUNTIME_RENDER_POLICY, JSONObject().apply {
				put(KEY_POLICY, config.runtimeRenderPolicy.policy.name)
				put(
					KEY_CONTINUOUS_FRAME_INTERVAL_MS,
					config.runtimeRenderPolicy.continuousFrameIntervalMs
				)
			})
			put(KEY_CAPABILITIES, JSONObject().apply {
				put(KEY_DYNAMIC_MOTION, config.capabilities.dynamicMotion)
				put(KEY_DYNAMIC_TEXTURES, config.capabilities.dynamicTextures)
				put(KEY_LOCATION_AWARE_LIGHTING, config.capabilities.locationAwareLighting)
				put(KEY_SUPPORTS_CLOUD_LAYER, config.capabilities.supportsCloudLayer)
				put(KEY_SUPPORTS_STAR_LAYER, config.capabilities.supportsStarLayer)
			})
			put(KEY_CREATOR, encodeCreatorConfig(config.creator))
			put(KEY_SERVICE_RENDER_POLICY, JSONObject().apply {
				put(KEY_OVERRIDE_POLICY, config.serviceRenderPolicy.overridePolicy?.name ?: JSONObject.NULL)
				put(
					KEY_OVERRIDE_FRAME_INTERVAL_MS,
					config.serviceRenderPolicy.overrideFrameIntervalMs ?: JSONObject.NULL
				)
				put(
					KEY_USE_POWER_SAVER_THROTTLE,
					config.serviceRenderPolicy.usePowerSaverThrottle
				)
				put(KEY_USE_THERMAL_THROTTLE, config.serviceRenderPolicy.useThermalThrottle)
				put(
					KEY_POWER_SAVER_POLICY,
					config.serviceRenderPolicy.powerSaverPolicy?.name ?: JSONObject.NULL
				)
				put(
					KEY_POWER_SAVER_FRAME_INTERVAL_MS,
					config.serviceRenderPolicy.powerSaverFrameIntervalMs ?: JSONObject.NULL
				)
				put(
					KEY_THERMAL_THROTTLE_FRAME_INTERVAL_MS,
					config.serviceRenderPolicy.thermalThrottleFrameIntervalMs ?: JSONObject.NULL
				)
			})
		}.toString()
	}

	fun decode(encoded: String): WallpaperConfig? {
		return runCatching {
			val root = JSONObject(encoded)
			val id = root.optString(KEY_ID, "").takeIf { it.isNotBlank() } ?: return null
			val defaults = WallpaperConfig.default(id)
			val name = root.optString(KEY_NAME, defaults.name).ifBlank { defaults.name }

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
				fallbackPathType = sunPathType
			)
			val moonOrbit = decodeOrbit(
				json = celestial?.optJSONObject(KEY_MOON_ORBIT),
				fallbackPathType = moonPathType
			)

			val features = root.optJSONObject(KEY_FEATURES)
			val atmosphereEnabled = features?.optBoolean(
				KEY_ATMOSPHERE_ENABLED,
				defaults.features.atmosphereEnabled
			) ?: defaults.features.atmosphereEnabled
			val lensFlareEnabled = features?.optBoolean(
				KEY_LENS_FLARE_ENABLED,
				defaults.features.lensFlareEnabled
			) ?: defaults.features.lensFlareEnabled
			val starsEnabled = features?.optBoolean(
				KEY_STARS_ENABLED,
				defaults.features.starsEnabled
			) ?: defaults.features.starsEnabled
			val effects = root.optJSONObject(KEY_EFFECTS)
			val wallpaperEffects = WallpaperEffects(
				clouds = decodeEffect(effects?.optJSONObject(KEY_CLOUDS)),
				stars = decodeEffect(effects?.optJSONObject(KEY_STARS)),
				fog = decodeEffect(effects?.optJSONObject(KEY_FOG)),
				rain = decodeEffect(effects?.optJSONObject(KEY_RAIN)),
				snow = decodeEffect(effects?.optJSONObject(KEY_SNOW))
			)

			val textures = root.optJSONObject(KEY_TEXTURES)
			val sunTexture = textures?.optString(KEY_SUN_TEXTURE, defaults.textures.sunTexture)
				?: defaults.textures.sunTexture
			val moonTexture = textures?.optString(KEY_MOON_TEXTURE, defaults.textures.moonTexture)
				?: defaults.textures.moonTexture
			val flareTexture = textures?.optNullableString(KEY_FLARE_TEXTURE)
			val backgroundTexture = textures?.optNullableString(KEY_BACKGROUND_TEXTURE)

			val customSkyColors = decodeCustomSkyColors(root.optJSONObject(KEY_CUSTOM_SKY_COLORS))

			val previewLoopDurationSeconds = when {
				root.has(KEY_PREVIEW_LOOP_DURATION_SECONDS) -> root.optDouble(
					KEY_PREVIEW_LOOP_DURATION_SECONDS,
					defaults.previewLoopDurationSeconds.toDouble()
				)
				root.has(KEY_ANIMATION_DURATION_SECONDS) -> root.optDouble(
					KEY_ANIMATION_DURATION_SECONDS,
					defaults.previewLoopDurationSeconds.toDouble()
				)
				else -> defaults.previewLoopDurationSeconds.toDouble()
			}.toFloat()
			val focusCatchUpDurationSeconds = root.optDouble(
				KEY_FOCUS_CATCH_UP_DURATION_SECONDS,
				defaults.focusCatchUpDurationSeconds.toDouble()
			).toFloat()

			val daylight = root.optJSONObject(KEY_DAYLIGHT)
			val sunriseMinute = daylight?.optInt(
				KEY_SUNRISE_MINUTE,
				defaults.daylight.sunriseMinute
			) ?: defaults.daylight.sunriseMinute
			val sunsetMinute = daylight?.optInt(
				KEY_SUNSET_MINUTE,
				defaults.daylight.sunsetMinute
			) ?: defaults.daylight.sunsetMinute
			val solarNoonMinute = if (daylight?.has(KEY_SOLAR_NOON_MINUTE) == true) {
				daylight.optInt(KEY_SOLAR_NOON_MINUTE, defaults.daylight.solarNoonMinute)
			} else {
				deriveSolarNoonMinute(
					sunriseMinute = sunriseMinute,
					sunsetMinute = sunsetMinute
				)
			}
			val timeZoneId = daylight?.optNullableString(KEY_TIME_ZONE_ID)

			val peakY = root.optDouble(KEY_PEAK_Y, defaults.peakY.toDouble()).toFloat()
			val belowHorizonOffset = root.optDouble(
				KEY_BELOW_HORIZON_OFFSET,
				defaults.belowHorizonOffset.toDouble()
			).toFloat()

			val shader = root.optJSONObject(KEY_SHADER)
			val fragmentAssetPath = shader?.optNullableString(KEY_FRAGMENT_ASSET_PATH)
			val mode = shader?.optString(KEY_MODE, defaults.shader.mode)?.ifBlank {
				defaults.shader.mode
			} ?: defaults.shader.mode
			val uniformOverrides = decodeShaderUniformOverrides(
				json = shader?.optJSONObject(KEY_UNIFORM_OVERRIDES)
			)
			val runtimeRenderPolicy = decodeRuntimeRenderPolicy(
				json = root.optJSONObject(KEY_RUNTIME_RENDER_POLICY),
				configId = id
			)
			val capabilities = decodeCapabilities(
				json = root.optJSONObject(KEY_CAPABILITIES),
				fallback = defaults.capabilities
			)
			val serviceRenderPolicy = decodeServiceRenderPolicy(
				json = root.optJSONObject(KEY_SERVICE_RENDER_POLICY),
				fallback = defaults.serviceRenderPolicy
			)
			val creator = decodeCreatorConfig(root.optJSONObject(KEY_CREATOR))

			WallpaperConfig(
				id = id,
				name = name,
				horizon = HorizonConfig(offset = horizonOffset),
				celestial = CelestialConfig(
					sunPathType = sunPathType,
					moonPathType = moonPathType,
					sunOrbit = sunOrbit,
					moonOrbit = moonOrbit
				),
				features = SkyFeatureFlags(
					atmosphereEnabled = atmosphereEnabled,
					lensFlareEnabled = lensFlareEnabled,
					starsEnabled = starsEnabled
				),
				effects = wallpaperEffects,
				textures = WallpaperTextures(
					sunTexture = sunTexture,
					moonTexture = moonTexture,
					flareTexture = flareTexture,
					backgroundTexture = backgroundTexture
				),
				customSkyColors = customSkyColors,
				previewLoopDurationSeconds = previewLoopDurationSeconds,
				focusCatchUpDurationSeconds = focusCatchUpDurationSeconds,
				daylight = DaylightConfig(
					sunriseMinute = sunriseMinute,
					sunsetMinute = sunsetMinute,
					solarNoonMinute = solarNoonMinute,
					timeZoneId = timeZoneId
				),
				peakY = peakY,
				belowHorizonOffset = belowHorizonOffset,
				shader = ShaderProfile(
					fragmentAssetPath = fragmentAssetPath,
					mode = mode,
					uniformOverrides = uniformOverrides
				),
				runtimeRenderPolicy = runtimeRenderPolicy,
				capabilities = capabilities,
				creator = creator,
				serviceRenderPolicy = serviceRenderPolicy
			)
		}.getOrNull()
	}

	private fun encodeOrbit(config: CelestialOrbitConfig?): JSONObject? {
		config ?: return null
		return JSONObject().apply {
			put(KEY_SUN_PATH_TYPE, config.pathType.name)
			put(KEY_START_X, config.startX ?: JSONObject.NULL)
			put(KEY_END_X, config.endX ?: JSONObject.NULL)
			put(KEY_PEAK_Y, config.peakY ?: JSONObject.NULL)
			put(KEY_HIDDEN_Y, config.hiddenY ?: JSONObject.NULL)
			put(KEY_CURVE, config.curve.name)
		}
	}

	private fun decodeOrbit(
		json: JSONObject?,
		fallbackPathType: PathType
	): CelestialOrbitConfig? {
		json ?: return null
		val hasOrbitValues = json.has(KEY_START_X) ||
			json.has(KEY_END_X) ||
			json.has(KEY_PEAK_Y) ||
			json.has(KEY_HIDDEN_Y) ||
			json.has(KEY_CURVE)
		if (!hasOrbitValues && !json.has(KEY_SUN_PATH_TYPE)) {
			return null
		}
		return CelestialOrbitConfig(
			pathType = parsePathType(
				raw = json.optString(KEY_SUN_PATH_TYPE, fallbackPathType.name),
				fallback = fallbackPathType
			),
			startX = json.optNullableFloat(KEY_START_X),
			endX = json.optNullableFloat(KEY_END_X),
			peakY = json.optNullableFloat(KEY_PEAK_Y),
			hiddenY = json.optNullableFloat(KEY_HIDDEN_Y),
			curve = parseOrbitCurve(
				raw = json.optString(KEY_CURVE, OrbitCurve.LINEAR.name),
				fallback = OrbitCurve.LINEAR
			)
		)
	}

	private fun parsePathType(raw: String?, fallback: PathType): PathType {
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

	private fun encodeEffect(config: EffectConfig?): JSONObject? {
		config ?: return null
		return JSONObject().apply {
			put(KEY_ENABLED, config.enabled)
			put(KEY_SPEED, config.speed.toDouble())
			put(KEY_DENSITY, config.density.toDouble())
			put(KEY_INTENSITY, config.intensity.toDouble())
		}
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

	private fun decodeCustomSkyColors(json: JSONObject?): SkyColors? {
		json ?: return null
		if (!json.has(KEY_SUNRISE_COLOR) || !json.has(KEY_DAY_COLOR) ||
			!json.has(KEY_SUNSET_COLOR) || !json.has(KEY_NIGHT_COLOR)
		) {
			return null
		}
		return SkyColors(
			sunriseColor = json.optInt(KEY_SUNRISE_COLOR),
			dayColor = json.optInt(KEY_DAY_COLOR),
			sunsetColor = json.optInt(KEY_SUNSET_COLOR),
			nightColor = json.optInt(KEY_NIGHT_COLOR)
		)
	}

	private fun decodeRuntimeRenderPolicy(
		json: JSONObject?,
		configId: String
	): RuntimeRenderPolicy {
		if (json == null) {
			return WallpaperConfig.legacyRuntimeRenderPolicy(configId)
		}
		val fallback = WallpaperConfig.legacyRuntimeRenderPolicy(configId)
		val policy = parseRenderPolicy(
			raw = json.optString(KEY_POLICY, fallback.policy.name),
			fallback = fallback.policy
		) ?: fallback.policy
		val intervalMs = json.optLong(
			KEY_CONTINUOUS_FRAME_INTERVAL_MS,
			fallback.continuousFrameIntervalMs
		).coerceAtLeast(1L)
		return RuntimeRenderPolicy(
			policy = policy,
			continuousFrameIntervalMs = intervalMs
		)
	}

	private fun encodeShaderUniformOverrides(
		overrides: ShaderUniformOverrides
	): JSONObject? {
		val hasOverrides = overrides.cityZoom != null ||
			overrides.cityVerticalOffset != null ||
			overrides.cityHorizontalOffset != null ||
			overrides.cloudAlpha != null ||
			overrides.cloudOffset != null
		if (!hasOverrides) return null
		return JSONObject().apply {
			put(KEY_CITY_ZOOM, overrides.cityZoom ?: JSONObject.NULL)
			put(KEY_CITY_VERTICAL_OFFSET, overrides.cityVerticalOffset ?: JSONObject.NULL)
			put(KEY_CITY_HORIZONTAL_OFFSET, overrides.cityHorizontalOffset ?: JSONObject.NULL)
			put(KEY_CLOUD_ALPHA, overrides.cloudAlpha ?: JSONObject.NULL)
			put(KEY_CLOUD_OFFSET, overrides.cloudOffset ?: JSONObject.NULL)
		}
	}

	private fun decodeShaderUniformOverrides(
		json: JSONObject?
	): ShaderUniformOverrides {
		json ?: return ShaderUniformOverrides()
		return ShaderUniformOverrides(
			cityZoom = json.optNullableFloat(KEY_CITY_ZOOM),
			cityVerticalOffset = json.optNullableFloat(KEY_CITY_VERTICAL_OFFSET),
			cityHorizontalOffset = json.optNullableFloat(KEY_CITY_HORIZONTAL_OFFSET),
			cloudAlpha = json.optNullableFloat(KEY_CLOUD_ALPHA),
			cloudOffset = json.optNullableFloat(KEY_CLOUD_OFFSET)
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

	private fun encodeCreatorConfig(config: CreatorConfig): JSONObject {
		return JSONObject().apply {
			put(KEY_SCHEMA_VERSION, config.schemaVersion)
			put(KEY_LAYERS, JSONArray().apply {
				config.layers.forEach { layer ->
					put(JSONObject().apply {
						put(KEY_TEXTURE_PATH, layer.texturePath)
						put(KEY_MEDIA_TYPE, layer.mediaType)
						put(KEY_MIME_TYPE, layer.mimeType)
						put(KEY_MOTION_TYPE, layer.motionType)
						put(KEY_MOTION_SPEED, layer.motionSpeed.toDouble())
						put(KEY_MOTION_AMPLITUDE, layer.motionAmplitude.toDouble())
						put(KEY_MOTION_DIRECTION, layer.motionDirection.toDouble())
						put(KEY_MOTION_DURATION, layer.motionDuration.toDouble())
						put(KEY_MOTION_START_X, layer.motionStartX.toDouble())
						put(KEY_MOTION_START_Y, layer.motionStartY.toDouble())
						put(KEY_MOTION_END_X, layer.motionEndX.toDouble())
						put(KEY_MOTION_END_Y, layer.motionEndY.toDouble())
						put(KEY_KEYFRAME_START_X, layer.keyframeStartX.toDouble())
						put(KEY_KEYFRAME_START_Y, layer.keyframeStartY.toDouble())
						put(KEY_KEYFRAME_END_X, layer.keyframeEndX.toDouble())
						put(KEY_KEYFRAME_END_Y, layer.keyframeEndY.toDouble())
						put(KEY_KEYFRAME_START_SCALE, layer.keyframeStartScale.toDouble())
						put(KEY_KEYFRAME_END_SCALE, layer.keyframeEndScale.toDouble())
						put(KEY_KEYFRAME_START_OPACITY, layer.keyframeStartOpacity.toDouble())
						put(KEY_KEYFRAME_END_OPACITY, layer.keyframeEndOpacity.toDouble())
						put(KEY_KEYFRAME_START_ROTATION, layer.keyframeStartRotation.toDouble())
						put(KEY_KEYFRAME_END_ROTATION, layer.keyframeEndRotation.toDouble())
						put(KEY_KEYFRAME_EASING, layer.keyframeEasing)
						put(KEY_KEYFRAME_PING_PONG, layer.keyframePingPong)
						put(KEY_OFFSET_X, layer.offsetX.toDouble())
						put(KEY_OFFSET_Y, layer.offsetY.toDouble())
						put(KEY_SCALE_X, layer.scaleX.toDouble())
						put(KEY_SCALE_Y, layer.scaleY.toDouble())
						put(KEY_OPACITY, layer.opacity.toDouble())
						put(KEY_BLEND_MODE, layer.blendMode)
						put(KEY_MASK_ENABLED, layer.maskEnabled)
						put(KEY_MASK_SHAPE, layer.maskShape)
						put(KEY_MASK_X, layer.maskX.toDouble())
						put(KEY_MASK_Y, layer.maskY.toDouble())
						put(KEY_MASK_RADIUS, layer.maskRadius.toDouble())
						put(KEY_MASK_SOFTNESS, layer.maskSoftness.toDouble())
						put(KEY_MASK_ANGLE, layer.maskAngle.toDouble())
						put(KEY_PARTICLE_PRESET, layer.particlePreset)
						put(KEY_PARTICLE_COUNT, layer.particleCount)
						put(KEY_PARTICLE_SIZE, layer.particleSize.toDouble())
						put(KEY_PARTICLE_SPEED, layer.particleSpeed.toDouble())
						put(KEY_PARTICLE_SPREAD, layer.particleSpread.toDouble())
						put(KEY_PARTICLE_OPACITY, layer.particleOpacity.toDouble())
						put(KEY_FIT_MODE, layer.fitMode)
						put(KEY_PHOTO_ROLE, layer.photoRole)
						put(KEY_VISIBLE, layer.visible)
					})
				}
			})
		}
	}

	private fun decodeCreatorConfig(json: JSONObject?): CreatorConfig {
		json ?: return CreatorConfig()
		val layersJson = json.optJSONArray(KEY_LAYERS)
		val layers = ArrayList<CreatorLayerConfig>(layersJson?.length() ?: 0)
		if (layersJson != null) {
			for (index in 0 until layersJson.length()) {
				val layer = layersJson.optJSONObject(index) ?: continue
				layers += CreatorLayerConfig(
					texturePath = layer.optString(KEY_TEXTURE_PATH, ""),
					mediaType = layer.optString(KEY_MEDIA_TYPE, "image"),
					mimeType = layer.optString(KEY_MIME_TYPE, ""),
					motionType = layer.optString(KEY_MOTION_TYPE, "STATIC"),
					motionSpeed = layer.optDouble(KEY_MOTION_SPEED, 1.0).toFloat(),
					motionAmplitude = layer.optDouble(KEY_MOTION_AMPLITUDE, 0.0).toFloat(),
					motionDirection = layer.optDouble(KEY_MOTION_DIRECTION, 0.0).toFloat(),
					motionDuration = layer.optDouble(KEY_MOTION_DURATION, 5.0).toFloat(),
					motionStartX = layer.optDouble(KEY_MOTION_START_X, 0.0).toFloat(),
					motionStartY = layer.optDouble(KEY_MOTION_START_Y, 0.0).toFloat(),
					motionEndX = layer.optDouble(KEY_MOTION_END_X, 0.0).toFloat(),
					motionEndY = layer.optDouble(KEY_MOTION_END_Y, 0.0).toFloat(),
					keyframeStartX = layer.optDouble(KEY_KEYFRAME_START_X, 0.0).toFloat(),
					keyframeStartY = layer.optDouble(KEY_KEYFRAME_START_Y, 0.0).toFloat(),
					keyframeEndX = layer.optDouble(KEY_KEYFRAME_END_X, 0.0).toFloat(),
					keyframeEndY = layer.optDouble(KEY_KEYFRAME_END_Y, 0.0).toFloat(),
					keyframeStartScale = layer.optDouble(KEY_KEYFRAME_START_SCALE, 1.0).toFloat(),
					keyframeEndScale = layer.optDouble(KEY_KEYFRAME_END_SCALE, 1.0).toFloat(),
					keyframeStartOpacity = layer.optDouble(KEY_KEYFRAME_START_OPACITY, 1.0).toFloat(),
					keyframeEndOpacity = layer.optDouble(KEY_KEYFRAME_END_OPACITY, 1.0).toFloat(),
					keyframeStartRotation = layer.optDouble(KEY_KEYFRAME_START_ROTATION, 0.0).toFloat(),
					keyframeEndRotation = layer.optDouble(KEY_KEYFRAME_END_ROTATION, 0.0).toFloat(),
					keyframeEasing = layer.optString(KEY_KEYFRAME_EASING, "LINEAR"),
					keyframePingPong = layer.optBoolean(KEY_KEYFRAME_PING_PONG, true),
					offsetX = layer.optDouble(KEY_OFFSET_X, 0.0).toFloat(),
					offsetY = layer.optDouble(KEY_OFFSET_Y, 0.0).toFloat(),
					scaleX = layer.optDouble(KEY_SCALE_X, 1.0).toFloat(),
					scaleY = layer.optDouble(KEY_SCALE_Y, 1.0).toFloat(),
					opacity = layer.optDouble(KEY_OPACITY, 1.0).toFloat(),
					blendMode = layer.optString(KEY_BLEND_MODE, "normal"),
					maskEnabled = layer.optBoolean(KEY_MASK_ENABLED, false),
					maskShape = layer.optString(KEY_MASK_SHAPE, "RADIAL"),
					maskX = layer.optDouble(KEY_MASK_X, 0.5).toFloat(),
					maskY = layer.optDouble(KEY_MASK_Y, 0.5).toFloat(),
					maskRadius = layer.optDouble(KEY_MASK_RADIUS, 0.45).toFloat(),
					maskSoftness = layer.optDouble(KEY_MASK_SOFTNESS, 0.15).toFloat(),
					maskAngle = layer.optDouble(KEY_MASK_ANGLE, 90.0).toFloat(),
					particlePreset = layer.optString(KEY_PARTICLE_PRESET, "STARS"),
					particleCount = layer.optInt(KEY_PARTICLE_COUNT, 80),
					particleSize = layer.optDouble(KEY_PARTICLE_SIZE, 0.012).toFloat(),
					particleSpeed = layer.optDouble(KEY_PARTICLE_SPEED, 0.25).toFloat(),
					particleSpread = layer.optDouble(KEY_PARTICLE_SPREAD, 1.0).toFloat(),
					particleOpacity = layer.optDouble(KEY_PARTICLE_OPACITY, 0.8).toFloat(),
					fitMode = layer.optString(KEY_FIT_MODE, "cover"),
					photoRole = layer.optString(KEY_PHOTO_ROLE, "NONE"),
					visible = layer.optBoolean(KEY_VISIBLE, true)
				)
			}
		}
		return CreatorConfig(
			schemaVersion = json.optInt(KEY_SCHEMA_VERSION, 1),
			layers = layers
		)
	}

	private fun parseRenderPolicy(
		raw: String?,
		fallback: RenderPolicy?
	): RenderPolicy? {
		if (raw.isNullOrBlank()) return fallback
		return runCatching { RenderPolicy.valueOf(raw) }.getOrElse { fallback }
	}

	private fun JSONObject.optNullableString(key: String): String? {
		if (isNull(key)) return null
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

	private fun deriveSolarNoonMinute(
		sunriseMinute: Int,
		sunsetMinute: Int
	): Int {
		val sunrise = sunriseMinute.coerceIn(0, 24 * 60)
		val sunset = sunsetMinute.coerceIn(0, 24 * 60)
		val duration = (sunset - sunrise).coerceAtLeast(1)
		return (sunrise + (duration / 2)).coerceIn(0, 24 * 60)
	}
}
