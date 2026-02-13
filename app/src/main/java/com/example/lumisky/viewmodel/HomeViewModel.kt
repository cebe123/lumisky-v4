package com.example.lumisky.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.core.Logger
import com.example.core.api.SunDaylight
import com.example.core.api.SunLocation
import com.example.core.api.SunTimesRepository
import com.example.core.location.LastKnownLocationProvider
import com.example.core.settings.AppSettingsRepository
import com.example.core.settings.AppThemeMode
import com.example.core.settings.LocationMode
import com.example.core.settings.ManualCity
import com.example.core.settings.PerformanceMode
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
	private val tag = "HomeViewModel"
	private val mainHandler = Handler(Looper.getMainLooper())
	private val catalogExecutor: ExecutorService = Executors.newSingleThreadExecutor()
	private val locationLabelExecutor: ExecutorService = Executors.newSingleThreadExecutor()
	private val _items = mutableStateListOf<HomeWallpaperItem>()
	private var lastFocusedCategoryKey: String? = null

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

	var performanceMode by mutableStateOf(settingsRepository.getPerformanceMode())
		private set

	var locationMode by mutableStateOf(settingsRepository.getLocationMode())
		private set

	var manualCity by mutableStateOf(settingsRepository.getManualCity())
		private set

	var locationLabel by mutableStateOf(manualCity.name)
		private set

	var gpsLocationAvailable by mutableStateOf(false)
		private set

	var systemLocationEnabled by mutableStateOf(
		runCatching { lastKnownLocationProvider.isLocationEnabled() }.getOrDefault(false)
	)
		private set

	var startupLoading by mutableStateOf(true)
		private set

	var startupProgress by mutableStateOf(0f)
		private set

	private var lastEffectiveLocation: SunLocation? = null
	private var liveGpsLocation: SunLocation? = null
	private var gpsPlaceLabel: String? = null
	private var lastGpsPlaceKey: String? = null
	private val sunTimesRefreshRunnable = object : Runnable {
		override fun run() {
			refreshSunTimes()
			schedulePeriodicSunTimesRefresh()
		}
	}

	init {
		snapshotProvider.setPerformanceMode(performanceMode)
		snapshotProvider.setOnSnapshotUpdatedListener { _ ->
			mainHandler.post {
				refreshSnapshotPaths()
			}
		}
		refreshLocationState()
		seedInitialCatalog(daylight)
		bootstrapCatalogAndSnapshots(daylight)
		refreshSunTimes()
		schedulePeriodicSunTimesRefresh()
		Logger.event(
			tag = tag,
			name = "init",
			"items" to _items.size,
			"locationMode" to locationMode,
			"manualCity" to manualCity.name,
			"performanceMode" to performanceMode
		)
	}

	fun onWallpaperSelected(id: String) {
		selectedWallpaperId = id
		liveWallpaperId = id
		Logger.event(tag, "wallpaper_selected", "id" to id)
		snapshotProvider.accelerateSnapshotGeneration(listOf(configFor(id)))
	}

	fun activateLivePreview(id: String) {
		if (liveWallpaperId == id) return
		liveWallpaperId = id
		Logger.event(tag, "live_preview_activated", "id" to id)
		snapshotProvider.accelerateSnapshotGeneration(listOf(configFor(id)))
	}

	fun onCategoryFocused(categoryWallpaperIds: List<String>) {
		if (categoryWallpaperIds.isEmpty()) return
		val normalized = categoryWallpaperIds.distinct()
		val key = normalized.joinToString(separator = "|")
		if (key == lastFocusedCategoryKey) return
		lastFocusedCategoryKey = key
		val byId = _items.associateBy { it.config.id }
		val focusedConfigs = normalized.mapNotNull { id -> byId[id]?.config }
		if (focusedConfigs.isEmpty()) return
		Logger.event(
			tag,
			"category_focus_prioritize",
			"wallpaperCount" to focusedConfigs.size
		)
		snapshotProvider.prioritizeSnapshotGeneration(focusedConfigs)
	}

	fun clearLivePreview() {
		liveWallpaperId = null
		Logger.d(tag, "live preview cleared")
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
		Logger.event(this.tag, "language_updated", "tag" to normalized)
	}

	fun updateHighRefreshEnabled(enabled: Boolean) {
		if (highRefreshEnabled == enabled) return
		highRefreshEnabled = enabled
		settingsRepository.setHighRefreshEnabled(enabled)
		Logger.event(tag, "high_refresh_updated", "enabled" to enabled)
	}

	fun updatePerformanceMode(mode: PerformanceMode) {
		if (performanceMode == mode) return
		performanceMode = mode
		settingsRepository.setPerformanceMode(mode)
		snapshotProvider.setPerformanceMode(mode)
		Logger.event(tag, "performance_mode_updated", "mode" to mode)
	}

	fun updateLocationMode(mode: LocationMode) {
		if (locationMode == mode) return
		locationMode = mode
		settingsRepository.setLocationMode(mode)
		Logger.event(tag, "location_mode_updated", "mode" to mode)
		refreshLocationState()
		if (mode == LocationMode.GPS) {
			requestImmediateGpsLocation()
		}
		refreshSunTimes()
	}

	fun updateManualCity(city: ManualCity) {
		manualCity = city
		settingsRepository.setManualCity(city)
		gpsPlaceLabel = null
		lastGpsPlaceKey = null
		Logger.event(tag, "manual_city_updated", "city" to city.name)
		refreshLocationState()
		refreshSunTimes()
	}

	fun refreshLocationState() {
		runCatching {
			systemLocationEnabled = runCatching { lastKnownLocationProvider.isLocationEnabled() }
				.getOrDefault(false)
			if (locationMode != LocationMode.GPS) {
				liveGpsLocation = null
				val lastGps = runCatching {
					lastKnownLocationProvider.getLastKnownLocation(
						label = "gps_last_manual",
						allowWhenLocationDisabled = true
					)
				}.getOrNull()
				lastGps?.let { maybeResolveGpsPlaceLabel(it) }
				gpsLocationAvailable = lastGps != null
				locationLabel = if (lastGps != null) {
					"Last GPS ${gpsPlaceLabel ?: formatGpsLabel(lastGps)}"
				} else {
					"${manualCity.name} (Default)"
				}
				return
			}
			if (!systemLocationEnabled) {
				liveGpsLocation = null
			}
			val liveGps = if (systemLocationEnabled) {
				liveGpsLocation ?: runCatching {
					lastKnownLocationProvider.getLastKnownLocation(label = "gps_live")
				}.getOrNull()
			} else {
				null
			}
			val lastGps = runCatching {
				lastKnownLocationProvider.getLastKnownLocation(
					label = "gps_last",
					allowWhenLocationDisabled = true
				)
			}.getOrNull()
			val preferredGps = liveGps ?: lastGps
			preferredGps?.let { maybeResolveGpsPlaceLabel(it) }
			gpsLocationAvailable = liveGps != null || lastGps != null

			locationLabel = when {
				liveGps != null -> gpsPlaceLabel ?: formatGpsLabel(liveGps)
				lastGps != null -> "Last GPS ${gpsPlaceLabel ?: formatGpsLabel(lastGps)}"
				else -> "${manualCity.name} (Default)"
			}
			Logger.event(
				tag,
				"location_state",
				"mode" to locationMode,
				"systemEnabled" to systemLocationEnabled,
				"gpsAvailable" to gpsLocationAvailable,
				"label" to locationLabel
			)
		}.onFailure {
			// Safety fallback to avoid crashes during location/provider transitions.
			liveGpsLocation = null
			gpsLocationAvailable = false
			locationLabel = "${manualCity.name} (Default)"
			Logger.w(tag, "refreshLocationState fallback", it)
		}
	}

	fun onSystemLocationProviderChanged() {
		runCatching {
			val before = systemLocationEnabled
			refreshLocationState()
			Logger.event(
				tag,
				"system_location_provider_changed",
				"before" to before,
				"after" to systemLocationEnabled,
				"mode" to locationMode
			)
			if (locationMode != LocationMode.GPS) return
			if (!before && systemLocationEnabled) {
				requestImmediateGpsLocation()
			}
			if (before != systemLocationEnabled) {
				refreshSunTimes()
			}
		}
	}

	fun refreshLocationAndSunTimes() {
		refreshLocationState()
		if (locationMode == LocationMode.GPS) {
			requestImmediateGpsLocation()
		}
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
		locationLabelExecutor.shutdownNow()
		sunTimesRepository.release()
	}

	private fun refreshSunTimes() {
		val candidates = resolveLocationCandidates()
		if (candidates.isEmpty()) return
		Logger.event(
			tag,
			"refresh_suntimes_start",
			"candidateCount" to candidates.size,
			"firstLabel" to candidates.firstOrNull()?.label
		)
		lastEffectiveLocation = candidates.firstOrNull()
		sunTimesRepository.refreshAsyncWithCandidates(candidates) { fetched ->
			mainHandler.post {
				if (fetched == daylight) return@post
				Logger.event(
					tag,
					"refresh_suntimes_applied",
					"sunrise" to fetched.sunriseMinute,
					"sunset" to fetched.sunsetMinute
				)
				daylight = fetched
				rebuildCatalog(fetched)
			}
		}
	}

	private fun bootstrapCatalogAndSnapshots(initialDaylight: SunDaylight) {
		catalogExecutor.execute {
			applySnapshotStorageMigrationIfNeeded()
			val configs = WallpaperCatalog.buildConfigs(daylight = initialDaylight)
			val startupOrderedConfigs = prioritizeForStartup(configs)
			val criticalStartupConfigs = selectStartupCriticalConfigs(startupOrderedConfigs)
			Logger.event(
				tag,
				"startup_snapshot_begin",
				"totalConfigs" to configs.size,
				"criticalConfigs" to criticalStartupConfigs.size
			)
			val fingerprint = buildCatalogFingerprint(configs)
			val startupStartMs = SystemClock.elapsedRealtime()
			val startupDeadlineMs = startupStartMs + STARTUP_MAX_WAIT_MS
			publishStartupProgress(
				configs = criticalStartupConfigs,
				startMs = startupStartMs,
				deadlineMs = startupDeadlineMs
			)
			while (
				snapshotProvider.hasMissingSnapshots(criticalStartupConfigs) &&
				SystemClock.elapsedRealtime() < startupDeadlineMs
			) {
				snapshotProvider.generateSnapshotsBlocking(
					configs = criticalStartupConfigs,
					timeoutMs = STARTUP_BLOCKING_CHUNK_MS,
					strict = true
				)
				publishStartupProgress(
					configs = criticalStartupConfigs,
					startMs = startupStartMs,
					deadlineMs = startupDeadlineMs
				)
			}

			publishStartupProgress(
				configs = criticalStartupConfigs,
				startMs = startupStartMs,
				deadlineMs = startupDeadlineMs
			)
			settingsRepository.setSnapshotCatalogFingerprint(fingerprint)
			settingsRepository.setSnapshotBootstrapCompleted(true)

			postCatalog(configs)
			mainHandler.post {
				startupProgress = 1f
				startupLoading = false
			}
			Logger.event(
				tag,
				"startup_snapshot_done",
				"elapsedMs" to (SystemClock.elapsedRealtime() - startupStartMs),
				"hasMissing" to snapshotProvider.hasMissingSnapshots(configs)
			)
			if (snapshotProvider.hasMissingSnapshots(configs)) {
				snapshotProvider.generateSnapshots(startupOrderedConfigs)
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

	private fun refreshSnapshotPaths() {
		if (_items.isEmpty()) return
		_items.indices.forEach { index ->
			val current = _items[index]
			val latestPath = snapshotProvider.getSnapshotPath(current.config)
			if (current.snapshotPath != latestPath) {
				_items[index] = current.copy(snapshotPath = latestPath)
			}
		}
	}

	private fun resolveLocationCandidates(): List<SunLocation> {
		val manual = SunLocation(
			label = manualCity.name,
			latitude = manualCity.latitude,
			longitude = manualCity.longitude
		)
		val defaultCity = resolveDefaultCity()

		return when (locationMode) {
			LocationMode.MANUAL -> buildList {
				val lastGps = runCatching {
					lastKnownLocationProvider.getLastKnownLocation(
						label = "gps_last_manual",
						allowWhenLocationDisabled = true
					)
				}.getOrNull()
				lastGps?.let { add(it) }
				add(manual)
				add(defaultCity)
			}.distinctBy { candidate ->
				"${candidate.latitude}|${candidate.longitude}"
			}
			LocationMode.GPS -> buildList {
				val liveGps = if (systemLocationEnabled) {
					liveGpsLocation ?: runCatching {
						lastKnownLocationProvider.getLastKnownLocation(label = "gps_live")
					}.getOrNull()
				} else {
					null
				}
				val lastGps = runCatching {
					lastKnownLocationProvider.getLastKnownLocation(
						label = "gps_last",
						allowWhenLocationDisabled = true
					)
				}.getOrNull()
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

	private fun requestImmediateGpsLocation() {
		if (locationMode != LocationMode.GPS || !systemLocationEnabled) return
		Logger.d(tag, "requestImmediateGpsLocation started")
		lastKnownLocationProvider.requestCurrentLocation(label = "gps_live") { location ->
			mainHandler.post {
				if (locationMode != LocationMode.GPS) return@post
				if (location != null) {
					liveGpsLocation = location
					Logger.event(tag, "gps_live_result", "lat" to location.latitude, "lon" to location.longitude)
				} else {
					Logger.w(tag, "gps_live_result null")
				}
				refreshLocationState()
				if (locationMode == LocationMode.GPS) {
					refreshSunTimes()
				}
			}
		}
	}

	private fun maybeResolveGpsPlaceLabel(location: SunLocation) {
		val key = gpsLocationKey(location)
		if (key == lastGpsPlaceKey && !gpsPlaceLabel.isNullOrBlank()) return
		lastGpsPlaceKey = key
		locationLabelExecutor.execute {
			val resolved = lastKnownLocationProvider.resolveCityOrDistrict(location) ?: return@execute
			mainHandler.post {
				if (key != lastGpsPlaceKey) return@post
				if (gpsPlaceLabel == resolved) return@post
				gpsPlaceLabel = resolved
				refreshLocationState()
			}
		}
	}

	private fun gpsLocationKey(location: SunLocation): String {
		return String.format(Locale.US, "%.3f|%.3f", location.latitude, location.longitude)
	}

	private fun applySnapshotStorageMigrationIfNeeded() {
		val currentVersion = settingsRepository.getSnapshotStorageVersion()
		if (currentVersion >= SNAPSHOT_STORAGE_VERSION) return
		Logger.event(
			tag,
			"snapshot_migration",
			"fromVersion" to currentVersion,
			"toVersion" to SNAPSHOT_STORAGE_VERSION
		)
		snapshotProvider.clearAllSnapshots()
		settingsRepository.clearSnapshotBootstrapState()
		settingsRepository.setSnapshotStorageVersion(SNAPSHOT_STORAGE_VERSION)
	}

	private fun publishStartupProgress(
		configs: List<WallpaperConfig>,
		startMs: Long,
		deadlineMs: Long
	) {
		val progress = snapshotProvider.snapshotProgress(configs)
		val now = SystemClock.elapsedRealtime()
		val timeProgress = if (deadlineMs > startMs) {
			((now - startMs).toFloat() / (deadlineMs - startMs).toFloat()).coerceIn(0f, 1f)
		} else {
			1f
		}
		val blended = maxOf(progress, timeProgress * STARTUP_TIME_PROGRESS_WEIGHT)
		mainHandler.post {
			startupProgress = blended.coerceIn(0f, 0.99f)
		}
	}

	private fun prioritizeForStartup(configs: List<WallpaperConfig>): List<WallpaperConfig> {
		if (configs.isEmpty()) return emptyList()
		val grouped = configs.groupBy { config -> startupCategoryOf(config) }
		val prioritized = buildList {
			STARTUP_CATEGORY_ORDER.forEach { category ->
				grouped[category]
					.orEmpty()
					.take(STARTUP_TOP_PER_CATEGORY)
					.forEach { add(it) }
			}
		}
		if (prioritized.isEmpty()) return configs
		val prioritizedIds = prioritized.asSequence().map { it.id }.toSet()
		val remaining = configs.filterNot { prioritizedIds.contains(it.id) }
		return prioritized + remaining
	}

	private fun selectStartupCriticalConfigs(configs: List<WallpaperConfig>): List<WallpaperConfig> {
		if (configs.isEmpty()) return emptyList()
		val grouped = configs.groupBy { config -> startupCategoryOf(config) }
		return buildList {
			STARTUP_CATEGORY_ORDER.forEach { category ->
				grouped[category]
					.orEmpty()
					.take(STARTUP_TOP_PER_CATEGORY)
					.forEach { add(it) }
			}
		}.distinctBy { config -> config.id }
	}

	private fun startupCategoryOf(config: WallpaperConfig): StartupCategory {
		return when {
			config.id.startsWith("city_") -> StartupCategory.CITIES
			config.id.startsWith("anime_") -> StartupCategory.ANIME
			config.id.startsWith("solar_horizon") ||
				config.id.startsWith("optical_sunset") ||
				config.id.startsWith("mars") -> StartupCategory.LANDSCAPES
			else -> StartupCategory.SPECIAL
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

	private enum class StartupCategory {
		SPECIAL,
		LANDSCAPES,
		CITIES,
		ANIME
	}

	companion object {
		private val STARTUP_CATEGORY_ORDER = listOf(
			StartupCategory.SPECIAL,
			StartupCategory.LANDSCAPES,
			StartupCategory.CITIES,
			StartupCategory.ANIME
		)
		private val DEFAULT_CITY = SunLocation(
			label = "default_city",
			latitude = 41.0082,
			longitude = 28.9784
		)
		private const val SUN_TIMES_REFRESH_INTERVAL_MS = 60L * 60L * 1000L
		private const val STARTUP_MAX_WAIT_MS = 10_000L
		private const val STARTUP_BLOCKING_CHUNK_MS = 500L
		private const val STARTUP_TOP_PER_CATEGORY = 3
		private const val STARTUP_TIME_PROGRESS_WEIGHT = 0.85f
		private const val SNAPSHOT_STORAGE_VERSION = 7
	}
}
