package com.example.wallpaper.service

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import com.example.core.Logger

private const val TAG = "WallpaperSelectionState"

fun queryLockWallpaperId(context: Context): Int? {
	val appContext = context.applicationContext
	return runCatching {
		WallpaperManager.getInstance(appContext).getWallpaperId(WallpaperManager.FLAG_LOCK)
	}.onFailure { throwable ->
		Logger.w(TAG, "queryLockWallpaperId failed", throwable)
	}.getOrNull()
}

fun isLumiskyHomeWallpaperActive(context: Context): Boolean {
	val appContext = context.applicationContext
	val expectedComponent = ComponentName(appContext, com.example.wallpaper.SkyWallpaperService::class.java)
	return runCatching {
		val wallpaperInfo = WallpaperManager.getInstance(appContext).getWallpaperInfo()
		if (wallpaperInfo == null) {
			Logger.d(TAG, "isLumiskyHomeWallpaperActive: wallpaperInfo unavailable")
		}
		wallpaperInfo != null &&
			wallpaperInfo.packageName == expectedComponent.packageName &&
			wallpaperInfo.serviceName == expectedComponent.className
	}.onFailure { throwable ->
		Logger.w(TAG, "isLumiskyHomeWallpaperActive failed", throwable)
	}.getOrDefault(false)
}

fun clearLockWallpaperOverrideIfNeeded(context: Context): Boolean {
	val appContext = context.applicationContext
	val lockWallpaperId = queryLockWallpaperId(appContext) ?: return false
	if (lockWallpaperId < 0) return false
	return runCatching {
		WallpaperManager.getInstance(appContext).clear(WallpaperManager.FLAG_LOCK)
		Logger.i(TAG, "cleared lock wallpaper override to share live wallpaper")
		true
	}.onFailure { throwable ->
		Logger.w(TAG, "clearLockWallpaperOverrideIfNeeded failed", throwable)
	}.getOrDefault(false)
}
