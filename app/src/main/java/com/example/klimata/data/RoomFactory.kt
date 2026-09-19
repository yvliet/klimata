package com.example.klimata.data

import java.util.UUID
import kotlin.math.roundToInt

object RoomFactory {

    fun createRoom(
        name: String,
        areaSquareMeters: Int,
        ceilingHeightMeters: Float = 2.8f,
        thermalMassType: String = "Medium",
        acBrand: String = "Daikin",
        acModel: String = "Inverter",
        acCapacity: String = "1.0 PK",
        acInverterType: String = "Inverter",
        targetTemp: Int = 24,
        location: String = "South Jakarta",
        dispatchMethod: String = "IR Blaster"
    ): RoomState {
        val id = "room_${UUID.randomUUID().toString().take(8)}"
        val cleanName = name.trim().ifEmpty { "Bedroom" }

        val (thermalFactor, thermalLabel) = when (thermalMassType.lowercase()) {
            "light", "drywall" -> 300 to "Low Thermal Mass"
            "heavy", "concrete" -> 420 to "High Thermal Inertia"
            else -> 350 to "Medium Thermal Mass"
        }

        val coolingLoadBtu = (areaSquareMeters * thermalFactor).coerceAtLeast(3500)

        val baseKwhSaved = (areaSquareMeters * 0.085f).coerceIn(1.1f, 3.8f)
        val tariffPerKwh = 1445
        val monthlySavingsIdr = (baseKwhSaved * 30 * tariffPerKwh).roundToInt()
        val formattedMonthlySavings = "Rp " + String.format(java.util.Locale.GERMAN, "%,d", monthlySavingsIdr)
        val monthlySavingsUsd = String.format(java.util.Locale.US, "%.2f", monthlySavingsIdr / 15600.0)

        val avoidedMonthlyCo2Kg = baseKwhSaved * 30 * 0.78f
        val treesEquiv = avoidedMonthlyCo2Kg / 24.5f
        val kmAvoided = avoidedMonthlyCo2Kg * 4.05f

        val savingsMetric = ImpactMetric(
            title = "Monthly Savings",
            primaryValue = formattedMonthlySavings,
            subtitle = "\$$monthlySavingsUsd USD • -38% kWh"
        )

        val carbonMetric = ImpactMetric(
            title = "Avoided Carbon",
            primaryValue = String.format(java.util.Locale.US, "%.1f kg", avoidedMonthlyCo2Kg),
            subtitle = String.format(java.util.Locale.US, "CO₂e offset • %.1f trees equiv", treesEquiv)
        )

        val weeklySavingsIdr = (monthlySavingsIdr / 4.3).roundToInt()
        val yearlySavingsIdr = monthlySavingsIdr * 12

        val savingsHistory = TimeBucketedImpact(
            weekly = ImpactMetric(
                title = "Weekly Savings",
                primaryValue = "Rp " + String.format(java.util.Locale.GERMAN, "%,d", weeklySavingsIdr),
                subtitle = "-38% kWh"
            ),
            monthly = savingsMetric,
            yearly = ImpactMetric(
                title = "Yearly Savings",
                primaryValue = "Rp " + String.format(java.util.Locale.GERMAN, "%,d", yearlySavingsIdr),
                subtitle = "-38% kWh"
            ),
            lifetime = savingsMetric
        )

        val carbonHistory = TimeBucketedImpact(
            weekly = ImpactMetric(
                title = "Avoided Carbon",
                primaryValue = String.format(java.util.Locale.US, "%.1f kg", avoidedMonthlyCo2Kg / 4.3f),
                subtitle = "CO₂e offset"
            ),
            monthly = carbonMetric,
            yearly = ImpactMetric(
                title = "Avoided Carbon",
                primaryValue = String.format(java.util.Locale.US, "%.1f kg", avoidedMonthlyCo2Kg * 12),
                subtitle = "CO₂e offset"
            ),
            lifetime = carbonMetric
        )

        val dynamicSchedule = generateThermalSteps(targetTemp = targetTemp)

        val profile = ACProfile(
            roomName = cleanName,
            brand = acBrand,
            model = acModel,
            capacity = acCapacity,
            inverterType = acInverterType,
            currentSetpoint = targetTemp,
            mode = "Cool"
        )

        val dispatch = DispatchState(
            isAutonomous = true,
            statusLabel = "Autonomous Autopilot",
            dispatchMethod = dispatchMethod
        )

        return RoomState(
            id = id,
            name = cleanName,
            location = location,
            profile = profile,
            currentTemp = targetTemp,
            targetTemp = targetTemp,
            isPowerOn = true,
            isEcoEnabled = true,
            weatherCondition = "Clear Night",
            thermalSteps = dynamicSchedule,
            dispatchState = dispatch,
            monthlySavings = savingsMetric,
            avoidedCarbon = carbonMetric,
            savingsHistory = savingsHistory,
            carbonHistory = carbonHistory,
            savingsBreakdown = SavingsBreakdown(
                compressorCyclingPercent = 65,
                fanCoastingPercent = 35,
                avgNightlyKwhSaved = baseKwhSaved,
                localTariffPerKwh = "Rp 1.445"
            ),
            carbonEquivalence = CarbonEquivalence(
                treesEquivalent = treesEquiv,
                drivingKmAvoided = kmAvoided,
                ledHoursEquivalent = baseKwhSaved * 30 * 1.5f,
                gridEmissionFactor = 0.78f
            ),
            areaSquareMeters = areaSquareMeters,
            ceilingHeightMeters = ceilingHeightMeters,
            thermalMassLabel = thermalLabel,
            coolingLoadBtu = coolingLoadBtu
        )
    }

    private fun generateThermalSteps(targetTemp: Int): List<ThermalStep> {
        val base = targetTemp.coerceIn(20, 26)
        return listOf(
            ThermalStep(
                time = "21:00",
                setpointCelsius = base,
                label = "Pre-Cool",
                phaseName = "Pre-Cool",
                fanMode = "High Fan",
                fanDetail = "Rapid Cool",
                outdoorTemp = 30,
                deltaLabel = "-6°C"
            ),
            ThermalStep(
                time = "22:00",
                setpointCelsius = base,
                label = "Pre-Cool",
                phaseName = "Pre-Cool",
                fanMode = "High Fan",
                fanDetail = "Rapid Cool",
                outdoorTemp = 29,
                deltaLabel = "-5°C",
                isActive = true
            ),
            ThermalStep(
                time = "23:00",
                setpointCelsius = base,
                label = "Pre-Cool",
                phaseName = "Pre-Cool",
                fanMode = "Auto Fan",
                fanDetail = "Equilibrate",
                outdoorTemp = 28,
                deltaLabel = "-4°C"
            ),
            ThermalStep(
                time = "00:00",
                setpointCelsius = base + 1,
                label = "Drift",
                phaseName = "Drift",
                fanMode = "Auto Fan",
                fanDetail = "Metabolic Sync",
                outdoorTemp = 28,
                deltaLabel = "-3°C"
            ),
            ThermalStep(
                time = "01:00",
                setpointCelsius = base + 1,
                label = "Drift",
                phaseName = "Drift",
                fanMode = "Auto Fan",
                fanDetail = "Deep Sleep",
                outdoorTemp = 27,
                deltaLabel = "-2°C"
            ),
            ThermalStep(
                time = "02:00",
                setpointCelsius = base + 1,
                label = "Drift",
                phaseName = "Drift",
                fanMode = "Quiet Fan",
                fanDetail = "Whisper",
                outdoorTemp = 26,
                deltaLabel = "-1°C"
            ),
            ThermalStep(
                time = "03:00",
                setpointCelsius = base + 2,
                label = "Ambient",
                phaseName = "Ambient",
                fanMode = "Quiet Fan",
                fanDetail = "Trough Sync",
                outdoorTemp = 25,
                deltaLabel = "+1°C"
            ),
            ThermalStep(
                time = "04:00",
                setpointCelsius = base + 2,
                label = "Ambient",
                phaseName = "Ambient",
                fanMode = "Quiet Fan",
                fanDetail = "Trough Sync",
                outdoorTemp = 25,
                deltaLabel = "+1°C"
            ),
            ThermalStep(
                time = "05:00",
                setpointCelsius = base + 2,
                label = "Ambient",
                phaseName = "Ambient",
                fanMode = "Quiet Fan",
                fanDetail = "Trough Sync",
                outdoorTemp = 24,
                deltaLabel = "+2°C"
            ),
            ThermalStep(
                time = "06:00",
                setpointCelsius = 0,
                label = "Coast",
                phaseName = "Coast",
                fanMode = "Circulation",
                fanDetail = "Fan Only",
                outdoorTemp = 24,
                deltaLabel = "Coast"
            ),
            ThermalStep(
                time = "07:00",
                setpointCelsius = 0,
                label = "Coast",
                phaseName = "Coast",
                fanMode = "Circulation",
                fanDetail = "Fan Only",
                outdoorTemp = 25,
                deltaLabel = "Coast"
            )
        )
    }

    fun assessAcMatch(capacity: String, areaSquareMeters: Int): AcMatchAssessment {
        val btu = when (capacity.trim()) {
            "0.5 PK" -> 5000
            "0.75 PK" -> 7000
            "1.0 PK" -> 9000
            "1.5 PK" -> 12000
            "2.0 PK" -> 18000
            else -> 9000
        }
        val targetBtu = areaSquareMeters * 350
        return when {
            btu >= targetBtu - 1500 && btu <= targetBtu + 2500 -> {
                AcMatchAssessment(
                    status = "Optimal Match",
                    description = "$capacity produces ~$btu BTU/h, ideal for your $areaSquareMeters m² room.",
                    isOptimal = true
                )
            }
            btu < targetBtu - 1500 -> {
                AcMatchAssessment(
                    status = "May Need More Headroom",
                    description = "$capacity (~$btu BTU/h) might run continuously in a $areaSquareMeters m² space during hot weather.",
                    isOptimal = false
                )
            }
            else -> {
                AcMatchAssessment(
                    status = "Generous Capacity",
                    description = "$capacity (~$btu BTU/h) cools your $areaSquareMeters m² room quickly with low strain.",
                    isOptimal = true
                )
            }
        }
    }
}

data class AcMatchAssessment(
    val status: String,
    val description: String,
    val isOptimal: Boolean
)
