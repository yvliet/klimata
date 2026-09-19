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
import com.adamglin.phosphoricons.light.Wind
import com.example.klimata.data.ACProfile
import com.example.klimata.data.DispatchState
import com.example.klimata.data.MockData
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.KlimataTheme
import com.example.klimata.ui.theme.LocalDiurnalColors
import com.example.klimata.ui.theme.MineralMintActive
import com.example.klimata.ui.theme.PowerGreenActive
import java.util.Locale

/**
 * Authentic multi-step vertical fan speed bar indicator.
 */
@Composable
fun FanBarsIndicator(
    fanSpeed: String,
    isPowerOn: Boolean,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF60A5FA),
) {
    val level = when (fanSpeed.lowercase(Locale.ROOT)) {
        "quiet", "low" -> 1
        "med", "medium" -> 3
        "high" -> 4
        "turbo", "max" -> 5
        else -> 2 // Auto
    }

    val barHeights = listOf(5.dp, 8.dp, 11.dp, 14.dp, 17.dp)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        barHeights.forEachIndexed { index, height ->
            val isLit = isPowerOn && (index < level)
            val barColor = when {
                !isPowerOn -> Color.White.copy(alpha = 0.12f)
                isLit -> activeColor
                else -> Color.White.copy(alpha = 0.18f)
            }
            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(height)
                    .clip(RoundedCornerShape(1.dp))
                    .background(barColor)
            )
        }
    }
}

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
    onModeChange: (String) -> Unit = {},
    onFanSpeedChange: (String) -> Unit = {},
    onSwingToggle: (Boolean) -> Unit = {},
    onCalibrateRemote: () -> Unit = {},
) {
    var isPowerOn by remember(initialPowerOn) { mutableStateOf(initialPowerOn) }
    var setpoint by remember(profile.currentSetpoint) { mutableIntStateOf(profile.currentSetpoint) }
    var activeMode by remember(profile.mode) { mutableStateOf(profile.mode) }
    var isEcoEnabled by remember(initialEcoEnabled) { mutableStateOf(initialEcoEnabled) }
    var activeFanSpeed by remember(profile.fanSpeed) { mutableStateOf(profile.fanSpeed) }
    var isSwingEnabled by remember(profile.swing) { mutableStateOf(profile.swing) }

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
            fanSpeed = activeFanSpeed,
            isSwing = isSwingEnabled,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )

        ACDigitalRemoteCard(
            isPowerOn = isPowerOn,
            setpoint = setpoint,
            currentMode = activeMode,
            isEcoEnabled = isEcoEnabled,
            fanSpeed = activeFanSpeed,
            isSwingEnabled = isSwingEnabled,
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
                onModeChange(activeMode)
            },
            onFanSpeedChange = {
                activeFanSpeed = it
                onFanSpeedChange(it)
            },
            onSwingToggle = {
                isSwingEnabled = it
                onSwingToggle(it)
            },
            onCalibrateRemote = onCalibrateRemote,
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
    fanSpeed: String = "Auto",
    isSwing: Boolean = false,
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
                    text = if (currentMode == "Fan") "Air Circulation" else "Current Setpoint",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.60f)
                    )
                )

                Spacer(modifier = Modifier.height(3.dp))

                Crossfade(
                    targetState = Triple(setpoint, isPowerOn, currentMode),
                    animationSpec = tween(180),
                    label = "SetpointCrossfade"
                ) { (currentTemp, powerState, mode) ->
                    if (mode == "Fan") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (powerState) fanSpeed else "--",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    letterSpacing = (-0.5).sp,
                                    color = if (powerState) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.40f)
                                )
                            )
                            FanBarsIndicator(
                                fanSpeed = fanSpeed,
                                isPowerOn = powerState,
                                activeColor = Color(0xFFFBBF24),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    } else {
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

                    val modeLabel = when {
                        !powerState -> "Standby"
                        mode == "Fan" -> "Fan (Compressor Off)"
                        mode == "Cool" -> "Cool Mode"
                        mode == "Dry" -> "Dry Dehumidify"
                        mode == "Auto" -> "Auto Climate"
                        else -> mode
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
                            text = modeLabel,
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

                // Status Row (Eco & Auto Swing)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Crossfade(
                        targetState = Pair(isEcoEnabled, isPowerOn),
                        animationSpec = tween(180),
                        label = "EcoCrossfade"
                    ) { (isEco, powerState) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
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
                                    fontSize = 11.sp,
                                    color = if (powerState && isEco) {
                                        Color.White.copy(alpha = 0.90f)
                                    } else {
                                        Color.White.copy(alpha = 0.45f)
                                    }
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(8.dp)
                            .background(Color.White.copy(alpha = 0.20f))
                    )

                    Crossfade(
                        targetState = Pair(isSwing, isPowerOn),
                        animationSpec = tween(180),
                        label = "SwingCrossfade"
                    ) { (swingActive, powerState) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Light.Wind,
                                contentDescription = null,
                                tint = if (powerState && swingActive) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.35f),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (powerState && swingActive) "Swing On" else "Swing Off",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = if (powerState && swingActive) {
                                        Color.White.copy(alpha = 0.90f)
                                    } else {
                                        Color.White.copy(alpha = 0.45f)
                                    }
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Signal Protocol Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Light.Broadcast,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.50f),
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = profile.irCodeSet?.replace('_', ' ')?.uppercase(Locale.ROOT) ?: "AUTO PROTOCOL",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 9.sp,
                            letterSpacing = 0.3.sp,
                            color = Color.White.copy(alpha = 0.60f)
                        ),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = profile.capacity,
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.5.sp,
                            color = Color.White.copy(alpha = 0.60f)
                        )
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(9.dp)
                            .background(Color.White.copy(alpha = 0.20f))
                    )
                    Text(
                        text = profile.inverterType,
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.5.sp,
                            color = Color.White.copy(alpha = 0.50f)
                        )
                    )
                }
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
    fanSpeed: String,
    isSwingEnabled: Boolean,
    onPowerToggle: (Boolean) -> Unit,
    onEcoToggle: (Boolean) -> Unit,
    onTempChange: (Int) -> Unit,
    onModeCycle: () -> Unit,
    onFanSpeedChange: (String) -> Unit,
    onSwingToggle: (Boolean) -> Unit,
    onCalibrateRemote: () -> Unit = {},
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

    val swingButtonColor by animateColorAsState(
        targetValue = when {
            !isPowerOn -> Color.White.copy(alpha = 0.04f)
            isSwingEnabled -> Color(0xFF38BDF8)
            else -> Color.White.copy(alpha = 0.08f)
        },
        animationSpec = tween(200),
        label = "RemoteSwingColor"
    )
    val swingIconColor by animateColorAsState(
        targetValue = when {
            !isPowerOn -> Color.White.copy(alpha = 0.25f)
            isSwingEnabled -> Color(0xFF0F172A)
            else -> Color.White.copy(alpha = 0.70f)
        },
        animationSpec = tween(200),
        label = "RemoteSwingIconColor"
    )

    val rawModeColor = when (currentMode) {
        "Cool" -> Color(0xFF60A5FA)
        "Dry" -> Color(0xFF38BDF8)
        "Fan" -> Color(0xFFFBBF24)
        "Auto" -> Color(0xFFA78BFA)
        else -> Color(0xFF60A5FA)
    }

    val modeBtnBgColor by animateColorAsState(
        targetValue = if (isPowerOn) rawModeColor else Color.White.copy(alpha = 0.04f),
        animationSpec = tween(200),
        label = "RemoteModeBtnBgColor"
    )

    val modeIconColor by animateColorAsState(
        targetValue = if (isPowerOn) Color(0xFF0F172A) else Color.White.copy(alpha = 0.25f),
        animationSpec = tween(200),
        label = "RemoteModeIconColor"
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
            // Header with Calibrate Signal trigger and Power dot
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick Remote Signal Calibration shortcut
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.07f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onCalibrateRemote()
                            }
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Light.Broadcast,
                            contentDescription = "Calibrate Remote Signal",
                            tint = Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.size(11.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(powerIndicatorDotColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Control Row (Power & Mode)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Power Pill
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(powerButtonColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onPowerToggle(!isPowerOn)
                        }
                        .padding(horizontal = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Light.Power,
                            contentDescription = "Toggle AC Power",
                            tint = powerIconColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isPowerOn) "ON" else "OFF",
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = powerIconColor
                            ),
                            maxLines = 1
                        )
                    }
                }

                // Mode Cycle Pill
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(modeBtnBgColor)
                        .clickable(
                            enabled = isPowerOn,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onModeCycle()
                        }
                        .padding(horizontal = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Crossfade(
                            targetState = currentMode,
                            animationSpec = tween(180),
                            label = "RemoteModeIconCrossfade"
                        ) { mode ->
                            val modeIcon = when (mode) {
                                "Cool" -> PhosphorIcons.Light.Snowflake
                                "Dry" -> PhosphorIcons.Light.Drop
                                "Fan" -> PhosphorIcons.Light.Fan
                                "Auto" -> PhosphorIcons.Light.ArrowsClockwise
                                else -> PhosphorIcons.Light.Snowflake
                            }

                            Icon(
                                imageVector = modeIcon,
                                contentDescription = "Cycle AC Mode (Current: $mode)",
                                tint = modeIconColor,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Text(
                            text = currentMode,
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = modeIconColor
                            ),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Comfort & Efficiency Row (Auto Swing & Eco Leaf)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Auto Swing Louvre Pill
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(swingButtonColor)
                        .clickable(
                            enabled = isPowerOn,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onSwingToggle(!isSwingEnabled)
                        }
                        .padding(horizontal = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Light.Wind,
                            contentDescription = "Toggle Auto Swing",
                            tint = swingIconColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (isSwingEnabled) "Swing" else "Fixed",
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = swingIconColor
                            ),
                            maxLines = 1
                        )
                    }
                }

                // Eco Leaf Pill
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(ecoButtonColor)
                        .clickable(
                            enabled = isPowerOn,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onEcoToggle(!isEcoEnabled)
                        }
                        .padding(horizontal = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Light.Leaf,
                            contentDescription = "Toggle Eco Mode",
                            tint = ecoIconColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (isEcoEnabled) "Eco On" else "Eco",
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = ecoIconColor
                            ),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stepped Fan Speed Meter & Selector Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .clickable(
                        enabled = isPowerOn,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        val nextSpeed = cycleFanSpeed(fanSpeed)
                        onFanSpeedChange(nextSpeed)
                    }
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Light.Fan,
                        contentDescription = "Fan Speed",
                        tint = if (isPowerOn) {
                            if (currentMode == "Fan") Color(0xFFFBBF24) else Color(0xFF60A5FA)
                        } else {
                            Color.White.copy(alpha = 0.30f)
                        },
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Fan: $fanSpeed",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = if (isPowerOn) Color.White.copy(alpha = 0.90f) else Color.White.copy(alpha = 0.40f)
                        )
                    )
                }

                FanBarsIndicator(
                    fanSpeed = fanSpeed,
                    isPowerOn = isPowerOn,
                    activeColor = if (currentMode == "Fan") Color(0xFFFBBF24) else Color(0xFF60A5FA)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mode-Adaptive Rocker Keys
            // In Cool/Dry/Auto: adjusts temperature (°C)
            // In Fan mode: compressor disengaged -> adjusts Fan Speed step (Auto -> Low -> Med -> High -> Turbo)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .heightIn(min = 60.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentMode == "Fan") {
                    // Fan Mode: Step Fan Down
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
                                val prevSpeed = stepFanSpeed(fanSpeed, stepUp = false)
                                onFanSpeedChange(prevSpeed)
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Light.Minus,
                                contentDescription = "Step Fan Down",
                                tint = stepperIconColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "SPEED",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 9.sp,
                                    color = stepperIconColor.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }

                    // Fan Mode: Step Fan Up
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
                                val nextSpeed = stepFanSpeed(fanSpeed, stepUp = true)
                                onFanSpeedChange(nextSpeed)
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "SPEED",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 9.sp,
                                    color = stepperIconColor.copy(alpha = 0.8f)
                                )
                            )
                            Icon(
                                imageVector = PhosphorIcons.Light.Plus,
                                contentDescription = "Step Fan Up",
                                tint = stepperIconColor,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                } else {
                    // Cooling / Dry / Auto Mode: Step Temperature Down
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
                                if (setpoint > 16) {
                                    onTempChange(setpoint - 1)
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Light.Minus,
                                contentDescription = "Decrease Temperature",
                                tint = stepperIconColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "TEMP",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 9.sp,
                                    color = stepperIconColor.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }

                    // Cooling / Dry / Auto Mode: Step Temperature Up
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "TEMP",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 9.sp,
                                    color = stepperIconColor.copy(alpha = 0.8f)
                                )
                            )
                            Icon(
                                imageVector = PhosphorIcons.Light.Plus,
                                contentDescription = "Increase Temperature",
                                tint = stepperIconColor,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun cycleFanSpeed(current: String): String {
    val speeds = listOf("Auto", "Low", "Med", "High", "Turbo")
    val idx = speeds.indexOfFirst { it.equals(current, ignoreCase = true) }
    return if (idx == -1 || idx >= speeds.lastIndex) speeds[0] else speeds[idx + 1]
}

private fun stepFanSpeed(current: String, stepUp: Boolean): String {
    val speeds = listOf("Auto", "Low", "Med", "High", "Turbo")
    val idx = speeds.indexOfFirst { it.equals(current, ignoreCase = true) }.coerceAtLeast(0)
    val nextIdx = if (stepUp) {
        (idx + 1).coerceAtMost(speeds.lastIndex)
    } else {
        (idx - 1).coerceAtLeast(0)
    }
    return speeds[nextIdx]
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
