package com.example.klimata

import com.example.klimata.data.models.AcDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AcDatabaseTest {

    @Test
    fun acDatabase_hasExtensiveCatalog() {
        val models = AcDatabase.allModels
        assertTrue("Catalog should contain at least 25 models", models.size >= 25)

        for (m in models) {
            assertTrue("Brand must not be blank", m.brand.isNotBlank())
            assertTrue("Model code must not be blank", m.modelCode.isNotBlank())
            assertTrue("Default capacity must be valid", m.defaultCapacity.contains("PK"))
            assertTrue("Inverter type must not be blank", m.inverterType.isNotBlank())
            assertTrue("IR protocol must not be blank", m.irProtocol.isNotBlank())
            assertTrue("Supported capacities must not be empty", m.supportedCapacities.isNotEmpty())
        }
    }

    @Test
    fun acDatabase_brandFilter_returnsCorrectSubset() {
        val sharpModels = AcDatabase.filter(brand = "Sharp")
        assertTrue("Sharp models must not be empty", sharpModels.isNotEmpty())
        assertTrue("All filtered models must be Sharp", sharpModels.all { it.brand == "Sharp" })

        val daikinModels = AcDatabase.filter(brand = "Daikin")
        assertTrue("Daikin models must not be empty", daikinModels.isNotEmpty())
        assertTrue("All filtered models must be Daikin", daikinModels.all { it.brand == "Daikin" })

        val lgModels = AcDatabase.filter(brand = "LG")
        assertTrue("LG models must not be empty", lgModels.isNotEmpty())
        assertTrue("All filtered models must be LG", lgModels.all { it.brand == "LG" })
    }

    @Test
    fun acDatabase_textSearch_findsByCodeOrFeature() {
        val ftkfMatches = AcDatabase.filter(query = "FTKF")
        assertTrue("FTKF search must match Daikin", ftkfMatches.any { it.modelCode.contains("FTKF") })

        val plasmaMatches = AcDatabase.filter(query = "Plasmacluster")
        assertTrue("Plasmacluster search must match Sharp", plasmaMatches.any { it.brand == "Sharp" })

        val nanoeMatches = AcDatabase.filter(query = "nanoe")
        assertTrue("nanoe search must match Panasonic", nanoeMatches.any { it.brand == "Panasonic" })
    }

    @Test
    fun acDatabase_bestMatch_findsTargetUnit() {
        val matchSharp = AcDatabase.findBestMatch("Sharp", "AH-XP10VXY")
        assertNotNull(matchSharp)
        assertEquals("Sharp", matchSharp?.brand)
        assertEquals("AH-XP10VXY", matchSharp?.modelCode)

        val matchDaikin = AcDatabase.findBestMatch("Daikin", "FTKF25")
        assertNotNull(matchDaikin)
        assertEquals("Daikin", matchDaikin?.brand)
        assertEquals("FTKF25", matchDaikin?.modelCode)

        val partialSharp = AcDatabase.findBestMatch("Sharp", "AH-A9")
        assertNotNull(partialSharp)
        assertEquals("Sharp", partialSharp?.brand)
    }
}
