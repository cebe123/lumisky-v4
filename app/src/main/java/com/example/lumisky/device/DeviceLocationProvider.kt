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
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class DeviceLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val snapshotFactory: DeviceLocationSnapshotFactory
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

    @SuppressLint("MissingPermission")
    suspend fun readLastKnownSnapshot(nowEpochMs: Long = System.currentTimeMillis()): DeviceLocationSnapshot? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        if (!hasLocationPermission()) return@withContext null

        runCatching {
            val lastLocation = fusedLocationClient.lastLocation.awaitCancellable()
            if (lastLocation != null) {
                val ageMs = nowEpochMs - lastLocation.time
                if (ageMs in 0..FRESH_LOCATION_MAX_AGE_MILLIS) {
                    return@withContext snapshotFactory.create(
                        latitude = lastLocation.latitude,
                        longitude = lastLocation.longitude,
                        capturedAtEpochMs = lastLocation.time
                    )
                }
            }

            if (isLocationEnabled()) {
                val cancellation = CancellationTokenSource()
                val request = CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                    .setDurationMillis(CURRENT_LOCATION_TIMEOUT_MILLIS)
                    .build()
                val currentLocation = fusedLocationClient
                    .getCurrentLocation(request, cancellation.token)
                    .awaitCancellable(cancellation::cancel)
                if (currentLocation != null) {
                    return@withContext snapshotFactory.create(
                        latitude = currentLocation.latitude,
                        longitude = currentLocation.longitude,
                        capturedAtEpochMs = currentLocation.time.takeIf { it > 0L } ?: nowEpochMs
                    )
                }
            }

            if (lastLocation != null) {
                return@withContext snapshotFactory.create(
                    latitude = lastLocation.latitude,
                    longitude = lastLocation.longitude,
                    capturedAtEpochMs = lastLocation.time
                )
            }
        }
        return@withContext null
    }

    private suspend fun <T> Task<T>.awaitCancellable(onCancellation: () -> Unit = {}): T? =
        suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { value ->
                if (continuation.isActive) continuation.resume(value)
            }
            addOnFailureListener {
                if (continuation.isActive) continuation.resume(null)
            }
            addOnCanceledListener {
                if (continuation.isActive) continuation.cancel()
            }
            continuation.invokeOnCancellation { onCancellation() }
        }

    private companion object {
        const val FRESH_LOCATION_MAX_AGE_MILLIS = 15 * 60 * 1000L
        const val CURRENT_LOCATION_TIMEOUT_MILLIS = 5_000L
    }
}
