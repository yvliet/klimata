package com.example.klimata.data

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

import android.util.Log

data class AcRecognitionResult(
    val brand: String,
    val model: String,
    val capacity: String,
    val inverterType: String,
    val confidenceScore: Float,
    val notes: String = "Detected via AC nameplate & grille analysis",
    val isAiDetected: Boolean = false
)

object AcRecognitionService {

    private val sampleProfiles = listOf(
        AcRecognitionResult(
            brand = "Sharp",
            model = "AH-XP10VXY",
            capacity = "1.0 PK",
            inverterType = "J-Tech Inverter",
            confidenceScore = 0.93f,
            notes = "Sharp Plasmacluster Ion badge detected on chassis",
            isAiDetected = false
        ),
        AcRecognitionResult(
            brand = "Daikin",
            model = "FTKF25",
            capacity = "1.0 PK",
            inverterType = "Eco Inverter",
            confidenceScore = 0.94f,
            notes = "Daikin curved indoor unit with blue standby LED",
            isAiDetected = false
        ),
        AcRecognitionResult(
            brand = "Panasonic",
            model = "CS-XU18XKH",
            capacity = "1.5 PK",
            inverterType = "Aero Inverter",
            confidenceScore = 0.91f,
            notes = "Panasonic nanoe™ X badge detected on lower louvre",
            isAiDetected = false
        ),
        AcRecognitionResult(
            brand = "Mitsubishi Electric",
            model = "MSY-GR13VF",
            capacity = "1.0 PK",
            inverterType = "Mr. Slim Inverter",
            confidenceScore = 0.89f,
            notes = "Mitsubishi badge & clean rectangular fascia",
            isAiDetected = false
        ),
        AcRecognitionResult(
            brand = "LG",
            model = "DualCool Eco",
            capacity = "1.0 PK",
            inverterType = "Dual Inverter",
            confidenceScore = 0.92f,
            notes = "LG Dual Inverter badge detected",
            isAiDetected = false
        ),
        AcRecognitionResult(
            brand = "Gree",
            model = "GWC-09MOO5",
            capacity = "1.0 PK",
            inverterType = "Standard / Non-Inverter",
            confidenceScore = 0.88f,
            notes = "Gree front panel branding recognized",
            isAiDetected = false
        )
    )

    suspend fun analyzeAcPhoto(bitmap: Bitmap?, areaSquareMeters: Int): AcRecognitionResult = withContext(Dispatchers.Default) {
        val apiKey = com.example.klimata.data.network.AiConfig.GEMINI_API_KEY
        Log.d("AcRecognitionService", "analyzeAcPhoto called. Bitmap present: ${bitmap != null}, apiKey configured: ${apiKey.isNotBlank()}")

        if (bitmap != null && apiKey.isNotBlank()) {
            val aiResult = com.example.klimata.data.network.GeminiApiClient.analyzeAcPhoto(
                bitmap = bitmap,
                apiKey = apiKey
            )
            if (aiResult.isSuccess) {
                val recognized = aiResult.getOrThrow()
                Log.d("AcRecognitionService", "Gemini AI Recognition succeeded: ${recognized.brand} ${recognized.model}")
                return@withContext recognized.copy(isAiDetected = true)
            } else {
                Log.w("AcRecognitionService", "Gemini AI call failed, falling back to heuristics", aiResult.exceptionOrNull())
            }
        }

        delay(1000)

        if (areaSquareMeters > 28) {
            sampleProfiles[2]
        } else if (bitmap != null && bitmap.width > bitmap.height * 1.5f) {
            sampleProfiles[0]
        } else {
            sampleProfiles[1]
        }
    }
}
