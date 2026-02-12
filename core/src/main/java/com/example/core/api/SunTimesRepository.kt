package com.example.core.api

import com.example.core.Logger
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

class SunTimesRepository(
	private val apiClient: SunTimesApiClient = SunTimesApiClient()
) {
	private val cached = AtomicReference<SunDaylight?>(null)

	fun currentOrFallback(): SunDaylight {
		return cached.get() ?: SunDaylight.fallback()
	}

	fun refreshAsync(
		latitude: Double,
		longitude: Double,
		onUpdated: (SunDaylight) -> Unit = {}
	) {
		thread(start = true, isDaemon = true, name = "SunTimesRefresh") {
			val fetched = apiClient.fetchDaylight(latitude, longitude) ?: return@thread
			cached.set(fetched)
			Logger.d(TAG, "Sun times updated: sunrise=${fetched.sunriseMinute} sunset=${fetched.sunsetMinute}")
			onUpdated(fetched)
		}
	}

	companion object {
		private const val TAG = "SunTimesRepository"
	}
}
