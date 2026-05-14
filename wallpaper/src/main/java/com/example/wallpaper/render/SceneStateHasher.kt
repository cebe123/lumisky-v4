package com.example.wallpaper.render

class SceneStateHasher {
	fun compute(
		visible: Boolean,
		surfaceAttached: Boolean,
		configFingerprintHash: Int,
		renderModeOrdinal: Int,
		sunX: Int,
		sunY: Int,
		moonX: Int,
		moonY: Int,
		nightBlend: Int,
		skyColor: Int,
		flareActive: Boolean
	): Int {
		var result = 17
		result = 31 * result + visible.toHashToken()
		result = 31 * result + surfaceAttached.toHashToken()
		result = 31 * result + configFingerprintHash
		result = 31 * result + renderModeOrdinal
		result = 31 * result + sunX
		result = 31 * result + sunY
		result = 31 * result + moonX
		result = 31 * result + moonY
		result = 31 * result + nightBlend
		result = 31 * result + skyColor
		result = 31 * result + flareActive.toHashToken()
		return result
	}

	private fun Boolean.toHashToken(): Int = if (this) 1 else 0
}
