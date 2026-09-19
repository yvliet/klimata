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
 * Hand-drawn doodle prompt for empty room state.
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onAddRoomClick()
            },
        horizontalAlignment = Alignment.End
    ) {
        // Compact organic sketched doodle arrow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .graphicsLayer { translationY = floatOffset }
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val primaryColor = Color.White.copy(alpha = 0.90f)
                val sketchColor = Color.White.copy(alpha = 0.35f)
                val strokeWidthPx = 2.0.dp.toPx()
                val sketchWidthPx = 1.2.dp.toPx()

                // Plus button center is exactly 84dp from screen right edge
                val endX = size.width - 84.dp.toPx()
                val endY = 2.dp.toPx()

                // Origin right above the prompt text
                val startX = size.width - 76.dp.toPx()
                val startY = size.height - 2.dp.toPx()

                // Control points creating an organic hand-sketched curve
                val ctrl1X = size.width - 68.dp.toPx()
                val ctrl1Y = size.height * 0.65f
                val ctrl2X = size.width - 72.dp.toPx()
                val ctrl2Y = size.height * 0.25f

                // 1. Primary hand-drawn shaft
                val mainPath = Path().apply {
                    moveTo(startX, startY)
                    cubicTo(ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, endX, endY)
                }

                drawPath(
                    path = mainPath,
                    color = primaryColor,
                    style = Stroke(
                        width = strokeWidthPx,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // 2. Wispy secondary sketch stroke for authentic pencil/doodle character
                val sketchPath = Path().apply {
                    moveTo(startX + 1.2f, startY - 0.5f)
                    cubicTo(
                        ctrl1X + 1.5f,
                        ctrl1Y + 1.0f,
                        ctrl2X - 1.0f,
                        ctrl2Y - 1.0f,
                        endX + 0.5f,
                        endY + 1.0f
                    )
                }

                drawPath(
                    path = sketchPath,
                    color = sketchColor,
                    style = Stroke(
                        width = sketchWidthPx,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // 3. Hand-sketched arrowhead
                val arrowLength = 9.dp.toPx()
                val tangentAngle = kotlin.math.atan2(endY - ctrl2Y, endX - ctrl2X)
                val barbSpread = Math.toRadians(30.0)

                val barb1Angle = tangentAngle + Math.PI - barbSpread
                val barb2Angle = tangentAngle + Math.PI + barbSpread

                val barb1X = endX + (arrowLength * cos(barb1Angle)).toFloat()
                val barb1Y = endY + (arrowLength * sin(barb1Angle)).toFloat()

                val barb2X = endX + (arrowLength * 0.85f * cos(barb2Angle)).toFloat()
                val barb2Y = endY + (arrowLength * 0.85f * sin(barb2Angle)).toFloat()

                // Left barb
                drawLine(
                    color = primaryColor,
                    start = Offset(endX, endY),
                    end = Offset(barb1X, barb1Y),
                    strokeWidth = strokeWidthPx,
                    cap = StrokeCap.Round
                )

                // Right barb (slightly asymmetrical like a true hand sketch)
                drawLine(
                    color = primaryColor,
                    start = Offset(endX, endY),
                    end = Offset(barb2X, barb2Y),
                    strokeWidth = strokeWidthPx,
                    cap = StrokeCap.Round
                )

                // Sketched reinforcement on left barb
                drawLine(
                    color = sketchColor,
                    start = Offset(endX + 0.5f, endY + 0.5f),
                    end = Offset(barb1X + 0.8f, barb1Y - 0.4f),
                    strokeWidth = sketchWidthPx,
                    cap = StrokeCap.Round
                )
            }
        }

        // Casual doodle prompt text (no shine stars, compact and clean)
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
                .padding(end = 24.dp, top = 2.dp)
                .graphicsLayer { translationY = floatOffset }
        )
    }
}
