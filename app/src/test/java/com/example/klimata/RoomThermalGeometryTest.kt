package com.example.klimata

import com.example.klimata.data.RoomFactory
import com.example.klimata.data.volumeCubicMeters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomThermalGeometryTest {

    @Test
    fun masterBedRoom_volumeMatchesExpectedDimensions() {
        val room = RoomFactory.createRoom("Master Bed", 20, 2.8f)
        assertEquals(20, room.areaSquareMeters)
        assertEquals(2.8f, room.ceilingHeightMeters, 0.01f)
        assertEquals(56, room.volumeCubicMeters)
    }

    @Test
    fun livingRoom_volumeMatchesExpectedDimensions() {
        val room = RoomFactory.createRoom("Living Room", 35, 3.0f)
        assertEquals(35, room.areaSquareMeters)
        assertEquals(3.0f, room.ceilingHeightMeters, 0.01f)
        assertEquals(105, room.volumeCubicMeters)
    }

    @Test
    fun studyRoom_volumeMatchesExpectedDimensions() {
        val room = RoomFactory.createRoom("Study", 14, 2.7f)
        assertEquals(14, room.areaSquareMeters)
        assertEquals(2.7f, room.ceilingHeightMeters, 0.01f)
        assertEquals(38, room.volumeCubicMeters)
    }

    @Test
    fun allRooms_havePositiveCoolingLoad() {
        val rooms = listOf(
            RoomFactory.createRoom("Master Bed", 20, 2.8f),
            RoomFactory.createRoom("Living Room", 35, 3.0f),
            RoomFactory.createRoom("Study", 14, 2.7f)
        )
        rooms.forEach { room ->
            assertTrue(room.coolingLoadBtu > 0)
            assertTrue(room.areaSquareMeters > 0)
            assertTrue(room.ceilingHeightMeters > 0f)
            assertTrue(room.volumeCubicMeters > 0)
        }
    }
}
