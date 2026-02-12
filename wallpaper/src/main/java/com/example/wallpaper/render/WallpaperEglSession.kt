package com.example.wallpaper.render

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.view.SurfaceHolder
import com.example.engine.renderer.RenderFrameState
import com.example.engine.shader.PreviewSkyProgram

class WallpaperEglSession {

	private val skyProgram = PreviewSkyProgram()

	private var display: EGLDisplay = EGL14.EGL_NO_DISPLAY
	private var context: EGLContext = EGL14.EGL_NO_CONTEXT
	private var surface: EGLSurface = EGL14.EGL_NO_SURFACE
	private var config: EGLConfig? = null

	private var viewportWidth: Int = 0
	private var viewportHeight: Int = 0

	fun attach(holder: SurfaceHolder, fragmentShaderOverride: String? = null): Boolean {
		release()

		if (!initDisplay()) return false
		if (!chooseConfig()) return false
		if (!createContext()) return false
		if (!createSurface(holder)) return false
		if (!makeCurrent()) return false

		val frame = holder.surfaceFrame
		viewportWidth = frame.width()
		viewportHeight = frame.height()
		GLES20.glViewport(0, 0, viewportWidth, viewportHeight)
		GLES20.glClearColor(0f, 0f, 0f, 1f)
		skyProgram.init(fragmentShaderOverride)
		skyProgram.setViewport(viewportWidth, viewportHeight)
		return true
	}

	fun draw(state: RenderFrameState): Boolean {
		if (surface == EGL14.EGL_NO_SURFACE) return false
		if (!makeCurrent()) return false

		if (viewportWidth <= 0 || viewportHeight <= 0) {
			queryViewportSize()
		}
		if (viewportWidth <= 0 || viewportHeight <= 0) return false

		GLES20.glViewport(0, 0, viewportWidth, viewportHeight)
		skyProgram.setViewport(viewportWidth, viewportHeight)
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
		skyProgram.draw(state)
		return EGL14.eglSwapBuffers(display, surface)
	}

	fun detachSurface() {
		release()
	}

	fun release() {
		skyProgram.release()

		if (display != EGL14.EGL_NO_DISPLAY) {
			EGL14.eglMakeCurrent(
				display,
				EGL14.EGL_NO_SURFACE,
				EGL14.EGL_NO_SURFACE,
				EGL14.EGL_NO_CONTEXT
			)
		}

		if (surface != EGL14.EGL_NO_SURFACE) {
			EGL14.eglDestroySurface(display, surface)
			surface = EGL14.EGL_NO_SURFACE
		}

		if (context != EGL14.EGL_NO_CONTEXT) {
			EGL14.eglDestroyContext(display, context)
			context = EGL14.EGL_NO_CONTEXT
		}

		if (display != EGL14.EGL_NO_DISPLAY) {
			EGL14.eglTerminate(display)
			display = EGL14.EGL_NO_DISPLAY
		}

		config = null
		viewportWidth = 0
		viewportHeight = 0
	}

	private fun initDisplay(): Boolean {
		display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
		if (display == EGL14.EGL_NO_DISPLAY) return false

		val version = IntArray(2)
		return EGL14.eglInitialize(display, version, 0, version, 1)
	}

	private fun chooseConfig(): Boolean {
		val attribs = intArrayOf(
			EGL14.EGL_RED_SIZE, 8,
			EGL14.EGL_GREEN_SIZE, 8,
			EGL14.EGL_BLUE_SIZE, 8,
			EGL14.EGL_ALPHA_SIZE, 8,
			EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
			EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,
			EGL14.EGL_NONE
		)
		val configs = arrayOfNulls<EGLConfig>(1)
		val numConfigs = IntArray(1)
		val ok = EGL14.eglChooseConfig(
			display,
			attribs,
			0,
			configs,
			0,
			configs.size,
			numConfigs,
			0
		)
		if (!ok || numConfigs[0] == 0) return false
		config = configs[0]
		return config != null
	}

	private fun createContext(): Boolean {
		val eglConfig = config ?: return false
		val contextAttribs = intArrayOf(
			EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
			EGL14.EGL_NONE
		)
		context = EGL14.eglCreateContext(
			display,
			eglConfig,
			EGL14.EGL_NO_CONTEXT,
			contextAttribs,
			0
		)
		return context != EGL14.EGL_NO_CONTEXT
	}

	private fun createSurface(holder: SurfaceHolder): Boolean {
		val eglConfig = config ?: return false
		val surfaceAttribs = intArrayOf(EGL14.EGL_NONE)
		surface = EGL14.eglCreateWindowSurface(
			display,
			eglConfig,
			holder.surface,
			surfaceAttribs,
			0
		)
		return surface != EGL14.EGL_NO_SURFACE
	}

	private fun makeCurrent(): Boolean {
		return EGL14.eglMakeCurrent(display, surface, surface, context)
	}

	private fun queryViewportSize() {
		val width = IntArray(1)
		val height = IntArray(1)
		EGL14.eglQuerySurface(display, surface, EGL14.EGL_WIDTH, width, 0)
		EGL14.eglQuerySurface(display, surface, EGL14.EGL_HEIGHT, height, 0)
		viewportWidth = width[0]
		viewportHeight = height[0]
	}
}
