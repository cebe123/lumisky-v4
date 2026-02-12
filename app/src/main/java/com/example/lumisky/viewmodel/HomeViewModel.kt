package com.example.lumisky.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.core.api.SunDaylight
import com.example.core.api.SunTimesRepository
import com.example.engine.config.WallpaperConfig
import com.example.lumisky.data.WallpaperCatalog
import com.example.snapshot.SnapshotProvider

data class HomeWallpaperItem(
	val config: WallpaperConfig,
	val snapshotPath: String?
)

class HomeViewModel(
	private val snapshotProvider: SnapshotProvider,
	private val sunTimesRepository: SunTimesRepository = SunTimesRepository()
) {
	private val mainHandler = Handler(Looper.getMainLooper())
	private val _items = mutableStateListOf<HomeWallpaperItem>()

	val items: List<HomeWallpaperItem>
		get() = _items

	var selectedWallpaperId by mutableStateOf<String?>(null)
		private set

	var liveWallpaperId by mutableStateOf<String?>(null)
		private set

	var daylight by mutableStateOf(sunTimesRepository.currentOrFallback())
		private set

	init {
		rebuildCatalog(daylight)
		refreshSunTimes()
	}

	fun onWallpaperSelected(id: String) {
		selectedWallpaperId = id
		liveWallpaperId = null
	}

	fun activateLivePreview(id: String) {
		if (selectedWallpaperId != id) return
		liveWallpaperId = id
	}

	fun configFor(id: String): WallpaperConfig {
		return _items.firstOrNull { it.config.id == id }?.config
			?: WallpaperCatalog.configById(id, daylight)
	}

	fun allConfigs(): List<WallpaperConfig> = _items.map { it.config }

	private fun refreshSunTimes() {
		// Default location can be replaced by device location service later.
		sunTimesRepository.refreshAsync(
			latitude = DEFAULT_LATITUDE,
			longitude = DEFAULT_LONGITUDE
		) { fetched ->
			mainHandler.post {
				daylight = fetched
				rebuildCatalog(fetched)
			}
		}
	}

	private fun rebuildCatalog(currentDaylight: SunDaylight) {
		val configs = WallpaperCatalog.buildConfigs(daylight = currentDaylight)
		snapshotProvider.generateSnapshots(configs)
		_items.clear()
		_items.addAll(
			configs.map { config ->
				HomeWallpaperItem(
					config = config,
					snapshotPath = snapshotProvider.getSnapshotPath(config.id)
				)
			}
		)

		if (selectedWallpaperId == null && _items.isNotEmpty()) {
			selectedWallpaperId = _items.first().config.id
		}
	}

	companion object {
		private const val DEFAULT_LATITUDE = 41.0082
		private const val DEFAULT_LONGITUDE = 28.9784
	}
}
