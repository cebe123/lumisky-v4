package com.example.engine.renderer

enum class RenderMode {
	IDLE,
	SNAPSHOT,
	FOCUS,
	PREVIEW,
	WALLPAPER_SERVICE
}

class RenderModeController(initialMode: RenderMode = RenderMode.IDLE) {
	var mode: RenderMode = initialMode
		private set

	val isRenderingEnabled: Boolean
		get() = mode != RenderMode.IDLE

	fun switchTo(next: RenderMode) {
		mode = next
	}
}
