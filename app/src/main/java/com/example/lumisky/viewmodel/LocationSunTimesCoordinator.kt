package com.example.lumisky.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
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
import com.example.core.settings.LocationMode
import com.example.core.settings.ManualCity
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

private data class ResolvedLocationState(
	val systemEnabled: Boolean,
	val liveGps: SunLocation?,
	val lastGps: SunLocation?,
	val resolvedAtMillis: Long
)

/**
 * Coordinates location state (GPS, manual city) and sun-times resolution.
 *
 * Extracted from [HomeViewModel] to isolate location/sun-times logic from
 * UI orchestration and reduce the ViewModel's responsibility surface.
 *
 * This class is **not thread-safe** — all public methods must be called
 * from the main thread (same as the originating ViewModel).
 */
internal class LocationSunTimesCoordinator(
	context: Context,
	private val sunTimesRepository: SunTimesRepository,
	private val settingsRepository: AppSettingsRepository,
	private val lastKnownLocationProvider: LastKnownLocationProvider,
	initialSettings: AppSettingsSnapshot,
	private val onDaylightResolved: (SunDaylight) -> Unit
) {
	private val tag = "LocationSunTimesCoordinator"
	private val mainHandler = Handler(Looper.getMainLooper())
	private val locationLabelExecutor: ExecutorService = ThreadPoolExecutor(
		1,
		1,
		0L,
		TimeUnit.MILLISECONDS,
		LinkedBlockingQueue(LOCATION_LABEL_QUEUE_CAPACITY),
		{ runnable ->
			Thread(runnable, LOCATION_LABEL_THREAD_NAME).apply {
				isDaemon = true
			}
		},
		ThreadPoolExecutor.AbortPolicy()
	)
	@Volatile
	private var released: Boolean = false

	// ---- observable state ----
	var locationMode: LocationMode by mutableStateOf(initialSettings.locationMode)
		private set
	var manualCity: ManualCity by mutableStateOf(initialSettings.manualCity)
		private set
	var locationLabel: String by mutableStateOf(resolveInitialLabel(initialSettings))
		private set
	var gpsLocationAvailable: Boolean by mutableStateOf(
		initialSettings.locationMode == LocationMode.GPS && initialSettings.automaticLocation != null
	)
		private set
	var systemLocationEnabled: Boolean by mutableStateOf(false)
		private set
	var locationRefreshInProgress: Boolean by mutableStateOf(false)
		private set
	var daylight: SunDaylight by mutableStateOf(resolveInitialDaylight(initialSettings))
		private set
	var lastKnownCity: ManualCity? by mutableStateOf(
		initialSettings.automaticLocation?.toLastKnownManualCity()
	)
		private set

	// ---- internal GPS state ----
	private var liveGpsLocation: SunLocation? = null
	private var automaticLocationSnapshot: LocationSnapshot? = initialSettings.automaticLocation
	private var lastKnownGpsLocation: SunLocation? =
		automaticLocationSnapshot?.toSunLocation(labelFallback = "gps_last")
	private var gpsPlaceLabel: String? = automaticLocationSnapshot?.label
	private var lastGpsPlaceKey: String? =
		lastKnownGpsLocation?.let(::gpsLocationKey)
	private var passiveLocationUpdatesStarted: Boolean = false

	// ---- debounce / cache ----
	private var lastGpsRequestAtMs: Long = 0L
	private var lastLocationStateRefreshAtMs: Long = 0L
	private var refreshPipelineScheduled = false
	private var refreshPipelineNeedsSunTimes = false
	private var refreshPipelineNeedsGpsRequest = false
	private var refreshPipelineForceLocationState = false
	private var refreshPipelineHandleProviderChange = false
	private var refreshPipelineReason = "idle"
	private var resolvedLocationStateCache: ResolvedLocationState? = null
	private var locationCandidatesCacheKey: String? = null
	private var locationCandidatesCache: List<SunLocation> = emptyList()
	private var locationCandidatesCacheAtMs: Long = 0L
	private var languageTag: String = initialSettings.languageTag

	// ---- runnables ----
	private val sunTimesRefreshRunnable = object : Runnable {
		override fun run() {
			if (released) return
			refreshSunTimes()
			schedulePeriodicSunTimesRefresh()
		}
	}
	private val refreshLocationAndSunTimesRunnable = Runnable {
		if (released) return@Runnable
		refreshPipelineScheduled = false
		val shouldRefreshSunTimes = refreshPipelineNeedsSunTimes
		val shouldRequestGps = refreshPipelineNeedsGpsRequest
		val shouldForceLocationState = refreshPipelineForceLocationState
		val shouldHandleProviderChange = refreshPipelineHandleProviderChange
		val reason = refreshPipelineReason
		refreshPipelineNeedsSunTimes = false
		refreshPipelineNeedsGpsRequest = false
		refreshPipelineForceLocationState = false
		refreshPipelineHandleProviderChange = false
		refreshPipelineReason = "idle"

		val wasSystemLocationEnabled = systemLocationEnabled
		val resolvedState = resolveLocationState(forceRefresh = shouldForceLocationState)
		Logger.d(
			tag,
			"LOCATION_REFRESH_COALESCED reason=$reason sunTimes=$shouldRefreshSunTimes gpsRequest=$shouldRequestGps"
		)
		refreshLocationStateNow(resolvedState)
		if (shouldHandleProviderChange && locationMode == LocationMode.GPS) {
			if (!resolvedState.systemEnabled) {
				updateLocationMode(LocationMode.MANUAL)
				lastKnownCity?.let { updateManualCity(it) }
				return@Runnable
			}
			if (resolvedState.systemEnabled && lastKnownLocationProvider.hasLocationPermission()) {
				requestImmediateGpsLocation()
			}
			if (wasSystemLocationEnabled != resolvedState.systemEnabled) {
				refreshSunTimesNow(resolvedState)
			}
		}
		if (shouldRequestGps && locationMode == LocationMode.GPS) {
			requestImmediateGpsLocation()
		}
		if (shouldRefreshSunTimes) {
			refreshSunTimesNow(resolvedState)
		}
	}
	private val startupRefreshRunnable = Runnable {
		if (released) return@Runnable
		val resolvedState = resolveLocationState(forceRefresh = true)
		refreshLocationStateNow(resolvedState)
		if (locationMode == LocationMode.GPS && !resolvedState.systemEnabled) {
			updateLocationMode(LocationMode.MANUAL)
			lastKnownCity?.let { updateManualCity(it) }
		} else if (locationMode == LocationMode.GPS) {
			requestImmediateGpsLocation()
		}
		refreshSunTimesNow(resolvedState)
	}

	// ---- lifecycle ----

	fun init() {
		if (released) return
		postToMain("startup_refresh") {
			startupRefreshRunnable.run()
		}
		schedulePeriodicSunTimesRefresh()
	}

	fun onForegroundStarted() {
		refreshOnForegroundIfStale()
		maybeStartPassiveLocationUpdates()
	}

	fun onForegroundStopped() {
		stopPassiveLocationUpdates()
	}

	fun release() {
		if (released) return
		released = true
		Logger.d(tag, "release called")
		lastKnownLocationProvider.cancelCurrentLocationRequest()
		stopPassiveLocationUpdates()
		mainHandler.removeCallbacks(sunTimesRefreshRunnable)
		mainHandler.removeCallbacks(refreshLocationAndSunTimesRunnable)
		mainHandler.removeCallbacks(startupRefreshRunnable)
		locationLabelExecutor.shutdown()
		try {
			if (!locationLabelExecutor.awaitTermination(
					LOCATION_LABEL_EXECUTOR_SHUTDOWN_TIMEOUT_MS,
					TimeUnit.MILLISECONDS
				)
			) {
				Logger.w(tag, "location label executor shutdown timed out")
				locationLabelExecutor.shutdownNow()
			}
		} catch (e: InterruptedException) {
			Thread.currentThread().interrupt()
			Logger.w(tag, "location label executor shutdown interrupted", e)
			locationLabelExecutor.shutdownNow()
		}
		sunTimesRepository.release()
	}

	// ---- public API ----

	fun updateLocationMode(mode: LocationMode) {
		if (locationMode == mode) return
		locationRefreshInProgress = false
		locationMode = mode
		invalidateResolvedLocationStateCache()
		invalidateLocationCandidatesCache()
		settingsRepository.setLocationMode(mode)
		refreshLocationAndSunTimesCoalesced(
			reason = "location_mode",
			requestGpsLocation = (mode == LocationMode.GPS)
		)
	}

	fun updateManualCity(city: ManualCity) {
		manualCity = city
		invalidateLocationCandidatesCache()
		settingsRepository.setManualCity(city)
		if (city.id != LAST_KNOWN_CITY_ID) {
			gpsPlaceLabel = null
			lastGpsPlaceKey = null
		}
		refreshLocationAndSunTimesCoalesced(reason = "manual_city")
	}

	fun updateLanguageTag(tag: String) {
		languageTag = tag
		if (manualCity.id != LAST_KNOWN_CITY_ID) {
			manualCity = AppSettingsDefaults.resolveCityById(manualCity.id, tag)
			settingsRepository.setManualCity(manualCity)
		}
		updateLastKnownCity(automaticLocationSnapshot)
		refreshLocationAndSunTimesCoalesced(reason = "language")
	}

	fun refreshLocationNow() {
		if (locationMode != LocationMode.GPS) {
			locationRefreshInProgress = false
			refreshLocationAndSunTimesCoalesced(
				reason = "refresh_now_manual",
				refreshSunTimes = true,
				debounceMs = 0L
			)
			return
		}
		if (!lastKnownLocationProvider.hasLocationPermission()) {
			locationRefreshInProgress = false
			refreshLocationAndSunTimesCoalesced(
				reason = "refresh_now_no_permission",
				refreshSunTimes = false,
				debounceMs = 0L
			)
			return
		}

		locationRefreshInProgress = true
		refreshLocationStateNow(resolveLocationState(forceRefresh = true))
		requestImmediateGpsLocation(force = true)
	}

	fun onSystemLocationProviderChanged() {
		invalidateResolvedLocationStateCache()
		refreshLocationAndSunTimesCoalesced(
			reason = "provider_changed",
			refreshSunTimes = false,
			forceLocationState = true,
			handleProviderChange = true
		)
	}

	fun applySettingsChanges(
		newLocationMode: LocationMode?,
		newManualCity: ManualCity?,
		newAutomaticLocation: LocationSnapshot?,
		newLanguageTag: String?
	) {
		var shouldRefreshLocation = false
		var shouldRefreshSunTimes = false

		if (newLanguageTag != null && languageTag != newLanguageTag) {
			languageTag = newLanguageTag
			invalidateLocationCandidatesCache()
			shouldRefreshLocation = true
			shouldRefreshSunTimes = true
		}
		if (newLocationMode != null && locationMode != newLocationMode) {
			locationMode = newLocationMode
			locationRefreshInProgress = false
			invalidateResolvedLocationStateCache()
			invalidateLocationCandidatesCache()
			shouldRefreshLocation = true
			shouldRefreshSunTimes = true
		}
		if (newManualCity != null && manualCity != newManualCity) {
			manualCity = newManualCity
			invalidateLocationCandidatesCache()
			shouldRefreshLocation = true
			shouldRefreshSunTimes = true
		}
		if (newAutomaticLocation != null && automaticLocationSnapshot != newAutomaticLocation) {
			automaticLocationSnapshot = newAutomaticLocation
			lastKnownGpsLocation =
				automaticLocationSnapshot?.toSunLocation(labelFallback = "gps_last")
			liveGpsLocation = automaticLocationSnapshot
				?.takeIf { it.source == LocationSource.CURRENT || it.source == LocationSource.PASSIVE }
				?.toSunLocation(labelFallback = "gps_live")
			gpsPlaceLabel = automaticLocationSnapshot?.label
			lastGpsPlaceKey = lastKnownGpsLocation?.let(::gpsLocationKey)
			updateLastKnownCity(automaticLocationSnapshot)
			invalidateLocationCandidatesCache()
			updateResolvedLocationStateCache()
			shouldRefreshLocation = true
			shouldRefreshSunTimes = true
		}

		if (shouldRefreshLocation || shouldRefreshSunTimes) {
			refreshLocationAndSunTimesCoalesced(
				reason = "settings_change",
				requestGpsLocation = false,
				refreshSunTimes = shouldRefreshSunTimes,
				debounceMs = 0L
			)
		}
	}

	fun resolveLocationCandidates(): List<SunLocation> {
		return resolveLocationCandidates(resolvedLocationState = freshResolvedLocationState())
	}

	private fun resolveLocationCandidates(
		resolvedLocationState: ResolvedLocationState?
	): List<SunLocation> {
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
			defaultCity = defaultCity,
			resolvedLocationState = resolvedLocationState
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
				val liveGps = resolvedLocationState?.liveGps ?: liveGpsLocation
				val lastGps = resolvedLocationState?.lastGps ?: lastKnownGpsLocation
				liveGps?.let { add(it.asGpsApiCandidate()) }
				lastGps?.let { add(it.asGpsApiCandidate()) }
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

	fun buildInitialLocationCandidates(settings: AppSettingsSnapshot): List<SunLocation> {
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

	// ---- internal ----

	fun refreshLocationState() {
		refreshLocationAndSunTimesCoalesced(
			reason = "refresh_location_state",
			refreshSunTimes = false
		)
	}

	private fun refreshLocationStateNow(resolvedState: ResolvedLocationState) {
		val refreshAtMs = resolvedState.resolvedAtMillis
		runCatching {
			systemLocationEnabled = resolvedState.systemEnabled
			val accessLevel = lastKnownLocationProvider.getLocationAccessLevel()

			if (locationMode != LocationMode.GPS) {
				liveGpsLocation = null
				gpsLocationAvailable = false
				locationLabel = manualCity.name
				locationRefreshInProgress = false
				stopPassiveLocationUpdates()
				return
			}

			lastKnownGpsLocation = resolvedState.lastGps
			liveGpsLocation = resolvedState.liveGps
			gpsPlaceLabel = automaticLocationSnapshot?.label
			lastGpsPlaceKey = lastKnownGpsLocation?.let(::gpsLocationKey)
			updateLastKnownCity(automaticLocationSnapshot)

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

	private fun refreshSunTimes() {
		refreshLocationAndSunTimesCoalesced(reason = "refresh_sun_times")
	}

	private fun refreshSunTimesNow(resolvedState: ResolvedLocationState? = null) {
		if (released) return
		val candidates = resolveLocationCandidates(resolvedState)
		if (candidates.isEmpty()) return
		sunTimesRepository.refreshResolvedAsyncWithCandidates(candidates) { resolution ->
			postToMain("sun_times_result") {
				syncAutomaticLocationTimeZoneIfNeeded(resolution)
				val fetched = resolution.daylight
				if (fetched != daylight) {
					daylight = fetched
				}
				onDaylightResolved(fetched)
			}
		}
	}

	private fun resolveLocationState(forceRefresh: Boolean = false): ResolvedLocationState {
		val now = SystemClock.elapsedRealtime()
		if (!forceRefresh) {
			freshResolvedLocationState(now)?.let { return it }
		}

		automaticLocationSnapshot = settingsRepository.getAutomaticLocation()
		val liveGps = liveGpsLocation
			?: automaticLocationSnapshot
				?.takeIf { it.source == LocationSource.CURRENT || it.source == LocationSource.PASSIVE }
				?.toSunLocation(labelFallback = "gps_live")
		val state = ResolvedLocationState(
			systemEnabled = runCatching { lastKnownLocationProvider.isLocationEnabled() }
				.getOrDefault(false),
			liveGps = liveGps,
			lastGps = automaticLocationSnapshot?.toSunLocation(labelFallback = "gps_last"),
			resolvedAtMillis = now
		)
		resolvedLocationStateCache = state
		Logger.d(
			tag,
			"LOCATION_PROVIDER_SCAN_PASS systemEnabled=${state.systemEnabled} liveGps=${state.liveGps != null} lastGps=${state.lastGps != null}"
		)
		return state
	}

	private fun freshResolvedLocationState(
		now: Long = SystemClock.elapsedRealtime()
	): ResolvedLocationState? {
		return resolvedLocationStateCache?.takeIf { state ->
			(now - state.resolvedAtMillis) in 0 until RESOLVED_LOCATION_STATE_CACHE_TTL_MS
		}
	}

	private fun invalidateResolvedLocationStateCache() {
		resolvedLocationStateCache = null
	}

	private fun invalidateLocationCandidatesCache() {
		locationCandidatesCacheKey = null
		locationCandidatesCache = emptyList()
		locationCandidatesCacheAtMs = 0L
	}

	private fun updateResolvedLocationStateCache() {
		resolvedLocationStateCache = ResolvedLocationState(
			systemEnabled = systemLocationEnabled,
			liveGps = liveGpsLocation,
			lastGps = lastKnownGpsLocation,
			resolvedAtMillis = SystemClock.elapsedRealtime()
		)
	}

	private fun requestImmediateGpsLocation(force: Boolean = false) {
		if (released) return
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
			postToMain("last_known_location_result") {
				if (locationMode != LocationMode.GPS) {
					locationRefreshInProgress = false
					return@postToMain
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
						refreshLocationStateNow(resolveLocationState(forceRefresh = false))
					}
					locationRefreshInProgress = false
					return@postToMain
				}
				if (shouldRequestCurrent) {
					requestCurrentGpsLocation()
				} else if (applied) {
					refreshLocationStateNow(resolveLocationState(forceRefresh = false))
					locationRefreshInProgress = false
				} else {
					locationRefreshInProgress = false
				}
			}
		}
	}

	private fun requestCurrentGpsLocation() {
		if (released) return
		val requestStartedAtMs = SystemClock.elapsedRealtime()
		val safetyTimeout = Runnable {
			if (released) return@Runnable
			if (locationMode == LocationMode.GPS && locationRefreshInProgress) {
				Logger.w(tag, "gps request safety timeout")
				locationRefreshInProgress = false
			}
		}
		mainHandler.postDelayed(safetyTimeout, GPS_REQUEST_RESPONSE_MAX_WAIT_MS)
		val preferLowPower = shouldPreferLowPowerCurrentLocation()
		lastKnownLocationProvider.requestCurrentLocation(
			preferLowPower = preferLowPower,
			maxUpdateAgeMillis = CURRENT_LOCATION_MAX_AGE_MS,
			timeoutMillis = CURRENT_LOCATION_TIMEOUT_MS
		) { location ->
			postToMain("current_location_result") {
				mainHandler.removeCallbacks(safetyTimeout)
				val elapsedMs = SystemClock.elapsedRealtime() - requestStartedAtMs
				if (elapsedMs > GPS_REQUEST_RESPONSE_MAX_WAIT_MS) {
					Logger.w(tag, "gps response too slow elapsedMs=$elapsedMs")
					locationRefreshInProgress = false
					return@postToMain
				}
				if (locationMode != LocationMode.GPS) {
					locationRefreshInProgress = false
					return@postToMain
				}
				if (location == null) {
					Logger.w(tag, "gps_current_result null")
					locationRefreshInProgress = false
					return@postToMain
				}
				applyAutomaticLocationSample(location, refreshSunTimes = true)
				locationRefreshInProgress = false
			}
		}
	}

	private fun refreshLocationAndSunTimesCoalesced(
		reason: String,
		requestGpsLocation: Boolean = false,
		refreshSunTimes: Boolean = true,
		debounceMs: Long = LOCATION_REFRESH_COALESCE_DELAY_MS,
		forceLocationState: Boolean = false,
		handleProviderChange: Boolean = false
	) {
		if (released) return
		if (Looper.myLooper() != mainHandler.looper) {
			postToMain("refresh_coalesced:$reason") {
				refreshLocationAndSunTimesCoalesced(
					reason = reason,
					requestGpsLocation = requestGpsLocation,
					refreshSunTimes = refreshSunTimes,
					debounceMs = debounceMs,
					forceLocationState = forceLocationState,
					handleProviderChange = handleProviderChange
				)
			}
			return
		}
		Logger.d(
			tag,
			"LOCATION_REFRESH_QUEUE reason=$reason sunTimes=$refreshSunTimes gpsRequest=$requestGpsLocation"
		)
		refreshPipelineNeedsGpsRequest = refreshPipelineNeedsGpsRequest || requestGpsLocation
		refreshPipelineNeedsSunTimes = refreshPipelineNeedsSunTimes || refreshSunTimes
		refreshPipelineForceLocationState = refreshPipelineForceLocationState || forceLocationState
		refreshPipelineHandleProviderChange = refreshPipelineHandleProviderChange || handleProviderChange
		refreshPipelineReason = if (refreshPipelineReason == "idle") {
			reason
		} else {
			"${refreshPipelineReason},$reason"
		}
		refreshPipelineScheduled = true
		val delayMs = debounceMs.coerceAtLeast(0L)
		mainHandler.removeCallbacks(refreshLocationAndSunTimesRunnable)
		if (!mainHandler.postDelayed(refreshLocationAndSunTimesRunnable, delayMs)) {
			Logger.w(tag, "failed to schedule location refresh pipeline reason=$reason")
			refreshPipelineScheduled = false
		}
	}

	private fun refreshOnForegroundIfStale(
		maxStaleMs: Long = FOREGROUND_LOCATION_REFRESH_STALE_MS
	) {
		val elapsed = SystemClock.elapsedRealtime() - lastLocationStateRefreshAtMs
		val hasFreshAutomaticLocation =
			elapsed in 0 until maxStaleMs &&
				automaticLocationSnapshot?.isFreshWithin(FOREGROUND_AUTOMATIC_LOCATION_MAX_AGE_MS) == true
		val shouldRequestGps = locationMode == LocationMode.GPS &&
			!hasFreshAutomaticLocation &&
			lastKnownLocationProvider.hasLocationPermission()
		refreshLocationAndSunTimesCoalesced(
			reason = "foreground",
			requestGpsLocation = shouldRequestGps,
			refreshSunTimes = true,
			debounceMs = LOCATION_REFRESH_COALESCE_DELAY_MS
		)
	}

	private fun maybeResolveGpsPlaceLabel(location: SunLocation) {
		if (released) return
		val key = gpsLocationKey(location)
		if (key == lastGpsPlaceKey && !gpsPlaceLabel.isNullOrBlank()) return
		lastGpsPlaceKey = key
		executeLocationLabelTask {
			if (released) return@executeLocationLabelTask
			val resolved =
				lastKnownLocationProvider.resolveCityOrDistrict(location)
					?: return@executeLocationLabelTask
			postToMain("gps_place_label_result") {
				if (key != lastGpsPlaceKey) return@postToMain
				if (gpsPlaceLabel == resolved) return@postToMain
				gpsPlaceLabel = resolved
				automaticLocationSnapshot?.let { snapshot ->
					if (gpsLocationKey(snapshot.toSunLocation(labelFallback = "gps_cached")) == key) {
						val updated = snapshot.copy(label = resolved)
						automaticLocationSnapshot = updated
						settingsRepository.setAutomaticLocation(updated)
						lastKnownGpsLocation = updated.toSunLocation(labelFallback = "gps_last")
						updateLastKnownCity(updated)
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
		if (released) return false
		if (!location.isFreshWithin(MAX_ACCEPTED_LOCATION_AGE_MS)) {
			Logger.w(
				tag,
				"location sample rejected as stale source=${location.source} capturedAt=${location.capturedAtEpochMs}"
			)
			locationRefreshInProgress = false
			return false
		}
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
		updateLastKnownCity(location)
		if (!location.label.isNullOrBlank()) {
			gpsPlaceLabel = location.label
		}
		invalidateLocationCandidatesCache()
		updateResolvedLocationStateCache()
		updateLocationLabelFromCachedGpsState()
		maybeResolveGpsPlaceLabel(lastKnownGpsLocation ?: return true)

		if (refreshSunTimes && shouldRefreshSunTimesForLocation(previous, location)) {
			refreshLocationAndSunTimesCoalesced(
				reason = "automatic_location",
				requestGpsLocation = false,
				refreshSunTimes = true,
				debounceMs = 0L
			)
		}
		return true
	}

	private fun syncAutomaticLocationTimeZoneIfNeeded(resolution: SunDaylightResolution) {
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
		updateLastKnownCity(updated)
		if (updated.source == LocationSource.CURRENT || updated.source == LocationSource.PASSIVE) {
			liveGpsLocation = updated.toSunLocation(labelFallback = "gps_live")
		}
		invalidateLocationCandidatesCache()
		updateResolvedLocationStateCache()
		updateLocationLabelFromCachedGpsState()
	}

	private fun maybeStartPassiveLocationUpdates() {
		if (passiveLocationUpdatesStarted) return
		if (locationMode != LocationMode.GPS) return
		if (!systemLocationEnabled || !lastKnownLocationProvider.hasLocationPermission()) return
		passiveLocationUpdatesStarted = true
		lastKnownLocationProvider.startPassiveLocationUpdates { location ->
			postToMain("passive_location_result") {
				if (locationMode != LocationMode.GPS) return@postToMain
				applyAutomaticLocationSample(location, refreshSunTimes = true)
			}
		}
	}

	private fun stopPassiveLocationUpdates() {
		if (!passiveLocationUpdatesStarted) return
		passiveLocationUpdatesStarted = false
		lastKnownLocationProvider.stopPassiveLocationUpdates()
	}

	private fun shouldRequestCurrentLocation(lastKnown: LocationSnapshot?): Boolean {
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

	private fun schedulePeriodicSunTimesRefresh() {
		if (released) return
		mainHandler.removeCallbacks(sunTimesRefreshRunnable)
		if (!mainHandler.postDelayed(sunTimesRefreshRunnable, SUN_TIMES_REFRESH_INTERVAL_MS)) {
			Logger.w(tag, "failed to schedule periodic sun-times refresh")
		}
	}

	private fun postToMain(
		reason: String,
		task: () -> Unit
	): Boolean {
		if (released) return false
		val posted = mainHandler.post {
			if (released) return@post
			task()
		}
		if (!posted) {
			Logger.w(tag, "main handler post failed reason=$reason")
		}
		return posted
	}

	private fun executeLocationLabelTask(task: () -> Unit) {
		if (released) return
		runCatching {
			locationLabelExecutor.execute(
				Runnable {
					if (released) return@Runnable
					task()
				}
			)
		}.onFailure { throwable ->
			Logger.w(tag, "location label task rejected", throwable)
		}
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

	private fun resolveInitialLabel(settings: AppSettingsSnapshot): String {
		return if (settings.locationMode == LocationMode.GPS) {
			settings.automaticLocation?.label ?: settings.manualCity.name
		} else {
			settings.manualCity.name
		}
	}

	private fun resolveInitialDaylight(settings: AppSettingsSnapshot): SunDaylight {
		return sunTimesRepository.currentOrFallbackForCandidates(
			buildInitialLocationCandidates(settings)
		)
	}

	private fun gpsLocationKey(location: SunLocation): String {
		return String.format(Locale.US, "%.3f|%.3f", location.latitude, location.longitude)
	}

	private fun formatGpsLabel(location: SunLocation): String {
		return formatGpsLabel(location.latitude, location.longitude)
	}

	private fun formatGpsLabel(latitude: Double, longitude: Double): String {
		return formatGpsCoordinatesLabel(latitude, longitude)
	}

	private fun updateLastKnownCity(location: LocationSnapshot?) {
		val nextCity = location?.toLastKnownManualCity()
		lastKnownCity = nextCity
		if (nextCity != null && manualCity.id == LAST_KNOWN_CITY_ID && manualCity != nextCity) {
			manualCity = nextCity
			settingsRepository.setManualCity(nextCity)
		}
	}

	private fun sunTimesLocationKey(location: LocationSnapshot): String {
		val latitudeBucket =
			(location.latitude * SUN_TIMES_REFRESH_LOCATION_BUCKET_SCALE).roundToInt()
		val longitudeBucket =
			(location.longitude * SUN_TIMES_REFRESH_LOCATION_BUCKET_SCALE).roundToInt()
		return "$latitudeBucket|$longitudeBucket|${location.timeZoneId}"
	}

	private fun buildLocationCandidatesCacheKey(
		manual: SunLocation,
		defaultCity: SunLocation,
		resolvedLocationState: ResolvedLocationState?
	): String {
		val liveGps = resolvedLocationState?.liveGps ?: liveGpsLocation
		val lastGps = resolvedLocationState?.lastGps ?: lastKnownGpsLocation
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

	companion object {
		private const val SUN_TIMES_REFRESH_INTERVAL_MS = 3L * 60L * 60L * 1000L
		private const val GPS_REQUEST_THROTTLE_MS = 1_500L
		private const val FOREGROUND_LOCATION_REFRESH_STALE_MS = 30L * 1000L
		private const val FOREGROUND_AUTOMATIC_LOCATION_MAX_AGE_MS = 30L * 60L * 1000L
		private const val LOCATION_REFRESH_COALESCE_DELAY_MS = 100L
		private const val RESOLVED_LOCATION_STATE_CACHE_TTL_MS = 100L
		private const val LOCATION_CANDIDATES_CACHE_TTL_MS = 1_500L
		private const val LAST_KNOWN_LOCATION_MAX_AGE_MS = 30L * 60L * 1000L
		private const val LOW_POWER_CURRENT_LOCATION_WINDOW_MS = 6L * 60L * 60L * 1000L
		private const val CURRENT_LOCATION_MAX_AGE_MS = 10L * 60L * 1000L
		private const val CURRENT_LOCATION_TIMEOUT_MS = 6_000L
		private const val GPS_REQUEST_RESPONSE_MAX_WAIT_MS = CURRENT_LOCATION_TIMEOUT_MS + 2_000L
		private const val MAX_ACCEPTED_LOCATION_AGE_MS = 24L * 60L * 60L * 1000L
		private const val MAX_ACCEPTABLE_LAST_LOCATION_ACCURACY_METERS = 15_000f
		private const val SUN_TIMES_REFRESH_LOCATION_BUCKET_SCALE = 100.0
		private const val LOCATION_LABEL_QUEUE_CAPACITY = 10
		private const val LOCATION_LABEL_EXECUTOR_SHUTDOWN_TIMEOUT_MS = 2_000L
		private const val LOCATION_LABEL_THREAD_NAME = "LocationLabel"
	}
}
