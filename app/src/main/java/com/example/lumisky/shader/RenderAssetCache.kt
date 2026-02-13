package com.example.lumisky.shader

import android.content.Context
import android.util.LruCache
import com.example.engine.config.WallpaperConfig

object RenderAssetCache {
	private val fragmentCache = object : LruCache<String, String>(MAX_FRAGMENT_ENTRIES) {}
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
		val resolvedPath = resolvePreferredTexturePath(context, normalized)
		synchronized(textureCache) {
			textureCache.get(resolvedPath)?.let { return it }
		}
		val loaded = runCatching {
			context.assets.open(resolvedPath).use { it.readBytes() }
		}.getOrNull() ?: return null
		synchronized(textureCache) {
			textureCache.put(resolvedPath, loaded)
		}
		return loaded
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

	private fun resolvePreferredTexturePath(
		context: Context,
		originalPath: String
	): String {
		val lower = originalPath.lowercase()
		if (lower.endsWith(".webp")) return originalPath
		val dot = originalPath.lastIndexOf('.')
		if (dot < 0) return originalPath
		val webpPath = "${originalPath.substring(0, dot)}.webp"
		return if (assetExists(context, webpPath)) webpPath else originalPath
	}

	private fun assetExists(
		context: Context,
		assetPath: String
	): Boolean {
		return runCatching {
			context.assets.open(assetPath).use { input ->
				input.read()
			}
			true
		}.getOrDefault(false)
	}

	private const val MAX_FRAGMENT_ENTRIES = 24
	private const val MAX_TEXTURE_CACHE_BYTES = 28 * 1024 * 1024
}

