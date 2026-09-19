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
    val cityName: String,
    val countryCode: String = "ID"
)

object LocationHelper {

    val DEFAULT_JAKARTA = LocationCoordinates(
        latitude = -6.2088,
        longitude = 106.8456,
        cityName = "South Jakarta",
        countryCode = "ID"
    )

    fun resolveDefaultLocation(locale: Locale = Locale.getDefault()): LocationCoordinates {
        val tz = java.util.TimeZone.getDefault().id.lowercase(Locale.ROOT)
        if (tz.contains("jakarta") || tz.contains("makassar") || tz.contains("jayapura") || tz.contains("pontianak")) {
            return DEFAULT_JAKARTA
        }

        return when (locale.country.uppercase(Locale.ROOT)) {
            "ID" -> DEFAULT_JAKARTA
            "US" -> LocationCoordinates(latitude = 34.0522, longitude = -118.2437, cityName = "Los Angeles", countryCode = "US")
            "SG" -> LocationCoordinates(latitude = 1.3521, longitude = 103.8198, cityName = "Singapore", countryCode = "SG")
            "JP" -> LocationCoordinates(latitude = 35.6762, longitude = 139.6503, cityName = "Tokyo", countryCode = "JP")
            "AU" -> LocationCoordinates(latitude = -33.8688, longitude = 151.2093, cityName = "Sydney", countryCode = "AU")
            "GB" -> LocationCoordinates(latitude = 51.5074, longitude = -0.1278, cityName = "London", countryCode = "GB")
            "DE" -> LocationCoordinates(latitude = 52.5200, longitude = 13.4050, cityName = "Berlin", countryCode = "DE")
            "MY" -> LocationCoordinates(latitude = 3.1390, longitude = 101.6869, cityName = "Kuala Lumpur", countryCode = "MY")
            "TH" -> LocationCoordinates(latitude = 13.7563, longitude = 100.5018, cityName = "Bangkok", countryCode = "TH")
            "VN" -> LocationCoordinates(latitude = 10.8231, longitude = 106.6297, cityName = "Ho Chi Minh City", countryCode = "VN")
            else -> DEFAULT_JAKARTA
        }
    }

    fun inferCountryCode(latitude: Double, longitude: Double): String {
        return when {
            latitude in -11.5..6.5 && longitude in 94.5..141.5 -> "ID"
            latitude in 1.15..1.48 && longitude in 103.55..104.1 -> "SG"
            latitude in 24.0..46.0 && longitude in 122.0..153.0 -> "JP"
            latitude in -44.0..-10.0 && longitude in 112.0..154.0 -> "AU"
            latitude in 24.0..50.0 && longitude in -125.0..-66.0 -> "US"
            latitude in 35.0..70.0 && longitude in -10.0..35.0 -> "EU"
            else -> "ID"
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun detectLocation(context: Context): LocationCoordinates = withContext(Dispatchers.IO) {
        val defaultLoc = resolveDefaultLocation(Locale.getDefault())
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return@withContext defaultLoc

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

            val loc = bestLocation ?: return@withContext defaultLoc
            val geo = reverseGeocode(context, loc.latitude, loc.longitude)
            val city = geo?.first ?: defaultLoc.cityName
            val countryCode = geo?.second ?: inferCountryCode(loc.latitude, loc.longitude)
            LocationCoordinates(loc.latitude, loc.longitude, city, countryCode)
        } catch (e: Exception) {
            defaultLoc
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun detectCity(context: Context): String? = withContext(Dispatchers.IO) {
        val result = detectLocation(context)
        result.cityName
    }

    private suspend fun reverseGeocode(context: Context, latitude: Double, longitude: Double): Pair<String, String?>? =
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    kotlinx.coroutines.withTimeoutOrNull(5000L) {
                        suspendCancellableCoroutine { continuation ->
                            try {
                                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                                    if (continuation.isActive) {
                                        val addr = addresses.firstOrNull()
                                        val city = addr?.locality
                                            ?: addr?.subAdminArea
                                            ?: addr?.adminArea
                                        val country = addr?.countryCode
                                        if (city != null) {
                                            continuation.resume(Pair(city, country))
                                        } else {
                                            continuation.resume(null)
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                if (continuation.isActive) {
                                    continuation.resume(null)
                                }
                            }
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val addr = addresses?.firstOrNull()
                    val city = addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea
                    val country = addr?.countryCode
                    if (city != null) Pair(city, country) else null
                }
            } catch (e: Exception) {
                null
            }
        }
}
