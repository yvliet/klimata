package com.example.klimata.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.CaretDown
import com.adamglin.phosphoricons.light.CaretUp
import com.adamglin.phosphoricons.light.Fan
import com.adamglin.phosphoricons.light.Leaf
import com.adamglin.phosphoricons.light.Moon
import com.adamglin.phosphoricons.light.Sun
import com.adamglin.phosphoricons.light.Thermometer
import com.adamglin.phosphoricons.light.Wind
import com.example.klimata.data.RoomState
import com.example.klimata.data.ThermalStep
import com.example.klimata.ui.components.DetailPageScaffold
import com.example.klimata.ui.components.EcoInfoDialog
import com.example.klimata.ui.components.bouncyClickable
import com.example.klimata.ui.theme.DetailActiveColumnHighlight
import com.example.klimata.ui.theme.DetailCardSurface
import com.example.klimata.ui.theme.DetailCardSurfaceElevated
import com.example.klimata.ui.theme.DetailCurveAmbient
import com.example.klimata.ui.theme.DetailCurveSetpoint
import com.example.klimata.ui.theme.DetailTextMuted
import com.example.klimata.ui.theme.DetailTextPrimary
import com.example.klimata.ui.theme.DetailTextSecondary
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.LocalMinuteOfDay
import com.example.klimata.ui.theme.MineralMintActive
import com.example.klimata.ui.theme.currentMinuteOfDay
import kotlin.math.roundToInt

@Composable
fun ScheduleDetailScreen(
    room: RoomState,
    onBackClick: () -> Unit = {},
) {
    var showEcoInfoDialog by remember { mutableStateOf(false) }

    if (showEcoInfoDialog) {
        EcoInfoDialog(
            isEcoEnabled = room.isEcoEnabled,
            onDismissRequest = { showEcoInfoDialog = false }
        )
    }

    val leafColor by animateColorAsState(
        targetValue = if (room.isEcoEnabled) MineralMintActive else DetailTextSecondary.copy(alpha = 0.40f),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "detailLeafIconTint"
    )

    val minuteOfDay = LocalMinuteOfDay.current ?: currentMinuteOfDay()
    val currentHour = (minuteOfDay / 60) % 24
    val title = if (currentHour in 21..23 || currentHour in 0..5) "Tonight's Schedule" else "24-Hour Schedule"

    DetailPageScaffold(
        title = title,
        subtitle = room.name,
        onBackClick = onBackClick,
        titleTrailingContent = {
            Icon(
                imageVector = PhosphorIcons.Light.Leaf,
                contentDescription = "Eco info",
                tint = leafColor,
                modifier = Modifier
                    .size(20.dp)
                    .bouncyClickable(
                        shape = CircleShape,
                        onClick = { showEcoInfoDialog = true }
                    )
            )
        }
    ) {
        MinimalistThermalForecastCard(
            steps = room.thermalSteps,
            isEcoEnabled = room.isEcoEnabled,
            modifier = Modifier.fillMaxWidth()
        )

        OvernightScheduleInfoCard(
            isEcoEnabled = room.isEcoEnabled,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun OvernightScheduleInfoCard(
    isEcoEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(DetailCardSurface)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = PhosphorIcons.Light.Moon,
                contentDescription = null,
                tint = if (isEcoEnabled) MineralMintActive else DetailTextSecondary,
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = "Diurnal Schedule",
                style = TextStyle(
                    fontFamily = JakartaFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = DetailTextPrimary
                )
            )
        }

        Text(
            text = if (isEcoEnabled) {
                "Klimata continuously balances room cooling and thermal inertia against outdoor ambient conditions across the full 24-hour cycle. Keep your phone facing the AC unit so adjustments reach the sensor."
            } else {
                "Automated schedule transitions are paused because Eco mode is off. Turn Eco on to resume automatic diurnal adjustments."
            },
            style = TextStyle(
                fontFamily = JakartaFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.5.sp,
                color = DetailTextSecondary,
                lineHeight = 17.sp
            )
        )
    }
}

/**
 * High-contrast minimalist thermal forecast card clearly separating top outdoor ambient weather
 * (Sky Blue curve, night weather icons, outdoor temperatures) from bottom indoor AC automation
 * (Mineral Mint curve, target setpoints, AC phase labels, and airflow modes).
 * Supports 1-hour resolution with internal horizontal scrolling and smooth grayed-out transitions when Eco is disabled.
 */
@Composable
private fun MinimalistThermalForecastCard(
    steps: List<ThermalStep>,
    isEcoEnabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val count = steps.size.coerceAtLeast(1)
    val minuteOfDay = LocalMinuteOfDay.current ?: currentMinuteOfDay()
    val currentHour = (minuteOfDay / 60) % 24
    val activeIndex = remember(steps, currentHour) {
        val exactMatch = steps.indexOfFirst {
            it.time.substringBefore(":").toIntOrNull() == currentHour
        }
        if (exactMatch >= 0) exactMatch
        else steps.indexOfFirst { it.isActive }.takeIf { it >= 0 } ?: 0
    }
    val colWidth = 74.dp
    val totalWidth = colWidth * count

    val setpointCurveColor by animateColorAsState(
        targetValue = if (isEcoEnabled) DetailCurveSetpoint else Color.White.copy(alpha = 0.20f),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "detailSetpointCurveColor"
    )
    val setpointDotColor by animateColorAsState(
        targetValue = if (isEcoEnabled) DetailCurveSetpoint else Color.White.copy(alpha = 0.25f),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "detailSetpointDotColor"
    )
    val dashLineAlpha by animateFloatAsState(
        targetValue = if (isEcoEnabled) 0.16f else 0.06f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "detailDashAlpha"
    )
    val activeDashLineAlpha by animateFloatAsState(
        targetValue = if (isEcoEnabled) 0.45f else 0.16f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "detailActiveDashAlpha"
    )
    val setpointTextAlpha by animateFloatAsState(
        targetValue = if (isEcoEnabled) 1f else 0.28f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "detailSetpointTextAlpha"
    )
    val phaseTagAlpha by animateFloatAsState(
        targetValue = if (isEcoEnabled) 1f else 0.22f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "detailPhaseTagAlpha"
    )
    val fanTagColor by animateColorAsState(
        targetValue = if (isEcoEnabled) MineralMintActive else Color.White.copy(alpha = 0.28f),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "detailFanTagColor"
    )
    val activePhaseTagColor by animateColorAsState(
        targetValue = if (isEcoEnabled) MineralMintActive else DetailTextSecondary.copy(alpha = 0.22f),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "detailActivePhaseTagColor"
    )
    val activeAirflowIconTint by animateColorAsState(
        targetValue = if (isEcoEnabled) MineralMintActive else DetailTextMuted.copy(alpha = 0.22f),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "detailActiveAirflowIconTint"
    )
    val activeAirflowTextTint by animateColorAsState(
        targetValue = if (isEcoEnabled) DetailTextPrimary else DetailTextMuted.copy(alpha = 0.22f),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "detailActiveAirflowTextTint"
    )
    val activeColumnBgColor by animateColorAsState(
        targetValue = if (isEcoEnabled) DetailActiveColumnHighlight else DetailCardSurface,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "detailActiveColBg"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(DetailCardSurface)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val density = LocalDensity.current
            val colWidthPx = with(density) { colWidth.toPx() }
            val totalWidthPx = with(density) { totalWidth.toPx() }
            val containerWidthPx = constraints.maxWidth.toFloat()
            val activeCenterX = (activeIndex + 0.5f) * colWidthPx
            val targetScrollPx = (activeCenterX - containerWidthPx * 0.25f).roundToInt()
            val maxScrollPx = (totalWidthPx - containerWidthPx).coerceAtLeast(0f).roundToInt()
            val clampedScroll = targetScrollPx.coerceIn(0, maxScrollPx)

            val scrollState = rememberScrollState(initial = clampedScroll)

            LaunchedEffect(activeIndex) {
                scrollState.animateScrollTo(
                    value = clampedScroll,
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                Box(modifier = Modifier.width(totalWidth)) {
                if (activeIndex in 0 until count) {
                    Row(modifier = Modifier.matchParentSize()) {
                        steps.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .width(colWidth)
                                    .fillMaxHeight()
                                    .then(
                                        if (index == activeIndex) {
                                            Modifier.background(activeColumnBgColor)
                                        } else {
                                            Modifier
                                        }
                                    )
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .width(totalWidth)
                        .padding(vertical = 20.dp)
                ) {
                    Row(modifier = Modifier.width(totalWidth)) {
                        steps.forEachIndexed { index, step ->
                            val isActive = index == activeIndex
                            Column(
                                modifier = Modifier.width(colWidth),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = step.time,
                                    style = TextStyle(
                                        fontFamily = JakartaFamily,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.5.sp,
                                        color = if (isActive) DetailTextPrimary else DetailTextSecondary,
                                        textAlign = TextAlign.Center
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                val hour = step.time.substringBefore(":").toIntOrNull() ?: 0
                                val weatherIcon: ImageVector = when {
                                    hour in 6..17 -> if (step.outdoorTemp >= 31) PhosphorIcons.Light.Sun else PhosphorIcons.Light.Thermometer
                                    hour in 18..20 -> PhosphorIcons.Light.Wind
                                    else -> PhosphorIcons.Light.Moon
                                }

                                Icon(
                                    imageVector = weatherIcon,
                                    contentDescription = "Outdoor weather",
                                    tint = if (isActive) DetailCurveAmbient else DetailCurveAmbient.copy(alpha = 0.60f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val chartHeight = 172.dp
                    Box(
                        modifier = Modifier
                            .width(totalWidth)
                            .height(chartHeight)
                    ) {
                        val density = LocalDensity.current
                        val colWidthPx = with(density) { colWidth.toPx() }
                        val widthPx = with(density) { totalWidth.toPx() }

                        val colCenters = remember(count, colWidthPx) {
                            List(count) { i -> colWidthPx * i + colWidthPx / 2f }
                        }

                        val ambientMin = (steps.minOfOrNull { it.outdoorTemp } ?: 24).toFloat()
                        val ambientMax = (steps.maxOfOrNull { it.outdoorTemp } ?: 30).toFloat()
                        val ambientSpan = (ambientMax - ambientMin).coerceAtLeast(1f)

                        val upperPoints = remember(steps, colCenters, ambientMin, ambientSpan, density) {
                            colCenters.mapIndexed { idx, cx ->
                                val step = steps[idx]
                                val norm = 1f - ((step.outdoorTemp - ambientMin) / ambientSpan).coerceIn(0f, 1f)
                                val y = with(density) { 28.dp.toPx() } + norm * with(density) { 38.dp.toPx() }
                                Offset(cx, y)
                            }
                        }

                        val activeTemps = remember(steps) {
                            steps.filter { it.setpointCelsius > 0 }.map { it.setpointCelsius }
                        }
                        val setpointMin = remember(activeTemps) { (activeTemps.minOrNull() ?: 24).toFloat() }
                        val setpointMax = remember(activeTemps) { (activeTemps.maxOrNull() ?: 26).toFloat() }
                        val setpointSpan = remember(setpointMin, setpointMax) { (setpointMax - setpointMin).coerceAtLeast(2f) }

                        val lowerPoints = remember(steps, colCenters, setpointMin, setpointSpan, density) {
                            colCenters.mapIndexed { idx, cx ->
                                val step = steps[idx]
                                val setpoint = if (step.setpointCelsius > 0) step.setpointCelsius.toFloat() else setpointMax
                                val norm = ((setpoint - setpointMin) / setpointSpan).coerceIn(0f, 1f)
                                val y = with(density) { 136.dp.toPx() } - norm * with(density) { 26.dp.toPx() }
                                Offset(cx, y)
                            }
                        }

                        val ambientCurvePath = remember { Path() }
                        val setpointCurvePath = remember { Path() }
                        val dashEffect = remember { PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f) }

                        Canvas(
                            modifier = Modifier
                                .width(totalWidth)
                                .height(chartHeight)
                        ) {
                            for (i in 0 until count) {
                                val pUpper = upperPoints.getOrNull(i) ?: continue
                                val pLower = lowerPoints.getOrNull(i) ?: continue
                                val isActive = i == activeIndex
                                drawLine(
                                    color = Color.White.copy(alpha = if (isActive) activeDashLineAlpha else dashLineAlpha),
                                    start = Offset(pUpper.x, pUpper.y + 4.dp.toPx()),
                                    end = Offset(pLower.x, pLower.y - 4.dp.toPx()),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = dashEffect
                                )
                            }

                            if (upperPoints.size >= 2) {
                                ambientCurvePath.rewind()
                                ambientCurvePath.moveTo(0f, upperPoints.first().y)
                                ambientCurvePath.lineTo(upperPoints.first().x, upperPoints.first().y)

                                for (i in 0 until upperPoints.size - 1) {
                                    val p0 = upperPoints[i]
                                    val p1 = upperPoints[i + 1]
                                    val dx = p1.x - p0.x
                                    ambientCurvePath.cubicTo(
                                        p0.x + dx * 0.5f, p0.y,
                                        p0.x + dx * 0.5f, p1.y,
                                        p1.x, p1.y
                                    )
                                }
                                ambientCurvePath.lineTo(widthPx, upperPoints.last().y)

                                drawPath(
                                    path = ambientCurvePath,
                                    color = DetailCurveAmbient,
                                    style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }

                            upperPoints.forEach { pt ->
                                drawCircle(
                                    color = DetailCardSurface,
                                    radius = 4.2.dp.toPx(),
                                    center = pt
                                )
                                drawCircle(
                                    color = DetailCurveAmbient,
                                    radius = 3.0.dp.toPx(),
                                    center = pt
                                )
                            }

                            if (lowerPoints.size >= 2) {
                                setpointCurvePath.rewind()
                                setpointCurvePath.moveTo(0f, lowerPoints.first().y)
                                setpointCurvePath.lineTo(lowerPoints.first().x, lowerPoints.first().y)

                                for (i in 0 until lowerPoints.size - 1) {
                                    val p0 = lowerPoints[i]
                                    val p1 = lowerPoints[i + 1]
                                    val dx = p1.x - p0.x
                                    setpointCurvePath.cubicTo(
                                        p0.x + dx * 0.5f, p0.y,
                                        p0.x + dx * 0.5f, p1.y,
                                        p1.x, p1.y
                                    )
                                }
                                setpointCurvePath.lineTo(widthPx, lowerPoints.last().y)

                                drawPath(
                                    path = setpointCurvePath,
                                    color = setpointCurveColor,
                                    style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }

                            lowerPoints.forEach { pt ->
                                drawCircle(
                                    color = DetailCardSurface,
                                    radius = 4.2.dp.toPx(),
                                    center = pt
                                )
                                drawCircle(
                                    color = setpointDotColor,
                                    radius = 3.0.dp.toPx(),
                                    center = pt
                                )
                            }
                        }

                        upperPoints.forEachIndexed { index, pt ->
                            val step = steps.getOrNull(index) ?: return@forEachIndexed
                            androidx.compose.runtime.key("upper_${step.time}") {
                                Box(
                                    modifier = Modifier
                                        .width(colWidth)
                                        .layout { measurable, constraints ->
                                            val placeable = measurable.measure(constraints)
                                            layout(placeable.width, placeable.height) {
                                                val posX = (pt.x - placeable.width / 2f).roundToInt()
                                                val posY = (pt.y - placeable.height - 5.dp.toPx()).roundToInt()
                                                placeable.placeRelative(posX, posY)
                                            }
                                        }
                                ) {
                                    Text(
                                        text = "${step.outdoorTemp}°",
                                        style = TextStyle(
                                            fontFamily = JakartaFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = DetailTextPrimary,
                                            textAlign = TextAlign.Center
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        lowerPoints.forEachIndexed { index, pt ->
                            val step = steps.getOrNull(index) ?: return@forEachIndexed
                            val isFanOnly = step.setpointCelsius == 0
                            androidx.compose.runtime.key("lower_${step.time}") {
                                Box(
                                    modifier = Modifier
                                        .width(colWidth)
                                        .layout { measurable, constraints ->
                                            val placeable = measurable.measure(constraints)
                                            layout(placeable.width, placeable.height) {
                                                val posX = (pt.x - placeable.width / 2f).roundToInt()
                                                val posY = (pt.y + 6.dp.toPx()).roundToInt()
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
                                                contentDescription = null,
                                                tint = fanTagColor,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "Fan",
                                                style = TextStyle(
                                                    fontFamily = JakartaFamily,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 11.5.sp,
                                                    color = fanTagColor
                                                )
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "${step.setpointCelsius}°",
                                            style = TextStyle(
                                                fontFamily = JakartaFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = DetailTextPrimary.copy(alpha = setpointTextAlpha),
                                                textAlign = TextAlign.Center
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.width(totalWidth)) {
                        steps.forEachIndexed { index, step ->
                            val isActive = index == activeIndex
                            val isFanOnly = step.setpointCelsius == 0
                            androidx.compose.runtime.key("bottom_${step.time}") {
                                Column(
                                    modifier = Modifier.width(colWidth),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = step.label,
                                        style = TextStyle(
                                            fontFamily = JakartaFamily,
                                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = if (isActive) activePhaseTagColor else DetailTextSecondary.copy(alpha = phaseTagAlpha),
                                            textAlign = TextAlign.Center
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 1
                                    )

                                    Spacer(modifier = Modifier.height(3.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = when {
                                                isFanOnly -> PhosphorIcons.Light.Wind
                                                step.fanMode.contains("High", ignoreCase = true) -> PhosphorIcons.Light.CaretUp
                                                step.fanMode.contains("Quiet", ignoreCase = true) -> PhosphorIcons.Light.CaretDown
                                                else -> PhosphorIcons.Light.CaretUp
                                            },
                                            contentDescription = null,
                                            tint = if (isActive) activeAirflowIconTint else DetailTextMuted.copy(alpha = phaseTagAlpha),
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = when {
                                                isFanOnly -> "Coast"
                                                step.fanMode.contains("High", ignoreCase = true) -> "High"
                                                step.fanMode.contains("Auto", ignoreCase = true) -> "Auto"
                                                step.fanMode.contains("Quiet", ignoreCase = true) -> "Quiet"
                                                else -> step.fanMode
                                            },
                                            style = TextStyle(
                                                fontFamily = JakartaFamily,
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 11.sp,
                                                color = if (isActive) activeAirflowTextTint else DetailTextMuted.copy(alpha = phaseTagAlpha),
                                                textAlign = TextAlign.Center
                                            ),
                                            maxLines = 1
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
    }
}

