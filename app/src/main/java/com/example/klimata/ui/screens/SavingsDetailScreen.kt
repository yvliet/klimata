package com.example.klimata.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.adamglin.phosphoricons.light.CurrencyDollar
import com.example.klimata.data.ImpactPeriod
import com.example.klimata.data.MockData
import com.example.klimata.data.RoomState
import com.example.klimata.ui.components.DetailPageScaffold
import com.example.klimata.ui.components.PeriodDropdown
import com.example.klimata.ui.theme.DetailCardSurface
import com.example.klimata.ui.theme.DetailCardSurfaceElevated
import com.example.klimata.ui.theme.DetailTextMuted
import com.example.klimata.ui.theme.DetailTextPrimary
import com.example.klimata.ui.theme.DetailTextSecondary
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.MineralMintActive

@Composable
fun SavingsDetailScreen(
    room: RoomState = MockData.masterBedRoom,
    onBackClick: () -> Unit = {},
) {
    var selectedPeriod by remember { mutableStateOf(ImpactPeriod.MONTHLY) }
    val currentMetric = room.savingsHistory.forPeriod(selectedPeriod)
    val breakdown = room.savingsBreakdown

    DetailPageScaffold(
        title = "${selectedPeriod.label} Savings",
        subtitle = "${room.name} • Utility Ledger",
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
                            text = "-38% kWh",
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
                    text = currentMetric.subtitle,
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = DetailTextSecondary
                    )
                )
            }
        }

        // Mini Breakdown Bar Visualizer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(DetailCardSurface)
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Savings Distribution",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = DetailTextPrimary
                    )
                )

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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MineralMintActive)
                        )
                        Column {
                            Text(
                                text = "Compressor Throttling",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = DetailTextPrimary
                                )
                            )
                            Text(
                                text = "${breakdown.compressorCyclingPercent}% of total savings",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 11.sp,
                                    color = DetailTextSecondary
                                )
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF636366))
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Thermal Coasting",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = DetailTextPrimary
                                )
                            )
                            Text(
                                text = "${breakdown.fanCoastingPercent}% of total savings",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 11.sp,
                                    color = DetailTextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Calculation Transparency Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(DetailCardSurface)
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Tariff & Computational Basis",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = DetailTextPrimary
                    )
                )

                Text(
                    text = "Computed from ~${breakdown.avgNightlyKwhSaved} kWh avoided per night against an unthrottled 20°C flat baseline, benchmarked at ${breakdown.localTariffPerKwh}/kWh (PLN R-1 residential tariff).",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = DetailTextSecondary
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

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
