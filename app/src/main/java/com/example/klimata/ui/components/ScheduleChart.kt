package com.example.klimata.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.CaretRight
import com.adamglin.phosphoricons.light.Fan
import com.adamglin.phosphoricons.light.Leaf
import com.example.klimata.data.MockData
import com.example.klimata.data.ThermalStep
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.KlimataTheme
import com.example.klimata.ui.theme.LocalDiurnalColors
import com.example.klimata.ui.theme.MineralMintActive
import kotlin.math.roundToInt

/**
 * Clean, minimalist home screen visualization of the 1-hour overnight thermal automation progression
 * with horizontal scrolling and single adaptive stepped AC setpoint spline.
 * Grays out smoothly when Eco automation is disabled.
 */
@Composable
fun ScheduleChart(
    modifier: Modifier = Modifier,
    steps: List<ThermalStep> = MockData.thermalSteps,
    isEcoEnabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    val diurnal = LocalDiurnalColors.current
    val count = steps.size.coerceAtLeast(1)
    val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val activeIndex = remember(steps, currentHour) {
        val exactMatch = steps.indexOfFirst {
            it.time.substringBefore(":").toIntOrNull() == currentHour
        }
        if (exactMatch >= 0) exactMatch
        else steps.indexOfFirst { it.isActive }.takeIf { it >= 0 } ?: 0
    }
    val colWidth = 64.dp
    val totalWidth = colWidth * count
    val scrollState = rememberScrollState()
    var showEcoInfoDialog by remember { mutableStateOf(false) }

    if (showEcoInfoDialog) {
        EcoInfoDialog(
            isEcoEnabled = isEcoEnabled,
            onDismissRequest = { showEcoInfoDialog = false }
        )
    }

    val color1 by animateColorAsState(
        targetValue = if (isEcoEnabled) Color(0xFF60A5FA) else Color.White.copy(alpha = 0.22f),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "homeLineC1"
    )
    val color2 by animateColorAsState(
        targetValue = if (isEcoEnabled) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.22f),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "homeLineC2"
    )
    val color3 by animateColorAsState(
        targetValue = if (isEcoEnabled) Color(0xFF34D399) else Color.White.copy(alpha = 0.22f),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "homeLineC3"
    )
    val lineGradientColors = listOf(color1, color2, color3)

    val fillTop by animateColorAsState(
        targetValue = if (isEcoEnabled) Color(0xFF38BDF8).copy(alpha = 0.18f) else Color.White.copy(alpha = 0.02f),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "homeFillTop"
    )
    val fillMid by animateColorAsState(
        targetValue = if (isEcoEnabled) Color(0xFF38BDF8).copy(alpha = 0.04f) else Color.Transparent,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "homeFillMid"
    )
    val fillGradientColors = listOf(fillTop, fillMid, Color.Transparent)

    val activeDashAlpha by animateFloatAsState(
        targetValue = if (isEcoEnabled) 0.35f else 0.12f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "homeActiveDashAlpha"
    )
    val activeDotAlpha by animateFloatAsState(
        targetValue = if (isEcoEnabled) 1f else 0.30f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "homeActiveDotAlpha"
    )
    val tempLabelAlpha by animateFloatAsState(
        targetValue = if (isEcoEnabled) 1f else 0.28f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "homeTempLabelAlpha"
    )
    val phaseLabelAlpha by animateFloatAsState(
        targetValue = if (isEcoEnabled) 0.60f else 0.22f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "homePhaseLabelAlpha"
    )
    val coastPhaseLabelColor by animateColorAsState(
        targetValue = if (isEcoEnabled) Color(0xFF6EE7B7) else Color.White.copy(alpha = 0.22f),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "homeCoastPhaseLabelColor"
    )
    val activeTimeLabelAlpha by animateFloatAsState(
        targetValue = if (isEcoEnabled) 1f else 0.45f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "homeActiveTimeLabelAlpha"
    )
    val timeLabelAlpha by animateFloatAsState(
        targetValue = if (isEcoEnabled) 0.85f else 0.35f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "homeTimeLabelAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .bouncyClickable(shape = RoundedCornerShape(24.dp), onClick = onClick)
            .background(diurnal.frostedCardBackground)
            .padding(vertical = 18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Card Header: Title and Navigation Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "More Details",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.60f)
                        )
                    )
                    Icon(
                        imageVector = PhosphorIcons.Light.CaretRight,
                        contentDescription = "Open schedule details",
                        tint = Color.White.copy(alpha = 0.50f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 18.dp)
            ) {
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

                val leafColor by animateColorAsState(
                    targetValue = if (isEcoEnabled) MineralMintActive else Color.White.copy(alpha = 0.35f),
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
                    label = "homeLeafIconTint"
                )
                val leafBgColor by animateColorAsState(
                    targetValue = if (isEcoEnabled) MineralMintActive.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.08f),
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
                    label = "homeLeafBg"
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(leafBgColor)
                        .clickable { showEcoInfoDialog = true }
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Light.Leaf,
                        contentDescription = "Eco info",
                        tint = leafColor,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontally Scrollable 1-Hour Chart Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                Column(modifier = Modifier.width(totalWidth)) {
                    val chartHeight = 94.dp
                    Box(
                        modifier = Modifier
                            .width(totalWidth)
                            .height(chartHeight)
                    ) {
                        val density = LocalDensity.current
                        val colWidthPx = with(density) { colWidth.toPx() }
                        val widthPx = with(density) { totalWidth.toPx() }
                        val heightPx = with(density) { chartHeight.toPx() }

                        val colCenters = remember(count, colWidthPx) {
                            List(count) { i -> colWidthPx * i + colWidthPx / 2f }
                        }

                        val topMargin = with(density) { 26.dp.toPx() }
                        val bottomMargin = with(density) { 14.dp.toPx() }
                        val usableHeight = heightPx - topMargin - bottomMargin

                        val activeTemps = remember(steps) {
                            steps.filter { it.setpointCelsius > 0 }.map { it.setpointCelsius }
                        }
                        val minTemp = remember(activeTemps) { (activeTemps.minOrNull() ?: 24).toFloat() }
                        val maxTemp = remember(activeTemps) { (activeTemps.maxOrNull() ?: 26).toFloat() }
                        val tempSpan = remember(minTemp, maxTemp) { (maxTemp - minTemp).coerceAtLeast(2f) }

                        val points = remember(steps, colCenters, usableHeight, topMargin, minTemp, tempSpan) {
                            colCenters.mapIndexed { idx, cx ->
                                val step = steps[idx]
                                val norm = if (step.setpointCelsius > 0) {
                                    ((step.setpointCelsius - minTemp) / tempSpan).coerceIn(0f, 1f)
                                } else {
                                    1f
                                }
                                val y = topMargin + usableHeight * (0.82f - norm * 0.55f)
                                Offset(cx, y)
                            }
                        }

                        val curvePath = remember { Path() }
                        val fillPath = remember { Path() }
                        val dashEffect = remember { PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f) }

                        Canvas(
                            modifier = Modifier
                                .width(totalWidth)
                                .height(chartHeight)
                                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                        ) {
                            val w = size.width
                            val h = size.height

                            // Active hour vertical dashed indicator line connecting directly to the dot
                            if (activeIndex in 0 until count) {
                                val activePoint = points[activeIndex]
                                drawLine(
                                    color = Color.White.copy(alpha = activeDashAlpha),
                                    start = Offset(activePoint.x, activePoint.y),
                                    end = Offset(activePoint.x, h),
                                    strokeWidth = 1.2.dp.toPx(),
                                    pathEffect = dashEffect
                                )
                            }

                            // Smooth spline curve across hourly steps
                            if (points.size >= 2) {
                                curvePath.rewind()
                                curvePath.moveTo(0f, points.first().y)
                                curvePath.lineTo(points.first().x, points.first().y)

                                for (i in 0 until points.size - 1) {
                                    val p0 = points[i]
                                    val p1 = points[i + 1]
                                    val dx = p1.x - p0.x
                                    curvePath.cubicTo(
                                        p0.x + dx * 0.5f, p0.y,
                                        p0.x + dx * 0.5f, p1.y,
                                        p1.x, p1.y
                                    )
                                }
                                curvePath.lineTo(w, points.last().y)

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
                            }

                            // Active dot rendered with background clearing so graph lines do not bleed through
                            if (activeIndex in 0 until count) {
                                val activePoint = points[activeIndex]
                                drawCircle(
                                    color = Color.Black,
                                    radius = 3.4.dp.toPx(),
                                    center = activePoint,
                                    blendMode = BlendMode.Clear
                                )
                                drawCircle(
                                    color = Color.White.copy(alpha = activeDotAlpha),
                                    radius = 3.4.dp.toPx(),
                                    center = activePoint
                                )
                            }
                        }

                        // Temperature / Fan labels positioned cleanly above each node
                        points.forEachIndexed { index, point ->
                            val step = steps.getOrNull(index) ?: return@forEachIndexed
                            val isFanOnly = step.setpointCelsius == 0
                            androidx.compose.runtime.key(step.time) {
                                Box(
                                    modifier = Modifier
                                        .width(colWidth)
                                        .layout { measurable, constraints ->
                                            val placeable = measurable.measure(constraints)
                                            layout(placeable.width, placeable.height) {
                                                val posX = (point.x - placeable.width / 2f).roundToInt()
                                                val posY = (point.y - placeable.height - 6.dp.toPx()).roundToInt()
                                                placeable.placeRelative(posX, posY)
                                            }
                                        }
                                ) {
                                    if (isFanOnly) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = PhosphorIcons.Light.Fan,
                                                contentDescription = "Fan Only",
                                                tint = Color.White.copy(alpha = tempLabelAlpha),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "Fan",
                                                style = TextStyle(
                                                    fontFamily = JakartaFamily,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 11.5.sp,
                                                    color = Color.White.copy(alpha = tempLabelAlpha)
                                                )
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "${step.setpointCelsius}°",
                                            style = TextStyle(
                                                fontFamily = JakartaFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = Color.White.copy(alpha = tempLabelAlpha),
                                                textAlign = TextAlign.Center
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bottom Phase Name & Time Row
                    Row(modifier = Modifier.width(totalWidth)) {
                        steps.forEachIndexed { index, step ->
                            val isActive = index == activeIndex
                            androidx.compose.runtime.key(step.time) {
                                Column(
                                    modifier = Modifier.width(colWidth),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = step.time,
                                        style = TextStyle(
                                            fontFamily = JakartaFamily,
                                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = if (isActive) activeTimeLabelAlpha else timeLabelAlpha),
                                            textAlign = TextAlign.Center
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = step.label,
                                        style = TextStyle(
                                            fontFamily = JakartaFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 11.sp,
                                            color = if (step.setpointCelsius == 0) coastPhaseLabelColor else Color.White.copy(alpha = phaseLabelAlpha),
                                            textAlign = TextAlign.Center
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
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
