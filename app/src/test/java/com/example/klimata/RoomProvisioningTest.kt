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

    @Test
    fun shoelaceArea_calculatesRectangularAndLShapedRooms() {
        // Rectangle: 4m x 5m = 20 m²
        val rectPts = listOf(
            androidx.compose.ui.geometry.Offset(0f, 0f),
            androidx.compose.ui.geometry.Offset(4f, 0f),
            androidx.compose.ui.geometry.Offset(4f, 5f),
            androidx.compose.ui.geometry.Offset(0f, 5f)
        )
        var rectSum = 0f
        for (i in rectPts.indices) {
            val p1 = rectPts[i]
            val p2 = rectPts[(i + 1) % rectPts.size]
            rectSum += (p1.x * p2.y - p2.x * p1.y)
        }
        val rectArea = kotlin.math.abs(rectSum / 2f)
        assertEquals(20f, rectArea, 0.01f)

        // L-shaped room: 6 vertices (e.g. 5x5 outer minus 2x3 cutout = 25 - 6 = 19 m²)
        val lShapePts = listOf(
            androidx.compose.ui.geometry.Offset(0f, 0f),
            androidx.compose.ui.geometry.Offset(5f, 0f),
            androidx.compose.ui.geometry.Offset(5f, 2f),
            androidx.compose.ui.geometry.Offset(3f, 2f),
            androidx.compose.ui.geometry.Offset(3f, 5f),
            androidx.compose.ui.geometry.Offset(0f, 5f)
        )
        var lSum = 0f
        for (i in lShapePts.indices) {
            val p1 = lShapePts[i]
            val p2 = lShapePts[(i + 1) % lShapePts.size]
            lSum += (p1.x * p2.y - p2.x * p1.y)
        }
        val lArea = kotlin.math.abs(lSum / 2f)
        assertEquals(19f, lArea, 0.01f)
    }
}
