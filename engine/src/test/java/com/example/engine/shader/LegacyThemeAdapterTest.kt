package com.example.engine.shader

import com.example.engine.config.WallpaperConfig
import com.example.engine.renderer.RenderFrameState
import com.example.engine.renderer.RenderMode
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyThemeAdapterTest {

	@Test
	fun focus_mode_uses_realtime_for_shader_time_but_keeps_accelerated_day_progress() {
		val adapter = LegacyThemeAdapter(
			elapsedRealtimeProvider = { 123_456L }
		)
		val state = RenderFrameState(
			mode = RenderMode.FOCUS,
			frameTimeMillis = 86_400_000L,
			dayProgress = 0.75f,
			sunriseMinute = 6 * 60,
			sunsetMinute = 18 * 60
		)

		val resolved = adapter.resolve(
			config = WallpaperConfig.default(id = "sky"),
			state = state
		)

		assertEquals(123.456f, resolved.timeSeconds, 0.0001f)
		assertEquals(0.75f, resolved.timeOfDay, 0.0001f)
	}

	@Test
	fun preview_loop_keeps_accelerated_shader_time() {
		val adapter = LegacyThemeAdapter(
			elapsedRealtimeProvider = { 123_456L }
		)
		val state = RenderFrameState(
			mode = RenderMode.PREVIEW,
			frameTimeMillis = 86_445_000L,
			dayProgress = 0.25f,
			sunriseMinute = 6 * 60,
			sunsetMinute = 18 * 60
		)

		val resolved = adapter.resolve(
			config = WallpaperConfig.default(id = "sky"),
			state = state
		)

		assertEquals(45.0f, resolved.timeSeconds, 0.0001f)
		assertEquals(0.25f, resolved.timeOfDay, 0.0001f)
	}
}
