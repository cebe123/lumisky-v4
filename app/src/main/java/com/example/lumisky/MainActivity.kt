package com.example.lumisky

import android.app.WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.example.engine.SkyEngine
import com.example.lumisky.ui.home.HomeScreen
import com.example.lumisky.viewmodel.HomeViewModel
import com.example.snapshot.SnapshotProvider

class MainActivity : ComponentActivity() {

	private val engine = SkyEngine()
	private val snapshotProvider by lazy { SnapshotProvider(applicationContext) }
	private val homeViewModel by lazy { HomeViewModel(snapshotProvider) }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		engine.init()
		snapshotProvider.warmUp()

		setContent {
			MaterialTheme {
				HomeScreen(
					items = homeViewModel.items,
					selectedWallpaperId = homeViewModel.selectedWallpaperId,
					liveWallpaperId = homeViewModel.liveWallpaperId,
					daylightLabel = "${homeViewModel.daylight.sunriseMinute} / ${homeViewModel.daylight.sunsetMinute}",
					onWallpaperSelected = { id ->
						homeViewModel.onWallpaperSelected(id)
					},
					onFocusReady = { id ->
						homeViewModel.activateLivePreview(id)
					},
					onOpenPreview = { id ->
						startActivity(
							Intent(this, PreviewActivity::class.java)
								.putExtra(PreviewActivity.EXTRA_WALLPAPER_ID, id)
						)
					},
					onOpenWallpaperPicker = {
						startActivity(Intent(ACTION_LIVE_WALLPAPER_CHOOSER))
					}
				)
			}
		}
	}

	override fun onDestroy() {
		snapshotProvider.release()
		engine.release()
		super.onDestroy()
	}
}
