/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Lumisky v5 Device katmanı bileşeni.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Lumisky v5 Device katmanı bileşeni.
 */
package com.example.lumisky.device

import android.annotation.SuppressLint
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.example.lumisky.data.DeviceLocationSnapshot
import com.example.lumisky.data.SettingsLocationPlanner
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val locationManager: LocationManager? =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    fun isLocationEnabled(): Boolean {
        val manager = locationManager ?: return false
        return runCatching {
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }.getOrDefault(false)
    }

    private fun resolveCityOrDistrict(latitude: Double, longitude: Double): String? {
        val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                val result = java.util.concurrent.atomic.AtomicReference<String?>(null)
                val latch = java.util.concurrent.CountDownLatch(1)
                geocoder.getFromLocation(latitude, longitude, 1, object : android.location.Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: List<android.location.Address>) {
                        val address = addresses.firstOrNull()
                        if (address != null) {
                            val label = listOfNotNull(
                                address.subAdminArea?.takeIf { it.isNotBlank() },
                                address.locality?.takeIf { it.isNotBlank() },
                                address.adminArea?.takeIf { it.isNotBlank() }
                            ).firstOrNull()
                            result.set(label)
                        }
                        latch.countDown()
                    }
                    override fun onError(errorMessage: String?) {
                        latch.countDown()
                    }
                })
                latch.await(2000, java.util.concurrent.TimeUnit.MILLISECONDS)
                result.get()
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val address = addresses?.firstOrNull()
                if (address != null) {
                    listOfNotNull(
                        address.subAdminArea?.takeIf { it.isNotBlank() },
                        address.locality?.takeIf { it.isNotBlank() },
                        address.adminArea?.takeIf { it.isNotBlank() }
                    ).firstOrNull()
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun readLastKnownSnapshot(nowEpochMs: Long = System.currentTimeMillis()): DeviceLocationSnapshot? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        if (!hasLocationPermission()) return@withContext null
        if (!isLocationEnabled()) return@withContext null

        try {
            // 1. Try to get last location
            val lastLocationTask = fusedLocationClient.lastLocation
            val lastLocation = Tasks.await(lastLocationTask)
            if (lastLocation != null) {
                val ageMs = nowEpochMs - lastLocation.time
                if (ageMs in 0..(15 * 60 * 1000)) { // less than 15 mins old
                    val label = resolveCityOrDistrict(lastLocation.latitude, lastLocation.longitude)
                        ?: SettingsLocationPlanner.formatCoordinates(
                            lastLocation.latitude,
                            lastLocation.longitude
                        )
                    return@withContext DeviceLocationSnapshot(
                        label = label,
                        latitude = lastLocation.latitude,
                        longitude = lastLocation.longitude,
                        timeZoneId = TimeZone.getDefault().id,
                        capturedAtEpochMs = lastLocation.time
                    )
                }
            }

            // 2. Request current location
            val cts = CancellationTokenSource()
            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .setDurationMillis(5000)
                .build()
            val currentLocationTask = fusedLocationClient.getCurrentLocation(request, cts.token)
            val currentLocation = Tasks.await(currentLocationTask)
            if (currentLocation != null) {
                val label = resolveCityOrDistrict(currentLocation.latitude, currentLocation.longitude)
                    ?: SettingsLocationPlanner.formatCoordinates(
                        currentLocation.latitude,
                        currentLocation.longitude
                    )
                return@withContext DeviceLocationSnapshot(
                    label = label,
                    latitude = currentLocation.latitude,
                    longitude = currentLocation.longitude,
                    timeZoneId = TimeZone.getDefault().id,
                    capturedAtEpochMs = currentLocation.time.takeIf { it > 0L } ?: nowEpochMs
                )
            }

            // Fallback to whatever lastLocation we had if current request fails/times out
            if (lastLocation != null) {
                val label = resolveCityOrDistrict(lastLocation.latitude, lastLocation.longitude)
                    ?: SettingsLocationPlanner.formatCoordinates(
                        lastLocation.latitude,
                        lastLocation.longitude
                    )
                return@withContext DeviceLocationSnapshot(
                    label = label,
                    latitude = lastLocation.latitude,
                    longitude = lastLocation.longitude,
                    timeZoneId = TimeZone.getDefault().id,
                    capturedAtEpochMs = lastLocation.time
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
}
