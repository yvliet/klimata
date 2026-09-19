package com.example.klimata.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * GPU-accelerated press interaction applying a tactile 0.97f scale-down and soft luminous
 * frosted sheen on touch, followed by a gentle, non-harsh spring release without triggering
 * Compose layout or recomposition passes.
 */
@Composable
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    pressedScale: Float = 0.97f,
    sheenAlpha: Float = 0.10f,
    shape: Shape = RoundedCornerShape(24.dp),
    onClick: () -> Unit,
): Modifier {
    if (!enabled) return this

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
        .clip(shape)
        .drawWithContent {
            drawContent()
            val currentSheen = sheenAnim.value
            if (currentSheen > 0.001f) {
                drawRect(color = Color.White.copy(alpha = currentSheen))
            }
        }
        .pointerInput(enabled, onClick) {
            detectTapGestures(
                onPress = {
                    val pressJob = coroutineScope.launch {
                        scaleAnim.animateTo(pressedScale, pressSpring)
                    }
                    val sheenJob = coroutineScope.launch {
                        sheenAnim.animateTo(sheenAlpha, pressSpring)
                    }
                    val released = try {
                        tryAwaitRelease()
                        true
                    } catch (e: Exception) {
                        false
                    }
                    pressJob.cancel()
                    sheenJob.cancel()
                    coroutineScope.launch {
                        scaleAnim.animateTo(1.0f, releaseSpring)
                    }
                    coroutineScope.launch {
                        sheenAnim.animateTo(0.0f, releaseSpring)
                    }
                    if (released) {
                        onClick()
                    }
                }
            )
        }
}
