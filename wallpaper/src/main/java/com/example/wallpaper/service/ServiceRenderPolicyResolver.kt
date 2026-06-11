package com.example.wallpaper.service

import com.example.core.settings.PerformanceMode
import com.example.engine.config.RenderPolicy
import com.example.engine.config.RuntimeRenderPolicy
import com.example.engine.config.WallpaperConfig

internal enum class WallpaperLoopMode {
	NONE,
	STATIC,
	MINUTE_TICK,
	VSYNC
}

internal data class ResolvedServiceRenderPolicy(
	val loopMode: WallpaperLoopMode,
	val frameIntervalMs: Long? = null,
	val targetFrameRateFps: Int? = null
)

internal class ServiceRenderPolicyResolver(
	private val isPowerSaveModeProvider: () -> Boolean = { false },
	private val thermalStatusProvider: () -> Int? = { null }
) {

	internal fun resolve(
		config: WallpaperConfig,
		previewMode: Boolean,
		visible: Boolean,
		surfaceAttached: Boolean,
		performanceMode: PerformanceMode = PerformanceMode.AUTO,
		displayRefreshRateHz: Int = DEFAULT_DISPLAY_REFRESH_RATE_HZ
	): ResolvedServiceRenderPolicy {
		if (!visible || !surfaceAttached) {
			return ResolvedServiceRenderPolicy(loopMode = WallpaperLoopMode.NONE)
		}
		if (previewMode) {
			return ResolvedServiceRenderPolicy(loopMode = WallpaperLoopMode.VSYNC)
		}

		val basePolicy = resolveBasePolicy(config)
		var policy = basePolicy.policy
		var frameIntervalMs = basePolicy.continuousFrameIntervalMs.coerceAtLeast(1L)
		if (policy != RenderPolicy.CONTINUOUS && shouldPromoteDynamicContentToContinuous(config)) {
			policy = RenderPolicy.CONTINUOUS
		}

		if (config.serviceRenderPolicy.usePowerSaverThrottle && isPowerSaveModeProvider()) {
			val powerSaverPolicy = config.serviceRenderPolicy.powerSaverPolicy
			if (powerSaverPolicy != null) {
				policy = powerSaverPolicy
			}
			val throttleInterval = config.serviceRenderPolicy.powerSaverFrameIntervalMs
				?: DEFAULT_POWER_SAVER_FRAME_INTERVAL_MS
			if (policy == RenderPolicy.CONTINUOUS) {
				frameIntervalMs = maxOf(frameIntervalMs, throttleInterval)
			}
		}

		if (config.serviceRenderPolicy.useThermalThrottle) {
			val thermalStatus = thermalStatusProvider()
			if (thermalStatus != null) {
				val thermalFloor = when {
					thermalStatus >= THERMAL_STATUS_SEVERE ->
						config.serviceRenderPolicy.thermalThrottleFrameIntervalMs
							?: DEFAULT_THERMAL_SEVERE_FRAME_INTERVAL_MS
					thermalStatus >= THERMAL_STATUS_MODERATE ->
						config.serviceRenderPolicy.thermalThrottleFrameIntervalMs
							?: DEFAULT_THERMAL_MODERATE_FRAME_INTERVAL_MS
					else -> null
				}
				if (thermalFloor != null && policy == RenderPolicy.CONTINUOUS) {
					frameIntervalMs = maxOf(frameIntervalMs, thermalFloor)
				}
			}
		}

		return when (policy) {
			RenderPolicy.STATIC -> ResolvedServiceRenderPolicy(loopMode = WallpaperLoopMode.STATIC)
			RenderPolicy.MINUTE_TICK -> ResolvedServiceRenderPolicy(loopMode = WallpaperLoopMode.MINUTE_TICK)
			RenderPolicy.CONTINUOUS -> ResolvedServiceRenderPolicy(
				loopMode = WallpaperLoopMode.VSYNC,
				frameIntervalMs = frameIntervalMs.coerceAtLeast(1L),
				targetFrameRateFps = resolveSettingsFrameRate(
					performanceMode = performanceMode,
					displayRefreshRateHz = displayRefreshRateHz
				)
			)
		}
	}

	private fun resolveBasePolicy(config: WallpaperConfig): RuntimeRenderPolicy {
		val overridePolicy = config.serviceRenderPolicy.overridePolicy
		val overrideFrameIntervalMs = config.serviceRenderPolicy.overrideFrameIntervalMs
		if (overridePolicy == null && overrideFrameIntervalMs == null) {
			return config.runtimeRenderPolicy
		}
		return RuntimeRenderPolicy(
			policy = overridePolicy ?: config.runtimeRenderPolicy.policy,
			continuousFrameIntervalMs = overrideFrameIntervalMs
				?: config.runtimeRenderPolicy.continuousFrameIntervalMs
		)
	}

	private fun shouldPromoteDynamicContentToContinuous(config: WallpaperConfig): Boolean {
		return config.capabilities.dynamicTextures
	}

	private fun resolveSettingsFrameRate(
		performanceMode: PerformanceMode,
		displayRefreshRateHz: Int
	): Int? {
		return when (performanceMode) {
			PerformanceMode.SMOOTH -> displayRefreshRateHz.coerceIn(
				MIN_SETTINGS_FRAME_RATE_FPS,
				SMOOTH_MAX_FRAME_RATE_FPS
			)
			PerformanceMode.BATTERY -> BATTERY_FRAME_RATE_FPS
			PerformanceMode.AUTO -> AUTO_FRAME_RATE_FPS
		}
	}

	private companion object {
		const val DEFAULT_DISPLAY_REFRESH_RATE_HZ = 60
		const val MIN_SETTINGS_FRAME_RATE_FPS = 30
		const val SMOOTH_MAX_FRAME_RATE_FPS = 90
		const val AUTO_FRAME_RATE_FPS = 30
		const val BATTERY_FRAME_RATE_FPS = 30
		const val DEFAULT_POWER_SAVER_FRAME_INTERVAL_MS = 66L
		const val DEFAULT_THERMAL_MODERATE_FRAME_INTERVAL_MS = 50L
		const val DEFAULT_THERMAL_SEVERE_FRAME_INTERVAL_MS = 100L
		const val THERMAL_STATUS_MODERATE = 2
		const val THERMAL_STATUS_SEVERE = 3
	}
}
