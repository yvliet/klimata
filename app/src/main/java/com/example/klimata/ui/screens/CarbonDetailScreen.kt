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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klimata.data.ImpactPeriod
import com.example.klimata.data.MockData
import com.example.klimata.data.RoomState
import com.example.klimata.ui.components.DetailPageScaffold
import com.example.klimata.ui.components.PeriodDropdown
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.LocalDiurnalColors
import com.example.klimata.ui.theme.MineralMintActive

@Composable
fun CarbonDetailScreen(
    room: RoomState = MockData.masterBedRoom,
    onBackClick: () -> Unit = {},
) {
    val diurnal = LocalDiurnalColors.current
    var selectedPeriod by remember { mutableStateOf(ImpactPeriod.MONTHLY) }
    val currentMetric = room.carbonHistory.forPeriod(selectedPeriod)
    val baseEquiv = room.carbonEquivalence

    // Scale tangible metrics based on selected period
    val periodScale = when (selectedPeriod) {
        ImpactPeriod.WEEKLY -> 0.23f
        ImpactPeriod.MONTHLY -> 1.0f
        ImpactPeriod.YEARLY -> 12.0f
        ImpactPeriod.LIFETIME -> 8.0f
    }

    val scaledTrees = String.format(java.util.Locale.US, "%.1f", baseEquiv.treesEquivalent * periodScale)
    val scaledDrivingKm = String.format(java.util.Locale.US, "%.1f", baseEquiv.drivingKmAvoided * periodScale)
    val scaledLedHours = String.format(java.util.Locale.US, "%.1f", baseEquiv.ledHoursEquivalent * periodScale)

    DetailPageScaffold(
        title = "Avoided Carbon",
        subtitle = "${room.name} • Ecological Balance",
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
                .background(diurnal.frostedCardBackground)
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
                            .background(Color.White.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = safeLeafIcon(),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$scaledTrees Trees eq.",
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = Color.White
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
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${currentMetric.subtitle} (${selectedPeriod.label})",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.70f)
                    )
                )
            }
        }

        // Tangible Physical Equivalencies Grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Tangible Ecological Equivalent",
                style = TextStyle(
                    fontFamily = JakartaFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White
                ),
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EquivalenceCard(
                    title = "Arboreal Offset",
                    value = "$scaledTrees",
                    unit = "Trees",
                    description = "Mature urban tree absorption",
                    modifier = Modifier.weight(1f)
                )

                EquivalenceCard(
                    title = "ICE Driving",
                    value = "$scaledDrivingKm",
                    unit = "km",
                    description = "Combustion tailpipe equivalent",
                    modifier = Modifier.weight(1f)
                )
            }

            EquivalenceCard(
                title = "Lighting Preservation",
                value = "$scaledLedHours",
                unit = "hrs",
                description = "High-efficiency home LED illumination powered",
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Calculation Transparency Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(diurnal.frostedCardBackground)
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Grid Emission Factor Methodology",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                )

                Text(
                    text = "Carbon abatement is modeled using the Java-Bali grid emission intensity of ~${baseEquiv.gridEmissionFactor} kg CO₂e per kWh. Curtailing unnecessary midnight compressor cycling directly reduces peaking fossil fuel generation during nighttime dispatch.",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                )
            }
        }

        // Lifetime Cumulative Milestone
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(diurnal.frostedCardBackground)
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
                            color = Color.White.copy(alpha = 0.60f)
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = room.carbonHistory.lifetime.primaryValue,
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MineralMintActive
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Verified Model",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun EquivalenceCard(
    title: String,
    value: String,
    unit: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    val diurnal = LocalDiurnalColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(diurnal.frostedCardBackground)
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = JakartaFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.65f)
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MineralMintActive
                    ),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = TextStyle(
                    fontFamily = JakartaFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = Color.White.copy(alpha = 0.55f)
                )
            )
        }
    }
}

private val FallbackLeafIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Leaf",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(2f, 22f); lineTo(12f, 12f)
            moveTo(20f, 4f)
            curveTo(12f, 4f, 4f, 12f, 4f, 20f)
            curveTo(12f, 20f, 20f, 12f, 20f, 4f)
            close()
        }
    }.build()
}

private fun getPhosphorLightIcon(iconName: String, fallback: ImageVector): ImageVector {
    return try {
        val clazz = Class.forName("com.adamglin.phosphoricons.light.${iconName}Kt")
        val phosphorIconsClass = Class.forName("com.adamglin.PhosphorIcons")
        val lightField = phosphorIconsClass.getField("Light")
        val lightObj = lightField[null]
        val lightClass = Class.forName("com.adamglin.PhosphorIcons\$Light")
        val method = clazz.getMethod("get$iconName", lightClass)
        method.invoke(null, lightObj) as ImageVector
    } catch (_: Throwable) {
        fallback
    }
}

@Composable
private fun safeLeafIcon(): ImageVector {
    if (LocalInspectionMode.current) {
        return FallbackLeafIcon
    }
    return getPhosphorLightIcon("Leaf", FallbackLeafIcon)
}
