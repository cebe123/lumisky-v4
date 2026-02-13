package com.example.wallpaper.render

data class SceneStateInput(
	val visible: Boolean,
	val surfaceAttached: Boolean,
	val configFingerprint: String,
	val renderMode: String,
	val sunX: Int,
	val sunY: Int,
	val moonX: Int,
	val moonY: Int,
	val nightBlend: Int,
	val skyColor: Int,
	val flareActive: Boolean
)

class SceneStateHasher {
	fun compute(input: SceneStateInput): Int {
		return listOf(
			input.visible,
			input.surfaceAttached,
			input.configFingerprint,
			input.renderMode,
			input.sunX,
			input.sunY,
			input.moonX,
			input.moonY,
			input.nightBlend,
			input.skyColor,
			input.flareActive
		).hashCode()
	}
}
