package com.example.wallpaper.service

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display

internal fun resolveWallpaperDisplayRefreshRateHz(context: Context): Int {
	val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
	val refreshRate = displayManager
		?.getDisplay(Display.DEFAULT_DISPLAY)
		?.refreshRate
		?: DEFAULT_REFRESH_RATE_HZ.toFloat()
	return refreshRate.toInt().coerceIn(DEFAULT_REFRESH_RATE_HZ, MAX_REFRESH_RATE_HZ)
}

private const val DEFAULT_REFRESH_RATE_HZ = 60
private const val MAX_REFRESH_RATE_HZ = 120
