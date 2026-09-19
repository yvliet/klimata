package com.example.klimata.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.Coffee
import com.adamglin.phosphoricons.light.CurrencyDollar
import com.adamglin.phosphoricons.light.Fan
import com.adamglin.phosphoricons.light.Heart
import com.adamglin.phosphoricons.light.Moon
import com.adamglin.phosphoricons.light.Sparkle
import com.example.klimata.data.HumanEquivalents
import com.example.klimata.data.ImpactPeriod
import com.example.klimata.data.MockData
import com.example.klimata.data.RoomState
import com.example.klimata.data.SavingsEquivalentFact
import com.example.klimata.data.SavingsFactIcon
import com.example.klimata.ui.components.DetailPageScaffold
import com.example.klimata.ui.components.PeriodDropdown
import com.example.klimata.ui.theme.DetailCardSurface
import com.example.klimata.ui.theme.DetailCardSurfaceElevated
import com.example.klimata.ui.theme.DetailTextMuted
import com.example.klimata.ui.theme.DetailTextPrimary
import com.example.klimata.ui.theme.DetailTextSecondary
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.MineralMintActive
import com.example.klimata.ui.theme.WarmAmber

@Composable
fun SavingsDetailScreen(
    room: RoomState = MockData.masterBedRoom,
    onBackClick: () -> Unit = {},
) {
    var selectedPeriod by remember { mutableStateOf(ImpactPeriod.MONTHLY) }
    val currentMetric = room.savingsHistory.forPeriod(selectedPeriod)
    val breakdown = room.savingsBreakdown
    val topHighlight = remember(selectedPeriod) {
        HumanEquivalents.getSavingsTopHighlight(selectedPeriod)
    }
    val facts = remember(selectedPeriod) {
        HumanEquivalents.getSavingsFacts(selectedPeriod)
    }

    DetailPageScaffold(
        title = "${selectedPeriod.label} Savings",
        subtitle = "${room.name} • Utility Ledger & Daily Impact",
        onBackClick = onBackClick,
        trailingContent = {
            PeriodDropdown(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { selectedPeriod = it }
            )
        }
    ) {
        // Hero Metric Elevation Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DetailCardSurface)
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MineralMintActive.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Light.CurrencyDollar,
                            contentDescription = null,
                            tint = MineralMintActive,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MineralMintActive.copy(alpha = 0.16f))
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = topHighlight,
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = MineralMintActive
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentMetric.primaryValue,
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        letterSpacing = (-0.5).sp,
                        color = DetailTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${currentMetric.subtitle} (${selectedPeriod.label})",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = DetailTextSecondary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Quietly saved while you sleep without sacrificing room comfort.",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = DetailTextMuted
                    )
                )
            }
        }

        // Did You Know: Real-World Pocket Equivalents
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Did You Know? Everyday Value",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = DetailTextPrimary
                    ),
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )

                Text(
                    text = "What this quietly buys",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.5.sp,
                        color = DetailTextMuted
                    ),
                    modifier = Modifier.padding(end = 4.dp)
                )
            }

            if (facts.size >= 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SavingsFactItemCard(
                        fact = facts[0],
                        modifier = Modifier.weight(1f)
                    )
                    SavingsFactItemCard(
                        fact = facts[1],
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (facts.size >= 3) {
                SavingsFactItemCard(
                    fact = facts[2],
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // The Nighttime Story: Realistic Breakdown
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(DetailCardSurface)
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Column {
                    Text(
                        text = "The Nighttime Story",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = DetailTextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Where the electricity cuts actually happened",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.5.sp,
                            color = DetailTextMuted
                        )
                    )
                }

                // Segmented Ratio Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(DetailCardSurfaceElevated)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .weight(breakdown.compressorCyclingPercent.toFloat())
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(MineralMintActive, Color(0xFF6EE7B7))
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Box(
                            modifier = Modifier
                                .weight(breakdown.fanCoastingPercent.toFloat())
                                .fillMaxHeight()
                                .background(Color(0xFF48484A))
                        )
                    }
                }

                // Narrative Story Blocks
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                            .background(MineralMintActive.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Light.Moon,
                                contentDescription = null,
                                tint = MineralMintActive,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "The 2 AM Metabolic Drift",
                                    style = TextStyle(
                                        fontFamily = JakartaFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.5.sp,
                                        color = DetailTextPrimary
                                    )
                                )
                                Text(
                                    text = "${breakdown.compressorCyclingPercent}% savings",
                                    style = TextStyle(
                                        fontFamily = JakartaFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.5.sp,
                                        color = MineralMintActive
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "As outdoor air cooled down to 26°C, your AC eased up from 24°C to 25°C. Deep sleep naturally lowers your body temperature, so you slept peacefully while the compressor rested for hours.",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp,
                                    color = DetailTextSecondary
                                )
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                            .background(Color(0xFF636366).copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Light.Fan,
                                contentDescription = null,
                                tint = Color(0xFFA1A1AA),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Dawn Thermal Coasting",
                                    style = TextStyle(
                                        fontFamily = JakartaFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.5.sp,
                                        color = DetailTextPrimary
                                    )
                                )
                                Text(
                                    text = "${breakdown.fanCoastingPercent}% savings",
                                    style = TextStyle(
                                        fontFamily = JakartaFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.5.sp,
                                        color = DetailTextSecondary
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "From 5:30 AM to sunrise, cold air remained trapped in your room's thermal mass. Klimata disengaged the heavy compressor entirely and floated on gentle fan circulation.",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp,
                                    color = DetailTextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Realistic Math & Hardware Profile Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(DetailCardSurface)
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Honest Math & Grid Reality",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = DetailTextPrimary
                    )
                )

                Text(
                    text = "Based on avoiding ~${breakdown.avgNightlyKwhSaved} kWh each night compared to a continuous flat 20°C freeze. Benchmarked at ${breakdown.localTariffPerKwh}/kWh (standard PLN R-1 residential tariff).",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = DetailTextSecondary
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hardware Profile",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = DetailTextMuted
                        )
                    )
                    Text(
                        text = "${room.profile.brand} ${room.profile.inverterType}",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = DetailTextPrimary
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DetailCardSurfaceElevated)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "💡 Prolonged AC Lifespan: Reducing aggressive start-stop compressor bursts prevents motor overheating, helping your unit last years longer.",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                            color = DetailTextSecondary
                        )
                    )
                }
            }
        }

        // Cross-Horizon Projection Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(DetailCardSurface)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when (selectedPeriod) {
                            ImpactPeriod.WEEKLY -> "Monthly Projection"
                            ImpactPeriod.MONTHLY -> "Annual Projection"
                            ImpactPeriod.YEARLY -> "Lifetime Trajectory"
                            ImpactPeriod.LIFETIME -> "Active Operating Window"
                        },
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.5.sp,
                            color = DetailTextMuted
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when (selectedPeriod) {
                            ImpactPeriod.WEEKLY -> room.savingsHistory.monthly.primaryValue
                            ImpactPeriod.MONTHLY -> room.savingsHistory.yearly.primaryValue
                            ImpactPeriod.YEARLY -> room.savingsHistory.lifetime.primaryValue
                            ImpactPeriod.LIFETIME -> "8 months under Klimata sync"
                        },
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = DetailTextPrimary
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DetailCardSurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Estimated",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = DetailTextSecondary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SavingsFactItemCard(
    fact: SavingsEquivalentFact,
    modifier: Modifier = Modifier,
) {
    val (icon, tint) = when (fact.iconType) {
        SavingsFactIcon.COFFEE -> PhosphorIcons.Light.Coffee to WarmAmber
        SavingsFactIcon.SUBSCRIPTION -> PhosphorIcons.Light.Sparkle to Color(0xFF38BDF8)
        SavingsFactIcon.AC_HEALTH -> PhosphorIcons.Light.Heart to MineralMintActive
        SavingsFactIcon.GETAWAY -> PhosphorIcons.Light.Sparkle to WarmAmber
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(DetailCardSurface)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fact.title,
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = DetailTextSecondary
                    )
                )

                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(tint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = fact.value,
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = DetailTextPrimary
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = fact.unit,
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = tint
                    ),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = fact.description,
                style = TextStyle(
                    fontFamily = JakartaFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = DetailTextMuted
                )
            )
        }
    }
}
