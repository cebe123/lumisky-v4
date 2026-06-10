package com.example.lumisky

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.UserManager
import androidx.work.Configuration
import com.example.lumisky.report.ErrorReporter

class LumiskyApplication : Application(), Configuration.Provider {
	private var unlockReceiver: BroadcastReceiver? = null

	override fun onCreate() {
		super.onCreate()
		ErrorReporter.installCrashCapture(applicationContext)
		installCrashlyticsDiagnosticsWhenUnlocked()
	}

	override val workManagerConfiguration: Configuration
		get() = Configuration.Builder().build()

	private fun installCrashlyticsDiagnosticsWhenUnlocked() {
		if (isUserUnlocked()) {
			ErrorReporter.installCrashlyticsDiagnostics(applicationContext)
			return
		}
		if (unlockReceiver != null) return
		unlockReceiver = object : BroadcastReceiver() {
			override fun onReceive(context: Context?, intent: Intent?) {
				if (intent?.action != Intent.ACTION_USER_UNLOCKED) return
				unregisterUnlockReceiver()
				ErrorReporter.installCrashlyticsDiagnostics(applicationContext)
			}
		}
		val filter = IntentFilter(Intent.ACTION_USER_UNLOCKED)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			registerReceiver(unlockReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
		} else {
			@Suppress("DEPRECATION")
			registerReceiver(unlockReceiver, filter)
		}
	}

	private fun unregisterUnlockReceiver() {
		unlockReceiver?.let { receiver ->
			runCatching { unregisterReceiver(receiver) }
		}
		unlockReceiver = null
	}

	private fun isUserUnlocked(): Boolean {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return true
		val userManager = getSystemService(UserManager::class.java) ?: return true
		return runCatching { userManager.isUserUnlocked }.getOrDefault(false)
	}
}
