package com.example.lumisky.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import com.example.core.Logger
import com.example.core.api.SunDaylight
import com.example.core.api.SunDaylightResolution
import com.example.core.api.SunLocation
import com.example.core.api.SunTimesRepository
import com.example.core.location.LastKnownLocationProvider
import com.example.core.location.LocationAccessLevel
import com.example.core.location.LocationSnapshot
import com.example.core.location.LocationSource
import com.example.core.location.asGpsApiCandidate
import com.example.core.location.matchesCoordinates
import com.example.core.location.withResolvedTimeZone
import com.example.core.settings.AppSettingsDefaults
import com.example.core.settings.AppSettingsRepository
import com.example.core.settings.AppSettingsSnapshot
import com.example.core.settings.HomeScrollSpeed
import com.example.core.settings.AppThemeMode
import com.example.core.settings.LocationMode
import com.example.core.settings.ManualCity
import com.example.core.settings.PerformanceMode
import com.example.engine.config.WallpaperConfig
import com.example.engine.config.WallpaperConfigStore
import com.example.lumisky.data.WallpaperCatalog
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt
import kotlin.random.Random

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
	private var locationCandidatesCacheKey: String? = null
	private var locationCandidatesCache: List<SunLocation> = emptyList()
	private var locationCandidatesCacheAtMs: Long = 0L
	private var settingsChangeListenerHandle: AutoCloseable? = null
	private val storedWallpaperId: String? = wallpaperConfigStore.loadSelected()?.id
	private val initialDaylight = resolveInitialDaylight(initialSettings)

	val items: List<HomeWallpaperItem>
		get() = _items.toList()

	var selectedWallpaperId by mutableStateOf(storedWallpaperId)
		private set

	var liveWallpaperId by mutableStateOf(storedWallpaperId)
		private set

	var daylight by mutableStateOf(initialDaylight)
		private set

	var appThemeMode by mutableStateOf(initialSettings.appThemeMode)
		private set

	var languageTag by mutableStateOf(initialSettings.languageTag)
		private set

	var highRefreshEnabled by mutableStateOf(initialSettings.highRefreshEnabled)
		private set

	var performanceMode by mutableStateOf(initialSettings.performanceMode)
		private set

	var homeScrollSpeed by mutableStateOf(initialSettings.homeScrollSpeed)
		private set

	var locationMode by mutableStateOf(initialSettings.locationMode)
		private set

	var manualCity by mutableStateOf(initialSettings.manualCity)
		private set

	var locationLabel by mutableStateOf(
		if (initialSettings.locationMode == LocationMode.GPS) {
			initialSettings.automaticLocation?.label ?: manualCity.name
		} else {
			manualCity.name
		}
	)
		private set

	var gpsLocationAvailable by mutableStateOf(
		initialSettings.locationMode == LocationMode.GPS && initialSettings.automaticLocation != null
	)
		private set

	var systemLocationEnabled by mutableStateOf(false)
		private set

	var locationRefreshInProgress by mutableStateOf(false)
		private set

	private var liveGpsLocation: SunLocation? = null
	private var automaticLocationSnapshot: LocationSnapshot? = initialSettings.automaticLocation
	private var lastKnownGpsLocation: SunLocation? = automaticLocationSnapshot?.toSunLocation(labelFallback = "gps_last")
	private var gpsPlaceLabel: String? = automaticLocationSnapshot?.label
	private var lastGpsPlaceKey: String? = automaticLocationSnapshot
		?.toSunLocation(labelFallback = "gps_last")
		?.let(::gpsLocationKey)
	private var passiveLocationUpdatesStarted: Boolean = false

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
		if (locationMode == LocationMode.GPS) {
			requestImmediateGpsLocation()
		}
		refreshSunTimes()
	}

	init {
		settingsChangeListenerHandle = settingsRepository.addChangeListener { snapshot ->
			mainHandler.post {
				applySettingsSnapshot(snapshot)
			}
		}
		seedInitialCatalog(daylight)
		mainHandler.post(startupRefreshRunnable)
		schedulePeriodicSunTimesRefresh()
		schedulePeriodicBackupCityRefresh()
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

	fun onForegroundStarted() {
		refreshOnForegroundIfStale()
		maybeStartPassiveLocationUpdates()
	}

	fun onForegroundStopped() {
		stopPassiveLocationUpdates()
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

	fun updateHomeScrollSpeed(speed: HomeScrollSpeed) {
		if (homeScrollSpeed == speed) return
		homeScrollSpeed = speed
		settingsRepository.setHomeScrollSpeed(speed)
	}

	fun updateLocationMode(mode: LocationMode) {
		if (locationMode == mode) return
		locationRefreshInProgress = false
		locationMode = mode
		settingsRepository.setLocationMode(mode)
		scheduleCoalescedLocationAndSunTimesRefresh(
			requestGpsLocation = (mode == LocationMode.GPS)
		)
	}

	fun refreshLocationNow() {
		if (locationMode != LocationMode.GPS) {
			locationRefreshInProgress = false
			refreshLocationState()
			refreshSunTimes()
			return
		}
		if (!lastKnownLocationProvider.hasLocationPermission()) {
			locationRefreshInProgress = false
			refreshLocationState()
			return
		}

		locationRefreshInProgress = true
		refreshLocationState()
		requestImmediateGpsLocation(force = true)
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
			val accessLevel = lastKnownLocationProvider.getLocationAccessLevel()

			if (locationMode != LocationMode.GPS) {
				liveGpsLocation = null
				gpsLocationAvailable = false
				locationLabel = manualCity.name
				locationRefreshInProgress = false
				stopPassiveLocationUpdates()
				return
			}

			automaticLocationSnapshot = settingsRepository.getAutomaticLocation()
			lastKnownGpsLocation = automaticLocationSnapshot?.toSunLocation(labelFallback = "gps_last")
			gpsPlaceLabel = automaticLocationSnapshot?.label
			lastGpsPlaceKey = lastKnownGpsLocation?.let(::gpsLocationKey)

			if (accessLevel == LocationAccessLevel.NONE) {
				liveGpsLocation = null
				locationRefreshInProgress = false
				stopPassiveLocationUpdates()
				updateLocationLabelFromCachedGpsState()
				return
			}
			if (systemLocationEnabled) {
				maybeStartPassiveLocationUpdates()
			} else {
				stopPassiveLocationUpdates()
			}
			val preferredGps = liveGpsLocation ?: lastKnownGpsLocation
			preferredGps?.let { maybeResolveGpsPlaceLabel(it) }
			updateLocationLabelFromCachedGpsState()
		}.onFailure {
			liveGpsLocation = null
			gpsLocationAvailable = false
			locationLabel = manualCity.name
			locationRefreshInProgress = false
			stopPassiveLocationUpdates()
			Logger.w(tag, "refreshLocationState fallback", it)
		}
		lastLocationStateRefreshAtMs = refreshAtMs
	}

	fun refreshOnForegroundIfStale(
		maxStaleMs: Long = FOREGROUND_LOCATION_REFRESH_STALE_MS
	) {
		val elapsed = SystemClock.elapsedRealtime() - lastLocationStateRefreshAtMs
		refreshLocationState()
		refreshSunTimes()
		if (locationMode != LocationMode.GPS) return
		if (elapsed in 0 until maxStaleMs &&
			automaticLocationSnapshot?.isFreshWithin(FOREGROUND_AUTOMATIC_LOCATION_MAX_AGE_MS) == true
		) {
			return
		}
		scheduleCoalescedLocationAndSunTimesRefresh(
			requestGpsLocation = lastKnownLocationProvider.hasLocationPermission(),
			refreshSunTimes = false,
			debounceMs = LOCATION_REFRESH_COALESCE_DELAY_MS
		)
	}

	fun onSystemLocationProviderChanged() {
		runCatching {
			val wasSystemLocationEnabled = systemLocationEnabled
			refreshLocationState()
			if (locationMode != LocationMode.GPS) return
			val shouldRequestGps = systemLocationEnabled && lastKnownLocationProvider.hasLocationPermission()
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
		settingsChangeListenerHandle?.close()
		settingsChangeListenerHandle = null
		stopPassiveLocationUpdates()
		mainHandler.removeCallbacks(sunTimesRefreshRunnable)
		mainHandler.removeCallbacks(startupBackupPrefetchRunnable)
		mainHandler.removeCallbacks(backupCityRefreshRunnable)
		mainHandler.removeCallbacks(refreshLocationAndSunTimesRunnable)
		mainHandler.removeCallbacks(startupRefreshRunnable)
		catalogExecutor.shutdownNow()
		locationLabelExecutor.shutdownNow()
		sunTimesRepository.release()
	}

	private fun applySettingsSnapshot(snapshot: AppSettingsSnapshot) {
		var shouldRefreshLocationState = false
		var shouldRefreshSunTimes = false

		if (appThemeMode != snapshot.appThemeMode) {
			appThemeMode = snapshot.appThemeMode
		}
		if (languageTag != snapshot.languageTag) {
			languageTag = snapshot.languageTag
			shouldRefreshLocationState = true
			shouldRefreshSunTimes = true
		}
		if (highRefreshEnabled != snapshot.highRefreshEnabled) {
			highRefreshEnabled = snapshot.highRefreshEnabled
		}
		if (performanceMode != snapshot.performanceMode) {
			performanceMode = snapshot.performanceMode
		}
		if (homeScrollSpeed != snapshot.homeScrollSpeed) {
			homeScrollSpeed = snapshot.homeScrollSpeed
		}
		if (locationMode != snapshot.locationMode) {
			locationMode = snapshot.locationMode
			locationRefreshInProgress = false
			shouldRefreshLocationState = true
			shouldRefreshSunTimes = true
		}
		if (manualCity != snapshot.manualCity) {
			manualCity = snapshot.manualCity
			shouldRefreshLocationState = true
			shouldRefreshSunTimes = true
		}
		if (automaticLocationSnapshot != snapshot.automaticLocation) {
			automaticLocationSnapshot = snapshot.automaticLocation
			lastKnownGpsLocation = automaticLocationSnapshot?.toSunLocation(labelFallback = "gps_last")
			liveGpsLocation = automaticLocationSnapshot
				?.takeIf { it.source == LocationSource.CURRENT || it.source == LocationSource.PASSIVE }
				?.toSunLocation(labelFallback = "gps_live")
			gpsPlaceLabel = automaticLocationSnapshot?.label
			lastGpsPlaceKey = lastKnownGpsLocation?.let(::gpsLocationKey)
			shouldRefreshLocationState = true
			shouldRefreshSunTimes = true
		}

		if (shouldRefreshLocationState) {
			refreshLocationState()
		}
		if (shouldRefreshSunTimes) {
			scheduleCoalescedLocationAndSunTimesRefresh(
				requestGpsLocation = false,
				refreshSunTimes = true,
				debounceMs = 0L
			)
		}
	}

	private fun resolveInitialDaylight(
		settings: AppSettingsSnapshot
	): SunDaylight {
		return sunTimesRepository.currentOrFallbackForCandidates(
			buildInitialLocationCandidates(settings)
		)
	}

	private fun buildInitialLocationCandidates(
		settings: AppSettingsSnapshot
	): List<SunLocation> {
		val manual = SunLocation(
			label = settings.manualCity.name,
			latitude = settings.manualCity.latitude,
			longitude = settings.manualCity.longitude,
			timeZoneId = settings.manualCity.timeZoneId
		)
		val localizedDefault = AppSettingsDefaults.defaultCity(settings.languageTag)
		val defaultCity = SunLocation(
			label = localizedDefault.name,
			latitude = localizedDefault.latitude,
			longitude = localizedDefault.longitude,
			timeZoneId = localizedDefault.timeZoneId
		)
		val gpsLocation = settings.automaticLocation
			?.takeIf { settings.locationMode == LocationMode.GPS }
			?.toSunLocation(labelFallback = "gps_initial")

		return buildList {
			gpsLocation?.let { add(it.asGpsApiCandidate()) }
			add(manual)
			add(defaultCity)
		}.distinctBy { candidate ->
			"${candidate.latitude}|${candidate.longitude}|${candidate.timeZoneId.orEmpty()}"
		}
	}

	private fun refreshSunTimes() {
		val candidates = resolveLocationCandidates()
		if (candidates.isEmpty()) return
		sunTimesRepository.refreshResolvedAsyncWithCandidates(candidates) { resolution ->
			mainHandler.post {
				syncAutomaticLocationTimeZoneIfNeeded(resolution)
				val fetched = resolution.daylight
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
			val configs = WallpaperCatalog.buildConfigs(
				daylight = currentDaylight
			)
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
				liveGpsLocation?.let { add(it.asGpsApiCandidate()) }
				lastKnownGpsLocation?.let { add(it.asGpsApiCandidate()) }
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

	private fun requestImmediateGpsLocation(
		force: Boolean = false
	) {
		if (locationMode != LocationMode.GPS || !lastKnownLocationProvider.hasLocationPermission()) {
			locationRefreshInProgress = false
			return
		}
		val now = SystemClock.elapsedRealtime()
		if (!force && (now - lastGpsRequestAtMs) < GPS_REQUEST_THROTTLE_MS) return
		lastGpsRequestAtMs = now
		lastKnownLocationProvider.requestLastKnownLocation(
			allowWhenLocationDisabled = true
		) { location ->
			mainHandler.post {
				if (locationMode != LocationMode.GPS) {
					locationRefreshInProgress = false
					return@post
				}
				val shouldRequestCurrent = shouldRequestCurrentLocation(location)
				val applied = location?.let { snapshot ->
					applyAutomaticLocationSample(
						location = snapshot,
						refreshSunTimes = !shouldRequestCurrent
					)
				} ?: false
				if (!systemLocationEnabled) {
					if (applied) {
						refreshLocationState()
					}
					locationRefreshInProgress = false
					return@post
				}
				if (shouldRequestCurrent) {
					requestCurrentGpsLocation()
				} else if (applied) {
					refreshLocationState()
					locationRefreshInProgress = false
				} else {
					locationRefreshInProgress = false
				}
			}
		}
	}

	private fun requestCurrentGpsLocation() {
		val preferLowPower = shouldPreferLowPowerCurrentLocation()
		lastKnownLocationProvider.requestCurrentLocation(
			preferLowPower = preferLowPower,
			maxUpdateAgeMillis = CURRENT_LOCATION_MAX_AGE_MS,
			timeoutMillis = CURRENT_LOCATION_TIMEOUT_MS
		) { location ->
			mainHandler.post {
				if (locationMode != LocationMode.GPS) {
					locationRefreshInProgress = false
					return@post
				}
				if (location == null) {
					Logger.w(tag, "gps_current_result null")
					locationRefreshInProgress = false
					return@post
				}
				applyAutomaticLocationSample(location, refreshSunTimes = true)
				locationRefreshInProgress = false
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
				automaticLocationSnapshot?.let { snapshot ->
					if (gpsLocationKey(snapshot.toSunLocation(labelFallback = "gps_cached")) == key) {
						val updated = snapshot.copy(label = resolved)
						automaticLocationSnapshot = updated
						settingsRepository.setAutomaticLocation(updated)
						lastKnownGpsLocation = updated.toSunLocation(labelFallback = "gps_last")
					}
				}
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
			lastGps != null -> "Last known ${gpsPlaceLabel ?: formatGpsLabel(lastGps)}"
			else -> manualCity.name
		}
	}

	private fun applyAutomaticLocationSample(
		location: LocationSnapshot,
		refreshSunTimes: Boolean
	): Boolean {
		val previous = automaticLocationSnapshot
		if (previous != null && previous.capturedAtEpochMs > location.capturedAtEpochMs) {
			return false
		}

		automaticLocationSnapshot = location
		settingsRepository.setAutomaticLocation(location)
		val resolvedSunLocation = location.toSunLocation(labelFallback = "gps_last")
		val nextPlaceKey = gpsLocationKey(resolvedSunLocation)
		if (nextPlaceKey != lastGpsPlaceKey) {
			gpsPlaceLabel = location.label
		}
		lastGpsPlaceKey = nextPlaceKey
		if (location.source == LocationSource.CURRENT || location.source == LocationSource.PASSIVE) {
			liveGpsLocation = location.toSunLocation(labelFallback = "gps_live")
		}
		lastKnownGpsLocation = resolvedSunLocation
		if (!location.label.isNullOrBlank()) {
			gpsPlaceLabel = location.label
		}
		updateLocationLabelFromCachedGpsState()
		maybeResolveGpsPlaceLabel(lastKnownGpsLocation ?: return true)

		if (refreshSunTimes && shouldRefreshSunTimesForLocation(previous, location)) {
			scheduleCoalescedLocationAndSunTimesRefresh(
				requestGpsLocation = false,
				refreshSunTimes = true,
				debounceMs = 0L
			)
		}
		return true
	}

	private fun syncAutomaticLocationTimeZoneIfNeeded(
		resolution: SunDaylightResolution
	) {
		if (locationMode != LocationMode.GPS) return
		val sourceLocation = resolution.sourceLocation ?: return
		val resolvedTimeZoneId = resolution.daylight.timeZoneId ?: return
		val snapshot = automaticLocationSnapshot ?: return
		if (!snapshot.matchesCoordinates(sourceLocation)) return
		val updated = snapshot.withResolvedTimeZone(resolvedTimeZoneId)
		if (updated == snapshot) return
		automaticLocationSnapshot = updated
		settingsRepository.setAutomaticLocation(updated)
		lastKnownGpsLocation = updated.toSunLocation(labelFallback = "gps_last")
		if (updated.source == LocationSource.CURRENT || updated.source == LocationSource.PASSIVE) {
			liveGpsLocation = updated.toSunLocation(labelFallback = "gps_live")
		}
		updateLocationLabelFromCachedGpsState()
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

	private fun shouldRequestCurrentLocation(
		lastKnown: LocationSnapshot?
	): Boolean {
		if (!systemLocationEnabled) return false
		if (lastKnown == null) return true
		if (!lastKnown.isFreshWithin(LAST_KNOWN_LOCATION_MAX_AGE_MS)) return true
		val accuracy = lastKnown.accuracyMeters ?: return false
		return accuracy > MAX_ACCEPTABLE_LAST_LOCATION_ACCURACY_METERS
	}

	private fun shouldPreferLowPowerCurrentLocation(): Boolean {
		if (lastKnownLocationProvider.getLocationAccessLevel() != LocationAccessLevel.PRECISE) {
			return true
		}
		return automaticLocationSnapshot?.isFreshWithin(LOW_POWER_CURRENT_LOCATION_WINDOW_MS) == true
	}

	private fun shouldRefreshSunTimesForLocation(
		previous: LocationSnapshot?,
		current: LocationSnapshot
	): Boolean {
		if (previous == null) return true
		if (previous.timeZoneId != current.timeZoneId) return true
		return sunTimesLocationKey(previous) != sunTimesLocationKey(current)
	}

	private fun maybeStartPassiveLocationUpdates() {
		if (passiveLocationUpdatesStarted) return
		if (locationMode != LocationMode.GPS) return
		if (!systemLocationEnabled || !lastKnownLocationProvider.hasLocationPermission()) return
		passiveLocationUpdatesStarted = true
		lastKnownLocationProvider.startPassiveLocationUpdates { location ->
			mainHandler.post {
				if (locationMode != LocationMode.GPS) return@post
				applyAutomaticLocationSample(location, refreshSunTimes = true)
			}
		}
	}

	private fun stopPassiveLocationUpdates() {
		if (!passiveLocationUpdatesStarted) return
		passiveLocationUpdatesStarted = false
		lastKnownLocationProvider.stopPassiveLocationUpdates()
	}

	private fun sunTimesLocationKey(location: LocationSnapshot): String {
		val latitudeBucket = (location.latitude * SUN_TIMES_REFRESH_LOCATION_BUCKET_SCALE).roundToInt()
		val longitudeBucket = (location.longitude * SUN_TIMES_REFRESH_LOCATION_BUCKET_SCALE).roundToInt()
		return "$latitudeBucket|$longitudeBucket|${location.timeZoneId}"
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
		private const val FOREGROUND_LOCATION_REFRESH_STALE_MS = 30L * 1000L
		private const val FOREGROUND_AUTOMATIC_LOCATION_MAX_AGE_MS = 30L * 60L * 1000L
		private const val BACKUP_CITY_REFRESH_INTERVAL_MS = 7L * 24L * 60L * 60L * 1000L
		private const val BACKUP_PREFETCH_STARTUP_DELAY_MS = 30_000L
		private const val BACKUP_PREFETCH_STARTUP_JITTER_MS = 10_000L
		private const val BACKUP_PREFETCH_STARTUP_CANDIDATE_LIMIT = 8
		private const val LOCATION_REFRESH_COALESCE_DELAY_MS = 120L
		private const val LOCATION_CANDIDATES_CACHE_TTL_MS = 1_500L
		private const val LAST_KNOWN_LOCATION_MAX_AGE_MS = 30L * 60L * 1000L
		private const val LOW_POWER_CURRENT_LOCATION_WINDOW_MS = 6L * 60L * 60L * 1000L
		private const val CURRENT_LOCATION_MAX_AGE_MS = 10L * 60L * 1000L
		private const val CURRENT_LOCATION_TIMEOUT_MS = 6_000L
		private const val MAX_ACCEPTABLE_LAST_LOCATION_ACCURACY_METERS = 15_000f
		private const val SUN_TIMES_REFRESH_LOCATION_BUCKET_SCALE = 100.0
	}
}
