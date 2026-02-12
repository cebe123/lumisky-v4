package com.example.wallpaper.render

data class SceneStateInput(
	val minute: Long,
	val visible: Boolean,
	val surfaceAttached: Boolean,
	val configFingerprint: String,
	val renderMode: String
)

class SceneStateHasher {
	fun compute(input: SceneStateInput): Int {
		return listOf(
			input.minute,
			input.visible,
			input.surfaceAttached,
			input.configFingerprint,
			input.renderMode
		).hashCode()
	}
}
