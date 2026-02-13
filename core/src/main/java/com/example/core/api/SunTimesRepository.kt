package com.example.core.api

import com.example.core.Logger
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

data class SunLocation(
	val label: String,
	val latitude: Double,
	val longitude: Double
)

class SunTimesRepository(
	private val apiClient: SunTimesApiClient = SunTimesApiClient()
) {
	private val cached = AtomicReference<SunDaylight?>(null)
	private val lastSuccessfulLocation = AtomicReference<SunLocation?>(null)

	fun currentOrFallback(): SunDaylight {
		return cached.get() ?: SunDaylight.fallback()
	}

	fun refreshAsync(
		latitude: Double,
		longitude: Double,
		onUpdated: (SunDaylight) -> Unit = {}
	) {
		refreshAsyncWithFallback(
			selectedCity = null,
			defaultCity = SunLocation(
				label = "direct",
				latitude = latitude,
				longitude = longitude
			),
			onUpdated = onUpdated
		)
	}

	fun refreshAsyncWithFallback(
		selectedCity: SunLocation?,
		defaultCity: SunLocation,
		onUpdated: (SunDaylight) -> Unit = {}
	) {
		thread(start = true, isDaemon = true, name = "SunTimesRefresh") {
			val candidates = buildCandidates(selectedCity, defaultCity)
			for (candidate in candidates) {
				val fetched = apiClient.fetchDaylight(candidate.latitude, candidate.longitude) ?: continue
				cached.set(fetched)
				lastSuccessfulLocation.set(candidate)
				Logger.d(
					TAG,
					"Sun times updated source=${candidate.label} sunrise=${fetched.sunriseMinute} sunset=${fetched.sunsetMinute}"
				)
				onUpdated(fetched)
				return@thread
			}
			onUpdated(currentOrFallback())
		}
	}

	private fun buildCandidates(
		selectedCity: SunLocation?,
		defaultCity: SunLocation
	): List<SunLocation> {
		return buildList {
			lastSuccessfulLocation.get()?.let { add(it) }
			selectedCity?.let { add(it) }
			add(defaultCity)
		}.distinctBy { candidate ->
			"${candidate.latitude}|${candidate.longitude}"
		}
	}

	companion object {
		private const val TAG = "SunTimesRepository"
	}
}
