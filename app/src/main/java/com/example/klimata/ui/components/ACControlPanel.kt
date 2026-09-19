package com.example.klimata.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klimata.data.ACProfile
import com.example.klimata.data.DispatchState
import com.example.klimata.data.MockData
import com.example.klimata.ui.theme.EcoGreen
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.KlimataTheme
import com.example.klimata.ui.theme.LocalDiurnalColors

/**
 * Tactical AC hardware control card.
 * Provides target setpoint stepping, mode telemetry, and compressor power toggling.
 */
@Composable
fun ACControlPanel(
    modifier: Modifier = Modifier,
    profile: ACProfile = MockData.acProfile,
    dispatch: DispatchState = MockData.dispatch,
    onPowerToggle: (Boolean) -> Unit = {},
    onTempChange: (Int) -> Unit = {},
) {
    val diurnal = LocalDiurnalColors.current
    var isPowerOn by remember { mutableStateOf(value = true) }
    var setpoint by remember(profile.currentSetpoint) { mutableIntStateOf(profile.currentSetpoint) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.85f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .clip(RoundedCornerShape(24.dp))
            .background(diurnal.frostedCardBackground)
            .padding(18.dp),
    ) {
        Crossfade(
            targetState = profile to dispatch,
            animationSpec = tween(220),
            label = "ACControlCrossfade"
        ) { (curProfile, curDispatch) ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Current Setpoint",
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${setpoint}°C",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 34.sp,
                                    color = Color.White,
                                    letterSpacing = (-0.5).sp
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Icon(
                                    imageVector = safeLeafIcon(),
                                    contentDescription = null,
                                    tint = com.example.klimata.ui.theme.MineralMintActive,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Eco Flow",
                                    style = TextStyle(
                                        fontFamily = JakartaFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.90f)
                                    )
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Setpoint bounds [18°C, 30°C] match residential split-unit IR firmware limits
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (setpoint > 18) {
                                        setpoint--
                                        onTempChange(setpoint)
                                    }
                                }
                        ) {
                            Icon(
                                imageVector = safeMinusIcon(),
                                contentDescription = "Decrease Temperature",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (setpoint < 30) {
                                        setpoint++
                                        onTempChange(setpoint)
                                    }
                                }
                        ) {
                            Icon(
                                imageVector = safePlusIcon(),
                                contentDescription = "Increase Temperature",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        val switchTrackColor by animateColorAsState(
                            targetValue = if (isPowerOn) com.example.klimata.ui.theme.MineralMintActive else Color.White.copy(alpha = 0.14f),
                            animationSpec = tween(220),
                            label = "switchTrackColor"
                        )
                        val switchThumbOffset by animateDpAsState(
                            targetValue = if (isPowerOn) 22.dp else 2.dp,
                            animationSpec = tween(220),
                            label = "switchThumbOffset"
                        )

                        Box(
                            modifier = Modifier
                                .size(width = 50.dp, height = 30.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .background(switchTrackColor)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    isPowerOn = !isPowerOn
                                    onPowerToggle(isPowerOn)
                                }
                                .padding(2.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                .offset(x = switchThumbOffset)
                                .size(24.dp)
                                .shadow(2.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color.White)
                            )
                        }
                    }
                }

                val dispatchMethod = curDispatch.dispatchMethod.ifEmpty { "IR Blaster" }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = safeBroadcastIcon(),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.50f),
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "$dispatchMethod • ${curProfile.brand} ${curProfile.model} • ${curProfile.capacity} • ${curProfile.inverterType}",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.5.sp,
                            color = Color.White.copy(alpha = 0.55f)
                        )
                    )
                }
            }
        }
    }
}

private val FallbackBroadcastIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Broadcast",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(12f, 13f)
            lineTo(12f, 13.01f)
            moveTo(8.5f, 9.5f)
            curveTo(6.5f, 11.5f, 6.5f, 14.5f, 8.5f, 16.5f)
            moveTo(15.5f, 9.5f)
            curveTo(17.5f, 11.5f, 17.5f, 14.5f, 15.5f, 16.5f)
            moveTo(5.5f, 6.5f)
            curveTo(2.5f, 9.5f, 2.5f, 16.5f, 5.5f, 19.5f)
            moveTo(18.5f, 6.5f)
            curveTo(21.5f, 9.5f, 21.5f, 16.5f, 18.5f, 19.5f)
        }
    }.build()
}

// Reflection lookup with zero-alloc vector fallbacks guarantees IDE preview rendering
// without throwing ClassNotFoundException when optional icon artifacts are unresolved.
private fun getPhosphorLightIcon(iconName: String, fallback: ImageVector): ImageVector {
    return try {
        val clazz = Class.forName("com.adamglin.phosphoricons.light.${iconName}Kt")
        val phosphorIconsClass = Class.forName("com.adamglin.PhosphorIcons")
        val lightField = phosphorIconsClass.getField("Light")
        val lightObj = lightField[null]
        val lightClass = Class.forName("com.adamglin.PhosphorIcons\$Light")
        val method = clazz.getMethod("get$iconName", lightClass)
        method.invoke(null, lightObj) as ImageVector
    } catch (_: Throwable) {
        fallback
    }
}

@Composable
private fun safeBroadcastIcon(): ImageVector {
    if (LocalInspectionMode.current) {
        return FallbackBroadcastIcon
    }
    return getPhosphorLightIcon("Broadcast", FallbackBroadcastIcon)
}

private val FallbackLeafIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Leaf",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(2f, 22f); lineTo(12f, 12f)
            moveTo(20f, 4f)
            curveTo(12f, 4f, 4f, 12f, 4f, 20f)
            curveTo(12f, 20f, 20f, 12f, 20f, 4f)
            close()
        }
    }.build()
}

@Composable
private fun safeLeafIcon(): ImageVector {
    if (LocalInspectionMode.current) {
        return FallbackLeafIcon
    }
    return getPhosphorLightIcon("Leaf", FallbackLeafIcon)
}

// Explicit vector paths bypass Compose Text font ascent/descent leading boxes,
// guaranteeing mathematical optical centering inside circular 32dp stepper targets.
private val FallbackMinusIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Minus",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2.4f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(6f, 12f)
            lineTo(18f, 12f)
        }
    }.build()
}

@Composable
private fun safeMinusIcon(): ImageVector {
    if (LocalInspectionMode.current) {
        return FallbackMinusIcon
    }
    return getPhosphorLightIcon("Minus", FallbackMinusIcon)
}

private val FallbackPlusIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Plus",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2.4f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(12f, 6f)
            lineTo(12f, 18f)
            moveTo(6f, 12f)
            lineTo(18f, 12f)
        }
    }.build()
}

@Composable
private fun safePlusIcon(): ImageVector {
    if (LocalInspectionMode.current) {
        return FallbackPlusIcon
    }
    return getPhosphorLightIcon("Plus", FallbackPlusIcon)
}

@Preview(showBackground = true, backgroundColor = 0xFF1976D2)
@Composable
private fun ACControlPanelPreview() {
    KlimataTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ACControlPanel()
        }
    }
}
