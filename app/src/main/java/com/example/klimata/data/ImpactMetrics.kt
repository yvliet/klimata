package com.example.klimata.data

import androidx.compose.runtime.Immutable

@Immutable
data class ImpactMetric(
    val title: String,
    val primaryValue: String,
    val subtitle: String,
)

@Immutable
data class TimeBucketedImpact(
    val weekly: ImpactMetric,
    val monthly: ImpactMetric,
    val yearly: ImpactMetric,
    val lifetime: ImpactMetric,
) {
    fun forPeriod(period: ImpactPeriod): ImpactMetric = when (period) {
        ImpactPeriod.WEEKLY -> weekly
        ImpactPeriod.MONTHLY -> monthly
        ImpactPeriod.YEARLY -> yearly
        ImpactPeriod.LIFETIME -> lifetime
    }
}

@Immutable
data class SavingsBreakdown(
    val compressorCyclingPercent: Int,
    val fanCoastingPercent: Int,
    val avgNightlyKwhSaved: Float,
    val localTariffPerKwh: String,
)

@Immutable
data class CarbonEquivalence(
    val treesEquivalent: Float,
    val drivingKmAvoided: Float,
    val ledHoursEquivalent: Float,
    val gridEmissionFactor: Float,
)
