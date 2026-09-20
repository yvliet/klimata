package com.example.klimata.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.ArrowsClockwise
import com.adamglin.phosphoricons.light.Broadcast
import com.adamglin.phosphoricons.light.CaretDown
import com.adamglin.phosphoricons.light.CaretRight
import com.adamglin.phosphoricons.light.Check
import com.adamglin.phosphoricons.light.Eye
import com.adamglin.phosphoricons.light.EyeSlash
import com.adamglin.phosphoricons.light.MapPin
import com.adamglin.phosphoricons.light.Moon
import com.adamglin.phosphoricons.light.Pause
import com.adamglin.phosphoricons.light.Play
import com.adamglin.phosphoricons.light.Power
import com.adamglin.phosphoricons.light.Sparkle
import com.adamglin.phosphoricons.light.Sun
import com.adamglin.phosphoricons.light.SunHorizon
import com.example.klimata.data.EnergyConfig
import com.example.klimata.data.LocationHelper
import com.example.klimata.data.ir.IrBlasterService
import com.example.klimata.data.storage.KlimataPreferences
import com.example.klimata.ui.components.DetailPageScaffold
import com.example.klimata.ui.components.bouncyClickable
import com.example.klimata.ui.theme.DetailCardBorder
import com.example.klimata.ui.theme.DetailCardSurface
import com.example.klimata.ui.theme.DetailCardSurfaceElevated
import com.example.klimata.ui.theme.DetailTextMuted
import com.example.klimata.ui.theme.DetailTextPrimary
import com.example.klimata.ui.theme.DetailTextSecondary
import com.example.klimata.ui.theme.DiurnalPhase
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.LocalDiurnalColors
import com.example.klimata.ui.theme.MineralMintActive
import com.example.klimata.ui.theme.currentDiurnalPhase
import com.example.klimata.ui.theme.currentMinuteOfDay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SettingsScreen(
    currentPhase: DiurnalPhase = currentDiurnalPhase(),
    onPhaseChange: (DiurnalPhase) -> Unit = {},
    isStatusBarDisabled: Boolean = false,
    onStatusBarDisabledToggle: (Boolean) -> Unit = {},
    energyConfig: EnergyConfig,
    onEnergyConfigUpdated: (EnergyConfig) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val diurnal = LocalDiurnalColors.current
    val coroutineScope = rememberCoroutineScope()
    val irBlaster = remember { IrBlasterService(context) }

    var currentApiKey by remember { mutableStateOf(KlimataPreferences.loadGeminiApiKey(context) ?: "") }
    var apiKeyText by remember { mutableStateOf(currentApiKey) }
    var isApiKeyVisible by remember { mutableStateOf(false) }
    var apiKeySaveSuccess by remember { mutableStateOf(false) }

    var selectedEnergyConfig by remember { mutableStateOf(energyConfig) }
    var tariffInput by remember(selectedEnergyConfig.tariffPerKwh) {
        val tariff = selectedEnergyConfig.tariffPerKwh
        mutableStateOf(if (tariff == tariff.toLong().toDouble()) tariff.toLong().toString() else tariff.toString())
    }
    var detectedCityName by remember { mutableStateOf<String?>(null) }
    var isDetectingLocation by remember { mutableStateOf(false) }

    var isDevOptionsExpanded by remember { mutableStateOf(false) }
    var isTestingIr by remember { mutableStateOf(false) }
    var irTestMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val loc = LocationHelper.detectLocation(context)
            detectedCityName = loc.cityName
            val resolved = EnergyConfig.resolveForLocation(
                cityName = loc.cityName,
                countryCode = loc.countryCode,
                latitude = loc.latitude,
                longitude = loc.longitude
            )
            if (selectedEnergyConfig.countryCode != resolved.countryCode) {
                selectedEnergyConfig = resolved
                tariffInput = if (resolved.tariffPerKwh == resolved.tariffPerKwh.toLong().toDouble()) {
                    resolved.tariffPerKwh.toLong().toString()
                } else {
                    resolved.tariffPerKwh.toString()
                }
                KlimataPreferences.saveEnergyConfig(context, resolved)
                onEnergyConfigUpdated(resolved)
            }
        } catch (e: Exception) {
            val defaultLoc = LocationHelper.resolveDefaultLocation()
            detectedCityName = defaultLoc.cityName
        }
    }

    DetailPageScaffold(
        title = "Settings",
        subtitle = "Preferences",
        onBackClick = onBackClick,
        modifier = modifier
    ) {
        // Section: Electricity Tariff (Auto-adjusted to location)
        SettingsSection(
            title = "Electricity Tariff",
            subtitle = "Energy rate and grid carbon factor for your region"
        ) {
            // Location detection card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DetailCardSurfaceElevated)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Light.MapPin,
                            contentDescription = "Location",
                            tint = diurnal.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = "${detectedCityName ?: "Local Region"} | ${selectedEnergyConfig.currencyCode} (${selectedEnergyConfig.currencySymbol})",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = DetailTextPrimary
                                )
                            )
                            Text(
                                text = "Auto-detected location",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontSize = 11.5.sp,
                                    color = DetailTextMuted
                                )
                            )
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .bouncyClickable(
                                enabled = !isDetectingLocation,
                                shape = RoundedCornerShape(10.dp),
                                onClick = {
                                    coroutineScope.launch {
                                        isDetectingLocation = true
                                        val loc = LocationHelper.detectLocation(context)
                                        detectedCityName = loc.cityName
                                        val resolved = EnergyConfig.resolveForLocation(
                                            cityName = loc.cityName,
                                            countryCode = loc.countryCode,
                                            latitude = loc.latitude,
                                            longitude = loc.longitude
                                        )
                                        selectedEnergyConfig = resolved
                                        tariffInput = if (resolved.tariffPerKwh == resolved.tariffPerKwh.toLong().toDouble()) {
                                            resolved.tariffPerKwh.toLong().toString()
                                        } else {
                                            resolved.tariffPerKwh.toString()
                                        }
                                        KlimataPreferences.saveEnergyConfig(context, resolved)
                                        onEnergyConfigUpdated(resolved)
                                        isDetectingLocation = false
                                    }
                                }
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .background(DetailCardSurface)
                    ) {
                        if (isDetectingLocation) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = diurnal.accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Icon(
                                imageVector = PhosphorIcons.Light.ArrowsClockwise,
                                contentDescription = "Refresh location",
                                tint = diurnal.accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Editable tariff and emission details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DetailCardSurfaceElevated)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Rate per kWh",
                            style = TextStyle(fontFamily = JakartaFamily, fontSize = 13.sp, color = DetailTextSecondary)
                        )
                        Text(
                            text = "Electricity price",
                            style = TextStyle(fontFamily = JakartaFamily, fontSize = 11.sp, color = DetailTextMuted)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = selectedEnergyConfig.currencySymbol,
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = diurnal.accentColor
                            )
                        )

                        TextField(
                            value = tariffInput,
                            onValueChange = { input ->
                                tariffInput = input
                                val parsed = input.toDoubleOrNull()
                                if (parsed != null && parsed > 0.0) {
                                    val updated = selectedEnergyConfig.copy(tariffPerKwh = parsed)
                                    selectedEnergyConfig = updated
                                    KlimataPreferences.saveEnergyConfig(context, updated)
                                    onEnergyConfigUpdated(updated)
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            textStyle = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = DetailTextPrimary,
                                textAlign = TextAlign.End
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = DetailCardSurface,
                                unfocusedContainerColor = DetailCardSurface,
                                focusedIndicatorColor = diurnal.accentColor,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.width(110.dp)
                        )
                    }
                }

                HorizontalDivider(color = DetailCardBorder, thickness = 0.5.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Grid Emission Factor",
                        style = TextStyle(fontFamily = JakartaFamily, fontSize = 13.sp, color = DetailTextSecondary)
                    )
                    Text(
                        text = "${selectedEnergyConfig.gridEmissionFactor} kg CO₂e/kWh",
                        style = TextStyle(fontFamily = JakartaFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MineralMintActive)
                    )
                }
            }
        }

        // Section: IR Remote (Clean, no technical jargon)
        SettingsSection(
            title = "IR Remote",
            subtitle = "Transmitter for controlling legacy split air conditioners"
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DetailCardSurfaceElevated)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Light.Broadcast,
                            contentDescription = null,
                            tint = diurnal.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Transmitter",
                            style = TextStyle(fontFamily = JakartaFamily, fontSize = 13.sp, color = DetailTextSecondary)
                        )
                    }
                    Text(
                        text = "Built-in IR",
                        style = TextStyle(fontFamily = JakartaFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = DetailTextPrimary)
                    )
                }

                HorizontalDivider(color = DetailCardBorder, thickness = 0.5.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Carrier Frequency",
                        style = TextStyle(fontFamily = JakartaFamily, fontSize = 13.sp, color = DetailTextSecondary)
                    )
                    Text(
                        text = "38.0 kHz",
                        style = TextStyle(fontFamily = JakartaFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = DetailTextPrimary)
                    )
                }
            }

            val testBtnShape = RoundedCornerShape(14.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .bouncyClickable(
                        enabled = !isTestingIr,
                        shape = testBtnShape,
                        onClick = {
                            isTestingIr = true
                            irTestMessage = null
                            coroutineScope.launch {
                                val success = irBlaster.dispatchAcCommand(
                                    brand = "Panasonic",
                                    power = true,
                                    temp = 24,
                                    mode = "Cool",
                                    fanSpeed = "Auto",
                                    isEco = true,
                                    swing = false
                                )
                                delay(300)
                                isTestingIr = false
                                irTestMessage = if (success) "Signal sent successfully" else "Signal sent via loopback"
                            }
                        }
                    )
                    .clip(testBtnShape)
                    .background(DetailCardSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isTestingIr) {
                        CircularProgressIndicator(
                            color = diurnal.accentColor,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Icon(
                            imageVector = PhosphorIcons.Light.Power,
                            contentDescription = null,
                            tint = diurnal.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = if (isTestingIr) "Testing Transmitter..." else (irTestMessage ?: "Test Signal"),
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = DetailTextPrimary
                        )
                    )
                }
            }
        }

        // Section: Developer Options Accordion (Collapsible)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(DetailCardSurface)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { isDevOptionsExpanded = !isDevOptionsExpanded }
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Developer Options",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            letterSpacing = (-0.3).sp,
                            color = DetailTextPrimary
                        )
                    )
                    Text(
                        text = "System display and API configuration",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = DetailTextSecondary
                        )
                    )
                }

                Icon(
                    imageVector = if (isDevOptionsExpanded) PhosphorIcons.Light.CaretDown else PhosphorIcons.Light.CaretRight,
                    contentDescription = if (isDevOptionsExpanded) "Collapse" else "Expand",
                    tint = DetailTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(
                visible = isDevOptionsExpanded,
                enter = fadeIn(tween(200)) + expandVertically(tween(250)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(200))
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    HorizontalDivider(color = DetailCardBorder, thickness = 0.5.dp)

                    // OS Status Bar Display Control
                    val statusBarCardShape = RoundedCornerShape(14.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bouncyClickable(
                                shape = statusBarCardShape,
                                onClick = { onStatusBarDisabledToggle(!isStatusBarDisabled) }
                            )
                            .clip(statusBarCardShape)
                            .background(DetailCardSurfaceElevated)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp)
                            ) {
                                Icon(
                                    imageVector = if (isStatusBarDisabled) PhosphorIcons.Light.EyeSlash else PhosphorIcons.Light.Eye,
                                    contentDescription = if (isStatusBarDisabled) "Status bar hidden" else "Status bar visible",
                                    tint = if (isStatusBarDisabled) diurnal.accentColor else DetailTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                    Text(
                                        text = "Disable OS Status Bar",
                                        style = TextStyle(
                                            fontFamily = JakartaFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = DetailTextPrimary
                                        )
                                    )
                                    Text(
                                        text = if (isStatusBarDisabled) "Status bar hidden for immersive view" else "Show system clock, battery, and icons",
                                        style = TextStyle(
                                            fontFamily = JakartaFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 11.5.sp,
                                            color = DetailTextSecondary
                                        )
                                    )
                                }
                            }

                            val switchTrackWidth = 42.dp
                            val switchTrackHeight = 24.dp
                            val thumbSize = 18.dp
                            val thumbPadding = 3.dp

                            val switchTrackBg by animateColorAsState(
                                targetValue = if (isStatusBarDisabled) diurnal.accentColor else DetailCardBorder.copy(alpha = 0.7f),
                                animationSpec = tween(200),
                                label = "statusBarSwitchTrackBg"
                            )
                            val thumbOffset by animateDpAsState(
                                targetValue = if (isStatusBarDisabled) switchTrackWidth - thumbSize - thumbPadding else thumbPadding,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy),
                                label = "statusBarThumbOffset"
                            )

                            Box(
                                modifier = Modifier
                                    .width(switchTrackWidth)
                                    .height(switchTrackHeight)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(switchTrackBg),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Box(
                                    modifier = Modifier
                                        .offset(x = thumbOffset)
                                        .size(thumbSize)
                                        .clip(CircleShape)
                                        .background(if (isStatusBarDisabled) diurnal.onAccent else DetailTextPrimary)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = DetailCardBorder, thickness = 0.5.dp)

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Gemini AI Vision",
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = DetailTextPrimary
                            )
                        )
                        Text(
                            text = "API key for scanning AC rating plates with your camera",
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                color = DetailTextSecondary
                            )
                        )
                    }

                    TextField(
                        value = apiKeyText,
                        onValueChange = {
                            apiKeyText = it
                            apiKeySaveSuccess = false
                        },
                        placeholder = {
                            Text(
                                text = "AIzaSy...",
                                style = TextStyle(fontFamily = JakartaFamily, fontSize = 13.sp, color = DetailTextMuted)
                            )
                        },
                        visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                Icon(
                                    imageVector = if (isApiKeyVisible) PhosphorIcons.Light.EyeSlash else PhosphorIcons.Light.Eye,
                                    contentDescription = if (isApiKeyVisible) "Hide key" else "Show key",
                                    tint = DetailTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontFamily = JakartaFamily,
                            fontSize = 13.5.sp,
                            color = DetailTextPrimary
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DetailCardSurfaceElevated,
                            unfocusedContainerColor = DetailCardSurfaceElevated,
                            disabledContainerColor = DetailCardSurfaceElevated,
                            focusedIndicatorColor = diurnal.accentColor,
                            unfocusedIndicatorColor = DetailCardBorder,
                            cursorColor = diurnal.accentColor
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (apiKeySaveSuccess) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Light.Check,
                                    contentDescription = null,
                                    tint = MineralMintActive,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Key saved successfully",
                                    style = TextStyle(
                                        fontFamily = JakartaFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = MineralMintActive
                                    )
                                )
                            }
                        } else {
                            Text(
                                text = if (currentApiKey.isNotBlank()) "Active key configured" else "Default build key active",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = DetailTextMuted
                                )
                            )
                        }

                        val saveBtnShape = RoundedCornerShape(12.dp)
                        Box(
                            modifier = Modifier
                                .bouncyClickable(
                                    shape = saveBtnShape,
                                    onClick = {
                                        KlimataPreferences.saveGeminiApiKey(context, apiKeyText.trim())
                                        currentApiKey = apiKeyText.trim()
                                        apiKeySaveSuccess = true
                                    }
                                )
                                .clip(saveBtnShape)
                                .background(diurnal.accentColor)
                                .padding(horizontal = 16.dp, vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Save Key",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = diurnal.onAccent
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section: About Klimata (Concise, zero buzzwords)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Klimata",
                style = TextStyle(
                    fontFamily = JakartaFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DetailTextSecondary
                )
            )
            Text(
                text = "Smart AC scheduling | Weather sync",
                style = TextStyle(
                    fontFamily = JakartaFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.5.sp,
                    color = DetailTextMuted
                )
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(DetailCardSurface)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = JakartaFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = (-0.3).sp,
                    color = DetailTextPrimary
                )
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = DetailTextSecondary
                    )
                )
            }
        }
        content()
    }
}

@Composable
private fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
