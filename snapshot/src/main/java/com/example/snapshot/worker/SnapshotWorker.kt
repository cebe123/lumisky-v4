package com.example.snapshot.worker

import android.content.Context
import android.os.Build
import android.os.PowerManager
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
	appContext: Context? = null,
	private val skyEngine: SkyEngine = SkyEngine(),
	private val bitmapFactory: SnapshotBitmapFactory = SnapshotBitmapFactory(),
	private val encoder: WebpEncoder = WebpEncoder()
) {
	private val powerManager: PowerManager? = appContext?.getSystemService(PowerManager::class.java)
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
		val config = queue.firstOrNull() ?: return false
		if (currentThermalStatus() >= THERMAL_STATUS_SEVERE) {
			return true
		}

		queue.removeFirst()
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

	fun nextDelayMs(): Long {
		if (isBatterySaverEnabled()) return BATTERY_SAVER_DELAY_MS
		return when (currentThermalStatus()) {
			in THERMAL_STATUS_MODERATE until THERMAL_STATUS_SEVERE -> THERMAL_MODERATE_DELAY_MS
			in THERMAL_STATUS_SEVERE..Int.MAX_VALUE -> THERMAL_SEVERE_DELAY_MS
			else -> DEFAULT_DELAY_MS
		}
	}

	fun release() {
		skyEngine.release()
	}

	private fun currentThermalStatus(): Int {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return THERMAL_STATUS_NONE
		return powerManager?.currentThermalStatus ?: THERMAL_STATUS_NONE
	}

	private fun isBatterySaverEnabled(): Boolean {
		return powerManager?.isPowerSaveMode == true
	}

	companion object {
		private const val THERMAL_STATUS_NONE = 0
		private const val THERMAL_STATUS_MODERATE = 2
		private const val THERMAL_STATUS_SEVERE = 3
		private const val DEFAULT_DELAY_MS = 6L
		private const val THERMAL_MODERATE_DELAY_MS = 60L
		private const val THERMAL_SEVERE_DELAY_MS = 400L
		private const val BATTERY_SAVER_DELAY_MS = 140L
	}
}
