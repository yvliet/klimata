package com.example.klimata

import com.example.klimata.data.MockData
import com.example.klimata.data.volumeCubicMeters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomThermalGeometryTest {

    @Test
    fun masterBedRoom_volumeMatchesExpectedDimensions() {
        val room = MockData.masterBedRoom
        assertEquals(20, room.areaSquareMeters)
        assertEquals(2.8f, room.ceilingHeightMeters, 0.01f)
        assertEquals(56, room.volumeCubicMeters)
    }

    @Test
    fun livingRoom_volumeMatchesExpectedDimensions() {
        val room = MockData.livingRoom
        assertEquals(35, room.areaSquareMeters)
        assertEquals(3.0f, room.ceilingHeightMeters, 0.01f)
        assertEquals(105, room.volumeCubicMeters)
    }

    @Test
    fun studyRoom_volumeMatchesExpectedDimensions() {
        val room = MockData.studyRoom
        assertEquals(14, room.areaSquareMeters)
        assertEquals(2.7f, room.ceilingHeightMeters, 0.01f)
        assertEquals(38, room.volumeCubicMeters)
    }

    @Test
    fun allRooms_havePositiveCoolingLoad() {
        MockData.rooms.forEach { room ->
            assertTrue(room.coolingLoadBtu > 0)
            assertTrue(room.areaSquareMeters > 0)
            assertTrue(room.ceilingHeightMeters > 0f)
            assertTrue(room.volumeCubicMeters > 0)
        }
    }
}
