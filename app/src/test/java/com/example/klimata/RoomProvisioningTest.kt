package com.example.klimata

import com.example.klimata.data.RoomFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomProvisioningTest {

    @Test
    fun createRoom_calculatesVolumeAndCoolingLoad() {
        val room = RoomFactory.createRoom(
            name = "Guest Room",
            areaSquareMeters = 20,
            ceilingHeightMeters = 2.8f,
            thermalMassType = "Medium",
            acBrand = "Daikin",
            acCapacity = "1.0 PK"
        )

        assertEquals("Guest Room", room.name)
        assertEquals(20, room.areaSquareMeters)
        assertEquals(56, (room.areaSquareMeters * room.ceilingHeightMeters).toInt())
        assertEquals(7000, room.coolingLoadBtu)
        assertEquals(11, room.thermalSteps.size)
        assertTrue(room.monthlySavings.primaryValue.startsWith("Rp"))
    }

    @Test
    fun assessAcMatch_identifiesOptimalCapacity() {
        val optimal = RoomFactory.assessAcMatch(capacity = "1.0 PK", areaSquareMeters = 20)
        assertTrue(optimal.isOptimal)
        assertEquals("Optimal Match", optimal.status)

        val undersized = RoomFactory.assessAcMatch(capacity = "0.5 PK", areaSquareMeters = 35)
        assertTrue(!undersized.isOptimal)
    }
}
