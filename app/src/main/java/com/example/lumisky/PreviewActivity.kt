package com.example.lumisky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.example.core.api.SunDaylight
import com.example.lumisky.data.WallpaperCatalog
import com.example.lumisky.ui.preview.PreviewScreen

class PreviewActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val wallpaperId = intent.getStringExtra(EXTRA_WALLPAPER_ID) ?: "preview_default"
		val config = WallpaperCatalog.configById(
			id = wallpaperId,
			daylight = SunDaylight.fallback()
		)

		setContent {
			MaterialTheme {
				PreviewScreen(
					config = config,
					onBack = { finish() }
				)
			}
		}
	}

	companion object {
		const val EXTRA_WALLPAPER_ID = "extra_wallpaper_id"
	}
}
