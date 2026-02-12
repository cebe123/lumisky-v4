package com.example.snapshot.worker

import com.example.engine.SkyEngine
import com.example.engine.config.WallpaperConfig
import com.example.engine.renderer.RenderMode
import com.example.engine.time.TimeManager
import com.example.core.Logger
import com.example.snapshot.encoder.SnapshotBitmapFactory
import com.example.snapshot.encoder.WebpEncoder
import com.example.snapshot.provider.SnapshotProvider

class SnapshotWorker(
	private val snapshotProvider: SnapshotProvider,
	private val skyEngine: SkyEngine = SkyEngine(),
	private val bitmapFactory: SnapshotBitmapFactory = SnapshotBitmapFactory(),
	private val encoder: WebpEncoder = WebpEncoder()
) {
	private val queue = ArrayDeque<WallpaperConfig>()
	private var generatedCount: Int = 0

	init {
		skyEngine.init()
		skyEngine.setRenderMode(RenderMode.SNAPSHOT)
	}

	@Synchronized
	fun enqueue(configs: List<WallpaperConfig>) {
		// Always restart from the first item of the latest list.
		queue.clear()
		queue.addAll(configs)
	}

	@Synchronized
	fun runNext(): Boolean {
		val config = queue.removeFirstOrNull() ?: return false
		val startNs = System.nanoTime()
		skyEngine.setConfig(config)

		val noonState = skyEngine.sampleAtDayProgress(
			dayProgress = TimeManager.NOON_PROGRESS,
			mode = RenderMode.SNAPSHOT,
			force = true
		) ?: return true

		val bitmap = bitmapFactory.create(noonState)
		try {
			val encoded = encoder.encode(bitmap) ?: return true
			snapshotProvider.putSnapshot(config.id, encoded)
			generatedCount += 1
			if (generatedCount % 20 == 0) {
				val elapsedMs = (System.nanoTime() - startNs) / 1_000_000.0
				Logger.d(
					"SnapshotWorker",
					"generated=$generatedCount last=${config.id} tookMs=${
						String.format(java.util.Locale.US, "%.2f", elapsedMs)
					}"
				)
			}
		} finally {
			bitmap.recycle()
		}
		return true
	}

	@Synchronized
	fun hasPending(): Boolean = queue.isNotEmpty()

	fun release() {
		skyEngine.release()
	}
}
