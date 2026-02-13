package com.example.engine.texture

import java.util.LinkedHashMap

class TexturePool(
	private val maxEntries: Int = DEFAULT_MAX_ENTRIES
) {
	private val handles = object : LinkedHashMap<String, Int>(16, 0.75f, true) {
		override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Int>?): Boolean {
			return size > maxEntries
		}
	}

	@Synchronized
	fun put(key: String, handle: Int) {
		handles[key] = handle
	}

	@Synchronized
	fun get(key: String): Int? = handles[key]

	@Synchronized
	fun touch(key: String) {
		handles[key]
	}

	@Synchronized
	fun size(): Int = handles.size

	@Synchronized
	fun clear() {
		handles.clear()
	}

	companion object {
		private const val DEFAULT_MAX_ENTRIES = 96
	}
}

class TextureLoader {
	fun load(path: String): Int {
		return path.hashCode()
	}
}

enum class DeviceTier {
	LOW,
	MID,
	HIGH
}

class DpiTextureSelector {
	fun choose(
		basePath: String,
		dpi: Int,
		deviceTier: DeviceTier = DeviceTier.MID
	): String {
		return "${basePath}_${chooseTextureSize(dpi, deviceTier)}"
	}

	fun chooseTextureSize(
		dpi: Int,
		deviceTier: DeviceTier = DeviceTier.MID
	): Int {
		val base = when {
			dpi >= 520 -> 2048
			dpi >= 360 -> 1024
			else -> 512
		}
		return when (deviceTier) {
			DeviceTier.HIGH -> base
			DeviceTier.MID -> if (base == 2048) 1024 else base
			DeviceTier.LOW -> 512
		}
	}
}
