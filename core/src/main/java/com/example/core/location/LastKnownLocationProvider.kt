package com.example.core.location

import android.annotation.SuppressLint
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.util.Consumer
import com.example.core.Logger
import com.example.core.api.SunLocation
import java.util.Locale

class LastKnownLocationProvider(
	context: Context
) {
	private val appContext = context.applicationContext
	private val geocoderLock = Any()
	private val localityCache = LinkedHashMap<String, String>(32, 0.75f, true)

	fun hasLocationPermission(): Boolean {
		return ContextCompat.checkSelfPermission(
			appContext,
			Manifest.permission.ACCESS_FINE_LOCATION
		) == PackageManager.PERMISSION_GRANTED
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
	fun getLastKnownLocation(
		label: String = "gps_last_known",
		allowWhenLocationDisabled: Boolean = false
	): SunLocation? {
		if (!hasLocationPermission()) return null
		if (!allowWhenLocationDisabled && !isLocationEnabled()) return null

		val manager = appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
		val providers = listOf(
			LocationManager.GPS_PROVIDER,
			LocationManager.NETWORK_PROVIDER,
			LocationManager.PASSIVE_PROVIDER
		)

		val best = runCatching {
			providers.asSequence()
				.mapNotNull { provider ->
					runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
				}
				.maxByOrNull { location ->
					location.time
				}
		}.getOrNull() ?: return null

		val resolved = SunLocation(
			label = label,
			latitude = best.latitude,
			longitude = best.longitude
		)
		Logger.event(
			"LocationProvider",
			"last_known_location",
			"label" to label,
			"lat" to resolved.latitude,
			"lon" to resolved.longitude
		)
		return resolved
	}

	@SuppressLint("MissingPermission")
	fun requestCurrentLocation(
		label: String = "gps_live",
		onResult: (SunLocation?) -> Unit
	) {
		if (!hasLocationPermission() || !isLocationEnabled()) {
			Logger.w("LocationProvider", "requestCurrentLocation denied: permission/location disabled")
			onResult(null)
			return
		}
		val manager = appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
		if (manager == null) {
			Logger.w("LocationProvider", "requestCurrentLocation: manager unavailable")
			onResult(null)
			return
		}

		val provider = when {
			runCatching { manager.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false) ->
				LocationManager.GPS_PROVIDER
			runCatching { manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }.getOrDefault(false) ->
				LocationManager.NETWORK_PROVIDER
			else -> LocationManager.PASSIVE_PROVIDER
		}

		try {
			LocationManagerCompat.getCurrentLocation(
				manager,
				provider,
				null as android.os.CancellationSignal?,
				ContextCompat.getMainExecutor(appContext),
				Consumer<Location?> { location: Location? ->
					if (location == null) {
						Logger.w(
							"LocationProvider",
							"getCurrentLocation returned null, fallback to last known"
						)
						onResult(getLastKnownLocation(label = label, allowWhenLocationDisabled = true))
						return@Consumer
					}
					val resolved = SunLocation(
						label = label,
						latitude = location.latitude,
						longitude = location.longitude
					)
					Logger.event(
						"LocationProvider",
						"current_location",
						"label" to label,
						"provider" to provider,
						"lat" to resolved.latitude,
						"lon" to resolved.longitude
					)
					onResult(resolved)
				}
			)
		} catch (_: Throwable) {
			Logger.w("LocationProvider", "getCurrentLocation failed, fallback to last known")
			onResult(getLastKnownLocation(label = label, allowWhenLocationDisabled = true))
		}
	}

	fun resolveCityOrDistrict(location: SunLocation): String? {
		val key = locationKey(location)
		synchronized(geocoderLock) {
			localityCache[key]?.let { return it }
		}

		val geocoder = Geocoder(appContext, Locale.getDefault())
		val address = runCatching {
			geocoder.getFromLocation(location.latitude, location.longitude, 1)
				?.firstOrNull()
		}.getOrNull() ?: return null

		val label = listOfNotNull(
			address.subAdminArea?.takeIf { it.isNotBlank() },
			address.locality?.takeIf { it.isNotBlank() },
			address.adminArea?.takeIf { it.isNotBlank() }
		).firstOrNull() ?: return null

		synchronized(geocoderLock) {
			localityCache[key] = label
			trimCacheLocked()
		}
		Logger.event("LocationProvider", "reverse_geocode", "label" to label)
		return label
	}

	private fun locationKey(location: SunLocation): String {
		return String.format(Locale.US, "%.3f|%.3f", location.latitude, location.longitude)
	}

	private fun trimCacheLocked() {
		if (localityCache.size <= MAX_LOCALITY_CACHE_SIZE) return
		val iterator = localityCache.entries.iterator()
		while (localityCache.size > MAX_LOCALITY_CACHE_SIZE && iterator.hasNext()) {
			iterator.next()
			iterator.remove()
		}
	}

	private companion object {
		private const val MAX_LOCALITY_CACHE_SIZE = 48
	}
}
