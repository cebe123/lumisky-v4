package com.example.engine.texture

class TexturePool {
	private val handles = mutableMapOf<String, Int>()

	fun put(key: String, handle: Int) {
		handles[key] = handle
	}

	fun get(key: String): Int? = handles[key]

	fun touch(key: String) {
		handles[key]
	}
}

class TextureLoader {
	fun load(path: String): Int {
		return path.hashCode()
	}
}

class DpiTextureSelector {
	fun choose(basePath: String, dpi: Int): String {
		return if (dpi >= 480) "${basePath}_xxhdpi" else "${basePath}_xhdpi"
	}
}
