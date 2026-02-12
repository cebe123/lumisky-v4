package com.example.snapshot

import android.content.Context
import com.example.engine.config.WallpaperConfig
import com.example.snapshot.provider.SnapshotProvider as InternalSnapshotProvider
import com.example.snapshot.worker.SnapshotWorker
import kotlin.concurrent.thread

class SnapshotProvider(
	context: Context,
	private val delegate: InternalSnapshotProvider = InternalSnapshotProvider(context)
) {
	private val worker = SnapshotWorker(snapshotProvider = delegate)
	private val generationLock = Any()
	@Volatile
	private var generationRunning: Boolean = false
	private var lastCatalogHash: Int? = null

	fun warmUp() {
		delegate.warmUp()
	}

	fun getSnapshotPath(wallpaperId: String): String? {
		return delegate.getSnapshotPath(wallpaperId)
	}

	fun generateSnapshots(configs: List<WallpaperConfig>) {
		if (configs.isEmpty()) return
		val catalogHash = configs.hashCode()
		val shouldStart = synchronized(generationLock) {
			if (lastCatalogHash == catalogHash) {
				return@synchronized false
			}
			lastCatalogHash = catalogHash
			worker.enqueue(configs)
			if (generationRunning) {
				false
			} else {
				generationRunning = true
				true
			}
		}

		if (!shouldStart) return

		thread(start = true, isDaemon = true, name = "SnapshotGenerate") {
			while (true) {
				if (worker.runNext()) continue

				val stop = synchronized(generationLock) {
					if (worker.hasPending()) {
						false
					} else {
						generationRunning = false
						true
					}
				}
				if (stop) break
			}
		}
	}

	fun release() {
		synchronized(generationLock) {
			lastCatalogHash = null
		}
		worker.release()
	}
}
