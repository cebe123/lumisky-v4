package com.example.wallpaper.render

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.view.SurfaceHolder
import com.example.core.Logger
import com.example.engine.config.WallpaperConfig
import com.example.engine.renderer.RenderFrameState
import com.example.engine.shader.PreviewSkyProgram

/**
 * Owns a single EGL context/surface pair. Public calls must stay on the render thread.
 */
class WallpaperEglSession {

	private val skyProgram = PreviewSkyProgram()

	private var display: EGLDisplay = EGL14.EGL_NO_DISPLAY
	private var context: EGLContext = EGL14.EGL_NO_CONTEXT
	private var surface: EGLSurface = EGL14.EGL_NO_SURFACE
	private var config: EGLConfig? = null
	private val eglVersion = IntArray(2)
	private val eglConfigs = arrayOfNulls<EGLConfig>(1)
	private val eglConfigCount = IntArray(1)
	private val queriedWidth = IntArray(1)
	private val queriedHeight = IntArray(1)

	@Volatile
	private var viewportWidth: Int = 0
	@Volatile
	private var viewportHeight: Int = 0
	private var preservedSwapBehaviorRequested: Boolean = false
	private var preservedSwapBehaviorEnabled: Boolean = false

	@Synchronized
	fun attach(
		holder: SurfaceHolder,
		config: WallpaperConfig = WallpaperConfig.default(),
		fragmentShaderOverride: String? = null,
		textureBytesLoader: ((String) -> ByteArray?)? = null
	): Boolean {
		release()

		if (!initDisplay()) return failAttach("initDisplay")
		if (!chooseConfig()) return failAttach("chooseConfig")
		if (!createContext()) return failAttach("createContext")
		if (!createSurface(holder)) return failAttach("createSurface")
		if (!makeCurrent("attach.makeCurrent")) return failAttach("makeCurrent")
		EGL14.eglSwapInterval(display, 1)
		preservedSwapBehaviorEnabled = configureSwapBehavior()

		val frame = holder.surfaceFrame
		viewportWidth = frame.width()
		viewportHeight = frame.height()
		if (viewportWidth <= 0 || viewportHeight <= 0) {
			queryViewportSize()
		}
		if (viewportWidth > 0 && viewportHeight > 0) {
			GLES20.glViewport(0, 0, viewportWidth, viewportHeight)
		}
		GLES20.glClearColor(0f, 0f, 0f, 1f)
		skyProgram.configure(config, textureBytesLoader)
		skyProgram.init(fragmentShaderOverride)
		if (viewportWidth > 0 && viewportHeight > 0) {
			skyProgram.setViewport(viewportWidth, viewportHeight)
		}
		return true
	}

	@Synchronized
	fun draw(state: RenderFrameState): Boolean {
		if (surface == EGL14.EGL_NO_SURFACE) return false
		if (!makeCurrent("draw.makeCurrent")) {
			release()
			return false
		}

		if (viewportWidth <= 0 || viewportHeight <= 0) {
			queryViewportSize()
		}
		if (viewportWidth <= 0 || viewportHeight <= 0) {
			Logger.e(TAG, "EGL draw skipped, viewport is invalid width=$viewportWidth height=$viewportHeight")
			return false
		}

		GLES20.glViewport(0, 0, viewportWidth, viewportHeight)
		skyProgram.setViewport(viewportWidth, viewportHeight)
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
		skyProgram.draw(state)
		if (!EGL14.eglSwapBuffers(display, surface)) {
			val error = EGL14.eglGetError()
			Logger.e(TAG, "eglSwapBuffers failed error=${eglErrorString(error)}")
			if (error == EGL14.EGL_BAD_SURFACE || error == EGL14.EGL_CONTEXT_LOST) {
				release()
			}
			return false
		}
		return true
	}

	@Synchronized
	fun reconfigure(
		config: WallpaperConfig,
		fragmentShaderOverride: String? = null,
		textureBytesLoader: ((String) -> ByteArray?)? = null
	): Boolean {
		if (display == EGL14.EGL_NO_DISPLAY ||
			context == EGL14.EGL_NO_CONTEXT ||
			surface == EGL14.EGL_NO_SURFACE
		) {
			return false
		}
		if (!makeCurrent("reconfigure.makeCurrent")) {
			release()
			return false
		}
		skyProgram.release()
		skyProgram.configure(config, textureBytesLoader)
		skyProgram.init(fragmentShaderOverride)
		if (viewportWidth <= 0 || viewportHeight <= 0) {
			queryViewportSize()
		}
		if (viewportWidth > 0 && viewportHeight > 0) {
			skyProgram.setViewport(viewportWidth, viewportHeight)
		}
		return true
	}

	@Synchronized
	fun detachSurface() {
		release()
	}

	@Synchronized
	fun release() {
		val hasEglResources = display != EGL14.EGL_NO_DISPLAY ||
			context != EGL14.EGL_NO_CONTEXT ||
			surface != EGL14.EGL_NO_SURFACE
		val canReleaseGlResources = display != EGL14.EGL_NO_DISPLAY &&
			context != EGL14.EGL_NO_CONTEXT &&
			surface != EGL14.EGL_NO_SURFACE &&
			makeCurrent("release.makeCurrent")

		if (hasEglResources && !canReleaseGlResources) {
			Logger.d(TAG, "releasing EGL session without current GL context")
		}
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
		preservedSwapBehaviorRequested = false
		preservedSwapBehaviorEnabled = false
	}

	private fun initDisplay(): Boolean {
		display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
		if (display == EGL14.EGL_NO_DISPLAY) {
			logEglError("eglGetDisplay")
			return false
		}

		if (!EGL14.eglInitialize(display, eglVersion, 0, eglVersion, 1)) {
			logEglError("eglInitialize")
			return false
		}
		return true
	}

	private fun chooseConfig(): Boolean {
		if (chooseConfig(surfaceType = EGL14.EGL_WINDOW_BIT or EGL_SWAP_BEHAVIOR_PRESERVED_BIT)) {
			preservedSwapBehaviorRequested = true
			return true
		}
		preservedSwapBehaviorRequested = false
		return chooseConfig(surfaceType = EGL14.EGL_WINDOW_BIT)
	}

	private fun chooseConfig(surfaceType: Int): Boolean {
		val attribs = intArrayOf(
			EGL14.EGL_RED_SIZE, 8,
			EGL14.EGL_GREEN_SIZE, 8,
			EGL14.EGL_BLUE_SIZE, 8,
			EGL14.EGL_ALPHA_SIZE, 8,
			EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
			EGL14.EGL_SURFACE_TYPE, surfaceType,
			EGL14.EGL_NONE
		)
		val ok = EGL14.eglChooseConfig(
			display,
			attribs,
			0,
			eglConfigs,
			0,
			eglConfigs.size,
			eglConfigCount,
			0
		)
		if (!ok || eglConfigCount[0] == 0) {
			logEglError("eglChooseConfig")
			return false
		}
		config = eglConfigs[0]
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
		if (context == EGL14.EGL_NO_CONTEXT) {
			logEglError("eglCreateContext")
			return false
		}
		return true
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
		if (surface == EGL14.EGL_NO_SURFACE) {
			logEglError("eglCreateWindowSurface")
			return false
		}
		return true
	}

	private fun makeCurrent(operation: String): Boolean {
		val ok = EGL14.eglMakeCurrent(display, surface, surface, context)
		if (!ok) {
			logEglError(operation)
		}
		return ok
	}

	private fun queryViewportSize() {
		val widthOk = EGL14.eglQuerySurface(display, surface, EGL14.EGL_WIDTH, queriedWidth, 0)
		val heightOk = EGL14.eglQuerySurface(display, surface, EGL14.EGL_HEIGHT, queriedHeight, 0)
		if (!widthOk || !heightOk) {
			logEglError("eglQuerySurface")
			viewportWidth = 0
			viewportHeight = 0
			return
		}
		viewportWidth = queriedWidth[0]
		viewportHeight = queriedHeight[0]
	}

	private fun configureSwapBehavior(): Boolean {
		if (!preservedSwapBehaviorRequested) {
			return false
		}
		val applied = EGL14.eglSurfaceAttrib(
			display,
			surface,
			EGL_SWAP_BEHAVIOR,
			EGL_BUFFER_PRESERVED
		)
		if (!applied) {
			logEglError("eglSurfaceAttrib")
			Logger.d(TAG, "Preserved swap behavior unavailable, continuing with destroyed back buffer")
			return false
		}
		Logger.d(TAG, "Preserved swap behavior enabled for wallpaper EGL surface")
		return true
	}

	private fun failAttach(operation: String): Boolean {
		Logger.e(TAG, "EGL attach failed at $operation")
		release()
		return false
	}

	private fun logEglError(operation: String) {
		val error = EGL14.eglGetError()
		if (error == EGL14.EGL_SUCCESS) {
			Logger.e(TAG, "$operation failed")
		} else {
			Logger.e(TAG, "$operation failed error=${eglErrorString(error)}")
		}
	}

	private fun eglErrorString(error: Int): String {
		return "0x${error.toString(16)}"
	}

	companion object {
		private const val TAG = "WallpaperEglSession"
		private const val EGL_SWAP_BEHAVIOR = 0x3093
		private const val EGL_BUFFER_PRESERVED = 0x3094
		private const val EGL_SWAP_BEHAVIOR_PRESERVED_BIT = 0x0400
	}
}
