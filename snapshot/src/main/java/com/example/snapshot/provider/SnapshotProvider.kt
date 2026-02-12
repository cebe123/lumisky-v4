package com.example.snapshot.provider

import android.content.Context
import com.example.snapshot.cache.SnapshotDiskCache
import com.example.snapshot.encoder.EncodedSnapshot

class SnapshotProvider(
	context: Context,
	private val diskCache: SnapshotDiskCache = SnapshotDiskCache(context = context)
) {
	fun warmUp() {
		// Disk cache directory is initialized eagerly in SnapshotDiskCache constructor.
	}

	fun getSnapshotPath(wallpaperId: String): String? {
		return diskCache.get(wallpaperId)
	}

	fun putSnapshot(wallpaperId: String, snapshot: EncodedSnapshot): String {
		return diskCache.put(wallpaperId, snapshot.bytes)
	}

	fun putSnapshotBytes(wallpaperId: String, bytes: ByteArray): String {
		return diskCache.put(wallpaperId, bytes)
	}
}
