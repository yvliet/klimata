package com.example.klimata

import com.example.klimata.data.engine.ThermalCalculationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ThermalCalculationEngineTest {

    @Test
    fun computeRoomThermalDynamics_calculatesThermodynamicLoad() {
        val result = ThermalCalculationEngine.computeRoomThermalDynamics(
            areaSquareMeters = 20,
            ceilingHeightMeters = 2.8f,
            thermalMassType = "Medium",
            acCapacity = "1.0 PK",
            acInverterType = "Eco Inverter",
            targetTemp = 24
        )

        assertEquals(7000, result.coolingLoadBtu)
        assertEquals("Medium Thermal Mass", result.thermalMassLabel)
        assertEquals(24, result.thermalSteps.size)
        assertTrue(result.monthlySavings.primaryValue.startsWith("Rp"))
        assertTrue(result.savingsBreakdown.avgNightlyKwhSaved > 0f)
        assertTrue(result.carbonEquivalence.treesEquivalent > 0f)
    }

    @Test
    fun generateSchedule_generatesStagedPhases() {
        val schedule = ThermalCalculationEngine.generateSchedule(
            targetTemp = 24,
            hourlyOutdoorTemps = mapOf(
                "12:00" to 34, "13:00" to 34, "14:00" to 33,
                "18:00" to 30, "19:00" to 29,
                "21:00" to 30, "22:00" to 29, "23:00" to 28, "00:00" to 28,
                "01:00" to 27, "02:00" to 26, "03:00" to 25, "04:00" to 25,
                "05:00" to 24, "06:00" to 24, "07:00" to 25, "08:00" to 27
            ),
            thermalMassType = "Medium",
            currentHour = 22
        )

        assertEquals(24, schedule.size)

        // 22:00 Pre-Cool at target setpoint (first element since currentHour = 22)
        val precool = schedule.first { it.time == "22:00" }
        assertEquals(24, precool.setpointCelsius)
        assertEquals("Pre-Cool", precool.phaseName)
        assertTrue(precool.isActive)

        // 01:00 Metabolic Drift at +1°C
        val drift = schedule.first { it.time == "01:00" }
        assertEquals(25, drift.setpointCelsius)
        assertEquals("Drift", drift.phaseName)

        // 03:00 Ambient Crest at +2°C
        val ambient = schedule.first { it.time == "03:00" }
        assertEquals(26, ambient.setpointCelsius)
        assertEquals("Ambient", ambient.phaseName)

        // 06:00 Thermal Coasting (compressor cut to 0)
        val coast = schedule.first { it.time == "06:00" }
        assertEquals(0, coast.setpointCelsius)
        assertEquals("Coast", coast.phaseName)
        assertEquals("Circulation", coast.fanMode)
        assertEquals("Coast", coast.deltaLabel)

        // Daytime Solar Peak at 13:00
        val solarPeak = schedule.first { it.time == "13:00" }
        assertEquals(24, solarPeak.setpointCelsius)
        assertEquals("Solar Peak", solarPeak.phaseName)

        // Grid Peak Eco at 18:00
        val gridEco = schedule.first { it.time == "18:00" }
        assertEquals(25, gridEco.setpointCelsius)
        assertEquals("Grid Eco", gridEco.phaseName)

        // Morning Comfort at 08:00
        val comfort = schedule.first { it.time == "08:00" }
        assertEquals(25, comfort.setpointCelsius)
        assertEquals("Comfort", comfort.phaseName)
    }

    @Test
    fun generateSchedule_accuratelyRepresentsCurrentTimeInAfternoon() {
        val schedule = ThermalCalculationEngine.generateSchedule(
            targetTemp = 24,
            currentHour = 14
        )

        assertEquals(24, schedule.size)
        // First element is the current hour (14:00)
        val firstStep = schedule.first()
        assertEquals("14:00", firstStep.time)
        assertTrue(firstStep.isActive)
        assertEquals("Solar Peak", firstStep.phaseName)

        // Other hours are not active
        val secondStep = schedule[1]
        assertEquals("15:00", secondStep.time)
        assertTrue(!secondStep.isActive)
    }

    @Test
    fun heavyThermalMass_initiatesCoastingEarlier() {
        val mediumSchedule = ThermalCalculationEngine.generateSchedule(
            targetTemp = 24,
            thermalMassType = "Medium"
        )
        val heavySchedule = ThermalCalculationEngine.generateSchedule(
            targetTemp = 24,
            thermalMassType = "Heavy"
        )

        // Medium mass AC runs at 05:00, heavy concrete coasts starting at 05:00
        val medium05 = mediumSchedule.first { it.time == "05:00" }
        val heavy05 = heavySchedule.first { it.time == "05:00" }

        assertEquals(26, medium05.setpointCelsius)
        assertEquals(0, heavy05.setpointCelsius)
        assertEquals("Coast", heavy05.phaseName)
    }

    @Test
    fun savingsAccounting_scalesWithAcCapacity() {
        val smallAc = ThermalCalculationEngine.computeRoomThermalDynamics(
            areaSquareMeters = 12,
            ceilingHeightMeters = 2.8f,
            thermalMassType = "Light",
            acCapacity = "0.5 PK",
            acInverterType = "Inverter",
            targetTemp = 24
        )

        val largeAc = ThermalCalculationEngine.computeRoomThermalDynamics(
            areaSquareMeters = 35,
            ceilingHeightMeters = 3.0f,
            thermalMassType = "Heavy",
            acCapacity = "2.0 PK",
            acInverterType = "Inverter",
            targetTemp = 24
        )

        assertTrue(largeAc.savingsBreakdown.avgNightlyKwhSaved > smallAc.savingsBreakdown.avgNightlyKwhSaved)
        assertTrue(largeAc.carbonEquivalence.treesEquivalent > smallAc.carbonEquivalence.treesEquivalent)
    }

    @Test
    fun energyConfig_formatsAccordingToRegion() {
        val usConfig = com.example.klimata.data.EnergyConfig.UNITED_STATES
        val usResult = ThermalCalculationEngine.computeRoomThermalDynamics(
            areaSquareMeters = 20,
            ceilingHeightMeters = 2.8f,
            thermalMassType = "Medium",
            acCapacity = "1.0 PK",
            acInverterType = "Inverter",
            targetTemp = 24,
            energyConfig = usConfig
        )

        assertTrue(usResult.monthlySavings.primaryValue.startsWith("$"))
        assertTrue(usResult.savingsBreakdown.localTariffPerKwh.startsWith("$0.16"))
        assertEquals(0.38f, usResult.carbonEquivalence.gridEmissionFactor)
    }

    @Test
    fun computeGridStatus_evaluatesDiurnalAndThermalStress() {
        assertEquals("Grid High Stress | Peak Cooling", ThermalCalculationEngine.computeGridStatus(hour = 14, outdoorTemp = 34))
        assertEquals("Grid Peak Demand", ThermalCalculationEngine.computeGridStatus(hour = 19, outdoorTemp = 29))
        assertEquals("Grid Off-Peak | Eco Window", ThermalCalculationEngine.computeGridStatus(hour = 23, outdoorTemp = 27))
        assertEquals("Grid Off-Peak | Eco Window", ThermalCalculationEngine.computeGridStatus(hour = 3, outdoorTemp = 25))
        assertEquals("Grid Normal", ThermalCalculationEngine.computeGridStatus(hour = 11, outdoorTemp = 29))
    }
}
