package com.example.lumisky.shader

import android.content.Context
import android.util.LruCache
import com.example.engine.config.WallpaperConfig

object RenderAssetCache {
	private val fragmentCache = object : LruCache<String, String>(MAX_FRAGMENT_ENTRIES) {}
	private val resolvedTexturePathCache = object : LruCache<String, String>(MAX_RESOLVED_TEXTURE_PATH_ENTRIES) {}
	private val textureCache = object : LruCache<String, ByteArray>(MAX_TEXTURE_CACHE_BYTES) {
		override fun sizeOf(key: String, value: ByteArray): Int = value.size
	}

	fun loadFragment(
		context: Context,
		assetPath: String?
	): String? {
		val normalized = assetPath?.takeIf { it.isNotBlank() } ?: return null
		synchronized(fragmentCache) {
			fragmentCache.get(normalized)?.let { return it }
		}
		val loaded = runCatching {
			context.assets.open(normalized).bufferedReader().use { it.readText() }
		}.getOrNull() ?: return null
		synchronized(fragmentCache) {
			fragmentCache.put(normalized, loaded)
		}
		return loaded
	}

	fun loadTextureBytes(
		context: Context,
		assetPath: String?
	): ByteArray? {
		val normalized = assetPath?.takeIf { it.isNotBlank() } ?: return null
		val cachedResolvedPath = getCachedResolvedTexturePath(normalized)
		if (cachedResolvedPath != null) {
			return loadTextureBytesForResolvedPath(context, cachedResolvedPath)
		}

		val resolvedTexture = resolvePreferredTextureBytes(context, normalized) ?: return null
		putCachedResolvedTexturePath(normalized, resolvedTexture.path)
		synchronized(textureCache) {
			textureCache.put(resolvedTexture.path, resolvedTexture.bytes)
		}
		return resolvedTexture.bytes
	}

	fun prewarmWallpaper(
		context: Context,
		config: WallpaperConfig
	) {
		loadFragment(context, config.shader.fragmentAssetPath)
		loadTextureBytes(context, config.textures.backgroundTexture)
		loadTextureBytes(context, config.textures.sunTexture)
		loadTextureBytes(context, config.textures.moonTexture)
		loadTextureBytes(context, config.textures.flareTexture)
	}

	private fun resolvePreferredTextureBytes(
		context: Context,
		originalPath: String
	): ResolvedTextureBytes? {
		val lower = originalPath.lowercase()
		if (lower.endsWith(".webp")) {
			val bytes = loadTextureBytesForResolvedPath(context, originalPath) ?: return null
			return ResolvedTextureBytes(originalPath, bytes)
		}

		val dot = originalPath.lastIndexOf('.')
		if (dot >= 0) {
			val webpPath = "${originalPath.substring(0, dot)}.webp"
			val webpBytes = loadTextureBytesForResolvedPath(context, webpPath)
			if (webpBytes != null) {
				return ResolvedTextureBytes(webpPath, webpBytes)
			}
		}

		val bytes = loadTextureBytesForResolvedPath(context, originalPath) ?: return null
		return ResolvedTextureBytes(originalPath, bytes)
	}

	private fun loadTextureBytesForResolvedPath(
		context: Context,
		assetPath: String
	): ByteArray? {
		synchronized(textureCache) {
			textureCache.get(assetPath)?.let { return it }
		}
		val loaded = runCatching {
			context.assets.open(assetPath).use { it.readBytes() }
		}.getOrNull() ?: return null
		synchronized(textureCache) {
			textureCache.put(assetPath, loaded)
		}
		return loaded
	}

	private fun getCachedResolvedTexturePath(originalPath: String): String? {
		synchronized(resolvedTexturePathCache) {
			return resolvedTexturePathCache.get(originalPath)
		}
	}

	private fun putCachedResolvedTexturePath(
		originalPath: String,
		resolvedPath: String
	) {
		synchronized(resolvedTexturePathCache) {
			resolvedTexturePathCache.put(originalPath, resolvedPath)
		}
		if (originalPath != resolvedPath) {
			synchronized(resolvedTexturePathCache) {
				if (resolvedTexturePathCache.get(resolvedPath) == null) {
					resolvedTexturePathCache.put(resolvedPath, resolvedPath)
				}
			}
		}
	}

	private data class ResolvedTextureBytes(
		val path: String,
		val bytes: ByteArray
	)

	private const val MAX_FRAGMENT_ENTRIES = 24
	private const val MAX_RESOLVED_TEXTURE_PATH_ENTRIES = 256
	private const val MAX_TEXTURE_CACHE_BYTES = 28 * 1024 * 1024
}

