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
    val title: String = "It's like..",
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
    val title: String = "It's like..",
    val value: String,
    val unit: String,
    val description: String,
    val iconType: CarbonFactIcon,
)

object HumanEquivalents {

    fun getSavingsFacts(period: ImpactPeriod): List<SavingsEquivalentFact> = when (period) {
        ImpactPeriod.WEEKLY -> listOf(
            SavingsEquivalentFact(
                title = "It's like..",
                value = "1",
                unit = "Coffee",
                description = "A cup of iced coffee or breakfast on your way to work.",
                iconType = SavingsFactIcon.COFFEE
            ),
            SavingsEquivalentFact(
                title = "It's like..",
                value = "0.3",
                unit = "Mo",
                description = "About a week of your Spotify or Netflix bill.",
                iconType = SavingsFactIcon.SUBSCRIPTION
            ),
            SavingsEquivalentFact(
                title = "It's like..",
                value = "13",
                unit = "hrs",
                description = "Giving your AC compressor a half-day break so it lasts longer.",
                iconType = SavingsFactIcon.AC_HEALTH
            )
        )
        ImpactPeriod.MONTHLY -> listOf(
            SavingsEquivalentFact(
                title = "It's like..",
                value = "4",
                unit = "Coffees",
                description = "Treating yourself to 4 iced coffees or morning breakfasts.",
                iconType = SavingsFactIcon.COFFEE
            ),
            SavingsEquivalentFact(
                title = "It's like..",
                value = "1.5",
                unit = "Months",
                description = "A whole month of Spotify or Netflix paid for.",
                iconType = SavingsFactIcon.SUBSCRIPTION
            ),
            SavingsEquivalentFact(
                title = "It's like..",
                value = "56",
                unit = "hrs",
                description = "Over two full days of your AC compressor resting.",
                iconType = SavingsFactIcon.AC_HEALTH
            )
        )
        ImpactPeriod.YEARLY -> listOf(
            SavingsEquivalentFact(
                title = "It's like..",
                value = "48",
                unit = "Coffees",
                description = "Almost a full year of weekly morning coffee runs.",
                iconType = SavingsFactIcon.COFFEE
            ),
            SavingsEquivalentFact(
                title = "It's like..",
                value = "3",
                unit = "Months",
                description = "About 3 months of home Wi-Fi paid for.",
                iconType = SavingsFactIcon.SUBSCRIPTION
            ),
            SavingsEquivalentFact(
                title = "It's like..",
                value = "675",
                unit = "hrs",
                description = "Nearly a whole month of your AC motor resting.",
                iconType = SavingsFactIcon.AC_HEALTH
            )
        )
        ImpactPeriod.LIFETIME -> listOf(
            SavingsEquivalentFact(
                title = "It's like..",
                value = "32",
                unit = "Coffees",
                description = "Over thirty morning coffees covered by smart cooling.",
                iconType = SavingsFactIcon.COFFEE
            ),
            SavingsEquivalentFact(
                title = "It's like..",
                value = "2",
                unit = "Months",
                description = "Two months of home internet bills saved.",
                iconType = SavingsFactIcon.SUBSCRIPTION
            ),
            SavingsEquivalentFact(
                title = "It's like..",
                value = "450",
                unit = "hrs",
                description = "Weeks of compressor rest, saving money on AC maintenance.",
                iconType = SavingsFactIcon.AC_HEALTH
            )
        )
    }

    fun getCarbonFacts(period: ImpactPeriod): List<CarbonEquivalentFact> = when (period) {
        ImpactPeriod.WEEKLY -> listOf(
            CarbonEquivalentFact(
                title = "It's like..",
                value = "0.3",
                unit = "Trees",
                description = "A young tree absorbing carbon for an entire week.",
                iconType = CarbonFactIcon.TREE
            ),
            CarbonEquivalentFact(
                title = "It's like..",
                value = "32",
                unit = "km",
                description = "Skipping about 6 scooter trips across town.",
                iconType = CarbonFactIcon.COMMUTE
            ),
            CarbonEquivalentFact(
                title = "It's like..",
                value = "950+",
                unit = "charges",
                description = "Charging your phone every night for over two years.",
                iconType = CarbonFactIcon.SMARTPHONE
            ),
            CarbonEquivalentFact(
                title = "It's like..",
                value = "13",
                unit = "hrs",
                description = "Keeping the living room lights on all evening.",
                iconType = CarbonFactIcon.LIGHTING
            )
        )
        ImpactPeriod.MONTHLY -> listOf(
            CarbonEquivalentFact(
                title = "It's like..",
                value = "1.4",
                unit = "Trees",
                description = "A tree absorbing carbon from the air for a whole month.",
                iconType = CarbonFactIcon.TREE
            ),
            CarbonEquivalentFact(
                title = "It's like..",
                value = "138",
                unit = "km",
                description = "Skipping 25 scooter rides, or driving to Bandung.",
                iconType = CarbonFactIcon.COMMUTE
            ),
            CarbonEquivalentFact(
                title = "It's like..",
                value = "4,100+",
                unit = "charges",
                description = "Charging your phone every day for 11 years.",
                iconType = CarbonFactIcon.SMARTPHONE
            ),
            CarbonEquivalentFact(
                title = "It's like..",
                value = "57",
                unit = "hrs",
                description = "Leaving your living room lights on for over two days straight.",
                iconType = CarbonFactIcon.LIGHTING
            )
        )
        ImpactPeriod.YEARLY -> listOf(
            CarbonEquivalentFact(
                title = "It's like..",
                value = "16.8",
                unit = "Trees",
                description = "A small grove of trees cleaning city air for a year.",
                iconType = CarbonFactIcon.TREE
            ),
            CarbonEquivalentFact(
                title = "It's like..",
                value = "1,660",
                unit = "km",
                description = "Driving all the way across Java and back.",
                iconType = CarbonFactIcon.COMMUTE
            ),
            CarbonEquivalentFact(
                title = "It's like..",
                value = "49,000+",
                unit = "charges",
                description = "Decades worth of charging your phone every night.",
                iconType = CarbonFactIcon.SMARTPHONE
            ),
            CarbonEquivalentFact(
                title = "It's like..",
                value = "680",
                unit = "hrs",
                description = "A month of continuous light without wasting power.",
                iconType = CarbonFactIcon.LIGHTING
            )
        )
        ImpactPeriod.LIFETIME -> listOf(
            CarbonEquivalentFact(
                title = "It's like..",
                value = "11.2",
                unit = "Trees",
                description = "Over 11 trees absorbing carbon while you used Klimata.",
                iconType = CarbonFactIcon.TREE
            ),
            CarbonEquivalentFact(
                title = "It's like..",
                value = "1,108",
                unit = "km",
                description = "Over a thousand kilometers of road trips without emissions.",
                iconType = CarbonFactIcon.COMMUTE
            ),
            CarbonEquivalentFact(
                title = "It's like..",
                value = "32,800+",
                unit = "charges",
                description = "Tens of thousands of phone charges kept off the grid.",
                iconType = CarbonFactIcon.SMARTPHONE
            ),
            CarbonEquivalentFact(
                title = "It's like..",
                value = "450",
                unit = "hrs",
                description = "Weeks of clean home lighting powered by saved energy.",
                iconType = CarbonFactIcon.LIGHTING
            )
        )
    }
}
