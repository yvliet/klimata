package com.example.klimata.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.example.klimata.SyncDialogStatusBar
import com.example.klimata.ui.theme.DetailCardSurfaceElevated
import kotlinx.coroutines.launch

@Composable
fun KlimataDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    anchorBounds: Rect = Rect.Zero,
    content: @Composable ColumnScope.() -> Unit
) {
    var isShowingDialog by remember { mutableStateOf(false) }
    val scaleAnim = remember { Animatable(0.20f) }
    val contentAlphaAnim = remember { Animatable(0.0f) }
    val scrimAlphaAnim = remember { Animatable(0.0f) }

    LaunchedEffect(expanded) {
        if (expanded) {
            scaleAnim.snapTo(0.20f)
            contentAlphaAnim.snapTo(0.0f)
            scrimAlphaAnim.snapTo(0.0f)
            isShowingDialog = true
            launch {
                scrimAlphaAnim.animateTo(
                    targetValue = 0.45f,
                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                )
            }
            launch {
                scaleAnim.animateTo(
                    targetValue = 1.0f,
                    animationSpec = spring(
                        dampingRatio = 0.78f,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            launch {
                contentAlphaAnim.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(280, easing = FastOutSlowInEasing)
                )
            }
        } else if (isShowingDialog) {
            val exitScaleSpec = tween<Float>(250, easing = FastOutSlowInEasing)
            val exitAlphaSpec = tween<Float>(180, easing = FastOutLinearInEasing)
            val exitScrimSpec = tween<Float>(250, easing = FastOutSlowInEasing)
            val jobScale = launch { scaleAnim.animateTo(0.20f, exitScaleSpec) }
            val jobAlpha = launch { contentAlphaAnim.animateTo(0.0f, exitAlphaSpec) }
            val jobScrim = launch { scrimAlphaAnim.animateTo(0.0f, exitScrimSpec) }
            jobScale.join()
            jobAlpha.join()
            jobScrim.join()
            isShowingDialog = false
        }
    }

    if (!isShowingDialog) return

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        SyncDialogStatusBar()
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            dialogWindow?.let { window ->
                window.setDimAmount(0f)
                window.setWindowAnimations(0)
            }
        }

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val density = LocalDensity.current
            val screenWidth = maxWidth
            val screenHeight = maxHeight

            val minMargin = 14.dp
            val verticalSpacing = 6.dp

            val isAnchorEmpty = anchorBounds.isEmpty
            val anchorLeftDp = if (!isAnchorEmpty) with(density) { anchorBounds.left.toDp() } else 0.dp
            val anchorRightDp = if (!isAnchorEmpty) with(density) { anchorBounds.right.toDp() } else screenWidth
            val anchorTopDp = if (!isAnchorEmpty) with(density) { anchorBounds.top.toDp() } else 0.dp
            val anchorBottomDp = if (!isAnchorEmpty) with(density) { anchorBounds.bottom.toDp() } else 54.dp

            val isAnchorOnRight = isAnchorEmpty || (anchorLeftDp + anchorRightDp) / 2f >= screenWidth / 2f
            val isAnchorNearBottom = !isAnchorEmpty && anchorBottomDp > screenHeight * 0.75f

            val transformOrigin = when {
                isAnchorNearBottom && isAnchorOnRight -> TransformOrigin(1.0f, 1.0f)
                isAnchorNearBottom && !isAnchorOnRight -> TransformOrigin(0.0f, 1.0f)
                !isAnchorNearBottom && isAnchorOnRight -> TransformOrigin(1.0f, 0.0f)
                else -> TransformOrigin(0.0f, 0.0f)
            }

            val topPadding = if (!isAnchorNearBottom) {
                if (!isAnchorEmpty) (anchorBottomDp + verticalSpacing).coerceAtLeast(minMargin) else 54.dp
            } else {
                minMargin
            }

            val bottomPadding = if (isAnchorNearBottom) {
                (screenHeight - anchorTopDp + verticalSpacing).coerceAtLeast(minMargin)
            } else {
                minMargin
            }

            val endPadding = if (isAnchorOnRight) {
                if (!isAnchorEmpty) (screenWidth - anchorRightDp).coerceAtLeast(minMargin) else minMargin
            } else {
                minMargin
            }

            val startPadding = if (!isAnchorOnRight) {
                anchorLeftDp.coerceAtLeast(minMargin)
            } else {
                minMargin
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = scrimAlphaAnim.value))
                    .pointerInput(Unit) {
                        detectTapGestures {
                            onDismissRequest()
                        }
                    }
            )

            Box(
                modifier = modifier
                    .align(
                        if (isAnchorNearBottom) {
                            if (isAnchorOnRight) Alignment.BottomEnd else Alignment.BottomStart
                        } else {
                            if (isAnchorOnRight) Alignment.TopEnd else Alignment.TopStart
                        }
                    )
                    .padding(
                        start = startPadding,
                        top = topPadding,
                        end = endPadding,
                        bottom = bottomPadding
                    )
                    .graphicsLayer {
                        this.transformOrigin = transformOrigin
                        scaleX = scaleAnim.value
                        scaleY = scaleAnim.value
                        alpha = contentAlphaAnim.value
                    }
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(18.dp),
                        ambientColor = Color.Black.copy(alpha = 0.5f),
                        spotColor = Color.Black.copy(alpha = 0.6f)
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .background(DetailCardSurfaceElevated)
                    .padding(vertical = 6.dp)
                    .width(IntrinsicSize.Max)
                    .widthIn(min = 140.dp)
            ) {
                Column(
                    modifier = Modifier.width(IntrinsicSize.Max),
                    content = content
                )
            }
        }
    }
}

@Composable
fun KlimataDropdownMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 11.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val bgAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.12f else 0.0f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMedium),
        label = "menuItemPressAlpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .background(Color.White.copy(alpha = bgAlpha))
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}
