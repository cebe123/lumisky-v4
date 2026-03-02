package com.example.lumisky.snapshot

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.PixelCopy
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.core.Logger
import com.example.core.api.SunDaylight
import com.example.core.api.SunLocation
import com.example.core.api.SunTimesRepository
import com.example.core.location.LastKnownLocationProvider
import com.example.core.settings.AppSettingsDefaults
import com.example.core.settings.AppSettingsRepository
import com.example.core.settings.LocationMode
import com.example.engine.config.WallpaperConfig
import com.example.engine.preview.PreviewGlRenderer
import com.example.engine.renderer.RenderMode
import com.example.lumisky.data.WallpaperCatalog
import com.example.lumisky.shader.RenderAssetCache
import com.example.lumisky.ui.common.PreviewRendererSurfaceView
import com.example.lumisky.ui.home.resolveHomePreviewFrameAspectRatio
import java.io.File
import java.io.FileOutputStream
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class ZenithSnapshotActivity : AppCompatActivity() {

	private val appSettingsRepository by lazy { AppSettingsRepository(applicationContext) }
	private val sunTimesRepository by lazy { SunTimesRepository() }
	private val lastKnownLocationProvider by lazy { LastKnownLocationProvider(applicationContext) }
	private val mainHandler = Handler(Looper.getMainLooper())
	private val worker = Executors.newSingleThreadExecutor()

	private lateinit var surfaceHost: FrameLayout
	private lateinit var statusView: TextView

	private var previewWidthPx: Int = 360
	private var previewHeightPx: Int = 720
	private var resolvedDaylight: SunDaylight = SunDaylight.fallback()
	private var outputDir: File? = null
	private var configs: List<WallpaperConfig> = emptyList()
	private val savedSnapshots = mutableListOf<SavedSnapshot>()
	private var currentSurfaceView: PreviewRendererSurfaceView? = null
	private var currentIndex: Int = 0
	private var currentSessionId: Int = 0
	private var currentCaptureAttempt: Int = 0
	private var captureQueued: Boolean = false
	private var pendingTimeoutRunnable: Runnable? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		resolvePreviewSize()
		buildUi()
		startGeneration()
	}

	override fun onDestroy() {
		clearRenderTimeout()
		mainHandler.removeCallbacksAndMessages(null)
		surfaceHost.removeAllViews()
		sunTimesRepository.release()
		worker.shutdownNow()
		super.onDestroy()
	}

	private fun startGeneration() {
		updateStatus("Zenith snapshot export is preparing...")
		worker.execute {
			val preparedOutputDir = runCatching {
				prepareOutputDirectory()
			}.getOrElse { error ->
				Logger.e(TAG, "snapshot output directory preparation failed", error)
				runOnUiThread {
					failAndFinish("Snapshot klasoru hazirlanamadi.")
				}
				return@execute
			}
			val daylight = resolveDaylight()
			val generatedConfigs = WallpaperCatalog.buildConfigs(daylight = daylight)
			generatedConfigs.forEach { config ->
				RenderAssetCache.prewarmWallpaper(applicationContext, config)
			}
			runOnUiThread {
				if (isFinishing || isDestroyed) return@runOnUiThread
				resolvedDaylight = daylight
				outputDir = preparedOutputDir
				configs = generatedConfigs
				savedSnapshots.clear()
				currentIndex = 0
				captureNextWallpaper()
			}
		}
	}

	private fun captureNextWallpaper() {
		if (currentIndex >= configs.size) {
			finalizeExport()
			return
		}

		val config = configs[currentIndex]
		val sessionId = ++currentSessionId
		currentCaptureAttempt = 0
		captureQueued = false
		updateStatus("Rendering ${currentIndex + 1}/${configs.size}: ${config.name}")
		renderWallpaper(config = config, sessionId = sessionId)
		scheduleRenderTimeout(sessionId)
	}

	private fun renderWallpaper(
		config: WallpaperConfig,
		sessionId: Int
	) {
		val fragmentOverride = RenderAssetCache.loadFragment(
			context = applicationContext,
			assetPath = config.shader.fragmentAssetPath
		)
		val renderer = PreviewGlRenderer(
			config = config,
			mode = RenderMode.PREVIEW,
			animateFullDayLoop = false,
			fixedDayProgress = resolveZenithProgress(config),
			initialFocusCatchUpEnabled = false,
			highRefreshEnabled = false,
			qualityScale = 1.0f,
			fragmentShaderOverride = fragmentOverride,
			textureBytesLoader = { assetPath ->
				RenderAssetCache.loadTextureBytes(applicationContext, assetPath)
			},
			onFrameDrawn = {
				onFrameDrawn(sessionId)
			}
		)
		val nextView = PreviewRendererSurfaceView(
			context = this,
			previewRenderer = renderer,
			initialPlaybackEnabled = true,
			requestRenderOnAttach = true
		)
		currentSurfaceView = nextView
		surfaceHost.removeAllViews()
		surfaceHost.addView(
			nextView,
			FrameLayout.LayoutParams(previewWidthPx, previewHeightPx).apply {
				gravity = Gravity.CENTER
			}
		)
	}

	private fun onFrameDrawn(sessionId: Int) {
		mainHandler.post {
			if (sessionId != currentSessionId || captureQueued) return@post
			captureQueued = true
			mainHandler.postDelayed({
				if (sessionId != currentSessionId) return@postDelayed
				captureCurrentWallpaper(sessionId)
			}, CAPTURE_DELAY_MS)
		}
	}

	private fun captureCurrentWallpaper(sessionId: Int) {
		clearRenderTimeout()
		val targetView = currentSurfaceView
		if (sessionId != currentSessionId || targetView == null) {
			handleCaptureFailure("Renderer view is no longer active.")
			return
		}
		if (targetView.width <= 0 || targetView.height <= 0) {
			handleCaptureFailure("Renderer view has no measured size.")
			return
		}

		val bitmap = Bitmap.createBitmap(
			targetView.width,
			targetView.height,
			Bitmap.Config.ARGB_8888
		)
		currentCaptureAttempt += 1
		PixelCopy.request(targetView, bitmap, { copyResult ->
			if (sessionId != currentSessionId) {
				bitmap.recycle()
				return@request
			}
			if (copyResult == PixelCopy.SUCCESS) {
				onCaptureSucceeded(bitmap)
			} else if (copyResult == PixelCopy.ERROR_SOURCE_NO_DATA &&
				currentCaptureAttempt < MAX_CAPTURE_ATTEMPTS
			) {
				bitmap.recycle()
				Logger.w(
					TAG,
					"pixel copy retry id=${configs.getOrNull(currentIndex)?.id} attempt=$currentCaptureAttempt"
				)
				scheduleRenderTimeout(sessionId)
				mainHandler.postDelayed({
					if (sessionId != currentSessionId) return@postDelayed
					captureCurrentWallpaper(sessionId)
				}, CAPTURE_RETRY_DELAY_MS)
			} else {
				bitmap.recycle()
				handleCaptureFailure("PixelCopy failed with code $copyResult.")
			}
		}, mainHandler)
	}

	private fun onCaptureSucceeded(bitmap: Bitmap) {
		val config = configs.getOrNull(currentIndex)
		if (config == null) {
			bitmap.recycle()
			failAndFinish("Snapshot export lost its active wallpaper.")
			return
		}
		persistCapturedBitmap(
			config = config,
			bitmap = bitmap,
			fallbackReason = null
		)
	}

	private fun handleCaptureFailure(reason: String) {
		val config = configs.getOrNull(currentIndex)
		if (config == null) {
			failAndFinish(reason)
			return
		}
		Logger.w(TAG, "snapshot fallback id=${config.id} reason=$reason")
		persistCapturedBitmap(
			config = config,
			bitmap = createFallbackBitmap(
				title = config.name,
				reason = reason
			),
			fallbackReason = reason
		)
	}

	private fun persistCapturedBitmap(
		config: WallpaperConfig,
		bitmap: Bitmap,
		fallbackReason: String?
	) {
		val snapshotNumber = currentIndex + 1
		val activeOutputDir = outputDir
		if (activeOutputDir == null) {
			bitmap.recycle()
			failAndFinish("Snapshot output directory is unavailable.")
			return
		}
		updateStatus("Saving $snapshotNumber/${configs.size}: ${config.name}")
		worker.execute {
			var exportBitmap: Bitmap? = null
			val outputFile = runCatching {
				exportBitmap = if (fallbackReason == null) {
					enhanceBitmapForExport(bitmap)
				} else {
					bitmap
				}
				writeSnapshotFile(
					directory = activeOutputDir,
					bitmap = exportBitmap ?: bitmap,
					config = config,
					snapshotNumber = snapshotNumber
				)
			}.onFailure { error ->
				Logger.e(TAG, "snapshot export write failed id=${config.id}", error)
			}.getOrNull()
			exportBitmap?.let { processed ->
				if (processed !== bitmap) {
					processed.recycle()
				}
			}
			bitmap.recycle()
			if (outputFile == null) {
				runOnUiThread {
					failAndFinish("Snapshot file could not be written.")
				}
				return@execute
			}
			savedSnapshots += SavedSnapshot(
				id = config.id,
				fileName = outputFile.name,
				fallbackReason = fallbackReason
			)
			runOnUiThread {
				if (isFinishing || isDestroyed) return@runOnUiThread
				Logger.i(TAG, "snapshot saved id=${config.id} file=${outputFile.name}")
				currentIndex += 1
				captureNextWallpaper()
			}
		}
	}

	private fun finalizeExport() {
		clearRenderTimeout()
		updateStatus("Finalizing snapshot export...")
		surfaceHost.removeAllViews()
		currentSurfaceView = null
		val activeOutputDir = outputDir
		if (activeOutputDir == null) {
			failAndFinish("Snapshot output directory is unavailable.")
			return
		}
		worker.execute {
			val markerFile = runCatching {
				writeCompletionMarker(activeOutputDir)
			}.onFailure { error ->
				Logger.e(TAG, "snapshot completion marker write failed", error)
			}.getOrNull()
			if (markerFile == null) {
				runOnUiThread {
					failAndFinish("Completion marker could not be written.")
				}
				return@execute
			}
			runOnUiThread {
				if (isFinishing || isDestroyed) return@runOnUiThread
				updateStatus("Saved ${savedSnapshots.size} snapshot(s) to ${activeOutputDir.absolutePath}")
				Toast.makeText(
					this,
					"Zenith snapshots saved.",
					Toast.LENGTH_LONG
				).show()
				Logger.i(TAG, "snapshot export completed path=${activeOutputDir.absolutePath}")
				finishAndRemoveTask()
			}
		}
	}

	private fun writeSnapshotFile(
		directory: File,
		bitmap: Bitmap,
		config: WallpaperConfig,
		snapshotNumber: Int
	): File {
		val safeId = sanitizeFileSegment(config.id)
		val outputFile = File(
			directory,
			String.format(Locale.US, "%02d-%s-zenith.png", snapshotNumber, safeId)
		)
		if (outputFile.exists() && !outputFile.delete()) {
			error("Could not replace existing snapshot file.")
		}
		FileOutputStream(outputFile).use { stream ->
			val compressed = bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
			stream.flush()
			if (!compressed) {
				error("Bitmap compression failed.")
			}
		}
		return outputFile
	}

	private fun writeCompletionMarker(directory: File): File {
		val markerFile = File(directory, COMPLETION_MARKER_FILE_NAME)
		if (markerFile.exists() && !markerFile.delete()) {
			error("Could not replace completion marker.")
		}
		val generatedAt = TIMESTAMP_FORMATTER.format(ZonedDateTime.now())
		val payload = buildString {
			appendLine("status=ok")
			appendLine("generated_at=$generatedAt")
			appendLine("solar_noon=${formatMinute(resolvedDaylight.solarNoonMinute)}")
			appendLine("count=${savedSnapshots.size}")
			savedSnapshots.forEach { snapshot ->
				append("file=${snapshot.fileName}")
				append(" id=${snapshot.id}")
				append(" fallback=${snapshot.fallbackReason != null}")
				snapshot.fallbackReason?.let { reason ->
					append(" reason=$reason")
				}
				appendLine()
			}
		}
		markerFile.writeText(payload)
		return markerFile
	}

	private fun prepareOutputDirectory(): File {
		val baseDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
			?: error("External pictures directory is unavailable.")
		val directory = File(baseDir, OUTPUT_DIRECTORY_NAME)
		if (directory.exists() && !directory.deleteRecursively()) {
			error("Could not clear old snapshot directory.")
		}
		if (!directory.exists() && !directory.mkdirs()) {
			error("Could not create snapshot directory.")
		}
		return directory
	}

	private fun resolveDaylight(): SunDaylight {
		var daylight = sunTimesRepository.currentOrFallback()
		val latch = CountDownLatch(1)
		sunTimesRepository.refreshAsyncWithCandidates(
			candidates = buildSunTimesCandidates()
		) { fetched ->
			daylight = fetched
			latch.countDown()
		}
		latch.await(SUN_TIMES_TIMEOUT_MS, TimeUnit.MILLISECONDS)
		Logger.i(
			TAG,
			"daylight resolved sunrise=${daylight.sunriseMinute} sunset=${daylight.sunsetMinute} solarNoon=${daylight.solarNoonMinute}"
		)
		return daylight
	}

	private fun buildSunTimesCandidates(): List<SunLocation> {
		val settings = appSettingsRepository.snapshot()
		val manualLocation = SunLocation(
			label = settings.manualCity.name,
			latitude = settings.manualCity.latitude,
			longitude = settings.manualCity.longitude,
			timeZoneId = settings.manualCity.timeZoneId
		)
		val defaultLocation = SunLocation(
			label = "default_city",
			latitude = AppSettingsDefaults.DEFAULT_CITY.latitude,
			longitude = AppSettingsDefaults.DEFAULT_CITY.longitude,
			timeZoneId = AppSettingsDefaults.DEFAULT_CITY.timeZoneId
		)

		return buildList {
			val systemLocationEnabled = runCatching {
				lastKnownLocationProvider.isLocationEnabled()
			}.getOrDefault(false)
			if (settings.locationMode == LocationMode.GPS && systemLocationEnabled) {
				val liveGps = lastKnownLocationProvider.getLastKnownLocation(label = "snapshot_gps_live")
				val lastGps = lastKnownLocationProvider.getLastKnownLocation(label = "snapshot_gps_last")
				liveGps?.let { add(it) }
				lastGps?.let { add(it) }
			}
			add(manualLocation)
			add(defaultLocation)
		}.distinctBy { candidate ->
			"${candidate.latitude}|${candidate.longitude}|${candidate.timeZoneId.orEmpty()}"
		}
	}

	private fun resolveZenithProgress(config: WallpaperConfig): Float {
		val minute = config.daylight.solarNoonMinute.coerceIn(0, MAX_DAY_MINUTE)
		return minute / MINUTES_PER_DAY.toFloat()
	}

	private fun resolvePreviewSize() {
		val displayMetrics = resources.displayMetrics
		val screenWidth = displayMetrics.widthPixels.coerceAtLeast(1)
		val screenHeight = displayMetrics.heightPixels.coerceAtLeast(screenWidth)
		val frameAspectRatio = resolveHomePreviewFrameAspectRatio(
			primaryEdge = screenWidth,
			secondaryEdge = screenHeight
		)
		val maxAllowedWidth = (screenWidth - dp(8)).coerceAtLeast(1)
		val maxAllowedHeight = (screenHeight - dp(16)).coerceAtLeast(1)
		val targetWidth = minOf(dp(EXPORT_TARGET_WIDTH_DP), maxAllowedWidth)
		val heightBoundWidth = (maxAllowedHeight / frameAspectRatio)
			.roundToInt()
			.coerceAtLeast(1)
		val preferredMinWidth = maxOf(
			dp(EXPORT_MIN_WIDTH_DP),
			(dp(EXPORT_MIN_HEIGHT_DP) / frameAspectRatio).roundToInt()
		).coerceAtMost(maxAllowedWidth)
		previewWidthPx = minOf(
			targetWidth.coerceAtLeast(preferredMinWidth),
			heightBoundWidth
		).coerceAtLeast(1)
		previewHeightPx = (previewWidthPx * frameAspectRatio)
			.roundToInt()
			.coerceAtLeast(1)
	}

	private fun buildUi() {
		surfaceHost = FrameLayout(this).apply {
			setBackgroundColor(Color.rgb(5, 10, 16))
		}
		statusView = TextView(this).apply {
			setTextColor(Color.WHITE)
			textSize = 14f
			gravity = Gravity.CENTER
			setPadding(dp(14), dp(10), dp(14), dp(10))
			setBackgroundColor(Color.argb(214, 12, 19, 29))
		}

		val root = FrameLayout(this).apply {
			setBackgroundColor(Color.BLACK)
			addView(
				surfaceHost,
				FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
			)
			addView(
				statusView,
				FrameLayout.LayoutParams(MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
					gravity = Gravity.BOTTOM
					leftMargin = dp(12)
					rightMargin = dp(12)
					bottomMargin = dp(18)
				}
			)
		}

		setContentView(root)
	}

	private fun createFallbackBitmap(
		title: String,
		reason: String
	): Bitmap {
		val bitmap = Bitmap.createBitmap(previewWidthPx, previewHeightPx, Bitmap.Config.ARGB_8888)
		val canvas = Canvas(bitmap)
		canvas.drawColor(Color.rgb(18, 27, 40))
		val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
			color = Color.WHITE
			textSize = dp(14).toFloat()
			isFakeBoldText = true
		}
		val reasonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
			color = Color.argb(220, 190, 204, 220)
			textSize = dp(10).toFloat()
		}
		canvas.drawText(
			ellipsize(title, titlePaint, (previewWidthPx - dp(24)).toFloat()),
			dp(12).toFloat(),
			(previewHeightPx / 2f) - dp(6),
			titlePaint
		)
		canvas.drawText(
			ellipsize(reason, reasonPaint, (previewWidthPx - dp(24)).toFloat()),
			dp(12).toFloat(),
			(previewHeightPx / 2f) + dp(16),
			reasonPaint
		)
		return bitmap
	}

	private fun enhanceBitmapForExport(source: Bitmap): Bitmap {
		val enhanced = Bitmap.createBitmap(
			source.width,
			source.height,
			Bitmap.Config.ARGB_8888
		)
		val saturationMatrix = ColorMatrix().apply {
			setSaturation(EXPORT_SATURATION)
		}
		val contrast = EXPORT_CONTRAST
		val brightnessOffset = EXPORT_BRIGHTNESS_OFFSET
		val contrastMatrix = ColorMatrix(
			floatArrayOf(
				contrast, 0f, 0f, 0f, brightnessOffset,
				0f, contrast, 0f, 0f, brightnessOffset,
				0f, 0f, contrast, 0f, brightnessOffset,
				0f, 0f, 0f, 1f, 0f
			)
		)
		saturationMatrix.postConcat(contrastMatrix)

		val canvas = Canvas(enhanced)
		val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
			isDither = true
			colorFilter = ColorMatrixColorFilter(saturationMatrix)
		}
		canvas.drawBitmap(source, 0f, 0f, bitmapPaint)

		// Mild warm tint keeps the export punchy without changing the art direction too much.
		val warmthPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
			color = Color.argb(EXPORT_WARMTH_ALPHA, 255, 214, 148)
		}
		canvas.drawRect(
			0f,
			0f,
			enhanced.width.toFloat(),
			enhanced.height.toFloat(),
			warmthPaint
		)
		return enhanced
	}

	private fun formatMinute(minute: Int): String {
		val normalized = minute.coerceIn(0, MAX_DAY_MINUTE)
		val hours = normalized / 60
		val minutes = normalized % 60
		return String.format(Locale.US, "%02d:%02d", hours, minutes)
	}

	private fun sanitizeFileSegment(value: String): String {
		val sanitized = value.lowercase(Locale.US)
			.replace(Regex("[^a-z0-9_-]+"), "_")
			.trim('_')
		return sanitized.ifBlank { "wallpaper" }
	}

	private fun ellipsize(
		value: String,
		paint: Paint,
		maxWidth: Float
	): String {
		if (paint.measureText(value) <= maxWidth) return value
		var truncated = value
		while (truncated.length > 1 && paint.measureText("$truncated...") > maxWidth) {
			truncated = truncated.dropLast(1)
		}
		return "$truncated..."
	}

	private fun updateStatus(message: String) {
		statusView.text = message
		Logger.i(TAG, message)
	}

	private fun scheduleRenderTimeout(sessionId: Int) {
		clearRenderTimeout()
		val timeoutRunnable = Runnable {
			if (sessionId != currentSessionId) return@Runnable
			handleCaptureFailure("Render timeout.")
		}
		pendingTimeoutRunnable = timeoutRunnable
		mainHandler.postDelayed(timeoutRunnable, RENDER_TIMEOUT_MS)
	}

	private fun clearRenderTimeout() {
		pendingTimeoutRunnable?.let { runnable ->
			mainHandler.removeCallbacks(runnable)
		}
		pendingTimeoutRunnable = null
	}

	private fun dp(value: Int): Int {
		return (value * resources.displayMetrics.density)
			.roundToInt()
			.coerceAtLeast(1)
	}

	private fun failAndFinish(message: String) {
		Logger.e(TAG, message)
		Toast.makeText(this, message, Toast.LENGTH_LONG).show()
		finishAndRemoveTask()
	}

	private data class SavedSnapshot(
		val id: String,
		val fileName: String,
		val fallbackReason: String?
	)

	companion object {
		private const val TAG = "ZenithSnapshotActivity"
		private const val OUTPUT_DIRECTORY_NAME = "zenith-snapshots"
		private const val COMPLETION_MARKER_FILE_NAME = "completed.txt"
		private const val MINUTES_PER_DAY = 24 * 60
		private const val MAX_DAY_MINUTE = MINUTES_PER_DAY - 1
		private const val EXPORT_TARGET_WIDTH_DP = 360
		private const val EXPORT_MIN_WIDTH_DP = 220
		private const val EXPORT_MIN_HEIGHT_DP = 420
		private const val EXPORT_SATURATION = 1.12f
		private const val EXPORT_CONTRAST = 1.06f
		private const val EXPORT_BRIGHTNESS_OFFSET = 6f
		private const val EXPORT_WARMTH_ALPHA = 8
		private const val SUN_TIMES_TIMEOUT_MS = 6_000L
		private const val CAPTURE_DELAY_MS = 90L
		private const val CAPTURE_RETRY_DELAY_MS = 140L
		private const val MAX_CAPTURE_ATTEMPTS = 4
		private const val RENDER_TIMEOUT_MS = 4_000L
		private val TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(
			"yyyy-MM-dd HH:mm:ss z",
			Locale.US
		)
	}
}
