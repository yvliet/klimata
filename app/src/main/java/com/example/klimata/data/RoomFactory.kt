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
        weatherCondition: String = "Clear Night",
        irCodeSet: String? = null
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

        val resolvedCodeSet = irCodeSet ?: resolveDefaultCodeSet(acBrand, acModel)

        val profile = ACProfile(
            roomName = cleanName,
            brand = acBrand,
            model = acModel,
            capacity = acCapacity,
            inverterType = acInverterType,
            currentSetpoint = targetTemp,
            mode = "Cool",
            irCodeSet = resolvedCodeSet
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

    fun resolveDefaultCodeSet(brand: String, modelCode: String?): String {
        val cleanBrand = brand.lowercase(java.util.Locale.ROOT)
        val m = modelCode?.lowercase(java.util.Locale.ROOT) ?: ""
        return when {
            cleanBrand.contains("sharp") -> when {
                m.contains("ucy") || m.contains("say") || m.contains("turbo") || m.contains("yb0f") -> "sharp_gree_oem"
                m.contains("crmc") || m.contains("a705") || m.contains("a820") || m.contains("pr13") -> "sharp_crmc_a705"
                m.contains("aux") || m.contains("ncy") -> "sharp_aux_oem"
                else -> "sharp_inverter_104"
            }
            cleanBrand.contains("daikin") -> if (m.contains("ftne") || m.contains("ftv") || m.contains("480")) "daikin_arc480" else "daikin_arc433"
            cleanBrand.contains("panasonic") -> if (m.contains("ckp") || m.contains("pc")) "panasonic_ckp" else "panasonic_dke"
            cleanBrand.contains("gree") || cleanBrand.contains("tcl") || cleanBrand.contains("aqua") -> "gree_yb0f2"
            cleanBrand.contains("mitsubishi") -> if (m.contains("heavy") || m.contains("srk")) "mitsubishi_heavy" else "mitsubishi_fd"
            cleanBrand.contains("lg") -> "lg_28bit"
            cleanBrand.contains("samsung") -> "samsung_14byte"
            cleanBrand.contains("midea") || cleanBrand.contains("toshiba") || cleanBrand.contains("carrier") -> "midea_r05"
            else -> "sharp_inverter_104"
        }
    }
}

data class AcMatchAssessment(
    val status: String,
    val description: String,
    val isOptimal: Boolean
)
