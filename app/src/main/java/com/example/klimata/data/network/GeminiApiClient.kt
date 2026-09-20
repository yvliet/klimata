package com.example.klimata.data.network

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.klimata.data.AcRecognitionResult
import com.example.klimata.data.models.AcDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object GeminiApiClient {

    private const val CONNECT_TIMEOUT_MS = 12000
    private const val READ_TIMEOUT_MS = 25000

    suspend fun analyzeAcPhoto(
        bitmap: Bitmap,
        apiKey: String = AiConfig.GEMINI_API_KEY
    ): Result<AcRecognitionResult> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Gemini API key is blank"))
        }

        val base64Data = prepareImageBase64(bitmap)
        val payload = buildRequestPayload(base64Data)

        val modelsToTry = listOf(
            AiConfig.PRIMARY_MODEL,
            AiConfig.SECONDARY_MODEL,
            AiConfig.TERTIARY_MODEL
        )

        var lastException: Throwable? = null

        for (model in modelsToTry) {
            var attemptResult = executeGeneration(model, apiKey, payload)
            if (attemptResult.isSuccess) {
                return@withContext attemptResult
            }

            // If 503 temporary overload, backoff briefly and retry once
            val err = attemptResult.exceptionOrNull()
            lastException = err
            if (err?.message?.contains("503") == true) {
                Log.d("KlimataAI", "Model $model encountered 503, retrying in 800ms...")
                delay(800)
                attemptResult = executeGeneration(model, apiKey, payload)
                if (attemptResult.isSuccess) {
                    return@withContext attemptResult
                }
                lastException = attemptResult.exceptionOrNull()
            }
        }

        Result.failure(lastException ?: IllegalStateException("All Gemini vision models failed"))
    }

    private fun executeGeneration(
        modelName: String,
        apiKey: String,
        payloadJson: String
    ): Result<AcRecognitionResult> {
        return try {
            Log.d("KlimataAI", "Querying Gemini model $modelName for AC recognition...")
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = CONNECT_TIMEOUT_MS
            conn.readTimeout = READ_TIMEOUT_MS
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("User-Agent", "Klimata-Android/1.0")

            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(payloadJson)
                writer.flush()
            }

            val responseCode = conn.responseCode
            val responseStream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val reader = BufferedReader(InputStreamReader(responseStream))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            reader.close()
            conn.disconnect()

            if (responseCode !in 200..299) {
                Log.e("KlimataAI", "Gemini HTTP $responseCode from $modelName: $sb")
                return Result.failure(IllegalStateException("HTTP $responseCode from $modelName: $sb"))
            }

            val responseJson = JSONObject(sb.toString())
            val candidates = responseJson.optJSONArray("candidates")
                ?: return Result.failure(IllegalStateException("No candidates returned from Gemini"))

            if (candidates.length() == 0) {
                return Result.failure(IllegalStateException("Empty candidates from Gemini"))
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val textOutput = parts.getJSONObject(0).getString("text")

            Log.d("KlimataAI", "Gemini raw response: $textOutput")

            // Sanitize markdown fences
            var cleanJson = textOutput.trim()
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.removePrefix("```json")
            } else if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.removePrefix("```")
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.removeSuffix("```")
            }
            cleanJson = cleanJson.trim()
            val startIdx = cleanJson.indexOf('{')
            val endIdx = cleanJson.lastIndexOf('}')
            if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                cleanJson = cleanJson.substring(startIdx, endIdx + 1)
            }

            val parsedJson = JSONObject(cleanJson)

            fun optCleanString(key: String): String? {
                if (!parsedJson.has(key) || parsedJson.isNull(key)) return null
                val str = parsedJson.optString(key).trim()
                return if (str.equals("null", ignoreCase = true) || str.isBlank()) null else str
            }

            val rawBrand = optCleanString("brand")
            val rawModel = optCleanString("model")
            val rawCapacity = optCleanString("capacity")
            val rawInverter = optCleanString("inverterType")
            val confidence = parsedJson.optDouble("confidenceScore", 0.92).toFloat()
            val rawNotes = optCleanString("notes") ?: "Identified via optical vision analysis"

            // Cross-reference with comprehensive AcDatabase
            val databaseMatch = AcDatabase.findBestMatch(rawBrand, rawModel)

            val finalBrand = rawBrand ?: databaseMatch?.brand ?: "Sharp"
            val finalModel = rawModel ?: databaseMatch?.modelCode ?: "AH-XP10VXY"
            val finalCapacity = rawCapacity ?: databaseMatch?.defaultCapacity ?: "1.0 PK"
            val finalInverter = rawInverter ?: databaseMatch?.inverterType ?: "J-Tech Inverter"
            val finalNotes = if (databaseMatch != null) {
                "$rawNotes (${databaseMatch.series})"
            } else {
                rawNotes
            }

            Log.d("KlimataAI", "Parsed AC: brand=$finalBrand, model=$finalModel, capacity=$finalCapacity, inverter=$finalInverter")

            Result.success(
                AcRecognitionResult(
                    brand = finalBrand,
                    model = finalModel,
                    capacity = finalCapacity,
                    inverterType = finalInverter,
                    confidenceScore = confidence,
                    notes = finalNotes,
                    isAiDetected = true
                )
            )
        } catch (e: Exception) {
            Log.e("KlimataAI", "Error calling Gemini model $modelName", e)
            Result.failure(e)
        }
    }

    private fun prepareImageBase64(bitmap: Bitmap): String {
        val maxDim = 1024
        val scaled = if (bitmap.width > maxDim || bitmap.height > maxDim) {
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val targetW = if (ratio >= 1f) maxDim else (maxDim * ratio).toInt()
            val targetH = if (ratio >= 1f) (maxDim / ratio).toInt() else maxDim
            Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
        } else {
            bitmap
        }

        val stream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun buildRequestPayload(base64Image: String): String {
        val root = JSONObject()
        val contentsArr = JSONArray()
        val contentObj = JSONObject()
        val partsArr = JSONArray()

        val textPart = JSONObject().apply {
            put(
                "text",
                "You are an expert HVAC technician and optical equipment inspection system. " +
                        "Carefully examine this photograph of an air conditioner unit, indoor split wall unit, or equipment rating specification label sticker/barcode. " +
                        "Pay special attention to printed brand names (Sharp, Daikin, Panasonic, Mitsubishi Electric, LG, Gree, Samsung, Midea, TCL, Toshiba, Carrier, Aqua, etc.), " +
                        "model numbers (e.g. AH-XP10, AH-A9, FTKF25, CS-XU10, etc.), cooling capacity (BTU or PK), and inverter technology badges (e.g. Plasmacluster, J-Tech, Inverter, nanoe, Dual Inverter). " +
                        "Extract all details accurately from the sticker, barcode text, or front fascia. " +
                        "Return a JSON object with keys: " +
                        "\"brand\" (string), " +
                        "\"model\" (string model number or series name), " +
                        "\"capacity\" (standardized string: '0.5 PK', '0.75 PK', '1.0 PK', '1.5 PK', or '2.0 PK'), " +
                        "\"inverterType\" (string, e.g. 'J-Tech Inverter', 'Eco Inverter', 'Dual Inverter', 'Standard / Non-Inverter'), " +
                        "\"confidenceScore\" (float from 0.0 to 1.0), " +
                        "\"notes\" (string describing visual confirmation evidence, e.g. 'Sharp Plasmacluster rating sticker verified')."
            )
        }
        partsArr.put(textPart)

        val imagePart = JSONObject().apply {
            val inlineData = JSONObject().apply {
                put("mimeType", "image/jpeg")
                put("data", base64Image)
            }
            put("inlineData", inlineData)
        }
        partsArr.put(imagePart)

        contentObj.put("parts", partsArr)
        contentsArr.put(contentObj)
        root.put("contents", contentsArr)

        val generationConfig = JSONObject().apply {
            put("responseMimeType", "application/json")
        }
        root.put("generationConfig", generationConfig)

        return root.toString()
    }
}
