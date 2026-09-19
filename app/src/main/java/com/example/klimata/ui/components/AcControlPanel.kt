package com.example.klimata.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import com.adamglin.phosphoricons.light.CaretRight
import com.adamglin.phosphoricons.light.Drop
import com.adamglin.phosphoricons.light.Fan
import com.adamglin.phosphoricons.light.Leaf
import com.adamglin.phosphoricons.light.Minus
import com.adamglin.phosphoricons.light.Plus
import com.adamglin.phosphoricons.light.Power
import com.adamglin.phosphoricons.light.Snowflake
import com.adamglin.phosphoricons.light.Wind
import com.example.klimata.data.AcProfile
import com.example.klimata.data.DispatchState
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.KlimataTheme
import com.example.klimata.ui.theme.LocalDiurnalColors
import com.example.klimata.ui.theme.MineralMintActive
import com.example.klimata.ui.theme.PowerGreenActive
import java.util.Locale

private val FanBarHeights = listOf(5.dp, 8.dp, 11.dp, 14.dp, 17.dp)
private val FanSpeeds = listOf("Auto", "Low", "Med", "High", "Turbo")

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
        else -> 2
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        FanBarHeights.forEachIndexed { index, height ->
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

@Composable
fun AcControlPanel(
    profile: AcProfile,
    dispatch: DispatchState,
    initialPowerOn: Boolean = true,
    initialEcoEnabled: Boolean = true,
    modifier: Modifier = Modifier,
    onPowerToggle: (Boolean) -> Unit = {},
    onEcoToggle: (Boolean) -> Unit = {},
    onTempChange: (Int) -> Unit = {},
    onModeChange: (String) -> Unit = {},
    onFanSpeedChange: (String) -> Unit = {},
    onSwingToggle: (Boolean) -> Unit = {},
    onCalibrateRemote: () -> Unit = {},
    onDetailsClick: () -> Unit = {},
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
            onCalibrateRemote = onCalibrateRemote,
            onDetailsClick = onDetailsClick,
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

@Composable
fun ACTelemetryCard(
    profile: AcProfile,
    dispatch: DispatchState,
    setpoint: Int,
    isPowerOn: Boolean,
    currentMode: String,
    isEcoEnabled: Boolean,
    fanSpeed: String = "Auto",
    isSwing: Boolean = false,
    onCalibrateRemote: () -> Unit = {},
    onDetailsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val diurnal = LocalDiurnalColors.current

    Box(
        modifier = modifier
            .bouncyClickable(
                shape = RoundedCornerShape(24.dp),
                onClick = onDetailsClick
            )
            .clip(RoundedCornerShape(24.dp))
            .background(diurnal.frostedCardBackground)
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentMode == "Fan") "Air Circulation" else "Current Setpoint",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.60f)
                        )
                    )

                    Icon(
                        imageVector = PhosphorIcons.Light.CaretRight,
                        contentDescription = "AC Unit Details",
                        tint = Color.White.copy(alpha = 0.35f),
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Crossfade(
                    targetState = Triple(setpoint, isPowerOn, currentMode),
                    animationSpec = tween(180),
                    label = "SetpointCrossfade"
                ) { (currentTemp, powerState, mode) ->
                    if (mode == "Fan") {
                        Row(
                            verticalAlignment = Alignment.Bottom,
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

                Spacer(modifier = Modifier.height(5.dp))

                Crossfade(
                    targetState = Pair(isSwing, isPowerOn),
                    animationSpec = tween(180),
                    label = "SwingCrossfade"
                ) { (swingActive, powerState) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Light.Wind,
                            contentDescription = null,
                            tint = if (powerState && swingActive) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.35f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (powerState && swingActive) "Swing On" else "Swing Fixed",
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

                Spacer(modifier = Modifier.height(5.dp))

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
            }

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
                        text = dispatch.dispatchMethod.ifEmpty { "IR Blaster" },
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.5.sp,
                            color = Color.White.copy(alpha = 0.50f)
                        )
                    )
                }
            }
        }
    }
}

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
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .bouncyClickable(
                            shape = RoundedCornerShape(14.dp),
                            onClick = { onPowerToggle(!isPowerOn) }
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(powerButtonColor)
                        .padding(horizontal = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Light.Power,
                            contentDescription = "Toggle AC Power",
                            tint = powerIconColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
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
                        .bouncyClickable(
                            enabled = isPowerOn,
                            shape = RoundedCornerShape(14.dp),
                            onClick = onModeCycle
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(modeBtnBgColor)
                        .padding(horizontal = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
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
                        Spacer(modifier = Modifier.width(4.dp))
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
                        .bouncyClickable(
                            enabled = isPowerOn,
                            shape = RoundedCornerShape(14.dp),
                            onClick = { onSwingToggle(!isSwingEnabled) }
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(swingButtonColor)
                        .padding(horizontal = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Light.Wind,
                            contentDescription = "Toggle Auto Swing",
                            tint = swingIconColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
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
                        .bouncyClickable(
                            enabled = isPowerOn,
                            shape = RoundedCornerShape(14.dp),
                            onClick = { onEcoToggle(!isEcoEnabled) }
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(ecoButtonColor)
                        .padding(horizontal = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Light.Leaf,
                            contentDescription = "Toggle Eco Mode",
                            tint = ecoIconColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Eco",
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
            val isFanMode = currentMode == "Fan"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .bouncyClickable(
                        enabled = isPowerOn && !isFanMode,
                        shape = RoundedCornerShape(14.dp),
                        onClick = {
                            val nextSpeed = cycleFanSpeed(fanSpeed)
                            onFanSpeedChange(nextSpeed)
                        }
                    )
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.06f))
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
                            if (isFanMode) Color(0xFFFBBF24) else Color(0xFF60A5FA)
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
                    activeColor = if (isFanMode) Color(0xFFFBBF24) else Color(0xFF60A5FA)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mode-Adaptive Rocker Keys
            // Cool/Dry/Auto: adjusts temperature (°C)
            // Fan mode: adjusts fan speed step (Auto -> Low -> Med -> High -> Turbo)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .heightIn(min = 60.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isFanMode) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .bouncyClickable(
                                enabled = isPowerOn,
                                shape = RoundedCornerShape(14.dp),
                                onClick = {
                                    val prevSpeed = stepFanSpeed(fanSpeed, stepUp = false)
                                    onFanSpeedChange(prevSpeed)
                                }
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .background(stepperBgColor)
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

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .bouncyClickable(
                                enabled = isPowerOn,
                                shape = RoundedCornerShape(14.dp),
                                onClick = {
                                    val nextSpeed = stepFanSpeed(fanSpeed, stepUp = true)
                                    onFanSpeedChange(nextSpeed)
                                }
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .background(stepperBgColor)
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
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .bouncyClickable(
                                enabled = isPowerOn,
                                shape = RoundedCornerShape(14.dp),
                                onClick = {
                                    if (setpoint > 16) {
                                        onTempChange(setpoint - 1)
                                    }
                                }
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .background(stepperBgColor)
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

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .bouncyClickable(
                                enabled = isPowerOn,
                                shape = RoundedCornerShape(14.dp),
                                onClick = {
                                    if (setpoint < 30) {
                                        onTempChange(setpoint + 1)
                                    }
                                }
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .background(stepperBgColor)
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
    val idx = FanSpeeds.indexOfFirst { it.equals(current, ignoreCase = true) }
    return if (idx == -1 || idx >= FanSpeeds.lastIndex) FanSpeeds[0] else FanSpeeds[idx + 1]
}

private fun stepFanSpeed(current: String, stepUp: Boolean): String {
    val idx = FanSpeeds.indexOfFirst { it.equals(current, ignoreCase = true) }.coerceAtLeast(0)
    val nextIdx = if (stepUp) {
        (idx + 1).coerceAtMost(FanSpeeds.lastIndex)
    } else {
        (idx - 1).coerceAtLeast(0)
    }
    return FanSpeeds[nextIdx]
}

@Composable
fun ACControlPanel(
    profile: AcProfile,
    dispatch: DispatchState,
    initialPowerOn: Boolean = true,
    initialEcoEnabled: Boolean = true,
    modifier: Modifier = Modifier,
    onPowerToggle: (Boolean) -> Unit = {},
    onEcoToggle: (Boolean) -> Unit = {},
    onTempChange: (Int) -> Unit = {},
    onModeChange: (String) -> Unit = {},
    onFanSpeedChange: (String) -> Unit = {},
    onSwingToggle: (Boolean) -> Unit = {},
    onCalibrateRemote: () -> Unit = {},
    onDetailsClick: () -> Unit = {},
) {
    AcControlPanel(
        profile = profile,
        dispatch = dispatch,
        initialPowerOn = initialPowerOn,
        initialEcoEnabled = initialEcoEnabled,
        modifier = modifier,
        onPowerToggle = onPowerToggle,
        onEcoToggle = onEcoToggle,
        onTempChange = onTempChange,
        onModeChange = onModeChange,
        onFanSpeedChange = onFanSpeedChange,
        onSwingToggle = onSwingToggle,
        onCalibrateRemote = onCalibrateRemote,
        onDetailsClick = onDetailsClick
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1976D2)
@Composable
private fun AcControlPanelPreview() {
    KlimataTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            AcControlPanel(
                profile = AcProfile(
                    roomName = "Master Bed",
                    brand = "Daikin",
                    model = "FTKF25",
                    capacity = "1.0 PK",
                    inverterType = "Eco Inverter",
                    currentSetpoint = 24,
                    mode = "Cool"
                ),
                dispatch = DispatchState(
                    isAutonomous = true,
                    statusLabel = "Autonomous Autopilot",
                    dispatchMethod = "IR Blaster"
                )
            )
        }
    }
}
