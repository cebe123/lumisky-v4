package com.example.lumisky.shader

import android.content.Context
import android.util.LruCache
import com.example.core.Logger
import com.example.core.assets.AssetTextLoader
import com.example.core.report.CrashDiagnostics
import com.example.engine.config.WallpaperConfig

object RenderAssetCache {
	private val fragmentLoadLocks = HashMap<String, Any>()
	private val textureLoadLocks = HashMap<String, Any>()
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
		return withLoadLock(
			lockStore = fragmentLoadLocks,
			key = normalized
		) {
			synchronized(fragmentCache) {
				fragmentCache.get(normalized)?.let { return@withLoadLock it }
			}
			val loaded = AssetTextLoader.load(context, normalized) ?: run {
				CrashDiagnostics.recordException(AssetLoadException("Fragment asset load failed path=$normalized"))
				return@withLoadLock null
			}
			synchronized(fragmentCache) {
				fragmentCache.put(normalized, loaded)
			}
			return@withLoadLock loaded
		}
	}

	fun cachedFragment(assetPath: String?): String? {
		val normalized = assetPath?.takeIf { it.isNotBlank() } ?: return null
		synchronized(fragmentCache) {
			return fragmentCache.get(normalized)
		}
	}

	fun loadTextureBytes(
		context: Context,
		assetPath: String?,
		preferPreviewVariant: Boolean = false
	): ByteArray? {
		val normalized = assetPath?.takeIf { it.isNotBlank() } ?: return null
		val cacheLookupKey = cacheLookupKey(
			originalPath = normalized,
			preferPreviewVariant = preferPreviewVariant
		)
		val cachedResolvedPath = getCachedResolvedTexturePath(cacheLookupKey)
		if (cachedResolvedPath != null) {
			Logger.v(TAG, "texture path cache hit original=$normalized resolved=$cachedResolvedPath")
			return loadTextureBytesForResolvedPath(context, cachedResolvedPath)
		}
		Logger.v(TAG, "texture path cache miss original=$normalized")

		val resolvedTexture = resolvePreferredTextureBytes(
			context = context,
			originalPath = normalized,
			preferPreviewVariant = preferPreviewVariant
		) ?: return null
		putCachedResolvedTexturePath(cacheLookupKey, resolvedTexture.path)
		synchronized(textureCache) {
			textureCache.put(resolvedTexture.path, resolvedTexture.bytes)
		}
		return resolvedTexture.bytes
	}

	fun prewarmWallpaper(
		context: Context,
		config: WallpaperConfig,
		preferPreviewVariant: Boolean = false
	) {
		loadFragment(context, config.shader.fragmentAssetPath)
		val uniqueTexturePaths = LinkedHashSet<String>(4)
		addTexturePath(uniqueTexturePaths, config.textures.backgroundTexture)
		addTexturePath(uniqueTexturePaths, config.textures.sunTexture)
		addTexturePath(uniqueTexturePaths, config.textures.moonTexture)
		addTexturePath(uniqueTexturePaths, config.textures.flareTexture)
		config.creator.layers.forEach { layer ->
			if (layer.mediaType == "image") {
				addTexturePath(uniqueTexturePaths, layer.texturePath)
			}
		}
		uniqueTexturePaths.forEach { texturePath ->
			loadTextureBytes(context, texturePath, preferPreviewVariant)
		}
	}

	private fun resolvePreferredTextureBytes(
		context: Context,
		originalPath: String,
		preferPreviewVariant: Boolean
	): ResolvedTextureBytes? {
		if (preferPreviewVariant) {
			previewVariantCandidates(originalPath).forEach { previewVariantPath ->
				val previewBytes = loadTextureBytesForResolvedPath(
					context = context,
					assetPath = previewVariantPath,
					recordFailure = false
				)
				if (previewBytes != null) {
					return ResolvedTextureBytes(previewVariantPath, previewBytes)
				}
			}
		}

		val lower = originalPath.lowercase()
		if (lower.endsWith(".webp")) {
			val bytes = loadTextureBytesForResolvedPath(context, originalPath) ?: return null
			return ResolvedTextureBytes(originalPath, bytes)
		}

		val dot = originalPath.lastIndexOf('.')
		if (dot >= 0) {
			val webpPath = "${originalPath.substring(0, dot)}.webp"
			val webpBytes = loadTextureBytesForResolvedPath(
				context = context,
				assetPath = webpPath,
				recordFailure = false
			)
			if (webpBytes != null) {
				return ResolvedTextureBytes(webpPath, webpBytes)
			}
		}

		val bytes = loadTextureBytesForResolvedPath(context, originalPath) ?: return null
		return ResolvedTextureBytes(originalPath, bytes)
	}

	private fun loadTextureBytesForResolvedPath(
		context: Context,
		assetPath: String,
		recordFailure: Boolean = true
	): ByteArray? {
		synchronized(textureCache) {
			textureCache.get(assetPath)?.let {
				Logger.v(TAG, "texture byte cache hit path=$assetPath size=${it.size}")
				return it
			}
		}
		Logger.v(TAG, "texture byte cache miss path=$assetPath")
		return withLoadLock(
			lockStore = textureLoadLocks,
			key = assetPath
		) {
			synchronized(textureCache) {
				textureCache.get(assetPath)?.let {
					Logger.v(TAG, "texture byte cache hit path=$assetPath size=${it.size}")
					return@withLoadLock it
				}
			}
			val loaded = runCatching {
				context.assets.open(assetPath).use { it.readBytes() }
			}.getOrNull() ?: run {
				if (recordFailure) {
					CrashDiagnostics.recordException(TextureAssetLoadException("Texture asset read failed path=$assetPath"))
				}
				return@withLoadLock null
			}
			Logger.v(TAG, "texture asset read path=$assetPath size=${loaded.size}")
			synchronized(textureCache) {
				textureCache.put(assetPath, loaded)
			}
			return@withLoadLock loaded
		}
	}

	private fun previewVariantPath(originalPath: String): String? {
		val dotIndex = originalPath.lastIndexOf('.')
		if (dotIndex <= 0) return null
		return "${originalPath.substring(0, dotIndex)}_preview${originalPath.substring(dotIndex)}"
	}

	private fun previewVariantCandidates(originalPath: String): List<String> {
		val candidates = LinkedHashSet<String>(2)
		previewVariantPath(originalPath)?.let(candidates::add)
		val dotIndex = originalPath.lastIndexOf('.')
		if (dotIndex > 0) {
			candidates.add("${originalPath.substring(0, dotIndex)}_preview.webp")
		}
		return candidates.toList()
	}

	private fun addTexturePath(
		target: LinkedHashSet<String>,
		assetPath: String?
	) {
		assetPath?.takeIf { it.isNotBlank() }?.let(target::add)
	}

	private fun cacheLookupKey(
		originalPath: String,
		preferPreviewVariant: Boolean
	): String {
		return if (preferPreviewVariant) {
			"preview::$originalPath"
		} else {
			"full::$originalPath"
		}
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

	private fun <T> withLoadLock(
		lockStore: HashMap<String, Any>,
		key: String,
		block: () -> T
	): T {
		val lock = synchronized(lockStore) {
			lockStore.getOrPut(key) { Any() }
		}
		return synchronized(lock) {
			try {
				block()
			} finally {
				synchronized(lockStore) {
					if (lockStore[key] === lock) {
						lockStore.remove(key)
					}
				}
			}
		}
	}

	private data class ResolvedTextureBytes(
		val path: String,
		val bytes: ByteArray
	)

	private class AssetLoadException(message: String) : RuntimeException(message)
	private class TextureAssetLoadException(message: String) : RuntimeException(message)

	private const val MAX_FRAGMENT_ENTRIES = 24
	private const val MAX_RESOLVED_TEXTURE_PATH_ENTRIES = 256
	private const val MAX_TEXTURE_CACHE_BYTES = 28 * 1024 * 1024
	private const val TAG = "RenderAssetCache"
}

