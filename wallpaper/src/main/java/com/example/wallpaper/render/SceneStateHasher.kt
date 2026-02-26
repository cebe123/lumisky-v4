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
		var result = 17
		result = 31 * result + input.visible.hashCode()
		result = 31 * result + input.surfaceAttached.hashCode()
		result = 31 * result + input.configFingerprint.hashCode()
		result = 31 * result + input.renderMode.hashCode()
		result = 31 * result + input.sunX
		result = 31 * result + input.sunY
		result = 31 * result + input.moonX
		result = 31 * result + input.moonY
		result = 31 * result + input.nightBlend
		result = 31 * result + input.skyColor
		result = 31 * result + input.flareActive.hashCode()
		return result
	}
}
