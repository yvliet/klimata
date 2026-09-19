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
        assertEquals(11, result.thermalSteps.size)
        assertTrue(result.monthlySavings.primaryValue.startsWith("Rp"))
        assertTrue(result.savingsBreakdown.avgNightlyKwhSaved > 0f)
        assertTrue(result.carbonEquivalence.treesEquivalent > 0f)
    }

    @Test
    fun generateSchedule_generatesStagedPhases() {
        val schedule = ThermalCalculationEngine.generateSchedule(
            targetTemp = 24,
            hourlyOutdoorTemps = mapOf(
                "21:00" to 30, "22:00" to 29, "23:00" to 28, "00:00" to 28,
                "01:00" to 27, "02:00" to 26, "03:00" to 25, "04:00" to 25,
                "05:00" to 24, "06:00" to 24, "07:00" to 25
            ),
            thermalMassType = "Medium",
            currentHour = 22
        )

        assertEquals(11, schedule.size)

        // 22:00 Pre-Cool at target setpoint
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
}
