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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import com.example.lumisky.ui.theme.LumiskyTheme
import com.example.lumisky.viewmodel.HomeViewModel
import com.example.wallpaper.service.ACTION_APPLY_STORED_WALLPAPER_CONFIG
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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
				homeViewModel.onSystemLocationProviderChanged()
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		configureLogging()
		applyLanguage(appSettingsRepository.getLanguageTag())
		super.onCreate(savedInstanceState)

		setContent {
			val darkTheme = when (homeViewModel.appThemeMode) {
				AppThemeMode.SYSTEM -> isSystemInDarkTheme()
				AppThemeMode.LIGHT -> false
				AppThemeMode.DARK -> true
			}
			LumiskyTheme(
				darkTheme = darkTheme
			) {
				var currentScreen by rememberSaveable { mutableStateOf(SCREEN_HOME) }
				val targetSystemBarColor = if (currentScreen == SCREEN_HOME) {
					HomeScreenBackgroundColor
				} else {
					MaterialTheme.colorScheme.background
				}
				val systemBarColor by animateColorAsState(
					targetValue = targetSystemBarColor,
					animationSpec = tween(durationMillis = 320),
					label = "main_system_bar_color"
				)
				SideEffect {
					window.statusBarColor = systemBarColor.toArgb()
					WindowCompat
						.getInsetsController(window, window.decorView)
						.isAppearanceLightStatusBars = systemBarColor.luminance() > 0.5f
				}

				AnimatedContent(
					targetState = currentScreen,
					transitionSpec = {
						val direction = if (targetState == SCREEN_SETTINGS) {
							AnimatedContentTransitionScope.SlideDirection.Left
						} else {
							AnimatedContentTransitionScope.SlideDirection.Right
						}
						(
							slideIntoContainer(
								towards = direction,
								animationSpec = tween(
									durationMillis = 360,
									easing = FastOutSlowInEasing
								),
								initialOffset = { offset -> offset / 3 }
							) + fadeIn(
								animationSpec = tween(durationMillis = 220, delayMillis = 60)
							)
						).togetherWith(
							slideOutOfContainer(
								towards = direction,
								animationSpec = tween(
									durationMillis = 280,
									easing = FastOutSlowInEasing
								),
								targetOffset = { offset -> offset / 4 }
							) + fadeOut(
								animationSpec = tween(durationMillis = 180)
							)
						).using(SizeTransform(clip = false))
					},
					label = "main_screen_transition"
				) { screen ->
					when (screen) {
						SCREEN_HOME -> HomeScreen(
							items = homeViewModel.items,
							selectedWallpaperId = homeViewModel.selectedWallpaperId,
							liveWallpaperId = homeViewModel.liveWallpaperId,
							highRefreshEnabled = homeViewModel.highRefreshEnabled,
							performanceMode = homeViewModel.performanceMode,
							onWallpaperSelected = { id ->
								homeViewModel.onWallpaperSelected(id)
							},
							onCategoryFocused = { ids ->
								homeViewModel.onCategoryFocused(ids)
							},
							onFocusReady = { id ->
								homeViewModel.activateLivePreview(id)
							},
							onFocusCleared = {
								homeViewModel.clearLivePreview()
							},
							onOpenPreview = { id ->
								openWallpaperSetScreen(id)
							},
							onNavigateSettings = {
								currentScreen = SCREEN_SETTINGS
							}
						)

						SCREEN_SETTINGS -> SettingsScreen(
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
								currentScreen = SCREEN_HOME
							}
						)
					}
				}
			}
		}
	}

	override fun onStart() {
		super.onStart()
		registerLocationModeReceiver()
		homeViewModel.refreshOnForegroundIfStale()
	}

	override fun onUserInteraction() {
		super.onUserInteraction()
		homeViewModel.onUserInteraction()
	}

	override fun onStop() {
		unregisterLocationModeReceiver()
		super.onStop()
	}

	override fun onDestroy() {
		unregisterLocationModeReceiver()
		setWallpaperExecutor.shutdownNow()
		sunTimesRepository.release()
		homeViewModel.release()
		super.onDestroy()
	}

	private fun applyLanguage(tag: String) {
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
	}

	private fun unregisterLocationModeReceiver() {
		if (!locationReceiverRegistered) return
		runCatching { unregisterReceiver(locationModeReceiver) }
		locationReceiverRegistered = false
	}

	private fun openWallpaperSetScreen(wallpaperId: String) {
		val baseConfig = homeViewModel.configFor(wallpaperId)
		applyWallpaperWithFreshSunTimes(baseConfig)
	}

	private fun applyWallpaperWithFreshSunTimes(baseConfig: WallpaperConfig) {
		if (applyingWallpaper) {
			Logger.w(TAG, "set wallpaper ignored, apply already in progress")
			return
		}
		applyingWallpaper = true
		setWallpaperExecutor.execute {
			try {
				val candidates = buildSunTimesCandidates()
				val latch = CountDownLatch(1)
				var resolvedDaylight = SunDaylight(
					sunriseMinute = baseConfig.daylight.sunriseMinute,
					sunsetMinute = baseConfig.daylight.sunsetMinute,
					solarNoonMinute = baseConfig.daylight.solarNoonMinute,
					timeZoneId = baseConfig.daylight.timeZoneId
				)
				sunTimesRepository.refreshAsyncWithCandidates(
					candidates = candidates
				) { fetched ->
					resolvedDaylight = fetched
					latch.countDown()
				}
				latch.await(SET_WALLPAPER_REFRESH_TIMEOUT_MS, TimeUnit.MILLISECONDS)

				val finalConfig = baseConfig.copy(
					daylight = DaylightConfig(
						sunriseMinute = resolvedDaylight.sunriseMinute,
						sunsetMinute = resolvedDaylight.sunsetMinute,
						solarNoonMinute = resolvedDaylight.solarNoonMinute,
						timeZoneId = resolvedDaylight.timeZoneId
					)
				)
				wallpaperConfigStore.saveSelected(finalConfig)
				notifyWallpaperConfigChanged()
				runOnUiThread {
					launchSystemWallpaperSetFlow()
				}
			} catch (t: Throwable) {
				Logger.e(TAG, "applyWallpaperWithFreshSunTimes failed", t)
			} finally {
				applyingWallpaper = false
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
		}.onFailure {
			Logger.w(TAG, "direct live wallpaper intent failed, falling back chooser", it)
			runCatching {
				startActivity(Intent(ACTION_LIVE_WALLPAPER_CHOOSER))
				overridePendingTransition(0, 0)
			}
		}
	}

	private fun notifyWallpaperConfigChanged() {
		runCatching {
			sendBroadcast(
				Intent(ACTION_APPLY_STORED_WALLPAPER_CONFIG)
					.setPackage(packageName)
			)
		}.onFailure {
			Logger.w(TAG, "failed to broadcast wallpaper config update", it)
		}
	}

	private fun openSystemLocationPanel() {
		val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
		runCatching {
			startActivity(intent)
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
	}

	companion object {
		private const val TAG = "MainActivity"
		private const val SCREEN_HOME = "home"
		private const val SCREEN_SETTINGS = "settings"
		private const val SET_WALLPAPER_REFRESH_TIMEOUT_MS = 1_800L
	}
}
