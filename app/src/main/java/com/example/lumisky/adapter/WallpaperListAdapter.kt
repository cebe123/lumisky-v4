package com.example.lumisky.adapter

data class WallpaperListItem(
	val id: String,
	val title: String,
	val snapshotPath: String?
)

class WallpaperListAdapter {
	fun submit(items: List<WallpaperListItem>) {
		// RecyclerView/LazyList entegrasyonu daha sonra eklenecek.
	}
}
