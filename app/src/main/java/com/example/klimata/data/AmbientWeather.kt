package com.example.klimata.data

import androidx.compose.runtime.Immutable

@Immutable
data class AmbientWeather(
    val location: String,
    val condition: String,
    val currentOutdoorTemp: Int,
    val highTemp: Int,
    val lowTemp: Int,
    val aqiValue: Int,
    val aqiLabel: String,
    val gridStatus: String,
)
