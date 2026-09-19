package com.example.klimata.ui.screens.provisioning

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klimata.sensor.WalkedCorner
import com.example.klimata.ui.theme.DetailCardBorder
import com.example.klimata.ui.theme.DetailCardSurface
import com.example.klimata.ui.theme.MineralMint
import com.example.klimata.ui.theme.MineralMintActive
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

@Composable
fun WalkPerimeterCanvas(
    corners: List<Offset>,
    walkedCorners: List<WalkedCorner>,
    currentWalkerPos: Offset,
    currentHeadingDeg: Float,
    isClosed: Boolean = false,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val dashEffect = remember { PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f) }
    val polyPath = remember { Path() }

    val labelPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = with(density) { 11.sp.toPx() }
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(DetailCardSurface),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension / 2f * 0.86f

            // Concentric precision range rings (clean static architectural grid)
            val ringCount = 3
            for (i in 1..ringCount) {
                val r = maxRadius * (i / ringCount.toFloat())
                drawCircle(
                    color = DetailCardBorder.copy(alpha = 0.50f),
                    radius = r,
                    center = center,
                    style = Stroke(width = 1.dp.toPx(), pathEffect = dashEffect)
                )
            }

            // Crosshair guide axes
            drawLine(
                color = DetailCardBorder.copy(alpha = 0.40f),
                start = Offset(center.x, center.y - maxRadius),
                end = Offset(center.x, center.y + maxRadius),
                strokeWidth = 1f
            )
            drawLine(
                color = DetailCardBorder.copy(alpha = 0.40f),
                start = Offset(center.x - maxRadius, center.y),
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 1f
            )

            // Normalize corners relative to canvas center
            val allPoints = (corners + currentWalkerPos)
            val minX = allPoints.minOfOrNull { it.x } ?: 0f
            val maxX = allPoints.maxOfOrNull { it.x } ?: 0f
            val minY = allPoints.minOfOrNull { it.y } ?: 0f
            val maxY = allPoints.maxOfOrNull { it.y } ?: 0f

            val spanX = max(maxX - minX, 3.5f)
            val spanY = max(maxY - minY, 3.5f)
            val maxSpan = max(spanX, spanY)

            val pxPerMeter = (maxRadius * 1.4f) / maxSpan
            val midX = (minX + maxX) / 2f
            val midY = (minY + maxY) / 2f

            fun mapToCanvas(pt: Offset): Offset {
                return Offset(
                    x = center.x + (pt.x - midX) * pxPerMeter,
                    y = center.y + (pt.y - midY) * pxPerMeter
                )
            }

            // Draw walked polygon / edges
            if (corners.isNotEmpty()) {
                val canvasPoints = corners.map { mapToCanvas(it) }

                polyPath.rewind()
                polyPath.moveTo(canvasPoints.first().x, canvasPoints.first().y)
                for (i in 1 until canvasPoints.size) {
                    polyPath.lineTo(canvasPoints[i].x, canvasPoints[i].y)
                }

                if (isClosed && canvasPoints.size >= 3) {
                    polyPath.close()
                    drawPath(
                        path = polyPath,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MineralMintActive.copy(alpha = 0.16f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = maxRadius
                        ),
                        style = Fill
                    )
                }

                // Path stroke
                drawPath(
                    path = polyPath,
                    color = MineralMintActive,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw dashed active leg to current walker position
                if (!isClosed) {
                    val lastCornerCanvas = canvasPoints.last()
                    val walkerCanvas = mapToCanvas(currentWalkerPos)
                    drawLine(
                        color = MineralMint.copy(alpha = 0.80f),
                        start = lastCornerCanvas,
                        end = walkerCanvas,
                        strokeWidth = 1.8.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }

                // Draw corner nodes & badges
                canvasPoints.forEachIndexed { index, nodePos ->
                    drawCircle(
                        color = DetailCardSurface,
                        radius = 8.dp.toPx(),
                        center = nodePos
                    )
                    drawCircle(
                        color = MineralMintActive,
                        radius = 4.5.dp.toPx(),
                        center = nodePos
                    )

                    // Draw corner number index
                    drawContext.canvas.nativeCanvas.drawText(
                        "${index + 1}",
                        nodePos.x,
                        nodePos.y - 12.dp.toPx(),
                        labelPaint
                    )
                }
            }

            // Draw current walker dot and directional heading pointer (static, no pulsing waves)
            val currentCanvasPos = mapToCanvas(currentWalkerPos)
            if (!isClosed) {
                // Directional heading arrow
                val rad = Math.toRadians((currentHeadingDeg - 90.0)).toFloat()
                val arrowEnd = Offset(
                    currentCanvasPos.x + 16.dp.toPx() * cos(rad),
                    currentCanvasPos.y + 16.dp.toPx() * sin(rad)
                )
                drawLine(
                    color = MineralMintActive,
                    start = currentCanvasPos,
                    end = arrowEnd,
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Solid white position dot with clean dark halo
                drawCircle(
                    color = Color.Black,
                    radius = 6.dp.toPx(),
                    center = currentCanvasPos
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.5.dp.toPx(),
                    center = currentCanvasPos
                )
            }
        }
    }
}
