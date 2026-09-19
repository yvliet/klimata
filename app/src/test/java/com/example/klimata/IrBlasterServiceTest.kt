package com.example.klimata

import android.content.ContextWrapper
import com.example.klimata.data.ir.IrBlasterService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IrBlasterServiceTest {

    private fun createTestService(): IrBlasterService {
        val dummyContext = ContextWrapper(null)
        return IrBlasterService(dummyContext)
    }

    @Test
    fun irBlasterService_instantiatesSafelyWithoutHardware() {
        val service = createTestService()
        assertNotNull(service)
    }

    @Test
    fun sharpInverter104_generatesExactPulseStructure() {
        val service = createTestService()
        val pulses = service.encodeSharpInverter104(
            power = true,
            temp = 24,
            mode = "Cool",
            fanSpeed = "Auto",
            isEco = false
        )

        // 13 bytes * 8 bits * 2 = 208 pulses + header(2) + stop(1) + trailing gap(1) = 212 pulses
        assertEquals(212, pulses.size)
        // Header mark & space
        assertEquals(3800, pulses[0])
        assertEquals(1900, pulses[1])
        // Stop mark & 40ms trailing lead-out space
        assertEquals(470, pulses[pulses.size - 2])
        assertEquals(40000, pulses.last())

        // Every pulse duration must be strictly positive
        assertTrue(pulses.all { it > 0 })
    }

    @Test
    fun sharpChecksum_matchesHardwareTestVector() {
        val service = createTestService()
        // Captured Sharp hardware frame from real remote:
        // AA 5A CF 10 D1 11 22 00 08 80 00 F0 F1
        val testFrame = intArrayOf(
            0xAA, 0x5A, 0xCF, 0x10, 0xD1, 0x11, 0x22, 0x00, 0x08, 0x80, 0x00, 0xF0, 0x01
        )
        val calculatedChecksum = service.calcSharpChecksum(testFrame)
        assertEquals(0x0F, calculatedChecksum)

        val finalizedByte12 = (calculatedChecksum shl 4) or (testFrame[12] and 0x0F)
        assertEquals(0xF1, finalizedByte12)
    }

    @Test
    fun sharpGreeOem_generatesDualBlockStructureWith20msGap() {
        val service = createTestService()
        val pulses = service.encodeGreePacket(
            power = true,
            temp = 24,
            mode = "Cool",
            fanSpeed = "Auto",
            isEco = false
        )

        // Header(2) + Block 1(64) + Footer 1(6) + Mid-gap(2) + Block 2(64) + Stop & Lead-out(2) = 140 pulses
        assertEquals(140, pulses.size)
        // Gree Header
        assertEquals(9000, pulses[0])
        assertEquals(4500, pulses[1])
        // Mid-frame pause of 20ms
        assertEquals(20000, pulses[73])
        // Trailing lead-out silence of 40ms
        assertEquals(40000, pulses.last())

        assertTrue(pulses.all { it > 0 })
    }

    @Test
    fun sharpCodeSets_providesComprehensiveCoverage() {
        val service = createTestService()
        val codeSets = service.getCodeSetsForBrand("Sharp")

        assertTrue(codeSets.size >= 4)
        val ids = codeSets.map { it.id }
        assertTrue(ids.contains("sharp_inverter_104"))
        assertTrue(ids.contains("sharp_gree_oem"))
        assertTrue(ids.contains("sharp_crmc_a705"))
        assertTrue(ids.contains("sharp_aux_oem"))
    }

    @Test
    fun sharpDefaultResolution_mapsUcySeriesToGreeOem() {
        val service = createTestService()
        assertEquals("sharp_gree_oem", service.getDefaultCodeSetForModel("Sharp", "AH-A9UCY"))
        assertEquals("sharp_gree_oem", service.getDefaultCodeSetForModel("Sharp", "AH-A5UCY"))
        assertEquals("sharp_gree_oem", service.getDefaultCodeSetForModel("Sharp", "Sayonara Panas"))
        assertEquals("sharp_inverter_104", service.getDefaultCodeSetForModel("Sharp", "AH-XP10VXY"))
        assertEquals("sharp_crmc_a705", service.getDefaultCodeSetForModel("Sharp", "CRMC-A705"))
    }

    @Test
    fun daikinPulseEncoding_generatesValidPulseStructure() {
        val service = createTestService()
        val pulses = service.encodeDaikinPacket(power = true, temp = 25, mode = "Cool", fanSpeed = "Auto", isEco = true)

        // 19 bytes * 8 bits * 2 + header(2) + stop(1) + trailing gap(1) = 308 pulses
        assertEquals(308, pulses.size)
        assertEquals(3500, pulses[0])
        assertEquals(1750, pulses[1])
        assertEquals(40000, pulses.last())
        assertTrue(pulses.all { it > 0 })
    }

    @Test
    fun lgPulseEncoding_generatesValidPulseStructure() {
        val service = createTestService()
        val pulses = service.encodeLgPacket(power = true, temp = 24, mode = "Cool", fanSpeed = "Auto", isEco = false)

        // 4 bytes * 8 bits * 2 + header(2) + stop(1) + trailing gap(1) = 68 pulses
        assertEquals(68, pulses.size)
        assertEquals(8500, pulses[0])
        assertEquals(4200, pulses[1])
        assertEquals(40000, pulses.last())
        assertTrue(pulses.all { it > 0 })
    }

    @Test
    fun samsungPulseEncoding_generatesValidPulseStructure() {
        val service = createTestService()
        val pulses = service.encodeSamsungPacket(power = true, temp = 24, mode = "Cool", fanSpeed = "Auto", isEco = false)

        // 7 bytes * 8 bits * 2 + header(2) + stop(1) + trailing gap(1) = 116 pulses
        assertEquals(116, pulses.size)
        assertEquals(3000, pulses[0])
        assertEquals(9000, pulses[1])
        assertEquals(40000, pulses.last())
        assertTrue(pulses.all { it > 0 })
    }

    @Test
    fun mideaPulseEncoding_generatesValidPulseStructure() {
        val service = createTestService()
        val pulses = service.encodeMideaPacket(power = true, temp = 24, mode = "Cool", fanSpeed = "Auto", isEco = false)

        // 6 bytes * 8 bits * 2 + header(2) + stop(1) + trailing gap(1) = 100 pulses
        assertEquals(100, pulses.size)
        assertEquals(4400, pulses[0])
        assertEquals(4400, pulses[1])
        assertEquals(40000, pulses.last())
        assertTrue(pulses.all { it > 0 })
    }

    @Test
    fun fanSpeedsAndSwing_encodeProperlyAcrossProtocols() {
        val service = createTestService()

        // 1. Sharp Inverter: Test Swing toggle
        val sharpSwingOn = service.encodeSharpInverter104(
            power = true, temp = 24, mode = "Cool", fanSpeed = "High", isEco = false, swing = true
        )
        val sharpSwingOff = service.encodeSharpInverter104(
            power = true, temp = 24, mode = "Cool", fanSpeed = "High", isEco = false, swing = false
        )
        assertEquals(212, sharpSwingOn.size)
        assertEquals(212, sharpSwingOff.size)

        // 2. Gree OEM: Test Swing and Turbo
        val greeTurboSwing = service.encodeGreePacket(
            power = true, temp = 24, mode = "Cool", fanSpeed = "Turbo", isEco = false, swing = true
        )
        val greeQuietFixed = service.encodeGreePacket(
            power = true, temp = 24, mode = "Cool", fanSpeed = "Quiet", isEco = false, swing = false
        )
        assertEquals(140, greeTurboSwing.size)
        assertEquals(140, greeQuietFixed.size)

        // 3. Daikin: Test Fan mode and Swing
        val daikinFanMode = service.encodeDaikinPacket(
            power = true, temp = 24, mode = "Fan", fanSpeed = "Med", isEco = false, swing = true
        )
        assertEquals(308, daikinFanMode.size)
    }
}
