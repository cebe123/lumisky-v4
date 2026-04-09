package com.example.wallpaper.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.UserManager
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.example.core.Logger
import com.example.core.settings.AppSettingsRepository
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
			displayRefreshRateProvider = {
				resolveWallpaperDisplayRefreshRateHz(appContext)
			}
		)
		private var daylightSyncCoordinator: WallpaperDaylightSyncCoordinator? = null
		private var configRefreshReceiverRegistered: Boolean = false
		private var userUnlockReceiverRegistered: Boolean = false
		private var daylightSyncDeferredUntilUnlockLogged: Boolean = false
		private var engineVisible: Boolean = false
		private var lastAppliedConfigSignature: String? = null
		private val configRefreshReceiver = object : BroadcastReceiver() {
			override fun onReceive(context: Context?, intent: Intent?) {
				if (intent?.action != ACTION_APPLY_STORED_WALLPAPER_CONFIG) return
				applyStoredConfig()
			}
		}
		private val userUnlockReceiver = object : BroadcastReceiver() {
			override fun onReceive(context: Context?, intent: Intent?) {
				if (intent?.action != Intent.ACTION_USER_UNLOCKED) return
				maybeStartDaylightSyncCoordinator()
				maybeRestoreLockScreenWallpaperSharing()
			}
		}

		override fun onCreate(surfaceHolder: SurfaceHolder) {
			super.onCreate(surfaceHolder)
			registerConfigRefreshReceiver()
			registerUserUnlockReceiver()
			renderController.setPreviewMode(isPreview)
			maybeStartDaylightSyncCoordinator()
			daylightSyncCoordinator?.setPreviewMode(isPreview)
			maybeRestoreLockScreenWallpaperSharing()
			applyStoredConfig()
			renderController.onCreate()
		}

		override fun onVisibilityChanged(visible: Boolean) {
			super.onVisibilityChanged(visible)
			engineVisible = visible
			renderController.setPreviewMode(isPreview)
			maybeStartDaylightSyncCoordinator()
			daylightSyncCoordinator?.setPreviewMode(isPreview)
			maybeRestoreLockScreenWallpaperSharing()
			if (visible) {
				applyStoredConfig()
			}
			daylightSyncCoordinator?.onVisibilityChanged(visible)
			renderController.onVisibilityChanged(visible)
		}

		override fun onSurfaceCreated(holder: SurfaceHolder) {
			super.onSurfaceCreated(holder)
			renderController.setPreviewMode(isPreview)
			maybeStartDaylightSyncCoordinator()
			daylightSyncCoordinator?.setPreviewMode(isPreview)
			maybeRestoreLockScreenWallpaperSharing()
			applyStoredConfig()
			renderController.onSurfaceCreated(holder)
		}

		override fun onSurfaceDestroyed(holder: SurfaceHolder) {
			renderController.onSurfaceDestroyed()
			super.onSurfaceDestroyed(holder)
		}

		override fun onDestroy() {
			daylightSyncCoordinator?.onDestroy()
			daylightSyncCoordinator = null
			unregisterUserUnlockReceiver()
			unregisterConfigRefreshReceiver()
			renderController.onDestroy()
			super.onDestroy()
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

		private fun applyStoredConfig() {
			val config = resolveConfigForCurrentEngine()
			config?.let {
				val signature = buildConfigSignature(
					id = it.id,
					isPreview = isPreview,
					sunrise = it.daylight.sunriseMinute,
					sunset = it.daylight.sunsetMinute,
					solarNoon = it.daylight.solarNoonMinute,
					timeZoneId = it.daylight.timeZoneId
				)
				if (lastAppliedConfigSignature == signature) {
					return
				}
				lastAppliedConfigSignature = signature
				renderController.setConfig(it)
			} ?: run {
				Logger.w("SkyWallpaperService", "apply_stored_config skipped: no saved config")
			}
		}

		private fun resolveConfigForCurrentEngine() =
			when {
				isPreview -> configStore.loadPreview() ?: configStore.loadSelected()
				else -> configStore.loadSelected()
			}

		private fun buildConfigSignature(
			id: String,
			isPreview: Boolean,
			sunrise: Int,
			sunset: Int,
			solarNoon: Int,
			timeZoneId: String?
		): String {
			return "$id|$isPreview|$sunrise|$sunset|$solarNoon|${timeZoneId.orEmpty()}"
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
	}
}
