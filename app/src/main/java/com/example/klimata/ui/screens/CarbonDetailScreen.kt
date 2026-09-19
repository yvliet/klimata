package com.example.klimata.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.adamglin.phosphoricons.light.Car
import com.adamglin.phosphoricons.light.DeviceMobile
import com.adamglin.phosphoricons.light.Leaf
import com.adamglin.phosphoricons.light.Lightbulb
import com.adamglin.phosphoricons.light.MoonStars
import com.adamglin.phosphoricons.light.Tree
import com.adamglin.phosphoricons.light.Wind
import com.example.klimata.data.CarbonEquivalentFact
import com.example.klimata.data.CarbonFactIcon
import com.example.klimata.data.HumanEquivalents
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
import com.example.klimata.ui.theme.WarmAmber

@Composable
fun CarbonDetailScreen(
    room: RoomState = MockData.masterBedRoom,
    onBackClick: () -> Unit = {},
) {
    var selectedPeriod by remember { mutableStateOf(ImpactPeriod.MONTHLY) }
    val currentMetric = room.carbonHistory.forPeriod(selectedPeriod)
    val baseEquiv = room.carbonEquivalence
    val topHighlight = remember(selectedPeriod) {
        HumanEquivalents.getCarbonTopHighlight(selectedPeriod)
    }
    val facts = remember(selectedPeriod) {
        HumanEquivalents.getCarbonFacts(selectedPeriod)
    }

    DetailPageScaffold(
        title = "Avoided Carbon",
        subtitle = "${room.name} • Clean Air & Human Impact",
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
                            imageVector = PhosphorIcons.Light.Tree,
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
                    text = "Kept out of our city skies without making your room feel warm.",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = DetailTextMuted
                    )
                )
            }
        }

        // Did You Know: Tangible Ecological Equivalents
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
                    text = "Did You Know? Real-World Footprint",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = DetailTextPrimary
                    ),
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )

                Text(
                    text = "Tangible equivalents",
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
                    CarbonFactItemCard(
                        fact = facts[0],
                        modifier = Modifier.weight(1f)
                    )
                    CarbonFactItemCard(
                        fact = facts[1],
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (facts.size >= 4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CarbonFactItemCard(
                        fact = facts[2],
                        modifier = Modifier.weight(1f)
                    )
                    CarbonFactItemCard(
                        fact = facts[3],
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Neighborhood Air Quality Story Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(DetailCardSurface)
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF38BDF8).copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Light.Wind,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Why Nighttime AC Matters in Jakarta",
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp,
                                color = DetailTextPrimary
                            )
                        )
                        Text(
                            text = "Java-Bali grid intensity: ~${baseEquiv.gridEmissionFactor} kg CO₂e / kWh",
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp,
                                color = DetailTextMuted
                            )
                        )
                    }
                }

                Text(
                    text = "Between midnight and 4 AM, urban power grids often fire up fossil-fuel peaking generators to feed millions of residential air conditioners blasting simultaneously. When your AC drifts gently instead of fighting the cool night air, you directly lower peak power plant dispatch — keeping smoke and soot out of our morning skies.",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = DetailTextSecondary
                    )
                )
            }
        }

        // Sleep Biology & Zero Discomfort Harmony
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(DetailCardSurface)
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MineralMintActive.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Light.MoonStars,
                            contentDescription = null,
                            tint = MineralMintActive,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Sleep Comfort Meets Green Impact",
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp,
                                color = DetailTextPrimary
                            )
                        )
                        Text(
                            text = "Circadian metabolic synchronization",
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp,
                                color = DetailTextMuted
                            )
                        )
                    }
                }

                Text(
                    text = "During deep sleep, your core body temperature naturally drops by ~1°C. An unthrottled 20°C flat setting forces you to shiver and pull heavy blankets, burning unnecessary power. Klimata's stepped curve matches your natural sleep physiology: healthier for your body, cooler for the planet.",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = DetailTextSecondary
                    )
                )
            }
        }

        // Lifetime Cumulative Milestone
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
                        text = "Lifetime Cumulative Offset",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.5.sp,
                            color = DetailTextMuted
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${room.carbonHistory.lifetime.primaryValue} CO₂e avoided",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MineralMintActive
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
                        text = "Verified Model",
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
private fun CarbonFactItemCard(
    fact: CarbonEquivalentFact,
    modifier: Modifier = Modifier,
) {
    val (icon, tint) = when (fact.iconType) {
        CarbonFactIcon.TREE -> PhosphorIcons.Light.Tree to MineralMintActive
        CarbonFactIcon.COMMUTE -> PhosphorIcons.Light.Car to WarmAmber
        CarbonFactIcon.SMARTPHONE -> PhosphorIcons.Light.DeviceMobile to Color(0xFF38BDF8)
        CarbonFactIcon.LIGHTING -> PhosphorIcons.Light.Lightbulb to Color(0xFFFBBF24)
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
