package com.example.klimata.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.CaretLeft
import com.example.klimata.ui.theme.DetailBlackBackground
import com.example.klimata.ui.theme.DetailCardSurface
import com.example.klimata.ui.theme.DetailTextPrimary
import com.example.klimata.ui.theme.DetailTextSecondary
import com.example.klimata.ui.theme.JakartaFamily

val DetailPageShape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp)

fun Modifier.detailPageContainer(
    backgroundColor: Color = DetailBlackBackground,
    shape: Shape = DetailPageShape,
): Modifier = this
    .fillMaxSize()
    .clip(shape)
    .background(backgroundColor)
    .statusBarsPadding()
    .navigationBarsPadding()

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
        // Minimalist top action row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 20.dp, top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .size(40.dp)
                    .bouncyClickable(
                        shape = CircleShape,
                        onClick = onBackClick
                    )
            ) {
                Icon(
                    imageVector = PhosphorIcons.Light.CaretLeft,
                    contentDescription = "Navigate back",
                    tint = DetailTextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            if (trailingContent != null) {
                trailingContent()
            }
        }

        // Title and context header
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
