package com.example.lumisky.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.core.Logger
import com.example.core.api.SunLocation
import com.example.core.api.SunTimesRepository
import com.example.core.settings.AppSettingsDefaults
import com.example.core.settings.AppSettingsRepository
import com.example.core.settings.ManualCity
import java.util.concurrent.TimeUnit

internal fun buildBackupCityPrefetchCandidates(
	languageTag: String,
	manualCity: ManualCity,
	maxCandidateCount: Int = Int.MAX_VALUE
): List<SunLocation> {
	val defaultCity = AppSettingsDefaults.defaultCity(languageTag)
	val supportedCities = AppSettingsDefaults.supportedCities(languageTag).map { city ->
		SunLocation(
			label = city.name,
			latitude = city.latitude,
			longitude = city.longitude,
			timeZoneId = city.timeZoneId
		)
	}
	val manual = SunLocation(
		label = manualCity.name,
		latitude = manualCity.latitude,
		longitude = manualCity.longitude,
		timeZoneId = manualCity.timeZoneId
	)
	val default = SunLocation(
		label = defaultCity.name,
		latitude = defaultCity.latitude,
		longitude = defaultCity.longitude,
		timeZoneId = defaultCity.timeZoneId
	)
	val uniqueCandidates = buildList {
		add(default)
		add(manual)
		addAll(supportedCities)
	}.distinctBy { candidate ->
		"${candidate.latitude}|${candidate.longitude}|${candidate.timeZoneId.orEmpty()}"
	}
	return if (maxCandidateCount > 0) {
		uniqueCandidates.take(maxCandidateCount)
	} else {
		emptyList()
	}
}

class BackupCityPrefetchWorker(
	appContext: Context,
	workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

	override fun doWork(): Result {
		val maxCandidateCount = inputData.getInt(KEY_MAX_CANDIDATE_COUNT, Int.MAX_VALUE)
		val settings = AppSettingsRepository(applicationContext).snapshot()
		val candidates = buildBackupCityPrefetchCandidates(
			languageTag = settings.languageTag,
			manualCity = settings.manualCity,
			maxCandidateCount = maxCandidateCount
		)
		if (candidates.isEmpty()) {
			return Result.success()
		}

		val repository = SunTimesRepository()
		return try {
			Logger.i(
				TAG,
				"BACKUP_PREFETCH_START candidates=${candidates.size} thread=${Thread.currentThread().name}"
			)
			val refreshedCount = repository.prefetchBackupBlocking(
				candidates = candidates,
				minRefreshIntervalMs = BACKUP_CITY_REFRESH_INTERVAL_MS
			)
			Logger.i(
				TAG,
				"backup city prefetch completed refreshed=$refreshedCount candidates=${candidates.size}"
			)
			Result.success()
		} catch (throwable: Throwable) {
			Logger.w(TAG, "backup city prefetch failed", throwable)
			Result.retry()
		} finally {
			repository.release()
		}
	}

	companion object {
		private const val TAG = "BackupCityPrefetchWorker"
		private const val UNIQUE_WORK_NAME = "backup_city_prefetch_startup"
		private const val KEY_MAX_CANDIDATE_COUNT = "max_candidate_count"
		private const val BACKUP_CITY_REFRESH_INTERVAL_MS = 7L * 24L * 60L * 60L * 1000L
		private const val BACKUP_PREFETCH_INITIAL_DELAY_MS = 5_000L

		fun enqueue(
			context: Context,
			maxCandidateCount: Int
		) {
			val request = OneTimeWorkRequestBuilder<BackupCityPrefetchWorker>()
				.setConstraints(
					Constraints.Builder()
						.setRequiredNetworkType(NetworkType.CONNECTED)
						.setRequiresBatteryNotLow(true)
						.build()
				)
				.setInitialDelay(BACKUP_PREFETCH_INITIAL_DELAY_MS, TimeUnit.MILLISECONDS)
				.setInputData(
					workDataOf(KEY_MAX_CANDIDATE_COUNT to maxCandidateCount)
				)
				.build()
			WorkManager.getInstance(context.applicationContext)
				.enqueueUniqueWork(
					UNIQUE_WORK_NAME,
					ExistingWorkPolicy.KEEP,
					request
				)
		}
	}
}
