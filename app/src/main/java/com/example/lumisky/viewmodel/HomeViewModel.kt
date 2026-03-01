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
import com.example.core.settings.AppSettingsSnapshot
import com.example.core.settings.AppThemeMode
import com.example.core.settings.LocationMode
import com.example.core.settings.ManualCity
import com.example.core.settings.PerformanceMode
import com.example.engine.config.WallpaperConfig
import com.example.lumisky.data.WallpaperCatalog
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.random.Random

data class HomeWallpaperItem(
	val config: WallpaperConfig
)

class HomeViewModel(
	context: Context,
	private val sunTimesRepository: SunTimesRepository = SunTimesRepository(),
	private val settingsRepository: AppSettingsRepository = AppSettingsRepository(context),
	private val lastKnownLocationProvider: LastKnownLocationProvider = LastKnownLocationProvider(context),
	private val initialSettings: AppSettingsSnapshot = settingsRepository.snapshot()
) {
	private val tag = "HomeViewModel"
	private val mainHandler = Handler(Looper.getMainLooper())
	private val catalogExecutor: ExecutorService = Executors.newSingleThreadExecutor()
	private val locationLabelExecutor: ExecutorService = Executors.newSingleThreadExecutor()
	private val _items = mutableStateListOf<HomeWallpaperItem>()
	private var lastFocusedCategoryKey: String? = null
	private var lastGpsRequestAtMs: Long = 0L
	private var lastLocationStateRefreshAtMs: Long = 0L
	private var refreshPipelineScheduled = false
	private var refreshPipelineNeedsSunTimes = false
	private var refreshPipelineNeedsGpsRequest = false
	private var startupBackupPrefetchScheduled = false
	private var startupBackupPrefetchCompleted = false
	private var lastLiveGpsProbeAtMs: Long = 0L
	private var lastLastGpsProbeAtMs: Long = 0L
	private var locationCandidatesCacheKey: String? = null
	private var locationCandidatesCache: List<SunLocation> = emptyList()
	private var locationCandidatesCacheAtMs: Long = 0L

	val items: List<HomeWallpaperItem>
		get() = _items

	var selectedWallpaperId by mutableStateOf<String?>(null)
		private set

	var liveWallpaperId by mutableStateOf<String?>(null)
		private set

	var daylight by mutableStateOf(sunTimesRepository.currentOrFallback())
		private set

	var appThemeMode by mutableStateOf(initialSettings.appThemeMode)
		private set

	var languageTag by mutableStateOf(initialSettings.languageTag)
		private set

	var highRefreshEnabled by mutableStateOf(initialSettings.highRefreshEnabled)
		private set

	var performanceMode by mutableStateOf(initialSettings.performanceMode)
		private set

	var locationMode by mutableStateOf(initialSettings.locationMode)
		private set

	var manualCity by mutableStateOf(initialSettings.manualCity)
		private set

	var locationLabel by mutableStateOf(manualCity.name)
		private set

	var gpsLocationAvailable by mutableStateOf(false)
		private set

	var systemLocationEnabled by mutableStateOf(false)
		private set

	private var liveGpsLocation: SunLocation? = null
	private var lastKnownGpsLocation: SunLocation? = null
	private var gpsPlaceLabel: String? = null
	private var lastGpsPlaceKey: String? = null

	private val sunTimesRefreshRunnable = object : Runnable {
		override fun run() {
			refreshSunTimes()
			schedulePeriodicSunTimesRefresh()
		}
	}
	private val startupBackupPrefetchRunnable = Runnable {
		startupBackupPrefetchScheduled = false
		if (startupBackupPrefetchCompleted) return@Runnable
		startupBackupPrefetchCompleted = true
		prefetchBackupCityCache(maxCandidateCount = BACKUP_PREFETCH_STARTUP_CANDIDATE_LIMIT)
	}
	private val backupCityRefreshRunnable = object : Runnable {
		override fun run() {
			prefetchBackupCityCache()
			schedulePeriodicBackupCityRefresh()
		}
	}
	private val refreshLocationAndSunTimesRunnable = Runnable {
		refreshPipelineScheduled = false
		val shouldRefreshSunTimes = refreshPipelineNeedsSunTimes
		val shouldRequestGps = refreshPipelineNeedsGpsRequest
		refreshPipelineNeedsSunTimes = false
		refreshPipelineNeedsGpsRequest = false

		refreshLocationState()
		if (shouldRequestGps && locationMode == LocationMode.GPS) {
			requestImmediateGpsLocation()
		}
		if (shouldRefreshSunTimes) {
			refreshSunTimes()
		}
	}
	private val startupRefreshRunnable = Runnable {
		refreshLocationState()
		refreshSunTimes()
	}

	init {
		seedInitialCatalog(daylight)
		mainHandler.post(startupRefreshRunnable)
		schedulePeriodicSunTimesRefresh()
		schedulePeriodicBackupCityRefresh()
	}

	fun onWallpaperSelected(id: String) {
		selectedWallpaperId = id
		liveWallpaperId = id
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
	}

	fun clearLivePreview() {
		liveWallpaperId = null
	}

	fun onUserInteraction() {
		scheduleStartupBackupPrefetchIfNeeded()
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
		manualCity = AppSettingsDefaults.resolveCityById(manualCity.id, normalized)
		settingsRepository.setManualCity(manualCity)
		refreshLocationState()
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
		if (locationMode == mode) return
		locationMode = mode
		settingsRepository.setLocationMode(mode)
		scheduleCoalescedLocationAndSunTimesRefresh(
			requestGpsLocation = (mode == LocationMode.GPS)
		)
	}

	fun updateManualCity(city: ManualCity) {
		manualCity = city
		settingsRepository.setManualCity(city)
		gpsPlaceLabel = null
		lastGpsPlaceKey = null
		scheduleCoalescedLocationAndSunTimesRefresh()
		prefetchBackupCityCache()
	}

	fun refreshLocationState() {
		val refreshAtMs = SystemClock.elapsedRealtime()
		runCatching {
			systemLocationEnabled = runCatching { lastKnownLocationProvider.isLocationEnabled() }
				.getOrDefault(false)

			if (locationMode != LocationMode.GPS) {
				liveGpsLocation = null
				lastKnownGpsLocation = null
				lastLiveGpsProbeAtMs = 0L
				lastLastGpsProbeAtMs = 0L
				gpsLocationAvailable = false
				locationLabel = manualCity.name
				return
			}

			if (!systemLocationEnabled) {
				liveGpsLocation = null
				lastKnownGpsLocation = null
				lastLiveGpsProbeAtMs = 0L
				lastLastGpsProbeAtMs = 0L
				gpsLocationAvailable = false
				locationLabel = manualCity.name
				return
			}

			val liveGps = if (systemLocationEnabled) {
				resolveCachedLiveGpsProbe(refreshAtMs)
			} else {
				null
			}
			val lastGps = resolveCachedLastGpsProbe(refreshAtMs)
			liveGpsLocation = liveGps
			lastKnownGpsLocation = lastGps
			val preferredGps = liveGps ?: lastGps
			preferredGps?.let { maybeResolveGpsPlaceLabel(it) }
			updateLocationLabelFromCachedGpsState()
		}.onFailure {
			liveGpsLocation = null
			lastKnownGpsLocation = null
			gpsLocationAvailable = false
			locationLabel = manualCity.name
			Logger.w(tag, "refreshLocationState fallback", it)
		}
		lastLocationStateRefreshAtMs = refreshAtMs
	}

	fun refreshOnForegroundIfStale(
		maxStaleMs: Long = FOREGROUND_LOCATION_REFRESH_STALE_MS
	) {
		val elapsed = SystemClock.elapsedRealtime() - lastLocationStateRefreshAtMs
		if (elapsed in 0 until maxStaleMs) return
		onSystemLocationProviderChanged()
	}

	fun onSystemLocationProviderChanged() {
		runCatching {
			val wasSystemLocationEnabled = systemLocationEnabled
			refreshLocationState()
			if (locationMode != LocationMode.GPS) return
			val shouldRequestGps = systemLocationEnabled && (!wasSystemLocationEnabled || !gpsLocationAvailable)
			val shouldRefreshSunTimes = wasSystemLocationEnabled != systemLocationEnabled
			if (shouldRequestGps || shouldRefreshSunTimes) {
				scheduleCoalescedLocationAndSunTimesRefresh(
					requestGpsLocation = shouldRequestGps,
					refreshSunTimes = shouldRefreshSunTimes,
					debounceMs = LOCATION_REFRESH_COALESCE_DELAY_MS
				)
			}
		}
	}

	fun configFor(id: String): WallpaperConfig {
		return _items.firstOrNull { it.config.id == id }?.config
			?: WallpaperCatalog.configById(
				id = id,
				daylight = daylight
			)
	}

	fun release() {
		mainHandler.removeCallbacks(sunTimesRefreshRunnable)
		mainHandler.removeCallbacks(startupBackupPrefetchRunnable)
		mainHandler.removeCallbacks(backupCityRefreshRunnable)
		mainHandler.removeCallbacks(refreshLocationAndSunTimesRunnable)
		mainHandler.removeCallbacks(startupRefreshRunnable)
		catalogExecutor.shutdownNow()
		locationLabelExecutor.shutdownNow()
		sunTimesRepository.release()
	}

	private fun refreshSunTimes() {
		val candidates = resolveLocationCandidates()
		if (candidates.isEmpty()) return
		sunTimesRepository.refreshAsyncWithCandidates(candidates) { fetched ->
			mainHandler.post {
				if (fetched == daylight) return@post
				daylight = fetched
				rebuildCatalog(fetched)
			}
		}
	}

	private fun seedInitialCatalog(currentDaylight: SunDaylight) {
		val configs = WallpaperCatalog.buildConfigs(
			daylight = currentDaylight
		)
		_items.clear()
		_items.addAll(configs.map { config -> HomeWallpaperItem(config = config) })
		if (selectedWallpaperId == null && _items.isNotEmpty()) {
			selectedWallpaperId = _items.first().config.id
		}
	}

	private fun rebuildCatalog(currentDaylight: SunDaylight) {
		catalogExecutor.execute {
			val configs = WallpaperCatalog.buildConfigs(
				daylight = currentDaylight
			)
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
			longitude = manualCity.longitude,
			timeZoneId = manualCity.timeZoneId
		)
		val defaultCity = resolveDefaultCity()
		val now = SystemClock.elapsedRealtime()

		val cachedKey = buildLocationCandidatesCacheKey(
			manual = manual,
			defaultCity = defaultCity
		)
		if (locationCandidatesCacheKey == cachedKey &&
			(now - locationCandidatesCacheAtMs) in 0 until LOCATION_CANDIDATES_CACHE_TTL_MS
		) {
			return locationCandidatesCache
		}

		val resolved = when (locationMode) {
			LocationMode.MANUAL -> buildList {
				add(manual)
				add(defaultCity)
			}.distinctBy { candidate ->
				"${candidate.latitude}|${candidate.longitude}|${candidate.timeZoneId.orEmpty()}"
			}
			LocationMode.GPS -> buildList {
				if (systemLocationEnabled) {
					val probeNowMs = SystemClock.elapsedRealtime()
					val liveGps = resolveCachedLiveGpsProbe(probeNowMs)
					val lastGps = resolveCachedLastGpsProbe(probeNowMs)
					liveGps?.let { add(it) }
					lastGps?.let { add(it) }
				}
				add(manual)
				add(defaultCity)
			}.distinctBy { candidate ->
				"${candidate.latitude}|${candidate.longitude}|${candidate.timeZoneId.orEmpty()}"
			}
		}
		locationCandidatesCacheKey = cachedKey
		locationCandidatesCache = resolved
		locationCandidatesCacheAtMs = now
		return resolved
	}

	private fun requestImmediateGpsLocation() {
		if (locationMode != LocationMode.GPS || !systemLocationEnabled) return
		val now = SystemClock.elapsedRealtime()
		if ((now - lastGpsRequestAtMs) < GPS_REQUEST_THROTTLE_MS) return
		lastGpsRequestAtMs = now
		lastKnownLocationProvider.requestCurrentLocation(label = "gps_live") { location ->
			mainHandler.post {
				if (locationMode != LocationMode.GPS) return@post
				if (location != null) {
					liveGpsLocation = location
					lastLiveGpsProbeAtMs = SystemClock.elapsedRealtime()
				} else {
					lastLiveGpsProbeAtMs = SystemClock.elapsedRealtime()
					Logger.w(tag, "gps_live_result null")
				}
				scheduleCoalescedLocationAndSunTimesRefresh(
					requestGpsLocation = false,
					refreshSunTimes = (locationMode == LocationMode.GPS),
					debounceMs = 0L
				)
			}
		}
	}

	private fun scheduleCoalescedLocationAndSunTimesRefresh(
		requestGpsLocation: Boolean = false,
		refreshSunTimes: Boolean = true,
		debounceMs: Long = LOCATION_REFRESH_COALESCE_DELAY_MS
	) {
		refreshPipelineNeedsGpsRequest = refreshPipelineNeedsGpsRequest || requestGpsLocation
		refreshPipelineNeedsSunTimes = refreshPipelineNeedsSunTimes || refreshSunTimes
		if (refreshPipelineScheduled) return
		refreshPipelineScheduled = true
		val delayMs = debounceMs.coerceAtLeast(0L)
		mainHandler.postDelayed(refreshLocationAndSunTimesRunnable, delayMs)
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
				if (locationMode == LocationMode.GPS) {
					updateLocationLabelFromCachedGpsState()
				}
			}
		}
	}

	private fun updateLocationLabelFromCachedGpsState() {
		val liveGps = liveGpsLocation
		val lastGps = lastKnownGpsLocation
		gpsLocationAvailable = liveGps != null || lastGps != null
		locationLabel = when {
			liveGps != null -> gpsPlaceLabel ?: formatGpsLabel(liveGps)
			lastGps != null -> "Last GPS ${gpsPlaceLabel ?: formatGpsLabel(lastGps)}"
			else -> manualCity.name
		}
	}

	private fun gpsLocationKey(location: SunLocation): String {
		return String.format(Locale.US, "%.3f|%.3f", location.latitude, location.longitude)
	}

	private fun resolveDefaultCity(): SunLocation {
		val localizedDefault = AppSettingsDefaults.defaultCity(languageTag)
		return SunLocation(
			label = localizedDefault.name,
			latitude = localizedDefault.latitude,
			longitude = localizedDefault.longitude,
			timeZoneId = localizedDefault.timeZoneId
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

	private fun scheduleStartupBackupPrefetch(delayMs: Long) {
		mainHandler.removeCallbacks(startupBackupPrefetchRunnable)
		mainHandler.postDelayed(startupBackupPrefetchRunnable, delayMs)
	}

	private fun scheduleStartupBackupPrefetchIfNeeded() {
		if (startupBackupPrefetchCompleted) return
		if (startupBackupPrefetchScheduled) return
		startupBackupPrefetchScheduled = true
		val delayMs = resolveStartupBackupPrefetchDelayMs()
		scheduleStartupBackupPrefetch(delayMs)
	}

	private fun resolveCachedLiveGpsProbe(nowMs: Long): SunLocation? {
		if ((nowMs - lastLiveGpsProbeAtMs) in 0 until LAST_KNOWN_GPS_PROBE_CACHE_TTL_MS) {
			return liveGpsLocation
		}
		lastLiveGpsProbeAtMs = nowMs
		liveGpsLocation = runCatching {
			lastKnownLocationProvider.getLastKnownLocation(label = "gps_live")
		}.getOrNull()
		return liveGpsLocation
	}

	private fun resolveCachedLastGpsProbe(nowMs: Long): SunLocation? {
		if ((nowMs - lastLastGpsProbeAtMs) in 0 until LAST_KNOWN_GPS_PROBE_CACHE_TTL_MS) {
			return lastKnownGpsLocation
		}
		lastLastGpsProbeAtMs = nowMs
		lastKnownGpsLocation = runCatching {
			lastKnownLocationProvider.getLastKnownLocation(
				label = "gps_last",
				allowWhenLocationDisabled = true
			)
		}.getOrNull()
		return lastKnownGpsLocation
	}

	private fun buildLocationCandidatesCacheKey(
		manual: SunLocation,
		defaultCity: SunLocation
	): String {
		val liveGps = liveGpsLocation
		val lastGps = lastKnownGpsLocation
		return buildString {
			append(locationMode.name)
			append('|')
			append(systemLocationEnabled)
			append('|')
			append(manual.latitude)
			append('|')
			append(manual.longitude)
			append('|')
			append(manual.timeZoneId)
			append('|')
			append(defaultCity.latitude)
			append('|')
			append(defaultCity.longitude)
			append('|')
			append(defaultCity.timeZoneId)
			append('|')
			append(liveGps?.latitude ?: "null")
			append('|')
			append(liveGps?.longitude ?: "null")
			append('|')
			append(liveGps?.timeZoneId ?: "null")
			append('|')
			append(lastGps?.latitude ?: "null")
			append('|')
			append(lastGps?.longitude ?: "null")
			append('|')
			append(lastGps?.timeZoneId ?: "null")
		}
	}

	private fun resolveStartupBackupPrefetchDelayMs(): Long {
		val jitter = Random.nextLong(
			from = 0L,
			until = BACKUP_PREFETCH_STARTUP_JITTER_MS + 1L
		)
		return BACKUP_PREFETCH_STARTUP_DELAY_MS + jitter
	}

	private fun prefetchBackupCityCache(maxCandidateCount: Int = Int.MAX_VALUE) {
		val defaultCity = resolveDefaultCity()
		val supportedCities = AppSettingsDefaults.supportedCities(languageTag).map { city ->
			SunLocation(
				label = city.name,
				latitude = city.latitude,
				longitude = city.longitude,
				timeZoneId = city.timeZoneId
			)
		}
		val manual = SunLocation(
			label = manualCity.name,
			latitude = manualCity.latitude,
			longitude = manualCity.longitude,
			timeZoneId = manualCity.timeZoneId
		)
		val candidates = buildList {
			add(defaultCity)
			add(manual)
			addAll(supportedCities)
		}.distinctBy { candidate ->
			"${candidate.latitude}|${candidate.longitude}|${candidate.timeZoneId.orEmpty()}"
		}
		val boundedCandidates = if (maxCandidateCount > 0) {
			candidates.take(maxCandidateCount)
		} else {
			emptyList()
		}
		if (boundedCandidates.isEmpty()) return
		sunTimesRepository.prefetchBackupAsync(
			candidates = boundedCandidates,
			minRefreshIntervalMs = BACKUP_CITY_REFRESH_INTERVAL_MS
		)
	}

	private fun Double.toDisplay(): String = String.format(Locale.US, "%.2f", this)

	companion object {
		private const val SUN_TIMES_REFRESH_INTERVAL_MS = 3L * 60L * 60L * 1000L
		private const val CATEGORY_PRIORITIZE_LIMIT = 24
		private const val GPS_REQUEST_THROTTLE_MS = 1_500L
		private const val FOREGROUND_LOCATION_REFRESH_STALE_MS = 1_500L
		private const val BACKUP_CITY_REFRESH_INTERVAL_MS = 7L * 24L * 60L * 60L * 1000L
		private const val BACKUP_PREFETCH_STARTUP_DELAY_MS = 30_000L
		private const val BACKUP_PREFETCH_STARTUP_JITTER_MS = 10_000L
		private const val BACKUP_PREFETCH_STARTUP_CANDIDATE_LIMIT = 8
		private const val LOCATION_REFRESH_COALESCE_DELAY_MS = 120L
		private const val LAST_KNOWN_GPS_PROBE_CACHE_TTL_MS = 1_500L
		private const val LOCATION_CANDIDATES_CACHE_TTL_MS = 1_500L
	}
}
