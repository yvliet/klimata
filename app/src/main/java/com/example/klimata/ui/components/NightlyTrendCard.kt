package com.example.klimata.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
 * savings and carbon reductions with slender bars and peak highlight indicators.
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
            // Header Row: Title & Subtitle + Primary Stat Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
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

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = trendData.primaryStat,
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = accentColor
                        )
                    )
                }
            }

            // Minimalist Bar Visualizer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                trendData.points.forEach { point ->
                    val fraction = (point.value / maxVal).coerceIn(0.08f, 1.0f)
                    val barHeight = (fraction * 58f).dp
                    val isHighlighted = point.isCurrent || point.isPeak

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Value label above bar
                        Text(
                            text = point.displayValue,
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                                fontSize = if (isDense) 8.5.sp else 9.5.sp,
                                color = if (isHighlighted) accentColor else DetailTextMuted
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Vertical bar track & filled bar
                        Box(
                            modifier = Modifier
                                .height(58.dp)
                                .width(barWidth),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            // Subtle background track
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(barWidth)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(DetailCardSurfaceElevated)
                            )

                            // Active progress fill
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

                        Spacer(modifier = Modifier.height(6.dp))

                        // Date / Period label
                        Text(
                            text = point.label,
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = if (point.isCurrent) FontWeight.Bold else FontWeight.Medium,
                                fontSize = labelFontSize,
                                color = if (point.isCurrent) DetailTextPrimary else DetailTextMuted
                            )
                        )
                    }
                }
            }
        }
    }
}
