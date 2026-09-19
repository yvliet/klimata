package com.example.klimata.data.network

object AiConfig {
    const val GEMINI_API_KEY: String = ""
    const val PRIMARY_MODEL: String = "gemini-3.8-flash"
    const val FALLBACK_MODEL: String = "gemini-3.5-flash"

    val isConfigured: Boolean
        get() = GEMINI_API_KEY.isNotBlank()
}
