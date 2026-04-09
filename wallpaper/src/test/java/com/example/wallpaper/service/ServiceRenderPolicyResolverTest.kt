package com.example.wallpaper.service

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
	fun dynamic_capability_promotes_minute_tick_to_continuous_loop() {
		val resolver = ServiceRenderPolicyResolver()
		val config = WallpaperConfig.default(id = "dynamic").copy(
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

		assertEquals(WallpaperLoopMode.VSYNC, resolved.loopMode)
		assertEquals(33L, resolved.frameIntervalMs)
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
}
