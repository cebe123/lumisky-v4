package com.example.core.api

import android.os.SystemClock
import com.example.core.Logger
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.roundToInt

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
	private val lastRefreshElapsedMs = AtomicLong(0L)
	private val lastRefreshLocationKey = AtomicReference<String?>(null)
	private val refreshExecutor = Executors.newSingleThreadExecutor()
	private val requestVersion = AtomicInteger(0)

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
		val candidates = buildList {
			selectedCity?.let { add(it) }
			add(defaultCity)
		}
		refreshAsyncWithCandidates(candidates, onUpdated)
	}

	fun refreshAsyncWithCandidates(
		candidates: List<SunLocation>,
		onUpdated: (SunDaylight) -> Unit = {}
	) {
		if (candidates.isEmpty()) {
			onUpdated(currentOrFallback())
			return
		}
		val preferredLocationKey = toLocationKey(candidates.first())
		val cachedDaylight = cached.get()
		val elapsedSinceRefresh = SystemClock.elapsedRealtime() - lastRefreshElapsedMs.get()
		if (
			cachedDaylight != null &&
			preferredLocationKey == lastRefreshLocationKey.get() &&
			elapsedSinceRefresh in 0 until MIN_REFRESH_INTERVAL_MS
		) {
			onUpdated(cachedDaylight)
			return
		}

		val requestId = requestVersion.incrementAndGet()
		refreshExecutor.execute {
			val orderedCandidates = buildCandidates(candidates)
			for (candidate in orderedCandidates) {
				if (requestId != requestVersion.get()) return@execute
				val fetched = apiClient.fetchDaylight(candidate.latitude, candidate.longitude) ?: continue
				lastRefreshElapsedMs.set(SystemClock.elapsedRealtime())
				lastRefreshLocationKey.set(toLocationKey(candidate))
				cached.set(fetched)
				lastSuccessfulLocation.set(candidate)
				Logger.d(
					TAG,
					"Sun times updated source=${candidate.label} sunrise=${fetched.sunriseMinute} sunset=${fetched.sunsetMinute}"
				)
				onUpdated(fetched)
				return@execute
			}
			if (requestId != requestVersion.get()) return@execute
			onUpdated(currentOrFallback())
		}
	}

	private fun buildCandidates(
		inputCandidates: List<SunLocation>
	): List<SunLocation> {
		return buildList {
			lastSuccessfulLocation.get()?.let { add(it) }
			addAll(inputCandidates)
		}.distinctBy { candidate ->
			"${candidate.latitude}|${candidate.longitude}"
		}
	}

	fun release() {
		requestVersion.incrementAndGet()
		refreshExecutor.shutdownNow()
	}

	private fun toLocationKey(location: SunLocation): String {
		val latitudeBucket = (location.latitude * LOCATION_BUCKET_SCALE).roundToInt()
		val longitudeBucket = (location.longitude * LOCATION_BUCKET_SCALE).roundToInt()
		return "$latitudeBucket|$longitudeBucket"
	}

	companion object {
		private const val TAG = "SunTimesRepository"
		private const val MIN_REFRESH_INTERVAL_MS = 5 * 60_000L
		private const val LOCATION_BUCKET_SCALE = 100.0
	}
}
