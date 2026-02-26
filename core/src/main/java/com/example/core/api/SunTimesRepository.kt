package com.example.core.api

import com.example.core.Logger
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.LinkedHashMap
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.roundToInt

data class SunLocation(
	val label: String,
	val latitude: Double,
	val longitude: Double
)

private data class CachedDaylightEntry(
	val daylight: SunDaylight,
	val locationKey: String,
	val sourceLabel: String,
	val dayKey: String,
	val updatedAtWallClockMs: Long
)

private data class CacheHit(
	val entry: CachedDaylightEntry,
	val layer: CacheLayer
)

private enum class CacheLayer {
	ACTIVE,
	BACKUP
}

class SunTimesRepository(
	private val apiClient: SunTimesApiClient = SunTimesApiClient()
) {
	private val cached = AtomicReference<SunDaylight?>(null)
	private val lastSuccessfulLocation = AtomicReference<SunLocation?>(null)
	private val refreshExecutor = Executors.newSingleThreadExecutor()
	private val backupPrefetchExecutor = Executors.newSingleThreadExecutor()
	private val requestVersion = AtomicInteger(0)

	fun currentOrFallback(): SunDaylight {
		return resolveFallback().first
	}

	fun refreshAsync(
		latitude: Double,
		longitude: Double,
		forceRefresh: Boolean = false,
		onUpdated: (SunDaylight) -> Unit = {}
	) {
		refreshAsyncWithFallback(
			selectedCity = null,
			defaultCity = SunLocation(
				label = "direct",
				latitude = latitude,
				longitude = longitude
			),
			forceRefresh = forceRefresh,
			onUpdated = onUpdated
		)
	}

	fun refreshAsyncWithFallback(
		selectedCity: SunLocation?,
		defaultCity: SunLocation,
		forceRefresh: Boolean = false,
		onUpdated: (SunDaylight) -> Unit = {}
	) {
		val candidates = buildList {
			selectedCity?.let { add(it) }
			add(defaultCity)
		}
		refreshAsyncWithCandidates(
			candidates = candidates,
			forceRefresh = forceRefresh,
			onUpdated = onUpdated
		)
	}

	fun refreshAsyncWithCandidates(
		candidates: List<SunLocation>,
		forceRefresh: Boolean = false,
		onUpdated: (SunDaylight) -> Unit = {}
	) {
		if (candidates.isEmpty()) {
			val (fallback, source) = resolveFallback()
			Logger.w(
				TAG,
				"Sun times decision=NO_CANDIDATE at=${toHourMinuteLabel(System.currentTimeMillis())} source=$source " +
					"sunrise=${fallback.sunriseMinute}(${toClockLabel(fallback.sunriseMinute)}) " +
					"sunset=${fallback.sunsetMinute}(${toClockLabel(fallback.sunsetMinute)})"
			)
			onUpdated(fallback)
			return
		}

		val orderedCandidates = buildCandidates(candidates)
		if (!forceRefresh) {
			val dayKey = currentDayKey()
			val cacheHit = findDailyHitByCandidatePriority(orderedCandidates, dayKey)
			if (cacheHit != null) {
				if (cacheHit.layer == CacheLayer.ACTIVE) {
					cached.set(cacheHit.entry.daylight)
				}
				val decision = if (cacheHit.layer == CacheLayer.ACTIVE) "HIT" else "HIT_BACKUP"
				Logger.d(
					TAG,
					"Sun times decision=$decision at=${toHourMinuteLabel(System.currentTimeMillis())} " +
						"day=$dayKey layer=${cacheHit.layer} source=${cacheHit.entry.sourceLabel} " +
						"sunrise=${cacheHit.entry.daylight.sunriseMinute}(${toClockLabel(cacheHit.entry.daylight.sunriseMinute)}) " +
						"sunset=${cacheHit.entry.daylight.sunsetMinute}(${toClockLabel(cacheHit.entry.daylight.sunsetMinute)})"
				)
				onUpdated(cacheHit.entry.daylight)
				return
			}
		}

		val requestId = requestVersion.incrementAndGet()
		refreshExecutor.execute {
			if (requestId != requestVersion.get()) return@execute

			val latestCandidates = buildCandidates(candidates)
			if (!forceRefresh) {
				val dayKey = currentDayKey()
				val cacheHit = findDailyHitByCandidatePriority(latestCandidates, dayKey)
				if (cacheHit != null) {
					if (cacheHit.layer == CacheLayer.ACTIVE) {
						cached.set(cacheHit.entry.daylight)
					}
					val decision = if (cacheHit.layer == CacheLayer.ACTIVE) {
						"HIT_POST_QUEUE"
					} else {
						"HIT_BACKUP_POST_QUEUE"
					}
					Logger.d(
						TAG,
						"Sun times decision=$decision at=${toHourMinuteLabel(System.currentTimeMillis())} " +
							"day=$dayKey layer=${cacheHit.layer} source=${cacheHit.entry.sourceLabel} " +
							"sunrise=${cacheHit.entry.daylight.sunriseMinute}(${toClockLabel(cacheHit.entry.daylight.sunriseMinute)}) " +
							"sunset=${cacheHit.entry.daylight.sunsetMinute}(${toClockLabel(cacheHit.entry.daylight.sunsetMinute)})"
					)
					onUpdated(cacheHit.entry.daylight)
					return@execute
				}
			}

			Logger.d(
				TAG,
				"Sun times decision=FETCH_START at=${toHourMinuteLabel(System.currentTimeMillis())} " +
					"forceRefresh=$forceRefresh candidates=${latestCandidates.size} day=${currentDayKey()}"
			)

			for (candidate in latestCandidates) {
				if (requestId != requestVersion.get()) return@execute
				Logger.d(
					TAG,
					"Sun times fetch attempt source=${candidate.label} " +
						"lat=${candidate.latitude} lon=${candidate.longitude}"
				)
				val fetched = apiClient.fetchDaylight(candidate.latitude, candidate.longitude)
				if (fetched == null) {
					Logger.w(
						TAG,
						"Sun times fetch failed source=${candidate.label} at=${toHourMinuteLabel(System.currentTimeMillis())}"
					)
					continue
				}

				val entry = storeSuccessfulFetch(candidate, fetched, CacheLayer.ACTIVE)
				Logger.d(
					TAG,
					"Sun times decision=FETCH_SUCCESS at=${toHourMinuteLabel(entry.updatedAtWallClockMs)} " +
						"day=${entry.dayKey} source=${entry.sourceLabel} " +
						"sunrise=${fetched.sunriseMinute}(${toClockLabel(fetched.sunriseMinute)}) " +
						"sunset=${fetched.sunsetMinute}(${toClockLabel(fetched.sunsetMinute)})"
				)
				onUpdated(fetched)
				return@execute
			}

			if (requestId != requestVersion.get()) return@execute
			val (fallback, source) = resolveFallback(latestCandidates)
			Logger.w(
				TAG,
				"Sun times decision=FAILBACK at=${toHourMinuteLabel(System.currentTimeMillis())} source=$source " +
					"sunrise=${fallback.sunriseMinute}(${toClockLabel(fallback.sunriseMinute)}) " +
					"sunset=${fallback.sunsetMinute}(${toClockLabel(fallback.sunsetMinute)})"
			)
			onUpdated(fallback)
		}
	}

	fun prefetchBackupAsync(
		candidates: List<SunLocation>,
		minRefreshIntervalMs: Long = DEFAULT_BACKUP_REFRESH_INTERVAL_MS
	) {
		val uniqueCandidates = candidates
			.distinctBy { "${it.latitude}|${it.longitude}" }
		if (uniqueCandidates.isEmpty()) return

		val nowMs = System.currentTimeMillis()
		val dueCandidates = uniqueCandidates.filter { candidate ->
			shouldRefreshBackup(candidate, nowMs, minRefreshIntervalMs)
		}
		if (dueCandidates.isEmpty()) {
			Logger.d(
				TAG,
				"Sun times decision=BACKUP_PREFETCH_SKIP at=${toHourMinuteLabel(nowMs)} " +
					"reason=all_fresh candidates=${uniqueCandidates.size} intervalMs=$minRefreshIntervalMs"
			)
			return
		}

		Logger.d(
			TAG,
			"Sun times decision=BACKUP_PREFETCH_START at=${toHourMinuteLabel(nowMs)} " +
				"candidates=${uniqueCandidates.size} due=${dueCandidates.size} intervalMs=$minRefreshIntervalMs"
		)
		backupPrefetchExecutor.execute {
			dueCandidates.forEach { candidate ->
				val attemptAt = System.currentTimeMillis()
				if (!shouldRefreshBackup(candidate, attemptAt, minRefreshIntervalMs)) {
					return@forEach
				}
				Logger.d(
					TAG,
					"Sun times backup fetch attempt source=${candidate.label} " +
						"lat=${candidate.latitude} lon=${candidate.longitude}"
				)
				val fetched = apiClient.fetchDaylight(candidate.latitude, candidate.longitude)
				if (fetched == null) {
					Logger.w(
						TAG,
						"Sun times backup fetch failed source=${candidate.label} at=${toHourMinuteLabel(System.currentTimeMillis())}"
					)
					return@forEach
				}
				val entry = storeSuccessfulFetch(candidate, fetched, CacheLayer.BACKUP)
				markBackupRefresh(candidate, entry.updatedAtWallClockMs)
				Logger.d(
					TAG,
					"Sun times decision=BACKUP_PREFETCH_SUCCESS at=${toHourMinuteLabel(entry.updatedAtWallClockMs)} " +
						"day=${entry.dayKey} source=${entry.sourceLabel} " +
						"sunrise=${fetched.sunriseMinute}(${toClockLabel(fetched.sunriseMinute)}) " +
						"sunset=${fetched.sunsetMinute}(${toClockLabel(fetched.sunsetMinute)})"
				)
			}
		}
	}

	fun release() {
		requestVersion.incrementAndGet()
		refreshExecutor.shutdownNow()
		backupPrefetchExecutor.shutdownNow()
	}

	private fun findDailyHitByCandidatePriority(
		candidates: List<SunLocation>,
		dayKey: String
	): CacheHit? {
		synchronized(cacheLock) {
			for (candidate in candidates) {
				val locationKey = toLocationKey(candidate)
				val key = toDailyCacheKey(locationKey, dayKey)
				sharedDailyCache[key]?.let { return CacheHit(it, CacheLayer.ACTIVE) }
				sharedBackupDailyCache[key]?.let { return CacheHit(it, CacheLayer.BACKUP) }
			}
		}
		return null
	}

	private fun storeSuccessfulFetch(
		candidate: SunLocation,
		daylight: SunDaylight,
		layer: CacheLayer
	): CachedDaylightEntry {
		val nowMs = System.currentTimeMillis()
		val locationKey = toLocationKey(candidate)
		val dayKey = currentDayKey(nowMs)
		val entry = CachedDaylightEntry(
			daylight = daylight,
			locationKey = locationKey,
			sourceLabel = candidate.label,
			dayKey = dayKey,
			updatedAtWallClockMs = nowMs
		)
		synchronized(cacheLock) {
			when (layer) {
				CacheLayer.ACTIVE -> {
					sharedDailyCache[toDailyCacheKey(locationKey, dayKey)] = entry
					sharedLocationCache[locationKey] = entry
				}
				CacheLayer.BACKUP -> {
					sharedBackupDailyCache[toDailyCacheKey(locationKey, dayKey)] = entry
					sharedBackupLocationCache[locationKey] = entry
				}
			}
			trimCacheLocked()
		}
		if (layer == CacheLayer.ACTIVE) {
			cached.set(daylight)
			lastSuccessfulLocation.set(candidate)
		}
		return entry
	}

	private fun resolveFallback(
		preferredCandidates: List<SunLocation> = emptyList()
	): Pair<SunDaylight, String> {
		val preferredLocationKeys = preferredCandidates
			.map { candidate -> toLocationKey(candidate) }
			.toSet()
		cached.get()?.let { daylight ->
			val location = lastSuccessfulLocation.get()
			if (location == null) {
				if (preferredLocationKeys.isEmpty()) return daylight to "instance_cache"
			} else {
				val cachedLocationKey = toLocationKey(location)
				if (preferredLocationKeys.isEmpty() || preferredLocationKeys.contains(cachedLocationKey)) {
					return daylight to "instance_cache:${location.label}"
				}
			}
		}
		synchronized(cacheLock) {
			for (candidate in preferredCandidates) {
				val locationKey = toLocationKey(candidate)
				sharedLocationCache[locationKey]?.let {
					return it.daylight to "location_cache:${candidate.label}"
				}
				sharedBackupLocationCache[locationKey]?.let {
					return it.daylight to "backup_location_cache:${candidate.label}"
				}
			}
		}
		return SunDaylight.fallback() to "default_fallback"
	}

	private fun buildCandidates(
		inputCandidates: List<SunLocation>
	): List<SunLocation> {
		return inputCandidates.distinctBy { candidate ->
			"${candidate.latitude}|${candidate.longitude}"
		}
	}

	private fun trimCacheLocked() {
		while (sharedDailyCache.size > MAX_DAILY_CACHE_ENTRIES) {
			val iterator = sharedDailyCache.entries.iterator()
			if (!iterator.hasNext()) break
			iterator.next()
			iterator.remove()
		}
		while (sharedLocationCache.size > MAX_LOCATION_CACHE_ENTRIES) {
			val iterator = sharedLocationCache.entries.iterator()
			if (!iterator.hasNext()) break
			iterator.next()
			iterator.remove()
		}
		while (sharedBackupDailyCache.size > MAX_DAILY_CACHE_ENTRIES) {
			val iterator = sharedBackupDailyCache.entries.iterator()
			if (!iterator.hasNext()) break
			iterator.next()
			iterator.remove()
		}
		while (sharedBackupLocationCache.size > MAX_LOCATION_CACHE_ENTRIES) {
			val iterator = sharedBackupLocationCache.entries.iterator()
			if (!iterator.hasNext()) break
			iterator.next()
			iterator.remove()
		}
		while (sharedBackupRefreshCache.size > MAX_BACKUP_REFRESH_ENTRIES) {
			val iterator = sharedBackupRefreshCache.entries.iterator()
			if (!iterator.hasNext()) break
			iterator.next()
			iterator.remove()
		}
	}

	private fun shouldRefreshBackup(
		candidate: SunLocation,
		nowMs: Long,
		minRefreshIntervalMs: Long
	): Boolean {
		val locationKey = toLocationKey(candidate)
		synchronized(cacheLock) {
			val lastRefreshAt = sharedBackupRefreshCache[locationKey] ?: return true
			return (nowMs - lastRefreshAt) >= minRefreshIntervalMs
		}
	}

	private fun markBackupRefresh(
		candidate: SunLocation,
		refreshedAtMs: Long
	) {
		val locationKey = toLocationKey(candidate)
		synchronized(cacheLock) {
			sharedBackupRefreshCache[locationKey] = refreshedAtMs
			trimCacheLocked()
		}
	}

	private fun currentDayKey(epochMillis: Long = System.currentTimeMillis()): String {
		return LocalDate.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault()).toString()
	}

	private fun toDailyCacheKey(locationKey: String, dayKey: String): String {
		return "$locationKey|$dayKey"
	}

	private fun toLocationKey(location: SunLocation): String {
		val latitudeBucket = (location.latitude * LOCATION_BUCKET_SCALE).roundToInt()
		val longitudeBucket = (location.longitude * LOCATION_BUCKET_SCALE).roundToInt()
		return "$latitudeBucket|$longitudeBucket"
	}

	private fun toClockLabel(minute: Int): String {
		val normalized = minute.coerceIn(0, (24 * 60) - 1)
		val hours = normalized / 60
		val minutes = normalized % 60
		return String.format(Locale.US, "%02d:%02d", hours, minutes)
	}

	private fun toHourMinuteLabel(epochMillis: Long): String {
		if (epochMillis <= 0L) return "--:--"
		val local = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault())
		return String.format(Locale.US, "%02d:%02d", local.hour, local.minute)
	}

	companion object {
		private const val TAG = "SunTimesRepository"
		private const val LOCATION_BUCKET_SCALE = 1000.0
		private const val MAX_DAILY_CACHE_ENTRIES = 96
		private const val MAX_LOCATION_CACHE_ENTRIES = 48
		private const val MAX_BACKUP_REFRESH_ENTRIES = 96
		private const val DEFAULT_BACKUP_REFRESH_INTERVAL_MS = 7L * 24L * 60L * 60L * 1000L
		private val cacheLock = Any()
		private val sharedDailyCache = LinkedHashMap<String, CachedDaylightEntry>(64, 0.75f, true)
		private val sharedLocationCache = LinkedHashMap<String, CachedDaylightEntry>(32, 0.75f, true)
		private val sharedBackupDailyCache = LinkedHashMap<String, CachedDaylightEntry>(64, 0.75f, true)
		private val sharedBackupLocationCache = LinkedHashMap<String, CachedDaylightEntry>(32, 0.75f, true)
		private val sharedBackupRefreshCache = LinkedHashMap<String, Long>(64, 0.75f, true)
	}
}
