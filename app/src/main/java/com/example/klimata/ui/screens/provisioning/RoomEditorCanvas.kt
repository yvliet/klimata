package com.example.klimata.ui.screens.provisioning

import android.graphics.Paint
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klimata.ui.theme.DetailCardBorder
import com.example.klimata.ui.theme.DetailCardSurface
import com.example.klimata.ui.theme.MineralMintActive
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Preset wireframe mode — renders a rectangular room schematic with dimension labels.
 * Width/length driven externally by sliders; canvas morphs via spring animation.
 */
@Composable
fun PresetRoomCanvas(
    widthMeters: Float,
    lengthMeters: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    val animWidth = remember { Animatable(widthMeters) }
    val animLength = remember { Animatable(lengthMeters) }

    LaunchedEffect(widthMeters) {
        animWidth.animateTo(
            widthMeters,
            animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
        )
    }
    LaunchedEffect(lengthMeters) {
        animLength.animateTo(
            lengthMeters,
            animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
        )
    }

    val labelPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = with(density) { 11.sp.toPx() }
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
            )
        }
    }

    val mintLabelPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.argb(255, 52, 211, 153)
            textSize = with(density) { 10.sp.toPx() }
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(DetailCardSurface),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = animWidth.value
            val l = animLength.value

            val padding = 48.dp.toPx()
            val availW = size.width - padding * 2
            val availH = size.height - padding * 2

            val scale = minOf(availW / w, availH / l)
            val roomPxW = w * scale
            val roomPxL = l * scale

            val left = (size.width - roomPxW) / 2f
            val top = (size.height - roomPxL) / 2f

            val corners = listOf(
                Offset(left, top),
                Offset(left + roomPxW, top),
                Offset(left + roomPxW, top + roomPxL),
                Offset(left, top + roomPxL)
            )

            // Subtle grid dots inside the room
            val gridStep = 0.5f * scale
            var gx = left + gridStep
            while (gx < left + roomPxW) {
                var gy = top + gridStep
                while (gy < top + roomPxL) {
                    drawCircle(
                        color = DetailCardBorder.copy(alpha = 0.25f),
                        radius = 1.5f,
                        center = Offset(gx, gy)
                    )
                    gy += gridStep
                }
                gx += gridStep
            }

            // Room polygon fill
            val roomPath = Path().apply {
                moveTo(corners[0].x, corners[0].y)
                corners.drop(1).forEach { lineTo(it.x, it.y) }
                close()
            }
            drawPath(
                roomPath,
                color = MineralMintActive.copy(alpha = 0.06f),
                style = Fill
            )

            // Room walls
            drawPath(
                roomPath,
                color = MineralMintActive,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Corner nodes
            corners.forEach { corner ->
                drawCircle(
                    color = MineralMintActive,
                    radius = 4.dp.toPx(),
                    center = corner
                )
            }

            // Dimension labels on each wall
            val wallLabels = listOf(
                Triple(corners[0], corners[1], w),   // top (width)
                Triple(corners[1], corners[2], l),   // right (length)
                Triple(corners[2], corners[3], w),   // bottom (width)
                Triple(corners[3], corners[0], l)    // left (length)
            )

            val labelOffset = 18.dp.toPx()
            wallLabels.forEachIndexed { i, (p1, p2, meters) ->
                val mid = Offset((p1.x + p2.x) / 2f, (p1.y + p2.y) / 2f)
                val label = "%.1f m".format(meters)

                val offsetY = when (i) {
                    0 -> -labelOffset
                    2 -> labelOffset + 4.dp.toPx()
                    else -> 0f
                }
                val offsetX = when (i) {
                    1 -> labelOffset + 4.dp.toPx()
                    3 -> -labelOffset - 4.dp.toPx()
                    else -> 0f
                }

                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    mid.x + offsetX,
                    mid.y + offsetY,
                    if (i == 0 || i == 2) labelPaint else mintLabelPaint
                )
            }
        }
    }
}

/**
 * Custom polygon editor — tap to place corners on a dot grid, drag to adjust.
 * Orthogonal snapping, loop closure, Shoelace area, wall dimension badges.
 */
@Composable
fun CustomRoomCanvas(
    vertices: List<Offset>,
    onVerticesChanged: (List<Offset>) -> Unit,
    isClosed: Boolean,
    onClosedChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val view = LocalView.current

    val gridSpacingMeters = 0.5f
    val pixelsPerMeter = with(density) { 60.dp.toPx() }
    val gridPx = gridSpacingMeters * pixelsPerMeter

    // Touch radius for vertex hit-testing (48dp Material spec)
    val touchRadiusPx = with(density) { 36.dp.toPx() }
    // Magnetic closure radius
    val closureRadiusPx = with(density) { 28.dp.toPx() }
    // Snap threshold for orthogonal locking
    val orthoSnapDeg = 7.5f

    var activeVertexIndex by remember { mutableStateOf<Int?>(null) }

    val labelPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = with(density) { 10.sp.toPx() }
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    val areaLabelPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.argb(255, 52, 211, 153)
            textSize = with(density) { 13.sp.toPx() }
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    fun snapToGrid(raw: Offset): Offset {
        val snappedX = (raw.x / gridPx).roundToInt() * gridPx
        val snappedY = (raw.y / gridPx).roundToInt() * gridPx
        return Offset(snappedX, snappedY)
    }

    fun snapOrtho(newPos: Offset, prevPos: Offset): Offset {
        val dx = newPos.x - prevPos.x
        val dy = newPos.y - prevPos.y
        val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
        val normAngle = ((angle % 90f) + 90f) % 90f

        return if (normAngle < orthoSnapDeg || normAngle > 90f - orthoSnapDeg) {
            // Near 0/90/180/270 — snap to axis
            if (abs(dx) > abs(dy)) {
                Offset(newPos.x, prevPos.y)
            } else {
                Offset(prevPos.x, newPos.y)
            }
        } else {
            newPos
        }
    }

    fun shoelaceArea(pts: List<Offset>): Float {
        if (pts.size < 3) return 0f
        var sum = 0f
        for (i in pts.indices) {
            val p1 = pts[i]
            val p2 = pts[(i + 1) % pts.size]
            sum += (p1.x * p2.y - p2.x * p1.y)
        }
        return abs(sum / 2f) / (pixelsPerMeter * pixelsPerMeter)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(DetailCardSurface),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isClosed) {
                    if (!isClosed) {
                        detectTapGestures { tapOffset ->
                            val snapped = snapToGrid(tapOffset)
                            val orthoSnapped = if (vertices.isNotEmpty()) {
                                snapOrtho(snapped, vertices.last())
                            } else {
                                snapped
                            }
                            val finalPos = snapToGrid(orthoSnapped)

                            // Check for closure
                            if (vertices.size >= 3) {
                                val distToFirst = sqrt(
                                    (finalPos.x - vertices.first().x).let { it * it } +
                                            (finalPos.y - vertices.first().y).let { it * it }
                                )
                                if (distToFirst < closureRadiusPx) {
                                    onClosedChanged(true)
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                    return@detectTapGestures
                                }
                            }

                            onVerticesChanged(vertices + finalPos)
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        }
                    }
                }
                .pointerInput(vertices, isClosed) {
                    if (isClosed && vertices.size >= 3) {
                        detectDragGestures(
                            onDragStart = { touchOffset ->
                                activeVertexIndex = vertices.indexOfFirst { vertex ->
                                    sqrt(
                                        (vertex.x - touchOffset.x).let { it * it } +
                                                (vertex.y - touchOffset.y).let { it * it }
                                    ) <= touchRadiusPx
                                }.takeIf { it != -1 }
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                activeVertexIndex?.let { index ->
                                    val raw = change.position
                                    val snapped = snapToGrid(raw)
                                    val newVerts = vertices.toMutableList()
                                    newVerts[index] = snapped
                                    onVerticesChanged(newVerts)
                                }
                            },
                            onDragEnd = {
                                activeVertexIndex = null
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            }
                        )
                    }
                }
        ) {
            // Dot grid background
            val cols = (size.width / gridPx).toInt() + 1
            val rows = (size.height / gridPx).toInt() + 1
            val gridOffsetX = (size.width - (cols - 1) * gridPx) / 2f
            val gridOffsetY = (size.height - (rows - 1) * gridPx) / 2f

            for (col in 0 until cols) {
                for (row in 0 until rows) {
                    drawCircle(
                        color = DetailCardBorder.copy(alpha = 0.30f),
                        radius = 1.5f,
                        center = Offset(
                            gridOffsetX + col * gridPx,
                            gridOffsetY + row * gridPx
                        )
                    )
                }
            }

            if (vertices.isEmpty()) return@Canvas

            // Draw polygon
            val polyPath = Path().apply {
                moveTo(vertices.first().x, vertices.first().y)
                for (i in 1 until vertices.size) {
                    lineTo(vertices[i].x, vertices[i].y)
                }
                if (isClosed) close()
            }

            if (isClosed) {
                drawPath(
                    polyPath,
                    color = MineralMintActive.copy(alpha = 0.08f),
                    style = Fill
                )
            }

            drawPath(
                polyPath,
                color = MineralMintActive,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Dashed active leg to indicate next wall direction
            if (!isClosed && vertices.size >= 2) {
                val last = vertices.last()
                val first = vertices.first()
                drawLine(
                    color = MineralMintActive.copy(alpha = 0.35f),
                    start = last,
                    end = first,
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                )
            }

            // Wall dimension badges
            val segCount = if (isClosed) vertices.size else vertices.size - 1
            for (i in 0 until segCount) {
                val p1 = vertices[i]
                val p2 = vertices[(i + 1) % vertices.size]
                val mid = Offset((p1.x + p2.x) / 2f, (p1.y + p2.y) / 2f)
                val lengthM = sqrt(
                    (p2.x - p1.x).let { it * it } + (p2.y - p1.y).let { it * it }
                ) / pixelsPerMeter
                val label = "%.1f m".format(lengthM)

                // Offset label perpendicular to wall
                val dx = p2.x - p1.x
                val dy = p2.y - p1.y
                val len = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                val nx = -dy / len * 14.dp.toPx()
                val ny = dx / len * 14.dp.toPx()

                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    mid.x + nx,
                    mid.y + ny,
                    labelPaint
                )
            }

            // Corner nodes
            vertices.forEachIndexed { i, vertex ->
                val isActive = i == activeVertexIndex
                val isFirst = i == 0

                // Closure target ring on first vertex when polygon not yet closed
                if (isFirst && !isClosed && vertices.size >= 3) {
                    drawCircle(
                        color = MineralMintActive.copy(alpha = 0.20f),
                        radius = closureRadiusPx,
                        center = vertex
                    )
                }

                drawCircle(
                    color = DetailCardSurface,
                    radius = if (isActive) 10.dp.toPx() else 7.dp.toPx(),
                    center = vertex
                )
                drawCircle(
                    color = MineralMintActive,
                    radius = if (isActive) 7.dp.toPx() else 4.5.dp.toPx(),
                    center = vertex
                )
            }

            // Area badge in center of closed polygon
            if (isClosed && vertices.size >= 3) {
                val area = shoelaceArea(vertices)
                val cx = vertices.map { it.x }.average().toFloat()
                val cy = vertices.map { it.y }.average().toFloat()

                drawContext.canvas.nativeCanvas.drawText(
                    "%.1f m²".format(area),
                    cx,
                    cy,
                    areaLabelPaint
                )
            }
        }
    }
}
