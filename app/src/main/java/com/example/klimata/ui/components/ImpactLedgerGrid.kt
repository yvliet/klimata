package com.example.klimata.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klimata.data.ImpactMetric
import com.example.klimata.data.MockData
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.KlimataTheme
import com.example.klimata.ui.theme.LocalDiurnalColors

/**
 * Dual metric summary cards displaying estimated billing reductions and avoided emissions.
 */
@Composable
fun ImpactLedgerGrid(
    modifier: Modifier = Modifier,
    savings: ImpactMetric = MockData.savingsMetric,
    carbon: ImpactMetric = MockData.carbonMetric,
    onSavingsClick: () -> Unit = {},
    onCarbonClick: () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FrostedMetricCard(
            title = savings.title.ifEmpty { "Monthly Savings" },
            primaryValue = savings.primaryValue.ifEmpty { "Rp 84.500" },
            badgeText = "-38% kWh",
            icon = safeCurrencyDollarIcon(),
            accentColor = com.example.klimata.ui.theme.MineralMintActive,
            onClick = onSavingsClick,
            modifier = Modifier.weight(1f)
        )

        FrostedMetricCard(
            title = carbon.title.ifEmpty { "Avoided Carbon" },
            primaryValue = carbon.primaryValue.ifEmpty { "34.2 kg" },
            badgeText = "1.4 Trees eq.",
            icon = safeLeafIcon(),
            accentColor = Color.White,
            onClick = onCarbonClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FrostedMetricCard(
    title: String,
    primaryValue: String,
    badgeText: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val diurnal = LocalDiurnalColors.current
    Box(
        modifier = modifier
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.85f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .clip(RoundedCornerShape(24.dp))
            .background(diurnal.frostedCardBackground)
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Crossfade(
            targetState = Triple(title, primaryValue, badgeText),
            animationSpec = tween(220),
            label = "FrostedMetricCardCrossfade"
        ) { (curTitle, curVal, curBadge) ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.18f))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = curBadge,
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                color = accentColor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = curVal,
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        letterSpacing = (-0.3).sp,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = curTitle,
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.70f)
                    )
                )
            }
        }
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
            strokeLineWidth = 2f,
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
private fun safeCurrencyDollarIcon(): ImageVector {
    if (LocalInspectionMode.current) {
        return FallbackCurrencyDollarIcon
    }
    return getPhosphorLightIcon("CurrencyDollar", FallbackCurrencyDollarIcon)
}

@Composable
private fun safeLeafIcon(): ImageVector {
    if (LocalInspectionMode.current) {
        return FallbackLeafIcon
    }
    return getPhosphorLightIcon("Leaf", FallbackLeafIcon)
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
