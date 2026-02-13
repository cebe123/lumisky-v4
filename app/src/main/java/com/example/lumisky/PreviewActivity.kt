package com.example.lumisky

import android.app.WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
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
import com.example.lumisky.data.WallpaperCatalog
import com.example.lumisky.ui.preview.PreviewScreen
import com.example.lumisky.ui.theme.LumiskyTheme
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PreviewActivity : AppCompatActivity() {

	private val appSettingsRepository by lazy { AppSettingsRepository(applicationContext) }
	private val wallpaperConfigStore by lazy { WallpaperConfigStore(applicationContext) }
	private val sunTimesRepository by lazy { SunTimesRepository() }
	private val lastKnownLocationProvider by lazy { LastKnownLocationProvider(applicationContext) }
	private val setWallpaperExecutor by lazy { Executors.newSingleThreadExecutor() }
	@Volatile
	private var applyingWallpaper: Boolean = false

	override fun onCreate(savedInstanceState: Bundle?) {
		applyLanguage(appSettingsRepository.getLanguageTag())
		super.onCreate(savedInstanceState)
		val wallpaperId = intent.getStringExtra(EXTRA_WALLPAPER_ID) ?: "preview_default"
		val daylight = DaylightConfig(
			sunriseMinute = intent.getIntExtra(EXTRA_SUNRISE_MINUTE, SunDaylight.fallback().sunriseMinute),
			sunsetMinute = intent.getIntExtra(EXTRA_SUNSET_MINUTE, SunDaylight.fallback().sunsetMinute)
		)
		val config = WallpaperCatalog.configById(
			id = wallpaperId,
			daylight = SunDaylight(
				sunriseMinute = daylight.sunriseMinute,
				sunsetMinute = daylight.sunsetMinute
			)
		).copy(daylight = daylight)
		val appThemeMode = appSettingsRepository.getAppThemeMode()
		val highRefreshEnabled = appSettingsRepository.isHighRefreshEnabled()

		setContent {
			val darkTheme = when (appThemeMode) {
				AppThemeMode.SYSTEM -> isSystemInDarkTheme()
				AppThemeMode.LIGHT -> false
				AppThemeMode.DARK -> true
			}
			LumiskyTheme(
				darkTheme = darkTheme,
				dynamicColor = false
			) {
				PreviewScreen(
					config = config,
					highRefreshEnabled = highRefreshEnabled,
					onSetWallpaper = {
						applyWallpaperWithFreshSunTimes(config)
					},
					onBack = { finish() }
				)
			}
		}
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

	override fun onDestroy() {
		setWallpaperExecutor.shutdownNow()
		sunTimesRepository.release()
		super.onDestroy()
	}

	private fun applyWallpaperWithFreshSunTimes(baseConfig: WallpaperConfig) {
		if (applyingWallpaper) return
		applyingWallpaper = true
		setWallpaperExecutor.execute {
			try {
				val settings = appSettingsRepository.snapshot()
				val manualLocation = SunLocation(
					label = settings.manualCity.name,
					latitude = settings.manualCity.latitude,
					longitude = settings.manualCity.longitude
				)
				val defaultLocation = SunLocation(
					label = "default_city",
					latitude = AppSettingsDefaults.DEFAULT_CITY.latitude,
					longitude = AppSettingsDefaults.DEFAULT_CITY.longitude
				)

				val candidates = buildList {
					if (settings.locationMode == LocationMode.GPS) {
						val liveGps = if (lastKnownLocationProvider.isLocationEnabled()) {
							lastKnownLocationProvider.getLastKnownLocation(label = "gps_live")
						} else {
							null
						}
						val lastGps = lastKnownLocationProvider.getLastKnownLocation(
							label = "gps_last",
							allowWhenLocationDisabled = true
						)
						liveGps?.let { add(it) }
						lastGps?.let { add(it) }
					}
					add(manualLocation)
					add(defaultLocation)
				}.distinctBy { "${it.latitude}|${it.longitude}" }

				val latch = CountDownLatch(1)
				var resolvedDaylight = SunDaylight(
					sunriseMinute = baseConfig.daylight.sunriseMinute,
					sunsetMinute = baseConfig.daylight.sunsetMinute
				)
				sunTimesRepository.refreshAsyncWithCandidates(candidates) { fetched ->
					resolvedDaylight = fetched
					latch.countDown()
				}
				latch.await(SET_WALLPAPER_REFRESH_TIMEOUT_MS, TimeUnit.MILLISECONDS)

				val finalConfig = baseConfig.copy(
					daylight = DaylightConfig(
						sunriseMinute = resolvedDaylight.sunriseMinute,
						sunsetMinute = resolvedDaylight.sunsetMinute
					)
				)
				wallpaperConfigStore.saveSelected(finalConfig)
				runOnUiThread {
					startActivity(Intent(ACTION_LIVE_WALLPAPER_CHOOSER))
				}
			} finally {
				applyingWallpaper = false
			}
		}
	}

	companion object {
		const val EXTRA_WALLPAPER_ID = "extra_wallpaper_id"
		const val EXTRA_SUNRISE_MINUTE = "extra_sunrise_minute"
		const val EXTRA_SUNSET_MINUTE = "extra_sunset_minute"
		private const val SET_WALLPAPER_REFRESH_TIMEOUT_MS = 1_800L
	}
}
