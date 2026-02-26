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
