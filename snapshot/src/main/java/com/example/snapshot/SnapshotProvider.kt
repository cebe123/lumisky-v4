package com.example.snapshot

import android.content.Context
import android.os.Process
import android.os.SystemClock
import com.example.core.Logger
import com.example.core.settings.PerformanceMode
import com.example.engine.config.WallpaperConfig
import com.example.snapshot.provider.SnapshotProvider as InternalSnapshotProvider
import com.example.snapshot.worker.SnapshotWorker
import kotlin.concurrent.thread

class SnapshotProvider(
	context: Context,
	private val delegate: InternalSnapshotProvider = InternalSnapshotProvider(context)
) {
	private val tag = "SnapshotProvider"
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

	fun setPerformanceMode(mode: PerformanceMode) {
		worker.setPerformanceMode(mode)
		Logger.event(tag, "performance_mode_set", "mode" to mode)
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
		Logger.event(tag, "enqueue_generate", "requested" to configs.size, "pending" to pending.size)

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
		Logger.d(tag, "starting background snapshot worker")
		runWorkerLoopInBackground()
	}

	fun accelerateSnapshotGeneration(configs: List<WallpaperConfig>) {
		if (configs.isEmpty()) return
		val pending = configs
			.distinctBy { it.id }
			.filter { delegate.getSnapshotPath(snapshotKey(it)) == null }
		if (pending.isEmpty()) return
		Logger.event(tag, "enqueue_accelerate", "requested" to configs.size, "pending" to pending.size)
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
			Logger.d(tag, "starting worker from accelerate")
			runWorkerLoopInBackground()
		}
	}

	fun prioritizeSnapshotGeneration(configs: List<WallpaperConfig>) {
		if (configs.isEmpty()) return
		val pending = configs
			.distinctBy { it.id }
			.filter { delegate.getSnapshotPath(snapshotKey(it)) == null }
		if (pending.isEmpty()) return
		Logger.event(tag, "enqueue_prioritize", "requested" to configs.size, "pending" to pending.size)

		val shouldStart = synchronized(generationLock) {
			worker.prioritize(pending)
			worker.boost(10_000L)
			if (generationRunning) {
				false
			} else {
				generationRunning = true
				true
			}
		}
		if (shouldStart) {
			Logger.d(tag, "starting worker from prioritize")
			runWorkerLoopInBackground()
		}
	}

	fun hasMissingSnapshots(configs: List<WallpaperConfig>): Boolean {
		return configs.any { delegate.getSnapshotPath(snapshotKey(it)) == null }
	}

	fun snapshotProgress(configs: List<WallpaperConfig>): Float {
		val uniqueKeys = configs
			.asSequence()
			.map { snapshotKey(it) }
			.distinct()
			.toList()
		if (uniqueKeys.isEmpty()) return 1f
		val available = uniqueKeys.count { key -> delegate.getSnapshotPath(key) != null }
		return (available.toFloat() / uniqueKeys.size.toFloat()).coerceIn(0f, 1f)
	}

	fun generateSnapshotsBlocking(
		configs: List<WallpaperConfig>,
		timeoutMs: Long = 5_000L,
		strict: Boolean = false
	): Boolean {
		if (configs.isEmpty()) return true
		val pending = configs
			.distinctBy { it.id }
			.filter { delegate.getSnapshotPath(snapshotKey(it)) == null }
		if (pending.isEmpty()) return true
		Logger.event(
			tag,
			"generate_blocking_start",
			"requested" to configs.size,
			"pending" to pending.size,
			"timeoutMs" to timeoutMs,
			"strict" to strict
		)

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
			val done = !hasMissingSnapshots(configs)
			Logger.event(tag, "generate_blocking_wait_done", "completed" to done)
			return done
		}

		while (SystemClock.elapsedRealtime() < deadlineMs) {
			val progressed = worker.runNext(ignoreThermalLimits = strict)
			if (!progressed) break
			if (!hasMissingSnapshots(configs)) break
			Thread.sleep(
				worker.nextDelayMs(ignoreThermalLimits = strict).coerceAtMost(MAX_BLOCKING_DELAY_MS)
			)
		}

		val completed = !hasMissingSnapshots(configs)
		synchronized(generationLock) {
			generationRunning = false
		}
		if (!completed && !strict) {
			generateSnapshots(configs)
		}
		Logger.event(tag, "generate_blocking_end", "completed" to completed)
		return completed
	}

	private fun runWorkerLoopInBackground() {
		thread(start = true, isDaemon = true, name = "SnapshotGenerate") {
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
			Logger.d(tag, "worker thread started")
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
			Logger.d(tag, "worker thread stopped")
		}
	}

	fun release() {
		snapshotUpdatedListener = null
		worker.release()
	}

	fun clearAllSnapshots() {
		synchronized(generationLock) {
			worker.clearPending()
			generationRunning = false
		}
		delegate.clearAll()
		Logger.w(tag, "all snapshots cleared")
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
		private const val SNAPSHOT_KEY_VERSION = "snapshot_v8"
		private const val WAIT_POLL_MS = 40L
		private const val MAX_BLOCKING_DELAY_MS = 20L
	}
}
