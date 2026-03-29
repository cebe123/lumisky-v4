package com.example.wallpaper.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
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
import com.example.engine.config.DaylightConfig
import com.example.engine.config.WallpaperConfigStore
import kotlin.math.roundToInt

class WallpaperDaylightSyncCoordinator(
	context: Context,
	private val configStore: WallpaperConfigStore = WallpaperConfigStore(context),
	private val settingsRepository: AppSettingsRepository = AppSettingsRepository(context),
	private val lastKnownLocationProvider: LastKnownLocationProvider = LastKnownLocationProvider(context),
	private val sunTimesRepository: SunTimesRepository = SunTimesRepository()
) {
	private val appContext = context.applicationContext
	private val mainHandler = Handler(Looper.getMainLooper())
	private var previewMode: Boolean = false
	private var visible: Boolean = false
	private var passiveLocationUpdatesStarted: Boolean = false
	private var locationModeReceiverRegistered: Boolean = false
	private var lastGpsRequestAtMs: Long = 0L
	private var settingsChangeListenerHandle: AutoCloseable? = null
	private var lastObservedSettingsLocationSignature: String? = null
	private var automaticLocationSnapshot: LocationSnapshot? = settingsRepository.getAutomaticLocation()
	private var liveGpsLocation: SunLocation? = automaticLocationSnapshot
		?.takeIf { it.source == LocationSource.CURRENT || it.source == LocationSource.PASSIVE }
		?.toSunLocation(labelFallback = "wallpaper_gps_live")
	private val tag = "WallpaperDaylightSync"
	private val periodicRefreshRunnable = object : Runnable {
		override fun run() {
			if (!visible || previewMode) return
			refreshDaylight(requestGpsLocation = true)
			schedulePeriodicRefresh()
		}
	}
	private val locationModeReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			if (intent?.action != LocationManager.MODE_CHANGED_ACTION) return
			handleSystemLocationProviderChanged()
		}
	}

	fun onCreate() {
		val initialSettings = settingsRepository.snapshot()
		lastObservedSettingsLocationSignature = buildSettingsLocationSignature(initialSettings)
		settingsChangeListenerHandle = settingsRepository.addChangeListener { snapshot ->
			mainHandler.post {
				handleSettingsSnapshot(snapshot)
			}
		}
		registerLocationModeReceiver()
	}

	fun onDestroy() {
		settingsChangeListenerHandle?.close()
		settingsChangeListenerHandle = null
		mainHandler.removeCallbacks(periodicRefreshRunnable)
		stopPassiveLocationUpdates()
		unregisterLocationModeReceiver()
		sunTimesRepository.release()
	}

	fun setPreviewMode(enabled: Boolean) {
		if (previewMode == enabled) return
		previewMode = enabled
		if (enabled) {
			mainHandler.removeCallbacks(periodicRefreshRunnable)
			stopPassiveLocationUpdates()
		} else if (visible) {
			refreshDaylight(requestGpsLocation = true)
			maybeStartPassiveLocationUpdates()
			schedulePeriodicRefresh()
		}
	}

	fun onVisibilityChanged(value: Boolean) {
		visible = value
		mainHandler.removeCallbacks(periodicRefreshRunnable)
		if (!value || previewMode) {
			stopPassiveLocationUpdates()
			return
		}
		refreshDaylight(requestGpsLocation = true)
		maybeStartPassiveLocationUpdates()
		schedulePeriodicRefresh()
	}

	private fun handleSystemLocationProviderChanged() {
		if (!visible || previewMode) return
		refreshAutomaticLocationSnapshot()
		if (settingsRepository.getLocationMode() != LocationMode.GPS) {
			stopPassiveLocationUpdates()
			refreshDaylight(requestGpsLocation = false)
			return
		}
		if (lastKnownLocationProvider.isLocationEnabled()) {
			maybeStartPassiveLocationUpdates()
		} else {
			stopPassiveLocationUpdates()
		}
		refreshDaylight(
			requestGpsLocation = lastKnownLocationProvider.hasLocationPermission()
		)
	}

	private fun handleSettingsSnapshot(settings: AppSettingsSnapshot) {
		val signature = buildSettingsLocationSignature(settings)
		if (signature == lastObservedSettingsLocationSignature) return
		lastObservedSettingsLocationSignature = signature
		refreshAutomaticLocationSnapshot()

		if (settings.locationMode != LocationMode.GPS) {
			stopPassiveLocationUpdates()
		} else if (!previewMode && visible && lastKnownLocationProvider.isLocationEnabled()) {
			maybeStartPassiveLocationUpdates()
		}

		if (previewMode) return
		resolveAndStoreDaylight(settings)
	}

	private fun refreshDaylight(requestGpsLocation: Boolean) {
		refreshAutomaticLocationSnapshot()
		if (previewMode) return
		val settings = settingsRepository.snapshot()
		if (settings.locationMode != LocationMode.GPS || !lastKnownLocationProvider.hasLocationPermission()) {
			stopPassiveLocationUpdates()
			resolveAndStoreDaylight(settings)
			return
		}
		if (!requestGpsLocation) {
			resolveAndStoreDaylight(settings)
			maybeStartPassiveLocationUpdates()
			return
		}
		requestImmediateGpsLocation(settings)
	}

	private fun requestImmediateGpsLocation(settings: com.example.core.settings.AppSettingsSnapshot) {
		val now = SystemClock.elapsedRealtime()
		if ((now - lastGpsRequestAtMs) < GPS_REQUEST_THROTTLE_MS) {
			resolveAndStoreDaylight(settings)
			return
		}
		lastGpsRequestAtMs = now
		lastKnownLocationProvider.requestLastKnownLocation(
			allowWhenLocationDisabled = true
		) { location ->
			mainHandler.post {
				val shouldRequestCurrent = shouldRequestCurrentLocation(location)
				location?.let { snapshot ->
					applyAutomaticLocationSample(
						location = snapshot,
						refreshDaylight = !shouldRequestCurrent
					)
				}
				if (shouldRequestCurrent) {
					requestCurrentGpsLocation()
				} else {
					resolveAndStoreDaylight(settingsRepository.snapshot())
				}
			}
		}
	}

	private fun requestCurrentGpsLocation() {
		lastKnownLocationProvider.requestCurrentLocation(
			preferLowPower = shouldPreferLowPowerCurrentLocation(),
			maxUpdateAgeMillis = CURRENT_LOCATION_MAX_AGE_MS,
			timeoutMillis = CURRENT_LOCATION_TIMEOUT_MS
		) { location ->
			mainHandler.post {
				location?.let { snapshot ->
					applyAutomaticLocationSample(
						location = snapshot,
						refreshDaylight = true
					)
				} ?: run {
					resolveAndStoreDaylight(settingsRepository.snapshot())
				}
			}
		}
	}

	private fun maybeStartPassiveLocationUpdates() {
		if (passiveLocationUpdatesStarted) return
		if (previewMode || !visible) return
		if (settingsRepository.getLocationMode() != LocationMode.GPS) return
		if (!lastKnownLocationProvider.hasLocationPermission()) return
		if (!lastKnownLocationProvider.isLocationEnabled()) return
		passiveLocationUpdatesStarted = true
		lastKnownLocationProvider.startPassiveLocationUpdates { location ->
			mainHandler.post {
				val refreshed = applyAutomaticLocationSample(
					location = location,
					refreshDaylight = true
				)
				if (!refreshed) {
					resolveAndStoreDaylight(settingsRepository.snapshot())
				}
			}
		}
	}

	private fun stopPassiveLocationUpdates() {
		if (!passiveLocationUpdatesStarted) return
		passiveLocationUpdatesStarted = false
		lastKnownLocationProvider.stopPassiveLocationUpdates()
	}

	private fun resolveAndStoreDaylight(settings: com.example.core.settings.AppSettingsSnapshot) {
		val currentConfig = configStore.loadSelected() ?: return
		val candidates = buildSunTimesCandidates(settings)
		if (candidates.isEmpty()) return
		sunTimesRepository.refreshResolvedAsyncWithCandidates(candidates = candidates) { resolution ->
			mainHandler.post {
				syncAutomaticLocationTimeZoneIfNeeded(resolution)
				updateStoredWallpaperDaylightIfNeeded(
					daylight = resolution.daylight,
					currentConfigId = currentConfig.id
				)
			}
		}
	}

	private fun updateStoredWallpaperDaylightIfNeeded(
		daylight: SunDaylight,
		currentConfigId: String
	) {
		val latestConfig = configStore.loadSelected() ?: return
		if (latestConfig.id != currentConfigId) return
		val updatedDaylight = DaylightConfig(
			sunriseMinute = daylight.sunriseMinute,
			sunsetMinute = daylight.sunsetMinute,
			solarNoonMinute = daylight.solarNoonMinute,
			timeZoneId = daylight.timeZoneId
		)
		if (latestConfig.daylight == updatedDaylight) return
		configStore.saveSelected(
			latestConfig.copy(daylight = updatedDaylight)
		)
		Logger.d(
			tag,
			"stored_daylight_updated wallpaper=${latestConfig.id} sunrise=${daylight.sunriseMinute} sunset=${daylight.sunsetMinute} solarNoon=${daylight.solarNoonMinute} tz=${daylight.timeZoneId.orEmpty()}"
		)
		appContext.sendBroadcast(
			Intent(ACTION_APPLY_STORED_WALLPAPER_CONFIG)
				.setPackage(appContext.packageName)
		)
	}

	private fun buildSunTimesCandidates(
		settings: com.example.core.settings.AppSettingsSnapshot
	): List<SunLocation> {
		val manualLocation = SunLocation(
			label = settings.manualCity.name,
			latitude = settings.manualCity.latitude,
			longitude = settings.manualCity.longitude,
			timeZoneId = settings.manualCity.timeZoneId
		)
		val defaultLocation = SunLocation(
			label = "default_city",
			latitude = AppSettingsDefaults.DEFAULT_CITY.latitude,
			longitude = AppSettingsDefaults.DEFAULT_CITY.longitude,
			timeZoneId = AppSettingsDefaults.DEFAULT_CITY.timeZoneId
		)
		return buildList {
			if (settings.locationMode == LocationMode.GPS) {
				liveGpsLocation?.let { add(it.asGpsApiCandidate()) }
				automaticLocationSnapshot
					?.toSunLocation(labelFallback = "wallpaper_gps_cached")
					?.asGpsApiCandidate()
					?.let { add(it) }
			}
			add(manualLocation)
			add(defaultLocation)
		}.distinctBy { candidate ->
			"${candidate.latitude}|${candidate.longitude}|${candidate.timeZoneId.orEmpty()}"
		}
	}

	private fun refreshAutomaticLocationSnapshot() {
		automaticLocationSnapshot = settingsRepository.getAutomaticLocation()
		if (automaticLocationSnapshot?.source != LocationSource.CURRENT &&
			automaticLocationSnapshot?.source != LocationSource.PASSIVE
		) {
			liveGpsLocation = null
		}
	}

	private fun buildSettingsLocationSignature(settings: AppSettingsSnapshot): String {
		val automaticLocation = settings.automaticLocation
		return buildString {
			append(settings.locationMode.name)
			append('|')
			append(settings.manualCity.id)
			append('|')
			append(settings.manualCity.latitude)
			append('|')
			append(settings.manualCity.longitude)
			append('|')
			append(settings.manualCity.timeZoneId)
			append('|')
			append(automaticLocation?.latitude ?: "null")
			append('|')
			append(automaticLocation?.longitude ?: "null")
			append('|')
			append(automaticLocation?.timeZoneId ?: "null")
		}
	}

	private fun applyAutomaticLocationSample(
		location: LocationSnapshot,
		refreshDaylight: Boolean
	): Boolean {
		val previous = automaticLocationSnapshot
		if (previous != null && previous.capturedAtEpochMs > location.capturedAtEpochMs) {
			return false
		}
		automaticLocationSnapshot = location
		settingsRepository.setAutomaticLocation(location)
		if (location.source == LocationSource.CURRENT || location.source == LocationSource.PASSIVE) {
			liveGpsLocation = location.toSunLocation(labelFallback = "wallpaper_gps_live")
		}
		if (refreshDaylight && shouldRefreshSunTimesForLocation(previous, location)) {
			resolveAndStoreDaylight(settingsRepository.snapshot())
		}
		return true
	}

	private fun syncAutomaticLocationTimeZoneIfNeeded(
		resolution: SunDaylightResolution
	) {
		if (settingsRepository.getLocationMode() != LocationMode.GPS) return
		val sourceLocation = resolution.sourceLocation ?: return
		val resolvedTimeZoneId = resolution.daylight.timeZoneId ?: return
		val snapshot = automaticLocationSnapshot ?: return
		if (!snapshot.matchesCoordinates(sourceLocation)) return
		val updated = snapshot.withResolvedTimeZone(resolvedTimeZoneId)
		if (updated == snapshot) return
		automaticLocationSnapshot = updated
		settingsRepository.setAutomaticLocation(updated)
		if (updated.source == LocationSource.CURRENT || updated.source == LocationSource.PASSIVE) {
			liveGpsLocation = updated.toSunLocation(labelFallback = "wallpaper_gps_live")
		}
	}

	private fun shouldRequestCurrentLocation(lastKnown: LocationSnapshot?): Boolean {
		if (!lastKnownLocationProvider.isLocationEnabled()) return false
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

	private fun sunTimesLocationKey(location: LocationSnapshot): String {
		val latitudeBucket = (location.latitude * SUN_TIMES_REFRESH_LOCATION_BUCKET_SCALE).roundToInt()
		val longitudeBucket = (location.longitude * SUN_TIMES_REFRESH_LOCATION_BUCKET_SCALE).roundToInt()
		return "$latitudeBucket|$longitudeBucket|${location.timeZoneId}"
	}

	private fun schedulePeriodicRefresh() {
		mainHandler.removeCallbacks(periodicRefreshRunnable)
		mainHandler.postDelayed(periodicRefreshRunnable, SUN_TIMES_REFRESH_INTERVAL_MS)
	}

	private fun registerLocationModeReceiver() {
		if (locationModeReceiverRegistered) return
		val filter = IntentFilter(LocationManager.MODE_CHANGED_ACTION)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			appContext.registerReceiver(locationModeReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
		} else {
			@Suppress("DEPRECATION")
			appContext.registerReceiver(locationModeReceiver, filter)
		}
		locationModeReceiverRegistered = true
	}

	private fun unregisterLocationModeReceiver() {
		if (!locationModeReceiverRegistered) return
		runCatching { appContext.unregisterReceiver(locationModeReceiver) }
			.onFailure { throwable ->
				Logger.w(tag, "unregisterLocationModeReceiver failed", throwable)
			}
		locationModeReceiverRegistered = false
	}

	private companion object {
		private const val SUN_TIMES_REFRESH_INTERVAL_MS = 3L * 60L * 60L * 1000L
		private const val GPS_REQUEST_THROTTLE_MS = 1_500L
		private const val LAST_KNOWN_LOCATION_MAX_AGE_MS = 30L * 60L * 1000L
		private const val LOW_POWER_CURRENT_LOCATION_WINDOW_MS = 6L * 60L * 60L * 1000L
		private const val CURRENT_LOCATION_MAX_AGE_MS = 10L * 60L * 1000L
		private const val CURRENT_LOCATION_TIMEOUT_MS = 6_000L
		private const val MAX_ACCEPTABLE_LAST_LOCATION_ACCURACY_METERS = 15_000f
		private const val SUN_TIMES_REFRESH_LOCATION_BUCKET_SCALE = 100.0
	}
}
