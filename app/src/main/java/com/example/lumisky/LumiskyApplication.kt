package com.example.lumisky

import android.app.Application
import androidx.work.Configuration

class LumiskyApplication : Application(), Configuration.Provider {
	override val workManagerConfiguration: Configuration
		get() = Configuration.Builder().build()
}
