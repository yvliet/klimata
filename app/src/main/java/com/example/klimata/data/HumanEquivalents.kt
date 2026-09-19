package com.example.klimata.data

import androidx.compose.runtime.Immutable

enum class SavingsFactIcon {
    COFFEE,
    SUBSCRIPTION,
    AC_HEALTH,
    GETAWAY,
}

@Immutable
data class SavingsEquivalentFact(
    val title: String,
    val value: String,
    val unit: String,
    val description: String,
    val iconType: SavingsFactIcon,
)

enum class CarbonFactIcon {
    TREE,
    COMMUTE,
    SMARTPHONE,
    LIGHTING,
}

@Immutable
data class CarbonEquivalentFact(
    val title: String,
    val value: String,
    val unit: String,
    val description: String,
    val iconType: CarbonFactIcon,
)

object HumanEquivalents {

    fun getSavingsTopHighlight(period: ImpactPeriod): String = when (period) {
        ImpactPeriod.WEEKLY -> "☕ ~1 Morning Coffee"
        ImpactPeriod.MONTHLY -> "☕ ~4 Artisan Coffees"
        ImpactPeriod.YEARLY -> "🏖️ Weekend Staycation"
        ImpactPeriod.LIFETIME -> "💰 Smart Utility Reserve"
    }

    fun getSavingsFacts(period: ImpactPeriod): List<SavingsEquivalentFact> = when (period) {
        ImpactPeriod.WEEKLY -> listOf(
            SavingsEquivalentFact(
                title = "Daily Treat",
                value = "1",
                unit = "Coffee",
                description = "A fresh morning iced kopi susu or warm pastry on your commute.",
                iconType = SavingsFactIcon.COFFEE
            ),
            SavingsEquivalentFact(
                title = "Streaming Credit",
                value = "0.3",
                unit = "Mo",
                description = "Covers roughly a week of Spotify Premium or mobile streaming.",
                iconType = SavingsFactIcon.SUBSCRIPTION
            ),
            SavingsEquivalentFact(
                title = "Compressor Rest",
                value = "13",
                unit = "hrs",
                description = "Over a half-day of compressor motor idle, reducing internal heat wear.",
                iconType = SavingsFactIcon.AC_HEALTH
            )
        )
        ImpactPeriod.MONTHLY -> listOf(
            SavingsEquivalentFact(
                title = "Everyday Treats",
                value = "4",
                unit = "Coffees",
                description = "Four cups of artisan iced latte or cozy weekend breakfasts with family.",
                iconType = SavingsFactIcon.COFFEE
            ),
            SavingsEquivalentFact(
                title = "Subscriptions",
                value = "1.5",
                unit = "Months",
                description = "Covers a full month of Spotify Premium or Netflix without dipping into cash.",
                iconType = SavingsFactIcon.SUBSCRIPTION
            ),
            SavingsEquivalentFact(
                title = "AC Health",
                value = "56",
                unit = "hrs",
                description = "Over 2 full days of compressor downtime, prolonging your unit's lifespan.",
                iconType = SavingsFactIcon.AC_HEALTH
            )
        )
        ImpactPeriod.YEARLY -> listOf(
            SavingsEquivalentFact(
                title = "Morning Ritual",
                value = "48",
                unit = "Coffees",
                description = "Nearly a full year of weekly morning coffee runs completely covered.",
                iconType = SavingsFactIcon.COFFEE
            ),
            SavingsEquivalentFact(
                title = "Home Broadband",
                value = "~3",
                unit = "Months",
                description = "Equivalent to nearly three billing cycles of high-speed home fiber Wi-Fi.",
                iconType = SavingsFactIcon.SUBSCRIPTION
            ),
            SavingsEquivalentFact(
                title = "Motor Preservation",
                value = "675",
                unit = "hrs",
                description = "Nearly an entire month of mechanical compressor rest saved over the year.",
                iconType = SavingsFactIcon.AC_HEALTH
            )
        )
        ImpactPeriod.LIFETIME -> listOf(
            SavingsEquivalentFact(
                title = "Quiet Treats",
                value = "32",
                unit = "Coffees",
                description = "Over thirty morning lattes paid for purely through intelligent sleep sync.",
                iconType = SavingsFactIcon.COFFEE
            ),
            SavingsEquivalentFact(
                title = "Internet Bills",
                value = "2",
                unit = "Months",
                description = "Two full monthly broadband subscriptions redirected to your savings.",
                iconType = SavingsFactIcon.SUBSCRIPTION
            ),
            SavingsEquivalentFact(
                title = "AC Unit Longevity",
                value = "450",
                unit = "hrs",
                description = "Cumulative compressor relief extending component life by estimated years.",
                iconType = SavingsFactIcon.AC_HEALTH
            )
        )
    }

    fun getCarbonTopHighlight(period: ImpactPeriod): String = when (period) {
        ImpactPeriod.WEEKLY -> "🌱 0.3 Trees Working"
        ImpactPeriod.MONTHLY -> "🌿 1.4 Trees Working"
        ImpactPeriod.YEARLY -> "🌳 16.8 Trees Grove"
        ImpactPeriod.LIFETIME -> "🍃 11.2 Trees Offset"
    }

    fun getCarbonFacts(period: ImpactPeriod): List<CarbonEquivalentFact> = when (period) {
        ImpactPeriod.WEEKLY -> listOf(
            CarbonEquivalentFact(
                title = "Urban Tree Work",
                value = "0.3",
                unit = "Trees",
                description = "What a leafy urban tree seedling absorbs from city air over a week.",
                iconType = CarbonFactIcon.TREE
            ),
            CarbonEquivalentFact(
                title = "City Scooter Rides",
                value = "32",
                unit = "km",
                description = "Skipping ~6 daily cross-town scooter trips across South Jakarta.",
                iconType = CarbonFactIcon.COMMUTE
            ),
            CarbonEquivalentFact(
                title = "Phone Charges",
                value = "950+",
                unit = "charges",
                description = "Recharging your smartphone every night for more than two and a half years.",
                iconType = CarbonFactIcon.SMARTPHONE
            ),
            CarbonEquivalentFact(
                title = "Home Lighting",
                value = "13",
                unit = "hrs",
                description = "Illuminating your living room with high-efficiency LED lights for evenings.",
                iconType = CarbonFactIcon.LIGHTING
            )
        )
        ImpactPeriod.MONTHLY -> listOf(
            CarbonEquivalentFact(
                title = "Urban Tree Work",
                value = "1.4",
                unit = "Trees",
                description = "What a mature urban tree absorbs and filters out over an entire month.",
                iconType = CarbonFactIcon.TREE
            ),
            CarbonEquivalentFact(
                title = "City Commute Avoided",
                value = "138",
                unit = "km",
                description = "Equal to ~25 daily scooter rides or driving from Jakarta to Bandung.",
                iconType = CarbonFactIcon.COMMUTE
            ),
            CarbonEquivalentFact(
                title = "Phone Recharges",
                value = "4,100+",
                unit = "charges",
                description = "Enough clean energy to charge your phone from 0% to 100% daily for 11 years.",
                iconType = CarbonFactIcon.SMARTPHONE
            ),
            CarbonEquivalentFact(
                title = "Living Room Lights",
                value = "57",
                unit = "hrs",
                description = "Continuous high-efficiency LED illumination for almost two and a half days.",
                iconType = CarbonFactIcon.LIGHTING
            )
        )
        ImpactPeriod.YEARLY -> listOf(
            CarbonEquivalentFact(
                title = "Personal Urban Grove",
                value = "16.8",
                unit = "Trees",
                description = "A personal cluster of urban trees filtering Jakarta's tropical skyline.",
                iconType = CarbonFactIcon.TREE
            ),
            CarbonEquivalentFact(
                title = "Island Road Trip",
                value = "1,660",
                unit = "km",
                description = "Driving the entire length of Java from Anyer to Banyuwangi and back.",
                iconType = CarbonFactIcon.COMMUTE
            ),
            CarbonEquivalentFact(
                title = "Phone Charges",
                value = "49,000+",
                unit = "charges",
                description = "Decades of smartphone power saved from nighttime peaker power plants.",
                iconType = CarbonFactIcon.SMARTPHONE
            ),
            CarbonEquivalentFact(
                title = "Continuous Glow",
                value = "680",
                unit = "hrs",
                description = "Nearly an entire month of continuous around-the-clock room lighting.",
                iconType = CarbonFactIcon.LIGHTING
            )
        )
        ImpactPeriod.LIFETIME -> listOf(
            CarbonEquivalentFact(
                title = "Cumulative Forest",
                value = "11.2",
                unit = "Trees",
                description = "Continuous carbon absorption across your active Klimata operating history.",
                iconType = CarbonFactIcon.TREE
            ),
            CarbonEquivalentFact(
                title = "Clean Highway Km",
                value = "1,108",
                unit = "km",
                description = "Over a thousand kilometers of tailpipe emissions avoided while asleep.",
                iconType = CarbonFactIcon.COMMUTE
            ),
            CarbonEquivalentFact(
                title = "Phone Charges",
                value = "32,800+",
                unit = "charges",
                description = "Tens of thousands of battery recharges spared from fossil grid generation.",
                iconType = CarbonFactIcon.SMARTPHONE
            ),
            CarbonEquivalentFact(
                title = "Home Lighting",
                value = "450",
                unit = "hrs",
                description = "Over two and a half weeks of continuous clean home illumination.",
                iconType = CarbonFactIcon.LIGHTING
            )
        )
    }
}
