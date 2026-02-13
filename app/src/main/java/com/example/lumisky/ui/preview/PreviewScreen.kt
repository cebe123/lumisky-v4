package com.example.lumisky.ui.preview

import android.os.Build
import android.os.PowerManager
import android.opengl.GLSurfaceView
import android.view.Choreographer
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.core.settings.PerformanceMode
import com.example.engine.config.ShaderDefaults
import com.example.engine.config.ShaderProfile
import com.example.engine.config.WallpaperConfig
import com.example.engine.preview.PreviewGlRenderer
import com.example.engine.renderer.RenderMode
import com.example.lumisky.shader.RenderAssetCache

@Composable
fun PreviewScreen(
	config: WallpaperConfig = WallpaperConfig.default(id = "preview_default").copy(
		shader = ShaderProfile(
			fragmentAssetPath = ShaderDefaults.DEFAULT_FRAGMENT_ASSET_PATH,
			mode = ShaderDefaults.DEFAULT_SHADER_MODE
		)
	),
	highRefreshEnabled: Boolean = true,
	performanceMode: PerformanceMode = PerformanceMode.AUTO,
	onSetWallpaper: () -> Unit = {},
	onBack: () -> Unit
) {
	Box(modifier = Modifier.fillMaxSize()) {
		AndroidView(
			modifier = Modifier.fillMaxSize(),
			factory = { context ->
				val fragmentOverride = RenderAssetCache.loadFragment(
					context = context,
					assetPath = config.shader.fragmentAssetPath
				)
				val powerManager = context.getSystemService(PowerManager::class.java)
				val renderer = PreviewGlRenderer(
					config = config,
					mode = RenderMode.PREVIEW,
					animateFullDayLoop = true,
					highRefreshEnabled = highRefreshEnabled,
					performanceMode = performanceMode,
					deviceRefreshRateProvider = { resolveDisplayRefreshRate(context) },
					thermalStatusProvider = {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
							powerManager?.currentThermalStatus
						} else {
							null
						}
					},
					isPowerSaveModeProvider = {
						powerManager?.isPowerSaveMode == true
					},
					fragmentShaderOverride = fragmentOverride,
					textureBytesLoader = { assetPath ->
						RenderAssetCache.loadTextureBytes(context, assetPath)
					}
				)
				object : GLSurfaceView(context) {
					private var lastRenderFrameNs: Long = 0L
					private var frameCallbackPosted: Boolean = false
					private val frameTicker = object : Choreographer.FrameCallback {
						override fun doFrame(frameTimeNanos: Long) {
							frameCallbackPosted = false
							val minIntervalNs = renderer.nextFrameDelayMs() * 1_000_000L
							if (frameTimeNanos - lastRenderFrameNs >= minIntervalNs) {
								requestRender()
								lastRenderFrameNs = frameTimeNanos
							}
							if (renderer.shouldContinueRendering() && windowVisibility == View.VISIBLE) {
								postFrameCallbackIfNeeded()
							}
						}
					}

					override fun onAttachedToWindow() {
						super.onAttachedToWindow()
						lastRenderFrameNs = 0L
						postFrameCallbackIfNeeded()
					}

					override fun onWindowVisibilityChanged(visibility: Int) {
						super.onWindowVisibilityChanged(visibility)
						if (visibility == View.VISIBLE && renderer.shouldContinueRendering()) {
							postFrameCallbackIfNeeded()
						} else {
							removeFrameCallback()
						}
					}

					override fun onDetachedFromWindow() {
						removeFrameCallback()
						runCatching {
							queueEvent { renderer.release() }
						}
						super.onDetachedFromWindow()
					}

					private fun postFrameCallbackIfNeeded() {
						if (frameCallbackPosted) return
						frameCallbackPosted = true
						Choreographer.getInstance().postFrameCallback(frameTicker)
					}

					private fun removeFrameCallback() {
						if (!frameCallbackPosted) return
						frameCallbackPosted = false
						Choreographer.getInstance().removeFrameCallback(frameTicker)
					}
				}.apply {
					setEGLContextClientVersion(2)
					setRenderer(renderer)
					renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
				}
			}
		)

		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(10.dp)
		) {
			Button(onClick = onBack) {
				Text("Geri")
			}
			Button(
				onClick = onSetWallpaper,
				modifier = Modifier
					.fillMaxWidth()
					.height(48.dp)
			) {
				Text("Uygula")
			}
		}

	}
}

private fun resolveDisplayRefreshRate(context: android.content.Context): Int {
	val refresh = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
		context.display?.refreshRate
	} else {
		val windowManager = context.getSystemService(android.content.Context.WINDOW_SERVICE) as? WindowManager
		@Suppress("DEPRECATION")
		windowManager?.defaultDisplay?.refreshRate
	} ?: DEFAULT_REFRESH_RATE.toFloat()
	return refresh.toInt().coerceIn(DEFAULT_REFRESH_RATE, MAX_REFRESH_RATE)
}

private const val DEFAULT_REFRESH_RATE = 60
private const val MAX_REFRESH_RATE = 120
