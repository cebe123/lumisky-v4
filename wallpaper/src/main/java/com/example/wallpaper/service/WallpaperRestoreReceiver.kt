package com.example.wallpaper.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.core.Logger
import com.example.core.settings.AppSettingsRepository
import com.example.engine.config.WallpaperConfigStore

class WallpaperRestoreReceiver : BroadcastReceiver() {

	override fun onReceive(context: Context, intent: Intent?) {
		val action = intent?.action ?: return
		if (action !in RESTORE_ACTIONS) return

		val appContext = context.applicationContext
		val storedConfig = WallpaperConfigStore(appContext).loadSelected()
		if (storedConfig == null) {
			Logger.d(TAG, "restore skipped for $action: no stored wallpaper config")
			return
		}

		runCatching {
			appContext.sendBroadcast(
				Intent(ACTION_APPLY_STORED_WALLPAPER_CONFIG)
					.setPackage(appContext.packageName)
			)
			restoreLockScreenSharingIfNeeded(
				appContext = appContext,
				action = action
			)
			Logger.i(TAG, "requested stored wallpaper restore for ${storedConfig.id} after $action")
		}.onFailure { throwable ->
			Logger.w(TAG, "failed to request stored wallpaper restore after $action", throwable)
		}
	}

	private fun restoreLockScreenSharingIfNeeded(
		appContext: Context,
		action: String
	) {
		if (!isLumiskyHomeWallpaperActive(appContext)) return
		if (action == Intent.ACTION_LOCKED_BOOT_COMPLETED) return

		val settingsRepository = AppSettingsRepository(appContext)
		val shouldRestore = when (settingsRepository.getRestoreLiveWallpaperOnLockScreen()) {
			true -> true
			false -> false
			null -> action == Intent.ACTION_MY_PACKAGE_REPLACED
		}
		if (!shouldRestore) return

		val restored = clearLockWallpaperOverrideIfNeeded(appContext)
		if (restored && settingsRepository.getRestoreLiveWallpaperOnLockScreen() == null) {
			settingsRepository.setRestoreLiveWallpaperOnLockScreen(true)
		}
	}

	companion object {
		private const val TAG = "WallpaperRestoreReceiver"
		private val RESTORE_ACTIONS = setOf(
			Intent.ACTION_LOCKED_BOOT_COMPLETED,
			Intent.ACTION_BOOT_COMPLETED,
			Intent.ACTION_MY_PACKAGE_REPLACED,
			Intent.ACTION_USER_UNLOCKED
		)
	}
}
