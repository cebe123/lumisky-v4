package com.example.lumisky

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.core.settings.AppSettingsDefaults
import com.example.core.settings.AppSettingsRepository
import com.example.core.settings.AppThemeMode
import com.example.lumisky.ui.home.HomeScreen
import com.example.lumisky.ui.settings.SettingsScreen
import com.example.lumisky.ui.theme.LumiskyTheme
import com.example.lumisky.viewmodel.HomeViewModel
import com.example.snapshot.SnapshotProvider

class MainActivity : AppCompatActivity() {

	private val appSettingsRepository by lazy { AppSettingsRepository(applicationContext) }
	private val snapshotProvider by lazy { SnapshotProvider(applicationContext) }
	private var locationReceiverRegistered: Boolean = false
	private val homeViewModel by lazy {
		HomeViewModel(
			context = applicationContext,
			snapshotProvider = snapshotProvider,
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
		applyLanguage(appSettingsRepository.getLanguageTag())
		super.onCreate(savedInstanceState)

		snapshotProvider.warmUp()

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
						highRefreshEnabled = homeViewModel.highRefreshEnabled,
						onWallpaperSelected = { id ->
							homeViewModel.onWallpaperSelected(id)
						},
						onFocusReady = { id ->
							homeViewModel.activateLivePreview(id)
						},
						onFocusCleared = {
							homeViewModel.clearLivePreview()
						},
						onOpenPreview = { id ->
							homeViewModel.clearLivePreview()
							startActivity(
								Intent(this, PreviewActivity::class.java)
									.putExtra(PreviewActivity.EXTRA_WALLPAPER_ID, id)
									.putExtra(
										PreviewActivity.EXTRA_SUNRISE_MINUTE,
										homeViewModel.daylight.sunriseMinute
									)
									.putExtra(
										PreviewActivity.EXTRA_SUNSET_MINUTE,
										homeViewModel.daylight.sunsetMinute
									)
							)
						},
						onNavigateSettings = {
							homeViewModel.clearLivePreview()
							currentScreen = "settings"
						}
					)

					"settings" -> SettingsScreen(
						appThemeMode = homeViewModel.appThemeMode,
						onCycleTheme = { homeViewModel.cycleAppThemeMode() },
						highRefreshEnabled = homeViewModel.highRefreshEnabled,
						onHighRefreshChanged = { enabled -> homeViewModel.updateHighRefreshEnabled(enabled) },
						locationMode = homeViewModel.locationMode,
						locationLabel = homeViewModel.locationLabel,
						gpsLocationAvailable = homeViewModel.gpsLocationAvailable,
						systemLocationEnabled = homeViewModel.systemLocationEnabled,
						onLocationModeChanged = { mode -> homeViewModel.updateLocationMode(mode) },
						manualCity = homeViewModel.manualCity,
						onManualCitySelected = { city -> homeViewModel.updateManualCity(city) },
						languageTag = homeViewModel.languageTag,
						onLanguageSelected = { tag ->
							homeViewModel.updateLanguageTag(tag)
							applyLanguage(tag)
						},
						onRefreshGpsState = { homeViewModel.refreshLocationAndSunTimes() },
						onNavigateHome = {
							currentScreen = "home"
						}
					)
				}
			}
		}
	}

	override fun onStart() {
		super.onStart()
		registerLocationModeReceiver()
		homeViewModel.onSystemLocationProviderChanged()
	}

	override fun onStop() {
		unregisterLocationModeReceiver()
		super.onStop()
	}

	override fun onDestroy() {
		unregisterLocationModeReceiver()
		homeViewModel.release()
		snapshotProvider.release()
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
}
