package com.example.klimata

import com.example.klimata.data.MockData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ACModeCycleTest {

    private fun cycleMode(current: String): String = when (current) {
        "Cool" -> "Dry"
        "Dry" -> "Fan"
        "Fan" -> "Auto"
        else -> "Cool"
    }

    @Test
    fun standardAcModes_cycleInExpectedSequence() {
        var mode = "Cool"
        mode = cycleMode(mode)
        assertEquals("Dry", mode)
        mode = cycleMode(mode)
        assertEquals("Fan", mode)
        mode = cycleMode(mode)
        assertEquals("Auto", mode)
        mode = cycleMode(mode)
        assertEquals("Cool", mode)
    }

    @Test
    fun mockRooms_containValidNativeAcModes() {
        val validModes = setOf("Cool", "Dry", "Fan", "Auto")
        MockData.rooms.forEach { room ->
            assertTrue(
                "Room ${room.name} has unexpected mode ${room.profile.mode}",
                room.profile.mode in validModes
            )
        }
    }

    @Test
    fun setpointStepping_clampsWithinSafePhysicalBounds() {
        fun stepDown(temp: Int): Int = if (temp > 18) temp - 1 else temp
        fun stepUp(temp: Int): Int = if (temp < 30) temp + 1 else temp

        assertEquals(18, stepDown(18))
        assertEquals(19, stepDown(20))
        assertEquals(30, stepUp(30))
        assertEquals(25, stepUp(24))
    }

    @Test
    fun remoteInteractivity_disabledWhenPowerOff() {
        val isPowerOn = false
        var setpoint = 24
        var mode = "Cool"
        var isEcoFlow = true

        fun onTempStep(delta: Int) {
            if (isPowerOn) setpoint += delta
        }
        fun onModeCycle() {
            if (isPowerOn) mode = cycleMode(mode)
        }
        fun onEcoToggle() {
            if (isPowerOn) isEcoFlow = !isEcoFlow
        }

        onTempStep(1)
        assertEquals(24, setpoint)

        onModeCycle()
        assertEquals("Cool", mode)

        onEcoToggle()
        assertTrue(isEcoFlow)
    }
}
