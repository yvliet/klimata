package com.example.klimata.data

import androidx.compose.runtime.Immutable
import kotlin.math.roundToInt

@Immutable
data class DispatchState(
    val isAutonomous: Boolean,
    val statusLabel: String,
    val dispatchMethod: String,
)

@Immutable
data class RoomState(
    val id: String,
    val name: String,
    val location: String = "South Jakarta",
    val profile: AcProfile,
    val currentTemp: Int,
    val targetTemp: Int,
    val isPowerOn: Boolean = true,
    val isEcoEnabled: Boolean = true,
    val weatherCondition: String = "Clear",
    val thermalSteps: List<ThermalStep>,
    val dispatchState: DispatchState,
    val monthlySavings: ImpactMetric,
    val avoidedCarbon: ImpactMetric,
    val savingsHistory: TimeBucketedImpact,
    val carbonHistory: TimeBucketedImpact,
    val savingsBreakdown: SavingsBreakdown,
    val carbonEquivalence: CarbonEquivalence,
    val areaSquareMeters: Int = 20,
    val ceilingHeightMeters: Float = 2.8f,
    val thermalMassLabel: String = "Medium Thermal Mass",
    val coolingLoadBtu: Int = 7000,
)

val RoomState.volumeCubicMeters: Int
    get() = (areaSquareMeters * ceilingHeightMeters).roundToInt()
