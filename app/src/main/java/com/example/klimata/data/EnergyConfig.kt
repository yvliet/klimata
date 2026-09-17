package com.example.klimata.data

import androidx.compose.runtime.Immutable
import java.util.Locale

/**
 * Regional energy economics and grid intensity configuration.
 * Adapts calculations to local electricity rates, currency symbols, and grid carbon factors.
 */
@Immutable
data class EnergyConfig(
    val countryCode: String = "ID",
    val currencyCode: String = "IDR",
    val currencySymbol: String = "Rp",
    val tariffPerKwh: Double = 1445.0,
    val gridEmissionFactor: Float = 0.78f,
    val currencyRateToUsd: Double = 15600.0,
) {
    companion object {
        val INDONESIA = EnergyConfig(
            countryCode = "ID",
            currencyCode = "IDR",
            currencySymbol = "Rp",
            tariffPerKwh = 1445.0,
            gridEmissionFactor = 0.78f,
            currencyRateToUsd = 15600.0,
        )

        val UNITED_STATES = EnergyConfig(
            countryCode = "US",
            currencyCode = "USD",
            currencySymbol = "$",
            tariffPerKwh = 0.16,
            gridEmissionFactor = 0.38f,
            currencyRateToUsd = 1.0,
        )

        val SINGAPORE = EnergyConfig(
            countryCode = "SG",
            currencyCode = "SGD",
            currencySymbol = "S$",
            tariffPerKwh = 0.30,
            gridEmissionFactor = 0.41f,
            currencyRateToUsd = 1.34,
        )

        val JAPAN = EnergyConfig(
            countryCode = "JP",
            currencyCode = "JPY",
            currencySymbol = "¥",
            tariffPerKwh = 31.0,
            gridEmissionFactor = 0.47f,
            currencyRateToUsd = 155.0,
        )

        val AUSTRALIA = EnergyConfig(
            countryCode = "AU",
            currencyCode = "AUD",
            currencySymbol = "A$",
            tariffPerKwh = 0.34,
            gridEmissionFactor = 0.55f,
            currencyRateToUsd = 1.52,
        )

        val EUROPE = EnergyConfig(
            countryCode = "EU",
            currencyCode = "EUR",
            currencySymbol = "€",
            tariffPerKwh = 0.28,
            gridEmissionFactor = 0.26f,
            currencyRateToUsd = 0.92,
        )

        fun resolveForLocation(
            cityName: String? = null,
            countryCode: String? = null,
            latitude: Double? = null,
            longitude: Double? = null
        ): EnergyConfig {
            val country = countryCode?.trim()?.uppercase(Locale.ROOT)
            if (!country.isNullOrEmpty()) {
                when (country) {
                    "ID", "INDONESIA" -> return INDONESIA
                    "US", "USA", "UNITED STATES" -> return UNITED_STATES
                    "SG", "SINGAPORE" -> return SINGAPORE
                    "JP", "JAPAN" -> return JAPAN
                    "AU", "AUSTRALIA" -> return AUSTRALIA
                    "GB", "DE", "FR", "NL", "ES", "IT", "EU" -> return EUROPE
                }
            }

            val city = cityName?.lowercase(Locale.ROOT) ?: ""
            if (city.isNotEmpty()) {
                val indonesianKeywords = listOf(
                    "pondok aren", "jakarta", "tangerang", "bandung", "surabaya",
                    "bali", "denpasar", "depok", "bekasi", "semarang", "yogyakarta",
                    "jogja", "medan", "makassar", "palembang", "bogor", "banten",
                    "indonesia", "kecamatan", "kelurahan", "jawa", "sumatera",
                    "sulawesi", "kalimantan", "malang", "solo", "surakarta", "batam"
                )
                if (indonesianKeywords.any { city.contains(it) }) {
                    return INDONESIA
                }

                val usKeywords = listOf(
                    "los angeles", "new york", "san francisco", "chicago", "seattle",
                    "austin", "miami", "boston", "dallas", "denver", "houston",
                    "phoenix", "california", "texas", "usa", "united states", "america"
                )
                if (usKeywords.any { city.contains(it) }) {
                    return UNITED_STATES
                }

                if (city.contains("singapore")) return SINGAPORE
                if (city.contains("tokyo") || city.contains("osaka") || city.contains("kyoto") || city.contains("japan")) return JAPAN
                if (city.contains("sydney") || city.contains("melbourne") || city.contains("brisbane") || city.contains("australia")) return AUSTRALIA
                if (city.contains("london") || city.contains("paris") || city.contains("berlin") || city.contains("amsterdam")) return EUROPE
            }

            if (latitude != null && longitude != null) {
                if (latitude in -11.5..6.5 && longitude in 94.5..141.5) {
                    return INDONESIA
                }
                if (latitude in 1.15..1.48 && longitude in 103.55..104.1) {
                    return SINGAPORE
                }
                if (latitude in 24.0..46.0 && longitude in 122.0..153.0) {
                    return JAPAN
                }
                if (latitude in -44.0..-10.0 && longitude in 112.0..154.0) {
                    return AUSTRALIA
                }
                if (latitude in 24.0..50.0 && longitude in -125.0..-66.0) {
                    return UNITED_STATES
                }
            }

            return INDONESIA
        }

        fun resolveForLocale(locale: Locale = Locale.getDefault(), countryOverride: String? = null): EnergyConfig {
            if (countryOverride != null) {
                return resolveForLocation(countryCode = countryOverride)
            }
            return resolveForLocation(countryCode = locale.country)
        }
    }
}
