package com.example.snapshot.worker

import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
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
	private val encoder: WebpEncoder = WebpEncoder(),
	private val onSnapshotGenerated: (String) -> Unit = {}
) {
	private val context = appContext?.applicationContext
	private val powerManager: PowerManager? = appContext?.getSystemService(PowerManager::class.java)
	private val queue = ArrayDeque<WallpaperConfig>()
	private val queuedIds = LinkedHashSet<String>()
	private var generatedCount: Int = 0
	private var engineInitialized: Boolean = false
	@Volatile
	private var boostUntilElapsedMs: Long = 0L
	private val baseSnapshotSize: Pair<Int, Int> = resolveBaseSnapshotSize(context)

	@Synchronized
	fun enqueue(configs: List<WallpaperConfig>) {
		configs.forEach { config ->
			if (queuedIds.add(config.id)) {
				queue.addLast(config)
			}
		}
	}

	@Synchronized
	fun runNext(): Boolean {
		val config = queue.firstOrNull() ?: return false
		if (currentThermalStatus() >= THERMAL_STATUS_SEVERE) {
			return true
		}
		val snapshotKey = snapshotKey(config)

		queue.removeFirst()
		queuedIds.remove(config.id)
		if (snapshotProvider.getSnapshotPath(snapshotKey) != null) {
			return true
		}
		val startNs = System.nanoTime()
		ensureEngineInitialized()
		skyEngine.setConfig(config)

		val noonState = skyEngine.sampleAtDayProgress(
			dayProgress = TimeManager.NOON_PROGRESS,
			mode = RenderMode.SNAPSHOT,
			force = true
		) ?: return true

		val bitmap = bitmapFactory.create(
			state = noonState,
			baseWidth = baseSnapshotSize.first,
			baseHeight = baseSnapshotSize.second
		)
		try {
			val encoded = encoder.encode(bitmap) ?: return true
			snapshotProvider.putSnapshot(snapshotKey, encoded)
			onSnapshotGenerated(config.id)
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

	fun boost(durationMs: Long = INTERACTION_BOOST_MS) {
		val until = SystemClock.elapsedRealtime() + durationMs.coerceAtLeast(500L)
		if (until > boostUntilElapsedMs) {
			boostUntilElapsedMs = until
		}
	}

	fun nextDelayMs(): Long {
		if (isBatterySaverEnabled()) return BATTERY_SAVER_DELAY_MS
		if (isBoostActive() && currentThermalStatus() < THERMAL_STATUS_MODERATE) {
			return BOOST_DELAY_MS
		}
		return when (currentThermalStatus()) {
			in THERMAL_STATUS_MODERATE until THERMAL_STATUS_SEVERE -> THERMAL_MODERATE_DELAY_MS
			in THERMAL_STATUS_SEVERE..Int.MAX_VALUE -> THERMAL_SEVERE_DELAY_MS
			else -> DEFAULT_DELAY_MS
		}
	}

	fun release() {
		if (!engineInitialized) return
		skyEngine.release()
		engineInitialized = false
	}

	private fun ensureEngineInitialized() {
		if (engineInitialized) return
		skyEngine.init()
		skyEngine.setRenderMode(RenderMode.SNAPSHOT)
		engineInitialized = true
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

	private fun currentThermalStatus(): Int {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return THERMAL_STATUS_NONE
		return powerManager?.currentThermalStatus ?: THERMAL_STATUS_NONE
	}

	private fun isBatterySaverEnabled(): Boolean {
		return powerManager?.isPowerSaveMode == true
	}

	private fun isBoostActive(): Boolean {
		return SystemClock.elapsedRealtime() < boostUntilElapsedMs
	}

	private fun resolveBaseSnapshotSize(context: Context?): Pair<Int, Int> {
		val metrics = context?.resources?.displayMetrics
		val widthPx = metrics?.widthPixels ?: DEFAULT_BASE_WIDTH
		val heightPx = metrics?.heightPixels ?: DEFAULT_BASE_HEIGHT
		val shortEdgePx = minOf(widthPx, heightPx).coerceAtLeast(1)
		val longEdgePx = maxOf(widthPx, heightPx).coerceAtLeast(shortEdgePx)
		val aspect = longEdgePx.toFloat() / shortEdgePx.toFloat()

		val targetShort = TARGET_SHORT_EDGE
		val targetLong = (targetShort * aspect).toInt().coerceIn(MIN_LONG_EDGE, MAX_LONG_EDGE)
		return if (heightPx >= widthPx) {
			targetShort to targetLong
		} else {
			targetLong to targetShort
		}
	}

	companion object {
		private const val THERMAL_STATUS_NONE = 0
		private const val THERMAL_STATUS_MODERATE = 2
		private const val THERMAL_STATUS_SEVERE = 3
		private const val DEFAULT_DELAY_MS = 45L
		private const val BOOST_DELAY_MS = 4L
		private const val THERMAL_MODERATE_DELAY_MS = 90L
		private const val THERMAL_SEVERE_DELAY_MS = 420L
		private const val BATTERY_SAVER_DELAY_MS = 180L
		private const val SNAPSHOT_KEY_VERSION = "snapshot_v2"
		private const val INTERACTION_BOOST_MS = 8_000L
		private const val TARGET_SHORT_EDGE = 520
		private const val MIN_LONG_EDGE = 860
		private const val MAX_LONG_EDGE = 1500
		private const val DEFAULT_BASE_WIDTH = 520
		private const val DEFAULT_BASE_HEIGHT = 924
	}
}
