package com.example.engine.preview

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.example.core.perf.RenderStatsTracker
import com.example.core.settings.PerformanceMode
import com.example.engine.SkyEngine
import com.example.engine.config.WallpaperConfig
import com.example.engine.renderer.RenderFrameState
import com.example.engine.renderer.RenderMode
import com.example.engine.shader.PreviewSkyProgram
import java.time.Instant
import java.time.ZoneId
import kotlin.math.abs
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

data class FocusCatchUpWindow(
	val startProgress: Float,
	val targetProgress: Float
)

fun resolveFocusCatchUpWindow(
	nowProgress: Float,
	sunriseMinute: Int,
	sunsetMinute: Int
): FocusCatchUpWindow {
	val normalizedNow = nowProgress.coerceIn(0f, 1f)
	val normalizedSunriseMinute = sunriseMinute.coerceIn(0, MINUTES_PER_DAY)
	val normalizedSunsetMinute = sunsetMinute.coerceIn(0, MINUTES_PER_DAY)
	val sunriseProgress = (normalizedSunriseMinute / MINUTES_PER_DAY.toFloat()).coerceIn(0f, 1f)
	val nowMinute = (normalizedNow * MINUTES_PER_DAY).toInt().coerceIn(0, MINUTES_PER_DAY)
	val isDaytime = if (normalizedSunsetMinute >= normalizedSunriseMinute) {
		nowMinute in normalizedSunriseMinute..normalizedSunsetMinute
	} else {
		nowMinute >= normalizedSunriseMinute || nowMinute <= normalizedSunsetMinute
	}
	val targetProgress = when {
		isDaytime -> 1f + normalizedNow
		normalizedNow >= sunriseProgress -> normalizedNow
		else -> normalizedNow + 1f
	}
	return FocusCatchUpWindow(
		startProgress = sunriseProgress,
		targetProgress = targetProgress
	)
}

class PreviewGlRenderer(
	private val skyEngine: SkyEngine = SkyEngine(),
	private val config: WallpaperConfig = WallpaperConfig.default(id = "preview_default"),
	private val mode: RenderMode = RenderMode.PREVIEW,
	private val animateFullDayLoop: Boolean = true,
	private val fixedDayProgress: Float? = null,
	private val initialFocusCatchUpEnabled: Boolean = false,
	private val highRefreshEnabled: Boolean = true,
	private val performanceMode: PerformanceMode = PerformanceMode.AUTO,
	private val deviceRefreshRateProvider: () -> Int = { DEFAULT_DISPLAY_FPS },
	private val qualityScale: Float = 1.0f,
	private val nowProvider: () -> Long = { System.currentTimeMillis() },
	private val thermalStatusProvider: () -> Int? = { null },
	private val isPowerSaveModeProvider: () -> Boolean = { false },
	private val fragmentShaderOverride: String? = null,
	private val textureBytesLoader: ((String) -> ByteArray?)? = null,
	private val onFrameDrawn: (() -> Unit)? = null
) : GLSurfaceView.Renderer {

	private var previewStartMillis: Long = 0L
	private var focusStartProgress: Float = 0f
	private var focusTargetProgress: Float = 0f
	private var focusFinalState: RenderFrameState? = null
	@Volatile
	private var focusCatchUpEnabled: Boolean = initialFocusCatchUpEnabled
	@Volatile
	private var focusAnimationCompleted: Boolean = false
	private var movingAvgDrawMillis: Float = 16f
	private var adaptiveQualityScale: Float = 1f
	private var qualityScaleLowerBound: Float = 0.5f
	private var qualityScaleUpperBound: Float = 1f
	private var currentTargetFps: Int = DEFAULT_DISPLAY_FPS

	private val skyProgram = PreviewSkyProgram()
	private val stats = RenderStatsTracker(
		tag = "PreviewGlRenderer",
		logEvery = 120
	)
	@Volatile
	private var released: Boolean = false

	override fun onSurfaceCreated(gl: GL10?, eglConfig: EGLConfig?) {
		if (released) return
		skyEngine.init(config)
		skyEngine.setRenderMode(mode)
		movingAvgDrawMillis = 16f
		initializeAdaptiveQualityState()
		skyProgram.configure(config, textureBytesLoader, adaptiveQualityScale)
		skyProgram.init(fragmentShaderOverride)
		previewStartMillis = nowProvider()
		initializeFocusState()
		GLES20.glClearColor(0f, 0f, 0f, 1f)
		stats.reset()
	}

	override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
		if (released) return
		GLES20.glViewport(0, 0, width, height)
		skyProgram.setViewport(width, height)
	}

	override fun onDrawFrame(gl: GL10?) {
		if (released) return
		val frameNowMillis = nowProvider()
		val frameStartNs = System.nanoTime()
		val state = resolveFrameState(frameNowMillis)

		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
		if (state != null) {
			skyProgram.draw(state)
			onFrameDrawn?.invoke()
			val drawDurationNs = System.nanoTime() - frameStartNs
			val drawDurationMs = drawDurationNs / 1_000_000f
			movingAvgDrawMillis = (movingAvgDrawMillis * 0.9f) + (drawDurationMs * 0.1f)
			currentTargetFps = resolveTargetFps()
			updateAdaptiveQuality(drawDurationMs, currentTargetFps)
			stats.onDraw(drawDurationNs)
		} else {
			stats.onSkip("state_null")
		}
	}

	fun nextFrameDelayMs(): Long {
		val targetFps = resolveTargetFps().coerceIn(MIN_TARGET_FPS, MAX_TARGET_FPS)
		currentTargetFps = targetFps
		return (1000L / targetFps).coerceAtLeast(1L)
	}

	fun shouldContinueRendering(): Boolean {
		if (released) return false
		if (fixedDayProgress != null) return false
		return when {
			animateFullDayLoop -> true
			focusCatchUpEnabled -> !focusAnimationCompleted
			else -> false
		}
	}

	fun setFocusPlaybackEnabled(
		enabled: Boolean,
		restartOnEnable: Boolean = false
	) {
		if (released || animateFullDayLoop || fixedDayProgress != null) return
		val shouldRestart = enabled && (!focusCatchUpEnabled || restartOnEnable)
		focusCatchUpEnabled = enabled
		if (shouldRestart) {
			previewStartMillis = nowProvider()
			initializeFocusState()
		}
	}

	fun release() {
		if (released) return
		released = true
		skyProgram.release()
		skyEngine.release()
	}

	private fun resolveFrameState(nowMillis: Long): RenderFrameState? {
		fixedDayProgress?.let { fixedProgress ->
			return skyEngine.sampleAtDayProgress(
				dayProgress = fixedProgress,
				mode = mode,
				force = true
			)
		}

		if (animateFullDayLoop) {
			val loopDurationMs = (config.previewLoopDurationSeconds * 1000f)
				.toLong()
				.coerceAtLeast(MIN_LOOP_DURATION_MS)
			val elapsed = (nowMillis - previewStartMillis).coerceAtLeast(0L)
			val loopProgress = (elapsed % loopDurationMs).toFloat() / loopDurationMs.toFloat()
			return skyEngine.sampleAtDayProgress(
				dayProgress = loopProgress,
				mode = mode,
				force = true
			)
		}

		if (!focusCatchUpEnabled) {
			return skyEngine.renderNow(force = true)
		}

		if (focusAnimationCompleted) {
			return focusFinalState
		}

		val durationMs = (config.focusCatchUpDurationSeconds * 1000f)
			.toLong()
			.coerceAtLeast(MIN_FOCUS_DURATION_MS)
		val elapsed = (nowMillis - previewStartMillis).coerceAtLeast(0L)
		val t = (elapsed.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
		val progress = wrapDayProgress(lerp(focusStartProgress, focusTargetProgress, t))
		val state = skyEngine.sampleAtDayProgress(
			dayProgress = progress,
			mode = mode,
			force = true
		)

		if (t >= 1f && state != null) {
			focusFinalState = state
			focusAnimationCompleted = true
		}
		return state
	}

	private fun resolveTargetFps(): Int {
		val displayFps = deviceRefreshRateProvider()
			.coerceIn(MIN_DISPLAY_FPS, MAX_TARGET_FPS)
		val smoothCap = if (highRefreshEnabled) displayFps else DEFAULT_DISPLAY_FPS
		val maxFps = when (performanceMode) {
			PerformanceMode.SMOOTH -> smoothCap
			PerformanceMode.BATTERY -> BATTERY_MODE_MAX_FPS.coerceAtMost(smoothCap)
			PerformanceMode.AUTO -> AUTO_MODE_MAX_FPS.coerceAtMost(smoothCap)
		}.coerceIn(MIN_TARGET_FPS, MAX_TARGET_FPS)
		if (isPowerSaveModeProvider() && performanceMode != PerformanceMode.SMOOTH) {
			return BATTERY_SAVER_MAX_FPS.coerceAtMost(maxFps).coerceAtLeast(MIN_TARGET_FPS)
		}

		val thermal = thermalStatusProvider()
		return when (performanceMode) {
			PerformanceMode.SMOOTH -> {
				if (thermal != null) {
					when {
						thermal >= THERMAL_STATUS_SEVERE -> SMOOTH_THERMAL_SEVERE_FPS
						thermal >= THERMAL_STATUS_MODERATE -> SMOOTH_THERMAL_MODERATE_FPS.coerceAtMost(maxFps)
						else -> maxFps
					}
				} else {
					maxFps
				}
			}
			PerformanceMode.BATTERY -> {
				if (thermal != null) {
					when {
						thermal >= THERMAL_STATUS_SEVERE -> MIN_TARGET_FPS
						thermal >= THERMAL_STATUS_MODERATE -> BATTERY_THERMAL_MODERATE_FPS
						else -> BATTERY_MODE_TARGET_FPS.coerceAtMost(maxFps)
					}
				} else {
					BATTERY_MODE_TARGET_FPS.coerceAtMost(maxFps)
				}
			}
			PerformanceMode.AUTO -> {
				if (thermal != null) {
					when {
						thermal >= THERMAL_STATUS_SEVERE -> MIN_TARGET_FPS
						thermal >= THERMAL_STATUS_MODERATE -> THERMAL_MODERATE_FPS
						else -> autoAdaptiveFps(maxFps)
					}
				} else {
					autoAdaptiveFps(maxFps)
				}
			}
		}
			.coerceIn(MIN_TARGET_FPS, MAX_TARGET_FPS)
	}

	private fun autoAdaptiveFps(maxFps: Int): Int {
		return when {
			movingAvgDrawMillis <= HIGH_FPS_DRAW_COST_MS -> maxFps
			movingAvgDrawMillis <= MID_HIGH_FPS_DRAW_COST_MS -> maxFps.coerceAtMost(MID_HIGH_TARGET_FPS)
			movingAvgDrawMillis <= MID_FPS_DRAW_COST_MS -> DEFAULT_DISPLAY_FPS
			movingAvgDrawMillis <= LOW_FPS_DRAW_COST_MS -> LOW_TARGET_FPS
			else -> MIN_TARGET_FPS
		}
	}

	private fun initializeFocusState() {
		if (!focusCatchUpEnabled) {
			focusAnimationCompleted = false
			focusFinalState = null
			return
		}

		val window = resolveFocusCatchUpWindow(
			nowProgress = currentDayProgress(),
			sunriseMinute = config.daylight.sunriseMinute,
			sunsetMinute = config.daylight.sunsetMinute
		)
		focusStartProgress = window.startProgress
		focusTargetProgress = window.targetProgress
		focusAnimationCompleted = false
		focusFinalState = null
	}

	private fun initializeAdaptiveQualityState() {
		val requested = qualityScale.coerceIn(0.3f, 1f)
		val modeCap = when (performanceMode) {
			PerformanceMode.SMOOTH -> 1f
			PerformanceMode.AUTO -> 0.92f
			PerformanceMode.BATTERY -> 0.80f
		}
		qualityScaleUpperBound = (requested * modeCap).coerceIn(0.3f, 1f)
		qualityScaleLowerBound = when (performanceMode) {
			PerformanceMode.SMOOTH -> 0.62f
			PerformanceMode.AUTO -> 0.50f
			PerformanceMode.BATTERY -> 0.42f
		}.coerceAtMost(qualityScaleUpperBound)
		adaptiveQualityScale = qualityScaleUpperBound
		currentTargetFps = resolveTargetFps()
	}

	private fun updateAdaptiveQuality(
		drawDurationMs: Float,
		targetFps: Int
	) {
		val budgetMs = (1000f / targetFps.coerceAtLeast(1).toFloat()).coerceAtLeast(1f)
		val previous = adaptiveQualityScale
		val downStep = when (performanceMode) {
			PerformanceMode.SMOOTH -> QUALITY_STEP_DOWN_SMOOTH
			PerformanceMode.AUTO -> QUALITY_STEP_DOWN_AUTO
			PerformanceMode.BATTERY -> QUALITY_STEP_DOWN_BATTERY
		}
		val upStep = when (performanceMode) {
			PerformanceMode.SMOOTH -> QUALITY_STEP_UP_SMOOTH
			PerformanceMode.AUTO -> QUALITY_STEP_UP_AUTO
			PerformanceMode.BATTERY -> QUALITY_STEP_UP_BATTERY
		}

		when {
			drawDurationMs > budgetMs * QUALITY_OVER_BUDGET_HARD -> {
				adaptiveQualityScale -= downStep * 1.35f
			}
			drawDurationMs > budgetMs * QUALITY_OVER_BUDGET -> {
				adaptiveQualityScale -= downStep
			}
			drawDurationMs < budgetMs * QUALITY_UNDER_BUDGET -> {
				adaptiveQualityScale += upStep
			}
		}
		adaptiveQualityScale = adaptiveQualityScale.coerceIn(
			qualityScaleLowerBound,
			qualityScaleUpperBound
		)
		if (abs(adaptiveQualityScale - previous) >= 0.01f) {
			skyProgram.setRenderQuality(adaptiveQualityScale)
		}
	}

	private fun currentDayProgress(): Float {
		val zone = config.daylight.timeZoneId
			?.trim()
			?.takeIf { it.isNotBlank() }
			?.let { runCatching { ZoneId.of(it) }.getOrNull() }
			?: ZoneId.systemDefault()
		val localTime = Instant.ofEpochMilli(nowProvider())
			.atZone(zone)
			.toLocalTime()
		return localTime.toNanoOfDay().toDouble()
			.div(NANOS_PER_DAY.toDouble())
			.toFloat()
	}

	private fun wrapDayProgress(value: Float): Float {
		val wrapped = value % 1f
		return if (wrapped < 0f) wrapped + 1f else wrapped
	}

	private fun Long.floorMod(mod: Long): Long {
		val value = this % mod
		return if (value < 0L) value + mod else value
	}

	private fun lerp(start: Float, end: Float, t: Float): Float {
		return start + ((end - start) * t.coerceIn(0f, 1f))
	}

	companion object {
		private const val NANOS_PER_DAY = 24L * 60L * 60L * 1_000_000_000L
		private const val MIN_LOOP_DURATION_MS = 1_000L
		private const val MIN_FOCUS_DURATION_MS = 300L
		private const val MIN_TARGET_FPS = 30
		private const val MIN_DISPLAY_FPS = 60
		private const val DEFAULT_DISPLAY_FPS = 60
		private const val MAX_TARGET_FPS = 120
		private const val LOW_TARGET_FPS = 45
		private const val MID_HIGH_TARGET_FPS = 90
		private const val HIGH_FPS_DRAW_COST_MS = 7.5f
		private const val MID_HIGH_FPS_DRAW_COST_MS = 10f
		private const val MID_FPS_DRAW_COST_MS = 14f
		private const val LOW_FPS_DRAW_COST_MS = 20f
		private const val AUTO_MODE_MAX_FPS = 90
		private const val THERMAL_MODERATE_FPS = 45
		private const val BATTERY_SAVER_MAX_FPS = 45
		private const val BATTERY_MODE_TARGET_FPS = 42
		private const val BATTERY_MODE_MAX_FPS = 60
		private const val SMOOTH_THERMAL_MODERATE_FPS = 72
		private const val SMOOTH_THERMAL_SEVERE_FPS = 45
		private const val BATTERY_THERMAL_MODERATE_FPS = 36
		private const val THERMAL_STATUS_MODERATE = 2
		private const val THERMAL_STATUS_SEVERE = 3

		private const val QUALITY_OVER_BUDGET = 0.95f
		private const val QUALITY_OVER_BUDGET_HARD = 1.10f
		private const val QUALITY_UNDER_BUDGET = 0.60f
		private const val QUALITY_STEP_DOWN_SMOOTH = 0.018f
		private const val QUALITY_STEP_DOWN_AUTO = 0.026f
		private const val QUALITY_STEP_DOWN_BATTERY = 0.020f
		private const val QUALITY_STEP_UP_SMOOTH = 0.008f
		private const val QUALITY_STEP_UP_AUTO = 0.012f
		private const val QUALITY_STEP_UP_BATTERY = 0.010f
	}
}

private const val MINUTES_PER_DAY = 24 * 60
