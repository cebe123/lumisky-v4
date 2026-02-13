package com.example.snapshot.worker

import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import com.example.core.settings.PerformanceMode
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
	@Volatile
	private var performanceMode: PerformanceMode = PerformanceMode.AUTO
	private val baseSnapshotSize: Pair<Int, Int> = resolveBaseSnapshotSize(context)

	@Synchronized
	fun enqueue(configs: List<WallpaperConfig>) {
		var added = 0
		configs.forEach { config ->
			if (queuedIds.add(config.id)) {
				queue.addLast(config)
				added += 1
			}
		}
		Logger.event("SnapshotWorker", "enqueue", "requested" to configs.size, "added" to added, "queueSize" to queue.size)
	}

	@Synchronized
	fun prioritize(configs: List<WallpaperConfig>) {
		if (configs.isEmpty()) return
		val currentById = LinkedHashMap<String, WallpaperConfig>(queue.size)
		queue.forEach { queued ->
			currentById[queued.id] = queued
		}

		val reordered = ArrayDeque<WallpaperConfig>()
		val reorderedIds = LinkedHashSet<String>()
		configs.forEach { preferred ->
			val candidate = currentById.remove(preferred.id) ?: preferred
			if (snapshotProvider.getSnapshotPath(snapshotKey(candidate)) != null) return@forEach
			if (reorderedIds.add(candidate.id)) {
				reordered.addLast(candidate)
			}
		}

		currentById.values.forEach { remaining ->
			if (reorderedIds.add(remaining.id)) {
				reordered.addLast(remaining)
			}
		}

		queue.clear()
		queue.addAll(reordered)
		queuedIds.clear()
		queuedIds.addAll(reorderedIds)
		Logger.event(
			"SnapshotWorker",
			"prioritize",
			"requested" to configs.size,
			"queueSize" to queue.size
		)
	}

	@Synchronized
	fun runNext(ignoreThermalLimits: Boolean = false): Boolean {
		val config = queue.firstOrNull() ?: return false
		if (!ignoreThermalLimits && currentThermalStatus() >= THERMAL_STATUS_SEVERE) {
			Logger.w("SnapshotWorker", "thermal severe; delaying generation")
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
		val snapshotFrameProgress = resolveSnapshotFrameProgress(config)
		Logger.event(
			"SnapshotWorker",
			"run_next",
			"id" to config.id,
			"queueRemaining" to queue.size,
			"frameIndex" to SNAPSHOT_FRAME_INDEX,
			"frameProgress" to String.format(java.util.Locale.US, "%.4f", snapshotFrameProgress)
		)

		val sampledState = skyEngine.sampleAtDayProgress(
			dayProgress = snapshotFrameProgress,
			mode = RenderMode.SNAPSHOT,
			force = true
		) ?: return true

		val bitmap = bitmapFactory.create(
			state = sampledState,
			config = config,
			textureBytesLoader = ::loadAssetBytes,
			baseWidth = baseSnapshotSize.first,
			baseHeight = baseSnapshotSize.second
		)
		try {
			val encoded = encoder.encode(bitmap) ?: return true
			snapshotProvider.putSnapshot(snapshotKey, encoded)
			onSnapshotGenerated(config.id)
			generatedCount += 1
			Logger.event(
				"SnapshotWorker",
				"snapshot_generated",
				"id" to config.id,
				"generatedCount" to generatedCount,
				"queueRemaining" to queue.size
			)
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

	fun setPerformanceMode(mode: PerformanceMode) {
		performanceMode = mode
		Logger.event("SnapshotWorker", "performance_mode_set", "mode" to mode)
	}

	fun nextDelayMs(ignoreThermalLimits: Boolean = false): Long {
		if (ignoreThermalLimits) return BLOCKING_DELAY_MS
		val thermal = currentThermalStatus()
		if (isBoostActive() && thermal < THERMAL_STATUS_MODERATE) {
			return when (performanceMode) {
				PerformanceMode.SMOOTH -> BOOST_DELAY_SMOOTH_MS
				PerformanceMode.BATTERY -> BOOST_DELAY_BATTERY_MS
				PerformanceMode.AUTO -> BOOST_DELAY_AUTO_MS
			}
		}
		if (isBatterySaverEnabled() && performanceMode != PerformanceMode.SMOOTH) {
			return BATTERY_SAVER_DELAY_MS
		}

		return when (performanceMode) {
			PerformanceMode.SMOOTH -> when (thermal) {
				in THERMAL_STATUS_MODERATE until THERMAL_STATUS_SEVERE -> THERMAL_MODERATE_DELAY_SMOOTH_MS
				in THERMAL_STATUS_SEVERE..Int.MAX_VALUE -> THERMAL_SEVERE_DELAY_SMOOTH_MS
				else -> DEFAULT_DELAY_SMOOTH_MS
			}
			PerformanceMode.BATTERY -> when (thermal) {
				in THERMAL_STATUS_MODERATE until THERMAL_STATUS_SEVERE -> THERMAL_MODERATE_DELAY_BATTERY_MS
				in THERMAL_STATUS_SEVERE..Int.MAX_VALUE -> THERMAL_SEVERE_DELAY_BATTERY_MS
				else -> DEFAULT_DELAY_BATTERY_MS
			}
			PerformanceMode.AUTO -> when (thermal) {
				in THERMAL_STATUS_MODERATE until THERMAL_STATUS_SEVERE -> THERMAL_MODERATE_DELAY_AUTO_MS
				in THERMAL_STATUS_SEVERE..Int.MAX_VALUE -> THERMAL_SEVERE_DELAY_AUTO_MS
				else -> DEFAULT_DELAY_AUTO_MS
			}
		}
	}

	fun release() {
		bitmapFactory.release()
		if (!engineInitialized) return
		skyEngine.release()
		engineInitialized = false
		Logger.d("SnapshotWorker", "released")
	}

	@Synchronized
	fun clearPending() {
		queue.clear()
		queuedIds.clear()
		boostUntilElapsedMs = 0L
		Logger.w("SnapshotWorker", "pending queue cleared")
	}

	private fun ensureEngineInitialized() {
		if (engineInitialized) return
		skyEngine.init()
		skyEngine.setRenderMode(RenderMode.SNAPSHOT)
		engineInitialized = true
	}

	private fun loadAssetBytes(path: String): ByteArray? {
		val appContext = context ?: return null
		return runCatching {
			appContext.assets.open(path).use { it.readBytes() }
		}.getOrNull()
	}

	private fun resolveSnapshotFrameProgress(config: WallpaperConfig): Float {
		val sunriseProgress = (
			config.daylight.sunriseMinute.coerceIn(0, TimeManager.MINUTES_PER_DAY) /
				TimeManager.MINUTES_PER_DAY.toFloat()
			).coerceIn(0f, 1f)
		val nowProgress = TimeManager().dayProgress().coerceIn(0f, 1f)
		val targetProgress = if (nowProgress >= sunriseProgress) nowProgress else nowProgress + 1f
		val catchUpDurationMs = (config.focusCatchUpDurationSeconds * 1000f)
			.coerceAtLeast(MIN_SNAPSHOT_CATCHUP_DURATION_MS)
		val frameDurationMs = 1000f / SNAPSHOT_REFERENCE_FPS.toFloat()
		val elapsedMs = (SNAPSHOT_FRAME_INDEX - 1).coerceAtLeast(0) * frameDurationMs
		val t = (elapsedMs / catchUpDurationMs).coerceIn(0f, 1f)
		return wrapDayProgress(sunriseProgress + ((targetProgress - sunriseProgress) * t))
	}

	private fun wrapDayProgress(value: Float): Float {
		val wrapped = value % 1f
		return if (wrapped < 0f) wrapped + 1f else wrapped
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
		private const val DEFAULT_DELAY_AUTO_MS = 60L
		private const val BOOST_DELAY_AUTO_MS = 20L
		private const val THERMAL_MODERATE_DELAY_AUTO_MS = 90L
		private const val THERMAL_SEVERE_DELAY_AUTO_MS = 420L

		private const val DEFAULT_DELAY_SMOOTH_MS = 18L
		private const val BOOST_DELAY_SMOOTH_MS = 10L
		private const val THERMAL_MODERATE_DELAY_SMOOTH_MS = 36L
		private const val THERMAL_SEVERE_DELAY_SMOOTH_MS = 140L

		private const val DEFAULT_DELAY_BATTERY_MS = 220L
		private const val BOOST_DELAY_BATTERY_MS = 28L
		private const val THERMAL_MODERATE_DELAY_BATTERY_MS = 320L
		private const val THERMAL_SEVERE_DELAY_BATTERY_MS = 620L

		private const val BATTERY_SAVER_DELAY_MS = 300L
		private const val BLOCKING_DELAY_MS = 6L
		private const val SNAPSHOT_KEY_VERSION = "snapshot_v8"
		private const val INTERACTION_BOOST_MS = 8_000L
		private const val TARGET_SHORT_EDGE = 520
		private const val MIN_LONG_EDGE = 860
		private const val MAX_LONG_EDGE = 1500
		private const val DEFAULT_BASE_WIDTH = 520
		private const val DEFAULT_BASE_HEIGHT = 924
		private const val SNAPSHOT_REFERENCE_FPS = 60
		private const val SNAPSHOT_FRAME_INDEX = 10
		private const val MIN_SNAPSHOT_CATCHUP_DURATION_MS = 300f
	}
}
