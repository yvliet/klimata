package com.example.klimata.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.CaretDown
import com.example.klimata.data.ImpactPeriod
import com.example.klimata.ui.theme.DetailTextPrimary
import com.example.klimata.ui.theme.DetailTextSecondary
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.LocalDiurnalColors

/**
 * Minimalist dark dropdown selector for toggling time-horizon impact views.
 */
@Composable
fun PeriodDropdown(
    selectedPeriod: ImpactPeriod,
    onPeriodSelected: (ImpactPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    val diurnal = LocalDiurnalColors.current
    var expanded by remember { mutableStateOf(false) }
    var anchorBounds by remember { mutableStateOf(Rect.Zero) }

    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = Spring.StiffnessLow
        ),
        label = "chevron_rotation"
    )

    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            anchorBounds = coordinates.boundsInWindow()
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .bouncyClickable(
                    shape = RoundedCornerShape(8.dp),
                    onClick = { expanded = true }
                )
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Text(
                text = selectedPeriod.label,
                style = TextStyle(
                    fontFamily = JakartaFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
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

        KlimataDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            anchorBounds = anchorBounds
        ) {
            ImpactPeriod.entries.forEach { period ->
                val isSelected = period == selectedPeriod
                KlimataDropdownMenuItem(
                    onClick = {
                        onPeriodSelected(period)
                        expanded = false
                    }
                ) {
                    Text(
                        text = period.label,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) diurnal.accentColor else DetailTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
