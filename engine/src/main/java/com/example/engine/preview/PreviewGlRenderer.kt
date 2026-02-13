package com.example.engine.preview

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.example.core.perf.RenderStatsTracker
import com.example.engine.SkyEngine
import com.example.engine.config.WallpaperConfig
import com.example.engine.renderer.RenderFrameState
import com.example.engine.renderer.RenderMode
import com.example.engine.shader.PreviewSkyProgram
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PreviewGlRenderer(
	private val skyEngine: SkyEngine = SkyEngine(),
	private val config: WallpaperConfig = WallpaperConfig.default(id = "preview_default"),
	private val mode: RenderMode = RenderMode.PREVIEW,
	private val animateFullDayLoop: Boolean = true,
	private val focusCatchUpEnabled: Boolean = false,
	private val highRefreshEnabled: Boolean = true,
	private val deviceRefreshRateProvider: () -> Int = { DEFAULT_DISPLAY_FPS },
	private val qualityScale: Float = 1.0f,
	private val nowProvider: () -> Long = { System.currentTimeMillis() },
	private val thermalStatusProvider: () -> Int? = { null },
	private val fragmentShaderOverride: String? = null,
	private val textureBytesLoader: ((String) -> ByteArray?)? = null
) : GLSurfaceView.Renderer {

	private var previewStartMillis: Long = 0L
	private var focusStartProgress: Float = 0f
	private var focusTargetProgress: Float = 0f
	private var focusFinalState: RenderFrameState? = null
	@Volatile
	private var focusAnimationCompleted: Boolean = false
	private var movingAvgDrawMillis: Float = 16f

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
		previewStartMillis = nowProvider()
		movingAvgDrawMillis = 16f
		initializeFocusState()
		skyProgram.configure(config, textureBytesLoader, qualityScale.coerceIn(0.3f, 1.0f))
		skyProgram.init(fragmentShaderOverride)
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
			val drawDurationNs = System.nanoTime() - frameStartNs
			movingAvgDrawMillis = (movingAvgDrawMillis * 0.9f) + (drawDurationNs / 1_000_000f * 0.1f)
			stats.onDraw(drawDurationNs)
		} else {
			stats.onSkip("state_null")
		}
	}

	fun nextFrameDelayMs(): Long {
		val targetFps = resolveTargetFps().coerceIn(MIN_TARGET_FPS, MAX_TARGET_FPS)
		return (1000L / targetFps).coerceAtLeast(1L)
	}

	fun shouldContinueRendering(): Boolean {
		if (released) return false
		return when {
			animateFullDayLoop -> true
			focusCatchUpEnabled -> !focusAnimationCompleted
			else -> false
		}
	}

	fun release() {
		if (released) return
		released = true
		skyProgram.release()
		skyEngine.release()
	}

	private fun resolveFrameState(nowMillis: Long): RenderFrameState? {
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
		val highRefreshCap = if (highRefreshEnabled) displayFps else DEFAULT_DISPLAY_FPS

		if (!animateFullDayLoop) {
			return if (highRefreshEnabled) {
				highRefreshCap.coerceAtLeast(DEFAULT_DISPLAY_FPS)
			} else {
				DEFAULT_DISPLAY_FPS
			}
		}

		val thermal = thermalStatusProvider()
		if (thermal != null) {
			return when {
				thermal >= THERMAL_STATUS_SEVERE -> MIN_TARGET_FPS
				thermal >= THERMAL_STATUS_MODERATE -> DEFAULT_DISPLAY_FPS
				else -> highRefreshCap
			}
		}
		if (!highRefreshEnabled) {
			return if (movingAvgDrawMillis <= MEDIUM_FPS_DRAW_COST_MS) {
				DEFAULT_DISPLAY_FPS
			} else {
				LOW_TARGET_FPS
			}
		}
		return if (movingAvgDrawMillis <= HIGH_FPS_DRAW_COST_MS) {
			highRefreshCap
		} else {
			DEFAULT_DISPLAY_FPS
		}
	}

	private fun initializeFocusState() {
		if (!focusCatchUpEnabled) {
			focusAnimationCompleted = false
			focusFinalState = null
			return
		}

		val sunrise = (config.daylight.sunriseMinute / MINUTES_PER_DAY.toFloat()).coerceIn(0f, 1f)
		val now = currentDayProgress()
		focusStartProgress = sunrise
		focusTargetProgress = if (now >= sunrise) now else now + 1f
		focusAnimationCompleted = false
		focusFinalState = null
	}

	private fun currentDayProgress(): Float {
		val dayMillis = nowProvider().floorMod(MILLIS_PER_DAY)
		return dayMillis.toFloat() / MILLIS_PER_DAY.toFloat()
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
		private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
		private const val MINUTES_PER_DAY = 24 * 60
		private const val MIN_LOOP_DURATION_MS = 1_000L
		private const val MIN_FOCUS_DURATION_MS = 300L
		private const val MIN_TARGET_FPS = 30
		private const val LOW_TARGET_FPS = 45
		private const val MIN_DISPLAY_FPS = 60
		private const val DEFAULT_DISPLAY_FPS = 60
		private const val MAX_PREVIEW_FPS = 60
		private const val MAX_TARGET_FPS = 120
		private const val HIGH_FPS_DRAW_COST_MS = 8f
		private const val MEDIUM_FPS_DRAW_COST_MS = 12f
		private const val THERMAL_STATUS_MODERATE = 2
		private const val THERMAL_STATUS_SEVERE = 3
	}
}
