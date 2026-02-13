package com.example.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.example.core.api.SunLocation

class LastKnownLocationProvider(
	context: Context
) {
	private val appContext = context.applicationContext

	fun hasLocationPermission(): Boolean {
		return ContextCompat.checkSelfPermission(
			appContext,
			Manifest.permission.ACCESS_FINE_LOCATION
		) == PackageManager.PERMISSION_GRANTED
	}

	fun isLocationEnabled(): Boolean {
		val manager = appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
		return LocationManagerCompat.isLocationEnabled(manager)
	}

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

		val best = providers.asSequence()
			.mapNotNull { provider ->
				runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
			}
			.maxByOrNull { location ->
				location.time
			} ?: return null

		return SunLocation(
			label = label,
			latitude = best.latitude,
			longitude = best.longitude
		)
	}
}
