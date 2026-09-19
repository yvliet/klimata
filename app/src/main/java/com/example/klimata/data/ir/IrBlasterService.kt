package com.example.klimata.data.ir

import android.content.Context
import android.hardware.ConsumerIrManager
import android.util.Log
import java.util.Locale

/**
 * Autonomous Hardware Infrared Dispatch Engine.
 *
 * Interfaces with Android's [ConsumerIrManager] to pulse 38 kHz infrared packets
 * through built-in phone IR blasters (e.g. Xiaomi, POCO, Redmi, Huawei).
 * Encodes vendor-specific AC telemetry frames for Sharp, Daikin, Panasonic,
 * Gree, Mitsubishi Electric, and generic NEC-modulated units.
 */
class IrBlasterService(private val context: Context) {

    private val irManager: ConsumerIrManager? by lazy {
        try {
            context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
        } catch (e: Exception) {
            Log.w(TAG, "ConsumerIrManager not available", e)
            null
        }
    }

    val hasEmitter: Boolean
        get() = irManager?.hasIrEmitter() == true

    fun dispatchAcCommand(
        brand: String,
        power: Boolean,
        temp: Int,
        mode: String = "Cool",
        fanSpeed: String = "Auto",
        isEco: Boolean = false
    ): Boolean {
        val manager = irManager
        if (manager == null || !manager.hasIrEmitter()) {
            Log.w(TAG, "Cannot dispatch IR: Device lacks hardware IR emitter")
            return false
        }

        val cleanBrand = brand.lowercase(Locale.ROOT)
        val pattern = when {
            cleanBrand.contains("sharp") -> encodeSharpPacket(power, temp, mode, fanSpeed, isEco)
            cleanBrand.contains("daikin") -> encodeDaikinPacket(power, temp, mode, fanSpeed, isEco)
            cleanBrand.contains("panasonic") -> encodePanasonicPacket(power, temp, mode, fanSpeed, isEco)
            cleanBrand.contains("gree") -> encodeGreePacket(power, temp, mode, fanSpeed, isEco)
            cleanBrand.contains("mitsubishi") -> encodeMitsubishiPacket(power, temp, mode, fanSpeed, isEco)
            else -> encodeSharpPacket(power, temp, mode, fanSpeed, isEco)
        }

        return try {
            manager.transmit(CARRIER_FREQ_38KHZ, pattern)
            Log.i(TAG, "Successfully fired 38kHz IR packet for $brand: Power=$power, Setpoint=$temp°C, Mode=$mode, Eco=$isEco (pulses: ${pattern.size})")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to transmit IR pulse", e)
            false
        }
    }

    /**
     * Sharp AC Infrared Protocol:
     * Carrier: 38,000 Hz
     * Header: 3800 µs Mark, 1900 µs Space
     * Bit 0: 450 µs Mark, 450 µs Space
     * Bit 1: 450 µs Mark, 1350 µs Space
     * Stop: 450 µs Mark
     * Total Frame: 13 bytes (104 bits)
     */
    private fun encodeSharpPacket(
        power: Boolean,
        temp: Int,
        mode: String,
        fanSpeed: String,
        isEco: Boolean
    ): IntArray {
        val bytes = IntArray(13)
        bytes[0] = 0xAA // Vendor ID 1
        bytes[1] = 0x5A // Vendor ID 2
        bytes[2] = 0xCF // Standard AC Packet
        bytes[3] = 0x10
        bytes[4] = 0x00

        // Temperature nibble: 18°C = 0x01, ..., 24°C = 0x07, 26°C = 0x09
        val tempCode = (temp.coerceIn(18, 30) - 17) and 0x0F
        bytes[5] = tempCode

        // Power & Mode
        val modeCode = when (mode.lowercase(Locale.ROOT)) {
            "dry" -> 0xD1
            "fan" -> 0xE1
            "auto" -> 0xF1
            else -> 0xC1 // Cool
        }
        bytes[6] = if (power) modeCode else 0x21 // 0x21 is Sharp Power-Off opcode

        // Fan Speed & Louver
        bytes[7] = when (fanSpeed.lowercase(Locale.ROOT)) {
            "quiet", "low" -> 0x40
            "high" -> 0x50
            else -> 0x20 // Auto
        }

        // Eco / Plasmacluster flag
        bytes[8] = if (isEco) 0x01 else 0x00
        bytes[9] = 0x00
        bytes[10] = 0x00
        bytes[11] = 0x00

        // Checksum: XOR sum across bytes 0..11
        var chk = 0
        for (i in 0..11) {
            chk = chk xor bytes[i]
        }
        bytes[12] = chk and 0xFF

        return buildPulses(
            headerMark = 3800,
            headerSpace = 1900,
            bitMark = 450,
            zeroSpace = 450,
            oneSpace = 1350,
            stopMark = 450,
            data = bytes
        )
    }

    /**
     * Daikin AC Protocol (ARC433):
     * Header: 3500 µs Mark, 1750 µs Space
     * Bit 0: 430 µs Mark, 430 µs Space
     * Bit 1: 430 µs Mark, 1300 µs Space
     */
    private fun encodeDaikinPacket(
        power: Boolean,
        temp: Int,
        mode: String,
        fanSpeed: String,
        isEco: Boolean
    ): IntArray {
        val bytes = IntArray(19)
        bytes[0] = 0x11
        bytes[1] = 0xDA
        bytes[2] = 0x27
        bytes[3] = 0x00
        bytes[4] = 0xC5
        bytes[5] = if (power) 0x01 else 0x00
        bytes[6] = when (mode.lowercase(Locale.ROOT)) {
            "dry" -> 0x20
            "fan" -> 0x60
            "auto" -> 0x00
            else -> 0x30 // Cool
        }
        bytes[7] = (temp.coerceIn(18, 32) * 2) and 0xFF
        bytes[8] = if (isEco) 0x04 else 0x00
        bytes[9] = when (fanSpeed.lowercase(Locale.ROOT)) {
            "quiet", "low" -> 0x30
            "high" -> 0x70
            else -> 0xA0 // Auto
        }
        for (i in 10..17) bytes[i] = 0x00

        var sum = 0
        for (i in 0..17) sum = (sum + bytes[i]) and 0xFF
        bytes[18] = sum

        return buildPulses(
            headerMark = 3500,
            headerSpace = 1750,
            bitMark = 430,
            zeroSpace = 430,
            oneSpace = 1300,
            stopMark = 430,
            data = bytes
        )
    }

    /**
     * Panasonic AC Protocol:
     * Header: 3500 µs Mark, 1750 µs Space
     * Bit 0: 440 µs Mark, 440 µs Space
     * Bit 1: 440 µs Mark, 1300 µs Space
     */
    private fun encodePanasonicPacket(
        power: Boolean,
        temp: Int,
        mode: String,
        fanSpeed: String,
        isEco: Boolean
    ): IntArray {
        val bytes = IntArray(18)
        bytes[0] = 0x02
        bytes[1] = 0x20
        bytes[2] = 0xE0
        bytes[3] = 0x04
        bytes[4] = 0x00
        bytes[5] = 0x00
        bytes[6] = 0x00
        bytes[7] = 0x06
        bytes[8] = 0x02
        bytes[9] = 0x20
        bytes[10] = 0xE0
        bytes[11] = 0x04
        bytes[12] = 0x00
        bytes[13] = if (power) 0x01 else 0x00
        bytes[14] = when (mode.lowercase(Locale.ROOT)) {
            "dry" -> 0x20
            "fan" -> 0x60
            "auto" -> 0x00
            else -> 0x30 // Cool
        }
        bytes[15] = (temp.coerceIn(16, 30) * 2) and 0xFF
        bytes[16] = if (isEco) 0x20 else 0x00

        var sum = 0
        for (i in 0..16) sum = (sum + bytes[i]) and 0xFF
        bytes[17] = sum

        return buildPulses(
            headerMark = 3500,
            headerSpace = 1750,
            bitMark = 440,
            zeroSpace = 440,
            oneSpace = 1300,
            stopMark = 440,
            data = bytes
        )
    }

    /**
     * Gree AC Protocol:
     * Header: 9000 µs Mark, 4500 µs Space
     * Bit 0: 560 µs Mark, 560 µs Space
     * Bit 1: 560 µs Mark, 1680 µs Space
     */
    private fun encodeGreePacket(
        power: Boolean,
        temp: Int,
        mode: String,
        fanSpeed: String,
        isEco: Boolean
    ): IntArray {
        val bytes = IntArray(8)
        bytes[0] = (when (mode.lowercase(Locale.ROOT)) {
            "dry" -> 0x02
            "fan" -> 0x03
            "auto" -> 0x00
            else -> 0x01
        }) or (if (power) 0x08 else 0x00)
        bytes[1] = (temp.coerceIn(16, 30) - 16) and 0x0F
        bytes[2] = if (isEco) 0x04 else 0x00
        bytes[3] = 0x50
        bytes[4] = 0x02
        bytes[5] = 0x20
        bytes[6] = 0x00
        bytes[7] = ((bytes[0] + bytes[1] + bytes[2] + bytes[3] + bytes[4] + bytes[5] + bytes[6]) and 0x0F)

        return buildPulses(
            headerMark = 9000,
            headerSpace = 4500,
            bitMark = 560,
            zeroSpace = 560,
            oneSpace = 1680,
            stopMark = 560,
            data = bytes
        )
    }

    /**
     * Mitsubishi Electric Protocol:
     * Header: 3400 µs Mark, 1700 µs Space
     * Bit 0: 450 µs Mark, 450 µs Space
     * Bit 1: 450 µs Mark, 1300 µs Space
     */
    private fun encodeMitsubishiPacket(
        power: Boolean,
        temp: Int,
        mode: String,
        fanSpeed: String,
        isEco: Boolean
    ): IntArray {
        val bytes = IntArray(18)
        bytes[0] = 0x23
        bytes[1] = 0xCB
        bytes[2] = 0x26
        bytes[3] = 0x01
        bytes[4] = 0x00
        bytes[5] = if (power) 0x20 else 0x00
        bytes[6] = when (mode.lowercase(Locale.ROOT)) {
            "dry" -> 0x10
            "fan" -> 0x38
            "auto" -> 0x20
            else -> 0x18 // Cool
        }
        bytes[7] = (temp.coerceIn(16, 31) - 16) and 0x0F
        bytes[8] = if (isEco) 0x02 else 0x00
        for (i in 9..16) bytes[i] = 0x00

        var sum = 0
        for (i in 0..16) sum = (sum + bytes[i]) and 0xFF
        bytes[17] = sum

        return buildPulses(
            headerMark = 3400,
            headerSpace = 1700,
            bitMark = 450,
            zeroSpace = 450,
            oneSpace = 1300,
            stopMark = 450,
            data = bytes
        )
    }

    /**
     * Converts raw bytes to microsecond mark/space timing pairs (LSB first).
     */
    private fun buildPulses(
        headerMark: Int,
        headerSpace: Int,
        bitMark: Int,
        zeroSpace: Int,
        oneSpace: Int,
        stopMark: Int,
        data: IntArray
    ): IntArray {
        val list = ArrayList<Int>(2 + data.size * 16 + 1)
        list.add(headerMark)
        list.add(headerSpace)

        for (byteVal in data) {
            for (bit in 0 until 8) {
                list.add(bitMark)
                val isOne = ((byteVal shr bit) and 1) == 1
                list.add(if (isOne) oneSpace else zeroSpace)
            }
        }

        list.add(stopMark)
        return list.toIntArray()
    }

    companion object {
        private const val TAG = "KlimataIR"
        private const val CARRIER_FREQ_38KHZ = 38000
    }
}
