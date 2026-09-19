package com.example.klimata

import android.content.Context
import com.example.klimata.data.ir.IrBlasterService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

class IrBlasterServiceTest {

    @Test
    fun irBlasterService_instantiatesSafelyWithoutHardware() {
        val mockContext = mock(Context::class.java)
        val service = IrBlasterService(mockContext)
        assertNotNull(service)
    }

    @Test
    fun sharpPulseEncoding_generatesValidPulsePairStructure() {
        val mockContext = mock(Context::class.java)
        val service = IrBlasterService(mockContext)

        // Using reflection to test private encoder
        val encodeMethod = IrBlasterService::class.java.getDeclaredMethod(
            "encodeSharpPacket",
            Boolean::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            String::class.java,
            String::class.java,
            Boolean::class.javaPrimitiveType
        ).apply { isAccessible = true }

        val pulses = encodeMethod.invoke(service, true, 24, "Cool", "Auto", false) as IntArray

        // 13 bytes * 8 bits = 104 bits * 2 pulses = 208 pulses + 2 (header) + 1 (stop) = 211 pulses
        assertEquals(211, pulses.size)
        // Sharp header mark & space
        assertEquals(3800, pulses[0])
        assertEquals(1900, pulses[1])
        // Stop bit mark
        assertEquals(450, pulses.last())

        // Every pulse duration must be strictly positive
        assertTrue(pulses.all { it > 0 })
    }

    @Test
    fun daikinPulseEncoding_generatesValidPulseStructure() {
        val mockContext = mock(Context::class.java)
        val service = IrBlasterService(mockContext)

        val encodeMethod = IrBlasterService::class.java.getDeclaredMethod(
            "encodeDaikinPacket",
            Boolean::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            String::class.java,
            String::class.java,
            Boolean::class.javaPrimitiveType
        ).apply { isAccessible = true }

        val pulses = encodeMethod.invoke(service, true, 25, "Cool", "Auto", true) as IntArray

        // 19 bytes * 8 bits * 2 + 3 = 307 pulses
        assertEquals(307, pulses.size)
        assertEquals(3500, pulses[0])
        assertEquals(1750, pulses[1])
        assertTrue(pulses.all { it > 0 })
    }
}
