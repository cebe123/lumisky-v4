package com.example.wallpaper.render

import android.content.Context
import com.example.core.assets.AssetTextLoader

object WallpaperShaderAssetLoader {
	fun loadFragment(context: Context, assetPath: String?): String? {
		return AssetTextLoader.load(context, assetPath)
	}
}
