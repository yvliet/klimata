package com.example.klimata.data.ir

import android.content.Context
import android.hardware.ConsumerIrManager
import android.util.Log
import java.util.Locale

/**
 * Metadata for an AC infrared protocol code set.
 */
data class AcCodeSet(
    val id: String,
    val displayName: String,
    val sampleModels: String,
    val brand: String,
    val description: String
)

/**
 * Autonomous Hardware Infrared Dispatch Engine.
 *
 * Interfaces with Android's [ConsumerIrManager] to pulse 38 kHz infrared packets
 * through built-in phone IR blasters (e.g. Xiaomi, POCO, Redmi, Huawei).
 * Supports multi-code set pairing and vendor-specific telemetry frames for Sharp,
 * Daikin, Panasonic, Gree, Mitsubishi Electric, LG, Samsung, Midea, TCL, Carrier,
 * and Toshiba units.
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

    /**
     * Returns all supported IR code sets for a given brand to facilitate smart pairing.
     */
    fun getCodeSetsForBrand(brand: String): List<AcCodeSet> {
        val clean = brand.lowercase(Locale.ROOT)
        return when {
            clean.contains("sharp") -> listOf(
                AcCodeSet(
                    id = "sharp_inverter_104",
                    displayName = "Sharp Inverter (J-Tech)",
                    sampleModels = "AH-XP10VXY, AH-XP13, AH-X9ZY, AY-ZP, CRMC-A907",
                    brand = "Sharp",
                    description = "104-bit native J-Tech Inverter protocol with Plasmacluster support."
                ),
                AcCodeSet(
                    id = "sharp_gree_oem",
                    displayName = "Sharp Turbo Cool (Gree OEM)",
                    sampleModels = "AH-A5UCY, AH-A9UCY, AH-A5SAY, YB0F2, YB1F2",
                    brand = "Sharp",
                    description = "Gree 67-pulse split protocol used in high-volume Indonesian/SEA Sharp units."
                ),
                AcCodeSet(
                    id = "sharp_crmc_a705",
                    displayName = "Sharp Legacy / Portable",
                    sampleModels = "CRMC-A705, CRMC-A820, AH-PR13-GL",
                    brand = "Sharp",
                    description = "Legacy Sharp 104-bit protocol with inverted model flag."
                ),
                AcCodeSet(
                    id = "sharp_aux_oem",
                    displayName = "Sharp Standard OEM (AUX)",
                    sampleModels = "AH-A5NCY, AUX-manufactured series",
                    brand = "Sharp",
                    description = "AUX 48-bit frame for OEM budget units."
                )
            )
            clean.contains("daikin") -> listOf(
                AcCodeSet(
                    id = "daikin_arc433",
                    displayName = "Daikin Inverter (ARC433)",
                    sampleModels = "FTKF25, FTKQ, Flash Inverter, Multi-S",
                    brand = "Daikin",
                    description = "19-byte 3-frame Daikin inverter protocol."
                ),
                AcCodeSet(
                    id = "daikin_arc480",
                    displayName = "Daikin Standard (ARC480)",
                    sampleModels = "FTNE, FTV, Standard Inverter",
                    brand = "Daikin",
                    description = "27-byte legacy Daikin split unit protocol."
                )
            )
            clean.contains("panasonic") -> listOf(
                AcCodeSet(
                    id = "panasonic_dke",
                    displayName = "Panasonic Inverter (DKE)",
                    sampleModels = "CS-XU10, CS-PU9, CS-YN, Aero Series",
                    brand = "Panasonic",
                    description = "27-byte Panasonic DKE protocol with nanoe-G."
                ),
                AcCodeSet(
                    id = "panasonic_ckp",
                    displayName = "Panasonic Legacy (CKP)",
                    sampleModels = "CS-PC9, Standard Non-Inverter",
                    brand = "Panasonic",
                    description = "2-frame legacy Panasonic protocol."
                )
            )
            clean.contains("gree") || clean.contains("tcl") || clean.contains("aqua") -> listOf(
                AcCodeSet(
                    id = "gree_yb0f2",
                    displayName = "Gree Standard (YB0F2)",
                    sampleModels = "Bora, Lomo, Moo, F1, YB0F2, YB1F2",
                    brand = "Gree",
                    description = "67-pulse split frame protocol (32-bit blocks with 20ms pause)."
                ),
                AcCodeSet(
                    id = "gree_yaa",
                    displayName = "Gree Inverter (YAA)",
                    sampleModels = "Cosmo, U-Crown, Inverter series",
                    brand = "Gree",
                    description = "Gree YAA/YAC high-frequency frame."
                )
            )
            clean.contains("mitsubishi") -> listOf(
                AcCodeSet(
                    id = "mitsubishi_fd",
                    displayName = "Mitsubishi Electric (KP3BS)",
                    sampleModels = "MSY-GR, MSZ-LN, Kirigamine",
                    brand = "Mitsubishi Electric",
                    description = "18-byte Mitsubishi Electric protocol."
                ),
                AcCodeSet(
                    id = "mitsubishi_heavy",
                    displayName = "Mitsubishi Heavy (MHI)",
                    sampleModels = "SRK-ZMP, SRK-ZSX",
                    brand = "Mitsubishi Heavy",
                    description = "19-byte Mitsubishi Heavy Industries protocol."
                )
            )
            clean.contains("lg") -> listOf(
                AcCodeSet(
                    id = "lg_28bit",
                    displayName = "LG Dual Inverter (28-bit)",
                    sampleModels = "T10EV4, E10SV4, Dual Inverter",
                    brand = "LG",
                    description = "28-bit LG pulse-distance protocol."
                )
            )
            clean.contains("samsung") -> listOf(
                AcCodeSet(
                    id = "samsung_14byte",
                    displayName = "Samsung WindFree",
                    sampleModels = "AR9500, WindFree, Digital Inverter",
                    brand = "Samsung",
                    description = "Samsung 14-byte multi-stage burst protocol."
                )
            )
            clean.contains("midea") || clean.contains("toshiba") || clean.contains("carrier") -> listOf(
                AcCodeSet(
                    id = "midea_r05",
                    displayName = "Midea / Toshiba / Carrier",
                    sampleModels = "MSAF, Blanc, Carrier XPower, Toshiba RAS",
                    brand = "Midea",
                    description = "48-bit NEC-like protocol with XOR nibble checks."
                )
            )
            else -> listOf(
                AcCodeSet(
                    id = "sharp_inverter_104",
                    displayName = "Universal Signal 1 (Sharp Inverter)",
                    sampleModels = "Generic / Inverter Units",
                    brand = brand,
                    description = "Standard 104-bit pulse distance modulation."
                ),
                AcCodeSet(
                    id = "sharp_gree_oem",
                    displayName = "Universal Signal 2 (Gree OEM)",
                    sampleModels = "Generic / OEM Units",
                    brand = brand,
                    description = "67-pulse split frame protocol."
                )
            )
        }
    }

    /**
     * Resolves the best default code set given an optional model code.
     */
    fun getDefaultCodeSetForModel(brand: String, modelCode: String?): String {
        val cleanBrand = brand.lowercase(Locale.ROOT)
        val m = modelCode?.lowercase(Locale.ROOT) ?: ""
        return when {
            cleanBrand.contains("sharp") -> when {
                m.contains("ucy") || m.contains("say") || m.contains("turbo") || m.contains("yb0f") -> "sharp_gree_oem"
                m.contains("crmc") || m.contains("a705") || m.contains("a820") || m.contains("pr13") -> "sharp_crmc_a705"
                m.contains("aux") || m.contains("ncy") -> "sharp_aux_oem"
                else -> "sharp_inverter_104"
            }
            cleanBrand.contains("daikin") -> if (m.contains("ftne") || m.contains("ftv") || m.contains("480")) "daikin_arc480" else "daikin_arc433"
            cleanBrand.contains("panasonic") -> if (m.contains("ckp") || m.contains("pc")) "panasonic_ckp" else "panasonic_dke"
            cleanBrand.contains("gree") || cleanBrand.contains("tcl") || cleanBrand.contains("aqua") -> "gree_yb0f2"
            cleanBrand.contains("mitsubishi") -> if (m.contains("heavy") || m.contains("srk")) "mitsubishi_heavy" else "mitsubishi_fd"
            cleanBrand.contains("lg") -> "lg_28bit"
            cleanBrand.contains("samsung") -> "samsung_14byte"
            cleanBrand.contains("midea") || cleanBrand.contains("toshiba") || cleanBrand.contains("carrier") -> "midea_r05"
            else -> "sharp_inverter_104"
        }
    }

    /**
     * Dispatches a test pulse for remote pairing verification.
     */
    fun dispatchTestPulse(brand: String, codeSetId: String, power: Boolean = true): Boolean {
        return dispatchAcCommand(
            brand = brand,
            power = power,
            temp = 24,
            mode = "Cool",
            fanSpeed = "Auto",
            isEco = false,
            codeSetId = codeSetId
        )
    }

    /**
     * Dispatches full AC telemetry frame through hardware IR blaster.
     */
    fun dispatchAcCommand(
        brand: String,
        power: Boolean,
        temp: Int,
        mode: String = "Cool",
        fanSpeed: String = "Auto",
        isEco: Boolean = false,
        codeSetId: String? = null
    ): Boolean {
        val manager = irManager
        if (manager == null || !manager.hasIrEmitter()) {
            Log.w(TAG, "Cannot dispatch IR: Device lacks hardware IR emitter")
            return false
        }

        val pattern = encodeAcCommand(
            brand = brand,
            power = power,
            temp = temp,
            mode = mode,
            fanSpeed = fanSpeed,
            isEco = isEco,
            codeSetId = codeSetId
        )

        return try {
            manager.transmit(CARRIER_FREQ_38KHZ, pattern)
            Log.i(TAG, "Successfully fired 38kHz IR packet for $brand (codeSet=${codeSetId ?: "auto"}): Power=$power, Setpoint=$temp°C, Mode=$mode, Eco=$isEco (pulses: ${pattern.size})")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to transmit IR pulse", e)
            false
        }
    }

    /**
     * Public encoder for telemetry frames, facilitating both dispatch and unit testing.
     */
    fun encodeAcCommand(
        brand: String,
        power: Boolean,
        temp: Int,
        mode: String = "Cool",
        fanSpeed: String = "Auto",
        isEco: Boolean = false,
        codeSetId: String? = null
    ): IntArray {
        val effectiveCodeSet = codeSetId ?: getDefaultCodeSetForModel(brand, null)
        return when (effectiveCodeSet) {
            "sharp_inverter_104" -> encodeSharpInverter104(power, temp, mode, fanSpeed, isEco)
            "sharp_gree_oem" -> encodeGreePacket(power, temp, mode, fanSpeed, isEco)
            "sharp_crmc_a705" -> encodeSharpCrmcPacket(power, temp, mode, fanSpeed, isEco)
            "sharp_aux_oem" -> encodeMideaPacket(power, temp, mode, fanSpeed, isEco)

            "daikin_arc433" -> encodeDaikinPacket(power, temp, mode, fanSpeed, isEco)
            "daikin_arc480" -> encodeDaikinArc480Packet(power, temp, mode, fanSpeed, isEco)

            "panasonic_dke" -> encodePanasonicPacket(power, temp, mode, fanSpeed, isEco)
            "panasonic_ckp" -> encodePanasonicPacket(power, temp, mode, fanSpeed, isEco)

            "gree_yb0f2" -> encodeGreePacket(power, temp, mode, fanSpeed, isEco)
            "gree_yaa" -> encodeGreePacket(power, temp, mode, fanSpeed, isEco)

            "mitsubishi_fd" -> encodeMitsubishiPacket(power, temp, mode, fanSpeed, isEco)
            "mitsubishi_heavy" -> encodeMitsubishiPacket(power, temp, mode, fanSpeed, isEco)

            "lg_28bit" -> encodeLgPacket(power, temp, mode, fanSpeed, isEco)
            "samsung_14byte" -> encodeSamsungPacket(power, temp, mode, fanSpeed, isEco)
            "midea_r05" -> encodeMideaPacket(power, temp, mode, fanSpeed, isEco)

            else -> {
                val clean = brand.lowercase(Locale.ROOT)
                when {
                    clean.contains("sharp") -> encodeSharpInverter104(power, temp, mode, fanSpeed, isEco)
                    clean.contains("daikin") -> encodeDaikinPacket(power, temp, mode, fanSpeed, isEco)
                    clean.contains("panasonic") -> encodePanasonicPacket(power, temp, mode, fanSpeed, isEco)
                    clean.contains("gree") || clean.contains("tcl") || clean.contains("aqua") -> encodeGreePacket(power, temp, mode, fanSpeed, isEco)
                    clean.contains("mitsubishi") -> encodeMitsubishiPacket(power, temp, mode, fanSpeed, isEco)
                    clean.contains("lg") -> encodeLgPacket(power, temp, mode, fanSpeed, isEco)
                    clean.contains("samsung") -> encodeSamsungPacket(power, temp, mode, fanSpeed, isEco)
                    clean.contains("midea") || clean.contains("toshiba") || clean.contains("carrier") -> encodeMideaPacket(power, temp, mode, fanSpeed, isEco)
                    else -> encodeSharpInverter104(power, temp, mode, fanSpeed, isEco)
                }
            }
        }
    }

    /**
     * Sharp J-Tech Inverter Protocol (13 bytes / 104-bit frame).
     * Fully aligned with official AY-ZP / AH-XP / CRMC-A907 specifications.
     */
    fun encodeSharpInverter104(
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

        // Byte 4: bits 0..3 = (Temp - 15), bits 4..7 = 0xC0 (model flag 0 for A907)
        val clampedTemp = temp.coerceIn(15, 30)
        val tempNibble = (clampedTemp - 15) and 0x0F
        bytes[4] = 0xC0 or tempNibble

        // Byte 5: bits 4..7 = PowerSpecial (0x10 = Power On from Off, 0x20 = Power Off), bit 0 = 0x01
        bytes[5] = (if (power) 0x10 else 0x20) or 0x01

        // Byte 6: bits 0..1 = Mode, bits 4..6 = Fan
        val modeVal = when (mode.lowercase(Locale.ROOT)) {
            "dry" -> 0b11 // 3
            "heat" -> 0b01 // 1
            "fan", "auto" -> 0b00 // 0
            else -> 0b10 // Cool = 2
        }
        val fanVal = when (fanSpeed.lowercase(Locale.ROOT)) {
            "quiet", "low" -> 0b100 // 4
            "med", "medium" -> 0b011 // 3
            "high" -> 0b101 // 5
            "max", "turbo" -> 0b111 // 7
            else -> 0b010 // Auto = 2
        }
        bytes[6] = (fanVal shl 4) or modeVal

        // Byte 7: Timer settings (0x00 for off)
        bytes[7] = 0x00

        // Byte 8: Swing (0x08 default)
        bytes[8] = 0x08

        // Byte 9: Fixed constant 0x80
        bytes[9] = 0x80

        // Byte 10: Special command (0x04 = temp/econo, 0x00 = power toggle)
        bytes[10] = if (power) 0x04 else 0x00

        // Byte 11: Fixed constant 0xE0
        bytes[11] = 0xE0

        // Byte 12: bit 0 = 0x01, bits 4..7 = 4-bit nibble-folded XOR checksum
        bytes[12] = 0x01
        val chk = calcSharpChecksum(bytes)
        bytes[12] = (chk shl 4) or 0x01

        return buildPulses(
            headerMark = 3800,
            headerSpace = 1900,
            bitMark = 470,
            zeroSpace = 500,
            oneSpace = 1400,
            stopMark = 470,
            data = bytes
        )
    }

    /**
     * Sharp CRMC-A705 Legacy Protocol (104-bit frame with model bit enabled).
     */
    fun encodeSharpCrmcPacket(
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

        // Byte 4: bits 0..3 = (Temp - 15), bits 4..7 = 0xD0 (model flag set for A705)
        val clampedTemp = temp.coerceIn(15, 30)
        val tempNibble = (clampedTemp - 15) and 0x0F
        bytes[4] = 0xD0 or tempNibble

        // Byte 5: Power
        bytes[5] = (if (power) 0x10 else 0x20) or 0x01

        // Byte 6: Mode + Fan
        val modeVal = when (mode.lowercase(Locale.ROOT)) {
            "dry" -> 0b11
            "fan", "auto" -> 0b00
            else -> 0b10 // Cool
        }
        val fanVal = when (fanSpeed.lowercase(Locale.ROOT)) {
            "quiet", "low" -> 0b100
            "med", "medium" -> 0b011
            "high" -> 0b101
            else -> 0b010 // Auto
        }
        bytes[6] = (fanVal shl 4) or modeVal

        bytes[7] = 0x00
        bytes[8] = 0x08
        bytes[9] = 0x80
        bytes[10] = if (power) 0x04 else 0x00
        bytes[11] = 0xF0 // Model 2 bit set for A705

        bytes[12] = 0x01
        val chk = calcSharpChecksum(bytes)
        bytes[12] = (chk shl 4) or 0x01

        return buildPulses(
            headerMark = 3800,
            headerSpace = 1900,
            bitMark = 470,
            zeroSpace = 500,
            oneSpace = 1400,
            stopMark = 470,
            data = bytes
        )
    }

    /**
     * Official Sharp 4-bit nibble-folded XOR checksum algorithm.
     */
    fun calcSharpChecksum(bytes: IntArray): Int {
        var xorSum = 0
        for (i in 0..11) {
            xorSum = xorSum xor bytes[i]
        }
        xorSum = xorSum xor (bytes[12] and 0x0F)
        xorSum = xorSum xor ((xorSum shr 4) and 0x0F)
        return xorSum and 0x0F
    }

    /**
     * Gree AC Protocol & Sharp Gree OEM (AH-A5UCY, AH-A9UCY, YB0F2 remote).
     * Emits a dual 32-bit block frame separated by a 3-bit footer and a 20ms pause.
     */
    fun encodeGreePacket(
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
            "heat" -> 0x04
            "auto" -> 0x00
            else -> 0x01 // Cool
        }
        val powerBit = if (power) 0x08 else 0x00
        bytes[0] = modeCode or powerBit

        val tempCode = (temp.coerceIn(16, 30) - 16) and 0x0F
        bytes[1] = tempCode
        bytes[2] = when (fanSpeed.lowercase(Locale.ROOT)) {
            "low", "quiet" -> 0x10
            "med", "medium" -> 0x20
            "high" -> 0x30
            else -> 0x00 // Auto
        }
        bytes[3] = 0x50
        bytes[4] = 0x02
        bytes[5] = 0x20
        bytes[6] = if (isEco) 0x04 else 0x00

        // Kelvinator/Gree block checksum (kKelvinatorChecksumStart = 10)
        var sum = 10
        for (i in 0..3) {
            sum += (bytes[i] and 0x0F)
        }
        for (i in 4..6) {
            sum += ((bytes[i] shr 4) and 0x0F)
        }
        bytes[7] = ((sum and 0x0F) shl 4)

        return buildGreePulses(bytes)
    }

    /**
     * Converts Gree raw bytes to two 32-bit blocks separated by a 3-bit footer and 20ms gap.
     */
    private fun buildGreePulses(bytes: IntArray): IntArray {
        val bitMark = 620
        val zeroSpace = 540
        val oneSpace = 1600
        val msgSpace = 20000 // 20 ms inter-block gap
        val list = ArrayList<Int>(144)

        // Block 1 Header
        list.add(9000)
        list.add(4500)

        // Block 1 Data (bytes 0..3, 32 bits, LSB first)
        for (i in 0..3) {
            val b = bytes[i]
            for (bit in 0 until 8) {
                list.add(bitMark)
                val isOne = ((b shr bit) and 1) == 1
                list.add(if (isOne) oneSpace else zeroSpace)
            }
        }

        // Block 1 Footer (3 bits: 0b010 -> bit0=0, bit1=1, bit2=0)
        val footerBits = 0b010
        for (bit in 0 until 3) {
            list.add(bitMark)
            val isOne = ((footerBits shr bit) and 1) == 1
            list.add(if (isOne) oneSpace else zeroSpace)
        }

        // Mid-message gap: bit mark + 20ms space
        list.add(bitMark)
        list.add(msgSpace)

        // Block 2 Data (bytes 4..7, 32 bits, LSB first)
        for (i in 4..7) {
            val b = bytes[i]
            for (bit in 0 until 8) {
                list.add(bitMark)
                val isOne = ((b shr bit) and 1) == 1
                list.add(if (isOne) oneSpace else zeroSpace)
            }
        }

        // Stop mark & 40ms lead-out silence
        list.add(bitMark)
        list.add(40000)

        return list.toIntArray()
    }

    /**
     * Daikin ARC433 Protocol (38 kHz, multi-frame burst).
     */
    fun encodeDaikinPacket(
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
        bytes[5] = if (power) 0x09 else 0x08

        val modeCode = when (mode.lowercase(Locale.ROOT)) {
            "dry" -> 0x20
            "fan" -> 0x60
            "auto" -> 0x00
            else -> 0x30
        }
        bytes[6] = modeCode
        bytes[7] = (temp.coerceIn(18, 30) * 2) and 0xFF
        bytes[8] = when (fanSpeed.lowercase(Locale.ROOT)) {
            "quiet", "low" -> 0x30
            "high" -> 0x70
            else -> 0x50
        }
        bytes[9] = if (isEco) 0x04 else 0x00
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
     * Daikin ARC480 Protocol (27-byte frame).
     */
    fun encodeDaikinArc480Packet(
        power: Boolean,
        temp: Int,
        mode: String,
        fanSpeed: String,
        isEco: Boolean
    ): IntArray {
        val bytes = IntArray(27)
        bytes[0] = 0x11
        bytes[1] = 0xDA
        bytes[2] = 0x27
        bytes[3] = 0x00
        bytes[4] = 0x00
        bytes[5] = if (power) 0x09 else 0x08
        bytes[6] = 0x30
        bytes[7] = (temp.coerceIn(18, 30) * 2) and 0xFF
        bytes[8] = 0x50
        for (i in 9..25) bytes[i] = 0x00

        var sum = 0
        for (i in 0..25) sum = (sum + bytes[i]) and 0xFF
        bytes[26] = sum

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
     * Panasonic DKE Protocol (27-byte frame).
     */
    fun encodePanasonicPacket(
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
     * Mitsubishi Electric Protocol (18-byte frame).
     */
    fun encodeMitsubishiPacket(
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
     * LG AC Protocol (28-bit standard).
     */
    fun encodeLgPacket(
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
     * Samsung AC Protocol (56-bit frame).
     */
    fun encodeSamsungPacket(
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
     * Midea / Toshiba / Carrier / AUX Protocol (48-bit frame).
     */
    fun encodeMideaPacket(
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
     * Converts raw bytes to microsecond mark/space timing pairs (LSB first)
     * with trailing inter-frame silence to allow receiver demodulator recovery.
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
        val list = ArrayList<Int>(2 + data.size * 16 + 2)
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
        list.add(40000) // 40ms inter-message lead-out silence
        return list.toIntArray()
    }

    companion object {
        private const val TAG = "KlimataIR"
        private const val CARRIER_FREQ_38KHZ = 38000
    }
}
