package com.example.klimata.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.KlimataTheme
import com.example.klimata.ui.theme.LocalDiurnalColors
import com.example.klimata.ui.theme.MineralMintActive

/**
 * 2-column Bento AC control row:
 * - Left card (ACTelemetryCard): Displays setpoint, hardware AC operational mode, Klimata Eco Flow status, and hardware profile.
 * - Right card (ACDigitalRemoteCard): Tactile digital remote with power key, Eco Flow leaf toggle, temp steppers, and hardware mode selector.
 */
@Composable
fun ACControlPanel(
    modifier: Modifier = Modifier,
    profile: ACProfile = MockData.acProfile,
    dispatch: DispatchState = MockData.dispatch,
    initialEcoFlowEnabled: Boolean = true,
    onPowerToggle: (Boolean) -> Unit = {},
    onEcoFlowToggle: (Boolean) -> Unit = {},
    onTempChange: (Int) -> Unit = {},
) {
    var isPowerOn by remember { mutableStateOf(value = true) }
    var setpoint by remember(profile.currentSetpoint) { mutableIntStateOf(profile.currentSetpoint) }
    var activeMode by remember(profile.mode) { mutableStateOf(profile.mode) }
    var isEcoFlowEnabled by remember { mutableStateOf(initialEcoFlowEnabled) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ACTelemetryCard(
            profile = profile,
            dispatch = dispatch,
            setpoint = setpoint,
            isPowerOn = isPowerOn,
            currentMode = activeMode,
            isEcoFlowEnabled = isEcoFlowEnabled,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )

        ACDigitalRemoteCard(
            isPowerOn = isPowerOn,
            setpoint = setpoint,
            currentMode = activeMode,
            isEcoFlowEnabled = isEcoFlowEnabled,
            onPowerToggle = {
                isPowerOn = it
                onPowerToggle(it)
            },
            onEcoFlowToggle = {
                isEcoFlowEnabled = it
                onEcoFlowToggle(it)
            },
            onTempChange = {
                setpoint = it
                onTempChange(it)
            },
            onModeCycle = {
                activeMode = when (activeMode) {
                    "Cool" -> "Dry"
                    "Dry" -> "Fan"
                    "Fan" -> "Auto"
                    else -> "Cool"
                }
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
    }
}

/**
 * AC status and hardware specifications card.
 */
@Composable
fun ACTelemetryCard(
    profile: ACProfile,
    dispatch: DispatchState,
    setpoint: Int,
    isPowerOn: Boolean,
    currentMode: String,
    isEcoFlowEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val diurnal = LocalDiurnalColors.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(diurnal.frostedCardBackground)
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Current Setpoint",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.60f)
                    )
                )

                Spacer(modifier = Modifier.height(3.dp))

                Crossfade(
                    targetState = Pair(setpoint, isPowerOn),
                    animationSpec = tween(180),
                    label = "SetpointCrossfade"
                ) { (currentTemp, powerState) ->
                    Text(
                        text = "${currentTemp}°C",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            letterSpacing = (-0.5).sp,
                            color = if (powerState) Color.White else Color.White.copy(alpha = 0.40f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Crossfade(
                    targetState = Pair(currentMode, isPowerOn),
                    animationSpec = tween(180),
                    label = "ACModeCrossfade"
                ) { (mode, powerState) ->
                    val (modeIcon, modeColor) = when (mode) {
                        "Cool" -> safeSnowflakeIcon() to Color(0xFF60A5FA)
                        "Dry" -> safeWaterDropIcon() to Color(0xFF38BDF8)
                        "Fan" -> safeFanBladeIcon() to Color(0xFFFBBF24)
                        "Auto" -> safeAutoRefreshIcon() to Color(0xFFA78BFA)
                        else -> safeSnowflakeIcon() to Color(0xFF60A5FA)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = modeIcon,
                            contentDescription = null,
                            tint = if (powerState) modeColor else Color.White.copy(alpha = 0.40f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (powerState) mode else "Standby",
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.5.sp,
                                color = if (powerState) Color.White.copy(alpha = 0.90f) else Color.White.copy(alpha = 0.45f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Crossfade(
                    targetState = Pair(isEcoFlowEnabled, isPowerOn),
                    animationSpec = tween(180),
                    label = "EcoFlowCrossfade"
                ) { (isEco, powerState) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = safeLeafIcon(),
                            contentDescription = null,
                            tint = if (powerState && isEco) MineralMintActive else Color.White.copy(alpha = 0.35f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (!powerState) {
                                "Eco Flow Standby"
                            } else if (isEco) {
                                "Eco Flow Active"
                            } else {
                                "Eco Flow Off"
                            },
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.5.sp,
                                color = if (powerState && isEco) {
                                    Color.White.copy(alpha = 0.90f)
                                } else {
                                    Color.White.copy(alpha = 0.45f)
                                }
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "${profile.brand} ${profile.model}",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp,
                        color = Color.White.copy(alpha = 0.80f)
                    )
                )
                Text(
                    text = "${profile.capacity} • ${profile.inverterType}",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.5.sp,
                        color = Color.White.copy(alpha = 0.50f)
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = safeBroadcastIcon(),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.45f),
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = dispatch.dispatchMethod.ifEmpty { "IR Blaster" },
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.45f)
                        )
                    )
                }
            }
        }
    }
}

/**
 * Tactile digital remote control card simulating an authentic physical AC controller.
 */
@Composable
fun ACDigitalRemoteCard(
    isPowerOn: Boolean,
    setpoint: Int,
    currentMode: String,
    isEcoFlowEnabled: Boolean,
    onPowerToggle: (Boolean) -> Unit,
    onEcoFlowToggle: (Boolean) -> Unit,
    onTempChange: (Int) -> Unit,
    onModeCycle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val diurnal = LocalDiurnalColors.current

    val powerButtonColor by animateColorAsState(
        targetValue = if (isPowerOn) MineralMintActive else Color.White.copy(alpha = 0.08f),
        animationSpec = tween(200),
        label = "RemotePowerColor"
    )
    val powerIconColor by animateColorAsState(
        targetValue = if (isPowerOn) Color(0xFF0F172A) else Color.White.copy(alpha = 0.70f),
        animationSpec = tween(200),
        label = "RemotePowerIconColor"
    )

    val ecoButtonColor by animateColorAsState(
        targetValue = if (isPowerOn && isEcoFlowEnabled) MineralMintActive else Color.White.copy(alpha = 0.08f),
        animationSpec = tween(200),
        label = "RemoteEcoColor"
    )
    val ecoIconColor by animateColorAsState(
        targetValue = if (isPowerOn && isEcoFlowEnabled) Color(0xFF0F172A) else Color.White.copy(alpha = 0.70f),
        animationSpec = tween(200),
        label = "RemoteEcoIconColor"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(diurnal.frostedCardBackground)
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Remote Control",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.60f)
                    )
                )

                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (isPowerOn) MineralMintActive else Color.White.copy(alpha = 0.20f))
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tactile Symmetrical Action Keys (Power and Eco Flow Leaf)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(powerButtonColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onPowerToggle(!isPowerOn)
                            }
                    ) {
                        Icon(
                            imageVector = safePowerIcon(),
                            contentDescription = "Toggle AC Power",
                            tint = powerIconColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ecoButtonColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onEcoFlowToggle(!isEcoFlowEnabled)
                            }
                    ) {
                        Icon(
                            imageVector = safeLeafIcon(),
                            contentDescription = "Toggle Eco Flow Mode",
                            tint = ecoIconColor,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Crossfade(
                    targetState = isPowerOn,
                    animationSpec = tween(180),
                    label = "PowerStateCrossfade"
                ) { powerState ->
                    Text(
                        text = if (powerState) "ON" else "OFF",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (powerState) MineralMintActive else Color.White.copy(alpha = 0.40f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tactile Temperature Rocker Keys
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (setpoint > 18) {
                                onTempChange(setpoint - 1)
                            }
                        }
                ) {
                    Icon(
                        imageVector = safeMinusIcon(),
                        contentDescription = "Decrease Temperature",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (setpoint < 30) {
                                onTempChange(setpoint + 1)
                            }
                        }
                ) {
                    Icon(
                        imageVector = safePlusIcon(),
                        contentDescription = "Increase Temperature",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tactile Mode Cycle Key with Mode Icon and Crossfade
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onModeCycle()
                    }
            ) {
                Crossfade(
                    targetState = currentMode,
                    animationSpec = tween(180),
                    label = "RemoteModeCrossfade"
                ) { mode ->
                    val (modeIcon, modeColor) = when (mode) {
                        "Cool" -> safeSnowflakeIcon() to Color(0xFF60A5FA)
                        "Dry" -> safeWaterDropIcon() to Color(0xFF38BDF8)
                        "Fan" -> safeFanBladeIcon() to Color(0xFFFBBF24)
                        "Auto" -> safeAutoRefreshIcon() to Color(0xFFA78BFA)
                        else -> safeSnowflakeIcon() to Color(0xFF60A5FA)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "MODE",
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.60f),
                                letterSpacing = 0.4.sp
                            )
                        )
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.35f))
                        )
                        Icon(
                            imageVector = modeIcon,
                            contentDescription = null,
                            tint = modeColor,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = mode.uppercase(),
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.90f),
                                letterSpacing = 0.3.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

private val FallbackPowerIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Power",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2.2f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(12f, 3f); lineTo(12f, 11f)
            moveTo(6.34f, 6.34f)
            curveTo(3.7f, 8.98f, 3.7f, 13.26f, 6.34f, 15.9f)
            curveTo(8.98f, 18.54f, 13.26f, 18.54f, 15.9f, 15.9f)
            curveTo(18.54f, 13.26f, 18.54f, 8.98f, 15.9f, 6.34f)
        }
    }.build()
}

@Composable
private fun safePowerIcon(): ImageVector {
    if (LocalInspectionMode.current) {
        return FallbackPowerIcon
    }
    return getPhosphorLightIcon("Power", FallbackPowerIcon)
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

private val FallbackSnowflakeIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Snowflake",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(12f, 3f); lineTo(12f, 21f)
            moveTo(3f, 12f); lineTo(21f, 12f)
            moveTo(5.64f, 5.64f); lineTo(18.36f, 18.36f)
            moveTo(18.36f, 5.64f); lineTo(5.64f, 18.36f)
            moveTo(9.5f, 6f); lineTo(12f, 8.5f); lineTo(14.5f, 6f)
            moveTo(9.5f, 18f); lineTo(12f, 15.5f); lineTo(14.5f, 18f)
            moveTo(6f, 9.5f); lineTo(8.5f, 12f); lineTo(6f, 14.5f)
            moveTo(18f, 9.5f); lineTo(15.5f, 12f); lineTo(18f, 14.5f)
        }
    }.build()
}

@Composable
private fun safeSnowflakeIcon(): ImageVector {
    if (LocalInspectionMode.current) {
        return FallbackSnowflakeIcon
    }
    return getPhosphorLightIcon("Snowflake", FallbackSnowflakeIcon)
}

private val FallbackWaterDropIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Drop",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(12f, 3f)
            curveTo(12f, 3f, 6f, 10.5f, 6f, 15f)
            curveTo(6f, 18.3f, 8.7f, 21f, 12f, 21f)
            curveTo(15.3f, 21f, 18f, 18.3f, 18f, 15f)
            curveTo(18f, 10.5f, 12f, 3f, 12f, 3f)
            close()
        }
    }.build()
}

@Composable
private fun safeWaterDropIcon(): ImageVector {
    if (LocalInspectionMode.current) {
        return FallbackWaterDropIcon
    }
    return getPhosphorLightIcon("Drop", FallbackWaterDropIcon)
}

private val FallbackFanBladeIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Fan",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(12f, 12f)
            curveTo(12f, 7f, 16f, 4f, 18f, 6f)
            curveTo(19f, 8f, 16f, 12f, 12f, 12f)
            curveTo(7f, 12f, 4f, 16f, 6f, 18f)
            curveTo(8f, 19f, 12f, 16f, 12f, 12f)
            curveTo(12f, 17f, 8f, 20f, 6f, 18f)
        }
    }.build()
}

@Composable
private fun safeFanBladeIcon(): ImageVector {
    if (LocalInspectionMode.current) {
        return FallbackFanBladeIcon
    }
    return getPhosphorLightIcon("Fan", FallbackFanBladeIcon)
}

private val FallbackAutoRefreshIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "ArrowsClockwise",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(12f, 4f)
            curveTo(16.4f, 4f, 20f, 7.6f, 20f, 12f)
            curveTo(20f, 13.5f, 19.6f, 14.9f, 18.8f, 16f)
            moveTo(10f, 2f); lineTo(12f, 4f); lineTo(10f, 6f)
            moveTo(12f, 20f)
            curveTo(7.6f, 20f, 4f, 16.4f, 4f, 12f)
            curveTo(4f, 10.5f, 4.4f, 9.1f, 5.2f, 8f)
            moveTo(14f, 22f); lineTo(12f, 20f); lineTo(14f, 18f)
        }
    }.build()
}

@Composable
private fun safeAutoRefreshIcon(): ImageVector {
    if (LocalInspectionMode.current) {
        return FallbackAutoRefreshIcon
    }
    return getPhosphorLightIcon("ArrowsClockwise", FallbackAutoRefreshIcon)
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
