package com.example.klimata.data.engine

import com.example.klimata.data.CarbonEquivalence
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

    private val STANDARD_NOCTURNAL_HOURS = listOf(
        "21:00", "22:00", "23:00", "00:00", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00"
    )

    fun computeRoomThermalDynamics(
        areaSquareMeters: Int,
        ceilingHeightMeters: Float,
        thermalMassType: String,
        acCapacity: String,
        acInverterType: String,
        targetTemp: Int,
        hourlyOutdoorTemps: Map<String, Int> = emptyMap(),
        tariffPerKwh: Int = 1445,
        gridEmissionFactor: Float = 0.78f
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

        val monthlySavingsIdr = (baseKwhSaved * 30 * tariffPerKwh).roundToInt()
        val formattedMonthlyIdr = "Rp " + String.format(Locale.GERMAN, "%,d", monthlySavingsIdr)
        val monthlySavingsUsd = String.format(Locale.US, "%.2f", monthlySavingsIdr / 15600.0)

        val avoidedMonthlyCo2Kg = baseKwhSaved * 30 * gridEmissionFactor
        val treesEquiv = avoidedMonthlyCo2Kg / 24.5f
        val kmAvoided = avoidedMonthlyCo2Kg * 4.05f

        val savingsMetric = ImpactMetric(
            title = "Monthly Savings",
            primaryValue = formattedMonthlyIdr,
            subtitle = "\$$monthlySavingsUsd USD / -38% kWh"
        )

        val carbonMetric = ImpactMetric(
            title = "Avoided Carbon",
            primaryValue = String.format(Locale.US, "%.1f kg", avoidedMonthlyCo2Kg),
            subtitle = String.format(Locale.US, "CO₂e offset / %.1f trees equiv", treesEquiv)
        )

        val weeklySavingsIdr = (monthlySavingsIdr / 4.3).roundToInt()
        val yearlySavingsIdr = monthlySavingsIdr * 12

        val savingsHistory = TimeBucketedImpact(
            weekly = ImpactMetric(
                title = "Weekly Savings",
                primaryValue = "Rp " + String.format(Locale.GERMAN, "%,d", weeklySavingsIdr),
                subtitle = "-38% kWh"
            ),
            monthly = savingsMetric,
            yearly = ImpactMetric(
                title = "Yearly Savings",
                primaryValue = "Rp " + String.format(Locale.GERMAN, "%,d", yearlySavingsIdr),
                subtitle = "-38% kWh"
            ),
            lifetime = savingsMetric
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
            lifetime = carbonMetric
        )

        val steps = generateSchedule(
            targetTemp = targetTemp,
            hourlyOutdoorTemps = hourlyOutdoorTemps,
            thermalMassType = thermalMassType
        )

        val coastingPercentage = ((coastingHours / 10f) * 100f).roundToInt().coerceIn(25, 45)
        val cyclingPercentage = 100 - coastingPercentage

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
                localTariffPerKwh = "Rp " + String.format(Locale.GERMAN, "%,d", tariffPerKwh)
            ),
            carbonEquivalence = CarbonEquivalence(
                treesEquivalent = treesEquiv,
                drivingKmAvoided = kmAvoided,
                ledHoursEquivalent = baseKwhSaved * 30 * 1.5f,
                gridEmissionFactor = gridEmissionFactor
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
            "21:00" to 30, "22:00" to 29, "23:00" to 28, "00:00" to 28,
            "01:00" to 27, "02:00" to 26, "03:00" to 25, "04:00" to 25,
            "05:00" to 24, "06:00" to 24, "07:00" to 25
        )

        return STANDARD_NOCTURNAL_HOURS.map { timeKey ->
            val outdoor = hourlyOutdoorTemps[timeKey] ?: fallbackTrajectory[timeKey] ?: 26
            val hourInt = timeKey.substring(0, 2).toInt()

            val isNowActive = (currentHour == hourInt) ||
                    (currentHour !in 21..23 && currentHour !in 0..7 && hourInt == 22)

            val (setpoint, phase, label, fanMode, fanDetail) = when (timeKey) {
                "21:00" -> Quintuple(base, "Pre-Cool", "Pre-Cool", "High Fan", "Rapid Cool")
                "22:00" -> Quintuple(base, "Pre-Cool", "Pre-Cool", "High Fan", "Rapid Cool")
                "23:00" -> Quintuple(base, "Pre-Cool", "Pre-Cool", "Auto Fan", "Equilibrate")
                "00:00" -> Quintuple(base + 1, "Drift", "Drift", "Auto Fan", "Metabolic Sync")
                "01:00" -> Quintuple(base + 1, "Drift", "Drift", "Auto Fan", "Deep Sleep")
                "02:00" -> Quintuple(base + 1, "Drift", "Drift", "Quiet Fan", "Whisper")
                "03:00" -> Quintuple((base + 2).coerceAtMost(26), "Ambient", "Ambient", "Quiet Fan", "Trough Sync")
                "04:00" -> Quintuple((base + 2).coerceAtMost(26), "Ambient", "Ambient", "Quiet Fan", "Trough Sync")
                "05:00" -> {
                    if (isHeavyMass) {
                        Quintuple(0, "Coast", "Coast", "Circulation", "Fan Only")
                    } else {
                        Quintuple((base + 2).coerceAtMost(26), "Ambient", "Ambient", "Quiet Fan", "Trough Sync")
                    }
                }
                "06:00" -> Quintuple(0, "Coast", "Coast", "Circulation", "Fan Only")
                "07:00" -> Quintuple(0, "Coast", "Coast", "Circulation", "Fan Only")
                else -> Quintuple(base, "Pre-Cool", "Pre-Cool", "Auto Fan", "Equilibrate")
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

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
