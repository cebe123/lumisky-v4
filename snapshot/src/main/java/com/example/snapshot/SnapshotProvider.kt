package com.example.snapshot

import android.content.Context
import android.os.Process
import android.os.SystemClock
import com.example.engine.config.WallpaperConfig
import com.example.snapshot.provider.SnapshotProvider as InternalSnapshotProvider
import com.example.snapshot.worker.SnapshotWorker
import kotlin.concurrent.thread

class SnapshotProvider(
	context: Context,
	private val delegate: InternalSnapshotProvider = InternalSnapshotProvider(context)
) {
	private val appContext = context.applicationContext
	@Volatile
	private var snapshotUpdatedListener: ((String) -> Unit)? = null
	private val worker = SnapshotWorker(
		snapshotProvider = delegate,
		appContext = appContext,
		onSnapshotGenerated = { wallpaperId ->
			snapshotUpdatedListener?.invoke(wallpaperId)
		}
	)
	private val generationLock = Any()
	@Volatile
	private var generationRunning: Boolean = false

	fun warmUp() {
		delegate.warmUp()
	}

	fun getSnapshotPath(wallpaperId: String): String? {
		return delegate.getSnapshotPath(wallpaperId)
	}

	fun getSnapshotPath(config: WallpaperConfig): String? {
		return delegate.getSnapshotPath(snapshotKey(config))
	}

	fun generateSnapshots(configs: List<WallpaperConfig>) {
		if (configs.isEmpty()) return
		val pending = configs
			.distinctBy { it.id }
			.filter { delegate.getSnapshotPath(snapshotKey(it)) == null }
		if (pending.isEmpty()) return

		val shouldStart = synchronized(generationLock) {
			worker.enqueue(pending)
			if (generationRunning) {
				false
			} else {
				generationRunning = true
				true
			}
		}

		if (!shouldStart) return
		runWorkerLoopInBackground()
	}

	fun accelerateSnapshotGeneration(configs: List<WallpaperConfig>) {
		if (configs.isEmpty()) return
		val pending = configs
			.distinctBy { it.id }
			.filter { delegate.getSnapshotPath(snapshotKey(it)) == null }
		if (pending.isEmpty()) return
		val shouldStart = synchronized(generationLock) {
			worker.boost(12_000L)
			worker.enqueue(pending)
			if (generationRunning) {
				false
			} else {
				generationRunning = true
				true
			}
		}
		if (shouldStart) {
			runWorkerLoopInBackground()
		}
	}

	fun hasMissingSnapshots(configs: List<WallpaperConfig>): Boolean {
		return configs.any { delegate.getSnapshotPath(snapshotKey(it)) == null }
	}

	fun generateSnapshotsBlocking(
		configs: List<WallpaperConfig>,
		timeoutMs: Long = 5_000L
	): Boolean {
		if (configs.isEmpty()) return true
		val pending = configs
			.distinctBy { it.id }
			.filter { delegate.getSnapshotPath(snapshotKey(it)) == null }
		if (pending.isEmpty()) return true

		val runInline = synchronized(generationLock) {
			worker.boost(timeoutMs + 2_000L)
			worker.enqueue(pending)
			if (generationRunning) {
				false
			} else {
				generationRunning = true
				true
			}
		}

		val deadlineMs = SystemClock.elapsedRealtime() + timeoutMs.coerceAtLeast(250L)
		if (!runInline) {
			while (SystemClock.elapsedRealtime() < deadlineMs) {
				if (!hasMissingSnapshots(configs)) return true
				Thread.sleep(WAIT_POLL_MS)
			}
			return !hasMissingSnapshots(configs)
		}

		while (SystemClock.elapsedRealtime() < deadlineMs) {
			val progressed = worker.runNext()
			if (!progressed) break
			if (!hasMissingSnapshots(configs)) break
			Thread.sleep(worker.nextDelayMs().coerceAtMost(MAX_BLOCKING_DELAY_MS))
		}

		val completed = !hasMissingSnapshots(configs)
		synchronized(generationLock) {
			generationRunning = false
		}
		if (!completed) {
			generateSnapshots(configs)
		}
		return completed
	}

	private fun runWorkerLoopInBackground() {
		thread(start = true, isDaemon = true, name = "SnapshotGenerate") {
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
			while (true) {
				if (worker.runNext()) {
					try {
						Thread.sleep(worker.nextDelayMs())
					} catch (_: InterruptedException) {
						Thread.currentThread().interrupt()
						break
					}
					continue
				}

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
		snapshotUpdatedListener = null
		worker.release()
	}

	fun setOnSnapshotUpdatedListener(listener: ((String) -> Unit)?) {
		snapshotUpdatedListener = listener
	}

	private fun snapshotKey(config: WallpaperConfig): String {
		return buildString {
			append(SNAPSHOT_KEY_VERSION)
			append('|')
			append(config.id)
			append('|')
			append(config.shader.fragmentAssetPath ?: "")
			append('|')
			append(config.shader.mode)
			append('|')
			append(config.textures.backgroundTexture ?: "")
			append('|')
			append(config.textures.sunTexture)
			append('|')
			append(config.textures.moonTexture)
			append('|')
			append(config.textures.flareTexture ?: "")
			append('|')
			append(config.horizon.offset)
			append('|')
			append(config.peakY)
			append('|')
			append(config.belowHorizonOffset)
			append('|')
			append(config.daylight.sunriseMinute)
			append('|')
			append(config.daylight.sunsetMinute)
		}
	}

	companion object {
		private const val SNAPSHOT_KEY_VERSION = "snapshot_v2"
		private const val WAIT_POLL_MS = 40L
		private const val MAX_BLOCKING_DELAY_MS = 20L
	}
}
