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
import com.adamglin.phosphoricons.light.Sparkle
import com.example.klimata.data.MockData
import com.example.klimata.data.RoomState
import com.example.klimata.data.ThermalStep
import com.example.klimata.ui.components.DetailPageScaffold
import com.example.klimata.ui.components.ScheduleChart
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.LocalDiurnalColors
import com.example.klimata.ui.theme.MineralMintActive
import com.example.klimata.ui.theme.MineralMintGlow

@Composable
fun ScheduleDetailScreen(
    room: RoomState = MockData.masterBedRoom,
    onBackClick: () -> Unit = {},
) {
    val diurnal = LocalDiurnalColors.current
    DetailPageScaffold(
        title = "Tonight's Schedule",
        subtitle = "${room.name} • Adaptive Thermal Drift",
        onBackClick = onBackClick,
    ) {
        ScheduleChart(
            steps = room.thermalSteps,
            modifier = Modifier.fillMaxWidth()
        )

        // Stepped Progression Timeline
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Automation Milestones",
                style = TextStyle(
                    fontFamily = JakartaFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.90f)
                ),
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            room.thermalSteps.forEachIndexed { index, step ->
                ThermalStepMilestoneCard(
                    step = step,
                    stepNumber = index + 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Domain Thermal Physics Explainer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(diurnal.frostedCardBackground)
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MineralMintActive.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Light.Sparkle,
                            contentDescription = null,
                            tint = MineralMintActive,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = "The Circadian Drift Principle",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    )
                }

                Text(
                    text = "Conventional timers freeze rooms at a flat 20°C all night. Klimata mirrors your biological sleep cycle: initial pre-cooling clears masonry heat, while gentle 1°C steps prevent 3 AM shivering as outdoor temperatures drop to their natural baseline.",
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

        // Hardware Dispatch Status
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
                        text = "Hardware Bridge",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.5.sp,
                            color = Color.White.copy(alpha = 0.60f)
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${room.profile.brand} ${room.profile.model} (${room.profile.capacity})",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MineralMintActive.copy(alpha = 0.16f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = room.dispatchState.dispatchMethod,
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            color = MineralMintActive
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ThermalStepMilestoneCard(
    step: ThermalStep,
    stepNumber: Int,
    modifier: Modifier = Modifier,
) {
    val diurnal = LocalDiurnalColors.current
    val isCoasting = step.setpointCelsius == 0
    val setpointDisplay = if (isCoasting) "Fan Only" else "${step.setpointCelsius}°C"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (step.isActive) Color.White.copy(alpha = 0.14f)
                else diurnal.frostedCardBackground
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            if (step.isActive) MineralMintGlow
                            else Color.White.copy(alpha = 0.12f)
                        )
                ) {
                    if (step.isActive) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MineralMintActive)
                        )
                    } else {
                        Text(
                            text = "$stepNumber",
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        )
                    }
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = step.time,
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "•",
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.40f)
                            )
                        )
                        Text(
                            text = step.label,
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.5.sp,
                                color = if (step.isActive) MineralMintActive else Color.White.copy(alpha = 0.85f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${step.fanMode} • Outdoor ${step.outdoorTemp}°C (${step.deltaLabel})",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.5.sp,
                            color = Color.White.copy(alpha = 0.60f)
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.14f))
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            ) {
                Text(
                    text = setpointDisplay,
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                )
            }
        }
    }
}
