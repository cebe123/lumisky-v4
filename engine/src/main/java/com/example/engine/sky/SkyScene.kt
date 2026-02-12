package com.example.engine.sky

data class Vec2(val x: Float, val y: Float)

data class SkyScene(
	val sun: Vec2 = Vec2(0f, 0f),
	val moon: Vec2 = Vec2(0f, 0f),
	val skyColor: Int = 0xFF000000.toInt()
)

class SkyGradient {
	fun sample(progress: Float): Int {
		val channel = (255f * progress.coerceIn(0f, 1f)).toInt()
		return (0xFF shl 24) or (channel shl 16) or (channel shl 8) or 255
	}
}

class SkyColorBlender {
	fun blend(base: Int, overlay: Int): Int {
		return if (overlay == 0) base else overlay
	}
}
