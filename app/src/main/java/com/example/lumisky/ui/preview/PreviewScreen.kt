package com.example.lumisky.ui.preview

import android.os.Build
import android.os.PowerManager
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
import com.example.lumisky.ui.common.PreviewRendererSurfaceView
import com.example.lumisky.ui.common.resolveDisplayRefreshRate

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
				PreviewRendererSurfaceView(
					context = context,
					previewRenderer = renderer,
					initialPlaybackEnabled = true
				)
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
