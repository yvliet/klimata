package com.example.klimata.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.KlimataTheme
import com.example.klimata.ui.theme.OnSkyPrimary
import com.example.klimata.ui.theme.OnSkySecondary

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset

import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import com.adamglin.phosphoricons.light.Check
import com.adamglin.phosphoricons.light.Cube
import com.adamglin.phosphoricons.light.DotsThreeVertical
import com.adamglin.phosphoricons.light.Gear
import com.adamglin.phosphoricons.light.House
import com.adamglin.phosphoricons.light.Moon
import com.adamglin.phosphoricons.light.Plus
import com.adamglin.phosphoricons.light.Sun
import com.adamglin.phosphoricons.light.SunHorizon
import com.example.klimata.ui.theme.DetailCardBorder
import com.example.klimata.ui.theme.DetailCardSurfaceElevated
import com.example.klimata.ui.theme.DetailTextPrimary
import com.example.klimata.ui.theme.DetailTextSecondary
import com.example.klimata.ui.theme.DiurnalPhase
import com.example.klimata.ui.theme.LocalDiurnalColors
import com.example.klimata.ui.theme.currentDiurnalPhase

/**
 * Decoupled room name and pager dots indicator.
 * Can be positioned above the hero temperature and smoothly docked into the top bar on scroll.
 */
@Composable
fun RoomIndicator(
    currentRoom: String,
    modifier: Modifier = Modifier,
    roomCount: Int = 3,
    currentRoomIndex: Int = 0,
    onRoomSelected: (Int) -> Unit = {},
) {
    val density = LocalDensity.current
    val slideOffsetPx = with(density) { 16.dp.roundToPx() }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        AnimatedContent(
            targetState = currentRoomIndex to currentRoom,
            transitionSpec = {
                val animSpec = tween<Float>(durationMillis = 220, easing = FastOutSlowInEasing)
                val slideSpec = tween<IntOffset>(durationMillis = 220, easing = FastOutSlowInEasing)
                if (targetState.first >= initialState.first) {
                    (slideInHorizontally(animationSpec = slideSpec) { slideOffsetPx } + fadeIn(animationSpec = animSpec))
                        .togetherWith(slideOutHorizontally(animationSpec = slideSpec) { -slideOffsetPx } + fadeOut(animationSpec = animSpec))
                } else {
                    (slideInHorizontally(animationSpec = slideSpec) { -slideOffsetPx } + fadeIn(animationSpec = animSpec))
                        .togetherWith(slideOutHorizontally(animationSpec = slideSpec) { slideOffsetPx } + fadeOut(animationSpec = animSpec))
                }
            },
            label = "RoomIndicatorTransition"
        ) { (_, roomName) ->
            Text(
                text = roomName,
                style = TextStyle(
                    fontFamily = JakartaFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                    lineHeight = 24.sp
                ),
                color = OnSkyPrimary
            )
        }

        if (roomCount > 0) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 0.dp)
            ) {
                val isPrimarySelected = currentRoomIndex == 0
                val cubeColor by animateColorAsState(
                    targetValue = if (isPrimarySelected) OnSkyPrimary else OnSkySecondary.copy(alpha = 0.45f),
                    animationSpec = tween(200),
                    label = "cubeColor"
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(width = 16.dp, height = 18.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onRoomSelected(0) }
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Light.Cube,
                        contentDescription = "Primary Room",
                        tint = cubeColor,
                        modifier = Modifier.size(12.dp)
                    )
                }

                for (i in 1 until roomCount) {
                    val isSelected = i == currentRoomIndex
                    val dotColor by animateColorAsState(
                        targetValue = if (isSelected) OnSkyPrimary else OnSkySecondary.copy(alpha = 0.45f),
                        animationSpec = tween(200),
                        label = "dotColor$i"
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(width = 16.dp, height = 18.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onRoomSelected(i) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Top bar action buttons (Add Room and More Options).
 */
@Composable
fun AmbientTopBarActions(
    modifier: Modifier = Modifier,
    phase: DiurnalPhase = currentDiurnalPhase(),
    onPhaseChange: (DiurnalPhase) -> Unit = {},
    onAddRoomClick: () -> Unit = {},
    onManageRoomsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var menuAnchorBounds by remember { mutableStateOf(Rect.Zero) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onAddRoomClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = PhosphorIcons.Light.Plus,
                contentDescription = "Add Room",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Box(
            modifier = Modifier.onGloballyPositioned { coordinates ->
                menuAnchorBounds = coordinates.boundsInWindow()
            }
        ) {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = PhosphorIcons.Light.DotsThreeVertical,
                    contentDescription = "More Options",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            KlimataDropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                anchorBounds = menuAnchorBounds
            ) {
                KlimataDropdownMenuItem(
                    onClick = {
                        menuExpanded = false
                        onManageRoomsClick()
                    }
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Light.House,
                        contentDescription = null,
                        tint = DetailTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Manage Rooms",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = DetailTextPrimary
                        )
                    )
                }

                HorizontalDivider(
                    color = DetailCardBorder,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                KlimataDropdownMenuItem(
                    onClick = {
                        menuExpanded = false
                        onSettingsClick()
                    }
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Light.Gear,
                        contentDescription = null,
                        tint = DetailTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Settings",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = DetailTextPrimary
                        )
                    )
                }
            }
        }
    }
}

/**
 * Pinned persistent header containing the room name, page indicator, and top actions.
 */
@Composable
fun AmbientHeader(
    currentRoom: String,
    modifier: Modifier = Modifier,
    roomCount: Int = 3,
    currentRoomIndex: Int = 0,
    phase: DiurnalPhase = currentDiurnalPhase(),
    onPhaseChange: (DiurnalPhase) -> Unit = {},
    onRoomSelected: (Int) -> Unit = {},
    onAddRoomClick: () -> Unit = {},
    onManageRoomsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        RoomIndicator(
            currentRoom = currentRoom,
            roomCount = roomCount,
            currentRoomIndex = currentRoomIndex,
            onRoomSelected = onRoomSelected
        )

        AmbientTopBarActions(
            phase = phase,
            onPhaseChange = onPhaseChange,
            onAddRoomClick = onAddRoomClick,
            onManageRoomsClick = onManageRoomsClick,
            onSettingsClick = onSettingsClick,
            onMenuClick = onMenuClick
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1976D2)
@Composable
private fun AmbientHeaderPreview() {
    KlimataTheme {
        AmbientHeader(
            currentRoom = "Master Bed",
            roomCount = 3,
            currentRoomIndex = 0
        )
    }
}
