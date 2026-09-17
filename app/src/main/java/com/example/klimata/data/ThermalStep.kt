package com.example.klimata.data

import androidx.compose.runtime.Immutable

@Immutable
data class ThermalStep(
    val time: String,
    val setpointCelsius: Int,
    val label: String,
    val phaseName: String,
    val fanMode: String,
    val fanDetail: String,
    val outdoorTemp: Int,
    val deltaLabel: String,
    val isActive: Boolean = false,
    val isCompleted: Boolean = false,
)
