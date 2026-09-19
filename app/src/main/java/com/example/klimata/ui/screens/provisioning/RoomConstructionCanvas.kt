package com.example.klimata.ui.screens.provisioning

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.klimata.ui.theme.DetailCardSurface
import com.example.klimata.ui.theme.MineralMint
import com.example.klimata.ui.theme.MineralMintActive
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * 3D isometric room wireframe build animation.
 * Accepts arbitrary N-sided floor polygon vertices (in meters) and ceiling height.
 * Uses bounding-box auto-framing so the wireframe is centered and never clipped at the top.
 */
@Composable
fun RoomConstructionCanvas(
    floorVertices: List<Offset>,
    ceilingHeightM: Float = 2.8f,
    onAnimationFinished: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2400, easing = FastOutSlowInEasing)
        )
        onAnimationFinished()
    }

    val rearWallPath = remember { Path() }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(DetailCardSurface),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // Fallback to standard 4.0m x 5.0m box if fewer than 3 vertices provided
            val rawVertices = if (floorVertices.size >= 3) {
                floorVertices
            } else {
                listOf(
                    Offset(0f, 0f),
                    Offset(4f, 0f),
                    Offset(4f, 5f),
                    Offset(0f, 5f)
                )
            }

            val n = rawVertices.size
            val curProgress = progress.value

            val isoAngleRad = 0.488f // ~28 degrees
            val cosA = cos(isoAngleRad)
            val sinA = sin(isoAngleRad)

            // Project each vertex (x, y, 0) and (x, y, H) into isometric (u, v) space
            val uvFloor = rawVertices.map { v ->
                Offset((v.x - v.y) * cosA, -(v.x + v.y) * sinA)
            }
            val uvCeil = rawVertices.map { v ->
                Offset((v.x - v.y) * cosA, -(v.x + v.y) * sinA - ceilingHeightM)
            }

            val allU = (uvFloor + uvCeil).map { it.x }
            val allV = (uvFloor + uvCeil).map { it.y }

            val minU = allU.minOrNull() ?: 0f
            val maxU = allU.maxOrNull() ?: 1f
            val minV = allV.minOrNull() ?: 0f
            val maxV = allV.maxOrNull() ?: 1f

            val spanU = max(maxU - minU, 0.1f)
            val spanV = max(maxV - minV, 0.1f)

            // Safe padding so all vertices, line strokes, and AC fixture stay inside canvas bounds
            val pad = 28.dp.toPx()
            val availW = canvasW - pad * 2
            val availH = canvasH - pad * 2

            val scale = min(availW / spanU, availH / spanV)
            val midU = (minU + maxU) / 2f
            val midV = (minV + maxV) / 2f

            fun toCanvas(uv: Offset): Offset {
                return Offset(
                    x = canvasW / 2f + (uv.x - midU) * scale,
                    y = canvasH / 2f + (uv.y - midV) * scale
                )
            }

            val pFloor = uvFloor.map { toCanvas(it) }
            val pCeil = uvCeil.map { toCanvas(it) }

            fun lerpPoint(p1: Offset, p2: Offset, t: Float): Offset {
                val clamped = t.coerceIn(0f, 1f)
                return Offset(p1.x + (p2.x - p1.x) * clamped, p1.y + (p2.y - p1.y) * clamped)
            }

            // Stage 1 (0.0 to 0.35): Floor wireframe draws segment by segment
            val floorP = (curProgress / 0.35f).coerceIn(0f, 1f)
            if (floorP > 0f) {
                for (i in 0 until n) {
                    val segStart = i / n.toFloat()
                    val segEnd = (i + 1) / n.toFloat()

                    if (floorP > segStart) {
                        val segT = ((floorP - segStart) / (segEnd - segStart)).coerceIn(0f, 1f)
                        val pStart = pFloor[i]
                        val pEnd = pFloor[(i + 1) % n]
                        drawLine(
                            color = MineralMintActive,
                            start = pStart,
                            end = lerpPoint(pStart, pEnd, segT),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Glowing nodes as perimeter progresses
                val nodeCount = (floorP * n).toInt() + 1
                for (i in 0 until min(nodeCount, n)) {
                    drawCircle(color = MineralMintActive, radius = 3.5.dp.toPx(), center = pFloor[i])
                }
            }

            // Stage 2 (0.35 to 0.65): N Vertical pillars rise upward
            if (curProgress > 0.35f) {
                val pillarP = ((curProgress - 0.35f) / 0.30f).coerceIn(0f, 1f)
                for (i in 0 until n) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.85f),
                        start = pFloor[i],
                        end = lerpPoint(pFloor[i], pCeil[i], pillarP),
                        strokeWidth = 1.8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // Stage 3 (0.65 to 0.85): Ceiling frame connects
            if (curProgress > 0.65f) {
                val ceilP = ((curProgress - 0.65f) / 0.20f).coerceIn(0f, 1f)
                for (i in 0 until n) {
                    val segStart = i / n.toFloat()
                    val segEnd = (i + 1) / n.toFloat()

                    if (ceilP > segStart) {
                        val segT = ((ceilP - segStart) / (segEnd - segStart)).coerceIn(0f, 1f)
                        val pStart = pCeil[i]
                        val pEnd = pCeil[(i + 1) % n]
                        drawLine(
                            color = MineralMintActive.copy(alpha = 0.90f),
                            start = pStart,
                            end = lerpPoint(pStart, pEnd, segT),
                            strokeWidth = 1.8.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                for (i in 0 until n) {
                    drawCircle(color = MineralMintActive, radius = 3.dp.toPx(), center = pCeil[i])
                }
            }

            // Stage 4 (0.85 to 1.0): Rear walls illuminate + Split AC unit mounts
            if (curProgress > 0.85f) {
                val wallAlpha = ((curProgress - 0.85f) / 0.15f).coerceIn(0f, 1f)

                // Find rear-most wall segment (furthest back in isometric space: smallest average Y on screen)
                var bestRearIdx = 0
                var minAvgY = Float.MAX_VALUE
                for (i in 0 until n) {
                    val avgY = (pCeil[i].y + pCeil[(i + 1) % n].y) / 2f
                    if (avgY < minAvgY) {
                        minAvgY = avgY
                        bestRearIdx = i
                    }
                }

                // Illuminate the rear wall with vertical gradient
                val r1Floor = pFloor[bestRearIdx]
                val r2Floor = pFloor[(bestRearIdx + 1) % n]
                val r2Ceil = pCeil[(bestRearIdx + 1) % n]
                val r1Ceil = pCeil[bestRearIdx]

                rearWallPath.rewind()
                rearWallPath.moveTo(r1Floor.x, r1Floor.y)
                rearWallPath.lineTo(r2Floor.x, r2Floor.y)
                rearWallPath.lineTo(r2Ceil.x, r2Ceil.y)
                rearWallPath.lineTo(r1Ceil.x, r1Ceil.y)
                rearWallPath.close()

                drawPath(
                    path = rearWallPath,
                    brush = Brush.verticalGradient(
                        listOf(
                            MineralMintActive.copy(alpha = 0.14f * wallAlpha),
                            Color.Transparent
                        ),
                        startY = min(r1Ceil.y, r2Ceil.y),
                        endY = max(r1Floor.y, r2Floor.y)
                    ),
                    style = Fill
                )

                // Mount AC unit along rear wall at ~75% ceiling height
                val acZ = 0.72f
                val acStartFloor = lerpPoint(r1Floor, r2Floor, 0.28f)
                val acEndFloor = lerpPoint(r1Floor, r2Floor, 0.72f)
                val acStartCeil = lerpPoint(r1Ceil, r2Ceil, 0.28f)
                val acEndCeil = lerpPoint(r1Ceil, r2Ceil, 0.72f)

                val acP1 = lerpPoint(acStartFloor, acStartCeil, acZ)
                val acP2 = lerpPoint(acEndFloor, acEndCeil, acZ)

                drawLine(
                    color = Color.White.copy(alpha = 0.92f * wallAlpha),
                    start = acP1,
                    end = acP2,
                    strokeWidth = 5.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Active mint louvre LED
                drawLine(
                    color = MineralMintActive.copy(alpha = wallAlpha),
                    start = Offset(acP1.x, acP1.y + 2.dp.toPx()),
                    end = Offset(acP2.x, acP2.y + 2.dp.toPx()),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
