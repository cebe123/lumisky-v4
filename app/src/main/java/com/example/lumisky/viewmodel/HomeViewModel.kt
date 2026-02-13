package com.example.lumisky.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.core.api.SunDaylight
import com.example.core.api.SunLocation
import com.example.core.api.SunTimesRepository
import com.example.core.location.LastKnownLocationProvider
import com.example.core.settings.AppSettingsRepository
import com.example.core.settings.AppThemeMode
import com.example.core.settings.LocationMode
import com.example.core.settings.ManualCity
import com.example.engine.config.WallpaperConfig
import com.example.lumisky.data.WallpaperCatalog
import com.example.snapshot.SnapshotProvider
import java.security.MessageDigest
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

data class HomeWallpaperItem(
	val config: WallpaperConfig,
	val snapshotPath: String?
)

class HomeViewModel(
	context: Context,
	private val snapshotProvider: SnapshotProvider,
	private val sunTimesRepository: SunTimesRepository = SunTimesRepository(),
	private val settingsRepository: AppSettingsRepository = AppSettingsRepository(context),
	private val lastKnownLocationProvider: LastKnownLocationProvider = LastKnownLocationProvider(context)
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

	var appThemeMode by mutableStateOf(settingsRepository.getAppThemeMode())
		private set

	var languageTag by mutableStateOf(settingsRepository.getLanguageTag())
		private set

	var highRefreshEnabled by mutableStateOf(settingsRepository.isHighRefreshEnabled())
		private set

	var locationMode by mutableStateOf(settingsRepository.getLocationMode())
		private set

	var manualCity by mutableStateOf(settingsRepository.getManualCity())
		private set

	var locationLabel by mutableStateOf(manualCity.name)
		private set

	var gpsLocationAvailable by mutableStateOf(false)
		private set

	var systemLocationEnabled by mutableStateOf(lastKnownLocationProvider.isLocationEnabled())
		private set

	var startupLoading by mutableStateOf(true)
		private set

	private var lastEffectiveLocation: SunLocation? = null
	private val sunTimesRefreshRunnable = object : Runnable {
		override fun run() {
			refreshSunTimes()
			schedulePeriodicSunTimesRefresh()
		}
	}

	init {
		snapshotProvider.setOnSnapshotUpdatedListener { wallpaperId ->
			mainHandler.post {
				updateSnapshotPath(wallpaperId)
			}
		}
		refreshLocationState()
		seedInitialCatalog(daylight)
		bootstrapCatalogAndSnapshots(daylight)
		refreshSunTimes()
		schedulePeriodicSunTimesRefresh()
	}

	fun onWallpaperSelected(id: String) {
		selectedWallpaperId = id
		liveWallpaperId = null
		snapshotProvider.accelerateSnapshotGeneration(listOf(configFor(id)))
	}

	fun activateLivePreview(id: String) {
		if (liveWallpaperId == id) return
		liveWallpaperId = id
		snapshotProvider.accelerateSnapshotGeneration(listOf(configFor(id)))
	}

	fun clearLivePreview() {
		liveWallpaperId = null
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
	}

	fun updateHighRefreshEnabled(enabled: Boolean) {
		if (highRefreshEnabled == enabled) return
		highRefreshEnabled = enabled
		settingsRepository.setHighRefreshEnabled(enabled)
	}

	fun updateLocationMode(mode: LocationMode) {
		if (locationMode == mode) return
		locationMode = mode
		settingsRepository.setLocationMode(mode)
		refreshLocationState()
		refreshSunTimes()
	}

	fun updateManualCity(city: ManualCity) {
		manualCity = city
		settingsRepository.setManualCity(city)
		refreshLocationState()
		refreshSunTimes()
	}

	fun refreshLocationState() {
		systemLocationEnabled = lastKnownLocationProvider.isLocationEnabled()
		val liveGps = if (systemLocationEnabled) {
			lastKnownLocationProvider.getLastKnownLocation(label = "gps_live")
		} else {
			null
		}
		val lastGps = lastKnownLocationProvider.getLastKnownLocation(
			label = "gps_last",
			allowWhenLocationDisabled = true
		)
		gpsLocationAvailable = liveGps != null

		locationLabel = when {
			locationMode == LocationMode.GPS && liveGps != null -> formatGpsLabel(liveGps)
			locationMode == LocationMode.GPS && lastGps != null -> "Last GPS ${formatGpsLabel(lastGps)}"
			locationMode == LocationMode.GPS -> "${manualCity.name} (Manual fallback)"
			else -> manualCity.name
		}
	}

	fun onSystemLocationProviderChanged() {
		val before = systemLocationEnabled
		refreshLocationState()
		if (locationMode == LocationMode.GPS && before != systemLocationEnabled) {
			refreshSunTimes()
		}
	}

	fun refreshLocationAndSunTimes() {
		refreshLocationState()
		refreshSunTimes()
	}

	fun configFor(id: String): WallpaperConfig {
		return _items.firstOrNull { it.config.id == id }?.config
			?: WallpaperCatalog.configById(id, daylight)
	}

	fun allConfigs(): List<WallpaperConfig> = _items.map { it.config }

	fun release() {
		mainHandler.removeCallbacks(sunTimesRefreshRunnable)
		snapshotProvider.setOnSnapshotUpdatedListener(null)
		catalogExecutor.shutdownNow()
		sunTimesRepository.release()
	}

	private fun refreshSunTimes() {
		val candidates = resolveLocationCandidates()
		if (candidates.isEmpty()) return
		lastEffectiveLocation = candidates.firstOrNull()
		sunTimesRepository.refreshAsyncWithCandidates(candidates) { fetched ->
			mainHandler.post {
				if (fetched == daylight) return@post
				daylight = fetched
				rebuildCatalog(fetched)
			}
		}
	}

	private fun bootstrapCatalogAndSnapshots(initialDaylight: SunDaylight) {
		catalogExecutor.execute {
			val configs = WallpaperCatalog.buildConfigs(daylight = initialDaylight)
			val fingerprint = buildCatalogFingerprint(configs)
			val shouldBootstrapBlocking = shouldRunSnapshotBootstrap(fingerprint)
			if (shouldBootstrapBlocking) {
				snapshotProvider.generateSnapshotsBlocking(
					configs = configs.take(STARTUP_BLOCKING_SNAPSHOT_COUNT),
					timeoutMs = STARTUP_BLOCKING_TIMEOUT_MS
				)
				settingsRepository.setSnapshotCatalogFingerprint(fingerprint)
				settingsRepository.setSnapshotBootstrapCompleted(true)
			}

			if (snapshotProvider.hasMissingSnapshots(configs)) {
				snapshotProvider.generateSnapshots(configs)
			}

			postCatalog(configs)
			mainHandler.post {
				startupLoading = false
			}
		}
	}

	private fun seedInitialCatalog(currentDaylight: SunDaylight) {
		val configs = WallpaperCatalog.buildConfigs(daylight = currentDaylight)
		val mapped = configs.map { config ->
			HomeWallpaperItem(
				config = config,
				snapshotPath = null
			)
		}
		_items.clear()
		_items.addAll(mapped)
		if (selectedWallpaperId == null && _items.isNotEmpty()) {
			selectedWallpaperId = _items.first().config.id
		}
	}

	private fun rebuildCatalog(currentDaylight: SunDaylight) {
		catalogExecutor.execute {
			val configs = WallpaperCatalog.buildConfigs(daylight = currentDaylight)
			if (snapshotProvider.hasMissingSnapshots(configs)) {
				snapshotProvider.generateSnapshots(configs)
			}
			postCatalog(configs)
		}
	}

	private fun postCatalog(configs: List<WallpaperConfig>) {
		val mapped = configs.map { config ->
			HomeWallpaperItem(
				config = config,
				snapshotPath = snapshotProvider.getSnapshotPath(config)
			)
		}
		mainHandler.post {
			_items.clear()
			_items.addAll(mapped)
			val selectedStillExists = selectedWallpaperId != null &&
				_items.any { it.config.id == selectedWallpaperId }
			if (!selectedStillExists && _items.isNotEmpty()) {
				selectedWallpaperId = _items.first().config.id
			}
		}
	}

	private fun updateSnapshotPath(wallpaperId: String) {
		val index = _items.indexOfFirst { it.config.id == wallpaperId }
		if (index == -1) return
		val current = _items[index]
		val latestPath = snapshotProvider.getSnapshotPath(current.config) ?: return
		if (current.snapshotPath == latestPath) return
		_items[index] = current.copy(snapshotPath = latestPath)
	}

	private fun resolveLocationCandidates(): List<SunLocation> {
		val manual = SunLocation(
			label = manualCity.name,
			latitude = manualCity.latitude,
			longitude = manualCity.longitude
		)
		val defaultCity = resolveDefaultCity()

		return when (locationMode) {
			LocationMode.MANUAL -> listOf(manual, defaultCity)
			LocationMode.GPS -> buildList {
				val liveGps = if (systemLocationEnabled) {
					lastKnownLocationProvider.getLastKnownLocation(label = "gps_live")
				} else {
					null
				}
				val lastGps = lastKnownLocationProvider.getLastKnownLocation(
					label = "gps_last",
					allowWhenLocationDisabled = true
				)
				liveGps?.let { add(it) }
				lastGps?.let { add(it) }
				lastEffectiveLocation?.let { add(it) }
				add(manual)
				add(defaultCity)
			}.distinctBy { candidate ->
				"${candidate.latitude}|${candidate.longitude}"
			}
		}
	}

	private fun resolveDefaultCity(): SunLocation {
		return SunLocation(
			label = DEFAULT_CITY.label,
			latitude = DEFAULT_CITY.latitude,
			longitude = DEFAULT_CITY.longitude
		)
	}

	private fun formatGpsLabel(location: SunLocation): String {
		return "(${location.latitude.toDisplay()}, ${location.longitude.toDisplay()})"
	}

	private fun schedulePeriodicSunTimesRefresh() {
		mainHandler.removeCallbacks(sunTimesRefreshRunnable)
		mainHandler.postDelayed(sunTimesRefreshRunnable, SUN_TIMES_REFRESH_INTERVAL_MS)
	}

	private fun shouldRunSnapshotBootstrap(catalogFingerprint: String): Boolean {
		if (!settingsRepository.isSnapshotBootstrapCompleted()) return true
		val previousFingerprint = settingsRepository.getSnapshotCatalogFingerprint()
		return previousFingerprint != catalogFingerprint
	}

	private fun buildCatalogFingerprint(configs: List<WallpaperConfig>): String {
		val digest = MessageDigest.getInstance("SHA-1")
		configs.forEach { config ->
			digest.update(config.id.toByteArray(Charsets.UTF_8))
			digest.update((config.shader.fragmentAssetPath ?: "").toByteArray(Charsets.UTF_8))
			digest.update((config.shader.mode).toByteArray(Charsets.UTF_8))
			digest.update((config.textures.backgroundTexture ?: "").toByteArray(Charsets.UTF_8))
			digest.update((config.textures.sunTexture).toByteArray(Charsets.UTF_8))
			digest.update((config.textures.moonTexture).toByteArray(Charsets.UTF_8))
		}
		return digest.digest().joinToString("") { "%02x".format(it) }
	}

	private fun Double.toDisplay(): String = String.format(Locale.US, "%.2f", this)

	companion object {
		private val DEFAULT_CITY = SunLocation(
			label = "default_city",
			latitude = 41.0082,
			longitude = 28.9784
		)
		private const val SUN_TIMES_REFRESH_INTERVAL_MS = 60L * 60L * 1000L
		private const val STARTUP_BLOCKING_TIMEOUT_MS = 5_000L
		private const val STARTUP_BLOCKING_SNAPSHOT_COUNT = 12
	}
}
