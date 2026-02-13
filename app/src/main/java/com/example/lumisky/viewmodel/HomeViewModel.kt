package com.example.lumisky.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.core.api.SunDaylight
import com.example.core.api.SunLocation
import com.example.core.api.SunTimesRepository
import com.example.engine.config.WallpaperConfig
import com.example.lumisky.data.WallpaperCatalog
import com.example.snapshot.SnapshotProvider
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

data class HomeWallpaperItem(
	val config: WallpaperConfig,
	val snapshotPath: String?
)

class HomeViewModel(
	private val snapshotProvider: SnapshotProvider,
	private val sunTimesRepository: SunTimesRepository = SunTimesRepository()
) {
	private val mainHandler = Handler(Looper.getMainLooper())
	private val catalogExecutor: ExecutorService = Executors.newSingleThreadExecutor()
	private val _items = mutableStateListOf<HomeWallpaperItem>()

	val items: List<HomeWallpaperItem>
		get() = _items

	var selectedWallpaperId by mutableStateOf<String?>(null)
		private set

	var liveWallpaperId by mutableStateOf<String?>(null)
		private set

	var daylight by mutableStateOf(sunTimesRepository.currentOrFallback())
		private set

	private var selectedCity: SunLocation? = null

	init {
		rebuildCatalog(daylight)
		refreshSunTimes()
	}

	fun onWallpaperSelected(id: String) {
		selectedWallpaperId = id
		liveWallpaperId = null
		snapshotProvider.generateSnapshots(listOf(configFor(id)))
	}

	fun activateLivePreview(id: String) {
		if (liveWallpaperId == id) return
		liveWallpaperId = id
		snapshotProvider.generateSnapshots(listOf(configFor(id)))
	}

	fun clearLivePreview() {
		liveWallpaperId = null
	}

	fun configFor(id: String): WallpaperConfig {
		return _items.firstOrNull { it.config.id == id }?.config
			?: WallpaperCatalog.configById(id, daylight)
	}

	fun allConfigs(): List<WallpaperConfig> = _items.map { it.config }

	fun setSelectedCity(location: SunLocation?) {
		selectedCity = location
		refreshSunTimes()
	}

	fun release() {
		catalogExecutor.shutdownNow()
	}

	private fun refreshSunTimes() {
		sunTimesRepository.refreshAsyncWithFallback(
			selectedCity = selectedCity,
			defaultCity = DEFAULT_CITY
		) { fetched ->
			mainHandler.post {
				daylight = fetched
				rebuildCatalog(fetched)
			}
		}
	}

	private fun rebuildCatalog(currentDaylight: SunDaylight) {
		catalogExecutor.execute {
			val configs = WallpaperCatalog.buildConfigs(daylight = currentDaylight)
			snapshotProvider.generateSnapshots(configs.take(INITIAL_SNAPSHOT_COUNT))
			val mapped = configs.map { config ->
				HomeWallpaperItem(
					config = config,
					snapshotPath = snapshotProvider.getSnapshotPath(config.id)
				)
			}
			mainHandler.post {
				_items.clear()
				_items.addAll(mapped)
				if (selectedWallpaperId == null && _items.isNotEmpty()) {
					selectedWallpaperId = _items.first().config.id
				}
			}
		}
	}

	companion object {
		private val DEFAULT_CITY = SunLocation(
			label = "default_city",
			latitude = 41.0082,
			longitude = 28.9784
		)
		private const val INITIAL_SNAPSHOT_COUNT = 10
	}
}
