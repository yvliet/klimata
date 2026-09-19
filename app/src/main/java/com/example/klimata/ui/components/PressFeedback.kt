package com.example.klimata.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * Modifier applying a tactile bounce scale-down (to 0.97f) with responsive spring physics,
 * strict corner shape clipping, and a smooth luminous frosted white sheen overlay when pressed.
 */
@Composable
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    pressedScale: Float = 0.97f,
    sheenAlpha: Float = 0.12f,
    shape: Shape = RoundedCornerShape(24.dp),
    onClick: () -> Unit,
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bouncyCardScale"
    )

    val animatedSheenAlpha by animateFloatAsState(
        targetValue = if (isPressed && enabled) sheenAlpha else 0.0f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bouncyCardSheen"
    )

    return this
        .graphicsLayer {
            scaleX = animatedScale
            scaleY = animatedScale
            this.shape = shape
            clip = true
        }
        .clip(shape)
        .drawWithContent {
            drawContent()
            if (animatedSheenAlpha > 0.001f) {
                drawRect(color = Color.White.copy(alpha = animatedSheenAlpha))
            }
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}
