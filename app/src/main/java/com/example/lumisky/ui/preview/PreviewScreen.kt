package com.example.lumisky.ui.preview

import android.opengl.GLSurfaceView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine.config.ShaderDefaults
import com.example.engine.config.ShaderProfile
import com.example.engine.config.WallpaperConfig
import com.example.engine.preview.PreviewGlRenderer
import com.example.engine.renderer.RenderMode
import com.example.lumisky.ui.debug.TemporaryDebugPanel
import com.example.lumisky.shader.ShaderAssetLoader

@Composable
fun PreviewScreen(
	config: WallpaperConfig = WallpaperConfig.default(id = "preview_default").copy(
		shader = ShaderProfile(
			fragmentAssetPath = ShaderDefaults.DEFAULT_FRAGMENT_ASSET_PATH,
			mode = ShaderDefaults.DEFAULT_SHADER_MODE
		)
	),
	onBack: () -> Unit
) {
	Box(modifier = Modifier.fillMaxSize()) {
		AndroidView(
			modifier = Modifier.fillMaxSize(),
			factory = { context ->
				val fragmentOverride = ShaderAssetLoader.loadFragment(
					context = context,
					assetPath = config.shader.fragmentAssetPath
				)
				GLSurfaceView(context).apply {
					setEGLContextClientVersion(2)
					setRenderer(
						PreviewGlRenderer(
							config = config,
							mode = RenderMode.PREVIEW,
							animateFullDayLoop = true,
							fragmentShaderOverride = fragmentOverride
						)
					)
					renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
				}
			}
		)

		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp)
		) {
			Button(onClick = onBack) {
				Text("Geri")
			}
		}

		TemporaryDebugPanel(
			modifier = Modifier
				.align(Alignment.BottomStart)
				.padding(12.dp)
		)
	}
}
