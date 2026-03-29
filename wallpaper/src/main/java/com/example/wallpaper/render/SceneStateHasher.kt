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
	fun compute(
		visible: Boolean,
		surfaceAttached: Boolean,
		configFingerprint: String,
		renderMode: String,
		sunX: Int,
		sunY: Int,
		moonX: Int,
		moonY: Int,
		nightBlend: Int,
		skyColor: Int,
		flareActive: Boolean
	): Int {
		var result = 17
		result = 31 * result + visible.hashCode()
		result = 31 * result + surfaceAttached.hashCode()
		result = 31 * result + configFingerprint.hashCode()
		result = 31 * result + renderMode.hashCode()
		result = 31 * result + sunX
		result = 31 * result + sunY
		result = 31 * result + moonX
		result = 31 * result + moonY
		result = 31 * result + nightBlend
		result = 31 * result + skyColor
		result = 31 * result + flareActive.hashCode()
		return result
	}

	fun compute(input: SceneStateInput): Int {
		return compute(
			visible = input.visible,
			surfaceAttached = input.surfaceAttached,
			configFingerprint = input.configFingerprint,
			renderMode = input.renderMode,
			sunX = input.sunX,
			sunY = input.sunY,
			moonX = input.moonX,
			moonY = input.moonY,
			nightBlend = input.nightBlend,
			skyColor = input.skyColor,
			flareActive = input.flareActive
		)
	}
}
