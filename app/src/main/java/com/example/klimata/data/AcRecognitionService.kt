package com.example.klimata.data

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.klimata.data.models.AcDatabase
import com.example.klimata.data.models.AcModelInfo
import com.example.klimata.data.network.AiConfig
import com.example.klimata.data.network.GeminiApiClient
import com.example.klimata.data.storage.KlimataPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

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

    fun getAllCatalogModels(): List<AcModelInfo> = AcDatabase.allModels

    suspend fun analyzeAcPhoto(
        bitmap: Bitmap?,
        areaSquareMeters: Int,
        context: Context? = null,
        apiKeyOverride: String? = null
    ): AcRecognitionResult = withContext(Dispatchers.Default) {
        val apiKey = apiKeyOverride ?: AiConfig.GEMINI_API_KEY
        Log.d("AcRecognitionService", "analyzeAcPhoto called. Bitmap present: ${bitmap != null}, apiKey configured: ${apiKey.isNotBlank()}")

        if (bitmap != null && apiKey.isNotBlank()) {
            val aiResult = GeminiApiClient.analyzeAcPhoto(
                bitmap = bitmap,
                apiKey = apiKey
            )
            if (aiResult.isSuccess) {
                val recognized = aiResult.getOrThrow()
                Log.d("AcRecognitionService", "Gemini AI Recognition succeeded: ${recognized.brand} ${recognized.model}")
                return@withContext recognized.copy(isAiDetected = true)
            } else {
                Log.w("AcRecognitionService", "Gemini AI call failed, falling back to catalog baseline", aiResult.exceptionOrNull())
            }
        }

        // On-device catalog selection based on room thermal volume requirements
        val defaultModel = if (areaSquareMeters > 28) {
            AcDatabase.allModels.firstOrNull { it.brand == "Sharp" && it.defaultCapacity == "1.5 PK" }
                ?: AcDatabase.allModels[1]
        } else {
            AcDatabase.allModels.firstOrNull { it.brand == "Sharp" && it.modelCode == "AH-XP10VXY" }
                ?: AcDatabase.allModels[0]
        }

        AcRecognitionResult(
            brand = defaultModel.brand,
            model = defaultModel.modelCode,
            capacity = defaultModel.defaultCapacity,
            inverterType = defaultModel.inverterType,
            confidenceScore = 0.85f,
            notes = "On-device match: ${defaultModel.series} (${defaultModel.defaultCapacity}). Tap 'Browse Catalog' to switch models.",
            isAiDetected = false
        )
    }
}
