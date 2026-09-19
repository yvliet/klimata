package com.example.klimata.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.getValue

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
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AnimatedContent(
            targetState = currentRoomIndex to currentRoom,
            transitionSpec = {
                if (targetState.first >= initialState.first) {
                    (slideInHorizontally(animationSpec = tween(220)) { fullWidth -> fullWidth } + fadeIn(animationSpec = tween(220)))
                        .togetherWith(slideOutHorizontally(animationSpec = tween(220)) { fullWidth -> -fullWidth } + fadeOut(animationSpec = tween(220)))
                } else {
                    (slideInHorizontally(animationSpec = tween(220)) { fullWidth -> -fullWidth } + fadeIn(animationSpec = tween(220)))
                        .togetherWith(slideOutHorizontally(animationSpec = tween(220)) { fullWidth -> fullWidth } + fadeOut(animationSpec = tween(220)))
                }
            },
            label = "RoomIndicatorTransition"
        ) { (_, roomName) ->
            Text(
                text = roomName,
                fontFamily = JakartaFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                color = OnSkyPrimary
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 2.dp)
        ) {
            Icon(
                imageVector = SimpleArrowIcon,
                contentDescription = null,
                tint = OnSkySecondary,
                modifier = Modifier.size(9.dp)
            )

            Spacer(modifier = Modifier.width(2.dp))

            for (i in 0 until roomCount) {
                val isSelected = i == currentRoomIndex
                val dotSize by animateDpAsState(
                    targetValue = if (isSelected) 6.dp else 4.dp,
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium),
                    label = "dotSize$i"
                )
                val dotColor by animateColorAsState(
                    targetValue = if (isSelected) OnSkyPrimary else OnSkySecondary.copy(alpha = 0.45f),
                    animationSpec = tween(200),
                    label = "dotColor$i"
                )
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(dotColor)
                        .clickable { onRoomSelected(i) }
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
                imageVector = PlusIcon,
                contentDescription = "Add Room",
                tint = OnSkyPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(
            onClick = onMenuClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = MoreVerticalIcon,
                contentDescription = "More Options",
                tint = OnSkyPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Top app bar displaying the current active room name, pager position indicator dots,
 * and navigation actions.
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

// Inlined vector paths avoid pulling in material-icons-extended (~30MB) for basic glyphs
private val SimpleArrowIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "SimpleArrow",
        defaultWidth = 12.dp,
        defaultHeight = 12.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = androidx.compose.ui.graphics.SolidColor(Color.White)) {
            moveTo(3f, 3f)
            lineTo(21f, 11f)
            lineTo(13f, 13f)
            lineTo(11f, 21f)
            close()
        }
    }.build()
}

private val PlusIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Plus",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = androidx.compose.ui.graphics.SolidColor(Color.White),
            strokeLineWidth = 2.2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round
        ) {
            moveTo(12f, 5f)
            lineTo(12f, 19f)
            moveTo(5f, 12f)
            lineTo(19f, 12f)
        }
    }.build()
}

private val MoreVerticalIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "MoreVertical",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = androidx.compose.ui.graphics.SolidColor(Color.White)) {
            moveTo(12f, 6f)
            arcTo(1.5f, 1.5f, 0f, true, true, 12f, 3f)
            arcTo(1.5f, 1.5f, 0f, false, true, 12f, 6f)
            close()
            moveTo(12f, 13.5f)
            arcTo(1.5f, 1.5f, 0f, true, true, 12f, 10.5f)
            arcTo(1.5f, 1.5f, 0f, false, true, 12f, 13.5f)
            close()
            moveTo(12f, 21f)
            arcTo(1.5f, 1.5f, 0f, true, true, 12f, 18f)
            arcTo(1.5f, 1.5f, 0f, false, true, 12f, 21f)
            close()
        }
    }.build()
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
