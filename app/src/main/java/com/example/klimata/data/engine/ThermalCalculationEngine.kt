package com.example.klimata.data.engine

import com.example.klimata.data.CarbonEquivalence
import com.example.klimata.data.EnergyConfig
import com.example.klimata.data.ImpactMetric
import com.example.klimata.data.SavingsBreakdown
import com.example.klimata.data.ThermalStep
import com.example.klimata.data.TimeBucketedImpact
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

data class ThermalComputationResult(
    val coolingLoadBtu: Int,
    val thermalMassLabel: String,
    val thermalSteps: List<ThermalStep>,
    val monthlySavings: ImpactMetric,
    val avoidedCarbon: ImpactMetric,
    val savingsHistory: TimeBucketedImpact,
    val carbonHistory: TimeBucketedImpact,
    val savingsBreakdown: SavingsBreakdown,
    val carbonEquivalence: CarbonEquivalence
)

object ThermalCalculationEngine {


    fun formatCurrency(amount: Double, config: EnergyConfig): String {
        return if (config.currencyCode == "IDR" || config.currencyCode == "JPY") {
            "${config.currencySymbol} " + String.format(Locale.GERMAN, "%,d", amount.roundToInt())
        } else {
            "${config.currencySymbol}" + String.format(Locale.US, "%.2f", amount)
        }
    }

    fun computeGridStatus(
        hour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        outdoorTemp: Int = 28
    ): String {
        return when {
            outdoorTemp >= 33 -> "Grid High Stress | Peak Cooling"
            hour in 17..21 -> "Grid Peak Demand"
            hour in 22..23 || hour in 0..6 -> "Grid Off-Peak | Eco Window"
            else -> "Grid Normal"
        }
    }

    fun computeRoomThermalDynamics(
        areaSquareMeters: Int,
        ceilingHeightMeters: Float,
        thermalMassType: String,
        acCapacity: String,
        acInverterType: String,
        targetTemp: Int,
        hourlyOutdoorTemps: Map<String, Int> = emptyMap(),
        energyConfig: EnergyConfig = EnergyConfig.INDONESIA
    ): ThermalComputationResult {
        val (thermalFactor, thermalLabel) = when (thermalMassType.lowercase(Locale.ROOT)) {
            "light", "drywall" -> 300 to "Low Thermal Mass"
            "heavy", "concrete" -> 420 to "High Thermal Inertia"
            else -> 350 to "Medium Thermal Mass"
        }

        val coolingLoadBtu = (areaSquareMeters * thermalFactor).coerceAtLeast(3500)

        val ratedWatts = when (acCapacity.trim()) {
            "0.5 PK" -> 380f
            "0.75 PK" -> 590f
            "1.5 PK" -> 1080f
            "2.0 PK" -> 1520f
            else -> if (acInverterType.contains("Non", ignoreCase = true)) 840f else 750f
        }

        val baselineNightlyKwh = (ratedWatts * 10f * 0.74f) / 1000f

        val coastingHours = when (thermalMassType.lowercase(Locale.ROOT)) {
            "heavy", "concrete" -> 2.0f
            "light", "drywall" -> 1.0f
            else -> 1.5f
        }

        val activeCompressorHours = 10f - coastingHours
        val modulatedWatts = ratedWatts * 0.76f
        val fanOnlyWatts = 30f
        val klimataNightlyKwh = ((modulatedWatts * activeCompressorHours) + (fanOnlyWatts * coastingHours)) / 1000f

        val baseKwhSaved = (baselineNightlyKwh - klimataNightlyKwh).coerceIn(0.95f, 4.2f)

        val monthlySavingsLocal = baseKwhSaved * 30 * energyConfig.tariffPerKwh
        val formattedMonthly = formatCurrency(monthlySavingsLocal, energyConfig)
        val monthlySavingsUsdVal = monthlySavingsLocal / energyConfig.currencyRateToUsd
        val monthlySavingsUsd = String.format(Locale.US, "%.2f", monthlySavingsUsdVal)

        val avoidedMonthlyCo2Kg = baseKwhSaved * 30 * energyConfig.gridEmissionFactor
        val treesEquiv = avoidedMonthlyCo2Kg / 24.5f
        val kmAvoided = avoidedMonthlyCo2Kg * 4.05f

        val savingsSubtitle = if (energyConfig.currencyCode == "USD") {
            "-38% kWh nightly"
        } else {
            "\$$monthlySavingsUsd USD / -38% kWh"
        }

        val savingsMetric = ImpactMetric(
            title = "Monthly Savings",
            primaryValue = formattedMonthly,
            subtitle = savingsSubtitle
        )

        val carbonMetric = ImpactMetric(
            title = "Avoided Carbon",
            primaryValue = String.format(Locale.US, "%.1f kg", avoidedMonthlyCo2Kg),
            subtitle = String.format(Locale.US, "CO₂e offset / %.1f trees equiv", treesEquiv)
        )

        val weeklySavings = monthlySavingsLocal / 4.3
        val yearlySavings = monthlySavingsLocal * 12
        val lifetimeSavings = yearlySavings * 3

        val savingsHistory = TimeBucketedImpact(
            weekly = ImpactMetric(
                title = "Weekly Savings",
                primaryValue = formatCurrency(weeklySavings, energyConfig),
                subtitle = "-38% kWh"
            ),
            monthly = savingsMetric,
            yearly = ImpactMetric(
                title = "Yearly Savings",
                primaryValue = formatCurrency(yearlySavings, energyConfig),
                subtitle = "-38% kWh"
            ),
            lifetime = ImpactMetric(
                title = "Lifetime Savings",
                primaryValue = formatCurrency(lifetimeSavings, energyConfig),
                subtitle = "-38% kWh (3-Yr)"
            )
        )

        val carbonHistory = TimeBucketedImpact(
            weekly = ImpactMetric(
                title = "Avoided Carbon",
                primaryValue = String.format(Locale.US, "%.1f kg", avoidedMonthlyCo2Kg / 4.3f),
                subtitle = "CO₂e offset"
            ),
            monthly = carbonMetric,
            yearly = ImpactMetric(
                title = "Avoided Carbon",
                primaryValue = String.format(Locale.US, "%.1f kg", avoidedMonthlyCo2Kg * 12),
                subtitle = "CO₂e offset"
            ),
            lifetime = ImpactMetric(
                title = "Avoided Carbon",
                primaryValue = String.format(Locale.US, "%.1f kg", avoidedMonthlyCo2Kg * 36),
                subtitle = "CO₂e offset (3-Yr)"
            )
        )

        val steps = generateSchedule(
            targetTemp = targetTemp,
            hourlyOutdoorTemps = hourlyOutdoorTemps,
            thermalMassType = thermalMassType
        )

        val coastingPercentage = ((coastingHours / 10f) * 100f).roundToInt().coerceIn(25, 45)
        val cyclingPercentage = 100 - coastingPercentage

        val tariffLabel = if (energyConfig.currencyCode == "IDR" || energyConfig.currencyCode == "JPY") {
            "${energyConfig.currencySymbol} " + String.format(Locale.GERMAN, "%,d", energyConfig.tariffPerKwh.roundToInt()) + " / kWh"
        } else {
            "${energyConfig.currencySymbol}" + String.format(Locale.US, "%.2f", energyConfig.tariffPerKwh) + " / kWh"
        }

        return ThermalComputationResult(
            coolingLoadBtu = coolingLoadBtu,
            thermalMassLabel = thermalLabel,
            thermalSteps = steps,
            monthlySavings = savingsMetric,
            avoidedCarbon = carbonMetric,
            savingsHistory = savingsHistory,
            carbonHistory = carbonHistory,
            savingsBreakdown = SavingsBreakdown(
                compressorCyclingPercent = cyclingPercentage,
                fanCoastingPercent = coastingPercentage,
                avgNightlyKwhSaved = baseKwhSaved,
                localTariffPerKwh = tariffLabel
            ),
            carbonEquivalence = CarbonEquivalence(
                treesEquivalent = treesEquiv,
                drivingKmAvoided = kmAvoided,
                ledHoursEquivalent = baseKwhSaved * 30 * 1.5f,
                gridEmissionFactor = energyConfig.gridEmissionFactor
            )
        )
    }

    fun generateSchedule(
        targetTemp: Int,
        hourlyOutdoorTemps: Map<String, Int> = emptyMap(),
        thermalMassType: String = "Medium",
        currentHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    ): List<ThermalStep> {
        val base = targetTemp.coerceIn(20, 26)
        val isHeavyMass = thermalMassType.equals("heavy", ignoreCase = true) || thermalMassType.equals("concrete", ignoreCase = true)

        val fallbackTrajectory = mapOf(
            "00:00" to 26, "01:00" to 25, "02:00" to 25, "03:00" to 24,
            "04:00" to 24, "05:00" to 24, "06:00" to 25, "07:00" to 26,
            "08:00" to 28, "09:00" to 30, "10:00" to 31, "11:00" to 32,
            "12:00" to 33, "13:00" to 33, "14:00" to 32, "15:00" to 31,
            "16:00" to 30, "17:00" to 29, "18:00" to 28, "19:00" to 28,
            "20:00" to 27, "21:00" to 27, "22:00" to 26, "23:00" to 26
        )

        return (0 until 24).map { offset ->
            val hourInt = (currentHour + offset) % 24
            val timeKey = String.format(Locale.US, "%02d:00", hourInt)
            val outdoor = hourlyOutdoorTemps[timeKey] ?: fallbackTrajectory[timeKey] ?: 28
            val isNowActive = (offset == 0)

            val setpoint: Int
            val phase: String
            val label: String
            val fanMode: String
            val fanDetail: String

            when (timeKey) {
                // Nighttime Pre-Cool: 21:00 - 23:00
                "21:00", "22:00" -> {
                    setpoint = base
                    phase = "Pre-Cool"
                    label = "Pre-Cool"
                    fanMode = "High Fan"
                    fanDetail = "Rapid Cool"
                }
                "23:00" -> {
                    setpoint = base
                    phase = "Pre-Cool"
                    label = "Pre-Cool"
                    fanMode = "Auto Fan"
                    fanDetail = "Equilibrate"
                }
                // Deep Sleep Metabolic Drift: 00:00 - 02:00
                "00:00" -> {
                    setpoint = base + 1
                    phase = "Drift"
                    label = "Drift"
                    fanMode = "Auto Fan"
                    fanDetail = "Metabolic Sync"
                }
                "01:00" -> {
                    setpoint = base + 1
                    phase = "Drift"
                    label = "Drift"
                    fanMode = "Auto Fan"
                    fanDetail = "Deep Sleep"
                }
                "02:00" -> {
                    setpoint = base + 1
                    phase = "Drift"
                    label = "Drift"
                    fanMode = "Quiet Fan"
                    fanDetail = "Whisper"
                }
                // Ambient Trough Sync: 03:00 - 04:00
                "03:00", "04:00" -> {
                    setpoint = (base + 2).coerceAtMost(26)
                    phase = "Ambient"
                    label = "Ambient"
                    fanMode = "Quiet Fan"
                    fanDetail = "Trough Sync"
                }
                // Early Morning Thermal Coasting: 05:00 - 06:00
                "05:00" -> {
                    if (isHeavyMass) {
                        setpoint = 0
                        phase = "Coast"
                        label = "Coast"
                        fanMode = "Circulation"
                        fanDetail = "Fan Only"
                    } else {
                        setpoint = (base + 2).coerceAtMost(26)
                        phase = "Ambient"
                        label = "Ambient"
                        fanMode = "Quiet Fan"
                        fanDetail = "Trough Sync"
                    }
                }
                "06:00" -> {
                    setpoint = 0
                    phase = "Coast"
                    label = "Coast"
                    fanMode = "Circulation"
                    fanDetail = "Fan Only"
                }
                // Morning Comfort: 07:00 - 08:00
                "07:00", "08:00" -> {
                    setpoint = (base + 1).coerceAtMost(26)
                    phase = "Comfort"
                    label = "Comfort"
                    fanMode = "Auto Fan"
                    fanDetail = "Morning Fresh"
                }
                // Daytime Maintenance: 09:00 - 11:00
                "09:00", "10:00", "11:00" -> {
                    setpoint = (base + 1).coerceAtMost(26)
                    phase = "Daytime"
                    label = "Daytime"
                    fanMode = "Auto Fan"
                    fanDetail = "Ambient Sync"
                }
                // Solar Peak Heat Load Suppression: 12:00 - 15:00
                "12:00", "13:00", "14:00", "15:00" -> {
                    setpoint = base
                    phase = "Solar Peak"
                    label = "Solar Peak"
                    fanMode = "High Fan"
                    fanDetail = "Solar Defense"
                }
                // Afternoon Transition: 16:00
                "16:00" -> {
                    setpoint = (base + 1).coerceAtMost(26)
                    phase = "Afternoon"
                    label = "Afternoon"
                    fanMode = "Auto Fan"
                    fanDetail = "Thermal Balance"
                }
                // Regional Grid Peak Demand Eco Throttling: 17:00 - 20:00
                "17:00", "18:00", "19:00", "20:00" -> {
                    setpoint = (base + 1).coerceAtMost(26)
                    phase = "Grid Eco"
                    label = "Grid Eco"
                    fanMode = "Eco Fan"
                    fanDetail = "Grid Relief"
                }
                else -> {
                    setpoint = base
                    phase = "Comfort"
                    label = "Comfort"
                    fanMode = "Auto Fan"
                    fanDetail = "Equilibrate"
                }
            }

            val delta = if (setpoint > 0) {
                val diff = setpoint - outdoor
                if (diff >= 0) "+$diff°C" else "$diff°C"
            } else {
                "Coast"
            }

            ThermalStep(
                time = timeKey,
                setpointCelsius = setpoint,
                label = label,
                phaseName = phase,
                fanMode = fanMode,
                fanDetail = fanDetail,
                outdoorTemp = outdoor,
                deltaLabel = delta,
                isActive = isNowActive,
                isCompleted = false
            )
        }
    }
}
