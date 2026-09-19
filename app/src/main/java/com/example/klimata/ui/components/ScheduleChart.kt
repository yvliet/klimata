package com.example.klimata.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.getValue
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

import com.example.klimata.ui.theme.LocalDiurnalColors

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
    val diurnal = LocalDiurnalColors.current
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
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.85f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.04f))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .background(diurnal.frostedCardBackground)
            .clickable(onClick = onClick)
            .padding(18.dp)
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
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.60f)
                    )
                )

                Icon(
                    imageVector = safeCaretRightIcon(),
                    contentDescription = "Open schedule details",
                    tint = Color.White.copy(alpha = 0.50f),
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "Stepped Drift",
                style = TextStyle(
                    fontFamily = JakartaFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White,
                    letterSpacing = (-0.3).sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

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

                val targetY0 = topMargin + (usableHeight * (if (steps.isNotEmpty() && steps[0].setpointCelsius > 0) {
                    1.0f - ((steps[0].setpointCelsius - 22f) / 10f).coerceIn(0.05f, 0.95f) * 0.5f
                } else 0.95f))

                val targetY1 = topMargin + (usableHeight * (if (steps.size > 1 && steps[1].setpointCelsius > 0) {
                    0.80f - ((steps[1].setpointCelsius - 23f) / 10f).coerceIn(0.05f, 0.95f) * 0.5f
                } else 0.62f))

                val targetY2 = topMargin + (usableHeight * (if (steps.size > 2 && steps[2].setpointCelsius > 0) {
                    0.50f - ((steps[2].setpointCelsius - 24f) / 10f).coerceIn(0.05f, 0.95f) * 0.5f
                } else 0.30f))

                val targetY3 = topMargin + (usableHeight * (if (steps.size > 3 && steps[3].setpointCelsius > 0) {
                    0.20f - ((steps[3].setpointCelsius - 25f) / 10f).coerceIn(0.05f, 0.95f) * 0.3f
                } else 0.05f))

                val animY0 by animateFloatAsState(
                    targetValue = targetY0,
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
                    label = "animY0"
                )
                val animY1 by animateFloatAsState(
                    targetValue = targetY1,
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
                    label = "animY1"
                )
                val animY2 by animateFloatAsState(
                    targetValue = targetY2,
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
                    label = "animY2"
                )
                val animY3 by animateFloatAsState(
                    targetValue = targetY3,
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
                    label = "animY3"
                )

                val points = listOf(
                    Offset(x0, animY0),
                    Offset(x1, animY1),
                    Offset(x2, animY2),
                    Offset(x3, animY3)
                )

                val curvePath = remember { Path() }
                val fillPath = remember { Path() }
                val dashEffect = remember { PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f) }
                val fillGradientColors = remember {
                    listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.03f),
                        Color.Transparent
                    )
                }
                val lineGradientColors = remember {
                    listOf(
                        Color.White.copy(alpha = 0.95f),
                        Color.White.copy(alpha = 0.85f),
                        Color.White.copy(alpha = 0.70f)
                    )
                }

                Canvas(modifier = Modifier.fillMaxWidth().height(chartHeight)) {
                    val w = size.width
                    val h = size.height

                    drawLine(
                        color = Color.White.copy(alpha = 0.28f),
                        start = Offset(x0, animY0),
                        end = Offset(x0, h),
                        strokeWidth = 1.2.dp.toPx(),
                        pathEffect = dashEffect
                    )

                    // S-curves between steps represent thermal equilibration lag rather than instant room shifts
                    curvePath.rewind()
                    curvePath.moveTo(0f, animY0)
                    curvePath.lineTo(x0, animY0)

                    val dx01 = x1 - x0
                    curvePath.cubicTo(
                        x0 + dx01 * 0.5f, animY0,
                        x0 + dx01 * 0.5f, animY1,
                        x1, animY1
                    )

                    val dx12 = x2 - x1
                    curvePath.cubicTo(
                        x1 + dx12 * 0.5f, animY1,
                        x1 + dx12 * 0.5f, animY2,
                        x2, animY2
                    )

                    val dx23 = x3 - x2
                    curvePath.cubicTo(
                        x2 + dx23 * 0.5f, animY2,
                        x2 + dx23 * 0.5f, animY3,
                        x3, animY3
                    )
                    curvePath.lineTo(w, animY3)

                    fillPath.rewind()
                    fillPath.addPath(curvePath)
                    fillPath.lineTo(w, h)
                    fillPath.lineTo(0f, h)
                    fillPath.close()

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(colors = fillGradientColors)
                    )

                    drawPath(
                        path = curvePath,
                        brush = Brush.horizontalGradient(colors = lineGradientColors),
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
                    val item = phases.getOrNull(index) ?: return@forEachIndexed
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
                        Crossfade(
                            targetState = item.isFanOnly to item.tempLabel,
                            animationSpec = tween(200),
                            label = "StepLabelCrossfade$index"
                        ) { (isFan, label) ->
                            if (isFan || label == null) {
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
                                    text = label,
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
            }

            Spacer(modifier = Modifier.height(8.dp))

            Crossfade(
                targetState = phases,
                animationSpec = tween(220),
                label = "PhaseRowCrossfade"
            ) { currentPhases ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    currentPhases.forEachIndexed { index, phase ->
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
