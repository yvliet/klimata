package com.example.klimata

import com.example.klimata.data.network.WeatherApiClient
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherApiClientTest {

    @Test
    fun mapWmoCodeToCondition_mapsKnownCodes() {
        assertEquals("Clear Night", WeatherApiClient.mapWmoCodeToCondition(0))
        assertEquals("Partly Cloudy", WeatherApiClient.mapWmoCodeToCondition(1))
        assertEquals("Partly Cloudy", WeatherApiClient.mapWmoCodeToCondition(2))
        assertEquals("Overcast", WeatherApiClient.mapWmoCodeToCondition(3))
        assertEquals("Fog", WeatherApiClient.mapWmoCodeToCondition(45))
        assertEquals("Rainy", WeatherApiClient.mapWmoCodeToCondition(61))
        assertEquals("Thunderstorm", WeatherApiClient.mapWmoCodeToCondition(95))
    }

    @Test
    fun mapAqiToLabel_categorizesHealthLevels() {
        assertEquals("Good", WeatherApiClient.mapAqiToLabel(35))
        assertEquals("Moderate", WeatherApiClient.mapAqiToLabel(75))
        assertEquals("Sensitive", WeatherApiClient.mapAqiToLabel(120))
        assertEquals("Unhealthy", WeatherApiClient.mapAqiToLabel(165))
        assertEquals("Very Unhealthy", WeatherApiClient.mapAqiToLabel(220))
    }
}
