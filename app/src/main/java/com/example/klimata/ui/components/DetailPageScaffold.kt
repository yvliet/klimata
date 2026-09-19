package com.example.klimata.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.ArrowLeft
import com.example.klimata.ui.theme.DetailBlackBackground
import com.example.klimata.ui.theme.DetailCardSurface
import com.example.klimata.ui.theme.DetailTextPrimary
import com.example.klimata.ui.theme.DetailTextSecondary
import com.example.klimata.ui.theme.JakartaFamily

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp

val LocalPageCornerRadius = compositionLocalOf { 0.dp }

val DetailPageShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

@Composable
fun Modifier.detailPageContainer(
    backgroundColor: Color = DetailBlackBackground,
    cornerRadius: Dp = LocalPageCornerRadius.current,
): Modifier {
    val shapeModifier = if (cornerRadius > 0.dp) {
        this.clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
    } else {
        this
    }
    return shapeModifier
        .fillMaxSize()
        .background(backgroundColor)
        .statusBarsPadding()
        .navigationBarsPadding()
}

@Composable
fun Modifier.detailPageContainer(
    brush: Brush,
    cornerRadius: Dp = LocalPageCornerRadius.current,
): Modifier {
    val shapeModifier = if (cornerRadius > 0.dp) {
        this.clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
    } else {
        this
    }
    return shapeModifier
        .fillMaxSize()
        .background(brush)
        .statusBarsPadding()
        .navigationBarsPadding()
}

fun Modifier.detailPageContainer(
    backgroundColor: Color = DetailBlackBackground,
    shape: Shape,
): Modifier = this
    .fillMaxSize()
    .clip(shape)
    .background(backgroundColor)
    .statusBarsPadding()
    .navigationBarsPadding()

/**
 * Minimalist back navigation button without background shape or circle.
 * Dims the icon on touch without overlays, ripples, or artificial background geometry.
 */
@Composable
fun NavigateBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Navigate back",
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.35f else 1.0f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMedium),
        label = "navigateBackAlpha"
    )

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .size(40.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Icon(
            imageVector = PhosphorIcons.Light.ArrowLeft,
            contentDescription = contentDescription,
            tint = DetailTextPrimary.copy(alpha = alpha),
            modifier = Modifier.size(22.dp)
        )
    }
}

/**
 * Shared structural layout for detail screens in minimalist OLED black styling.
 */
@Composable
fun DetailPageScaffold(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    titleTrailingContent: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier.detailPageContainer()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 20.dp, top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigateBackButton(onClick = onBackClick)

            if (trailingContent != null) {
                trailingContent()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        letterSpacing = (-0.5).sp,
                        color = DetailTextPrimary
                    )
                )
                if (titleTrailingContent != null) {
                    titleTrailingContent()
                }
            }
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = DetailTextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}
