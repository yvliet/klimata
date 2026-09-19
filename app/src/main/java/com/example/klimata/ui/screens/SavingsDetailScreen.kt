package com.example.klimata.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.Coffee
import com.adamglin.phosphoricons.light.CurrencyDollar
import com.adamglin.phosphoricons.light.Heart
import com.adamglin.phosphoricons.light.Sparkle
import com.example.klimata.data.HumanEquivalents
import com.example.klimata.data.ImpactPeriod
import com.example.klimata.data.MockData
import com.example.klimata.data.RoomState
import com.example.klimata.data.SavingsEquivalentFact
import com.example.klimata.data.SavingsFactIcon
import com.example.klimata.ui.components.DetailPageScaffold
import com.example.klimata.ui.components.NightlyTrendCard
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
    val trendData = remember(selectedPeriod) {
        HumanEquivalents.getSavingsTrendData(selectedPeriod)
    }
    val facts = remember(selectedPeriod) {
        HumanEquivalents.getSavingsFacts(selectedPeriod)
    }

    DetailPageScaffold(
        title = "${selectedPeriod.label} Savings",
        subtitle = room.name,
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
                    text = "Saved while you sleep, without making your room feel any warmer.",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = DetailTextMuted
                    )
                )
            }
        }

        // Dynamic Horizon Trend & Statistics Graph
        NightlyTrendCard(
            trendData = trendData,
            accentColor = MineralMintActive,
            modifier = Modifier.fillMaxWidth()
        )

        // Did You Know: Tangible Equivalents
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Did You Know?",
                style = TextStyle(
                    fontFamily = JakartaFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = DetailTextPrimary
                ),
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )

            if (facts.size >= 2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SavingsFactItemCard(
                        fact = facts[0],
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    SavingsFactItemCard(
                        fact = facts[1],
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
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
                if (fact.unit.isNotEmpty()) {
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
