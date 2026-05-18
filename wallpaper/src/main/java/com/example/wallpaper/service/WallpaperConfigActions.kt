package com.example.wallpaper.service

/**
 * Action broadcast when the stored wallpaper configuration has changed.
 * This is listened to by [SkyWallpaperService] to dynamically update the active wallpaper,
 * and may be broadcast by other apps or modules configuring the wallpaper.
 */
@Suppress("unused")
const val ACTION_APPLY_STORED_WALLPAPER_CONFIG =
	"com.example.lumisky.action.APPLY_STORED_WALLPAPER_CONFIG"