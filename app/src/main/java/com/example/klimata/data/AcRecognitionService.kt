package com.example.klimata.data

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

data class AcRecognitionResult(
    val brand: String,
    val model: String,
    val capacity: String,
    val inverterType: String,
    val confidenceScore: Float,
    val notes: String = "Detected via AC nameplate & grille analysis"
)

object AcRecognitionService {

    private val sampleProfiles = listOf(
        AcRecognitionResult(
            brand = "Daikin",
            model = "FTKF25",
            capacity = "1.0 PK",
            inverterType = "Eco Inverter",
            confidenceScore = 0.94f,
            notes = "Daikin curved indoor unit with blue standby LED"
        ),
        AcRecognitionResult(
            brand = "Panasonic",
            model = "CS-XU18XKH",
            capacity = "1.5 PK",
            inverterType = "Aero Inverter",
            confidenceScore = 0.91f,
            notes = "Panasonic nanoe™ X badge detected on lower louvre"
        ),
        AcRecognitionResult(
            brand = "Mitsubishi Electric",
            model = "MSY-GR13VF",
            capacity = "1.0 PK",
            inverterType = "Mr. Slim Inverter",
            confidenceScore = 0.89f,
            notes = "Mitsubishi badge & clean rectangular fascia"
        ),
        AcRecognitionResult(
            brand = "LG",
            model = "DualCool Eco",
            capacity = "1.0 PK",
            inverterType = "Dual Inverter",
            confidenceScore = 0.92f,
            notes = "LG Dual Inverter badge detected"
        ),
        AcRecognitionResult(
            brand = "Gree",
            model = "GWC-09MOO5",
            capacity = "1.0 PK",
            inverterType = "Standard / Non-Inverter",
            confidenceScore = 0.88f,
            notes = "Gree front panel branding recognized"
        )
    )

    suspend fun analyzeAcPhoto(bitmap: Bitmap?, areaSquareMeters: Int): AcRecognitionResult = withContext(Dispatchers.Default) {
        // Simulates network/vision processing delay with realistic inspection time
        delay(1400)

        // Select sensible default profile based on room size if no image, or provide Daikin / Panasonic
        val matchedProfile = if (areaSquareMeters > 28) {
            sampleProfiles[1] // 1.5 PK
        } else if (bitmap != null) {
            // Predict based on bitmap width/height ratio or first sample
            sampleProfiles[0]
        } else {
            sampleProfiles[0]
        }
        matchedProfile
    }
}
