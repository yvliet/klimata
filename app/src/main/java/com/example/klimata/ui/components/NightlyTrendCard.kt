package com.example.klimata.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klimata.data.TrendChartData
import com.example.klimata.ui.theme.DetailCardSurface
import com.example.klimata.ui.theme.DetailCardSurfaceElevated
import com.example.klimata.ui.theme.DetailTextMuted
import com.example.klimata.ui.theme.DetailTextPrimary
import com.example.klimata.ui.theme.DetailTextSecondary
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.MineralMintActive

/**
 * Minimalist, high-contrast trend bar chart presenting day-by-day, weekly, or monthly
 * activity with slender bars and subtle horizontal dotted grid lines.
 */
@Composable
fun NightlyTrendCard(
    trendData: TrendChartData,
    modifier: Modifier = Modifier,
    accentColor: Color = MineralMintActive,
) {
    val maxVal = remember(trendData) {
        trendData.points.maxOfOrNull { it.value }?.takeIf { it > 0f } ?: 1f
    }
    val isDense = trendData.points.size > 8
    val barWidth = if (isDense) 6.dp else 10.dp
    val labelFontSize = if (isDense) 9.5.sp else 10.5.sp

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(DetailCardSurface)
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column {
                Text(
                    text = trendData.title,
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp,
                        color = DetailTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = trendData.primaryStat,
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp,
                            color = DetailTextSecondary
                        )
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(9.dp)
                            .background(DetailTextMuted.copy(alpha = 0.35f))
                    )
                    Text(
                        text = trendData.subtitle,
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.5.sp,
                            color = DetailTextMuted
                        )
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    trendData.points.forEach { point ->
                        val isHighlighted = point.isCurrent || point.isPeak
                        Text(
                            text = point.displayValue,
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                                fontSize = if (isDense) 8.5.sp else 9.5.sp,
                                color = if (isHighlighted) accentColor else DetailTextMuted
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                        val strokeWidth = 1.dp.toPx()
                        val lineColor = Color.White.copy(alpha = 0.08f)

                        val yLevels = listOf(1.dp.toPx(), size.height * 0.5f, size.height - 1.dp.toPx())
                        yLevels.forEach { y ->
                            drawLine(
                                color = lineColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeWidth,
                                pathEffect = dashEffect
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        trendData.points.forEach { point ->
                            val fraction = (point.value / maxVal).coerceIn(0.08f, 1.0f)
                            val barHeight = (fraction * 58f).dp
                            val isHighlighted = point.isCurrent || point.isPeak

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(barWidth)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(DetailCardSurfaceElevated)
                                )

                                Box(
                                    modifier = Modifier
                                        .height(barHeight)
                                        .width(barWidth)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            if (isHighlighted) accentColor
                                            else accentColor.copy(alpha = 0.35f)
                                        )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    trendData.points.forEach { point ->
                        Text(
                            text = point.label,
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = if (point.isCurrent) FontWeight.Bold else FontWeight.Medium,
                                fontSize = labelFontSize,
                                color = if (point.isCurrent) DetailTextPrimary else DetailTextMuted
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
