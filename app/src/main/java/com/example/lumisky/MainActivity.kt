package com.example.lumisky

import android.app.Activity
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.os.LocaleListCompat
import androidx.core.view.WindowCompat
import com.example.core.Logger
import com.example.core.api.SunDaylight
import com.example.core.api.SunDaylightResolution
import com.example.core.api.SunLocation
import com.example.core.api.SunTimesRepository
import com.example.core.location.asGpsApiCandidate
import com.example.core.location.matchesCoordinates
import com.example.core.location.withResolvedTimeZone
import com.example.core.settings.AppSettingsDefaults
import com.example.core.settings.AppSettingsRepository
import com.example.core.settings.AppThemeMode
import com.example.core.settings.LocationMode
import com.example.engine.config.DaylightConfig
import com.example.engine.config.WallpaperConfig
import com.example.engine.config.WallpaperConfigStore
import com.example.lumisky.ui.home.HomeScreen
import com.example.lumisky.ui.home.HomeScreenBackgroundColor
import com.example.lumisky.ui.home.LaunchSkeleton
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
	private val setWallpaperExecutor by lazy { Executors.newSingleThreadExecutor() }
	private val homeViewModelState = mutableStateOf<HomeViewModel?>(null)
	private val liveWallpaperPreviewLauncher = registerForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) { result ->
		handleLiveWallpaperPreviewResult(result.resultCode)
	}
	private val systemLocationPanelLauncher = registerForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) {
		handleSystemLocationPanelClosed()
	}

	@Volatile
	private var applyingWallpaper: Boolean = false
	private var locationReceiverRegistered: Boolean = false
	private var awaitingSystemLocationEnableResult: Boolean = false
	private var launchThemeMode: AppThemeMode = AppThemeMode.SYSTEM
	private val locationModeReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			if (intent?.action == LocationManager.MODE_CHANGED_ACTION) {
				homeViewModelOrNull()?.onSystemLocationProviderChanged()
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		configureLogging()
		launchThemeMode = appSettingsRepository.getAppThemeMode()
		val launchLanguageTag = appSettingsRepository.getLanguageTag()
		applyLanguage(launchLanguageTag)
		super.onCreate(savedInstanceState)

		setContent {
			val homeViewModel = homeViewModelState.value
			val resolvedThemeMode = homeViewModel?.appThemeMode ?: launchThemeMode
			val darkTheme = when (resolvedThemeMode) {
				AppThemeMode.SYSTEM -> isSystemInDarkTheme()
				AppThemeMode.LIGHT -> false
				AppThemeMode.DARK -> true
			}
			LumiskyTheme(
				darkTheme = darkTheme
			) {
				var currentScreen by rememberSaveable { mutableStateOf(SCREEN_HOME) }
				var startupAnimationsEnabled by rememberSaveable { mutableStateOf(false) }
				val targetSystemBarColor = if (currentScreen == SCREEN_HOME) {
					HomeScreenBackgroundColor
				} else {
					MaterialTheme.colorScheme.background
				}
				val systemBarColor = if (startupAnimationsEnabled) {
					animateColorAsState(
						targetValue = targetSystemBarColor,
						animationSpec = tween(durationMillis = 320),
						label = "main_system_bar_color"
					).value
				} else {
					targetSystemBarColor
				}
				SideEffect {
					window.statusBarColor = systemBarColor.toArgb()
					WindowCompat
						.getInsetsController(window, window.decorView)
						.isAppearanceLightStatusBars = systemBarColor.luminance() > 0.5f
				}

				LaunchedEffect(Unit) {
					withFrameNanos { }
					ensureHomeViewModelCreated()
				}

				if (homeViewModel == null) {
					LaunchSkeleton()
				} else {
					val screenContent: @Composable (String) -> Unit = { screen ->
						when (screen) {
							SCREEN_HOME -> HomeScreen(
								items = homeViewModel.items,
								selectedWallpaperId = homeViewModel.selectedWallpaperId,
								liveWallpaperId = homeViewModel.liveWallpaperId,
								highRefreshEnabled = homeViewModel.highRefreshEnabled,
								performanceMode = homeViewModel.performanceMode,
								onCategoryFocused = { ids ->
									homeViewModel.onCategoryFocused(ids)
								},
								onFocusReady = { id ->
									homeViewModel.activateLivePreview(id)
								},
								onFocusCleared = {
									homeViewModel.clearLivePreview()
								},
								onSetWallpaper = { id ->
									requestWallpaperApply(id)
								},
								onNavigateSettings = {
									currentScreen = SCREEN_SETTINGS
								},
								startupDeferNonCriticalContentOnFirstRender = !startupAnimationsEnabled,
								startupAnimationsEnabled = startupAnimationsEnabled
							)

							SCREEN_SETTINGS -> SettingsScreen(
								appThemeMode = homeViewModel.appThemeMode,
								onThemeModeSelected = { mode -> homeViewModel.updateAppThemeMode(mode) },
								highRefreshEnabled = homeViewModel.highRefreshEnabled,
								onHighRefreshChanged = { enabled ->
									homeViewModel.updateHighRefreshEnabled(
										enabled
									)
								},
								performanceMode = homeViewModel.performanceMode,
								onPerformanceModeChanged = { mode ->
									homeViewModel.updatePerformanceMode(
										mode
									)
								},
								locationMode = homeViewModel.locationMode,
								locationLabel = homeViewModel.locationLabel,
								daylight = homeViewModel.daylight,
								gpsLocationAvailable = homeViewModel.gpsLocationAvailable,
								systemLocationEnabled = homeViewModel.systemLocationEnabled,
								locationRefreshInProgress = homeViewModel.locationRefreshInProgress,
								onLocationModeChanged = { mode ->
									homeViewModel.updateLocationMode(
										mode
									)
								},
								onRefreshLocation = {
									homeViewModel.refreshLocationNow()
								},
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
					LaunchedEffect(homeViewModel) {
						withFrameNanos { }
						startupAnimationsEnabled = true
						reportFullyDrawn()
					}
					LaunchedEffect(homeViewModel.daylight) {
						syncStoredWallpaperDaylightIfNeeded(homeViewModel.daylight)
					}
					if (startupAnimationsEnabled) {
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
											animationSpec = tween(
												durationMillis = 220,
												delayMillis = 60
											)
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
							screenContent(screen)
						}
					} else {
						screenContent(currentScreen)
					}
				}
			}
		}
	}

	override fun onStart() {
		super.onStart()
		registerLocationModeReceiver()
		homeViewModelOrNull()?.onForegroundStarted()
	}

	override fun onUserInteraction() {
		super.onUserInteraction()
		homeViewModelOrNull()?.onUserInteraction()
	}

	override fun onStop() {
		homeViewModelOrNull()?.onForegroundStopped()
		unregisterLocationModeReceiver()
		super.onStop()
	}

	override fun onDestroy() {
		unregisterLocationModeReceiver()
		setWallpaperExecutor.shutdownNow()
		sunTimesRepository.release()
		homeViewModelOrNull()?.release()
		super.onDestroy()
	}

	private fun applyLanguage(tag: String) {
		val locales = if (tag == AppSettingsDefaults.LANGUAGE_SYSTEM) {
			LocaleListCompat.getEmptyLocaleList()
		} else {
			LocaleListCompat.forLanguageTags(tag)
		}
		if (AppCompatDelegate.getApplicationLocales()
				.toLanguageTags() == locales.toLanguageTags()
		) {
			return
		}
		AppCompatDelegate.setApplicationLocales(locales)
	}

	private fun registerLocationModeReceiver() {
		if (locationReceiverRegistered) return
		val filter = IntentFilter(LocationManager.MODE_CHANGED_ACTION)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			registerReceiver(locationModeReceiver, filter, RECEIVER_NOT_EXPORTED)
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

	private fun requestWallpaperApply(wallpaperId: String) {
		val homeViewModel = homeViewModelOrNull() ?: return
		val baseConfig = homeViewModel.configFor(wallpaperId)
		applyWallpaperWithFreshSunTimes(baseConfig = baseConfig)
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
				var resolvedDaylightResolution: SunDaylightResolution? = null
				var resolvedDaylight = SunDaylight(
					sunriseMinute = baseConfig.daylight.sunriseMinute,
					sunsetMinute = baseConfig.daylight.sunsetMinute,
					solarNoonMinute = baseConfig.daylight.solarNoonMinute,
					timeZoneId = baseConfig.daylight.timeZoneId
				)
				sunTimesRepository.refreshResolvedAsyncWithCandidates(
					candidates = candidates
				) { resolution ->
					resolvedDaylightResolution = resolution
					resolvedDaylight = resolution.daylight
					latch.countDown()
				}
				latch.await(SET_WALLPAPER_REFRESH_TIMEOUT_MS, TimeUnit.MILLISECONDS)
				resolvedDaylightResolution?.let(::syncAutomaticLocationTimeZoneIfNeeded)

				val finalConfig = baseConfig.copy(
					daylight = DaylightConfig(
						sunriseMinute = resolvedDaylight.sunriseMinute,
						sunsetMinute = resolvedDaylight.sunsetMinute,
						solarNoonMinute = resolvedDaylight.solarNoonMinute,
						timeZoneId = resolvedDaylight.timeZoneId
					)
				)
				wallpaperConfigStore.savePreview(finalConfig)
				runOnUiThread {
					launchSystemWallpaperSetFlow()
				}
			} catch (t: Throwable) {
				wallpaperConfigStore.clearPreview()
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
			if (settings.locationMode == LocationMode.GPS) {
				settings.automaticLocation
					?.toSunLocation(labelFallback = "gps_cached")
					?.asGpsApiCandidate()
					?.let { add(it) }
			}
			add(manualLocation)
			add(defaultLocation)
		}.distinctBy { "${it.latitude}|${it.longitude}|${it.timeZoneId.orEmpty()}" }
	}

	private fun syncAutomaticLocationTimeZoneIfNeeded(
		resolution: SunDaylightResolution
	) {
		val sourceLocation = resolution.sourceLocation ?: return
		val resolvedTimeZoneId = resolution.daylight.timeZoneId ?: return
		if (appSettingsRepository.getLocationMode() != LocationMode.GPS) return
		val automaticLocation = appSettingsRepository.getAutomaticLocation() ?: return
		if (!automaticLocation.matchesCoordinates(sourceLocation)) return
		val updated = automaticLocation.withResolvedTimeZone(resolvedTimeZoneId)
		if (updated == automaticLocation) return
		appSettingsRepository.setAutomaticLocation(updated)
	}

	private fun handleLiveWallpaperPreviewResult(resultCode: Int) {
		when (resultCode) {
			Activity.RESULT_OK -> {
				val promoted = wallpaperConfigStore.promotePreviewToSelected()
				if (promoted != null) {
					notifyWallpaperConfigChanged()
				}
			}
			else -> wallpaperConfigStore.clearPreview()
		}
	}

	private fun syncStoredWallpaperDaylightIfNeeded(daylight: SunDaylight) {
		if (daylight.timeZoneId.isNullOrBlank()) return
		setWallpaperExecutor.execute {
			runCatching {
				val storedConfig = wallpaperConfigStore.loadSelected() ?: return@runCatching
				val updatedDaylight = DaylightConfig(
					sunriseMinute = daylight.sunriseMinute,
					sunsetMinute = daylight.sunsetMinute,
					solarNoonMinute = daylight.solarNoonMinute,
					timeZoneId = daylight.timeZoneId
				)
				if (storedConfig.daylight == updatedDaylight) return@runCatching
				wallpaperConfigStore.saveSelected(
					storedConfig.copy(daylight = updatedDaylight)
				)
				notifyWallpaperConfigChanged()
			}.onFailure { throwable ->
				Logger.w(TAG, "syncStoredWallpaperDaylightIfNeeded failed", throwable)
			}
		}
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
			liveWallpaperPreviewLauncher.launch(directIntent)
		}.onFailure {
			wallpaperConfigStore.clearPreview()
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
			awaitingSystemLocationEnableResult = true
			systemLocationPanelLauncher.launch(intent)
		}.onFailure {
			awaitingSystemLocationEnableResult = false
			Logger.w(TAG, "failed to open system location panel", it)
		}
	}

	private fun handleSystemLocationPanelClosed() {
		if (!awaitingSystemLocationEnableResult) return
		awaitingSystemLocationEnableResult = false
		val homeViewModel = homeViewModelOrNull() ?: return
		homeViewModel.onSystemLocationProviderChanged()
		if (!homeViewModel.systemLocationEnabled && homeViewModel.locationMode == LocationMode.GPS) {
			homeViewModel.updateLocationMode(LocationMode.MANUAL)
		}
	}

	private fun configureLogging() {
		val debuggable =
			(applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
		Logger.configure(
			Logger.Config(
				minLevel = if (debuggable) Logger.Level.VERBOSE else Logger.Level.DEBUG
			)
		)
		Logger.restartSession()
	}

	internal fun homeViewModelOrNull(): HomeViewModel? = homeViewModelState.value

	private fun ensureHomeViewModelCreated(): HomeViewModel {
		homeViewModelOrNull()?.let { return it }
		val initialSettings = appSettingsRepository.snapshot()
		return HomeViewModel(
			context = applicationContext,
			settingsRepository = appSettingsRepository,
			initialSettings = initialSettings
		).also { created ->
			homeViewModelState.value = created
		}
	}

	companion object {
		private const val TAG = "MainActivity"
		private const val SCREEN_HOME = "home"
		private const val SCREEN_SETTINGS = "settings"
		private const val SET_WALLPAPER_REFRESH_TIMEOUT_MS = 1_800L
	}
}
