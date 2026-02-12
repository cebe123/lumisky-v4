package com.example.engine.preview

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.example.core.perf.RenderStatsTracker
import com.example.engine.SkyEngine
import com.example.engine.config.WallpaperConfig
import com.example.engine.renderer.RenderMode
import com.example.engine.shader.PreviewSkyProgram
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PreviewGlRenderer(
	private val skyEngine: SkyEngine = SkyEngine(),
	private val config: WallpaperConfig = WallpaperConfig.default(id = "preview_default"),
	private val mode: RenderMode = RenderMode.PREVIEW,
	private val animateFullDayLoop: Boolean = true,
	private val fragmentShaderOverride: String? = null
) : GLSurfaceView.Renderer {

	private var previewStartMillis: Long = 0L
	private val skyProgram = PreviewSkyProgram()
	private val stats = RenderStatsTracker(
		tag = "PreviewGlRenderer",
		logEvery = 120
	)

	override fun onSurfaceCreated(gl: GL10?, eglConfig: EGLConfig?) {
		skyEngine.init(config)
		skyEngine.setRenderMode(mode)
		previewStartMillis = System.currentTimeMillis()
		skyProgram.init(fragmentShaderOverride)
		GLES20.glClearColor(0f, 0f, 0f, 1f)
		stats.reset()
	}

	override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
		GLES20.glViewport(0, 0, width, height)
		skyProgram.setViewport(width, height)
	}

	override fun onDrawFrame(gl: GL10?) {
		val frameStartNs = System.nanoTime()
		val state = if (animateFullDayLoop) {
			val elapsed = (System.currentTimeMillis() - previewStartMillis).coerceAtLeast(0L)
			val loopProgress = (elapsed % PREVIEW_LOOP_MILLIS).toFloat() / PREVIEW_LOOP_MILLIS.toFloat()
			skyEngine.sampleAtDayProgress(
				dayProgress = loopProgress,
				mode = mode,
				force = true
			)
		} else {
			skyEngine.renderNow(force = true)
		}

		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
		if (state != null) {
			skyProgram.draw(state)
			stats.onDraw(System.nanoTime() - frameStartNs)
		} else {
			stats.onSkip("state_null")
		}
	}

	companion object {
		private const val PREVIEW_LOOP_MILLIS = 8_000L
	}
}
