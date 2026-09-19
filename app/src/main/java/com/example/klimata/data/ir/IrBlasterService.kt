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
 * Gree, Mitsubishi Electric, LG, Samsung, Midea, TCL, Carrier, and Toshiba units.
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
            cleanBrand.contains("gree") || cleanBrand.contains("tcl") || cleanBrand.contains("aqua") ->
                encodeGreePacket(power, temp, mode, fanSpeed, isEco)
            cleanBrand.contains("mitsubishi") -> encodeMitsubishiPacket(power, temp, mode, fanSpeed, isEco)
            cleanBrand.contains("lg") -> encodeLgPacket(power, temp, mode, fanSpeed, isEco)
            cleanBrand.contains("samsung") -> encodeSamsungPacket(power, temp, mode, fanSpeed, isEco)
            cleanBrand.contains("midea") || cleanBrand.contains("toshiba") || cleanBrand.contains("carrier") ->
                encodeMideaPacket(power, temp, mode, fanSpeed, isEco)
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
     * Sharp AC Infrared Protocol (104-bit):
     * Carrier: 38,000 Hz
     * Header: 3800 µs Mark, 1900 µs Space
     * Bit 0: 450 µs Mark, 450 µs Space
     * Bit 1: 450 µs Mark, 1350 µs Space
     * Stop: 450 µs Mark
     */
    private fun encodeSharpPacket(
        power: Boolean,
        temp: Int,
        mode: String,
        fanSpeed: String,
        isEco: Boolean
    ): IntArray {
        val bytes = IntArray(13)
        bytes[0] = 0xAA
        bytes[1] = 0x5A
        bytes[2] = 0xCF
        bytes[3] = 0x10
        bytes[4] = 0x00

        val tempCode = (temp.coerceIn(18, 30) - 17) and 0x0F
        bytes[5] = tempCode

        val modeCode = when (mode.lowercase(Locale.ROOT)) {
            "dry" -> 0xD1
            "fan" -> 0xE1
            "auto" -> 0xF1
            else -> 0xC1
        }
        bytes[6] = if (power) modeCode else 0x21

        bytes[7] = when (fanSpeed.lowercase(Locale.ROOT)) {
            "quiet", "low" -> 0x40
            "high" -> 0x50
            else -> 0x20
        }

        bytes[8] = if (isEco) 0x01 else 0x00
        bytes[9] = 0x00
        bytes[10] = 0x00
        bytes[11] = 0x00

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
     * Daikin ARC433 Protocol (38 kHz, multi-frame burst)
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
        bytes[4] = 0x00

        val modeByte = when (mode.lowercase(Locale.ROOT)) {
            "dry" -> 0x20
            "fan" -> 0x60
            "auto" -> 0x00
            else -> 0x30
        }
        bytes[5] = if (power) (modeByte or 0x01) else 0x00

        val t = temp.coerceIn(18, 32)
        bytes[6] = (t * 2) and 0xFF

        bytes[7] = 0x00
        bytes[8] = when (fanSpeed.lowercase(Locale.ROOT)) {
            "low" -> 0x30
            "high" -> 0x70
            else -> 0xA0
        }

        bytes[9] = 0x00
        bytes[10] = 0x00
        bytes[11] = 0x00
        bytes[12] = 0x00
        bytes[13] = 0x00
        bytes[14] = 0x00
        bytes[15] = 0x00
        bytes[16] = if (isEco) 0x04 else 0x00
        bytes[17] = 0x00

        var sum = 0
        for (i in 0..17) {
            sum = (sum + bytes[i]) and 0xFF
        }
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
     * Panasonic Protocol (216-bit timing sequence)
     */
    private fun encodePanasonicPacket(
        power: Boolean,
        temp: Int,
        mode: String,
        fanSpeed: String,
        isEco: Boolean
    ): IntArray {
        val bytes = IntArray(27)
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
        bytes[14] = (temp.coerceIn(16, 30) * 2) and 0xFF
        bytes[15] = 0x80
        bytes[16] = when (mode.lowercase(Locale.ROOT)) {
            "dry" -> 0x20
            "fan" -> 0x60
            "auto" -> 0x00
            else -> 0x30
        }
        bytes[17] = if (isEco) 0x20 else 0x00
        for (i in 18..25) bytes[i] = 0x00

        var sum = 0
        for (i in 0..25) sum = (sum + bytes[i]) and 0xFF
        bytes[26] = sum

        return buildPulses(
            headerMark = 3500,
            headerSpace = 3500,
            bitMark = 450,
            zeroSpace = 450,
            oneSpace = 1300,
            stopMark = 450,
            data = bytes
        )
    }

    /**
     * Gree AC Protocol
     */
    private fun encodeGreePacket(
        power: Boolean,
        temp: Int,
        mode: String,
        fanSpeed: String,
        isEco: Boolean
    ): IntArray {
        val bytes = IntArray(8)
        val modeCode = when (mode.lowercase(Locale.ROOT)) {
            "dry" -> 0x02
            "fan" -> 0x03
            "auto" -> 0x00
            else -> 0x01
        }
        val powerBit = if (power) 0x08 else 0x00
        bytes[0] = modeCode or powerBit

        val tempCode = (temp.coerceIn(16, 30) - 16) and 0x0F
        bytes[1] = tempCode
        bytes[2] = 0x20
        bytes[3] = 0x50
        bytes[4] = 0x02
        bytes[5] = 0x20
        bytes[6] = if (isEco) 0x04 else 0x00

        var sum = 0
        for (i in 0..6) sum = (sum + bytes[i]) and 0xFF
        bytes[7] = sum

        return buildPulses(
            headerMark = 9000,
            headerSpace = 4500,
            bitMark = 650,
            zeroSpace = 550,
            oneSpace = 1650,
            stopMark = 650,
            data = bytes
        )
    }

    /**
     * Mitsubishi Electric Protocol
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
            else -> 0x18
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
     * LG AC Protocol (28-bit standard)
     */
    private fun encodeLgPacket(
        power: Boolean,
        temp: Int,
        mode: String,
        fanSpeed: String,
        isEco: Boolean
    ): IntArray {
        val bytes = IntArray(4)
        bytes[0] = 0x88 // LG Header / Address

        if (!power) {
            bytes[1] = 0xC0
            bytes[2] = 0x05
        } else {
            val modeCode = when (mode.lowercase(Locale.ROOT)) {
                "dry" -> 0x01
                "fan" -> 0x02
                "auto" -> 0x03
                else -> 0x00 // Cool
            }
            bytes[1] = (modeCode shl 4) or (if (isEco) 0x08 else 0x00)
            bytes[2] = ((temp.coerceIn(18, 30) - 15) shl 4) or 0x05
        }

        // LG checksum: sum of all preceding nibbles modulo 16
        var nibbleSum = 0
        for (i in 0..2) {
            nibbleSum += (bytes[i] shr 4) and 0x0F
            nibbleSum += bytes[i] and 0x0F
        }
        bytes[3] = (nibbleSum and 0x0F)

        return buildPulses(
            headerMark = 8500,
            headerSpace = 4200,
            bitMark = 550,
            zeroSpace = 550,
            oneSpace = 1600,
            stopMark = 550,
            data = bytes
        )
    }

    /**
     * Samsung AC Protocol (56-bit frame)
     */
    private fun encodeSamsungPacket(
        power: Boolean,
        temp: Int,
        mode: String,
        fanSpeed: String,
        isEco: Boolean
    ): IntArray {
        val bytes = IntArray(7)
        bytes[0] = 0x02
        bytes[1] = 0xB2
        bytes[2] = 0x0F
        bytes[3] = if (power) 0x00 else 0xF0
        bytes[4] = ((temp.coerceIn(16, 30) - 16) shl 4) or (if (isEco) 0x02 else 0x00)
        bytes[5] = 0x20

        var chk = 0
        for (i in 0..5) chk = chk xor bytes[i]
        bytes[6] = chk

        return buildPulses(
            headerMark = 3000,
            headerSpace = 9000,
            bitMark = 450,
            zeroSpace = 450,
            oneSpace = 1400,
            stopMark = 450,
            data = bytes
        )
    }

    /**
     * Midea / Toshiba / Carrier Protocol (48-bit frame)
     */
    private fun encodeMideaPacket(
        power: Boolean,
        temp: Int,
        mode: String,
        fanSpeed: String,
        isEco: Boolean
    ): IntArray {
        val bytes = IntArray(6)
        bytes[0] = 0xB2
        bytes[1] = 0x4D
        bytes[2] = if (power) 0x1F else 0x7B

        val tempCode = (temp.coerceIn(17, 30) - 17) and 0x0F
        bytes[3] = (tempCode shl 4) or (if (isEco) 0x04 else 0x00)
        bytes[4] = 0xE0
        bytes[5] = (bytes[2] xor bytes[3] xor bytes[4]) and 0xFF

        return buildPulses(
            headerMark = 4400,
            headerSpace = 4400,
            bitMark = 560,
            zeroSpace = 560,
            oneSpace = 1600,
            stopMark = 560,
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
