package com.example.klimata.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.CaretDown
import com.adamglin.phosphoricons.light.Check
import com.example.klimata.data.ImpactPeriod
import com.example.klimata.ui.theme.DetailCardSurface
import com.example.klimata.ui.theme.DetailCardSurfaceElevated
import com.example.klimata.ui.theme.DetailTextPrimary
import com.example.klimata.ui.theme.DetailTextSecondary
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.MineralMintActive

/**
 * Minimalist dark dropdown selector for toggling time-horizon impact views.
 */
@Composable
fun PeriodDropdown(
    selectedPeriod: ImpactPeriod,
    onPeriodSelected: (ImpactPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "chevron_rotation"
    )

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(DetailCardSurface)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = selectedPeriod.label,
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = DetailTextPrimary
                    )
                )
                Icon(
                    imageVector = PhosphorIcons.Light.CaretDown,
                    contentDescription = "Select time period",
                    tint = DetailTextSecondary,
                    modifier = Modifier
                        .size(12.dp)
                        .graphicsLayer { rotationZ = chevronRotation }
                )
            }
        }

        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(
                surface = DetailCardSurfaceElevated,
                surfaceContainer = DetailCardSurfaceElevated,
                surfaceContainerHigh = DetailCardSurfaceElevated,
                onSurface = DetailTextPrimary
            )
        ) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = RoundedCornerShape(16.dp),
                containerColor = DetailCardSurfaceElevated,
                tonalElevation = 0.dp,
                shadowElevation = 14.dp,
                border = null,
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
                                    color = if (isSelected) MineralMintActive else DetailTextPrimary
                                )
                            )
                        },
                        trailingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = PhosphorIcons.Light.Check,
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
