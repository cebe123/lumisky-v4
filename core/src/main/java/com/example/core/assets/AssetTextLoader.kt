package com.example.core.assets

import android.content.Context

object AssetTextLoader {
	fun load(context: Context, assetPath: String?): String? {
		if (assetPath.isNullOrBlank()) return null
		return runCatching {
			context.assets.open(assetPath).bufferedReader().use { it.readText() }
		}.getOrNull()
	}
}
