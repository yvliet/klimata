package com.example.klimata.ui.screens.provisioning

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.ArrowsClockwise
import com.adamglin.phosphoricons.light.Camera
import com.adamglin.phosphoricons.light.CaretLeft
import com.adamglin.phosphoricons.light.Check
import com.adamglin.phosphoricons.light.MapPin
import com.example.klimata.data.AcRecognitionService
import com.example.klimata.data.LocationHelper
import com.example.klimata.data.RoomFactory
import com.example.klimata.data.RoomState
import com.example.klimata.data.engine.ThermalCalculationEngine
import com.example.klimata.data.network.AmbientWeatherReport
import com.example.klimata.data.network.WeatherApiClient
import com.example.klimata.data.storage.KlimataPreferences
import com.example.klimata.ui.components.bouncyClickable
import com.example.klimata.ui.components.detailPageContainer
import com.example.klimata.ui.theme.DetailBlackBackground
import com.example.klimata.ui.theme.DetailCardBorder
import com.example.klimata.ui.theme.DetailCardSurface
import com.example.klimata.ui.theme.DetailCardSurfaceElevated
import com.example.klimata.ui.theme.DetailCurveAmbient
import com.example.klimata.ui.theme.DetailTextMuted
import com.example.klimata.ui.theme.DetailTextPrimary
import com.example.klimata.ui.theme.DetailTextSecondary
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.LocalDiurnalColors
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

enum class WizardStage {
    NAME_ROOM,
    ROOM_SIZING,
    CONSTRUCTION_ANIMATION,
    CAPTURE_AC,
    CONFIRM_AC,
    LOCATION_SYNC,
    IMPACT_REVEAL
}

private fun shoelaceAreaM2(vertices: List<Offset>, pixelsPerMeter: Float): Float {
    if (vertices.size < 3) return 0f
    var sum = 0f
    for (i in vertices.indices) {
        val p1 = vertices[i]
        val p2 = vertices[(i + 1) % vertices.size]
        sum += (p1.x * p2.y - p2.x * p1.y)
    }
    return abs(sum / 2f) / (pixelsPerMeter * pixelsPerMeter)
}

@Composable
fun AddRoomWizardScreen(
    isOnboarding: Boolean = true,
    onBackClick: () -> Unit = {},
    onRoomCreated: (RoomState) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val diurnal = LocalDiurnalColors.current

    var currentStage by remember { mutableStateOf(WizardStage.NAME_ROOM) }

    // Room specification state
    var roomName by remember { mutableStateOf("Master Bedroom") }
    var areaSquareMeters by remember { mutableIntStateOf(20) }
    var ceilingHeight by remember { mutableFloatStateOf(2.8f) }
    var thermalMass by remember { mutableStateOf("Medium") }
    var manualLength by remember { mutableFloatStateOf(5.0f) }
    var manualWidth by remember { mutableFloatStateOf(4.0f) }
    var sizingMode by remember { mutableStateOf("presets") }
    var selectedPreset by remember { mutableStateOf<String?>("Standard") }

    // Custom canvas polygon state
    var customVertices by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var isPolygonClosed by remember { mutableStateOf(false) }

    val floorVerticesInMeters = remember(sizingMode, manualWidth, manualLength, customVertices, isPolygonClosed) {
        if (sizingMode == "custom" && isPolygonClosed && customVertices.size >= 3) {
            val minX = customVertices.minOf { it.x }
            val minY = customVertices.minOf { it.y }
            val pxPerMeter = with(density) { 60.dp.toPx() }
            customVertices.map { Offset((it.x - minX) / pxPerMeter, (it.y - minY) / pxPerMeter) }
        } else {
            listOf(
                Offset(0f, 0f),
                Offset(manualWidth, 0f),
                Offset(manualWidth, manualLength),
                Offset(0f, manualLength)
            )
        }
    }

    // AC hardware state
    var acBrand by remember { mutableStateOf("Sharp") }
    var acModel by remember { mutableStateOf("AH-XP10") }
    var acCapacity by remember { mutableStateOf("1.0 PK") }
    var acInverterType by remember { mutableStateOf("J-Tech Inverter") }
    var acPhotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isAnalyzingPhoto by remember { mutableStateOf(false) }
    var isAiVerified by remember { mutableStateOf(false) }

    // Location state
    var selectedLocation by remember { mutableStateOf("South Jakarta") }
    var liveWeatherReport by remember { mutableStateOf<AmbientWeatherReport?>(null) }
    var isDetectingGps by remember { mutableStateOf(false) }
    var gpsStatusMessage by remember { mutableStateOf<String?>(null) }


    // GPS location & live Open-Meteo weather detection logic
    fun runGpsDetection() {
        coroutineScope.launch {
            isDetectingGps = true
            gpsStatusMessage = "Detecting GPS location..."
            val loc = LocationHelper.detectLocation(context)
            selectedLocation = loc.cityName
            gpsStatusMessage = "Fetching Open-Meteo weather for ${loc.cityName}..."
            val weatherResult = WeatherApiClient.fetchWeather(loc.latitude, loc.longitude, loc.cityName)
            if (weatherResult.isSuccess) {
                val report = weatherResult.getOrThrow()
                liveWeatherReport = report
                KlimataPreferences.saveWeather(context, report)
                gpsStatusMessage = "${report.condition} · ${report.currentOutdoorTemp}°C · AQI ${report.aqiValue} (${report.aqiLabel})"
            } else {
                gpsStatusMessage = "Location synchronized: ${loc.cityName}"
            }
            isDetectingGps = false
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            runGpsDetection()
        } else {
            gpsStatusMessage = "Using manual city selection"
        }
    }

    // Auto-detect GPS when entering location sync stage
    LaunchedEffect(currentStage) {
        if (currentStage == WizardStage.LOCATION_SYNC) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                runGpsDetection()
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
    }

    // Photo pickers
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val bmp = try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                    android.graphics.ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            } catch (e: Exception) {
                null
            }
            if (bmp != null) {
                acPhotoBitmap = bmp
                isAnalyzingPhoto = true
                currentStage = WizardStage.CONFIRM_AC
                coroutineScope.launch {
                    val result = AcRecognitionService.analyzeAcPhoto(bmp, areaSquareMeters)
                    acBrand = result.brand
                    acModel = result.model
                    acCapacity = result.capacity
                    acInverterType = result.inverterType
                    isAiVerified = result.isAiDetected
                    isAnalyzingPhoto = false
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        if (bmp != null) {
            acPhotoBitmap = bmp
            isAnalyzingPhoto = true
            currentStage = WizardStage.CONFIRM_AC
            coroutineScope.launch {
                val result = AcRecognitionService.analyzeAcPhoto(bmp, areaSquareMeters)
                acBrand = result.brand
                acModel = result.model
                acCapacity = result.capacity
                acInverterType = result.inverterType
                isAiVerified = result.isAiDetected
                isAnalyzingPhoto = false
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                try {
                    galleryLauncher.launch("image/*")
                } catch (ignored: Exception) {}
            }
        } else {
            try {
                galleryLauncher.launch("image/*")
            } catch (ignored: Exception) {}
        }
    }

    fun completeAndSaveRoom() {
        val newRoom = RoomFactory.createRoom(
            name = roomName,
            areaSquareMeters = areaSquareMeters,
            ceilingHeightMeters = ceilingHeight,
            thermalMassType = thermalMass,
            acBrand = acBrand,
            acModel = acModel,
            acCapacity = acCapacity,
            acInverterType = acInverterType,
            location = selectedLocation,
            hourlyOutdoorTemps = liveWeatherReport?.hourlyTemps ?: emptyMap(),
            weatherCondition = liveWeatherReport?.condition ?: "Clear Night"
        )
        onRoomCreated(newRoom)
    }

    fun handleBack() {
        when (currentStage) {
            WizardStage.NAME_ROOM -> onBackClick()
            WizardStage.ROOM_SIZING -> currentStage = WizardStage.NAME_ROOM
            WizardStage.CONSTRUCTION_ANIMATION -> currentStage = WizardStage.ROOM_SIZING
            WizardStage.CAPTURE_AC -> currentStage = WizardStage.ROOM_SIZING
            WizardStage.CONFIRM_AC -> currentStage = WizardStage.CAPTURE_AC
            WizardStage.LOCATION_SYNC -> currentStage = WizardStage.CONFIRM_AC
            WizardStage.IMPACT_REVEAL -> currentStage = WizardStage.LOCATION_SYNC
        }
    }

    // Intercept system back button / back swipe gesture so user steps back within wizard instead of exiting
    BackHandler {
        handleBack()
    }

    BoxWithConstraints(
        modifier = modifier.detailPageContainer()
    ) {
        val screenHeight = maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Minimal top navigation header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .size(40.dp)
                        .bouncyClickable(
                            shape = CircleShape,
                            onClick = { handleBack() }
                        )
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Light.CaretLeft,
                        contentDescription = "Back",
                        tint = DetailTextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = if (isOnboarding) "Room Setup" else "Add Room",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = DetailTextSecondary
                    )
                )

                Spacer(modifier = Modifier.size(40.dp))
            }

            AnimatedContent(
                targetState = currentStage,
                transitionSpec = {
                    val forward = targetState.ordinal > initialState.ordinal
                    if (forward) {
                        (slideInHorizontally { width -> width / 3 } + fadeIn(tween(200)))
                            .togetherWith(slideOutHorizontally { width -> -width / 3 } + fadeOut(tween(200)))
                    } else {
                        (slideInHorizontally { width -> -width / 3 } + fadeIn(tween(200)))
                            .togetherWith(slideOutHorizontally { width -> width / 3 } + fadeOut(tween(200)))
                    }
                },
                label = "WizardStageTransition"
            ) { stage ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (stage) {
                        // Stage 1: Room Naming
                        WizardStage.NAME_ROOM -> {
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "What should we call this room?",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    letterSpacing = (-0.5).sp,
                                    color = DetailTextPrimary,
                                    textAlign = TextAlign.Center
                                )
                            )

                            Text(
                                text = "Choose a name or create your own.",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = DetailTextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            TextField(
                                value = roomName,
                                onValueChange = { roomName = it },
                                placeholder = {
                                    Text(
                                        text = "e.g. Master Bedroom",
                                        style = TextStyle(
                                            fontFamily = JakartaFamily,
                                            fontSize = 15.sp,
                                            color = DetailTextMuted
                                        )
                                    )
                                },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = DetailTextPrimary,
                                    textAlign = TextAlign.Center
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = DetailCardSurface,
                                    unfocusedContainerColor = DetailCardSurface,
                                    disabledContainerColor = DetailCardSurface,
                                    focusedIndicatorColor = diurnal.accentColor,
                                    unfocusedIndicatorColor = DetailCardBorder,
                                    cursorColor = diurnal.accentColor
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            PrimaryActionButton(
                                label = "Continue to Sizing",
                                enabled = roomName.isNotBlank(),
                                onClick = {
                                    currentStage = WizardStage.ROOM_SIZING
                                }
                            )
                        }

                        // Stage 2: Room Sizing
                        WizardStage.ROOM_SIZING -> {
                            Text(
                                text = "Lets size your room",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    letterSpacing = (-0.5).sp,
                                    color = DetailTextPrimary,
                                    textAlign = TextAlign.Center
                                )
                            )

                            Text(
                                text = if (sizingMode == "presets") {
                                    "Adjust dimensions to match your room in 3D."
                                } else {
                                    "Tap corners on the grid to plot your room."
                                },
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = DetailTextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            )

                            // Mode Switcher
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(DetailCardSurface)
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .bouncyClickable(
                                            shape = RoundedCornerShape(10.dp),
                                            onClick = { sizingMode = "presets" }
                                        )
                                        .background(if (sizingMode == "presets") diurnal.accentColor else Color.Transparent)
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Presets",
                                        style = TextStyle(
                                            fontFamily = JakartaFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = if (sizingMode == "presets") diurnal.onAccent else DetailTextSecondary
                                        )
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .bouncyClickable(
                                            shape = RoundedCornerShape(10.dp),
                                            onClick = { sizingMode = "custom" }
                                        )
                                        .background(if (sizingMode == "custom") diurnal.accentColor else Color.Transparent)
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Custom Canvas",
                                        style = TextStyle(
                                            fontFamily = JakartaFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = if (sizingMode == "custom") diurnal.onAccent else DetailTextSecondary
                                        )
                                    )
                                }
                            }

                            if (sizingMode == "presets") {
                                // Live 3D isometric room wireframe canvas with 2D/3D toggle
                                PresetRoomCanvas(
                                    widthMeters = manualWidth,
                                    lengthMeters = manualLength,
                                    ceilingHeightMeters = ceilingHeight,
                                    accentColor = diurnal.accentColor,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(210.dp)
                                )

                                // Live area and volume badge with math multiplication symbol
                                val areaDisplay = (manualWidth * manualLength)
                                val volumeDisplay = (areaDisplay * ceilingHeight).roundToInt()
                                Text(
                                    text = "%.1f m² × %d m³".format(areaDisplay, volumeDisplay),
                                    style = TextStyle(
                                        fontFamily = JakartaFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = diurnal.accentColor,
                                        textAlign = TextAlign.Center
                                    )
                                )

                                // Room size presets
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(DetailCardSurface)
                                        .padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(
                                            Triple("Compact", 9, 3.0f to 3.0f),
                                            Triple("Standard", 16, 4.0f to 4.0f),
                                            Triple("Spacious", 25, 5.0f to 5.0f)
                                        ).forEach { (label, sqm, dims) ->
                                            val isSelected = selectedPreset == label
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .bouncyClickable(
                                                        shape = RoundedCornerShape(12.dp),
                                                        onClick = {
                                                            selectedPreset = label
                                                            areaSquareMeters = sqm
                                                            manualWidth = dims.first
                                                            manualLength = dims.second
                                                        }
                                                    )
                                                    .background(if (isSelected) diurnal.accentColor.copy(alpha = 0.20f) else DetailCardSurfaceElevated)
                                                    .padding(vertical = 10.dp)
                                            ) {
                                                Text(
                                                    text = "$label\n$sqm m²",
                                                    style = TextStyle(
                                                        fontFamily = JakartaFamily,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        fontSize = 12.sp,
                                                        color = if (isSelected) diurnal.accentColor else DetailTextPrimary,
                                                        textAlign = TextAlign.Center
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    // Width slider
                                    Column {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(text = "Width", style = TextStyle(fontFamily = JakartaFamily, fontSize = 12.sp, color = DetailTextSecondary))
                                            Text(text = "%.1f m".format(manualWidth), style = TextStyle(fontFamily = JakartaFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DetailTextPrimary))
                                        }
                                        Slider(
                                            value = manualWidth,
                                            onValueChange = {
                                                selectedPreset = null
                                                manualWidth = (it * 10f).roundToInt() / 10f
                                                areaSquareMeters = (manualWidth * manualLength).roundToInt().coerceIn(8, 80)
                                            },
                                            valueRange = 2.5f..8.0f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = diurnal.accentColor,
                                                activeTrackColor = diurnal.accentColor,
                                                inactiveTrackColor = DetailCardBorder
                                            )
                                        )
                                    }

                                    // Length slider
                                    Column {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(text = "Length", style = TextStyle(fontFamily = JakartaFamily, fontSize = 12.sp, color = DetailTextSecondary))
                                            Text(text = "%.1f m".format(manualLength), style = TextStyle(fontFamily = JakartaFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DetailTextPrimary))
                                        }
                                        Slider(
                                            value = manualLength,
                                            onValueChange = {
                                                selectedPreset = null
                                                manualLength = (it * 10f).roundToInt() / 10f
                                                areaSquareMeters = (manualWidth * manualLength).roundToInt().coerceIn(8, 80)
                                            },
                                            valueRange = 2.5f..10.0f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = diurnal.accentColor,
                                                activeTrackColor = diurnal.accentColor,
                                                inactiveTrackColor = DetailCardBorder
                                            )
                                        )
                                    }

                                    // Ceiling Height slider
                                    Column {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(text = "Ceiling Height", style = TextStyle(fontFamily = JakartaFamily, fontSize = 12.sp, color = DetailTextSecondary))
                                            Text(text = "%.1f m".format(ceilingHeight), style = TextStyle(fontFamily = JakartaFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DetailTextPrimary))
                                        }
                                        Slider(
                                            value = ceilingHeight,
                                            onValueChange = {
                                                selectedPreset = null
                                                ceilingHeight = (it * 10f).roundToInt() / 10f
                                            },
                                            valueRange = 2.2f..4.0f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = diurnal.accentColor,
                                                activeTrackColor = diurnal.accentColor,
                                                inactiveTrackColor = DetailCardBorder
                                            )
                                        )
                                    }
                                }

                                WallThermalMaterialPicker(
                                    thermalMass = thermalMass,
                                    onThermalMassChange = { thermalMass = it },
                                    accentColor = diurnal.accentColor
                                )

                                PrimaryActionButton(
                                    label = "Confirm Room ($areaSquareMeters m²)",
                                    onClick = { currentStage = WizardStage.CONSTRUCTION_ANIMATION }
                                )
                            } else {
                                // Custom polygon canvas: 3D preview when closed, interactive grid when drafting
                                if (isPolygonClosed && customVertices.size >= 3) {
                                    RoomConstructionCanvas(
                                        floorVertices = floorVerticesInMeters,
                                        ceilingHeightM = ceilingHeight,
                                        accentColor = diurnal.accentColor,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(240.dp)
                                    )
                                } else {
                                    CustomRoomCanvas(
                                        vertices = customVertices,
                                        onVerticesChanged = { customVertices = it },
                                        isClosed = isPolygonClosed,
                                        onClosedChanged = { isPolygonClosed = it },
                                        accentColor = diurnal.accentColor,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(280.dp)
                                    )
                                }

                                // Area readout and controls
                                if (customVertices.isNotEmpty()) {
                                    val pxPerMeter = with(density) { 60.dp.toPx() }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${customVertices.size} corners",
                                            style = TextStyle(
                                                fontFamily = JakartaFamily,
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 12.5.sp,
                                                color = DetailTextSecondary
                                            )
                                        )

                                        if (isPolygonClosed && customVertices.size >= 3) {
                                            val customArea = shoelaceAreaM2(customVertices, pxPerMeter)
                                            val customVol = (customArea * ceilingHeight).roundToInt()
                                            Box(
                                                modifier = Modifier
                                                    .padding(horizontal = 6.dp)
                                                    .width(1.dp)
                                                    .height(9.dp)
                                                    .background(DetailTextMuted.copy(alpha = 0.40f))
                                            )
                                            Text(
                                                text = "%.1f m² × %d m³".format(customArea, customVol),
                                                style = TextStyle(
                                                    fontFamily = JakartaFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = diurnal.accentColor
                                                )
                                            )
                                        }
                                    }

                                    // Explicit Close Shape action button when >= 3 corners placed
                                    if (!isPolygonClosed && customVertices.size >= 3) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp)
                                                .bouncyClickable(
                                                    shape = RoundedCornerShape(12.dp),
                                                    onClick = { isPolygonClosed = true }
                                                )
                                                .background(diurnal.accentColor.copy(alpha = 0.18f))
                                        ) {
                                            Text(
                                                text = "Close Shape (${customVertices.size} Corners)",
                                                style = TextStyle(
                                                    fontFamily = JakartaFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = diurnal.accentColor
                                                )
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Undo / Edit button
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .bouncyClickable(
                                                    shape = RoundedCornerShape(12.dp),
                                                    onClick = {
                                                        if (isPolygonClosed) {
                                                            isPolygonClosed = false
                                                        } else if (customVertices.isNotEmpty()) {
                                                            customVertices = customVertices.dropLast(1)
                                                        }
                                                    }
                                                )
                                                .background(DetailCardSurface)
                                        ) {
                                            Text(
                                                text = if (isPolygonClosed) "Edit Corners" else "Undo",
                                                style = TextStyle(
                                                    fontFamily = JakartaFamily,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.5.sp,
                                                    color = DetailTextPrimary
                                                )
                                            )
                                        }

                                        // Reset button
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .bouncyClickable(
                                                    shape = RoundedCornerShape(12.dp),
                                                    onClick = {
                                                        customVertices = emptyList()
                                                        isPolygonClosed = false
                                                    }
                                                )
                                                .background(DetailCardSurface)
                                        ) {
                                            Text(
                                                text = "Reset",
                                                style = TextStyle(
                                                    fontFamily = JakartaFamily,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.5.sp,
                                                    color = DetailTextSecondary
                                                )
                                            )
                                        }
                                    }

                                    // Ceiling Height Slider on Custom Canvas
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(DetailCardSurface)
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Ceiling Height",
                                                style = TextStyle(
                                                    fontFamily = JakartaFamily,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.5.sp,
                                                    color = DetailTextPrimary
                                                )
                                            )
                                            Text(
                                                text = "%.1f m".format(ceilingHeight),
                                                style = TextStyle(
                                                    fontFamily = JakartaFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.5.sp,
                                                    color = diurnal.accentColor
                                                )
                                            )
                                        }
                                        Slider(
                                            value = ceilingHeight,
                                            onValueChange = {
                                                ceilingHeight = (it * 10f).roundToInt() / 10f
                                            },
                                            valueRange = 2.2f..4.0f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = diurnal.accentColor,
                                                activeTrackColor = diurnal.accentColor,
                                                inactiveTrackColor = DetailCardBorder
                                            )
                                        )
                                    }
                                }

                                WallThermalMaterialPicker(
                                    thermalMass = thermalMass,
                                    onThermalMassChange = { thermalMass = it },
                                    accentColor = diurnal.accentColor
                                )

                                val canFinishCustom = isPolygonClosed && customVertices.size >= 3
                                PrimaryActionButton(
                                    label = if (canFinishCustom) {
                                        val pxPerMeter = with(density) { 60.dp.toPx() }
                                        val customArea = shoelaceAreaM2(customVertices, pxPerMeter).roundToInt()
                                        "Confirm Room ($customArea m²)"
                                    } else {
                                        "Close the shape first"
                                    },
                                    enabled = canFinishCustom,
                                    onClick = {
                                        val pxPerMeter = with(density) { 60.dp.toPx() }
                                        areaSquareMeters = shoelaceAreaM2(customVertices, pxPerMeter).roundToInt().coerceIn(8, 80)
                                        currentStage = WizardStage.CONSTRUCTION_ANIMATION
                                    }
                                )
                            }
                        }

                        // Stage 3: Room Construction Animation
                        WizardStage.CONSTRUCTION_ANIMATION -> {
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Constructing room wireframe",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    letterSpacing = (-0.5).sp,
                                    color = DetailTextPrimary,
                                    textAlign = TextAlign.Center
                                )
                            )

                            Text(
                                text = "Synthesizing spatial geometry and thermal volume...",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = DetailTextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            )

                            RoomConstructionCanvas(
                                floorVertices = floorVerticesInMeters,
                                ceilingHeightM = ceilingHeight,
                                accentColor = diurnal.accentColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp)
                            )

                            val volume = (areaSquareMeters * ceilingHeight).roundToInt()
                            Text(
                                text = "$areaSquareMeters m² × $volume m³",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = diurnal.accentColor,
                                    textAlign = TextAlign.Center
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            PrimaryActionButton(
                                label = "Next: Pair AC Unit",
                                onClick = { currentStage = WizardStage.CAPTURE_AC }
                            )
                        }

                        // Stage 4: Photograph AC or Choose from Gallery
                        WizardStage.CAPTURE_AC -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = screenHeight - 110.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Capture your AC unit",
                                        style = TextStyle(
                                            fontFamily = JakartaFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 24.sp,
                                            letterSpacing = (-0.5).sp,
                                            color = DetailTextPrimary,
                                            textAlign = TextAlign.Center
                                        )
                                    )

                                    Text(
                                        text = "Take a picture of the indoor AC unit or its model sticker.",
                                        style = TextStyle(
                                            fontFamily = JakartaFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 13.sp,
                                            color = DetailTextSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                    )

                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .bouncyClickable(
                                                shape = RoundedCornerShape(22.dp),
                                                onClick = {
                                                    val hasCameraPermission = ContextCompat.checkSelfPermission(
                                                        context,
                                                        Manifest.permission.CAMERA
                                                    ) == PackageManager.PERMISSION_GRANTED

                                                    if (hasCameraPermission) {
                                                        try {
                                                            cameraLauncher.launch(null)
                                                        } catch (e: Exception) {
                                                            try {
                                                                galleryLauncher.launch("image/*")
                                                            } catch (ignored: Exception) {}
                                                        }
                                                    } else {
                                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                                    }
                                                }
                                            )
                                            .background(DetailCardSurface)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(52.dp)
                                                    .clip(CircleShape)
                                                    .background(diurnal.accentColor.copy(alpha = 0.16f))
                                            ) {
                                                Icon(
                                                    imageVector = PhosphorIcons.Light.Camera,
                                                    contentDescription = "Take Photo",
                                                    tint = diurnal.accentColor,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Text(
                                                text = "Take a Picture of AC",
                                                style = TextStyle(
                                                    fontFamily = JakartaFamily,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 14.sp,
                                                    color = DetailTextPrimary
                                                )
                                            )
                                        }
                                    }

                                    Text(
                                        text = "or choose a photo from your gallery",
                                        style = TextStyle(
                                            fontFamily = JakartaFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 12.5.sp,
                                            color = DetailTextSecondary,
                                            textAlign = TextAlign.Center
                                        ),
                                        modifier = Modifier
                                            .bouncyClickable(
                                                shape = RoundedCornerShape(8.dp),
                                                onClick = { galleryLauncher.launch("image/*") }
                                            )
                                            .padding(vertical = 4.dp, horizontal = 8.dp)
                                    )
                                }

                                Text(
                                    text = "Skip and choose AC model manually",
                                    style = TextStyle(
                                        fontFamily = JakartaFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = DetailTextMuted
                                    ),
                                    modifier = Modifier
                                        .bouncyClickable(
                                            shape = RoundedCornerShape(8.dp),
                                            onClick = { currentStage = WizardStage.CONFIRM_AC }
                                        )
                                        .padding(bottom = 16.dp, top = 8.dp, start = 8.dp, end = 8.dp)
                                )
                            }
                        }

                        // Stage 5: AC Confirmation
                        WizardStage.CONFIRM_AC -> {
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (isAnalyzingPhoto) "Analyzing AC..." else "Is this right?",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    letterSpacing = (-0.5).sp,
                                    color = DetailTextPrimary,
                                    textAlign = TextAlign.Center
                                )
                            )

                            Text(
                                text = if (isAnalyzingPhoto) {
                                    "Reading unit badge and capacity profile..."
                                } else {
                                    "Verify your AC specifications below."
                                },
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = DetailTextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            )

                            if (isAnalyzingPhoto) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(DetailCardSurface)
                                ) {
                                    CircularProgressIndicator(
                                        color = diurnal.accentColor,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            } else {
                                acPhotoBitmap?.let { bmp ->
                                    Box(
                                        modifier = Modifier
                                            .size(96.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(DetailCardSurface)
                                    ) {
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = "AC Photo",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                val assessment = RoomFactory.assessAcMatch(acCapacity, areaSquareMeters)
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(DetailCardSurface)
                                        .padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "$acBrand $acModel",
                                            style = TextStyle(
                                                fontFamily = JakartaFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = DetailTextPrimary
                                            )
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(diurnal.accentColor.copy(alpha = 0.16f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = if (isAiVerified) "Gemini Vision Verified" else assessment.status,
                                                style = TextStyle(
                                                    fontFamily = JakartaFamily,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 11.sp,
                                                    color = diurnal.accentColor
                                                )
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = acCapacity,
                                            style = TextStyle(
                                                fontFamily = JakartaFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 13.sp,
                                                color = DetailTextSecondary
                                            )
                                        )
                                        Box(
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(9.dp)
                                                .background(DetailTextMuted.copy(alpha = 0.40f))
                                        )
                                        Text(
                                            text = acInverterType,
                                            style = TextStyle(
                                                fontFamily = JakartaFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 13.sp,
                                                color = DetailTextSecondary
                                            )
                                        )
                                    }

                                    Text(
                                        text = assessment.description,
                                        style = TextStyle(
                                            fontFamily = JakartaFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 12.sp,
                                            lineHeight = 17.sp,
                                            color = DetailTextMuted
                                        )
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .bouncyClickable(
                                                shape = RoundedCornerShape(14.dp),
                                                onClick = {
                                                    acBrand = when (acBrand) {
                                                        "Sharp" -> "Daikin"
                                                        "Daikin" -> "Panasonic"
                                                        "Panasonic" -> "Mitsubishi"
                                                        "Mitsubishi" -> "LG"
                                                        "LG" -> "Gree"
                                                        else -> "Sharp"
                                                    }
                                                    acModel = when (acBrand) {
                                                        "Sharp" -> "AH-XP10"
                                                        "Daikin" -> "FTKF25"
                                                        "Panasonic" -> "CS-XU18XKH"
                                                        "Mitsubishi" -> "MSY-GR13VF"
                                                        "LG" -> "DualCool"
                                                        else -> "Eco Series"
                                                    }
                                                    acCapacity = when (acCapacity) {
                                                        "0.5 PK" -> "0.75 PK"
                                                        "0.75 PK" -> "1.0 PK"
                                                        "1.0 PK" -> "1.5 PK"
                                                        "1.5 PK" -> "2.0 PK"
                                                        else -> "0.5 PK"
                                                    }
                                                }
                                            )
                                            .background(DetailCardSurface)
                                    ) {
                                        Text(
                                            text = "Change Specs",
                                            style = TextStyle(
                                                fontFamily = JakartaFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 13.sp,
                                                color = DetailTextSecondary
                                            )
                                        )
                                    }

                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .height(48.dp)
                                            .bouncyClickable(
                                                shape = RoundedCornerShape(14.dp),
                                                onClick = {
                                                    currentStage = WizardStage.LOCATION_SYNC
                                                }
                                            )
                                            .background(diurnal.accentColor)
                                    ) {
                                        Text(
                                            text = "Yes, Looks Right",
                                            style = TextStyle(
                                                fontFamily = JakartaFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = diurnal.onAccent
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Stage 6: GPS & Weather Microclimate Sync
                        WizardStage.LOCATION_SYNC -> {
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Local weather sync",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    letterSpacing = (-0.5).sp,
                                    color = DetailTextPrimary,
                                    textAlign = TextAlign.Center
                                )
                            )

                            Text(
                                text = "Klimata balances indoor thermal inertia against outdoor midnight temperature troughs.",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = DetailTextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            )

                            // Live GPS Status Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(DetailCardSurface)
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                                imageVector = PhosphorIcons.Light.MapPin,
                                                contentDescription = null,
                                                tint = diurnal.accentColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "Current Microclimate: $selectedLocation",
                                                style = TextStyle(
                                                    fontFamily = JakartaFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = DetailTextPrimary
                                                )
                                            )
                                        }

                                        if (isDetectingGps) {
                                            CircularProgressIndicator(
                                                color = diurnal.accentColor,
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .bouncyClickable(
                                                        shape = CircleShape,
                                                        onClick = { runGpsDetection() }
                                                    )
                                                    .background(DetailCardSurfaceElevated)
                                            ) {
                                                Icon(
                                                    imageVector = PhosphorIcons.Light.ArrowsClockwise,
                                                    contentDescription = "Refresh GPS",
                                                    tint = diurnal.accentColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    gpsStatusMessage?.let { status ->
                                        Text(
                                            text = status,
                                            style = TextStyle(
                                                fontFamily = JakartaFamily,
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 12.sp,
                                                color = DetailTextSecondary
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            PrimaryActionButton(
                                label = if (isOnboarding) "Generate Autonomous Schedule" else "Save Room",
                                onClick = {
                                    if (isOnboarding) {
                                        currentStage = WizardStage.IMPACT_REVEAL
                                    } else {
                                        completeAndSaveRoom()
                                    }
                                }
                            )
                        }

                        // Stage 7: Quiet-Confidence Impact Reveal (First-run Onboarding only)
                        WizardStage.IMPACT_REVEAL -> {
                            val dynamicImpact = remember(areaSquareMeters, ceilingHeight, thermalMass, acCapacity, acInverterType, liveWeatherReport) {
                                ThermalCalculationEngine.computeRoomThermalDynamics(
                                    areaSquareMeters = areaSquareMeters,
                                    ceilingHeightMeters = ceilingHeight,
                                    thermalMassType = thermalMass,
                                    acCapacity = acCapacity,
                                    acInverterType = acInverterType,
                                    targetTemp = 24,
                                    hourlyOutdoorTemps = liveWeatherReport?.hourlyTemps ?: emptyMap()
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Your projected savings",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    letterSpacing = (-0.5).sp,
                                    color = DetailTextPrimary,
                                    textAlign = TextAlign.Center
                                )
                            )

                            Text(
                                text = "Based on $areaSquareMeters m² in $selectedLocation with $acCapacity.",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = DetailTextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(DetailCardSurface)
                                    .padding(22.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Estimated Monthly Savings",
                                        style = TextStyle(
                                            fontFamily = JakartaFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 12.sp,
                                            color = DetailTextSecondary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = dynamicImpact.monthlySavings.primaryValue,
                                        style = TextStyle(
                                            fontFamily = JakartaFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 34.sp,
                                            letterSpacing = (-0.5).sp,
                                            color = diurnal.accentColor
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = dynamicImpact.monthlySavings.subtitle,
                                        style = TextStyle(
                                            fontFamily = JakartaFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = DetailTextPrimary
                                        )
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ImpactStatCard(
                                        title = "Trees Equivalent",
                                        value = "%.1f".format(Locale.US, dynamicImpact.carbonEquivalence.treesEquivalent),
                                        unit = "trees/mo",
                                        description = "CO₂ captured naturally",
                                        accentColor = diurnal.accentColor,
                                        modifier = Modifier.weight(1f)
                                    )
                                    ImpactStatCard(
                                        title = "Driving Avoided",
                                        value = "%.0f".format(Locale.US, dynamicImpact.carbonEquivalence.drivingKmAvoided),
                                        unit = "km",
                                        description = "Combustion tailpipe offset",
                                        accentColor = diurnal.accentColor,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            PrimaryActionButton(
                                label = "Enter My Room",
                                onClick = { completeAndSaveRoom() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrimaryActionButton(
    label: String,
    enabled: Boolean = true,
    accentColor: Color = LocalDiurnalColors.current.accentColor,
    onAccent: Color = LocalDiurnalColors.current.onAccent,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .bouncyClickable(
                enabled = enabled,
                shape = RoundedCornerShape(16.dp),
                onClick = onClick
            )
            .background(if (enabled) accentColor else DetailCardSurfaceElevated)
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = JakartaFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (enabled) onAccent else DetailTextMuted
            )
        )
    }
}

@Composable
private fun WallThermalMaterialPicker(
    thermalMass: String,
    onThermalMassChange: (String) -> Unit,
    accentColor: Color = LocalDiurnalColors.current.accentColor,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DetailCardSurface)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Wall Material",
            style = TextStyle(
                fontFamily = JakartaFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.5.sp,
                color = DetailTextPrimary
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple("Light", "Drywall", "Light"),
                Triple("Medium", "Brick", "Medium"),
                Triple("Heavy", "Concrete", "Heavy")
            ).forEach { (category, material, type) ->
                val isSelected = thermalMass == type
                val cardShape = RoundedCornerShape(14.dp)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .bouncyClickable(
                            shape = cardShape,
                            onClick = { onThermalMassChange(type) }
                        )
                        .background(if (isSelected) accentColor.copy(alpha = 0.12f) else DetailCardSurfaceElevated)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) accentColor else DetailCardBorder.copy(alpha = 0.5f),
                            shape = cardShape
                        )
                        .padding(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Architectural pattern swatch window
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) accentColor.copy(alpha = 0.14f) else DetailCardSurface)
                        ) {
                            val patternColor = if (isSelected) accentColor else DetailTextSecondary.copy(alpha = 0.60f)
                            MaterialPatternCanvas(
                                materialType = type,
                                color = patternColor,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp, vertical = 3.dp)
                            )
                        }

                        // Compact name: category on top, material underneath
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = category,
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) accentColor else DetailTextPrimary,
                                    textAlign = TextAlign.Center
                                )
                            )
                            Text(
                                text = material,
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 10.5.sp,
                                    color = if (isSelected) accentColor.copy(alpha = 0.85f) else DetailTextMuted,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialPatternCanvas(
    materialType: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        when (materialType) {
            "Light" -> {
                // Drywall: Clean evenly spaced vertical lines
                val strokeW = 1.4f
                val lineCount = 5
                val step = w / (lineCount + 1)
                for (i in 1..lineCount) {
                    val x = i * step
                    drawLine(
                        color = color,
                        start = Offset(x, h * 0.16f),
                        end = Offset(x, h * 0.84f),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                }
            }

            "Medium" -> {
                // Brick: Running bond masonry with mortar courses and staggered vertical head joints
                val strokeW = 1.3f
                val rows = 3
                val rowH = h / rows

                // Horizontal mortar courses
                for (r in 1 until rows) {
                    val y = r * rowH
                    drawLine(
                        color = color,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = strokeW
                    )
                }

                // Staggered vertical head joints
                val cols = 3
                val colW = w / cols
                for (r in 0 until rows) {
                    val y1 = r * rowH
                    val y2 = (r + 1) * rowH
                    val offset = if (r % 2 == 0) 0f else colW * 0.5f

                    var x = offset
                    while (x < w) {
                        if (x > 2f && x < w - 2f) {
                            drawLine(
                                color = color,
                                start = Offset(x, y1),
                                end = Offset(x, y2),
                                strokeWidth = strokeW
                            )
                        }
                        x += colW
                    }
                }
            }

            "Heavy" -> {
                // Concrete: 45-degree diagonal structural cross-hatch lines
                val strokeW = 1.3f
                val spacing = 8.dp.toPx()
                var startX = -h
                while (startX < w + h) {
                    val p1 = Offset(startX, 0f)
                    val p2 = Offset(startX + h, h)
                    drawLine(
                        color = color,
                        start = p1,
                        end = p2,
                        strokeWidth = strokeW
                    )
                    startX += spacing
                }
            }
        }
    }
}

@Composable
private fun ImpactStatCard(
    title: String,
    value: String,
    unit: String,
    description: String,
    accentColor: Color = LocalDiurnalColors.current.accentColor,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DetailCardSurface)
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = JakartaFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = DetailTextSecondary
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = DetailTextPrimary
                    )
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = unit,
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = accentColor
                    ),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = TextStyle(
                    fontFamily = JakartaFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.5.sp,
                    color = DetailTextMuted
                )
            )
        }
    }
}
