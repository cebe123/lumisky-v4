package com.example.wallpaper.service

import com.example.core.settings.PerformanceMode
import com.example.engine.config.RenderPolicy
import com.example.engine.config.RuntimeRenderPolicy
import com.example.engine.config.ServiceRenderPolicy
import com.example.engine.config.WallpaperCapabilities
import com.example.engine.config.WallpaperConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class ServiceRenderPolicyResolverTest {

	@Test
	fun preview_mode_always_uses_vsync_loop() {
		val resolver = ServiceRenderPolicyResolver()

		val resolved = resolver.resolve(
			config = WallpaperConfig.default(id = "preview"),
			previewMode = true,
			visible = true,
			surfaceAttached = true
		)

		assertEquals(WallpaperLoopMode.VSYNC, resolved.loopMode)
	}

	@Test
	fun dynamic_textures_keep_continuous_loop() {
		val resolver = ServiceRenderPolicyResolver()
		val config = WallpaperConfig.default(id = "dynamic_texture").copy(
			runtimeRenderPolicy = RuntimeRenderPolicy(
				policy = RenderPolicy.MINUTE_TICK,
				continuousFrameIntervalMs = 33L
			),
			capabilities = WallpaperCapabilities(
				dynamicMotion = false,
				dynamicTextures = true,
				locationAwareLighting = true,
				supportsCloudLayer = false,
				supportsStarLayer = true
			)
		)

		val resolved = resolver.resolve(
			config = config,
			previewMode = false,
			visible = true,
			surfaceAttached = true
		)

		assertEquals(WallpaperLoopMode.VSYNC, resolved.loopMode)
		assertEquals(33L, resolved.frameIntervalMs)
	}

	@Test
	fun dynamic_motion_without_dynamic_textures_can_remain_minute_tick() {
		val resolver = ServiceRenderPolicyResolver()
		val config = WallpaperConfig.default(id = "dynamic_motion_only").copy(
			runtimeRenderPolicy = RuntimeRenderPolicy(
				policy = RenderPolicy.MINUTE_TICK,
				continuousFrameIntervalMs = 33L
			),
			capabilities = WallpaperCapabilities(
				dynamicMotion = true,
				dynamicTextures = false,
				locationAwareLighting = true,
				supportsCloudLayer = false,
				supportsStarLayer = true
			)
		)

		val resolved = resolver.resolve(
			config = config,
			previewMode = false,
			visible = true,
			surfaceAttached = true
		)

		assertEquals(WallpaperLoopMode.MINUTE_TICK, resolved.loopMode)
	}

	@Test
	fun power_saver_uses_throttled_interval_when_service_policy_allows_it() {
		val resolver = ServiceRenderPolicyResolver(
			isPowerSaveModeProvider = { true }
		)
		val config = WallpaperConfig.default(id = "warrior").copy(
			runtimeRenderPolicy = RuntimeRenderPolicy(
				policy = RenderPolicy.CONTINUOUS,
				continuousFrameIntervalMs = 16L
			),
			serviceRenderPolicy = ServiceRenderPolicy(
				overridePolicy = RenderPolicy.CONTINUOUS,
				overrideFrameIntervalMs = 16L,
				powerSaverFrameIntervalMs = 80L
			)
		)

		val resolved = resolver.resolve(
			config = config,
			previewMode = false,
			visible = true,
			surfaceAttached = true
		)

		assertEquals(WallpaperLoopMode.VSYNC, resolved.loopMode)
		assertEquals(80L, resolved.frameIntervalMs)
	}

	@Test
	fun smooth_mode_caps_service_wallpaper_at_90_fps() {
		val resolver = ServiceRenderPolicyResolver()
		val config = WallpaperConfig.default(id = "smooth").copy(
			runtimeRenderPolicy = RuntimeRenderPolicy(
				policy = RenderPolicy.CONTINUOUS,
				continuousFrameIntervalMs = 16L
			)
		)

		val resolved = resolver.resolve(
			config = config,
			previewMode = false,
			visible = true,
			surfaceAttached = true,
			performanceMode = PerformanceMode.SMOOTH,
			displayRefreshRateHz = 120
		)

		assertEquals(WallpaperLoopMode.VSYNC, resolved.loopMode)
		assertEquals(90, resolved.targetFrameRateFps)
	}

	@Test
	fun smooth_mode_keeps_service_wallpaper_at_least_30_fps() {
		val resolver = ServiceRenderPolicyResolver()
		val config = WallpaperConfig.default(id = "smooth").copy(
			runtimeRenderPolicy = RuntimeRenderPolicy(
				policy = RenderPolicy.CONTINUOUS,
				continuousFrameIntervalMs = 16L
			)
		)

		val resolved = resolver.resolve(
			config = config,
			previewMode = false,
			visible = true,
			surfaceAttached = true,
			performanceMode = PerformanceMode.SMOOTH,
			displayRefreshRateHz = 24
		)

		assertEquals(WallpaperLoopMode.VSYNC, resolved.loopMode)
		assertEquals(30, resolved.targetFrameRateFps)
	}

	@Test
	fun battery_mode_targets_30_fps_for_service_wallpaper() {
		val resolver = ServiceRenderPolicyResolver()
		val config = WallpaperConfig.default(id = "battery").copy(
			runtimeRenderPolicy = RuntimeRenderPolicy(
				policy = RenderPolicy.CONTINUOUS,
				continuousFrameIntervalMs = 16L
			)
		)

		val resolved = resolver.resolve(
			config = config,
			previewMode = false,
			visible = true,
			surfaceAttached = true,
			performanceMode = PerformanceMode.BATTERY,
			displayRefreshRateHz = 120
		)

		assertEquals(WallpaperLoopMode.VSYNC, resolved.loopMode)
		assertEquals(30, resolved.targetFrameRateFps)
	}

	@Test
	fun auto_mode_targets_30_fps_for_dynamic_service_wallpaper() {
		val resolver = ServiceRenderPolicyResolver()
		val config = WallpaperConfig.default(id = "auto").copy(
			runtimeRenderPolicy = RuntimeRenderPolicy(
				policy = RenderPolicy.CONTINUOUS,
				continuousFrameIntervalMs = 16L
			)
		)

		val resolved = resolver.resolve(
			config = config,
			previewMode = false,
			visible = true,
			surfaceAttached = true,
			performanceMode = PerformanceMode.AUTO,
			displayRefreshRateHz = 90
		)

		assertEquals(WallpaperLoopMode.VSYNC, resolved.loopMode)
		assertEquals(30, resolved.targetFrameRateFps)
	}

	@Test
	fun static_policy_disables_background_loops() {
		val resolver = ServiceRenderPolicyResolver()
		val config = WallpaperConfig.default(id = "static").copy(
			runtimeRenderPolicy = RuntimeRenderPolicy(
				policy = RenderPolicy.STATIC,
				continuousFrameIntervalMs = 16L
			)
		)

		val resolved = resolver.resolve(
			config = config,
			previewMode = false,
			visible = true,
			surfaceAttached = true
		)

		assertEquals(WallpaperLoopMode.STATIC, resolved.loopMode)
	}

	@Test
	fun flower_wallpaper_demoted_to_minute_tick_during_day() {
		val resolver = ServiceRenderPolicyResolver()
		val config = WallpaperConfig.default(id = "flower").copy(
			runtimeRenderPolicy = RuntimeRenderPolicy(
				policy = RenderPolicy.CONTINUOUS,
				continuousFrameIntervalMs = 50L
			),
			capabilities = WallpaperCapabilities(
				dynamicMotion = true,
				dynamicTextures = true,
				locationAwareLighting = true,
				supportsCloudLayer = false,
				supportsStarLayer = true
			)
		)

		val resolved = resolver.resolve(
			config = config,
			previewMode = false,
			visible = true,
			surfaceAttached = true,
			isNight = false
		)

		assertEquals(WallpaperLoopMode.MINUTE_TICK, resolved.loopMode)
	}

	@Test
	fun flower_wallpaper_remains_continuous_at_night() {
		val resolver = ServiceRenderPolicyResolver()
		val config = WallpaperConfig.default(id = "flower").copy(
			runtimeRenderPolicy = RuntimeRenderPolicy(
				policy = RenderPolicy.CONTINUOUS,
				continuousFrameIntervalMs = 50L
			),
			capabilities = WallpaperCapabilities(
				dynamicMotion = true,
				dynamicTextures = true,
				locationAwareLighting = true,
				supportsCloudLayer = false,
				supportsStarLayer = true
			)
		)

		val resolved = resolver.resolve(
			config = config,
			previewMode = false,
			visible = true,
			surfaceAttached = true,
			isNight = true
		)

		assertEquals(WallpaperLoopMode.VSYNC, resolved.loopMode)
	}
}
