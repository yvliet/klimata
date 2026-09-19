package com.example.klimata.data

import androidx.compose.runtime.Immutable

enum class SavingsFactIcon {
    COFFEE,
    SUBSCRIPTION,
    AC_HEALTH,
    DINING,
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

@Immutable
data class TrendPoint(
    val label: String,
    val value: Float,
    val displayValue: String,
    val isPeak: Boolean = false,
    val isCurrent: Boolean = false,
)

@Immutable
data class TrendChartData(
    val title: String,
    val primaryStat: String,
    val subtitle: String,
    val points: List<TrendPoint>,
)

object HumanEquivalents {

    fun getSavingsTrendData(period: ImpactPeriod): TrendChartData = when (period) {
        ImpactPeriod.WEEKLY -> TrendChartData(
            title = "Daily Savings",
            primaryStat = "Rp 2.800 / night avg",
            subtitle = "Best night: Saturday (Rp 3.300)",
            points = listOf(
                TrendPoint("M", 2.6f, "2.6k"),
                TrendPoint("T", 2.7f, "2.7k"),
                TrendPoint("W", 2.5f, "2.5k"),
                TrendPoint("T", 2.9f, "2.9k"),
                TrendPoint("F", 2.8f, "2.8k"),
                TrendPoint("S", 3.3f, "3.3k", isPeak = true),
                TrendPoint("S", 2.8f, "2.8k", isCurrent = true)
            )
        )
        ImpactPeriod.MONTHLY -> TrendChartData(
            title = "Weekly Pace",
            primaryStat = "Rp 21.100 / week avg",
            subtitle = "Best week: Week 3 (Rp 22.800)",
            points = listOf(
                TrendPoint("W1", 20.5f, "20.5k"),
                TrendPoint("W2", 21.0f, "21.0k"),
                TrendPoint("W3", 22.8f, "22.8k", isPeak = true),
                TrendPoint("W4", 20.2f, "20.2k", isCurrent = true)
            )
        )
        ImpactPeriod.YEARLY -> TrendChartData(
            title = "Monthly History",
            primaryStat = "Rp 84.500 / mo avg",
            subtitle = "Peak season: July (Rp 94.000)",
            points = listOf(
                TrendPoint("J", 82f, "82k"),
                TrendPoint("F", 80f, "80k"),
                TrendPoint("M", 84f, "84k"),
                TrendPoint("A", 86f, "86k"),
                TrendPoint("M", 88f, "88k"),
                TrendPoint("J", 90f, "90k"),
                TrendPoint("J", 94f, "94k", isPeak = true),
                TrendPoint("A", 92f, "92k"),
                TrendPoint("S", 85f, "85k", isCurrent = true),
                TrendPoint("O", 79f, "79k"),
                TrendPoint("N", 76f, "76k"),
                TrendPoint("D", 78f, "78k")
            )
        )
        ImpactPeriod.LIFETIME -> TrendChartData(
            title = "Operating Timeline",
            primaryStat = "8 months active",
            subtitle = "Average: Rp 84.500 / month",
            points = listOf(
                TrendPoint("Feb", 80f, "80k"),
                TrendPoint("Mar", 84f, "84k"),
                TrendPoint("Apr", 86f, "86k"),
                TrendPoint("May", 88f, "88k"),
                TrendPoint("Jun", 90f, "90k"),
                TrendPoint("Jul", 94f, "94k", isPeak = true),
                TrendPoint("Aug", 92f, "92k"),
                TrendPoint("Sep", 85f, "85k", isCurrent = true)
            )
        )
    }

    fun getCarbonTrendData(period: ImpactPeriod): TrendChartData = when (period) {
        ImpactPeriod.WEEKLY -> TrendChartData(
            title = "Daily Avoided Carbon",
            primaryStat = "1.1 kg / night avg",
            subtitle = "Cleanest night: Saturday (1.3 kg)",
            points = listOf(
                TrendPoint("M", 1.1f, "1.1"),
                TrendPoint("T", 1.1f, "1.1"),
                TrendPoint("W", 1.0f, "1.0"),
                TrendPoint("T", 1.2f, "1.2"),
                TrendPoint("F", 1.2f, "1.2"),
                TrendPoint("S", 1.3f, "1.3", isPeak = true),
                TrendPoint("S", 1.1f, "1.1", isCurrent = true)
            )
        )
        ImpactPeriod.MONTHLY -> TrendChartData(
            title = "Weekly Abatement",
            primaryStat = "8.5 kg / week avg",
            subtitle = "Best week: Week 3 (9.1 kg)",
            points = listOf(
                TrendPoint("W1", 8.2f, "8.2"),
                TrendPoint("W2", 8.5f, "8.5"),
                TrendPoint("W3", 9.1f, "9.1", isPeak = true),
                TrendPoint("W4", 8.4f, "8.4", isCurrent = true)
            )
        )
        ImpactPeriod.YEARLY -> TrendChartData(
            title = "Monthly Emissions Offset",
            primaryStat = "34.2 kg / mo avg",
            subtitle = "Peak offset: July (38.0 kg)",
            points = listOf(
                TrendPoint("J", 33f, "33"),
                TrendPoint("F", 32f, "32"),
                TrendPoint("M", 34f, "34"),
                TrendPoint("A", 35f, "35"),
                TrendPoint("M", 36f, "36"),
                TrendPoint("J", 37f, "37"),
                TrendPoint("J", 38f, "38", isPeak = true),
                TrendPoint("A", 37f, "37"),
                TrendPoint("S", 34f, "34", isCurrent = true),
                TrendPoint("O", 32f, "32"),
                TrendPoint("N", 31f, "31"),
                TrendPoint("D", 31f, "31")
            )
        )
        ImpactPeriod.LIFETIME -> TrendChartData(
            title = "Operating Timeline",
            primaryStat = "8 months verified",
            subtitle = "Average: 34.2 kg / month",
            points = listOf(
                TrendPoint("Feb", 32f, "32"),
                TrendPoint("Mar", 34f, "34"),
                TrendPoint("Apr", 35f, "35"),
                TrendPoint("May", 36f, "36"),
                TrendPoint("Jun", 37f, "37"),
                TrendPoint("Jul", 38f, "38", isPeak = true),
                TrendPoint("Aug", 37f, "37"),
                TrendPoint("Sep", 34f, "34", isCurrent = true)
            )
        )
    }

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
            ),
            SavingsEquivalentFact(
                title = "It's like..",
                value = "1",
                unit = "Meal",
                description = "A street food dinner or quick lunch delivery covered.",
                iconType = SavingsFactIcon.DINING
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
            ),
            SavingsEquivalentFact(
                title = "It's like..",
                value = "2",
                unit = "Meals",
                description = "A couple of cozy takeout dinners or boba cafe runs.",
                iconType = SavingsFactIcon.DINING
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
            ),
            SavingsEquivalentFact(
                title = "It's like..",
                value = "24",
                unit = "Meals",
                description = "Two dozen favorite restaurant dinners or food deliveries.",
                iconType = SavingsFactIcon.DINING
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
            ),
            SavingsEquivalentFact(
                title = "It's like..",
                value = "16",
                unit = "Meals",
                description = "Over a dozen takeout dinners paid for with power savings.",
                iconType = SavingsFactIcon.DINING
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
