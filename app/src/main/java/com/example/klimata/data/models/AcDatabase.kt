package com.example.klimata.data.models

data class AcModelInfo(
    val brand: String,
    val series: String,
    val modelCode: String,
    val supportedCapacities: List<String>,
    val defaultCapacity: String,
    val inverterType: String,
    val irProtocol: String,
    val notes: String
)

object AcDatabase {

    val allModels: List<AcModelInfo> = listOf(
        // Sharp
        AcModelInfo(
            brand = "Sharp",
            series = "J-Tech Inverter Plasmacluster",
            modelCode = "AH-XP10VXY",
            supportedCapacities = listOf("0.5 PK", "1.0 PK", "1.5 PK", "2.0 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "J-Tech Inverter",
            irProtocol = "SHARP_104BIT",
            notes = "Plasmacluster Ion high-density 7000 with Eco Mode energy modulation"
        ),
        AcModelInfo(
            brand = "Sharp",
            series = "J-Tech Inverter Plasmacluster",
            modelCode = "AH-XP13VXY",
            supportedCapacities = listOf("1.5 PK", "2.0 PK"),
            defaultCapacity = "1.5 PK",
            inverterType = "J-Tech Inverter",
            irProtocol = "SHARP_104BIT",
            notes = "High-output J-Tech Inverter with 4-way auto swing"
        ),
        AcModelInfo(
            brand = "Sharp",
            series = "Turbo Cool Sayonara Panas (Gree OEM)",
            modelCode = "AH-A9UCY",
            supportedCapacities = listOf("0.5 PK", "0.75 PK", "1.0 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Standard / Non-Inverter",
            irProtocol = "sharp_gree_oem",
            notes = "OEM Gree platform (YB0F2 / YB1F2 remote) with Turbo Cool"
        ),
        AcModelInfo(
            brand = "Sharp",
            series = "Turbo Cool Standard (Gree OEM)",
            modelCode = "AH-A5UCY",
            supportedCapacities = listOf("0.5 PK"),
            defaultCapacity = "0.5 PK",
            inverterType = "Standard / Non-Inverter",
            irProtocol = "sharp_gree_oem",
            notes = "Ultra low wattage 350W compact bedroom unit (Gree OEM YB0F2 remote)"
        ),
        AcModelInfo(
            brand = "Sharp",
            series = "Generic / Universal Sharp",
            modelCode = "Sharp Universal",
            supportedCapacities = listOf("0.5 PK", "0.75 PK", "1.0 PK", "1.5 PK", "2.0 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Standard / Inverter",
            irProtocol = "sharp_inverter_104",
            notes = "Universal multi-signal profile with smart code pairing for unknown Sharp ACs"
        ),
        AcModelInfo(
            brand = "Sharp",
            series = "J-Tech Inverter Eco",
            modelCode = "AH-X9ZY",
            supportedCapacities = listOf("0.75 PK", "1.0 PK", "1.5 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "J-Tech Inverter",
            irProtocol = "SHARP_104BIT",
            notes = "Baby Sleep Mode & 14°C low temperature operation"
        ),
        AcModelInfo(
            brand = "Sharp",
            series = "Plasmacluster Standard",
            modelCode = "AH-A9SAY",
            supportedCapacities = listOf("0.75 PK", "1.0 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Standard / Non-Inverter",
            irProtocol = "SHARP_104BIT",
            notes = "Sayonara Panas with Plasmacluster air purification"
        ),
        AcModelInfo(
            brand = "Sharp",
            series = "J-Tech Inverter Eco Series",
            modelCode = "AH-X12ZY",
            supportedCapacities = listOf("1.5 PK"),
            defaultCapacity = "1.5 PK",
            inverterType = "J-Tech Inverter",
            irProtocol = "SHARP_104BIT",
            notes = "Inverter with Coanda airflow and 7 Shields protection"
        ),

        // Daikin
        AcModelInfo(
            brand = "Daikin",
            series = "Flash Inverter",
            modelCode = "FTKF25",
            supportedCapacities = listOf("0.5 PK", "0.75 PK", "1.0 PK", "1.5 PK", "2.0 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Eco Inverter",
            irProtocol = "DAIKIN_ARC433",
            notes = "Curved fascia with standby LED indicator and Gin-ION deodorizing filter"
        ),
        AcModelInfo(
            brand = "Daikin",
            series = "Flash Inverter",
            modelCode = "FTKF35",
            supportedCapacities = listOf("1.5 PK", "2.0 PK"),
            defaultCapacity = "1.5 PK",
            inverterType = "Eco Inverter",
            irProtocol = "DAIKIN_ARC433",
            notes = "12,000 BTU Inverter with PM2.5 filter"
        ),
        AcModelInfo(
            brand = "Daikin",
            series = "Multi-S Super Inverter",
            modelCode = "FTKQ25",
            supportedCapacities = listOf("0.5 PK", "1.0 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Eco Inverter",
            irProtocol = "DAIKIN_ARC433",
            notes = "Super Inverter with intelligent eye human sensor"
        ),
        AcModelInfo(
            brand = "Daikin",
            series = "Super Smile Standard",
            modelCode = "FTC25",
            supportedCapacities = listOf("0.5 PK", "0.75 PK", "1.0 PK", "1.5 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Standard / Non-Inverter",
            irProtocol = "DAIKIN_ARC433",
            notes = "High durability smile curve front grille"
        ),
        AcModelInfo(
            brand = "Daikin",
            series = "Premium Urusara Inverter",
            modelCode = "FTKZ25",
            supportedCapacities = listOf("1.0 PK", "1.5 PK", "2.0 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Eco Inverter",
            irProtocol = "DAIKIN_ARC433",
            notes = "Flagship humidity control and Streamer air purifier"
        ),
        AcModelInfo(
            brand = "Daikin",
            series = "Lite Standard",
            modelCode = "FTV25",
            supportedCapacities = listOf("0.5 PK", "1.0 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Standard / Non-Inverter",
            irProtocol = "DAIKIN_ARC433",
            notes = "Compact lightweight residential series"
        ),

        // Panasonic
        AcModelInfo(
            brand = "Panasonic",
            series = "Premium Inverter nanoe™ X",
            modelCode = "CS-XU10XKH",
            supportedCapacities = listOf("1.0 PK", "1.5 PK", "2.0 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Aero Inverter",
            irProtocol = "PANASONIC",
            notes = "nanoe™ X generator Mark 2 with AEROWINGS twin louvers"
        ),
        AcModelInfo(
            brand = "Panasonic",
            series = "Premium Inverter nanoe™ X",
            modelCode = "CS-XU18XKH",
            supportedCapacities = listOf("1.5 PK", "2.0 PK"),
            defaultCapacity = "2.0 PK",
            inverterType = "Aero Inverter",
            irProtocol = "PANASONIC",
            notes = "High-tonnage quiet bedroom airflow"
        ),
        AcModelInfo(
            brand = "Panasonic",
            series = "Standard Inverter nanoe-G",
            modelCode = "CS-PU9XKH",
            supportedCapacities = listOf("0.5 PK", "1.0 PK", "1.5 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Aero Inverter",
            irProtocol = "PANASONIC",
            notes = "ECO Mode with Artificial Intelligence (A.I.) control"
        ),
        AcModelInfo(
            brand = "Panasonic",
            series = "Standard Non-Inverter",
            modelCode = "CS-YN9WKJ",
            supportedCapacities = listOf("0.5 PK", "0.75 PK", "1.0 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Standard / Non-Inverter",
            irProtocol = "PANASONIC",
            notes = "si-BiRU blue fin anti-corrosion condenser"
        ),
        AcModelInfo(
            brand = "Panasonic",
            series = "si-BiRU Standard",
            modelCode = "CS-YN5WKJ",
            supportedCapacities = listOf("0.5 PK"),
            defaultCapacity = "0.5 PK",
            inverterType = "Standard / Non-Inverter",
            irProtocol = "PANASONIC",
            notes = "Ultra low power 390W R32 standard model"
        ),

        // Mitsubishi Electric
        AcModelInfo(
            brand = "Mitsubishi Electric",
            series = "Mr. Slim Inverter",
            modelCode = "MSY-GR13VF",
            supportedCapacities = listOf("1.0 PK", "1.5 PK", "2.0 PK"),
            defaultCapacity = "1.5 PK",
            inverterType = "Mr. Slim Inverter",
            irProtocol = "MITSUBISHI_ELECTRIC",
            notes = "Dual Barrier Coating with Nano Platinum filter"
        ),
        AcModelInfo(
            brand = "Mitsubishi Electric",
            series = "Mr. Slim Inverter",
            modelCode = "MSY-GR10VF",
            supportedCapacities = listOf("0.75 PK", "1.0 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Mr. Slim Inverter",
            irProtocol = "MITSUBISHI_ELECTRIC",
            notes = "Ultra-quiet 19dB nighttime whisper sound profile"
        ),
        AcModelInfo(
            brand = "Mitsubishi Electric",
            series = "Standard Non-Inverter",
            modelCode = "MS-JR10VF",
            supportedCapacities = listOf("0.5 PK", "1.0 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Standard / Non-Inverter",
            irProtocol = "MITSUBISHI_ELECTRIC",
            notes = "Heavy duty tropical compressor rating"
        ),
        AcModelInfo(
            brand = "Mitsubishi Electric",
            series = "Kirigamine Premium",
            modelCode = "MSZ-LN25VG",
            supportedCapacities = listOf("1.0 PK", "1.5 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Mr. Slim Inverter",
            irProtocol = "MITSUBISHI_ELECTRIC",
            notes = "3D i-See Sensor with double flap airflow"
        ),

        // LG
        AcModelInfo(
            brand = "LG",
            series = "DualCool Eco Inverter",
            modelCode = "T06EV4",
            supportedCapacities = listOf("0.5 PK"),
            defaultCapacity = "0.5 PK",
            inverterType = "Dual Inverter",
            irProtocol = "LG_32BIT",
            notes = "Dual Inverter Compressor with active energy control"
        ),
        AcModelInfo(
            brand = "LG",
            series = "DualCool Eco Inverter",
            modelCode = "T10EV4",
            supportedCapacities = listOf("1.0 PK", "1.5 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Dual Inverter",
            irProtocol = "LG_32BIT",
            notes = "Dual Inverter with Watt Control (40%, 60%, 80%)"
        ),
        AcModelInfo(
            brand = "LG",
            series = "DualCool Smart Inverter",
            modelCode = "E10SV5",
            supportedCapacities = listOf("1.0 PK", "1.5 PK", "2.0 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Dual Inverter",
            irProtocol = "LG_32BIT",
            notes = "ThinQ Wi-Fi with pre-filter & allergy filter"
        ),
        AcModelInfo(
            brand = "LG",
            series = "DualCool New Hercules",
            modelCode = "H05TN4",
            supportedCapacities = listOf("0.5 PK", "1.0 PK"),
            defaultCapacity = "0.5 PK",
            inverterType = "Standard / Non-Inverter",
            irProtocol = "LG_32BIT",
            notes = "Turbo cooling with Gold Fin anti-corrosion"
        ),
        AcModelInfo(
            brand = "LG",
            series = "Artcool Mirror Inverter",
            modelCode = "A06ECV",
            supportedCapacities = listOf("0.5 PK", "1.0 PK", "1.5 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Dual Inverter",
            irProtocol = "LG_32BIT",
            notes = "Tempered glass black mirror chassis with ionizer"
        ),

        // Gree
        AcModelInfo(
            brand = "Gree",
            series = "Standard Non-Inverter MOO5",
            modelCode = "GWC-09MOO5",
            supportedCapacities = listOf("0.5 PK", "0.75 PK", "1.0 PK", "1.5 PK", "2.0 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Standard / Non-Inverter",
            irProtocol = "GREE",
            notes = "Frozen Power rapid cooling with Blue Fin anti-rust"
        ),
        AcModelInfo(
            brand = "Gree",
            series = "Standard MOO5 Super Low Watt",
            modelCode = "GWC-05MOO5",
            supportedCapacities = listOf("0.5 PK"),
            defaultCapacity = "0.5 PK",
            inverterType = "Standard / Non-Inverter",
            irProtocol = "GREE",
            notes = "370W low-draw bedroom unit"
        ),
        AcModelInfo(
            brand = "Gree",
            series = "F1 Series Inverter",
            modelCode = "GWC-09F1",
            supportedCapacities = listOf("0.75 PK", "1.0 PK", "1.5 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Inverter",
            irProtocol = "GREE",
            notes = "G10 Inverter technology down to 1Hz compressor frequency"
        ),
        AcModelInfo(
            brand = "Gree",
            series = "U-Crown Luxury Inverter",
            modelCode = "GWC-09U1",
            supportedCapacities = listOf("1.0 PK", "1.5 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Inverter",
            irProtocol = "GREE",
            notes = "Aluminum alloy ultra-slim profile with 2-stage compressor"
        ),

        // Samsung
        AcModelInfo(
            brand = "Samsung",
            series = "WindFree™ Ultra Inverter",
            modelCode = "AR10BYFAM",
            supportedCapacities = listOf("1.0 PK", "1.5 PK", "2.0 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Digital Inverter Boost",
            irProtocol = "SAMSUNG_56BIT",
            notes = "23,000 micro-holes for still air cooling without direct drafts"
        ),
        AcModelInfo(
            brand = "Samsung",
            series = "WindFree™ Lite Inverter",
            modelCode = "AR10TYHZCW",
            supportedCapacities = listOf("0.5 PK", "1.0 PK", "1.5 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Digital Inverter Boost",
            irProtocol = "SAMSUNG_56BIT",
            notes = "Fast cooling mode followed by WindFree auto switch"
        ),
        AcModelInfo(
            brand = "Samsung",
            series = "Digital Inverter Alpha",
            modelCode = "AR09AYHAA",
            supportedCapacities = listOf("0.5 PK", "1.0 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Digital Inverter Boost",
            irProtocol = "SAMSUNG_56BIT",
            notes = "Triple Protector Plus voltage stabilization"
        ),

        // Midea
        AcModelInfo(
            brand = "Midea",
            series = "All Easy Pro Inverter",
            modelCode = "MSEP-09CRN1",
            supportedCapacities = listOf("0.75 PK", "1.0 PK", "1.5 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Inverter Quattro",
            irProtocol = "MIDEA_48BIT",
            notes = "1-screw quick cleaning architecture with Gear energy saving"
        ),
        AcModelInfo(
            brand = "Midea",
            series = "Breezeless Inverter",
            modelCode = "MSFA-09CRDN8",
            supportedCapacities = listOf("1.0 PK", "1.5 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Inverter Quattro",
            irProtocol = "MIDEA_48BIT",
            notes = "Twinflap with 7,928 micro-holes for gentle diffuse cooling"
        ),
        AcModelInfo(
            brand = "Midea",
            series = "Standard Non-Inverter",
            modelCode = "MSAF-05CRN2",
            supportedCapacities = listOf("0.5 PK", "1.0 PK"),
            defaultCapacity = "0.5 PK",
            inverterType = "Standard / Non-Inverter",
            irProtocol = "MIDEA_48BIT",
            notes = "High density filter with Prime Guard golden fin"
        ),

        // TCL
        AcModelInfo(
            brand = "TCL",
            series = "GentleCool Smart Inverter",
            modelCode = "TAC-09CSD",
            supportedCapacities = listOf("0.5 PK", "1.0 PK", "1.5 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Inverter",
            irProtocol = "GREE",
            notes = "Gentle breeze mode with AI inverter algorithm"
        ),

        // Toshiba
        AcModelInfo(
            brand = "Toshiba",
            series = "Daiseikai Inverter",
            modelCode = "RAS-10PKVSG",
            supportedCapacities = listOf("1.0 PK", "1.5 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Inverter",
            irProtocol = "MIDEA_48BIT",
            notes = "Plasma ionizer with Magic Coil self-cleaning"
        ),

        // Carrier
        AcModelInfo(
            brand = "Carrier",
            series = "OptiClean Inverter",
            modelCode = "42QHG009",
            supportedCapacities = listOf("1.0 PK", "1.5 PK"),
            defaultCapacity = "1.0 PK",
            inverterType = "Inverter",
            irProtocol = "MIDEA_48BIT",
            notes = "Active energy management and turbo mode"
        ),

        // Aqua Japan
        AcModelInfo(
            brand = "Aqua Japan",
            series = "Turbo Cool Standard",
            modelCode = "AQA-KCR5ANR",
            supportedCapacities = listOf("0.5 PK", "1.0 PK"),
            defaultCapacity = "0.5 PK",
            inverterType = "Standard / Non-Inverter",
            irProtocol = "GREE",
            notes = "Wide voltage operation down to 160V"
        )
    )

    fun getBrands(): List<String> = listOf(
        "All",
        "Sharp",
        "Daikin",
        "Panasonic",
        "Mitsubishi Electric",
        "LG",
        "Gree",
        "Samsung",
        "Midea",
        "TCL",
        "Toshiba",
        "Carrier",
        "Aqua Japan"
    )

    fun filter(brand: String? = null, query: String? = null): List<AcModelInfo> {
        return allModels.filter { model ->
            val matchesBrand = if (brand.isNullOrBlank() || brand.equals("All", ignoreCase = true)) {
                true
            } else {
                model.brand.equals(brand, ignoreCase = true)
            }

            val matchesQuery = if (query.isNullOrBlank()) {
                true
            } else {
                val q = query.trim().lowercase()
                model.brand.lowercase().contains(q) ||
                        model.modelCode.lowercase().contains(q) ||
                        model.series.lowercase().contains(q) ||
                        model.inverterType.lowercase().contains(q) ||
                        model.notes.lowercase().contains(q)
            }

            matchesBrand && matchesQuery
        }
    }

    fun findBestMatch(brandCandidate: String?, modelCandidate: String?): AcModelInfo? {
        if (brandCandidate.isNullOrBlank() && modelCandidate.isNullOrBlank()) return null

        val b = brandCandidate?.trim()?.lowercase() ?: ""
        val m = modelCandidate?.trim()?.lowercase() ?: ""

        // Exact model code match
        if (m.isNotBlank()) {
            allModels.firstOrNull { it.modelCode.equals(m, ignoreCase = true) }?.let { return it }
            allModels.firstOrNull { m.contains(it.modelCode.lowercase()) || it.modelCode.lowercase().contains(m) }?.let { return it }
        }

        // Brand + series or partial model match
        if (b.isNotBlank()) {
            val brandModels = allModels.filter { it.brand.lowercase().contains(b) || b.contains(it.brand.lowercase()) }
            if (brandModels.isNotEmpty()) {
                if (m.isNotBlank()) {
                    brandModels.firstOrNull { it.series.lowercase().contains(m) || it.notes.lowercase().contains(m) }?.let { return it }
                }
                return brandModels.first()
            }
        }

        return null
    }
}
