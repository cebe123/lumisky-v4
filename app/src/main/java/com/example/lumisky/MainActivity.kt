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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.core.Logger
import com.example.core.settings.AppSettingsDefaults
import com.example.core.settings.AppSettingsRepository
import com.example.core.settings.AppThemeMode
import com.example.engine.config.WallpaperConfigStore
import com.example.lumisky.ui.home.HomeScreen
import com.example.lumisky.ui.settings.SettingsScreen
import com.example.lumisky.ui.debug.FrameJankTelemetry
import com.example.lumisky.ui.theme.LumiskyTheme
import com.example.lumisky.viewmodel.HomeViewModel

class MainActivity : AppCompatActivity() {

	private val appSettingsRepository by lazy { AppSettingsRepository(applicationContext) }
	private val wallpaperConfigStore by lazy { WallpaperConfigStore(applicationContext) }
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

				when (currentScreen) {
					"home" -> HomeScreen(
						items = homeViewModel.items,
						selectedWallpaperId = homeViewModel.selectedWallpaperId,
						liveWallpaperId = homeViewModel.liveWallpaperId,
						daylightLabel = "${homeViewModel.daylight.sunriseMinute} / ${homeViewModel.daylight.sunsetMinute}",
						startupLoading = homeViewModel.startupLoading,
						startupProgress = homeViewModel.startupProgress,
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
							homeViewModel.clearLivePreview()
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
		val config = homeViewModel.configFor(wallpaperId)
		wallpaperConfigStore.saveSelected(config)
		Logger.event(TAG, "open_wallpaper_set", "id" to wallpaperId, "configName" to config.name)
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

	companion object {
		private const val TAG = "MainActivity"
	}
}
