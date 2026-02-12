package com.example.wallpaper.render

import android.content.Context

object WallpaperShaderAssetLoader {
	fun loadFragment(context: Context, assetPath: String?): String? {
		if (assetPath.isNullOrBlank()) return null
		return runCatching {
			context.assets.open(assetPath).bufferedReader().use { it.readText() }
		}.getOrNull()
	}
}
