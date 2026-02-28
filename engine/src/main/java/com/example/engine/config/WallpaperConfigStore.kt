package com.example.engine.config

import android.content.Context
import android.content.SharedPreferences
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
		writeEncoded(credentialPreferences, encoded)
		writeEncoded(deviceProtectedPreferences, encoded)
	}

	fun loadSelected(): WallpaperConfig? {
		val deviceEncoded = readEncoded(deviceProtectedPreferences)
		if (deviceEncoded != null) {
			WallpaperConfigJsonCodec.decode(deviceEncoded)?.let { return it }
			removeEncoded(deviceProtectedPreferences)
		}

		val credentialEncoded = readEncoded(credentialPreferences)
		if (credentialEncoded != null) {
			val decoded = WallpaperConfigJsonCodec.decode(credentialEncoded)
			if (decoded != null) {
				writeEncoded(deviceProtectedPreferences, credentialEncoded)
				return decoded
			}
			removeEncoded(credentialPreferences)
		}
		return null
	}

	fun clearSelected() {
		removeEncoded(credentialPreferences)
		removeEncoded(deviceProtectedPreferences)
	}

	private fun readEncoded(preferences: SharedPreferences?): String? {
		if (preferences == null) return null
		return runCatching {
			preferences.getString(KEY_SELECTED_CONFIG_JSON, null)
		}.getOrNull()
	}

	private fun writeEncoded(
		preferences: SharedPreferences?,
		encoded: String
	) {
		if (preferences == null) return
		runCatching {
			val editor = preferences.edit()
				.putString(KEY_SELECTED_CONFIG_JSON, encoded)
			if (!editor.commit()) {
				editor.apply()
			}
		}
	}

	private fun removeEncoded(preferences: SharedPreferences?) {
		if (preferences == null) return
		runCatching {
			val editor = preferences.edit()
				.remove(KEY_SELECTED_CONFIG_JSON)
			if (!editor.commit()) {
				editor.apply()
			}
		}
	}

	companion object {
		private const val PREFERENCES_NAME = "lumisky_wallpaper_config_store"
		private const val KEY_SELECTED_CONFIG_JSON = "selected_config_json"
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
	private const val KEY_FEATURES = "features"
	private const val KEY_ATMOSPHERE_ENABLED = "atmosphereEnabled"
	private const val KEY_LENS_FLARE_ENABLED = "lensFlareEnabled"
	private const val KEY_STARS_ENABLED = "starsEnabled"
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
	private const val KEY_RUNTIME_RENDER_POLICY = "runtimeRenderPolicy"
	private const val KEY_POLICY = "policy"
	private const val KEY_CONTINUOUS_FRAME_INTERVAL_MS = "continuousFrameIntervalMs"

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
			})
			put(KEY_FEATURES, JSONObject().apply {
				put(KEY_ATMOSPHERE_ENABLED, config.features.atmosphereEnabled)
				put(KEY_LENS_FLARE_ENABLED, config.features.lensFlareEnabled)
				put(KEY_STARS_ENABLED, config.features.starsEnabled)
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
			put(KEY_ANIMATION_DURATION_SECONDS, config.previewLoopDurationSeconds.toDouble())
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
			})
			put(KEY_RUNTIME_RENDER_POLICY, JSONObject().apply {
				put(KEY_POLICY, config.runtimeRenderPolicy.policy.name)
				put(
					KEY_CONTINUOUS_FRAME_INTERVAL_MS,
					config.runtimeRenderPolicy.continuousFrameIntervalMs
				)
			})
		}.toString()
	}

	fun decode(encoded: String): WallpaperConfig? {
		return runCatching {
			val root = JSONObject(encoded)
			val defaults = WallpaperConfig.default()

			val id = root.optString(KEY_ID, "").takeIf { it.isNotBlank() } ?: return null
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
			val runtimeRenderPolicy = decodeRuntimeRenderPolicy(
				json = root.optJSONObject(KEY_RUNTIME_RENDER_POLICY),
				configId = id
			)

			WallpaperConfig(
				id = id,
				name = name,
				horizon = HorizonConfig(offset = horizonOffset),
				celestial = CelestialConfig(
					sunPathType = sunPathType,
					moonPathType = moonPathType
				),
				features = SkyFeatureFlags(
					atmosphereEnabled = atmosphereEnabled,
					lensFlareEnabled = lensFlareEnabled,
					starsEnabled = starsEnabled
				),
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
					mode = mode
				),
				runtimeRenderPolicy = runtimeRenderPolicy
			)
		}.getOrNull()
	}

	private fun parsePathType(raw: String?, fallback: PathType): PathType {
		if (raw.isNullOrBlank()) return fallback
		return runCatching { PathType.valueOf(raw) }.getOrElse { fallback }
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
		)
		val intervalMs = json.optLong(
			KEY_CONTINUOUS_FRAME_INTERVAL_MS,
			fallback.continuousFrameIntervalMs
		).coerceAtLeast(1L)
		return RuntimeRenderPolicy(
			policy = policy,
			continuousFrameIntervalMs = intervalMs
		)
	}

	private fun parseRenderPolicy(
		raw: String?,
		fallback: RenderPolicy
	): RenderPolicy {
		if (raw.isNullOrBlank()) return fallback
		return runCatching { RenderPolicy.valueOf(raw) }.getOrElse { fallback }
	}

	private fun JSONObject.optNullableString(key: String): String? {
		if (isNull(key)) return null
		return optString(key, "").takeIf { it.isNotBlank() }
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
