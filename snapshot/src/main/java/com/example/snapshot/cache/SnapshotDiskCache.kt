package com.example.snapshot.cache

import android.content.Context
import java.io.File
import java.security.MessageDigest

class SnapshotDiskCache(
	context: Context,
	private val maxEntries: Int = 512
) {
	private val lock = Any()
	private val cacheDir: File = File(context.filesDir, CACHE_FOLDER).apply { mkdirs() }

	fun get(key: String): String? {
		synchronized(lock) {
			val file = cacheFile(key)
			if (!file.exists()) return null
			return file.absolutePath
		}
	}

	fun put(key: String, bytes: ByteArray): String {
		synchronized(lock) {
			val file = cacheFile(key)
			file.outputStream().use { stream ->
				stream.write(bytes)
				stream.flush()
			}
			file.setLastModified(System.currentTimeMillis())
			trimToSizeLocked()
			return file.absolutePath
		}
	}

	fun clear() {
		synchronized(lock) {
			cacheDir.listFiles()?.forEach { it.delete() }
		}
	}

	private fun trimToSizeLocked() {
		val files = cacheDir.listFiles()?.toList() ?: return
		if (files.size <= maxEntries) return
		val entries = files.map { file ->
			CacheEntry(
				file = file,
				lastModified = file.lastModified()
			)
		}
		val sorted = entries.sortedWith(
			compareByDescending<CacheEntry> { it.lastModified }
				.thenBy { it.file.name }
		)
		sorted.drop(maxEntries).forEach { it.file.delete() }
	}

	private fun cacheFile(key: String): File {
		val hash = sha1(key)
		return File(cacheDir, "$hash.webp")
	}

	private fun sha1(source: String): String {
		val digest = MessageDigest.getInstance("SHA-1").digest(source.toByteArray(Charsets.UTF_8))
		return digest.joinToString("") { "%02x".format(it) }
	}

	companion object {
		private const val CACHE_FOLDER = "wallpaper_snapshots"
	}

	private data class CacheEntry(
		val file: File,
		val lastModified: Long
	)
}

class LruSnapshotManager(
	private val diskCache: SnapshotDiskCache
) {
	fun load(key: String): String? = diskCache.get(key)

	fun save(key: String, bytes: ByteArray): String = diskCache.put(key, bytes)
}
