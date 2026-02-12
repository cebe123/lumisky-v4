package com.example.lumisky.ui.home

import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine.config.WallpaperConfig
import com.example.engine.preview.PreviewGlRenderer
import com.example.engine.renderer.RenderMode
import com.example.lumisky.shader.ShaderAssetLoader
import com.example.lumisky.ui.debug.TemporaryDebugPanel
import com.example.lumisky.viewmodel.HomeWallpaperItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun HomeScreen(
	items: List<HomeWallpaperItem>,
	selectedWallpaperId: String?,
	liveWallpaperId: String?,
	daylightLabel: String,
	onWallpaperSelected: (String) -> Unit,
	onFocusReady: (String) -> Unit,
	onOpenPreview: (String) -> Unit,
	onOpenWallpaperPicker: () -> Unit
) {
	LaunchedEffect(selectedWallpaperId) {
		val id = selectedWallpaperId ?: return@LaunchedEffect
		delay(1_000L)
		onFocusReady(id)
	}

	Box(modifier = Modifier.fillMaxSize()) {
		Surface(modifier = Modifier.fillMaxSize()) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(16.dp),
				verticalArrangement = Arrangement.spacedBy(12.dp)
			) {
				Text(
					text = "Wallpaper library (${items.size})",
					style = MaterialTheme.typography.titleMedium
				)
				Text(
					text = "Sun API: $daylightLabel",
					style = MaterialTheme.typography.bodySmall
				)

				LazyColumn(
					modifier = Modifier
						.weight(1f)
						.fillMaxWidth(),
					verticalArrangement = Arrangement.spacedBy(10.dp)
				) {
					items(items, key = { it.config.id }) { item ->
						val isSelected = item.config.id == selectedWallpaperId
						val isLive = item.config.id == liveWallpaperId
						WallpaperRow(
							item = item,
							isSelected = isSelected,
							isLive = isLive,
							onClick = { onWallpaperSelected(item.config.id) },
							onOpenPreview = { onOpenPreview(item.config.id) }
						)
					}
				}

				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					Button(
						onClick = {
							selectedWallpaperId?.let { onOpenPreview(it) }
						},
						enabled = selectedWallpaperId != null,
						modifier = Modifier.weight(1f)
					) {
						Text("Preview")
					}
					OutlinedButton(
						onClick = onOpenWallpaperPicker,
						modifier = Modifier.weight(1f)
					) {
						Text("Set Wallpaper")
					}
				}
			}
		}

		TemporaryDebugPanel(
			modifier = Modifier
				.align(Alignment.BottomStart)
				.padding(12.dp)
		)
	}
}

@Composable
private fun WallpaperRow(
	item: HomeWallpaperItem,
	isSelected: Boolean,
	isLive: Boolean,
	onClick: () -> Unit,
	onOpenPreview: () -> Unit
) {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
	) {
		Column(modifier = Modifier.fillMaxWidth()) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(12.dp),
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				SnapshotThumbnail(
					path = item.snapshotPath,
					modifier = Modifier
						.height(72.dp)
						.weight(0.35f)
				)

				Column(
					modifier = Modifier.weight(0.65f),
					verticalArrangement = Arrangement.spacedBy(6.dp)
				) {
					Text(item.config.name, style = MaterialTheme.typography.titleSmall)
					Text("ID: ${item.config.id}", style = MaterialTheme.typography.bodySmall)
					Text(
						text = if (isLive) "State: LIVE" else if (isSelected) "State: SELECTED" else "State: SNAPSHOT",
						style = MaterialTheme.typography.bodySmall
					)
					OutlinedButton(onClick = onOpenPreview) {
						Text("Open Preview")
					}
				}
			}

			if (isLive) {
				FocusedWallpaperPreview(
					config = item.config,
					modifier = Modifier
						.fillMaxWidth()
						.height(160.dp)
				)
			}
		}
	}
}

@Composable
private fun FocusedWallpaperPreview(
	config: WallpaperConfig,
	modifier: Modifier = Modifier
) {
	AndroidView(
		modifier = modifier,
		factory = { context ->
			val fragmentOverride = ShaderAssetLoader.loadFragment(
				context = context,
				assetPath = config.shader.fragmentAssetPath
			)
			val renderer = PreviewGlRenderer(
				config = config,
				mode = RenderMode.FOCUS,
				animateFullDayLoop = false,
				focusCatchUpEnabled = true,
				fragmentShaderOverride = fragmentOverride
			)
			object : GLSurfaceView(context) {
				private val frameTicker = object : Runnable {
					override fun run() {
						requestRender()
						if (renderer.shouldContinueRendering()) {
							postDelayed(this, FRAME_INTERVAL_MS)
						}
					}
				}

				override fun onAttachedToWindow() {
					super.onAttachedToWindow()
					post(frameTicker)
				}

				override fun onDetachedFromWindow() {
					removeCallbacks(frameTicker)
					super.onDetachedFromWindow()
				}
			}.apply {
				setEGLContextClientVersion(2)
				setRenderer(renderer)
				renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
			}
		}
	)
}

@Composable
private fun SnapshotThumbnail(
	path: String?,
	modifier: Modifier = Modifier
) {
	val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, key1 = path) {
		value = withContext(Dispatchers.IO) {
			decodeSnapshot(path)
		}
	}
	if (bitmap != null) {
		Image(
			bitmap = bitmap!!.asImageBitmap(),
			contentDescription = null,
			modifier = modifier
		)
	} else {
		Box(
			modifier = modifier.background(Color(0xFF1F2C44)),
			contentAlignment = Alignment.Center
		) {
			Text("No snapshot", color = Color.White)
		}
	}
}

private fun decodeSnapshot(path: String?) = when {
	path.isNullOrBlank() -> null
	path.startsWith("data:image") -> decodeDataUri(path)
	else -> decodeFile(path)
}

private fun decodeFile(path: String) = runCatching {
	val file = File(path)
	if (!file.exists()) return@runCatching null
	BitmapFactory.decodeFile(file.absolutePath)
}.getOrNull()

private fun decodeDataUri(dataUri: String) = runCatching {
	val payload = dataUri.substringAfter("base64,", missingDelimiterValue = "")
	if (payload.isBlank()) return@runCatching null
	val bytes = Base64.decode(payload, Base64.DEFAULT)
	BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}.getOrNull()

private const val FRAME_INTERVAL_MS = 16L
