package com.example.engine.shader

import com.example.engine.config.WallpaperConfig
import com.example.engine.renderer.RenderFrameState

internal data class PreviewEffectState(
	val cloudOffset: Float,
	val cloudAlpha: Float,
	val starsEnabled: Boolean
)

internal class PreviewEffectAdapter {

	fun resolve(
		config: WallpaperConfig,
		state: RenderFrameState
	): PreviewEffectState {
		val starsEnabled = config.effects.stars?.enabled ?: state.starsEnabled
		val cloudEffect = config.effects.clouds
		val cloudAlpha = when {
			cloudEffect?.enabled == false -> 0f
			config.shader.uniformOverrides.cloudAlpha != null ->
				config.shader.uniformOverrides.cloudAlpha
			cloudEffect?.enabled == true -> cloudEffect.intensity
			else -> 0f
		}.coerceIn(0f, 1f)
		val cloudOffsetBase = config.shader.uniformOverrides.cloudOffset ?: 0f
		val cloudOffset = if (cloudEffect?.enabled == true || cloudOffsetBase != 0f) {
			val seconds = (state.frameTimeMillis % CLOUD_TIME_WINDOW_MS).toFloat() / 1000f
			cloudOffsetBase + (seconds * (cloudEffect?.speed ?: 0f) * CLOUD_SCROLL_SCALE)
		} else {
			0f
		}
		return PreviewEffectState(
			cloudOffset = cloudOffset,
			cloudAlpha = cloudAlpha,
			starsEnabled = starsEnabled
		)
	}

	private companion object {
		const val CLOUD_TIME_WINDOW_MS = 120_000L
		const val CLOUD_SCROLL_SCALE = 0.02f
	}
}
