package com.example.klimata.data.network

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

@Immutable
data class AmbientWeatherReport(
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val currentOutdoorTemp: Int,
    val highTemp: Int,
    val lowTemp: Int,
    val relativeHumidity: Int,
    val condition: String,
    val aqiValue: Int,
    val aqiLabel: String,
    val hourlyTemps: Map<String, Int>,
    val gridStatus: String = "Grid Normal",
    val lastUpdatedMillis: Long = System.currentTimeMillis()
)

object WeatherApiClient {

    private const val CONNECT_TIMEOUT_MS = 6000
    private const val READ_TIMEOUT_MS = 6000

    suspend fun fetchWeather(
        latitude: Double,
        longitude: Double,
        locationName: String = "South Jakarta"
    ): Result<AmbientWeatherReport> = withContext(Dispatchers.IO) {
        try {
            val forecastUrl = "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=%.4f&longitude=%.4f".format(Locale.US, latitude, longitude) +
                    "&current=temperature_2m,relative_humidity_2m,weather_code,apparent_temperature" +
                    "&hourly=temperature_2m,weather_code" +
                    "&daily=temperature_2m_max,temperature_2m_min" +
                    "&timezone=auto&forecast_days=2"

            val aqiUrl = "https://air-quality-api.open-meteo.com/v1/air-quality" +
                    "?latitude=%.4f&longitude=%.4f&current=us_aqi".format(Locale.US, latitude, longitude)

            val forecastJson = getJson(forecastUrl)
            val aqiJson = try { getJson(aqiUrl) } catch (ignored: Exception) { null }

            val currentObj = forecastJson.getJSONObject("current")
            val currentTemp = currentObj.getDouble("temperature_2m").roundToInt()
            val humidity = currentObj.getInt("relative_humidity_2m")
            val weatherCode = currentObj.getInt("weather_code")

            val dailyObj = forecastJson.getJSONObject("daily")
            val maxTemps = dailyObj.getJSONArray("temperature_2m_max")
            val minTemps = dailyObj.getJSONArray("temperature_2m_min")
            val highTemp = if (maxTemps.length() > 0) maxTemps.getDouble(0).roundToInt() else currentTemp + 4
            val lowTemp = if (minTemps.length() > 0) minTemps.getDouble(0).roundToInt() else currentTemp - 5

            val condition = mapWmoCodeToCondition(weatherCode)

            var aqiVal = 101
            var aqiLbl = "Moderate"
            aqiJson?.optJSONObject("current")?.let {
                if (it.has("us_aqi")) {
                    aqiVal = it.getInt("us_aqi")
                    aqiLbl = mapAqiToLabel(aqiVal)
                }
            }

            val hourlyTemps = extract24HourForecast(forecastJson.getJSONObject("hourly"))
            val gridStatus = com.example.klimata.data.engine.ThermalCalculationEngine.computeGridStatus(
                outdoorTemp = currentTemp
            )

            Result.success(
                AmbientWeatherReport(
                    location = locationName,
                    latitude = latitude,
                    longitude = longitude,
                    currentOutdoorTemp = currentTemp,
                    highTemp = highTemp,
                    lowTemp = lowTemp,
                    relativeHumidity = humidity,
                    condition = condition,
                    aqiValue = aqiVal,
                    aqiLabel = aqiLbl,
                    hourlyTemps = hourlyTemps,
                    gridStatus = gridStatus
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extract24HourForecast(hourlyObj: JSONObject): Map<String, Int> {
        val timeArr = hourlyObj.getJSONArray("time")
        val tempArr = hourlyObj.getJSONArray("temperature_2m")
        val result = mutableMapOf<String, Int>()

        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val currentHourSuffix = String.format(Locale.US, "T%02d:00", currentHour)

        var startIndex = 0
        for (i in 0 until timeArr.length()) {
            if (timeArr.getString(i).contains(currentHourSuffix)) {
                startIndex = i
                break
            }
        }

        for (i in startIndex until (startIndex + 24).coerceAtMost(timeArr.length())) {
            val rawTime = timeArr.getString(i)
            val timeKey = rawTime.substringAfter("T").take(5)
            val temp = tempArr.getDouble(i).roundToInt()
            result[timeKey] = temp
        }

        var lastTemp = 28
        for (offset in 0 until 24) {
            val hour = (currentHour + offset) % 24
            val key = String.format(Locale.US, "%02d:00", hour)
            if (!result.containsKey(key)) {
                result[key] = lastTemp
            } else {
                lastTemp = result[key] ?: lastTemp
            }
        }

        return result
    }

    internal fun mapWmoCodeToCondition(code: Int): String = when (code) {
        0 -> "Clear Night"
        1, 2 -> "Partly Cloudy"
        3 -> "Overcast"
        45, 48 -> "Fog"
        51, 53, 55, 61, 63, 65, 80, 81, 82 -> "Rainy"
        95, 96, 99 -> "Thunderstorm"
        else -> "Partly Cloudy"
    }

    internal fun mapAqiToLabel(aqi: Int): String = when {
        aqi <= 50 -> "Good"
        aqi <= 100 -> "Moderate"
        aqi <= 150 -> "Sensitive"
        aqi <= 200 -> "Unhealthy"
        else -> "Very Unhealthy"
    }

    private fun getJson(urlString: String): JSONObject {
        val url = java.net.URI(urlString).toURL()
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = CONNECT_TIMEOUT_MS
        conn.readTimeout = READ_TIMEOUT_MS
        conn.setRequestProperty("Accept", "application/json")
        conn.setRequestProperty("User-Agent", "Klimata-Android/1.0")

        return try {
            val responseCode = conn.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw IllegalStateException("HTTP response $responseCode from $urlString")
            }
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            JSONObject(text)
        } finally {
            conn.disconnect()
        }
    }
}
