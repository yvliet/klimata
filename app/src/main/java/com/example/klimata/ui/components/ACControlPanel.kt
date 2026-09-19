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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.ArrowsClockwise
import com.adamglin.phosphoricons.light.Broadcast
import com.adamglin.phosphoricons.light.Drop
import com.adamglin.phosphoricons.light.Fan
import com.adamglin.phosphoricons.light.Leaf
import com.adamglin.phosphoricons.light.Minus
import com.adamglin.phosphoricons.light.Plus
import com.adamglin.phosphoricons.light.Power
import com.adamglin.phosphoricons.light.Snowflake
import com.example.klimata.data.ACProfile
import com.example.klimata.data.DispatchState
import com.example.klimata.data.MockData
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.KlimataTheme
import com.example.klimata.ui.theme.LocalDiurnalColors
import com.example.klimata.ui.theme.MineralMintActive
import com.example.klimata.ui.theme.PowerGreenActive

/**
 * 2-column Bento AC control row:
 * - Left card (ACTelemetryCard): Displays setpoint, hardware AC operational mode, Klimata Eco status, and hardware profile.
 * - Right card (ACDigitalRemoteCard): Tactile digital remote with power key, Eco leaf toggle, temp steppers, and hardware mode selector.
 */
@Composable
fun ACControlPanel(
    modifier: Modifier = Modifier,
    profile: ACProfile = MockData.acProfile,
    dispatch: DispatchState = MockData.dispatch,
    initialPowerOn: Boolean = true,
    initialEcoEnabled: Boolean = true,
    onPowerToggle: (Boolean) -> Unit = {},
    onEcoToggle: (Boolean) -> Unit = {},
    onTempChange: (Int) -> Unit = {},
) {
    var isPowerOn by remember(initialPowerOn) { mutableStateOf(initialPowerOn) }
    var setpoint by remember(profile.currentSetpoint) { mutableIntStateOf(profile.currentSetpoint) }
    var activeMode by remember(profile.mode) { mutableStateOf(profile.mode) }
    var isEcoEnabled by remember(initialEcoEnabled) { mutableStateOf(initialEcoEnabled) }

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
            isEcoEnabled = isEcoEnabled,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )

        ACDigitalRemoteCard(
            isPowerOn = isPowerOn,
            setpoint = setpoint,
            currentMode = activeMode,
            isEcoEnabled = isEcoEnabled,
            onPowerToggle = {
                isPowerOn = it
                onPowerToggle(it)
            },
            onEcoToggle = {
                isEcoEnabled = it
                onEcoToggle(it)
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
    isEcoEnabled: Boolean,
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
                        "Cool" -> PhosphorIcons.Light.Snowflake to Color(0xFF60A5FA)
                        "Dry" -> PhosphorIcons.Light.Drop to Color(0xFF38BDF8)
                        "Fan" -> PhosphorIcons.Light.Fan to Color(0xFFFBBF24)
                        "Auto" -> PhosphorIcons.Light.ArrowsClockwise to Color(0xFFA78BFA)
                        else -> PhosphorIcons.Light.Snowflake to Color(0xFF60A5FA)
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
                    targetState = Pair(isEcoEnabled, isPowerOn),
                    animationSpec = tween(180),
                    label = "EcoCrossfade"
                ) { (isEco, powerState) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Light.Leaf,
                            contentDescription = null,
                            tint = if (powerState && isEco) MineralMintActive else Color.White.copy(alpha = 0.35f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (!powerState) {
                                "Eco Standby"
                            } else if (isEco) {
                                "Eco Active"
                            } else {
                                "Eco Off"
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
                        imageVector = PhosphorIcons.Light.Broadcast,
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
    isEcoEnabled: Boolean,
    onPowerToggle: (Boolean) -> Unit,
    onEcoToggle: (Boolean) -> Unit,
    onTempChange: (Int) -> Unit,
    onModeCycle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val diurnal = LocalDiurnalColors.current

    val powerButtonColor by animateColorAsState(
        targetValue = if (isPowerOn) PowerGreenActive else Color.White.copy(alpha = 0.08f),
        animationSpec = tween(200),
        label = "RemotePowerColor"
    )
    val powerIconColor by animateColorAsState(
        targetValue = if (isPowerOn) Color(0xFF0F172A) else Color.White.copy(alpha = 0.70f),
        animationSpec = tween(200),
        label = "RemotePowerIconColor"
    )

    val ecoButtonColor by animateColorAsState(
        targetValue = when {
            !isPowerOn -> Color.White.copy(alpha = 0.04f)
            isEcoEnabled -> MineralMintActive
            else -> Color.White.copy(alpha = 0.08f)
        },
        animationSpec = tween(200),
        label = "RemoteEcoColor"
    )
    val ecoIconColor by animateColorAsState(
        targetValue = when {
            !isPowerOn -> Color.White.copy(alpha = 0.25f)
            isEcoEnabled -> Color(0xFF0F172A)
            else -> Color.White.copy(alpha = 0.70f)
        },
        animationSpec = tween(200),
        label = "RemoteEcoIconColor"
    )

    val stepperBgColor by animateColorAsState(
        targetValue = if (isPowerOn) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.04f),
        animationSpec = tween(200),
        label = "RemoteStepperBgColor"
    )
    val stepperIconColor by animateColorAsState(
        targetValue = if (isPowerOn) Color.White else Color.White.copy(alpha = 0.25f),
        animationSpec = tween(200),
        label = "RemoteStepperIconColor"
    )

    val modeBtnBgColor by animateColorAsState(
        targetValue = if (isPowerOn) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.04f),
        animationSpec = tween(200),
        label = "RemoteModeBtnBgColor"
    )

    val powerIndicatorDotColor by animateColorAsState(
        targetValue = if (isPowerOn) PowerGreenActive else Color.White.copy(alpha = 0.20f),
        animationSpec = tween(200),
        label = "RemotePowerDot"
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
                        .background(powerIndicatorDotColor)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tactile Action Keys (Power, Eco Leaf, Mode)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
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
                        imageVector = PhosphorIcons.Light.Power,
                        contentDescription = "Toggle AC Power",
                        tint = powerIconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ecoButtonColor)
                        .clickable(
                            enabled = isPowerOn,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onEcoToggle(!isEcoEnabled)
                        }
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Light.Leaf,
                        contentDescription = "Toggle Eco Mode",
                        tint = ecoIconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(modeBtnBgColor)
                        .clickable(
                            enabled = isPowerOn,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onModeCycle()
                        }
                ) {
                    Crossfade(
                        targetState = Pair(currentMode, isPowerOn),
                        animationSpec = tween(180),
                        label = "RemoteModeIconCrossfade"
                    ) { (mode, powerState) ->
                        val (modeIcon, rawModeColor) = when (mode) {
                            "Cool" -> PhosphorIcons.Light.Snowflake to Color(0xFF60A5FA)
                            "Dry" -> PhosphorIcons.Light.Drop to Color(0xFF38BDF8)
                            "Fan" -> PhosphorIcons.Light.Fan to Color(0xFFFBBF24)
                            "Auto" -> PhosphorIcons.Light.ArrowsClockwise to Color(0xFFA78BFA)
                            else -> PhosphorIcons.Light.Snowflake to Color(0xFF60A5FA)
                        }
                        val modeColor = if (powerState) rawModeColor else Color.White.copy(alpha = 0.25f)

                        Icon(
                            imageVector = modeIcon,
                            contentDescription = "Cycle AC Mode (Current: $mode)",
                            tint = modeColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tactile Temperature Rocker Keys
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .heightIn(min = 72.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .background(stepperBgColor)
                        .clickable(
                            enabled = isPowerOn,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (setpoint > 18) {
                                onTempChange(setpoint - 1)
                            }
                        }
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Light.Minus,
                        contentDescription = "Decrease Temperature",
                        tint = stepperIconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .background(stepperBgColor)
                        .clickable(
                            enabled = isPowerOn,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (setpoint < 30) {
                                onTempChange(setpoint + 1)
                            }
                        }
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Light.Plus,
                        contentDescription = "Increase Temperature",
                        tint = stepperIconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
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
