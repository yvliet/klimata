package com.example.klimata.data.network

import com.example.klimata.BuildConfig

object AiConfig {
    /**
     * Reads from BuildConfig (populated from `local.properties` via `gemini.api.key=...`)
     * or can be supplied directly via [MANUAL_KEY].
     */
    val GEMINI_API_KEY: String
        get() = BuildConfig.GEMINI_API_KEY.ifBlank { MANUAL_KEY }

    private const val MANUAL_KEY: String = ""

    const val PRIMARY_MODEL: String = "gemini-3.6-flash"
    const val FALLBACK_MODEL: String = "gemini-3.8-flash"

    val isConfigured: Boolean
        get() = GEMINI_API_KEY.isNotBlank()
}
