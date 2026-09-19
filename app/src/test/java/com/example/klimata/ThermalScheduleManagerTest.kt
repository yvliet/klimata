package com.example.klimata

import com.example.klimata.data.AcProfile
import com.example.klimata.data.CarbonEquivalence
import com.example.klimata.data.DispatchState
import com.example.klimata.data.ImpactMetric
import com.example.klimata.data.RoomState
import com.example.klimata.data.SavingsBreakdown
import com.example.klimata.data.ThermalStep
import com.example.klimata.data.TimeBucketedImpact
import com.example.klimata.data.ir.ThermalScheduleManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ThermalScheduleManagerTest {

    private fun createStubRoom(
        id: String = "room-1",
        isPowerOn: Boolean = true,
        isEcoEnabled: Boolean = true
    ): RoomState {
        val dummyMetric = ImpactMetric("Savings", "Rp 85.000", "-38% kWh")
        val dummyHistory = TimeBucketedImpact(dummyMetric, dummyMetric, dummyMetric, dummyMetric)
        return RoomState(
            id = id,
            name = "Master Bedroom",
            location = "South Jakarta",
            profile = AcProfile(
                roomName = "Master Bedroom",
                brand = "Sharp",
                model = "AH-XP10VXY",
                capacity = "1.0 PK",
                inverterType = "J-Tech Inverter",
                currentSetpoint = 24,
                mode = "Cool",
                fanSpeed = "Auto",
                swing = false,
                irCodeSet = "sharp_inverter_104"
            ),
            currentTemp = 24,
            targetTemp = 24,
            isPowerOn = isPowerOn,
            isEcoEnabled = isEcoEnabled,
            weatherCondition = "Clear Night",
            thermalSteps = listOf(
                ThermalStep("22:00", 24, "Pre-Cool", "Pre-Cool", "High Fan", "Rapid Cool", 27, "-3°C", false, false),
                ThermalStep("01:00", 25, "Drift", "Drift", "Auto Fan", "Metabolic Sync", 25, "0°C", false, false),
                ThermalStep("03:00", 26, "Ambient", "Ambient", "Quiet Fan", "Trough Sync", 24, "+2°C", false, false),
                ThermalStep("05:00", 0, "Coast", "Coast", "Circulation", "Fan Only", 24, "Coast", false, false)
            ),
            dispatchState = DispatchState(false, "Standby", "ConsumerIR"),
            monthlySavings = dummyMetric,
            avoidedCarbon = dummyMetric,
            savingsHistory = dummyHistory,
            carbonHistory = dummyHistory,
            savingsBreakdown = SavingsBreakdown(65, 35, 1.85f, "Rp 1.445 / kWh"),
            carbonEquivalence = CarbonEquivalence(1.4f, 138.5f, 57.2f, 0.78f),
            areaSquareMeters = 18,
            ceilingHeightMeters = 2.8f,
            thermalMassLabel = "High Thermal Inertia",
            coolingLoadBtu = 7500
        )
    }

    @Test
    fun calculateNextStepTimeMillis_advancesToUpcomingTopOfTheHour() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 22)
            set(Calendar.MINUTE, 15)
            set(Calendar.SECOND, 30)
            set(Calendar.MILLISECOND, 0)
        }
        val fromMillis = calendar.timeInMillis
        val nextStepMs = ThermalScheduleManager.calculateNextStepTimeMillis(fromMillis)

        val nextCalendar = Calendar.getInstance().apply { timeInMillis = nextStepMs }
        assertEquals(23, nextCalendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, nextCalendar.get(Calendar.MINUTE))
        assertEquals(0, nextCalendar.get(Calendar.SECOND))
        assertEquals(0, nextCalendar.get(Calendar.MILLISECOND))
    }

    @Test
    fun hasActiveAutonomousRooms_returnsTrueWhenPowerAndEcoAreActive() {
        val activeRooms = listOf(createStubRoom(isPowerOn = true, isEcoEnabled = true))
        assertTrue(ThermalScheduleManager.hasActiveAutonomousRooms(activeRooms))
    }

    @Test
    fun hasActiveAutonomousRooms_returnsFalseWhenPowerOrEcoDisabled() {
        val poweredOffRooms = listOf(createStubRoom(isPowerOn = false, isEcoEnabled = true))
        assertFalse(ThermalScheduleManager.hasActiveAutonomousRooms(poweredOffRooms))

        val ecoDisabledRooms = listOf(createStubRoom(isPowerOn = true, isEcoEnabled = false))
        assertFalse(ThermalScheduleManager.hasActiveAutonomousRooms(ecoDisabledRooms))

        val emptyRooms = emptyList<RoomState>()
        assertFalse(ThermalScheduleManager.hasActiveAutonomousRooms(emptyRooms))
    }
}
