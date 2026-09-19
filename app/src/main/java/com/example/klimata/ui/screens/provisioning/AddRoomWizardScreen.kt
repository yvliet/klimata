package com.example.klimata.ui.screens.provisioning

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.core.content.FileProvider
import com.example.klimata.data.models.AcDatabase
import com.example.klimata.data.models.AcModelInfo
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
import androidx.compose.ui.graphics.asImageBitmap
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
import com.adamglin.phosphoricons.light.Broadcast
import com.adamglin.phosphoricons.light.Camera
import com.adamglin.phosphoricons.light.Check
import com.adamglin.phosphoricons.light.MapPin
import com.adamglin.phosphoricons.light.Moon
import com.adamglin.phosphoricons.light.Power
import com.adamglin.phosphoricons.light.Sparkle
import com.example.klimata.data.AcRecognitionService
import com.example.klimata.data.LocationHelper
import com.example.klimata.data.RoomFactory
import com.example.klimata.data.RoomState
import com.example.klimata.data.ir.IrBlasterService
import com.example.klimata.ui.components.AcCatalogBrowserSheet
import com.example.klimata.ui.components.AcRemotePairingModal
import com.example.klimata.ui.components.NavigateBackButton
import com.example.klimata.ui.theme.MineralMintActive
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

    var roomName by remember { mutableStateOf("Master Bedroom") }
    var areaSquareMeters by remember { mutableIntStateOf(20) }
    var ceilingHeight by remember { mutableFloatStateOf(2.8f) }
    var thermalMass by remember { mutableStateOf("Medium") }
    var manualLength by remember { mutableFloatStateOf(5.0f) }
    var manualWidth by remember { mutableFloatStateOf(4.0f) }
    var sizingMode by remember { mutableStateOf("presets") }
    var selectedPreset by remember { mutableStateOf<String?>("Standard") }

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

    var acBrand by remember { mutableStateOf("Sharp") }
    var acModel by remember { mutableStateOf("AH-XP10") }
    var acCapacity by remember { mutableStateOf("1.0 PK") }
    var acInverterType by remember { mutableStateOf("J-Tech Inverter") }
    var acPhotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isAnalyzingPhoto by remember { mutableStateOf(false) }
    var showModelPicker by remember { mutableStateOf(false) }
    var showPairingModal by remember { mutableStateOf(false) }
    var selectedCodeSetId by remember { mutableStateOf<String?>(null) }
    var tempPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }

    var selectedLocation by remember { mutableStateOf("South Jakarta") }
    var liveWeatherReport by remember { mutableStateOf<AmbientWeatherReport?>(null) }
    var isDetectingGps by remember { mutableStateOf(false) }
    var gpsStatusMessage by remember { mutableStateOf<String?>(null) }

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
                gpsStatusMessage = "${report.condition} | ${report.currentOutdoorTemp}°C | AQI ${report.aqiValue} (${report.aqiLabel})"
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
                    val result = AcRecognitionService.analyzeAcPhoto(bmp, areaSquareMeters, context = context)
                    acBrand = result.brand
                    acModel = result.model
                    acCapacity = result.capacity
                    acInverterType = result.inverterType
                    isAnalyzingPhoto = false
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = tempPhotoUri
        if (success && uri != null) {
            val bmp = try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                    android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.isMutableRequired = true
                    }
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
                    val result = AcRecognitionService.analyzeAcPhoto(bmp, areaSquareMeters, context = context)
                    acBrand = result.brand
                    acModel = result.model
                    acCapacity = result.capacity
                    acInverterType = result.inverterType
                    isAnalyzingPhoto = false
                }
            }
        }
    }

    fun launchHighResCamera() {
        try {
            val photoFile = java.io.File.createTempFile("klimata_ac_", ".jpg", context.cacheDir)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            tempPhotoUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            android.util.Log.e("AddRoomWizard", "Failed to launch camera via FileProvider", e)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchHighResCamera()
        } else {
            android.util.Log.w("AddRoomWizard", "Camera permission denied")
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
            weatherCondition = liveWeatherReport?.condition ?: "Clear Night",
            irCodeSet = selectedCodeSetId
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigateBackButton(
                    onClick = { handleBack() },
                    contentDescription = "Back"
                )

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
                                text = "Name this space to personalize your overnight cooling schedule.",
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

                        WizardStage.ROOM_SIZING -> {
                            Text(
                                text = "Set room dimensions",
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
                                    "Adjust dimensions to visualize your room in 3D."
                                } else {
                                    "Tap corners on the grid to plot your custom floor plan."
                                },
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = DetailTextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            )

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(DetailCardSurface)
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val tabShape = RoundedCornerShape(10.dp)
                                Box(
                                    modifier = Modifier
                                        .bouncyClickable(
                                            shape = tabShape,
                                            onClick = { sizingMode = "presets" }
                                        )
                                        .clip(tabShape)
                                        .background(if (sizingMode == "presets") diurnal.accentColor else Color.Transparent)
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Standard",
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
                                            shape = tabShape,
                                            onClick = { sizingMode = "custom" }
                                        )
                                        .clip(tabShape)
                                        .background(if (sizingMode == "custom") diurnal.accentColor else Color.Transparent)
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Floorplan",
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
                                PresetRoomCanvas(
                                    widthMeters = manualWidth,
                                    lengthMeters = manualLength,
                                    ceilingHeightMeters = ceilingHeight,
                                    accentColor = diurnal.accentColor,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(210.dp)
                                )

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
                                            val presetShape = RoundedCornerShape(12.dp)
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .bouncyClickable(
                                                        shape = presetShape,
                                                        onClick = {
                                                            selectedPreset = label
                                                            areaSquareMeters = sqm
                                                            manualWidth = dims.first
                                                            manualLength = dims.second
                                                        }
                                                    )
                                                    .clip(presetShape)
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

                                    if (!isPolygonClosed && customVertices.size >= 3) {
                                        val closeShape = RoundedCornerShape(12.dp)
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp)
                                                .bouncyClickable(
                                                    shape = closeShape,
                                                    onClick = { isPolygonClosed = true }
                                                )
                                                .clip(closeShape)
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
                                        val actionShape = RoundedCornerShape(12.dp)
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .bouncyClickable(
                                                    shape = actionShape,
                                                    onClick = {
                                                        if (isPolygonClosed) {
                                                            isPolygonClosed = false
                                                        } else if (customVertices.isNotEmpty()) {
                                                            customVertices = customVertices.dropLast(1)
                                                        }
                                                    }
                                                )
                                                .clip(actionShape)
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

                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .bouncyClickable(
                                                    shape = actionShape,
                                                    onClick = {
                                                        customVertices = emptyList()
                                                        isPolygonClosed = false
                                                    }
                                                )
                                                .clip(actionShape)
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

                        WizardStage.CONSTRUCTION_ANIMATION -> {
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Building room wireframe",
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
                                text = "Calculating thermal mass and room volume...",
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
                                label = "Continue to AC Setup",
                                onClick = { currentStage = WizardStage.CAPTURE_AC }
                            )
                        }

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
                                        text = "Identify your AC unit",
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
                                        text = "Photograph your indoor AC unit or its model label.",
                                        style = TextStyle(
                                            fontFamily = JakartaFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 13.sp,
                                            color = DetailTextSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                    )

                                    val cameraShape = RoundedCornerShape(22.dp)
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .bouncyClickable(
                                                shape = cameraShape,
                                                onClick = {
                                                    val hasCameraPermission = ContextCompat.checkSelfPermission(
                                                        context,
                                                        Manifest.permission.CAMERA
                                                    ) == PackageManager.PERMISSION_GRANTED

                                                    if (hasCameraPermission) {
                                                        launchHighResCamera()
                                                    } else {
                                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                                    }
                                                }
                                            )
                                            .clip(cameraShape)
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
                                                text = "Take Photo of AC",
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
                                        text = "or choose from photo library",
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

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .bouncyClickable(
                                            shape = RoundedCornerShape(14.dp),
                                            onClick = { showModelPicker = true }
                                        )
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(DetailCardSurface)
                                        .padding(vertical = 14.dp, horizontal = 16.dp)
                                ) {
                                    Text(
                                        text = "Select AC model from catalog",
                                        style = TextStyle(
                                            fontFamily = JakartaFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.5.sp,
                                            color = DetailTextPrimary
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        WizardStage.CONFIRM_AC -> {
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (isAnalyzingPhoto) "Analyzing photo..." else "Confirm AC hardware",
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
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
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

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = acInverterType,
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
                                            text = "Hardware IR Ready",
                                            style = TextStyle(
                                                fontFamily = JakartaFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 13.sp,
                                                color = diurnal.accentColor
                                            )
                                        )
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "Cooling Capacity",
                                            style = TextStyle(
                                                fontFamily = JakartaFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.5.sp,
                                                color = DetailTextMuted
                                            )
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf("0.5 PK", "0.75 PK", "1.0 PK", "1.5 PK", "2.0 PK").forEach { cap ->
                                                val isCapSelected = acCapacity == cap
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .bouncyClickable(
                                                            shape = RoundedCornerShape(8.dp),
                                                            onClick = { acCapacity = cap }
                                                        )
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isCapSelected) diurnal.accentColor.copy(alpha = 0.20f) else DetailCardSurfaceElevated)
                                                        .padding(vertical = 7.dp)
                                                ) {
                                                    Text(
                                                        text = cap,
                                                        style = TextStyle(
                                                            fontFamily = JakartaFamily,
                                                            fontWeight = if (isCapSelected) FontWeight.Bold else FontWeight.Medium,
                                                            fontSize = 11.sp,
                                                            color = if (isCapSelected) diurnal.accentColor else DetailTextSecondary
                                                        )
                                                    )
                                                }
                                            }
                                        }
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

                                val irBlasterService = remember { IrBlasterService(context) }
                                val effectiveCodeSetId = selectedCodeSetId ?: RoomFactory.resolveDefaultCodeSet(acBrand, acModel)
                                val brandCodeSets = remember(acBrand) { irBlasterService.getCodeSetsForBrand(acBrand) }
                                val activeCodeSet = brandCodeSets.find { it.id == effectiveCodeSetId } ?: brandCodeSets.firstOrNull()

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(DetailCardSurface)
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "IR Remote Signal",
                                            style = TextStyle(
                                                fontFamily = JakartaFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = DetailTextPrimary
                                            )
                                        )

                                        Text(
                                            text = activeCodeSet?.displayName ?: "Signal 1",
                                            style = TextStyle(
                                                fontFamily = JakartaFamily,
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 12.sp,
                                                color = DetailTextSecondary
                                            )
                                        )
                                    }

                                    Text(
                                        text = activeCodeSet?.description ?: "Verified protocol timing for your hardware.",
                                        style = TextStyle(
                                            fontFamily = JakartaFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            color = DetailTextSecondary
                                        )
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(42.dp)
                                                .bouncyClickable(
                                                    shape = RoundedCornerShape(12.dp),
                                                    onClick = {
                                                        coroutineScope.launch {
                                                             irBlasterService.dispatchTestPulse(
                                                                brand = acBrand,
                                                                codeSetId = effectiveCodeSetId,
                                                                power = true
                                                            )
                                                        }
                                                    }
                                                )
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(DetailCardSurfaceElevated)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = PhosphorIcons.Light.Power,
                                                    contentDescription = null,
                                                    tint = diurnal.accentColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "Test Power",
                                                    style = TextStyle(
                                                        fontFamily = JakartaFamily,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 12.sp,
                                                        color = DetailTextPrimary
                                                    )
                                                )
                                            }
                                        }

                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1.3f)
                                                .height(42.dp)
                                                .bouncyClickable(
                                                    shape = RoundedCornerShape(12.dp),
                                                    onClick = { showPairingModal = true }
                                                )
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(diurnal.accentColor.copy(alpha = 0.15f))
                                        ) {
                                            Text(
                                                text = "Pair Remote (${brandCodeSets.size} Signals)",
                                                style = TextStyle(
                                                    fontFamily = JakartaFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = diurnal.accentColor
                                                )
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = PhosphorIcons.Light.Broadcast,
                                            contentDescription = null,
                                            tint = diurnal.accentColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "Please point phone top towards AC receiver",
                                            style = TextStyle(
                                                fontFamily = JakartaFamily,
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 11.5.sp,
                                                color = DetailTextSecondary
                                            )
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val navBtnShape = RoundedCornerShape(14.dp)
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .bouncyClickable(
                                                shape = navBtnShape,
                                                onClick = { showModelPicker = true }
                                            )
                                            .clip(navBtnShape)
                                            .background(DetailCardSurface)
                                    ) {
                                        Text(
                                            text = "Browse Catalog",
                                            style = TextStyle(
                                                fontFamily = JakartaFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = DetailTextPrimary
                                            )
                                        )
                                    }

                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .height(48.dp)
                                            .bouncyClickable(
                                                shape = navBtnShape,
                                                onClick = {
                                                    currentStage = WizardStage.LOCATION_SYNC
                                                }
                                            )
                                            .clip(navBtnShape)
                                            .background(diurnal.accentColor)
                                    ) {
                                        Text(
                                            text = "Confirm & Continue",
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

                        WizardStage.LOCATION_SYNC -> {
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Microclimate sync",
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
                                text = "Klimata balances indoor thermal dynamics against outdoor midnight weather patterns.",
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
                                                text = "Local Microclimate: $selectedLocation",
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
                                                    .clip(CircleShape)
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
                                label = if (isOnboarding) "Generate Thermal Schedule" else "Save Room",
                                onClick = {
                                    if (isOnboarding) {
                                        currentStage = WizardStage.IMPACT_REVEAL
                                    } else {
                                        completeAndSaveRoom()
                                    }
                                }
                            )
                        }

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
                                text = "Projected overnight savings",
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

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(DetailCardSurface)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = PhosphorIcons.Light.Moon,
                                        contentDescription = null,
                                        tint = diurnal.accentColor,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Text(
                                        text = "Overnight Setup",
                                        style = TextStyle(
                                            fontFamily = JakartaFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = DetailTextPrimary
                                        )
                                    )
                                }

                                listOf(
                                    Triple(
                                        PhosphorIcons.Light.Broadcast,
                                        "Bedside Placement",
                                        "Keep your phone on your nightstand facing the AC unit."
                                    ),
                                    Triple(
                                        PhosphorIcons.Light.Sparkle,
                                        "Nighttime Adjustments",
                                        "Temperature automatically adjusts from 22:00 to 06:00 while you sleep."
                                    )
                                ).forEach { (icon, title, desc) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(DetailCardSurfaceElevated)
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = diurnal.accentColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = title,
                                                style = TextStyle(
                                                    fontFamily = JakartaFamily,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp,
                                                    color = DetailTextPrimary
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = desc,
                                                style = TextStyle(
                                                    fontFamily = JakartaFamily,
                                                    fontWeight = FontWeight.Normal,
                                                    fontSize = 11.5.sp,
                                                    color = DetailTextPrimary,
                                                    lineHeight = 15.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            PrimaryActionButton(
                                label = "Finish Setup",
                                onClick = { completeAndSaveRoom() }
                            )
                        }
                    }
                }
            }
        }

        BackHandler(enabled = showModelPicker) {
            showModelPicker = false
        }

        AnimatedVisibility(
            visible = showModelPicker,
            enter = fadeIn(tween(200)) + slideInVertically(
                animationSpec = tween(280, easing = FastOutSlowInEasing),
                initialOffsetY = { fullHeight -> fullHeight }
            ),
            exit = fadeOut(tween(180)) + slideOutVertically(
                animationSpec = tween(240, easing = FastOutSlowInEasing),
                targetOffsetY = { fullHeight -> fullHeight }
            )
        ) {
            AcCatalogBrowserSheet(
                onDismiss = { showModelPicker = false },
                onModelSelected = { selected ->
                    acBrand = selected.brand
                    acModel = selected.modelCode
                    acCapacity = selected.defaultCapacity
                    acInverterType = selected.inverterType
                    selectedCodeSetId = RoomFactory.resolveDefaultCodeSet(selected.brand, selected.modelCode)
                    showModelPicker = false
                    // Advance past the photo capture stage — catalog pick is sufficient identification
                    if (currentStage == WizardStage.CAPTURE_AC) {
                        currentStage = WizardStage.CONFIRM_AC
                    }
                },
                accentColor = diurnal.accentColor
            )
        }

        if (showPairingModal) {
            AcRemotePairingModal(
                brand = acBrand,
                currentCodeSetId = selectedCodeSetId ?: RoomFactory.resolveDefaultCodeSet(acBrand, acModel),
                onCodeSetSelected = { codeSetId ->
                    selectedCodeSetId = codeSetId
                },
                onDismiss = { showPairingModal = false }
            )
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
    val buttonShape = RoundedCornerShape(16.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .bouncyClickable(
                enabled = enabled,
                shape = buttonShape,
                onClick = onClick
            )
            .clip(buttonShape)
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
                        .clip(cardShape)
                        .background(if (isSelected) accentColor.copy(alpha = 0.18f) else DetailCardSurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
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

