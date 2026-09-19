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
import kotlin.math.sin

@Composable
fun RoomConstructionCanvas(
    areaM2: Int,
    heightM: Float = 2.8f,
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

    val floorPath = remember { Path() }
    val leftWallPath = remember { Path() }
    val rightWallPath = remember { Path() }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(DetailCardSurface),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val curProgress = progress.value

            val isoAngleRad = 0.488f // ~28 degrees
            val cosA = cos(isoAngleRad)
            val sinA = sin(isoAngleRad)

            val maxVerticalSpan = h * 0.72f
            val baseRoomH = maxVerticalSpan * 0.38f * (heightM / 2.8f).coerceIn(0.8f, 1.2f)
            val baseGroundSpan = (maxVerticalSpan - baseRoomH) / sinA
            val areaScale = (areaM2 / 20f).coerceIn(0.75f, 1.25f)
            val roomW = baseGroundSpan * 0.52f * areaScale
            val roomD = baseGroundSpan * 0.48f * areaScale
            val roomH = baseRoomH

            val originX = w / 2f - (roomW - roomD) * cosA * 0.12f
            val originY = h / 2f + (roomW + roomD) * sinA * 0.25f

            fun iso(x: Float, y: Float, z: Float): Offset {
                val px = originX + (x - y) * cosA
                val py = originY - (x + y) * sinA - z
                return Offset(px, py)
            }

            fun lerpPoint(p1: Offset, p2: Offset, t: Float): Offset {
                val clamped = t.coerceIn(0f, 1f)
                return Offset(p1.x + (p2.x - p1.x) * clamped, p1.y + (p2.y - p1.y) * clamped)
            }

            val pOrigin = iso(0f, 0f, 0f)
            val pX = iso(roomW, 0f, 0f)
            val pY = iso(0f, roomD, 0f)
            val pFar = iso(roomW, roomD, 0f)

            val pTopOrigin = iso(0f, 0f, roomH)
            val pTopX = iso(roomW, 0f, roomH)
            val pTopY = iso(0f, roomD, roomH)
            val pTopFar = iso(roomW, roomD, roomH)

            // Stage 1 (0.0 to 0.35): Floor wireframe draws line-by-line
            val floorP = (curProgress / 0.35f).coerceIn(0f, 1f)
            if (floorP > 0f) {
                // Segment 1: pOrigin -> pX (0.0 to 0.25 of floor)
                val s1P = (floorP / 0.25f).coerceIn(0f, 1f)
                drawLine(
                    color = MineralMintActive,
                    start = pOrigin,
                    end = lerpPoint(pOrigin, pX, s1P),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Segment 2: pX -> pFar (0.25 to 0.50)
                if (floorP > 0.25f) {
                    val s2P = ((floorP - 0.25f) / 0.25f).coerceIn(0f, 1f)
                    drawLine(
                        color = MineralMintActive,
                        start = pX,
                        end = lerpPoint(pX, pFar, s2P),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Segment 3: pFar -> pY (0.50 to 0.75)
                if (floorP > 0.50f) {
                    val s3P = ((floorP - 0.50f) / 0.25f).coerceIn(0f, 1f)
                    drawLine(
                        color = MineralMintActive,
                        start = pFar,
                        end = lerpPoint(pFar, pY, s3P),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Segment 4: pY -> pOrigin (0.75 to 1.0)
                if (floorP > 0.75f) {
                    val s4P = ((floorP - 0.75f) / 0.25f).coerceIn(0f, 1f)
                    drawLine(
                        color = MineralMintActive,
                        start = pY,
                        end = lerpPoint(pY, pOrigin, s4P),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Floor nodes
                val nodeCount = when {
                    floorP > 0.75f -> 4
                    floorP > 0.50f -> 3
                    floorP > 0.25f -> 2
                    floorP > 0.05f -> 1
                    else -> 0
                }
                val pts = listOf(pOrigin, pX, pFar, pY)
                for (i in 0 until nodeCount) {
                    drawCircle(color = MineralMintActive, radius = 3.5.dp.toPx(), center = pts[i])
                }
            }

            // Stage 2 (0.35 to 0.65): Vertical pillars rise upward from the floor
            if (curProgress > 0.35f) {
                val pillarP = ((curProgress - 0.35f) / 0.30f).coerceIn(0f, 1f)

                // Pillar 1: pOrigin -> pTopOrigin
                drawLine(
                    color = Color.White.copy(alpha = 0.85f),
                    start = pOrigin,
                    end = lerpPoint(pOrigin, pTopOrigin, pillarP),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Pillar 2: pX -> pTopX
                drawLine(
                    color = Color.White.copy(alpha = 0.85f),
                    start = pX,
                    end = lerpPoint(pX, pTopX, pillarP),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Pillar 3: pY -> pTopY
                drawLine(
                    color = Color.White.copy(alpha = 0.85f),
                    start = pY,
                    end = lerpPoint(pY, pTopY, pillarP),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Pillar 4: pFar -> pTopFar
                drawLine(
                    color = Color.White.copy(alpha = 0.85f),
                    start = pFar,
                    end = lerpPoint(pFar, pTopFar, pillarP),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Stage 3 (0.65 to 0.85): Ceiling frame connects
            if (curProgress > 0.65f) {
                val ceilP = ((curProgress - 0.65f) / 0.20f).coerceIn(0f, 1f)
                drawLine(
                    color = MineralMintActive.copy(alpha = 0.90f),
                    start = pTopOrigin,
                    end = lerpPoint(pTopOrigin, pTopX, ceilP),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = MineralMintActive.copy(alpha = 0.90f),
                    start = pTopX,
                    end = lerpPoint(pTopX, pTopFar, ceilP),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = MineralMintActive.copy(alpha = 0.90f),
                    start = pTopFar,
                    end = lerpPoint(pTopFar, pTopY, ceilP),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = MineralMintActive.copy(alpha = 0.90f),
                    start = pTopY,
                    end = lerpPoint(pTopY, pTopOrigin, ceilP),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Stage 4 (0.85 to 1.0): Walls softly illuminate + Split AC unit mounts
            if (curProgress > 0.85f) {
                val wallAlpha = ((curProgress - 0.85f) / 0.15f).coerceIn(0f, 1f)

                // Left wall translucent plane
                leftWallPath.rewind()
                leftWallPath.moveTo(pOrigin.x, pOrigin.y)
                leftWallPath.lineTo(pY.x, pY.y)
                leftWallPath.lineTo(pTopY.x, pTopY.y)
                leftWallPath.lineTo(pTopOrigin.x, pTopOrigin.y)
                leftWallPath.close()
                drawPath(
                    path = leftWallPath,
                    brush = Brush.verticalGradient(
                        listOf(
                            MineralMintActive.copy(alpha = 0.12f * wallAlpha),
                            Color.Transparent
                        ),
                        startY = pTopY.y,
                        endY = pOrigin.y
                    ),
                    style = Fill
                )

                // Right wall translucent plane
                rightWallPath.rewind()
                rightWallPath.moveTo(pY.x, pY.y)
                rightWallPath.lineTo(pFar.x, pFar.y)
                rightWallPath.lineTo(pTopFar.x, pTopFar.y)
                rightWallPath.lineTo(pTopY.x, pTopY.y)
                rightWallPath.close()
                drawPath(
                    path = rightWallPath,
                    brush = Brush.verticalGradient(
                        listOf(
                            MineralMint.copy(alpha = 0.15f * wallAlpha),
                            Color.Transparent
                        ),
                        startY = pTopFar.y,
                        endY = pY.y
                    ),
                    style = Fill
                )

                // Mounted AC wall unit on the back wall
                val acZ = roomH * 0.75f
                val acY1 = roomD * 0.35f
                val acY2 = roomD * 0.65f
                val acP1 = iso(roomW, acY1, acZ)
                val acP2 = iso(roomW, acY2, acZ)

                drawLine(
                    color = Color.White.copy(alpha = 0.90f * wallAlpha),
                    start = acP1,
                    end = acP2,
                    strokeWidth = 6.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Active mint louvre LED
                drawLine(
                    color = MineralMintActive.copy(alpha = wallAlpha),
                    start = Offset(acP1.x, acP1.y + 2.dp.toPx()),
                    end = Offset(acP2.x, acP2.y + 2.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
