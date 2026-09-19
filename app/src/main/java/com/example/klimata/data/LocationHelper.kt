package com.example.klimata.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class LocationCoordinates(
    val latitude: Double,
    val longitude: Double,
    val cityName: String
)

object LocationHelper {

    val DEFAULT_JAKARTA = LocationCoordinates(
        latitude = -6.2088,
        longitude = 106.8456,
        cityName = "South Jakarta"
    )

    @SuppressLint("MissingPermission")
    suspend fun detectLocation(context: Context): LocationCoordinates = withContext(Dispatchers.IO) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return@withContext DEFAULT_JAKARTA

            val providers = listOf(
                LocationManager.NETWORK_PROVIDER,
                LocationManager.GPS_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )

            var bestLocation: Location? = null
            for (provider in providers) {
                if (locationManager.isProviderEnabled(provider)) {
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null) {
                        if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                            bestLocation = loc
                        }
                    }
                }
            }

            val loc = bestLocation ?: return@withContext DEFAULT_JAKARTA
            val city = reverseGeocode(context, loc.latitude, loc.longitude) ?: "South Jakarta"
            LocationCoordinates(loc.latitude, loc.longitude, city)
        } catch (e: Exception) {
            DEFAULT_JAKARTA
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun detectCity(context: Context): String? = withContext(Dispatchers.IO) {
        val result = detectLocation(context)
        result.cityName
    }

    private suspend fun reverseGeocode(context: Context, latitude: Double, longitude: Double): String? =
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                            val addr = addresses.firstOrNull()
                            val city = addr?.locality
                                ?: addr?.subAdminArea
                                ?: addr?.adminArea
                            continuation.resume(city)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val addr = addresses?.firstOrNull()
                    addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea
                }
            } catch (e: Exception) {
                null
            }
        }
}
