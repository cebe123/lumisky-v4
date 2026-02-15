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
import com.example.core.settings.AppSettingsDefaults
import com.example.core.settings.AppSettingsRepository
import com.example.core.settings.AppThemeMode
import com.example.core.settings.LocationMode
import com.example.core.settings.ManualCity
import com.example.core.settings.PerformanceMode
import com.example.engine.config.WallpaperConfig
import com.example.lumisky.data.WallpaperCatalog
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

data class HomeWallpaperItem(
	val config: WallpaperConfig
)

class HomeViewModel(
	context: Context,
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
	private var lastGpsRequestAtMs: Long = 0L
	private var lastLocationStateRefreshAtMs: Long = 0L

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

	var startupLoading by mutableStateOf(false)
		private set

	var startupProgress by mutableStateOf(1f)
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
	private val backupCityRefreshRunnable = object : Runnable {
		override fun run() {
			prefetchBackupCityCache()
			schedulePeriodicBackupCityRefresh()
		}
	}

	init {
		refreshLocationState()
		seedInitialCatalog(daylight)
		refreshSunTimes()
		prefetchBackupCityCache()
		schedulePeriodicSunTimesRefresh()
		schedulePeriodicBackupCityRefresh()
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
	}

	fun activateLivePreview(id: String) {
		if (liveWallpaperId == id) return
		liveWallpaperId = id
		Logger.event(tag, "live_preview_activated", "id" to id)
	}

	fun onCategoryFocused(categoryWallpaperIds: List<String>) {
		if (categoryWallpaperIds.isEmpty()) return
		val normalized = categoryWallpaperIds
			.distinct()
			.take(CATEGORY_PRIORITIZE_LIMIT)
		val key = normalized.joinToString(separator = "|")
		if (key == lastFocusedCategoryKey) return
		lastFocusedCategoryKey = key
		Logger.event(
			tag,
			"category_focus_prioritize",
			"wallpaperCount" to normalized.size
		)
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
		prefetchBackupCityCache()
	}

	fun refreshLocationState() {
		val refreshAtMs = SystemClock.elapsedRealtime()
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
			liveGpsLocation = null
			gpsLocationAvailable = false
			locationLabel = "${manualCity.name} (Default)"
			Logger.w(tag, "refreshLocationState fallback", it)
		}
		lastLocationStateRefreshAtMs = refreshAtMs
	}

	fun refreshOnForegroundIfStale(
		maxStaleMs: Long = FOREGROUND_LOCATION_REFRESH_STALE_MS
	) {
		val elapsed = SystemClock.elapsedRealtime() - lastLocationStateRefreshAtMs
		if (elapsed in 0 until maxStaleMs) {
			Logger.d(tag, "foreground refresh skipped elapsedMs=$elapsed thresholdMs=$maxStaleMs")
			return
		}
		onSystemLocationProviderChanged()
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
			if (systemLocationEnabled && (!before || !gpsLocationAvailable)) {
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
		mainHandler.removeCallbacks(backupCityRefreshRunnable)
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

	private fun seedInitialCatalog(currentDaylight: SunDaylight) {
		val configs = WallpaperCatalog.buildConfigs(daylight = currentDaylight)
		_items.clear()
		_items.addAll(configs.map { config -> HomeWallpaperItem(config = config) })
		if (selectedWallpaperId == null && _items.isNotEmpty()) {
			selectedWallpaperId = _items.first().config.id
		}
	}

	private fun rebuildCatalog(currentDaylight: SunDaylight) {
		catalogExecutor.execute {
			val configs = WallpaperCatalog.buildConfigs(daylight = currentDaylight)
			postCatalog(configs)
		}
	}

	private fun postCatalog(configs: List<WallpaperConfig>) {
		val mapped = configs.map { config -> HomeWallpaperItem(config = config) }
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
		val now = SystemClock.elapsedRealtime()
		if ((now - lastGpsRequestAtMs) < GPS_REQUEST_THROTTLE_MS) return
		lastGpsRequestAtMs = now
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

	private fun schedulePeriodicBackupCityRefresh() {
		mainHandler.removeCallbacks(backupCityRefreshRunnable)
		mainHandler.postDelayed(backupCityRefreshRunnable, BACKUP_CITY_REFRESH_INTERVAL_MS)
	}

	private fun prefetchBackupCityCache() {
		val defaultCity = resolveDefaultCity()
		val supportedCities = AppSettingsDefaults.SUPPORTED_CITIES.map { city ->
			SunLocation(
				label = city.name,
				latitude = city.latitude,
				longitude = city.longitude
			)
		}
		val manual = SunLocation(
			label = manualCity.name,
			latitude = manualCity.latitude,
			longitude = manualCity.longitude
		)
		val candidates = buildList {
			add(defaultCity)
			addAll(supportedCities)
			add(manual)
		}.distinctBy { candidate ->
			"${candidate.latitude}|${candidate.longitude}"
		}
		Logger.event(
			tag,
			"backup_suntimes_prefetch_request",
			"candidateCount" to candidates.size,
			"intervalHours" to (BACKUP_CITY_REFRESH_INTERVAL_MS / (60L * 60L * 1000L))
		)
		sunTimesRepository.prefetchBackupAsync(
			candidates = candidates,
			minRefreshIntervalMs = BACKUP_CITY_REFRESH_INTERVAL_MS
		)
	}

	private fun Double.toDisplay(): String = String.format(Locale.US, "%.2f", this)

	companion object {
		private val DEFAULT_CITY = SunLocation(
			label = "default_city",
			latitude = 41.0082,
			longitude = 28.9784
		)
		private const val SUN_TIMES_REFRESH_INTERVAL_MS = 3L * 60L * 60L * 1000L
		private const val CATEGORY_PRIORITIZE_LIMIT = 24
		private const val GPS_REQUEST_THROTTLE_MS = 1_500L
		private const val FOREGROUND_LOCATION_REFRESH_STALE_MS = 1_500L
		private const val BACKUP_CITY_REFRESH_INTERVAL_MS = 7L * 24L * 60L * 60L * 1000L
	}
}
