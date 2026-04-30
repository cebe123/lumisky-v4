package com.example.engine.shader

import android.os.SystemClock
import com.example.engine.config.ShaderUniformOverrides
import com.example.engine.config.WallpaperConfig
import com.example.engine.renderer.RenderFrameState
import com.example.engine.renderer.RenderMode
import kotlin.math.floor

internal data class LegacySunColor(
	val red: Float,
	val green: Float,
	val blue: Float
)

internal data class CityTuning(
	val zoom: Float,
	val verticalOffset: Float,
	val horizontalOffset: Float
)

internal data class LegacyThemeState(
	val sunColor: LegacySunColor,
	val timeSeconds: Float,
	val timeOfDay: Float,
	val solarNoonMinute: Float,
	val nightAmount: Float,
	val cityTuning: CityTuning,
	val swapForegroundPair: Boolean
)

internal class LegacyThemeAdapter(
	private val elapsedRealtimeProvider: () -> Long = { SystemClock.elapsedRealtime() }
) {

	fun resolve(
		config: WallpaperConfig,
		state: RenderFrameState
	): LegacyThemeState {
		val minute = (state.dayProgress * MINUTES_PER_DAY).coerceIn(0f, MINUTES_PER_DAY.toFloat())
		return LegacyThemeState(
			sunColor = resolveLegacySunColor(config, state),
			timeSeconds = resolveLegacyTimeSeconds(config, state),
			timeOfDay = resolveLegacyTimeOfDay(config, state),
			solarNoonMinute = resolveLegacySunZenithMinute(
				config = config,
				sunrise = state.sunriseMinute,
				sunset = state.sunsetMinute
			).toFloat(),
			nightAmount = calculateLegacyNightAmount(
				minute = minute,
				sunrise = state.sunriseMinute.toFloat(),
				sunset = state.sunsetMinute.toFloat()
			),
			cityTuning = resolveCityTuning(config),
			swapForegroundPair = isWarrior(config.id)
		)
	}

	private fun resolveCityTuning(config: WallpaperConfig): CityTuning {
		val overrides = config.shader.uniformOverrides
		val fallback = when {
			config.id.contains("city_istanbul") -> CITY_TUNING_ISTANBUL
			config.id.contains("city_newyork") -> CITY_TUNING_NEW_YORK
			config.id.contains("city_tokyo") -> CITY_TUNING_TOKYO
			config.id.contains("city_paris") -> CITY_TUNING_PARIS
			else -> CITY_TUNING_DEFAULT
		}
		return applyOverrides(fallback, overrides)
	}

	private fun applyOverrides(
		base: CityTuning,
		overrides: ShaderUniformOverrides
	): CityTuning {
		return CityTuning(
			zoom = overrides.cityZoom ?: base.zoom,
			verticalOffset = overrides.cityVerticalOffset ?: base.verticalOffset,
			horizontalOffset = overrides.cityHorizontalOffset ?: base.horizontalOffset
		)
	}

	private fun resolveLegacySunColor(
		config: WallpaperConfig,
		state: RenderFrameState
	): LegacySunColor {
		val minute = (state.dayProgress * MINUTES_PER_DAY).coerceIn(0f, MINUTES_PER_DAY.toFloat())
		val sunrise = state.sunriseMinute.toFloat()
		val sunset = state.sunsetMinute.toFloat()
		if (sunset <= sunrise || minute < sunrise || minute > sunset) {
			return LegacySunColor(red = 1.0f, green = 0.9f, blue = 0.8f)
		}
		val dayProgress = ((minute - sunrise) / (sunset - sunrise)).coerceIn(0f, 1f)
		var colorProgress = if (dayProgress < 0.5f) {
			dayProgress * 2f
		} else {
			2f - (dayProgress * 2f)
		}
		if (usesNaturalSunColor(config.id)) {
			colorProgress = 1f - colorProgress
		}
		return LegacySunColor(
			red = 1.0f,
			green = 0.9f - (0.4f * colorProgress),
			blue = 0.8f - (0.6f * colorProgress)
		)
	}

	private fun resolveLegacyTimeOfDay(
		config: WallpaperConfig,
		state: RenderFrameState
	): Float {
		val id = config.id.lowercase()
		return when {
			id.contains("mars") || id.contains("warrior") -> resolveMarsWarriorTimeOfDay(config, state)
			id.contains("optical_sunset") -> resolveOpticalSunsetTimeOfDay(config, state)
			else -> state.dayProgress.coerceIn(0f, 1f)
		}
	}

	private fun resolveLegacySunZenithMinute(
		config: WallpaperConfig,
		sunrise: Int,
		sunset: Int
	): Int {
		val fallback = sunrise + ((sunset - sunrise) / 2)
		val configured = config.daylight.solarNoonMinute
		return if (configured in sunrise..sunset) configured else fallback
	}

	private fun resolveMarsWarriorTimeOfDay(
		config: WallpaperConfig,
		state: RenderFrameState
	): Float {
		val sunrise = state.sunriseMinute
		val sunset = state.sunsetMinute
		if (sunset <= sunrise) return state.dayProgress.coerceIn(0f, 1f)
		val minute = ((state.dayProgress * MINUTES_PER_DAY).toInt() % MINUTES_PER_DAY + MINUTES_PER_DAY) % MINUTES_PER_DAY
		val zenithMinute = resolveLegacySunZenithMinute(config, sunrise, sunset)
		val horizon = 0.40f
		val result = when {
			minute in sunrise..zenithMinute && zenithMinute > sunrise -> {
				val progress = (minute - sunrise).toFloat() / (zenithMinute - sunrise).toFloat()
				horizon * (1.0f - progress)
			}
			minute in zenithMinute..sunset && sunset > zenithMinute -> {
				val progress = (minute - zenithMinute).toFloat() / (sunset - zenithMinute).toFloat()
				horizon * progress
			}
			else -> {
				var minutesSinceSunset = minute - sunset
				if (minutesSinceSunset < 0) minutesSinceSunset += MINUTES_PER_DAY
				val nightDuration = ((MINUTES_PER_DAY - sunset) + sunrise).coerceAtLeast(1)
				val progress = minutesSinceSunset.toFloat() / nightDuration.toFloat()
				horizon + (progress * (1.0f - horizon))
			}
		}
		return result.coerceIn(0f, 1f)
	}

	private fun resolveOpticalSunsetTimeOfDay(
		config: WallpaperConfig,
		state: RenderFrameState
	): Float {
		val sunrise = state.sunriseMinute
		val sunset = state.sunsetMinute
		if (sunset <= sunrise) return state.dayProgress.coerceIn(0f, 1f)
		val minute = ((state.dayProgress * MINUTES_PER_DAY).toInt() % MINUTES_PER_DAY + MINUTES_PER_DAY) % MINUTES_PER_DAY
		val zenithMinute = resolveLegacySunZenithMinute(config, sunrise, sunset)
		val horizon = 0.2666f
		val result = when {
			minute in sunrise..zenithMinute && zenithMinute > sunrise -> {
				val progress = (minute - sunrise).toFloat() / (zenithMinute - sunrise).toFloat()
				horizon * (1.0f - progress)
			}
			minute in zenithMinute..sunset && sunset > zenithMinute -> {
				val progress = (minute - zenithMinute).toFloat() / (sunset - zenithMinute).toFloat()
				horizon * progress
			}
			else -> {
				var minutesSinceSunset = minute - sunset
				if (minutesSinceSunset < 0) minutesSinceSunset += MINUTES_PER_DAY
				val nightDuration = ((MINUTES_PER_DAY - sunset) + sunrise).coerceAtLeast(1)
				val progress = minutesSinceSunset.toFloat() / nightDuration.toFloat()
				if (progress < 0.5f) {
					val p = progress * 2.0f
					horizon + (p * (1.0f - horizon))
				} else {
					val p = (progress - 0.5f) * 2.0f
					1.0f - (p * (1.0f - horizon))
				}
			}
		}
		return result.coerceIn(0f, 1f)
	}

	private fun usesNaturalSunColor(configId: String): Boolean {
		return configId.lowercase().contains("pixel_forest")
	}

	private fun isWarrior(configId: String): Boolean {
		return configId.lowercase().contains("warrior")
	}

	private fun isFlower(configId: String): Boolean {
		return configId.lowercase().contains("flower")
	}

	private fun resolveLegacyTimeSeconds(
		config: WallpaperConfig,
		state: RenderFrameState
	): Float {
		val id = config.id.lowercase()
		if (!isWarrior(id)) {
			val baseSeconds = when (state.mode) {
				RenderMode.FOCUS,
				RenderMode.WALLPAPER_SERVICE -> realtimeSeconds()
				else -> (state.frameTimeMillis % LEGACY_TIME_WINDOW_MS).toFloat() / 1000f
			}
			return when {
				isFlower(id) && state.mode == RenderMode.WALLPAPER_SERVICE ->
					quantizeTimeSeconds(baseSeconds * FLOWER_WALLPAPER_TWINKLE_TIME_SCALE, FLOWER_WALLPAPER_TWINKLE_FPS)
				else -> baseSeconds
			}
		}

		return when (state.mode) {
			RenderMode.FOCUS -> realtimeSeconds()
			RenderMode.PREVIEW -> {
				val acceleratedSeconds = (state.frameTimeMillis % MILLIS_PER_DAY).toFloat() / 1000f
				quantizeTimeSeconds(acceleratedSeconds, WARRIOR_TEXTURE_FPS)
			}
			RenderMode.WALLPAPER_SERVICE -> {
				quantizeTimeSeconds(realtimeSeconds(), WARRIOR_TEXTURE_FPS)
			}
			else -> (state.frameTimeMillis % LEGACY_TIME_WINDOW_MS).toFloat() / 1000f
		}
	}

	private fun realtimeSeconds(): Float {
		return (elapsedRealtimeProvider() % LEGACY_REALTIME_WINDOW_MS).toFloat() / 1000f
	}

	private fun quantizeTimeSeconds(
		seconds: Float,
		fps: Float
	): Float {
		if (fps <= 0f) return seconds
		val frameStep = 1f / fps
		return floor(seconds / frameStep) * frameStep
	}

	private fun calculateLegacyNightAmount(
		minute: Float,
		sunrise: Float,
		sunset: Float
	): Float {
		// Normalize minutes relative to sunset to handle wrap-around days easily.
		val relM = (minute - sunset + MINUTES_PER_DAY) % MINUTES_PER_DAY
		val relR = (sunrise - sunset + MINUTES_PER_DAY) % MINUTES_PER_DAY

		if (relM < NIGHT_TRANSITION_AFTER_SUNSET_MIN) {
			return smoothstep(0f, NIGHT_TRANSITION_AFTER_SUNSET_MIN, relM)
		}

		val relSunriseStart = relR - NIGHT_TRANSITION_BEFORE_SUNRISE_WIDE_MIN
		val relSunriseEnd = relR + NIGHT_TRANSITION_AFTER_SUNRISE_MIN

		if (relM >= relSunriseStart && relM <= relSunriseEnd) {
			val t = smoothstep(relSunriseStart, relSunriseEnd, relM)
			return 1f - t
		}

		if (relM > NIGHT_TRANSITION_AFTER_SUNSET_MIN && relM < relSunriseStart) {
			return 1f
		}

		return 0f
	}

	private fun smoothstep(
		edge0: Float,
		edge1: Float,
		value: Float
	): Float {
		if (edge0 == edge1) return if (value < edge0) 0f else 1f
		val t = ((value - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
		return t * t * (3f - (2f * t))
	}

	private companion object {
		const val MINUTES_PER_DAY = 1440
		const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
		const val LEGACY_TIME_WINDOW_MS = 100_000L
		const val LEGACY_REALTIME_WINDOW_MS = 1_000_000L
		const val WARRIOR_TEXTURE_FPS = 10f
		const val NIGHT_TRANSITION_AFTER_SUNSET_MIN = 20f
		const val NIGHT_TRANSITION_BEFORE_SUNRISE_MIN = 20f
		const val NIGHT_TRANSITION_BEFORE_SUNRISE_WIDE_MIN = 30f
		const val NIGHT_TRANSITION_AFTER_SUNRISE_MIN = 10f
		const val FLOWER_WALLPAPER_TWINKLE_TIME_SCALE = 3.4f
		const val FLOWER_WALLPAPER_TWINKLE_FPS = 24f

		val CITY_TUNING_ISTANBUL = CityTuning(zoom = 0.60f, verticalOffset = 0.04f, horizontalOffset = 0.0f)
		val CITY_TUNING_NEW_YORK = CityTuning(zoom = 0.70f, verticalOffset = 0.04f, horizontalOffset = 0.0f)
		val CITY_TUNING_TOKYO = CityTuning(zoom = 0.75f, verticalOffset = 0.04f, horizontalOffset = 0.0f)
		val CITY_TUNING_PARIS = CityTuning(zoom = 0.70f, verticalOffset = 0.04f, horizontalOffset = 0.0f)
		val CITY_TUNING_DEFAULT = CityTuning(zoom = 0.85f, verticalOffset = 0.04f, horizontalOffset = 0.0f)
	}
}
