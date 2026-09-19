package com.example.klimata.ui.screens.provisioning

import android.graphics.Paint
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.sp
import com.example.klimata.ui.theme.DetailCardBorder
import com.example.klimata.ui.theme.DetailCardSurface
import com.example.klimata.ui.theme.DetailCardSurfaceElevated
import com.example.klimata.ui.theme.DetailTextPrimary
import com.example.klimata.ui.theme.DetailTextSecondary
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.LocalDiurnalColors
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Interactive room wireframe preview with live 3D isometric and 2D plan views.
 * Updates 1:1 immediately with slider values for zero-latency dragging.
 */
@Composable
fun PresetRoomCanvas(
    widthMeters: Float,
    lengthMeters: Float,
    ceilingHeightMeters: Float = 2.8f,
    accentColor: Color = LocalDiurnalColors.current.accentColor,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    val labelPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = with(density) { 10.5.sp.toPx() }
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    val accentLabelPaint = remember(density, accentColor) {
        Paint().apply {
            color = accentColor.toArgb()
            textSize = with(density) { 10.sp.toPx() }
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    val rearWallRight = remember { Path() }
    val rearWallLeft = remember { Path() }
    val floorPath = remember { Path() }
    val ceilPath = remember { Path() }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(DetailCardSurface),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            val isoAngle = 0.5236f
            val cosA = cos(isoAngle)
            val sinA = sin(isoAngle)

            val w = widthMeters
            val l = lengthMeters
            val h = ceilingHeightMeters

            val pts3D = listOf(
                Triple(0f, 0f, 0f),
                Triple(w, 0f, 0f),
                Triple(w, l, 0f),
                Triple(0f, l, 0f),
                Triple(0f, 0f, h),
                Triple(w, 0f, h),
                Triple(w, l, h),
                Triple(0f, l, h)
            )

            val uvPts = pts3D.map { (x, y, z) ->
                val u = (x - y) * cosA
                val v = -(x + y) * sinA - z
                Offset(u, v)
            }

            val minU = uvPts.minOf { it.x }
            val maxU = uvPts.maxOf { it.x }
            val minV = uvPts.minOf { it.y }
            val maxV = uvPts.maxOf { it.y }

            val spanU = max(maxU - minU, 0.1f)
            val spanV = max(maxV - minV, 0.1f)

            val pad = 36.dp.toPx()
            val availW = canvasW - pad * 2
            val availH = canvasH - pad * 2

            val scale = min(availW / spanU, availH / spanV)
            val midU = (minU + maxU) / 2f
            val midV = (minV + maxV) / 2f

            fun project(uv: Offset): Offset {
                return Offset(
                    x = canvasW / 2f + (uv.x - midU) * scale,
                    y = canvasH / 2f + (uv.y - midV) * scale
                )
            }

            val p = uvPts.map { project(it) }

            rearWallRight.rewind()
            rearWallRight.moveTo(p[1].x, p[1].y)
            rearWallRight.lineTo(p[2].x, p[2].y)
            rearWallRight.lineTo(p[6].x, p[6].y)
            rearWallRight.lineTo(p[5].x, p[5].y)
            rearWallRight.close()
            drawPath(rearWallRight, color = accentColor.copy(alpha = 0.04f), style = Fill)

            rearWallLeft.rewind()
            rearWallLeft.moveTo(p[3].x, p[3].y)
            rearWallLeft.lineTo(p[2].x, p[2].y)
            rearWallLeft.lineTo(p[6].x, p[6].y)
            rearWallLeft.lineTo(p[7].x, p[7].y)
            rearWallLeft.close()
            drawPath(rearWallLeft, color = accentColor.copy(alpha = 0.05f), style = Fill)

            floorPath.rewind()
            floorPath.moveTo(p[0].x, p[0].y)
            floorPath.lineTo(p[1].x, p[1].y)
            floorPath.lineTo(p[2].x, p[2].y)
            floorPath.lineTo(p[3].x, p[3].y)
            floorPath.close()
            drawPath(floorPath, color = accentColor.copy(alpha = 0.06f), style = Fill)
            drawPath(floorPath, color = accentColor.copy(alpha = 0.85f), style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round))

            val pillarColor = Color.White.copy(alpha = 0.70f)
            val pillarStroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
            drawLine(pillarColor, p[0], p[4], strokeWidth = pillarStroke.width, cap = StrokeCap.Round)
            drawLine(pillarColor, p[1], p[5], strokeWidth = pillarStroke.width, cap = StrokeCap.Round)
            drawLine(pillarColor, p[2], p[6], strokeWidth = pillarStroke.width, cap = StrokeCap.Round)
            drawLine(pillarColor, p[3], p[7], strokeWidth = pillarStroke.width, cap = StrokeCap.Round)

            ceilPath.rewind()
            ceilPath.moveTo(p[4].x, p[4].y)
            ceilPath.lineTo(p[5].x, p[5].y)
            ceilPath.lineTo(p[6].x, p[6].y)
            ceilPath.lineTo(p[7].x, p[7].y)
            ceilPath.close()
            drawPath(ceilPath, color = accentColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

            for (node in p) {
                drawCircle(color = DetailCardSurface, radius = 4.5.dp.toPx(), center = node)
                drawCircle(color = accentColor, radius = 3.dp.toPx(), center = node)
            }

            val midWidth = Offset((p[0].x + p[1].x) / 2f, (p[0].y + p[1].y) / 2f + 14.dp.toPx())
            drawContext.canvas.nativeCanvas.drawText(
                "W: %.1f m".format(w),
                midWidth.x,
                midWidth.y,
                labelPaint
            )

            val midLength = Offset((p[0].x + p[3].x) / 2f - 22.dp.toPx(), (p[0].y + p[3].y) / 2f + 6.dp.toPx())
            drawContext.canvas.nativeCanvas.drawText(
                "L: %.1f m".format(l),
                midLength.x,
                midLength.y,
                labelPaint
            )

            val midHeight = Offset(p[1].x + 22.dp.toPx(), (p[1].y + p[5].y) / 2f)
            drawContext.canvas.nativeCanvas.drawText(
                "H: %.1f m".format(h),
                midHeight.x,
                midHeight.y,
                accentLabelPaint
            )
        }
    }
}

/**
 * Custom polygon editor — tap to place corners on a dot grid, drag to adjust.
 * Uses rememberUpdatedState to prevent stale pointer input closures so any number of corners can be added.
 */
@Composable
fun CustomRoomCanvas(
    vertices: List<Offset>,
    onVerticesChanged: (List<Offset>) -> Unit,
    isClosed: Boolean,
    onClosedChanged: (Boolean) -> Unit,
    accentColor: Color = LocalDiurnalColors.current.accentColor,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val view = LocalView.current

    // Live updated state references to prevent stale closures in pointerInput
    val currentVertices by rememberUpdatedState(vertices)
    val currentIsClosed by rememberUpdatedState(isClosed)
    val currentOnVerticesChanged by rememberUpdatedState(onVerticesChanged)
    val currentOnClosedChanged by rememberUpdatedState(onClosedChanged)

    val gridSpacingMeters = 0.5f
    val pixelsPerMeter = with(density) { 60.dp.toPx() }
    val gridPx = gridSpacingMeters * pixelsPerMeter

    // Touch radius for vertex hit-testing (48dp spec)
    val touchRadiusPx = with(density) { 40.dp.toPx() }
    // Magnetic closure radius around first vertex
    val closureRadiusPx = with(density) { 32.dp.toPx() }

    var draggedVertexIndex by remember { mutableStateOf<Int?>(null) }

    val labelPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = with(density) { 10.sp.toPx() }
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    val areaLabelPaint = remember(density, accentColor) {
        Paint().apply {
            color = accentColor.toArgb()
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

    val polyPath = remember { Path() }
    val draftDashEffect = remember { PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(DetailCardSurface),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        if (currentIsClosed) return@detectTapGestures

                        val liveVerts = currentVertices
                        val snapped = snapToGrid(tapOffset)

                        // Check for magnetic closure on start vertex
                        if (liveVerts.size >= 3) {
                            val distToFirst = sqrt(
                                (snapped.x - liveVerts.first().x).let { it * it } +
                                        (snapped.y - liveVerts.first().y).let { it * it }
                            )
                            if (distToFirst <= closureRadiusPx) {
                                currentOnClosedChanged(true)
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                return@detectTapGestures
                            }
                        }

                        // Prevent duplicate taps at identical position as previous vertex
                        if (liveVerts.isNotEmpty()) {
                            val distToLast = sqrt(
                                (snapped.x - liveVerts.last().x).let { it * it } +
                                        (snapped.y - liveVerts.last().y).let { it * it }
                            )
                            if (distToLast < gridPx * 0.75f) {
                                return@detectTapGestures
                            }
                        }

                        currentOnVerticesChanged(liveVerts + snapped)
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { touchOffset ->
                            if (!currentIsClosed) return@detectDragGestures
                            val liveVerts = currentVertices
                            val idx = liveVerts.indexOfFirst { vertex ->
                                sqrt(
                                    (vertex.x - touchOffset.x).let { it * it } +
                                            (vertex.y - touchOffset.y).let { it * it }
                                ) <= touchRadiusPx
                            }
                            if (idx >= 0) {
                                draggedVertexIndex = idx
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            }
                        },
                        onDrag = { change, _ ->
                            val idx = draggedVertexIndex ?: return@detectDragGestures
                            change.consume()
                            val snapped = snapToGrid(change.position)
                            val liveVerts = currentVertices.toMutableList()
                            if (idx in liveVerts.indices) {
                                liveVerts[idx] = snapped
                                currentOnVerticesChanged(liveVerts)
                            }
                        },
                        onDragEnd = {
                            if (draggedVertexIndex != null) {
                                draggedVertexIndex = null
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            }
                        },
                        onDragCancel = {
                            draggedVertexIndex = null
                        }
                    )
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height

            val cols = (canvasW / gridPx).toInt()
            val rows = (canvasH / gridPx).toInt()
            val gridOffsetX = (canvasW - cols * gridPx) / 2f
            val gridOffsetY = (canvasH - rows * gridPx) / 2f

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

            polyPath.rewind()
            polyPath.moveTo(vertices.first().x, vertices.first().y)
            for (i in 1 until vertices.size) {
                polyPath.lineTo(vertices[i].x, vertices[i].y)
            }
            if (isClosed) polyPath.close()

            if (isClosed) {
                drawPath(
                    polyPath,
                    color = accentColor.copy(alpha = 0.08f),
                    style = Fill
                )
            }

            drawPath(
                polyPath,
                color = accentColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            if (!isClosed && vertices.size >= 2) {
                val last = vertices.last()
                val first = vertices.first()
                drawLine(
                    color = accentColor.copy(alpha = 0.35f),
                    start = last,
                    end = first,
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = draftDashEffect
                )
            }

            val segCount = if (isClosed) vertices.size else vertices.size - 1
            for (i in 0 until segCount) {
                val p1 = vertices[i]
                val p2 = vertices[(i + 1) % vertices.size]
                val mid = Offset((p1.x + p2.x) / 2f, (p1.y + p2.y) / 2f)
                val lengthM = sqrt(
                    (p2.x - p1.x).let { it * it } + (p2.y - p1.y).let { it * it }
                ) / pixelsPerMeter
                val label = "%.1f m".format(lengthM)

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

            vertices.forEachIndexed { i, vertex ->
                val isActive = i == draggedVertexIndex
                val isFirst = i == 0

                if (isFirst && !isClosed && vertices.size >= 3) {
                    drawCircle(
                        color = accentColor.copy(alpha = 0.25f),
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
                    color = accentColor,
                    radius = if (isActive) 7.dp.toPx() else 4.5.dp.toPx(),
                    center = vertex
                )
            }

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
