package com.example.lumisky

import android.app.WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
import android.app.WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER
import android.app.WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.WindowCompat
import com.example.core.Logger
import com.example.core.api.SunDaylight
import com.example.core.api.SunLocation
import com.example.core.api.SunTimesRepository
import com.example.core.location.LastKnownLocationProvider
import com.example.core.settings.AppSettingsDefaults
import com.example.core.settings.AppSettingsRepository
import com.example.core.settings.AppThemeMode
import com.example.core.settings.LocationMode
import com.example.engine.config.DaylightConfig
import com.example.engine.config.WallpaperConfig
import com.example.engine.config.WallpaperConfigStore
import com.example.lumisky.ui.home.HomeScreen
import com.example.lumisky.ui.home.HomeScreenBackgroundColor
import com.example.lumisky.ui.settings.SettingsScreen
import com.example.lumisky.ui.debug.FrameJankTelemetry
import com.example.lumisky.ui.theme.LumiskyTheme
import com.example.lumisky.viewmodel.HomeViewModel
import com.example.wallpaper.service.ACTION_APPLY_STORED_WALLPAPER_CONFIG
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.Locale

class MainActivity : AppCompatActivity() {

	private val appSettingsRepository by lazy { AppSettingsRepository(applicationContext) }
	private val wallpaperConfigStore by lazy { WallpaperConfigStore(applicationContext) }
	private val sunTimesRepository by lazy { SunTimesRepository() }
	private val lastKnownLocationProvider by lazy { LastKnownLocationProvider(applicationContext) }
	private val setWallpaperExecutor by lazy { Executors.newSingleThreadExecutor() }
	@Volatile
	private var applyingWallpaper: Boolean = false
	private var locationReceiverRegistered: Boolean = false
	internal val homeViewModel by lazy {
		HomeViewModel(
			context = applicationContext,
			settingsRepository = appSettingsRepository
		)
	}
	private val locationModeReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			if (intent?.action == LocationManager.MODE_CHANGED_ACTION) {
				Logger.d(TAG, "LocationManager MODE_CHANGED broadcast received")
				homeViewModel.onSystemLocationProviderChanged()
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		configureLogging()
		Logger.event(
			tag = TAG,
			name = "onCreate",
			"savedState" to (savedInstanceState != null)
		)
		applyLanguage(appSettingsRepository.getLanguageTag())
		super.onCreate(savedInstanceState)

		setContent {
			val darkTheme = when (homeViewModel.appThemeMode) {
				AppThemeMode.SYSTEM -> isSystemInDarkTheme()
				AppThemeMode.LIGHT -> false
				AppThemeMode.DARK -> true
			}
			LumiskyTheme(
				darkTheme = darkTheme,
				dynamicColor = false
			) {
				var currentScreen by rememberSaveable { mutableStateOf("home") }
				val systemBarColor = if (currentScreen == "home") {
					HomeScreenBackgroundColor
				} else {
					MaterialTheme.colorScheme.background
				}
				SideEffect {
					window.statusBarColor = systemBarColor.toArgb()
					WindowCompat
						.getInsetsController(window, window.decorView)
						.isAppearanceLightStatusBars = systemBarColor.luminance() > 0.5f
				}

				when (currentScreen) {
					"home" -> HomeScreen(
						items = homeViewModel.items,
						selectedWallpaperId = homeViewModel.selectedWallpaperId,
						liveWallpaperId = homeViewModel.liveWallpaperId,
						highRefreshEnabled = homeViewModel.highRefreshEnabled,
						performanceMode = homeViewModel.performanceMode,
						onWallpaperSelected = { id ->
							Logger.event(TAG, "wallpaper_selected", "id" to id)
							homeViewModel.onWallpaperSelected(id)
						},
						onCategoryFocused = { ids ->
							Logger.event(TAG, "category_focused", "count" to ids.size)
							homeViewModel.onCategoryFocused(ids)
						},
						onFocusReady = { id ->
							Logger.event(TAG, "focus_ready", "id" to id)
							homeViewModel.activateLivePreview(id)
						},
						onFocusCleared = {
							Logger.d(TAG, "focus_cleared")
							homeViewModel.clearLivePreview()
						},
						onOpenPreview = { id ->
							Logger.event(TAG, "open_set_screen", "id" to id)
							openWallpaperSetScreen(id)
						},
						onNavigateSettings = {
							Logger.d(TAG, "navigate_settings")
							currentScreen = "settings"
						}
					)

					"settings" -> SettingsScreen(
						appThemeMode = homeViewModel.appThemeMode,
						onCycleTheme = { homeViewModel.cycleAppThemeMode() },
						highRefreshEnabled = homeViewModel.highRefreshEnabled,
						onHighRefreshChanged = { enabled -> homeViewModel.updateHighRefreshEnabled(enabled) },
						performanceMode = homeViewModel.performanceMode,
						onPerformanceModeChanged = { mode -> homeViewModel.updatePerformanceMode(mode) },
						locationMode = homeViewModel.locationMode,
						locationLabel = homeViewModel.locationLabel,
						daylight = homeViewModel.daylight,
						gpsLocationAvailable = homeViewModel.gpsLocationAvailable,
						systemLocationEnabled = homeViewModel.systemLocationEnabled,
						onLocationModeChanged = { mode -> homeViewModel.updateLocationMode(mode) },
						onRequestEnableSystemLocation = { openSystemLocationPanel() },
						manualCity = homeViewModel.manualCity,
						onManualCitySelected = { city -> homeViewModel.updateManualCity(city) },
						languageTag = homeViewModel.languageTag,
						onLanguageSelected = { tag ->
							homeViewModel.updateLanguageTag(tag)
							applyLanguage(tag)
						},
						onNavigateHome = {
							Logger.d(TAG, "navigate_home")
							currentScreen = "home"
						}
					)
				}
			}
		}
	}

	override fun onStart() {
		super.onStart()
		Logger.d(TAG, "onStart")
		FrameJankTelemetry.start(this, "MainActivity")
		registerLocationModeReceiver()
		homeViewModel.refreshOnForegroundIfStale()
	}

	override fun onUserInteraction() {
		super.onUserInteraction()
		homeViewModel.onUserInteraction()
	}

	override fun onStop() {
		Logger.d(TAG, "onStop")
		FrameJankTelemetry.stop("MainActivity")
		unregisterLocationModeReceiver()
		super.onStop()
	}

	override fun onDestroy() {
		Logger.d(TAG, "onDestroy")
		FrameJankTelemetry.stop("MainActivity")
		unregisterLocationModeReceiver()
		setWallpaperExecutor.shutdownNow()
		sunTimesRepository.release()
		homeViewModel.release()
		super.onDestroy()
	}

	private fun applyLanguage(tag: String) {
		Logger.event(TAG, "apply_language", "tag" to tag)
		val locales = if (tag == AppSettingsDefaults.LANGUAGE_SYSTEM) {
			LocaleListCompat.getEmptyLocaleList()
		} else {
			LocaleListCompat.forLanguageTags(tag)
		}
		if (AppCompatDelegate.getApplicationLocales().toLanguageTags() == locales.toLanguageTags()) {
			return
		}
		AppCompatDelegate.setApplicationLocales(locales)
	}

	private fun registerLocationModeReceiver() {
		if (locationReceiverRegistered) return
		val filter = IntentFilter(LocationManager.MODE_CHANGED_ACTION)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			registerReceiver(locationModeReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
		} else {
			@Suppress("DEPRECATION")
			registerReceiver(locationModeReceiver, filter)
		}
		locationReceiverRegistered = true
		Logger.d(TAG, "location receiver registered")
	}

	private fun unregisterLocationModeReceiver() {
		if (!locationReceiverRegistered) return
		runCatching { unregisterReceiver(locationModeReceiver) }
		locationReceiverRegistered = false
		Logger.d(TAG, "location receiver unregistered")
	}

	private fun openWallpaperSetScreen(wallpaperId: String) {
		val baseConfig = homeViewModel.configFor(wallpaperId)
		Logger.event(TAG, "open_wallpaper_set", "id" to wallpaperId, "configName" to baseConfig.name)
		applyWallpaperWithFreshSunTimes(baseConfig)
	}

	private fun applyWallpaperWithFreshSunTimes(baseConfig: WallpaperConfig) {
		if (applyingWallpaper) {
			Logger.w(TAG, "set wallpaper ignored, apply already in progress")
			return
		}
		applyingWallpaper = true
		Logger.event(TAG, "set_wallpaper_start", "id" to baseConfig.id)
		setWallpaperExecutor.execute {
			try {
				val candidates = buildSunTimesCandidates()
				Logger.event(
					TAG,
					"sunTimes_candidates",
					"count" to candidates.size
				)
				val latch = CountDownLatch(1)
				var resolvedDaylight = SunDaylight(
					sunriseMinute = baseConfig.daylight.sunriseMinute,
					sunsetMinute = baseConfig.daylight.sunsetMinute,
					solarNoonMinute = baseConfig.daylight.solarNoonMinute,
					timeZoneId = baseConfig.daylight.timeZoneId
				)
				Logger.event(TAG, "sunTimes_fetch_request", "policy" to "daily_location_swr")
				sunTimesRepository.refreshAsyncWithCandidates(
					candidates = candidates
				) { fetched ->
					resolvedDaylight = fetched
					latch.countDown()
				}
				val completedInTime = latch.await(SET_WALLPAPER_REFRESH_TIMEOUT_MS, TimeUnit.MILLISECONDS)
				Logger.event(
					TAG,
					"sunTimes_resolved",
					"completedInTime" to completedInTime,
					"sunrise" to resolvedDaylight.sunriseMinute,
					"sunset" to resolvedDaylight.sunsetMinute,
					"sunriseTime" to toClockLabel(resolvedDaylight.sunriseMinute),
					"sunsetTime" to toClockLabel(resolvedDaylight.sunsetMinute)
				)

				val finalConfig = baseConfig.copy(
					daylight = DaylightConfig(
						sunriseMinute = resolvedDaylight.sunriseMinute,
						sunsetMinute = resolvedDaylight.sunsetMinute,
						solarNoonMinute = resolvedDaylight.solarNoonMinute,
						timeZoneId = resolvedDaylight.timeZoneId
					)
				)
				wallpaperConfigStore.saveSelected(finalConfig)
				Logger.event(
					TAG,
					"wallpaper_config_saved",
					"id" to finalConfig.id,
					"sunrise" to finalConfig.daylight.sunriseMinute,
					"sunset" to finalConfig.daylight.sunsetMinute,
					"sunriseTime" to toClockLabel(finalConfig.daylight.sunriseMinute),
					"sunsetTime" to toClockLabel(finalConfig.daylight.sunsetMinute)
				)
				notifyWallpaperConfigChanged()
				runOnUiThread {
					launchSystemWallpaperSetFlow()
				}
			} catch (t: Throwable) {
				Logger.e(TAG, "applyWallpaperWithFreshSunTimes failed", t)
			} finally {
				applyingWallpaper = false
				Logger.d(TAG, "set wallpaper flow finished")
			}
		}
	}

	private fun buildSunTimesCandidates(): List<SunLocation> {
		val settings = appSettingsRepository.snapshot()
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
			val systemLocationEnabled = runCatching {
				lastKnownLocationProvider.isLocationEnabled()
			}.getOrDefault(false)
			if (settings.locationMode == LocationMode.GPS && systemLocationEnabled) {
				val liveGps = lastKnownLocationProvider.getLastKnownLocation(label = "gps_live")
				val lastGps = lastKnownLocationProvider.getLastKnownLocation(label = "gps_last")
				liveGps?.let { add(it) }
				lastGps?.let { add(it) }
			}
			add(manualLocation)
			add(defaultLocation)
		}.distinctBy { "${it.latitude}|${it.longitude}|${it.timeZoneId.orEmpty()}" }
	}

	private fun launchSystemWallpaperSetFlow() {
		val directIntent = Intent(ACTION_CHANGE_LIVE_WALLPAPER).apply {
			putExtra(
				EXTRA_LIVE_WALLPAPER_COMPONENT,
				ComponentName(
					this@MainActivity,
					com.example.wallpaper.SkyWallpaperService::class.java
				)
			)
		}
		runCatching {
			startActivity(directIntent)
			overridePendingTransition(0, 0)
			Logger.d(TAG, "opened ACTION_CHANGE_LIVE_WALLPAPER")
		}.onFailure {
			Logger.w(TAG, "direct live wallpaper intent failed, falling back chooser", it)
			runCatching {
				startActivity(Intent(ACTION_LIVE_WALLPAPER_CHOOSER))
				overridePendingTransition(0, 0)
				Logger.d(TAG, "opened ACTION_LIVE_WALLPAPER_CHOOSER")
			}
		}
	}

	private fun notifyWallpaperConfigChanged() {
		runCatching {
			sendBroadcast(
				Intent(ACTION_APPLY_STORED_WALLPAPER_CONFIG)
					.setPackage(packageName)
			)
			Logger.d(TAG, "broadcasted wallpaper config update")
		}.onFailure {
			Logger.w(TAG, "failed to broadcast wallpaper config update", it)
		}
	}

	private fun openSystemLocationPanel() {
		val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
		runCatching {
			startActivity(intent)
			Logger.d(TAG, "opened system location panel")
		}.onFailure {
			Logger.w(TAG, "failed to open system location panel", it)
		}
	}

	private fun configureLogging() {
		val debuggable = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
		Logger.configure(
			Logger.Config(
				enabled = true,
				minLevel = if (debuggable) Logger.Level.VERBOSE else Logger.Level.DEBUG,
				tagPrefix = "Lumisky",
				includeThread = true,
				includeUptimeMs = true
			)
		)
		Logger.restartSession()
		Logger.event(TAG, "logger_configured", "debuggable" to debuggable)
	}

	private fun toClockLabel(minute: Int): String {
		val normalized = minute.coerceIn(0, (24 * 60) - 1)
		val hours = normalized / 60
		val minutes = normalized % 60
		return String.format(Locale.US, "%02d:%02d", hours, minutes)
	}

	companion object {
		private const val TAG = "MainActivity"
		private const val SET_WALLPAPER_REFRESH_TIMEOUT_MS = 1_800L
	}
}
