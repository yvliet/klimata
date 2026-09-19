package com.example.klimata.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.ArrowsVertical
import com.adamglin.phosphoricons.light.BoundingBox
import com.adamglin.phosphoricons.light.Cube
import com.adamglin.phosphoricons.light.Snowflake
import com.example.klimata.data.MockData
import com.example.klimata.data.RoomState
import com.example.klimata.data.volumeCubicMeters
import com.example.klimata.ui.components.DetailPageScaffold
import com.example.klimata.ui.theme.DetailCardSurface
import com.example.klimata.ui.theme.DetailCardSurfaceElevated
import com.example.klimata.ui.theme.DetailTextMuted
import com.example.klimata.ui.theme.DetailTextPrimary
import com.example.klimata.ui.theme.DetailTextSecondary
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.MineralMint
import com.example.klimata.ui.theme.MineralMintActive
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RoomThermalDetailScreen(
    room: RoomState = MockData.masterBedRoom,
    onBackClick: () -> Unit = {},
) {
    DetailPageScaffold(
        title = "Room Space & Cooling",
        subtitle = room.name,
        onBackClick = onBackClick
    ) {
        // Hero Room Architecture & 3D Spatial Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DetailCardSurface)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DetailCardSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Light.Cube,
                            contentDescription = null,
                            tint = MineralMintActive,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Floor Area & Height",
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = DetailTextPrimary
                            ),
                            modifier = Modifier.alignByBaseline()
                        )
                        Text(
                            text = "${room.areaSquareMeters} m² / ${room.volumeCubicMeters} m³",
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                color = DetailTextMuted
                            ),
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${room.volumeCubicMeters} m³ Air Volume",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            letterSpacing = (-0.5).sp,
                            color = DetailTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Room dimensions and cooling air distribution",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = DetailTextSecondary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                DetailedIsometricRoomCanvas(
                    areaM2 = room.areaSquareMeters,
                    heightM = room.ceilingHeightMeters,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Dimension metric items
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DimensionStatItem(
                        title = "Floor Area",
                        value = "${room.areaSquareMeters} m²",
                        icon = PhosphorIcons.Light.BoundingBox,
                        modifier = Modifier.weight(1f)
                    )
                    DimensionStatItem(
                        title = "Ceiling Height",
                        value = "${room.ceilingHeightMeters} m",
                        icon = PhosphorIcons.Light.ArrowsVertical,
                        modifier = Modifier.weight(1f)
                    )
                    DimensionStatItem(
                        title = "Cooling Load",
                        value = "${room.coolingLoadBtu} BTU/h",
                        icon = PhosphorIcons.Light.Snowflake,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DimensionStatItem(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DetailTextMuted,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = JakartaFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = DetailTextMuted
                ),
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            style = TextStyle(
                fontFamily = JakartaFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = (-0.2).sp,
                color = DetailTextPrimary
            ),
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun DetailedIsometricRoomCanvas(
    areaM2: Int,
    heightM: Float,
    modifier: Modifier = Modifier,
) {
    val targetNormArea = (areaM2 / 35f).coerceIn(0.40f, 1.0f)
    val targetNormHeight = (heightM / 3.0f).coerceIn(0.65f, 1.0f)

    val animatedArea by animateFloatAsState(
        targetValue = targetNormArea,
        animationSpec = spring(
            dampingRatio = 0.80f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "IsoAreaSpring"
    )

    val animatedHeight by animateFloatAsState(
        targetValue = targetNormHeight,
        animationSpec = spring(
            dampingRatio = 0.80f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "IsoHeightSpring"
    )

    val floorPath = remember { Path() }
    val leftWallPath = remember { Path() }
    val rightWallPath = remember { Path() }
    val coolingConePath = remember { Path() }
    val acFrontPath = remember { Path() }
    val acTopPath = remember { Path() }
    val acBottomPath = remember { Path() }
    val streamLine1 = remember { Path() }
    val streamLine2 = remember { Path() }
    val dashEffect = remember { PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f) }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val isoAngleRad = 0.488f // ~28 degrees
        val cosA = cos(isoAngleRad)
        val sinA = sin(isoAngleRad)

        val maxVerticalSpan = h * 0.78f
        val baseRoomH = maxVerticalSpan * 0.38f * (0.80f + animatedHeight * 0.20f)
        val baseGroundSpan = (maxVerticalSpan - baseRoomH) / sinA
        val areaScale = (0.75f + animatedArea * 0.25f)
        val roomW = baseGroundSpan * 0.53f * areaScale
        val roomD = baseGroundSpan * 0.47f * areaScale
        val roomH = baseRoomH
        val acDepth = 7.dp.toPx()

        val minRelX = -roomD * cosA - acDepth * cosA
        val maxRelX = roomW * cosA
        val relCenterX = (minRelX + maxRelX) / 2f
        val originX = (w / 2f) - relCenterX

        val minRelY = -(roomW + roomD) * sinA - roomH
        val maxRelY = 0f
        val relCenterY = (minRelY + maxRelY) / 2f
        val originY = (h / 2f) - relCenterY

        fun iso(x: Float, y: Float, z: Float): Offset {
            val px = originX + (x - y) * cosA
            val py = originY - (x + y) * sinA - z
            return Offset(px, py)
        }

        val pOrigin = iso(0f, 0f, 0f)
        val pX = iso(roomW, 0f, 0f)
        val pY = iso(0f, roomD, 0f)
        val pCorner = iso(roomW, roomD, 0f)

        val pTopOrigin = iso(0f, 0f, roomH)
        val pTopX = iso(roomW, 0f, roomH)
        val pTopY = iso(0f, roomD, roomH)
        val pTopCorner = iso(roomW, roomD, roomH)

        // Back-Left Wall
        leftWallPath.rewind()
        leftWallPath.moveTo(pOrigin.x, pOrigin.y)
        leftWallPath.lineTo(pY.x, pY.y)
        leftWallPath.lineTo(pTopY.x, pTopY.y)
        leftWallPath.lineTo(pTopOrigin.x, pTopOrigin.y)
        leftWallPath.close()

        drawPath(
            path = leftWallPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.08f),
                    Color.White.copy(alpha = 0.02f)
                ),
                startY = pTopY.y,
                endY = pOrigin.y
            )
        )
        drawPath(
            path = leftWallPath,
            color = Color.White.copy(alpha = 0.12f),
            style = Stroke(width = 1f)
        )

        // Back-Right Wall
        rightWallPath.rewind()
        rightWallPath.moveTo(pY.x, pY.y)
        rightWallPath.lineTo(pCorner.x, pCorner.y)
        rightWallPath.lineTo(pTopCorner.x, pTopCorner.y)
        rightWallPath.lineTo(pTopY.x, pTopY.y)
        rightWallPath.close()

        drawPath(
            path = rightWallPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.10f),
                    Color.White.copy(alpha = 0.03f)
                ),
                startY = pTopCorner.y,
                endY = pY.y
            )
        )
        drawPath(
            path = rightWallPath,
            color = Color.White.copy(alpha = 0.15f),
            style = Stroke(width = 1f)
        )

        // Floor Grid
        val gridSteps = 4
        for (i in 1 until gridSteps) {
            val frac = i / gridSteps.toFloat()
            val start1 = iso(roomW * frac, 0f, 0f)
            val end1 = iso(roomW * frac, roomD, 0f)
            drawLine(
                color = Color.White.copy(alpha = 0.07f),
                start = start1,
                end = end1,
                strokeWidth = 1f
            )

            val start2 = iso(0f, roomD * frac, 0f)
            val end2 = iso(roomW, roomD * frac, 0f)
            drawLine(
                color = Color.White.copy(alpha = 0.07f),
                start = start2,
                end = end2,
                strokeWidth = 1f
            )
        }

        // Floor boundary
        floorPath.rewind()
        floorPath.moveTo(pOrigin.x, pOrigin.y)
        floorPath.lineTo(pX.x, pX.y)
        floorPath.lineTo(pCorner.x, pCorner.y)
        floorPath.lineTo(pY.x, pY.y)
        floorPath.close()

        drawPath(
            path = floorPath,
            color = Color.White.copy(alpha = 0.04f),
            style = Fill
        )
        drawPath(
            path = floorPath,
            color = Color.White.copy(alpha = 0.18f),
            style = Stroke(width = 1.2f)
        )

        // Split AC Wall Unit mounted on Back-Right wall
        val acYStart = roomD * 0.35f
        val acYEnd = roomD * 0.72f
        val acZBottom = roomH * 0.68f
        val acZTop = roomH * 0.88f

        val acBack1 = iso(roomW, acYStart, acZBottom)
        val acBack2 = iso(roomW, acYEnd, acZBottom)
        val acBack3 = iso(roomW, acYEnd, acZTop)
        val acBack4 = iso(roomW, acYStart, acZTop)

        val acFront1 = Offset(acBack1.x - acDepth * cosA, acBack1.y - acDepth * sinA)
        val acFront2 = Offset(acBack2.x - acDepth * cosA, acBack2.y - acDepth * sinA)
        val acFront3 = Offset(acBack3.x - acDepth * cosA, acBack3.y - acDepth * sinA)
        val acFront4 = Offset(acBack4.x - acDepth * cosA, acBack4.y - acDepth * sinA)

        acFrontPath.rewind()
        acFrontPath.moveTo(acFront1.x, acFront1.y)
        acFrontPath.lineTo(acFront2.x, acFront2.y)
        acFrontPath.lineTo(acFront3.x, acFront3.y)
        acFrontPath.lineTo(acFront4.x, acFront4.y)
        acFrontPath.close()
        drawPath(acFrontPath, Color.White.copy(alpha = 0.88f))
        drawPath(path = acFrontPath, color = Color.White, style = Stroke(width = 1.2f))

        acTopPath.rewind()
        acTopPath.moveTo(acFront4.x, acFront4.y)
        acTopPath.lineTo(acFront3.x, acFront3.y)
        acTopPath.lineTo(acBack3.x, acBack3.y)
        acTopPath.lineTo(acBack4.x, acBack4.y)
        acTopPath.close()
        drawPath(acTopPath, Color.White.copy(alpha = 0.72f))

        acBottomPath.rewind()
        acBottomPath.moveTo(acFront1.x, acFront1.y)
        acBottomPath.lineTo(acFront2.x, acFront2.y)
        acBottomPath.lineTo(acBack2.x, acBack2.y)
        acBottomPath.lineTo(acBack1.x, acBack1.y)
        acBottomPath.close()
        drawPath(acBottomPath, Color.White.copy(alpha = 0.45f))

        // Active cooling LED strip on AC unit
        drawLine(
            color = MineralMintActive,
            start = Offset(
                acFront1.x + (acFront2.x - acFront1.x) * 0.15f,
                acFront1.y + (acFront2.y - acFront1.y) * 0.15f - 1.5f
            ),
            end = Offset(
                acFront1.x + (acFront2.x - acFront1.x) * 0.85f,
                acFront1.y + (acFront2.y - acFront1.y) * 0.85f - 1.5f
            ),
            strokeWidth = 2.4f,
            cap = StrokeCap.Round
        )

        // Downward Cooling Thermal Streamlines from AC Vent
        val ventCenter = Offset(
            (acFront1.x + acFront2.x) * 0.5f,
            (acFront1.y + acFront2.y) * 0.5f
        )
        val floorTarget1 = iso(roomW * 0.35f, roomD * 0.25f, 0f)
        val floorTarget2 = iso(roomW * 0.20f, roomD * 0.55f, 0f)
        val floorTarget3 = iso(roomW * 0.50f, roomD * 0.75f, 0f)

        coolingConePath.rewind()
        coolingConePath.moveTo(acFront1.x, acFront1.y)
        coolingConePath.lineTo(acFront2.x, acFront2.y)
        coolingConePath.lineTo(floorTarget3.x, floorTarget3.y)
        coolingConePath.lineTo(floorTarget1.x, floorTarget1.y)
        coolingConePath.close()

        drawPath(
            path = coolingConePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    MineralMintActive.copy(alpha = 0.22f),
                    MineralMint.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                startY = ventCenter.y,
                endY = floorTarget2.y
            )
        )

        streamLine1.rewind()
        streamLine1.moveTo(ventCenter.x, ventCenter.y)
        streamLine1.cubicTo(
            ventCenter.x - 24f, ventCenter.y + 18f,
            floorTarget1.x + 12f, floorTarget1.y - 22f,
            floorTarget1.x, floorTarget1.y
        )
        drawPath(
            path = streamLine1,
            color = MineralMintActive.copy(alpha = 0.70f),
            style = Stroke(width = 1.5f, pathEffect = dashEffect)
        )

        streamLine2.rewind()
        streamLine2.moveTo(ventCenter.x, ventCenter.y)
        streamLine2.cubicTo(
            ventCenter.x - 18f, ventCenter.y + 28f,
            floorTarget2.x + 16f, floorTarget2.y - 18f,
            floorTarget2.x, floorTarget2.y
        )
        drawPath(
            path = streamLine2,
            color = MineralMintActive.copy(alpha = 0.55f),
            style = Stroke(width = 1.3f, pathEffect = dashEffect)
        )

        // Front Room Wireframe Bounding Highlights
        drawLine(
            color = Color.White.copy(alpha = 0.24f),
            start = pX,
            end = pTopX,
            strokeWidth = 1f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.24f),
            start = pOrigin,
            end = pTopOrigin,
            strokeWidth = 1f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.16f),
            start = pTopOrigin,
            end = pTopX,
            strokeWidth = 1f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.16f),
            start = pTopOrigin,
            end = pTopY,
            strokeWidth = 1f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.16f),
            start = pTopX,
            end = pTopCorner,
            strokeWidth = 1f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.16f),
            start = pTopY,
            end = pTopCorner,
            strokeWidth = 1f
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun RoomThermalDetailScreenPreview() {
    RoomThermalDetailScreen(room = MockData.masterBedRoom)
}
