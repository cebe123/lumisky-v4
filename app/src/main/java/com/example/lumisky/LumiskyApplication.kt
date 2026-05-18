package com.example.lumisky

import android.app.Application
import androidx.work.Configuration
import com.example.lumisky.report.ErrorReporter

class LumiskyApplication : Application(), Configuration.Provider {
	override fun onCreate() {
		super.onCreate()
		ErrorReporter.installCrashlyticsDiagnostics(applicationContext)
		ErrorReporter.installCrashCapture(applicationContext)
	}

	override val workManagerConfiguration: Configuration
		get() = Configuration.Builder().build()
}
