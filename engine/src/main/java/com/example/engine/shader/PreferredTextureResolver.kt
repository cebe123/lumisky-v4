package com.example.engine.shader

internal data class ResolvedTextureAsset(
	val path: String,
	val bytes: ByteArray
)

internal class PreferredTextureResolver(
	private val maxEntries: Int = DEFAULT_MAX_ENTRIES
) {
	private val resolvedPathCache = object : LinkedHashMap<String, String>(maxEntries, 0.75f, true) {
		override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean {
			return size > maxEntries
		}
	}

	fun resolve(
		originalPath: String,
		loader: (String) -> ByteArray?
	): ResolvedTextureAsset? {
		val cachedResolvedPath = synchronized(resolvedPathCache) {
			resolvedPathCache[originalPath]
		}
		if (cachedResolvedPath != null) {
			readNonEmptyBytes(cachedResolvedPath, loader)?.let { bytes ->
				return ResolvedTextureAsset(path = cachedResolvedPath, bytes = bytes)
			}
			synchronized(resolvedPathCache) {
				resolvedPathCache.remove(originalPath)
			}
		}

		val lower = originalPath.lowercase()
		if (lower.endsWith(".webp")) {
			return resolveAndCache(
				originalPath = originalPath,
				resolvedPath = originalPath,
				loader = loader
			)
		}

		val extIndex = originalPath.lastIndexOf('.')
		if (extIndex >= 0) {
			val webpCandidate = "${originalPath.substring(0, extIndex)}.webp"
			resolveAndCache(
				originalPath = originalPath,
				resolvedPath = webpCandidate,
				loader = loader
			)?.let { return it }
		}

		return resolveAndCache(
			originalPath = originalPath,
			resolvedPath = originalPath,
			loader = loader
		)
	}

	private fun resolveAndCache(
		originalPath: String,
		resolvedPath: String,
		loader: (String) -> ByteArray?
	): ResolvedTextureAsset? {
		val bytes = readNonEmptyBytes(resolvedPath, loader) ?: return null
		synchronized(resolvedPathCache) {
			resolvedPathCache[originalPath] = resolvedPath
			if (originalPath != resolvedPath) {
				resolvedPathCache[resolvedPath] = resolvedPath
			}
		}
		return ResolvedTextureAsset(path = resolvedPath, bytes = bytes)
	}

	private fun readNonEmptyBytes(
		path: String,
		loader: (String) -> ByteArray?
	): ByteArray? {
		return runCatching { loader(path) }.getOrNull()
			?.takeIf { it.isNotEmpty() }
	}

	private companion object {
		private const val DEFAULT_MAX_ENTRIES = 256
	}
}
