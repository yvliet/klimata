package com.example.klimata.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klimata.ui.theme.JakartaFamily
import kotlin.math.cos
import kotlin.math.sin

/**
 * Hand-drawn empty state illustration for room provisioning.
 * Compact organic sketched arrow curving up to the top-right (+) button
 * with casual invitation text, neatly tucked to prevent overlapping temperature readouts.
 */
@Composable
fun EmptyRoomsDoodlePrompt(
    onAddRoomClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "DoodleFloatTransition")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "DoodleFloat"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onAddRoomClick()
            }
            .graphicsLayer { translationY = floatOffset }
    ) {
        // Doodle text positioned inline directly to the left of the arrow tail
        Text(
            text = "add your room here!",
            style = TextStyle(
                fontFamily = JakartaFamily,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                letterSpacing = 0.2.sp,
                color = Color.White.copy(alpha = 0.90f)
            ),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 150.dp, top = 25.dp)
        )

        // Hand-drawn doodle arrow swooping from next to the text up to the (+) button
        Canvas(modifier = Modifier.matchParentSize()) {
            val arrowColor = Color.White.copy(alpha = 0.95f)
            val strokeWidthPx = 2.4.dp.toPx()

            // Tip targets directly at the bottom-left of the (+) button
            val tipX = size.width - 90.dp.toPx()
            val tipY = 8.dp.toPx()

            // Origin starts right after the exclamation mark of "add your room here!"
            val startX = size.width - 142.dp.toPx()
            val startY = 34.dp.toPx()

            // Natural organic curve swooping up and right
            val ctrlX = size.width - 114.dp.toPx()
            val ctrlY = 32.dp.toPx()

            // 1. Curved shaft
            val shaftPath = Path().apply {
                moveTo(startX, startY)
                quadraticTo(ctrlX, ctrlY, tipX, tipY)
            }

            drawPath(
                path = shaftPath,
                color = arrowColor,
                style = Stroke(
                    width = strokeWidthPx,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 2. Unmistakable hand-drawn chevron arrowhead (>) pointing directly at the (+) button
            val dx = tipX - ctrlX
            val dy = tipY - ctrlY
            val tangentAngle = kotlin.math.atan2(dy, dx)
            val barbLength = 11.dp.toPx()
            val barbSpread = Math.toRadians(32.0)

            // Both wings branch back symmetrically on opposite sides of the incoming shaft
            val barb1Angle = tangentAngle + Math.PI - barbSpread
            val barb2Angle = tangentAngle + Math.PI + barbSpread

            val barb1X = tipX + (barbLength * cos(barb1Angle)).toFloat()
            val barb1Y = tipY + (barbLength * sin(barb1Angle)).toFloat()

            val barb2X = tipX + (barbLength * cos(barb2Angle)).toFloat()
            val barb2Y = tipY + (barbLength * sin(barb2Angle)).toFloat()

            val arrowHeadPath = Path().apply {
                moveTo(barb1X, barb1Y)
                lineTo(tipX, tipY)
                lineTo(barb2X, barb2Y)
            }

            drawPath(
                path = arrowHeadPath,
                color = arrowColor,
                style = Stroke(
                    width = strokeWidthPx,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}
