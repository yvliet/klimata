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
        dispatchMethod: String = "IR Blaster",
        hourlyOutdoorTemps: Map<String, Int> = emptyMap(),
        weatherCondition: String = "Clear Night"
    ): RoomState {
        val id = "room_${UUID.randomUUID().toString().take(8)}"
        val cleanName = name.trim().ifEmpty { "Bedroom" }

        val computation = com.example.klimata.data.engine.ThermalCalculationEngine.computeRoomThermalDynamics(
            areaSquareMeters = areaSquareMeters,
            ceilingHeightMeters = ceilingHeightMeters,
            thermalMassType = thermalMassType,
            acCapacity = acCapacity,
            acInverterType = acInverterType,
            targetTemp = targetTemp,
            hourlyOutdoorTemps = hourlyOutdoorTemps
        )

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
            weatherCondition = weatherCondition,
            thermalSteps = computation.thermalSteps,
            dispatchState = dispatch,
            monthlySavings = computation.monthlySavings,
            avoidedCarbon = computation.avoidedCarbon,
            savingsHistory = computation.savingsHistory,
            carbonHistory = computation.carbonHistory,
            savingsBreakdown = computation.savingsBreakdown,
            carbonEquivalence = computation.carbonEquivalence,
            areaSquareMeters = areaSquareMeters,
            ceilingHeightMeters = ceilingHeightMeters,
            thermalMassLabel = computation.thermalMassLabel,
            coolingLoadBtu = computation.coolingLoadBtu
        )
    }

    fun generateThermalSteps(
        targetTemp: Int,
        hourlyOutdoorTemps: Map<String, Int> = emptyMap(),
        thermalMassType: String = "Medium"
    ): List<ThermalStep> {
        return com.example.klimata.data.engine.ThermalCalculationEngine.generateSchedule(
            targetTemp = targetTemp,
            hourlyOutdoorTemps = hourlyOutdoorTemps,
            thermalMassType = thermalMassType
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
