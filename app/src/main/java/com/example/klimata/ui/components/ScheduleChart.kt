package com.example.klimata.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klimata.data.MockData
import com.example.klimata.data.ThermalStep
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.KlimataTheme
import kotlin.math.roundToInt

/**
 * Visualizes the 4-phase overnight thermal automation progression:
 * - 22:00 (Pre-Cool): High fan setpoint to clear residual daytime masonry heat.
 * - 01:00 (Drift): +1°C step-up matching deep-sleep metabolic deceleration.
 * - 03:30 (Ambient): +1°C step-up as outdoor ambient reaches nocturnal trough.
 * - 05:30 (Coasting): Compressor cutoff, coasting on fan circulation before dawn.
 */
@Composable
fun ScheduleChart(
    modifier: Modifier = Modifier,
    steps: List<ThermalStep> = MockData.thermalSteps,
    onClick: () -> Unit = {},
) {
    val phases = remember(steps) {
        if (steps.size >= 4) {
            listOf(
                ScheduleItem(time = steps[0].time, tempLabel = "${steps[0].setpointCelsius}°", isFanOnly = steps[0].setpointCelsius == 0, phaseName = "Pre-Cool", isActive = steps[0].isActive),
                ScheduleItem(time = steps[1].time, tempLabel = "${steps[1].setpointCelsius}°", isFanOnly = steps[1].setpointCelsius == 0, phaseName = "Drift", isActive = steps[1].isActive),
                ScheduleItem(time = steps[2].time, tempLabel = "${steps[2].setpointCelsius}°", isFanOnly = steps[2].setpointCelsius == 0, phaseName = "Ambient", isActive = steps[2].isActive),
                ScheduleItem(time = steps[3].time, tempLabel = if (steps[3].setpointCelsius > 0) "${steps[3].setpointCelsius}°" else null, isFanOnly = true, phaseName = "Coasting", isActive = steps[3].isActive),
            )
        } else {
            listOf(
                ScheduleItem(time = "22:00", tempLabel = "24°", isFanOnly = false, phaseName = "Pre-Cool", isActive = true),
                ScheduleItem(time = "01:00", tempLabel = "25°", isFanOnly = false, phaseName = "Drift", isActive = false),
                ScheduleItem(time = "03:30", tempLabel = "26°", isFanOnly = false, phaseName = "Ambient", isActive = false),
                ScheduleItem(time = "05:30", tempLabel = null, isFanOnly = true, phaseName = "Coasting", isActive = false),
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .border(1.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tonight's Schedule",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Stepped Drift",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.60f)
                        )
                    )
                    Icon(
                        imageVector = safeCaretRightIcon(),
                        contentDescription = "Open schedule details",
                        tint = Color.White.copy(alpha = 0.50f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val chartHeight = 84.dp
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight)
            ) {
                val widthPx = constraints.maxWidth.toFloat()
                val density = androidx.compose.ui.platform.LocalDensity.current
                val heightPx = with(density) { chartHeight.toPx() }

                val x0 = widthPx * 0.125f
                val x1 = widthPx * 0.375f
                val x2 = widthPx * 0.625f
                val x3 = widthPx * 0.875f

                val topMargin = with(density) { 24.dp.toPx() }
                val bottomMargin = with(density) { 14.dp.toPx() }
                val usableHeight = heightPx - topMargin - bottomMargin

                val y0 = topMargin + (usableHeight * 0.95f)
                val y1 = topMargin + (usableHeight * 0.62f)
                val y2 = topMargin + (usableHeight * 0.30f)
                val y3 = topMargin + (usableHeight * 0.05f)

                val points = listOf(
                    Offset(x0, y0),
                    Offset(x1, y1),
                    Offset(x2, y2),
                    Offset(x3, y3)
                )

                Canvas(modifier = Modifier.fillMaxWidth().height(chartHeight)) {
                    val w = size.width
                    val h = size.height

                    drawLine(
                        color = Color.White.copy(alpha = 0.28f),
                        start = Offset(x0, y0),
                        end = Offset(x0, h),
                        strokeWidth = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )

                    // S-curves between steps represent thermal equilibration lag rather than instant room shifts
                    val curvePath = Path().apply {
                        moveTo(0f, y0)
                        lineTo(x0, y0)

                        val dx01 = x1 - x0
                        cubicTo(
                            x0 + dx01 * 0.5f, y0,
                            x0 + dx01 * 0.5f, y1,
                            x1, y1
                        )

                        val dx12 = x2 - x1
                        cubicTo(
                            x1 + dx12 * 0.5f, y1,
                            x1 + dx12 * 0.5f, y2,
                            x2, y2
                        )

                        val dx23 = x3 - x2
                        cubicTo(
                            x2 + dx23 * 0.5f, y2,
                            x2 + dx23 * 0.5f, y3,
                            x3, y3
                        )

                        lineTo(w, y3)
                    }

                    val fillPath = Path().apply {
                        addPath(curvePath)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.12f),
                                Color.White.copy(alpha = 0.03f),
                                Color.Transparent
                            )
                        )
                    )

                    drawPath(
                        path = curvePath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.95f),
                                Color.White.copy(alpha = 0.85f),
                                Color.White.copy(alpha = 0.70f)
                            )
                        ),
                        style = Stroke(
                            width = 2.4.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )

                    points.forEachIndexed { index, point ->
                        val isActive = phases.getOrNull(index)?.isActive == true
                        if (isActive) {
                            drawCircle(
                                color = com.example.klimata.ui.theme.MineralMintGlow,
                                radius = 8.5.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 4.5.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = com.example.klimata.ui.theme.MineralMintActive,
                                radius = 2.5.dp.toPx(),
                                center = point
                            )
                        } else {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.25f),
                                radius = 5.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3.5.dp.toPx(),
                                center = point
                            )
                        }
                    }
                }

                // Modifier.layout positions actual Composable nodes above canvas coordinates,
                // avoiding TextMeasurer allocation overhead and text caching churn inside Canvas.
                points.forEachIndexed { index, point ->
                    val item = phases[index]
                    Box(
                        modifier = Modifier.layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            layout(placeable.width, placeable.height) {
                                val posX = (point.x - placeable.width / 2f).roundToInt()
                                val posY = (point.y - placeable.height - 7.dp.toPx()).roundToInt()
                                placeable.placeRelative(posX, posY)
                            }
                        }
                    ) {
                        if (item.isFanOnly || item.tempLabel == null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = safeFanIcon(),
                                    contentDescription = "Fan Only",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Fan",
                                    style = TextStyle(
                                        fontFamily = JakartaFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                )
                            }
                        } else {
                            Text(
                                text = item.tempLabel,
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                phases.forEachIndexed { index, phase ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = phase.time,
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = if (phase.isActive) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 12.sp,
                                color = if (phase.isActive) Color.White else Color.White.copy(alpha = 0.85f)
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = phase.phaseName,
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp,
                                color = if (index == 3) Color(0xFF6EE7B7) else Color.White.copy(alpha = 0.60f)
                            )
                        )
                    }
                }
            }
        }
    }
}

private data class ScheduleItem(
    val time: String,
    val tempLabel: String?,
    val isFanOnly: Boolean,
    val phaseName: String,
    val isActive: Boolean = false,
)

private val FallbackFanIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Fan",
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
            moveTo(12f, 12f)
            curveTo(12f, 7f, 16f, 4f, 18f, 6f)
            curveTo(19f, 8f, 16f, 12f, 12f, 12f)
            curveTo(7f, 12f, 4f, 16f, 6f, 18f)
            curveTo(8f, 19f, 12f, 16f, 12f, 12f)
            curveTo(12f, 17f, 8f, 20f, 6f, 18f)
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
private fun safeFanIcon(): ImageVector {
    if (LocalInspectionMode.current) {
        return FallbackFanIcon
    }
    return getPhosphorLightIcon("Fan", FallbackFanIcon)
}

private val FallbackCaretRightIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "CaretRight",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2.2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(9f, 6f)
            lineTo(15f, 12f)
            lineTo(9f, 18f)
        }
    }.build()
}

@Composable
private fun safeCaretRightIcon(): ImageVector {
    if (LocalInspectionMode.current) {
        return FallbackCaretRightIcon
    }
    return getPhosphorLightIcon("CaretRight", FallbackCaretRightIcon)
}

@Preview(showBackground = true, backgroundColor = 0xFF1976D2)
@Composable
private fun ScheduleChartPreview() {
    KlimataTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ScheduleChart()
        }
    }
}
