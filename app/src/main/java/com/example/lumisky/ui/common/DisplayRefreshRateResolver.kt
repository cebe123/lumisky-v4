package com.example.lumisky.ui.common

import android.content.Context
import android.os.Build
import android.view.WindowManager

fun resolveDisplayRefreshRate(context: Context): Int {
	val refresh = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
		context.display?.refreshRate
	} else {
		val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
		@Suppress("DEPRECATION")
		windowManager?.defaultDisplay?.refreshRate
	} ?: DEFAULT_REFRESH_RATE.toFloat()
	return refresh.toInt().coerceIn(DEFAULT_REFRESH_RATE, MAX_REFRESH_RATE)
}

private const val DEFAULT_REFRESH_RATE = 60
private const val MAX_REFRESH_RATE = 120
