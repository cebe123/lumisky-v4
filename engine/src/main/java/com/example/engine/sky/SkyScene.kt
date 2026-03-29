package com.example.engine.sky

class Vec2(
	var x: Float = 0f,
	var y: Float = 0f
) {
	fun set(
		x: Float,
		y: Float
	): Vec2 {
		this.x = x
		this.y = y
		return this
	}
}

class SkyScene(
	val sun: Vec2 = Vec2(),
	val moon: Vec2 = Vec2(),
	var skyColor: Int = 0xFF000000.toInt()
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
