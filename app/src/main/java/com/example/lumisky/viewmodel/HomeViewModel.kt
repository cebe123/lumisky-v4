package com.example.lumisky.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import com.example.core.api.SunDaylight
import com.example.core.api.SunTimesRepository
import com.example.core.location.LastKnownLocationProvider
import com.example.core.settings.AppSettingsRepository
import com.example.core.settings.AppSettingsSnapshot
import com.example.core.settings.AppThemeMode
import com.example.core.settings.LocationMode
import com.example.core.settings.ManualCity
import com.example.core.settings.PerformanceMode
import com.example.engine.config.WallpaperConfig
import com.example.engine.config.WallpaperConfigStore
import com.example.lumisky.data.WallpaperCatalog
import com.example.lumisky.data.WallpaperCatalogRepository
import com.example.lumisky.work.BackupCityPrefetchWorker
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

data class HomeWallpaperItem(
	val config: WallpaperConfig
)

class HomeViewModel(
	context: Context,
	private val sunTimesRepository: SunTimesRepository = SunTimesRepository(),
	private val settingsRepository: AppSettingsRepository = AppSettingsRepository(context),
	private val wallpaperConfigStore: WallpaperConfigStore = WallpaperConfigStore(context),
	private val lastKnownLocationProvider: LastKnownLocationProvider = LastKnownLocationProvider(context),
	private val initialSettings: AppSettingsSnapshot = settingsRepository.snapshot()
) {
	private val appContext = context.applicationContext
	private val wallpaperCatalogRepository: WallpaperCatalogRepository =
		WallpaperCatalog.repository(context)
	private val mainHandler = Handler(Looper.getMainLooper())
	private val catalogExecutor: ExecutorService = Executors.newSingleThreadExecutor()
	private val _items = mutableStateListOf<HomeWallpaperItem>()
	private var lastFocusedCategoryKey: String? = null
	private var settingsChangeListenerHandle: AutoCloseable? = null
	private val storedWallpaperId: String? = wallpaperConfigStore.loadSelected()?.id

	private val locationCoordinator = LocationSunTimesCoordinator(
		context = appContext,
		sunTimesRepository = sunTimesRepository,
		settingsRepository = settingsRepository,
		lastKnownLocationProvider = lastKnownLocationProvider,
		initialSettings = initialSettings,
		onDaylightResolved = { newDaylight ->
			if (daylight != newDaylight) {
				daylight = newDaylight
				rebuildCatalog(newDaylight)
			}
			wallpaperDaylightSyncVersion += 1
		}
	)

	// ---- backup prefetch ----
	private var startupBackupPrefetchEnqueued = false

	val items: List<HomeWallpaperItem>
		get() = _items.toList()

	var selectedWallpaperId by mutableStateOf(storedWallpaperId)
		private set

	var liveWallpaperId by mutableStateOf(storedWallpaperId)
		private set

	private val initialDaylight: SunDaylight = sunTimesRepository.currentOrFallbackForCandidates(
		locationCoordinator.buildInitialLocationCandidates(initialSettings)
	)

	var daylight by mutableStateOf(initialDaylight)
		private set

	var wallpaperDaylightSyncVersion by mutableStateOf(0)
		private set

	var appThemeMode by mutableStateOf(initialSettings.appThemeMode)
		private set

	var languageTag by mutableStateOf(initialSettings.languageTag)
		private set

	var highRefreshEnabled by mutableStateOf(initialSettings.highRefreshEnabled)
		private set

	var performanceMode by mutableStateOf(initialSettings.performanceMode)
		private set

	// Delegated location state (read-only from coordinator)
	val locationMode: LocationMode get() = locationCoordinator.locationMode
	val manualCity: ManualCity get() = locationCoordinator.manualCity
	val lastKnownCity: ManualCity? get() = locationCoordinator.lastKnownCity
	val locationLabel: String get() = locationCoordinator.locationLabel
	val gpsLocationAvailable: Boolean get() = locationCoordinator.gpsLocationAvailable
	val systemLocationEnabled: Boolean get() = locationCoordinator.systemLocationEnabled
	val locationRefreshInProgress: Boolean get() = locationCoordinator.locationRefreshInProgress

	init {
		settingsChangeListenerHandle = settingsRepository.addChangeListener { snapshot ->
			mainHandler.post {
				applySettingsSnapshot(snapshot)
			}
		}
		seedInitialCatalog(daylight)
		locationCoordinator.init()
		enqueueBackupCityPrefetchIfNeeded()
	}

	fun activateLivePreview(id: String) {
		if (liveWallpaperId == id) return
		liveWallpaperId = id
	}

	fun onCategoryFocused(categoryWallpaperIds: List<String>) {
		if (categoryWallpaperIds.isEmpty()) return
		val normalized = categoryWallpaperIds
			.distinct()
			.take(CATEGORY_PRIORITIZE_LIMIT)
		val key = normalized.joinToString(separator = "|")
		if (key == lastFocusedCategoryKey) return
		lastFocusedCategoryKey = key
		enqueueBackupCityPrefetch()
	}

	fun clearLivePreview() {
		liveWallpaperId = null
	}

	fun onUserInteraction() {
		enqueueBackupCityPrefetchIfNeeded()
	}

	fun onForegroundStarted() {
		locationCoordinator.onForegroundStarted()
	}

	fun onForegroundStopped() {
		locationCoordinator.onForegroundStopped()
	}

	fun updateAppThemeMode(mode: AppThemeMode) {
		if (appThemeMode == mode) return
		appThemeMode = mode
		settingsRepository.setAppThemeMode(mode)
	}

	fun cycleAppThemeMode() {
		val next = when (appThemeMode) {
			AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
			AppThemeMode.LIGHT -> AppThemeMode.DARK
			AppThemeMode.DARK -> AppThemeMode.SYSTEM
		}
		updateAppThemeMode(next)
	}

	fun updateLanguageTag(tag: String) {
		val normalized = tag.ifBlank { "system" }
		if (languageTag == normalized) return
		languageTag = normalized
		settingsRepository.setLanguageTag(normalized)
		locationCoordinator.updateLanguageTag(normalized)
	}

	fun updateHighRefreshEnabled(enabled: Boolean) {
		if (highRefreshEnabled == enabled) return
		highRefreshEnabled = enabled
		settingsRepository.setHighRefreshEnabled(enabled)
	}

	fun updatePerformanceMode(mode: PerformanceMode) {
		if (performanceMode == mode) return
		performanceMode = mode
		settingsRepository.setPerformanceMode(mode)
	}

	fun updateLocationMode(mode: LocationMode) {
		locationCoordinator.updateLocationMode(mode)
	}

	fun refreshLocationNow() {
		locationCoordinator.refreshLocationNow()
	}

	fun updateManualCity(city: ManualCity) {
		locationCoordinator.updateManualCity(city)
		enqueueBackupCityPrefetch()
	}

	fun refreshLocationState() {
		locationCoordinator.refreshLocationState()
	}

	fun onSystemLocationProviderChanged() {
		locationCoordinator.onSystemLocationProviderChanged()
	}

	fun configFor(id: String): WallpaperConfig {
		return _items.firstOrNull { it.config.id == id }?.config
			?: wallpaperCatalogRepository.configById(id = id, daylight = daylight)
	}

	fun release() {
		settingsChangeListenerHandle?.close()
		settingsChangeListenerHandle = null
		locationCoordinator.release()
		catalogExecutor.shutdownNow()
	}

	// ---- wallpaper selection ----

	fun selectWallpaper(id: String) {
		if (selectedWallpaperId == id) return
		selectedWallpaperId = id
	}

	fun applySelectedWallpaper(config: WallpaperConfig) {
		selectedWallpaperId = config.id
		wallpaperConfigStore.saveSelected(config)
		// Broadcast is sent from MainActivity after apply flow
	}

	fun previewWallpaper(config: WallpaperConfig) {
		wallpaperConfigStore.savePreview(config)
	}

	fun clearPreviewWallpaper() {
		wallpaperConfigStore.clearPreview()
	}

	fun clearSelection() {
		if (selectedWallpaperId == storedWallpaperId) return
		selectedWallpaperId = storedWallpaperId
		if (liveWallpaperId != null && _items.none { it.config.id == liveWallpaperId }) {
			liveWallpaperId = null
		}
	}

	// ---- private catalog ----

	private fun applySettingsSnapshot(snapshot: AppSettingsSnapshot) {
		if (appThemeMode != snapshot.appThemeMode) {
			appThemeMode = snapshot.appThemeMode
		}
		if (languageTag != snapshot.languageTag) {
			languageTag = snapshot.languageTag
		}
		if (highRefreshEnabled != snapshot.highRefreshEnabled) {
			highRefreshEnabled = snapshot.highRefreshEnabled
		}
		if (performanceMode != snapshot.performanceMode) {
			performanceMode = snapshot.performanceMode
		}

		locationCoordinator.applySettingsChanges(
			newLocationMode = snapshot.locationMode.takeIf { it != locationMode },
			newManualCity = snapshot.manualCity.takeIf { it != manualCity },
			newAutomaticLocation = snapshot.automaticLocation,
			newLanguageTag = snapshot.languageTag.takeIf { it != languageTag }
		)
	}

	private fun seedInitialCatalog(currentDaylight: SunDaylight) {
		val configs = wallpaperCatalogRepository.buildConfigs(daylight = currentDaylight)
		publishItems(configs.map { config -> HomeWallpaperItem(config = config) })
		if (selectedWallpaperId != null && _items.none { it.config.id == selectedWallpaperId }) {
			selectedWallpaperId = null
		}
		if (liveWallpaperId != null && _items.none { it.config.id == liveWallpaperId }) {
			liveWallpaperId = null
		}
	}

	private fun rebuildCatalog(currentDaylight: SunDaylight) {
		catalogExecutor.execute {
			val configs = wallpaperCatalogRepository.buildConfigs(daylight = currentDaylight)
			postCatalog(configs)
		}
	}

	private fun postCatalog(configs: List<WallpaperConfig>) {
		val mapped = configs.map { config -> HomeWallpaperItem(config = config) }
		mainHandler.post {
			publishItems(mapped)
			if (selectedWallpaperId != null && _items.none { it.config.id == selectedWallpaperId }) {
				selectedWallpaperId = null
			}
			if (liveWallpaperId != null && _items.none { it.config.id == liveWallpaperId }) {
				liveWallpaperId = null
			}
		}
	}

	private fun publishItems(mapped: List<HomeWallpaperItem>): Boolean {
		if (_items.size == mapped.size && _items.indices.all { index -> _items[index] == mapped[index] }) {
			return false
		}
		Snapshot.withMutableSnapshot {
			_items.clear()
			_items.addAll(mapped)
		}
		return true
	}

	// ---- backup prefetch ----

	private fun enqueueBackupCityPrefetchIfNeeded() {
		if (startupBackupPrefetchEnqueued) return
		startupBackupPrefetchEnqueued = true
		enqueueBackupCityPrefetch()
	}

	private fun enqueueBackupCityPrefetch() {
		BackupCityPrefetchWorker.enqueue(
			context = appContext,
			maxCandidateCount = BACKUP_PREFETCH_STARTUP_CANDIDATE_LIMIT
		)
	}

	companion object {
		private const val CATEGORY_PRIORITIZE_LIMIT = 24
		private const val BACKUP_PREFETCH_STARTUP_CANDIDATE_LIMIT = 8
	}
}
