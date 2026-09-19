package com.example.klimata.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * GPU-accelerated press interaction applying a tactile 0.96f scale-down and soft luminous
 * frosted sheen on touch, followed by a gentle, non-harsh spring release without triggering
 * Compose layout or recomposition passes.
 */
@Composable
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    pressedScale: Float = 0.96f,
    sheenAlpha: Float = 0.08f,
    shape: Shape = RoundedCornerShape(16.dp),
    onClick: () -> Unit,
): Modifier {
    if (!enabled) return this

    val currentOnClick by rememberUpdatedState(onClick)
    val coroutineScope = rememberCoroutineScope()
    val scaleAnim = remember { Animatable(1.0f) }
    val sheenAnim = remember { Animatable(0.0f) }

    val pressSpring = remember {
        spring<Float>(
            dampingRatio = 0.80f,
            stiffness = Spring.StiffnessMedium
        )
    }

    val releaseSpring = remember {
        spring<Float>(
            dampingRatio = 0.90f,
            stiffness = Spring.StiffnessMediumLow
        )
    }

    return this
        .graphicsLayer {
            scaleX = scaleAnim.value
            scaleY = scaleAnim.value
            this.shape = shape
            clip = true
        }
        .drawWithContent {
            drawContent()
            val currentSheen = sheenAnim.value
            if (currentSheen > 0.001f) {
                drawRect(color = Color.White.copy(alpha = currentSheen))
            }
        }
        .pointerInput(enabled) {
            detectTapGestures(
                onPress = {
                    val pressJob = coroutineScope.launch {
                        scaleAnim.animateTo(pressedScale, pressSpring)
                    }
                    val sheenJob = coroutineScope.launch {
                        sheenAnim.animateTo(sheenAlpha, pressSpring)
                    }
                    try {
                        tryAwaitRelease()
                    } finally {
                        coroutineScope.launch {
                            pressJob.join()
                            scaleAnim.animateTo(1.0f, releaseSpring)
                        }
                        coroutineScope.launch {
                            sheenJob.join()
                            sheenAnim.animateTo(0.0f, releaseSpring)
                        }
                    }
                },
                onTap = {
                    currentOnClick()
                }
            )
        }
}
