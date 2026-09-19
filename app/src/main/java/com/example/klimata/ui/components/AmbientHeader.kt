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
import com.adamglin.phosphoricons.light.Cube
import com.adamglin.phosphoricons.light.DotsThreeVertical
import com.adamglin.phosphoricons.light.Plus

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
        verticalArrangement = Arrangement.spacedBy(4.dp)
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

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 2.dp)
        ) {
            val isPrimarySelected = currentRoomIndex == 0
            val cubeColor by animateColorAsState(
                targetValue = if (isPrimarySelected) OnSkyPrimary else OnSkySecondary.copy(alpha = 0.45f),
                animationSpec = tween(200),
                label = "cubeColor"
            )

            Icon(
                imageVector = PhosphorIcons.Light.Cube,
                contentDescription = "Primary Room",
                tint = cubeColor,
                modifier = Modifier
                    .size(12.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onRoomSelected(0) }
            )

            for (i in 1 until roomCount) {
                val isSelected = i == currentRoomIndex
                val dotColor by animateColorAsState(
                    targetValue = if (isSelected) OnSkyPrimary else OnSkySecondary.copy(alpha = 0.45f),
                    animationSpec = tween(200),
                    label = "dotColor$i"
                )
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onRoomSelected(i) }
                )
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
    onAddRoomClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onAddRoomClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = PhosphorIcons.Light.Plus,
                contentDescription = "Add Room",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(
            onClick = onMenuClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = PhosphorIcons.Light.DotsThreeVertical,
                contentDescription = "More Options",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
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
    onRoomSelected: (Int) -> Unit = {},
    onAddRoomClick: () -> Unit = {},
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
            onAddRoomClick = onAddRoomClick,
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
