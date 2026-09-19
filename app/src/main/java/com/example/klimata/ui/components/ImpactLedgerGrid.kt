package com.example.klimata.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klimata.data.ImpactMetric
import com.example.klimata.data.MockData
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.KlimataTheme
import com.example.klimata.ui.theme.LocalDiurnalColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * Dual metric summary cards displaying estimated billing reductions and avoided emissions
 * with circular arc gauges and milestone indicators.
 */
@Composable
fun ImpactLedgerGrid(
    modifier: Modifier = Modifier,
    savings: ImpactMetric = MockData.savingsMetric,
    carbon: ImpactMetric = MockData.carbonMetric,
    onSavingsClick: () -> Unit = {},
    onCarbonClick: () -> Unit = {},
) {
    // 1 Tree absorbs ~22.0 kg CO2 / year baseline (EPA / Forestry standard)
    val carbonValueNumeric = carbon.primaryValue
        .filter { it.isDigit() || it == '.' }
        .toFloatOrNull() ?: 34.2f
    val treeEquivKg = 22.0f
    val currentTreeRemainder = carbonValueNumeric % treeEquivKg
    val nextTreeRemaining = (treeEquivKg - currentTreeRemainder).coerceAtLeast(0.1f)
    val carbonProgress = (currentTreeRemainder / treeEquivKg).coerceIn(0.08f, 1.0f)
    val carbonSubtitle = String.format(java.util.Locale.US, "%.1f kg to next tree", nextTreeRemaining)

    val savingsSubtitle = "Rp 15.5k to goal"
    val savingsProgress = 0.845f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FrostedMetricCard(
            title = savings.title.ifEmpty { "Monthly Savings" },
            primaryValue = savings.primaryValue.ifEmpty { "Rp 84.500" },
            subtitle = savingsSubtitle,
            progressFraction = savingsProgress,
            centerIcon = safeCurrencyDollarIcon(),
            gradientColors = listOf(
                Color(0xFF6EE7B7),
                Color(0xFF34D399),
                Color(0xFF10B981)
            ),
            iconTint = Color(0xFF34D399),
            onClick = onSavingsClick,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )

        FrostedMetricCard(
            title = carbon.title.ifEmpty { "Avoided Carbon" },
            primaryValue = carbon.primaryValue.ifEmpty { "34.2 kg" },
            subtitle = carbonSubtitle,
            progressFraction = carbonProgress,
            centerIcon = safeTreeIcon(),
            gradientColors = listOf(
                Color(0xFF6EE7B7),
                Color(0xFF2DD4BF),
                Color(0xFF0EA5E9)
            ),
            iconTint = Color(0xFF2DD4BF),
            onClick = onCarbonClick,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
    }
}

@Composable
private fun FrostedMetricCard(
    title: String,
    primaryValue: String,
    subtitle: String,
    progressFraction: Float,
    centerIcon: ImageVector,
    gradientColors: List<Color>,
    iconTint: Color,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val diurnal = LocalDiurnalColors.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(diurnal.frostedCardBackground)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.60f)
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = primaryValue,
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        letterSpacing = (-0.4).sp,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.60f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                CircularArcGauge(
                    progress = progressFraction,
                    gradientColors = gradientColors,
                    centerIcon = centerIcon,
                    iconTint = iconTint,
                    modifier = Modifier.size(56.dp)
                )
            }
        }
    }
}

@Composable
private fun CircularArcGauge(
    progress: Float,
    gradientColors: List<Color>,
    centerIcon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        ) {
            val w = size.width
            val h = size.height
            val strokeWidthPx = 4.5.dp.toPx()
            val thumbRadiusPx = 3.2.dp.toPx()
            val cutoutRadiusPx = thumbRadiusPx + 2.dp.toPx()

            val arcSize = Size(w - strokeWidthPx, h - strokeWidthPx)
            val topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f)
            val center = Offset(w / 2f, h / 2f)
            val radius = (minOf(w, h) - strokeWidthPx) / 2f

            val startAngle = 135f
            val totalSweepAngle = 270f
            val clampedProgress = progress.coerceIn(0.04f, 1.0f)
            val activeSweep = totalSweepAngle * clampedProgress

            // Translucent background track
            drawArc(
                color = Color.White.copy(alpha = 0.12f),
                startAngle = startAngle,
                sweepAngle = totalSweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            // Active gradient progress arc
            drawArc(
                brush = Brush.horizontalGradient(
                    colors = gradientColors,
                    startX = 0f,
                    endX = w
                ),
                startAngle = startAngle,
                sweepAngle = activeSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            // Indicator thumb dot coordinates
            val endAngleDeg = startAngle + activeSweep
            val endAngleRad = Math.toRadians(endAngleDeg.toDouble())
            val thumbX = center.x + radius * cos(endAngleRad).toFloat()
            val thumbY = center.y + radius * sin(endAngleRad).toFloat()
            val thumbCenter = Offset(thumbX, thumbY)

            // Transparent halo cutout clearing track and progress bar to reveal card background
            drawCircle(
                color = Color.Transparent,
                radius = cutoutRadiusPx,
                center = thumbCenter,
                blendMode = BlendMode.Clear
            )

            // Solid white thumb dot centered inside cutout
            drawCircle(
                color = Color.White,
                radius = thumbRadiusPx,
                center = thumbCenter
            )
        }

        Icon(
            imageVector = centerIcon,
            contentDescription = null,
            tint = iconTint.copy(alpha = 0.90f),
            modifier = Modifier.size(18.dp)
        )
    }
}

private val FallbackCurrencyDollarIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "CurrencyDollar",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(12f, 2f); lineTo(12f, 22f)
            moveTo(17f, 6f); lineTo(10f, 6f)
            curveTo(8.5f, 6f, 7.5f, 7f, 7.5f, 8.5f)
            curveTo(7.5f, 10f, 8.5f, 11f, 10f, 11f)
            lineTo(14f, 12f)
            curveTo(15.5f, 12f, 16.5f, 13f, 16.5f, 14.5f)
            curveTo(16.5f, 16f, 15.5f, 17f, 14f, 17f)
            lineTo(7f, 17f)
        }
    }.build()
}

private val FallbackTreeIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Tree",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round
        ) {
            // Trunk
            moveTo(12f, 22f); lineTo(12f, 17f)
            // Foliage outline
            moveTo(12f, 2f)
            lineTo(6f, 10f); lineTo(8.5f, 10f)
            lineTo(5f, 17f); lineTo(19f, 17f)
            lineTo(15.5f, 10f); lineTo(18f, 10f)
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
private fun safeCurrencyDollarIcon(): ImageVector {
    if (LocalInspectionMode.current) {
        return FallbackCurrencyDollarIcon
    }
    return getPhosphorLightIcon("CurrencyDollar", FallbackCurrencyDollarIcon)
}

@Composable
private fun safeTreeIcon(): ImageVector {
    if (LocalInspectionMode.current) {
        return FallbackTreeIcon
    }
    return getPhosphorLightIcon("Tree", FallbackTreeIcon)
}

@Preview(showBackground = true, backgroundColor = 0xFF1976D2)
@Composable
private fun ImpactLedgerGridPreview() {
    KlimataTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ImpactLedgerGrid(
                savings = MockData.savingsMetric,
                carbon = MockData.carbonMetric
            )
        }
    }
}
