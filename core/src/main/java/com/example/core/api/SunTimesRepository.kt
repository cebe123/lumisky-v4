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
	val longitude: Double,
	val timeZoneId: String? = null
)

data class SunDaylightResolution(
	val daylight: SunDaylight,
	val sourceLocation: SunLocation?
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
	val candidate: SunLocation
)

private enum class CacheLayer {
	ACTIVE,
	BACKUP
}

class SunTimesRepository(
	private val apiClient: SunTimesApiClient = SunTimesApiClient(),
	private val nowProvider: () -> Long = System::currentTimeMillis
) {
	private val cachedEntry = AtomicReference<CachedDaylightEntry?>(null)
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
				longitude = longitude,
				timeZoneId = ZoneId.systemDefault().id
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
		refreshResolvedAsyncWithCandidates(
			candidates = candidates,
			forceRefresh = forceRefresh
		) { resolution ->
			onUpdated(resolution.daylight)
		}
	}

	fun refreshResolvedAsyncWithCandidates(
		candidates: List<SunLocation>,
		forceRefresh: Boolean = false,
		onUpdated: (SunDaylightResolution) -> Unit = {}
	) {
		if (candidates.isEmpty()) {
			val (fallback, source) = resolveFallback()
			Logger.w(
				TAG,
				"Sun times decision=NO_CANDIDATE at=${toHourMinuteLabel(System.currentTimeMillis())} source=$source " +
					"sunrise=${fallback.sunriseMinute}(${toClockLabel(fallback.sunriseMinute)}) " +
					"sunset=${fallback.sunsetMinute}(${toClockLabel(fallback.sunsetMinute)})"
			)
			onUpdated(SunDaylightResolution(daylight = fallback, sourceLocation = null))
			return
		}

		val orderedCandidates = buildCandidates(candidates)
		if (!forceRefresh) {
			val cacheHit = findDailyHitByCandidatePriority(
				candidates = orderedCandidates,
				epochMillis = nowProvider()
			)
			if (cacheHit != null) {
				cacheResolvedEntry(cacheHit.entry)
				onUpdated(
					SunDaylightResolution(
						daylight = cacheHit.entry.daylight,
						sourceLocation = cacheHit.candidate
					)
				)
				return
			}
		}

		val requestId = requestVersion.incrementAndGet()
		refreshExecutor.execute {
			if (requestId != requestVersion.get()) return@execute

			val latestCandidates = buildCandidates(candidates)
			if (!forceRefresh) {
				val cacheHit = findDailyHitByCandidatePriority(
					candidates = latestCandidates,
					epochMillis = nowProvider()
				)
				if (cacheHit != null) {
					cacheResolvedEntry(cacheHit.entry)
					onUpdated(
						SunDaylightResolution(
							daylight = cacheHit.entry.daylight,
							sourceLocation = cacheHit.candidate
						)
					)
					return@execute
				}
			}

			for (candidate in latestCandidates) {
				if (requestId != requestVersion.get()) return@execute
				val fetched = apiClient.fetchDaylight(
					latitude = candidate.latitude,
					longitude = candidate.longitude,
					timeZoneId = candidate.timeZoneId
				)
				if (fetched == null) {
					Logger.w(
						TAG,
						"Sun times fetch failed source=${candidate.label} at=${toHourMinuteLabel(System.currentTimeMillis())}"
					)
					continue
				}

				val entry = storeSuccessfulFetch(candidate, fetched, CacheLayer.ACTIVE)
				cacheResolvedEntry(entry)
				onUpdated(
					SunDaylightResolution(
						daylight = fetched,
						sourceLocation = candidate
					)
				)
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
			onUpdated(SunDaylightResolution(daylight = fallback, sourceLocation = null))
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
		if (dueCandidates.isEmpty()) return
		backupPrefetchExecutor.execute {
			dueCandidates.forEach { candidate ->
				val attemptAt = System.currentTimeMillis()
				if (!shouldRefreshBackup(candidate, attemptAt, minRefreshIntervalMs)) {
					return@forEach
				}
				val fetched = apiClient.fetchDaylight(
					latitude = candidate.latitude,
					longitude = candidate.longitude,
					timeZoneId = candidate.timeZoneId
				)
				if (fetched == null) {
					Logger.w(
						TAG,
						"Sun times backup fetch failed source=${candidate.label} at=${toHourMinuteLabel(System.currentTimeMillis())}"
					)
					return@forEach
				}
				val entry = storeSuccessfulFetch(candidate, fetched, CacheLayer.BACKUP)
				markBackupRefresh(candidate, entry.updatedAtWallClockMs)
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
		epochMillis: Long
	): CacheHit? {
		synchronized(cacheLock) {
			for (candidate in candidates) {
				val locationKey = toLocationKey(candidate)
				val dayKey = currentDayKey(
					timeZoneId = candidate.timeZoneId,
					epochMillis = epochMillis
				)
				val key = toDailyCacheKey(locationKey, dayKey)
				sharedDailyCache[key]?.let { return CacheHit(it, candidate) }
				sharedBackupDailyCache[key]?.let { return CacheHit(it, candidate) }
			}
		}
		return null
	}

	private fun storeSuccessfulFetch(
		candidate: SunLocation,
		daylight: SunDaylight,
		layer: CacheLayer
	): CachedDaylightEntry {
		val nowMs = nowProvider()
		val effectiveTimeZoneId = daylight.timeZoneId ?: candidate.timeZoneId
		val effectiveLocation = candidate.copy(timeZoneId = effectiveTimeZoneId)
		val locationKey = toLocationKey(effectiveLocation)
		val dayKey = currentDayKey(
			timeZoneId = effectiveTimeZoneId,
			epochMillis = nowMs
		)
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
			cachedEntry.set(entry)
		}
		return entry
	}

	private fun resolveFallback(
		preferredCandidates: List<SunLocation> = emptyList()
	): Pair<SunDaylight, String> {
		val nowMs = nowProvider()
		val preferredLocationKeys = preferredCandidates
			.map { candidate -> toLocationKey(candidate) }
			.toSet()
		cachedEntry.get()?.let { entry ->
			val matchesPreferred = preferredLocationKeys.isEmpty() ||
				preferredLocationKeys.contains(entry.locationKey)
			if (matchesPreferred && isEntryCurrent(entry, nowMs)) {
				return entry.daylight to "instance_cache:${entry.sourceLabel}"
			}
		}
		synchronized(cacheLock) {
			for (candidate in preferredCandidates) {
				val locationKey = toLocationKey(candidate)
				sharedLocationCache[locationKey]?.takeIf { entry ->
					isEntryCurrentForCandidate(entry, candidate, nowMs)
				}?.let {
					return it.daylight to "location_cache:${candidate.label}"
				}
				sharedBackupLocationCache[locationKey]?.takeIf { entry ->
					isEntryCurrentForCandidate(entry, candidate, nowMs)
				}?.let {
					return it.daylight to "backup_location_cache:${candidate.label}"
				}
			}
			if (preferredCandidates.isEmpty()) {
				sharedLocationCache.values.firstOrNull { entry ->
					isEntryCurrent(entry, nowMs)
				}?.let { entry ->
					return entry.daylight to "location_cache:${entry.sourceLabel}"
				}
				sharedBackupLocationCache.values.firstOrNull { entry ->
					isEntryCurrent(entry, nowMs)
				}?.let { entry ->
					return entry.daylight to "backup_location_cache:${entry.sourceLabel}"
				}
			}
		}
		return SunDaylight.fallback() to "default_fallback"
	}

	private fun buildCandidates(
		inputCandidates: List<SunLocation>
	): List<SunLocation> {
		return inputCandidates.distinctBy { candidate ->
			"${candidate.latitude}|${candidate.longitude}|${candidate.timeZoneId.orEmpty()}"
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

	private fun cacheResolvedEntry(entry: CachedDaylightEntry) {
		cachedEntry.set(entry)
	}

	private fun isEntryCurrent(
		entry: CachedDaylightEntry,
		epochMillis: Long
	): Boolean {
		return entry.dayKey == currentDayKey(
			timeZoneId = entry.daylight.timeZoneId,
			epochMillis = epochMillis
		)
	}

	private fun isEntryCurrentForCandidate(
		entry: CachedDaylightEntry,
		candidate: SunLocation,
		epochMillis: Long
	): Boolean {
		return entry.dayKey == currentDayKey(
			timeZoneId = candidate.timeZoneId ?: entry.daylight.timeZoneId,
			epochMillis = epochMillis
		)
	}

	private fun currentDayKey(
		timeZoneId: String?,
		epochMillis: Long = nowProvider()
	): String {
		return LocalDate.ofInstant(
			Instant.ofEpochMilli(epochMillis),
			resolveZoneId(timeZoneId)
		).toString()
	}

	private fun toDailyCacheKey(locationKey: String, dayKey: String): String {
		return "$locationKey|$dayKey"
	}

	private fun toLocationKey(location: SunLocation): String {
		val latitudeBucket = (location.latitude * LOCATION_BUCKET_SCALE).roundToInt()
		val longitudeBucket = (location.longitude * LOCATION_BUCKET_SCALE).roundToInt()
		val normalizedTimeZone = normalizeTimeZoneId(location.timeZoneId)
		return "$latitudeBucket|$longitudeBucket|$normalizedTimeZone"
	}

	private fun resolveZoneId(timeZoneId: String?): ZoneId {
		return normalizeTimeZoneId(timeZoneId)
			.takeIf { it.isNotBlank() }
			?.let { normalized -> runCatching { ZoneId.of(normalized) }.getOrNull() }
			?: ZoneId.systemDefault()
	}

	private fun normalizeTimeZoneId(timeZoneId: String?): String {
		val normalized = timeZoneId?.trim().orEmpty()
		if (normalized.isBlank()) return ""
		return runCatching { ZoneId.of(normalized).id }.getOrDefault(normalized)
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
