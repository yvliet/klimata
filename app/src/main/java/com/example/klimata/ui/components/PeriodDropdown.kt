package com.example.klimata.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klimata.data.ImpactPeriod
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.MineralMintActive

/**
 * Compact frosted dropdown selector for toggling between time-horizon impact views.
 */
@Composable
fun PeriodDropdown(
    selectedPeriod: ImpactPeriod,
    onPeriodSelected: (ImpactPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.16f))
                .border(1.dp, Color.White.copy(alpha = 0.26f), RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = selectedPeriod.label,
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                )
                Icon(
                    imageVector = safeCaretDownIcon(),
                    contentDescription = "Select time period",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(
                surface = Color(0xFF131D2E),
                onSurface = Color.White
            )
        ) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(Color(0xFF131D2E).copy(alpha = 0.94f))
                    .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
            ) {
                ImpactPeriod.entries.forEach { period ->
                    val isSelected = period == selectedPeriod
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = period.label,
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (isSelected) MineralMintActive else Color.White.copy(alpha = 0.85f)
                                )
                            )
                        },
                        trailingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = safeCheckIcon(),
                                    contentDescription = null,
                                    tint = MineralMintActive,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        } else null,
                        onClick = {
                            onPeriodSelected(period)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

private val FallbackCaretDownIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "CaretDown",
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
            moveTo(6f, 9f)
            lineTo(12f, 15f)
            lineTo(18f, 9f)
        }
    }.build()
}

private val FallbackCheckIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Check",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2.4f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(4f, 12f)
            lineTo(9f, 17f)
            lineTo(20f, 6f)
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
private fun safeCaretDownIcon(): ImageVector {
    if (LocalInspectionMode.current) {
        return FallbackCaretDownIcon
    }
    return getPhosphorLightIcon("CaretDown", FallbackCaretDownIcon)
}

@Composable
private fun safeCheckIcon(): ImageVector {
    if (LocalInspectionMode.current) {
        return FallbackCheckIcon
    }
    return getPhosphorLightIcon("Check", FallbackCheckIcon)
}
