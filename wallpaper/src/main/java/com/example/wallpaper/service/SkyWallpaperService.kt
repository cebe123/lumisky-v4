package com.example.wallpaper.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.PowerManager
import android.os.UserManager
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.example.core.Logger
import com.example.core.motion.TiltParallaxTracker
import com.example.core.report.CrashDiagnostics
import com.example.core.settings.AppSettingsRepository
import com.example.engine.config.WallpaperConfig
import com.example.engine.config.WallpaperConfigStore
import com.example.wallpaper.engine.WallpaperRenderEngine
import com.example.wallpaper.render.SceneStateHasher

open class SkyWallpaperService : WallpaperService() {

	override fun onCreateEngine(): Engine {
		return SkyWallpaperEngine()
	}

	private inner class SkyWallpaperEngine : Engine() {
		private val appContext = this@SkyWallpaperService.applicationContext
		private val configStore = WallpaperConfigStore(appContext)
		private val settingsRepository = AppSettingsRepository(appContext)
		private val renderController = WallpaperRenderController(
			renderEngine = WallpaperRenderEngine(appContext),
			scheduler = MinuteTickScheduler(),
			hasher = SceneStateHasher(),
			policyResolver = ServiceRenderPolicyResolver(
				isPowerSaveModeProvider = {
					isBatterySaver()
				},
				thermalStatusProvider = {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
						appContext.getSystemService(PowerManager::class.java)?.currentThermalStatus
					} else {
						null
					}
				}
			),
			displayRefreshRateProvider = {
				resolveWallpaperDisplayRefreshRateHz(appContext)
			}
		)
		private var daylightSyncCoordinator: WallpaperDaylightSyncCoordinator? = null
		private var configRefreshReceiverRegistered: Boolean = false
		private var userUnlockReceiverRegistered: Boolean = false
		private var settingsChangeSubscription: AutoCloseable? = null
		private var daylightSyncDeferredUntilUnlockLogged: Boolean = false
		private var renderSettingsDeferredUntilUnlockLogged: Boolean = false
		private var engineVisible: Boolean = false
		private var engineSurfaceAttached: Boolean = false
		private var lastAppliedConfigSignature: String? = null
		private val tiltParallaxTracker = TiltParallaxTracker(appContext) { x, y ->
			renderController.setParallaxOffset(x, y)
		}
		private val configRefreshReceiver = object : BroadcastReceiver() {
			override fun onReceive(context: Context?, intent: Intent?) {
				if (intent?.action != ACTION_APPLY_STORED_WALLPAPER_CONFIG) return
				applyStoredConfig()
			}
		}
		private val userUnlockReceiver = object : BroadcastReceiver() {
			override fun onReceive(context: Context?, intent: Intent?) {
				if (intent?.action != Intent.ACTION_USER_UNLOCKED) return
				applyUserUnlockedSettingsIfAvailable()
				maybeStartDaylightSyncCoordinator()
				maybeRestoreLockScreenWallpaperSharing()
				applyStoredConfig()
			}
		}

		override fun onCreate(surfaceHolder: SurfaceHolder) {
			super.onCreate(surfaceHolder)
			registerConfigRefreshReceiver()
			registerUserUnlockReceiver()
			applyUserUnlockedSettingsIfAvailable()
			reportBatterySaver()
			renderController.setPreviewMode(isPreview)
			maybeStartDaylightSyncCoordinator()
			daylightSyncCoordinator?.setPreviewMode(isPreview)
			maybeRestoreLockScreenWallpaperSharing()
			applyStoredConfig()
			renderController.onCreate()
			updateParallaxTrackingState()
		}

		override fun onVisibilityChanged(visible: Boolean) {
			super.onVisibilityChanged(visible)
			engineVisible = visible
			renderController.setPreviewMode(isPreview)
			maybeStartDaylightSyncCoordinator()
			daylightSyncCoordinator?.setPreviewMode(isPreview)
			maybeRestoreLockScreenWallpaperSharing()
			if (visible) {
				applyUserUnlockedSettingsIfAvailable()
				reportBatterySaver()
				applyStoredConfig()
			}
			daylightSyncCoordinator?.onVisibilityChanged(visible)
			updateParallaxTrackingState()
			renderController.onVisibilityChanged(visible)
		}

		override fun onSurfaceCreated(holder: SurfaceHolder) {
			super.onSurfaceCreated(holder)
			engineSurfaceAttached = true
			renderController.setPreviewMode(isPreview)
			applyUserUnlockedSettingsIfAvailable()
			reportBatterySaver()
			maybeStartDaylightSyncCoordinator()
			daylightSyncCoordinator?.setPreviewMode(isPreview)
			maybeRestoreLockScreenWallpaperSharing()
			applyStoredConfig()
			updateParallaxTrackingState()
			renderController.onSurfaceCreated(holder)
		}

		override fun onSurfaceDestroyed(holder: SurfaceHolder) {
			engineSurfaceAttached = false
			updateParallaxTrackingState()
			renderController.onSurfaceDestroyed()
			super.onSurfaceDestroyed(holder)
		}

		override fun onDestroy() {
			tiltParallaxTracker.close()
			daylightSyncCoordinator?.onDestroy()
			daylightSyncCoordinator = null
			unregisterSettingsChangeListener()
			unregisterUserUnlockReceiver()
			unregisterConfigRefreshReceiver()
			renderController.onDestroy()
			super.onDestroy()
		}

		private fun updateParallaxTrackingState() {
			if (engineVisible && engineSurfaceAttached) {
				tiltParallaxTracker.start()
			} else {
				tiltParallaxTracker.stop()
			}
		}

		private fun maybeStartDaylightSyncCoordinator() {
			if (daylightSyncCoordinator != null) return
			if (!isUserUnlocked()) {
				if (!daylightSyncDeferredUntilUnlockLogged) {
					Logger.i(TAG, "daylight sync deferred until user unlock")
					daylightSyncDeferredUntilUnlockLogged = true
				}
				return
			}
			daylightSyncDeferredUntilUnlockLogged = false
			daylightSyncCoordinator = WallpaperDaylightSyncCoordinator(context = appContext).also { coordinator ->
				coordinator.setPreviewMode(isPreview)
				coordinator.onCreate()
				if (engineVisible) {
					coordinator.onVisibilityChanged(true)
				}
			}
			unregisterUserUnlockReceiver()
		}

		private fun maybeRestoreLockScreenWallpaperSharing() {
			if (isPreview) return
			if (settingsRepository.getRestoreLiveWallpaperOnLockScreen() != true) return
			clearLockWallpaperOverrideIfNeeded(appContext)
		}

		private fun registerSettingsChangeListener() {
			if (settingsChangeSubscription != null) return
			settingsChangeSubscription = settingsRepository.addChangeListener { snapshot ->
				renderController.setPerformanceMode(snapshot.performanceMode)
			}
		}

		private fun unregisterSettingsChangeListener() {
			settingsChangeSubscription?.let { subscription ->
				runCatching { subscription.close() }
			}
			settingsChangeSubscription = null
		}

		private fun refreshRenderSettings() {
			renderController.setPerformanceMode(settingsRepository.getPerformanceMode())
		}

		private fun isBatterySaver(): Boolean {
			return appContext.getSystemService(PowerManager::class.java)?.isPowerSaveMode == true
		}

		private fun reportBatterySaver() {
			CrashDiagnostics.setCustomKey("battery_saver", isBatterySaver())
		}

		private fun applyStoredConfig() {
			val config = resolveConfigForCurrentEngine()
			config?.let {
				val signature = buildConfigSignature(config = it, isPreview = isPreview)
				if (lastAppliedConfigSignature == signature) {
					return
				}
				lastAppliedConfigSignature = signature
				renderController.setConfig(it)
			} ?: run {
				Logger.w("SkyWallpaperService", "apply_stored_config skipped: no saved config")
			}
		}

		private fun applyUserUnlockedSettingsIfAvailable(): Boolean {
			if (!isUserUnlocked()) {
				if (!renderSettingsDeferredUntilUnlockLogged) {
					Logger.i(TAG, "render settings deferred until user unlock")
					renderSettingsDeferredUntilUnlockLogged = true
				}
				return false
			}
			renderSettingsDeferredUntilUnlockLogged = false
			registerSettingsChangeListener()
			refreshRenderSettings()
			return true
		}

		private fun resolveConfigForCurrentEngine() =
			when {
				isPreview -> configStore.loadPreview() ?: configStore.loadSelected() ?: DEFAULT_WALLPAPER_CONFIG
				else -> configStore.loadSelected() ?: configStore.loadPreview() ?: DEFAULT_WALLPAPER_CONFIG
			}

		private fun buildConfigSignature(
			config: WallpaperConfig,
			isPreview: Boolean
		): String {
			return "$isPreview|${config.hashCode()}"
		}

		private fun registerConfigRefreshReceiver() {
			if (configRefreshReceiverRegistered) return
			val filter = IntentFilter(ACTION_APPLY_STORED_WALLPAPER_CONFIG)
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				registerReceiver(configRefreshReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
			} else {
				@Suppress("DEPRECATION")
				registerReceiver(configRefreshReceiver, filter)
			}
			configRefreshReceiverRegistered = true
		}

		private fun unregisterConfigRefreshReceiver() {
			if (!configRefreshReceiverRegistered) return
			runCatching { unregisterReceiver(configRefreshReceiver) }
			configRefreshReceiverRegistered = false
		}

		private fun isUserUnlocked(): Boolean {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return true
			val userManager = getSystemService(UserManager::class.java) ?: return true
			return runCatching { userManager.isUserUnlocked }
				.getOrElse { throwable ->
					Logger.w(TAG, "isUserUnlocked check failed", throwable)
					true
				}
		}

		private fun registerUserUnlockReceiver() {
			if (userUnlockReceiverRegistered || isUserUnlocked()) return
			val filter = IntentFilter(Intent.ACTION_USER_UNLOCKED)
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				registerReceiver(userUnlockReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
			} else {
				@Suppress("DEPRECATION")
				registerReceiver(userUnlockReceiver, filter)
			}
			userUnlockReceiverRegistered = true
		}

		private fun unregisterUserUnlockReceiver() {
			if (!userUnlockReceiverRegistered) return
			runCatching { unregisterReceiver(userUnlockReceiver) }
			userUnlockReceiverRegistered = false
		}
	}

	companion object {
		private const val TAG = "SkyWallpaperService"
		private val DEFAULT_WALLPAPER_CONFIG = WallpaperConfig.default()
	}
}
