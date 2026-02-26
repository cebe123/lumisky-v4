package com.example.lumisky

import android.app.WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
import android.app.WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER
import android.app.WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
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
import com.example.lumisky.data.WallpaperCatalog
import com.example.lumisky.ui.debug.FrameJankTelemetry
import com.example.lumisky.ui.preview.PreviewScreen
import com.example.lumisky.ui.theme.LumiskyTheme
import com.example.wallpaper.service.ACTION_APPLY_STORED_WALLPAPER_CONFIG
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.Locale

class PreviewActivity : AppCompatActivity() {

	private val appSettingsRepository by lazy { AppSettingsRepository(applicationContext) }
	private val wallpaperConfigStore by lazy { WallpaperConfigStore(applicationContext) }
	private val sunTimesRepository by lazy { SunTimesRepository() }
	private val lastKnownLocationProvider by lazy { LastKnownLocationProvider(applicationContext) }
	private val setWallpaperExecutor by lazy { Executors.newSingleThreadExecutor() }
	@Volatile
	private var applyingWallpaper: Boolean = false
	private var autoApplyMode: Boolean = false

	override fun onCreate(savedInstanceState: Bundle?) {
		Logger.event(TAG, "onCreate", "savedState" to (savedInstanceState != null))
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
		autoApplyMode = intent.getBooleanExtra(EXTRA_AUTO_APPLY, false)
		val appThemeMode = appSettingsRepository.getAppThemeMode()
		val highRefreshEnabled = appSettingsRepository.isHighRefreshEnabled()
		val performanceMode = appSettingsRepository.getPerformanceMode()
		Logger.event(
			TAG,
			"preview_init",
			"wallpaperId" to wallpaperId,
			"autoApply" to autoApplyMode,
			"highRefresh" to highRefreshEnabled,
			"performanceMode" to performanceMode
		)
		if (autoApplyMode) {
			applyWallpaperWithFreshSunTimes(config)
			return
		}

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
					performanceMode = performanceMode,
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
		Logger.d(TAG, "onDestroy")
		FrameJankTelemetry.stop("PreviewActivity")
		setWallpaperExecutor.shutdownNow()
		sunTimesRepository.release()
		super.onDestroy()
	}

	override fun onStart() {
		super.onStart()
		Logger.d(TAG, "onStart")
		FrameJankTelemetry.start(this, "PreviewActivity")
	}

	override fun onStop() {
		Logger.d(TAG, "onStop")
		FrameJankTelemetry.stop("PreviewActivity")
		super.onStop()
	}

	private fun applyWallpaperWithFreshSunTimes(baseConfig: WallpaperConfig) {
		if (applyingWallpaper) {
			Logger.w(TAG, "applyWallpaper ignored, already running")
			return
		}
		Logger.event(TAG, "applyWallpaper_start", "wallpaperId" to baseConfig.id)
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
				}.distinctBy { "${it.latitude}|${it.longitude}" }
					Logger.event(
						TAG,
						"sunTimes_candidates",
						"count" to candidates.size,
						"locationMode" to settings.locationMode
					)

					val latch = CountDownLatch(1)
					var resolvedDaylight = SunDaylight(
						sunriseMinute = baseConfig.daylight.sunriseMinute,
						sunsetMinute = baseConfig.daylight.sunsetMinute
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
						sunsetMinute = resolvedDaylight.sunsetMinute
					)
				)
				wallpaperConfigStore.saveSelected(finalConfig)
				notifyWallpaperConfigChanged()
				runOnUiThread {
					val directIntent = Intent(ACTION_CHANGE_LIVE_WALLPAPER).apply {
						putExtra(
							EXTRA_LIVE_WALLPAPER_COMPONENT,
							ComponentName(
								this@PreviewActivity,
								com.example.wallpaper.SkyWallpaperService::class.java
							)
						)
					}
					runCatching {
						startActivity(directIntent)
						Logger.d(TAG, "opened ACTION_CHANGE_LIVE_WALLPAPER")
					}.onFailure {
						Logger.w(TAG, "direct apply failed, opening chooser", it)
						startActivity(Intent(ACTION_LIVE_WALLPAPER_CHOOSER))
						Logger.d(TAG, "opened ACTION_LIVE_WALLPAPER_CHOOSER")
					}
					if (autoApplyMode) {
						Logger.d(TAG, "autoApplyMode true -> finish")
						finish()
					}
				}
			} catch (t: Throwable) {
				Logger.e(TAG, "applyWallpaperWithFreshSunTimes failed", t)
				throw t
			} finally {
				applyingWallpaper = false
				Logger.d(TAG, "applyWallpaper finished")
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

	private fun toClockLabel(minute: Int): String {
		val normalized = minute.coerceIn(0, (24 * 60) - 1)
		val hours = normalized / 60
		val minutes = normalized % 60
		return String.format(Locale.US, "%02d:%02d", hours, minutes)
	}

	companion object {
		private const val TAG = "PreviewActivity"
		const val EXTRA_WALLPAPER_ID = "extra_wallpaper_id"
		const val EXTRA_SUNRISE_MINUTE = "extra_sunrise_minute"
		const val EXTRA_SUNSET_MINUTE = "extra_sunset_minute"
		const val EXTRA_AUTO_APPLY = "extra_auto_apply"
		private const val SET_WALLPAPER_REFRESH_TIMEOUT_MS = 1_800L
	}
}
