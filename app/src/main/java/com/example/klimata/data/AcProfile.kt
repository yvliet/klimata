package com.example.klimata.data

import androidx.compose.runtime.Immutable

@Immutable
data class AcProfile(
    val roomName: String,
    val brand: String,
    val model: String,
    val capacity: String,
    val inverterType: String,
    val currentSetpoint: Int,
    val mode: String,
    val irCodeSet: String? = null,
    val fanSpeed: String = "Auto",
    val swing: Boolean = false,
)

typealias ACProfile = AcProfile
