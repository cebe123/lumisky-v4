package com.example.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.example.core.Logger
import com.example.core.api.SunLocation
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.time.ZoneId
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class LastKnownLocationProvider(
	context: Context
) {
	private val appContext = context.applicationContext
	private val fusedLocationClient: FusedLocationProviderClient =
		LocationServices.getFusedLocationProviderClient(appContext)
	private val geocoderLock = Any()
	private val passiveListenerLock = Any()
	private val localityCache = object : LinkedHashMap<String, String>(32, 0.75f, true) {
		override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean {
			return size > MAX_LOCALITY_CACHE_SIZE
		}
	}
	private var passiveLocationCallback: LocationCallback? = null

	fun hasLocationPermission(): Boolean {
		return getLocationAccessLevel() != LocationAccessLevel.NONE
	}

	fun hasPreciseLocationPermission(): Boolean {
		return ContextCompat.checkSelfPermission(
			appContext,
			Manifest.permission.ACCESS_FINE_LOCATION
		) == PackageManager.PERMISSION_GRANTED
	}

	fun getLocationAccessLevel(): LocationAccessLevel {
		return when {
			hasPreciseLocationPermission() -> LocationAccessLevel.PRECISE
			ContextCompat.checkSelfPermission(
				appContext,
				Manifest.permission.ACCESS_COARSE_LOCATION
			) == PackageManager.PERMISSION_GRANTED -> LocationAccessLevel.APPROXIMATE
			else -> LocationAccessLevel.NONE
		}
	}

	fun isLocationEnabled(): Boolean {
		val manager = appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
		return runCatching { LocationManagerCompat.isLocationEnabled(manager) }
			.getOrElse {
				runCatching {
					manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
						manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
				}.getOrDefault(false)
			}
	}

	@SuppressLint("MissingPermission")
	fun requestLastKnownLocation(
		allowWhenLocationDisabled: Boolean = false,
		onResult: (LocationSnapshot?) -> Unit
	) {
		if (!hasLocationPermission()) {
			onResult(null)
			return
		}
		if (!allowWhenLocationDisabled && !isLocationEnabled()) {
			onResult(null)
			return
		}

		runCatching {
			fusedLocationClient.lastLocation
				.addOnSuccessListener { location ->
					onResult(location?.toSnapshot(source = LocationSource.LAST_KNOWN))
				}
				.addOnFailureListener { throwable ->
					Logger.w(TAG, "requestLastKnownLocation failed", throwable)
					onResult(null)
				}
		}.onFailure { throwable ->
			Logger.w(TAG, "requestLastKnownLocation setup failed", throwable)
			onResult(null)
		}
	}

	@SuppressLint("MissingPermission")
	fun requestCurrentLocation(
		preferLowPower: Boolean = false,
		maxUpdateAgeMillis: Long = DEFAULT_CURRENT_LOCATION_MAX_AGE_MS,
		timeoutMillis: Long = DEFAULT_CURRENT_LOCATION_TIMEOUT_MS,
		onResult: (LocationSnapshot?) -> Unit
	) {
		val accessLevel = getLocationAccessLevel()
		if (accessLevel == LocationAccessLevel.NONE || !isLocationEnabled()) {
			Logger.w(TAG, "requestCurrentLocation denied: permission/location disabled")
			onResult(null)
			return
		}

		val priority = if (preferLowPower) {
			Priority.PRIORITY_LOW_POWER
		} else {
			Priority.PRIORITY_BALANCED_POWER_ACCURACY
		}
		val cancellation = CancellationTokenSource()
		val request = CurrentLocationRequest.Builder()
			.setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
			.setPriority(priority)
			.setMaxUpdateAgeMillis(maxUpdateAgeMillis.coerceAtLeast(0L))
			.setDurationMillis(timeoutMillis.coerceAtLeast(1L))
			.build()

		runCatching {
			fusedLocationClient.getCurrentLocation(request, cancellation.token)
				.addOnSuccessListener { location ->
					if (location == null) {
						Logger.w(TAG, "requestCurrentLocation returned null")
						onResult(null)
						return@addOnSuccessListener
					}
					onResult(location.toSnapshot(source = LocationSource.CURRENT))
				}
				.addOnFailureListener { throwable ->
					Logger.w(TAG, "requestCurrentLocation failed", throwable)
					onResult(null)
				}
		}.onFailure { throwable ->
			Logger.w(TAG, "requestCurrentLocation setup failed", throwable)
			onResult(null)
		}
	}

	@SuppressLint("MissingPermission")
	fun startPassiveLocationUpdates(
		minUpdateDistanceMeters: Float = DEFAULT_PASSIVE_MIN_DISTANCE_METERS,
		onLocation: (LocationSnapshot) -> Unit
	) {
		if (!hasLocationPermission()) return
		synchronized(passiveListenerLock) {
			if (passiveLocationCallback != null) return
		}

		val request = LocationRequest.Builder(
			Priority.PRIORITY_PASSIVE,
			DEFAULT_PASSIVE_INTERVAL_MS
		)
			.setMinUpdateDistanceMeters(minUpdateDistanceMeters.coerceAtLeast(0f))
			.setMinUpdateIntervalMillis(DEFAULT_PASSIVE_MIN_INTERVAL_MS)
			.setMaxUpdateDelayMillis(DEFAULT_PASSIVE_MAX_DELAY_MS)
			.build()
		val callback = object : LocationCallback() {
			override fun onLocationResult(result: LocationResult) {
				result.lastLocation?.let { location ->
					onLocation(location.toSnapshot(source = LocationSource.PASSIVE))
				}
			}
		}

		runCatching {
			fusedLocationClient.requestLocationUpdates(
				request,
				callback,
				Looper.getMainLooper()
			).addOnSuccessListener {
				synchronized(passiveListenerLock) {
					passiveLocationCallback = callback
				}
			}.addOnFailureListener { throwable ->
				Logger.w(TAG, "startPassiveLocationUpdates failed", throwable)
			}
		}.onFailure { throwable ->
			Logger.w(TAG, "startPassiveLocationUpdates setup failed", throwable)
		}
	}

	fun stopPassiveLocationUpdates() {
		val callback = synchronized(passiveListenerLock) {
			val active = passiveLocationCallback
			passiveLocationCallback = null
			active
		} ?: return

		runCatching { fusedLocationClient.removeLocationUpdates(callback) }
			.onFailure { throwable ->
				Logger.w(TAG, "stopPassiveLocationUpdates failed", throwable)
			}
	}

	fun resolveCityOrDistrict(location: SunLocation): String? {
		val key = locationKey(location)
		synchronized(geocoderLock) {
			localityCache[key]?.let { return it }
		}

		val geocoder = Geocoder(appContext, Locale.getDefault())
		val address = resolveAddress(geocoder, location) ?: return null

		val label = listOfNotNull(
			address.subAdminArea?.takeIf { it.isNotBlank() },
			address.locality?.takeIf { it.isNotBlank() },
			address.adminArea?.takeIf { it.isNotBlank() }
		).firstOrNull() ?: return null

		synchronized(geocoderLock) {
			localityCache[key] = label
		}
		return label
	}

	private fun resolveAddress(
		geocoder: Geocoder,
		location: SunLocation
	): android.location.Address? {
		return runCatching {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				val result = AtomicReference<android.location.Address?>(null)
				val latch = CountDownLatch(1)
				geocoder.getFromLocation(
					location.latitude,
					location.longitude,
					1,
					object : Geocoder.GeocodeListener {
						override fun onGeocode(addresses: MutableList<android.location.Address>) {
							result.set(addresses.firstOrNull())
							latch.countDown()
						}

						override fun onError(errorMessage: String?) {
							latch.countDown()
						}
					}
				)
				latch.await(GEOCODER_TIMEOUT_MS, TimeUnit.MILLISECONDS)
				result.get()
			} else {
				@Suppress("DEPRECATION")
				geocoder.getFromLocation(location.latitude, location.longitude, 1)
					?.firstOrNull()
			}
		}.getOrNull()
	}

	private fun Location.toSnapshot(
		source: LocationSource
	): LocationSnapshot {
		return LocationSnapshot(
			latitude = latitude,
			longitude = longitude,
			timeZoneId = ZoneId.systemDefault().id,
			label = null,
			accuracyMeters = if (hasAccuracy()) accuracy else null,
			capturedAtEpochMs = time.takeIf { it > 0L } ?: System.currentTimeMillis(),
			accessLevel = getLocationAccessLevel(),
			source = source
		)
	}

	private fun locationKey(location: SunLocation): String {
		return String.format(Locale.US, "%.3f|%.3f", location.latitude, location.longitude)
	}

	private companion object {
		private const val TAG = "LocationProvider"
		private const val MAX_LOCALITY_CACHE_SIZE = 48
		private const val DEFAULT_CURRENT_LOCATION_MAX_AGE_MS = 15L * 60L * 1000L
		private const val DEFAULT_CURRENT_LOCATION_TIMEOUT_MS = 6_000L
		private const val DEFAULT_PASSIVE_INTERVAL_MS = 30L * 60L * 1000L
		private const val DEFAULT_PASSIVE_MIN_INTERVAL_MS = 5L * 60L * 1000L
		private const val DEFAULT_PASSIVE_MAX_DELAY_MS = 30L * 60L * 1000L
		private const val DEFAULT_PASSIVE_MIN_DISTANCE_METERS = 25_000f
		private const val GEOCODER_TIMEOUT_MS = 2_000L
	}
}
