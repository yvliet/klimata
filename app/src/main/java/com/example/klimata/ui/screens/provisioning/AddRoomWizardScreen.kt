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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.adamglin.phosphoricons.light.Camera
import com.adamglin.phosphoricons.light.CaretLeft
import com.adamglin.phosphoricons.light.Check
import com.adamglin.phosphoricons.light.MapPin
import com.example.klimata.data.AcRecognitionService
import com.example.klimata.data.LocationHelper
import com.example.klimata.data.RoomFactory
import com.example.klimata.data.RoomState
import com.example.klimata.ui.components.bouncyClickable
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
    onRoomCreated: (RoomState) -> Unit = {}
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
    var acBrand by remember { mutableStateOf("Daikin") }
    var acModel by remember { mutableStateOf("FTKF25") }
    var acCapacity by remember { mutableStateOf("1.0 PK") }
    var acInverterType by remember { mutableStateOf("Eco Inverter") }
    var acPhotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isAnalyzingPhoto by remember { mutableStateOf(false) }

    // Location state
    var selectedLocation by remember { mutableStateOf("South Jakarta") }
    var isDetectingGps by remember { mutableStateOf(false) }
    var gpsStatusMessage by remember { mutableStateOf<String?>(null) }


    // GPS location detection logic
    fun runGpsDetection() {
        coroutineScope.launch {
            isDetectingGps = true
            gpsStatusMessage = "Checking GPS location..."
            val detected = LocationHelper.detectCity(context)
            if (!detected.isNullOrBlank()) {
                selectedLocation = detected
                gpsStatusMessage = "Location synchronized: $detected"
            } else {
                gpsStatusMessage = "Using selected city microclimate"
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
            location = selectedLocation
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DetailBlackBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
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
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DetailCardSurface)
                        .clickable { handleBack() }
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Light.CaretLeft,
                        contentDescription = "Back",
                        tint = DetailTextPrimary,
                        modifier = Modifier.size(18.dp)
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

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Suggestions",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 11.5.sp,
                                    color = DetailTextMuted
                                )
                            )

                            val suggestions = listOf("Master Bedroom", "Living Room", "Study Room", "Studio", "Guest Room")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        suggestions.take(3).forEach { name ->
                                            val isSelected = roomName == name
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSelected) diurnal.accentColor.copy(alpha = 0.20f) else DetailCardSurface)
                                                    .clickable { roomName = name }
                                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                                            ) {
                                                Text(
                                                    text = name,
                                                    style = TextStyle(
                                                        fontFamily = JakartaFamily,
                                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                                        fontSize = 12.5.sp,
                                                        color = if (isSelected) diurnal.accentColor else DetailTextPrimary
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        suggestions.drop(3).forEach { name ->
                                            val isSelected = roomName == name
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSelected) diurnal.accentColor.copy(alpha = 0.20f) else DetailCardSurface)
                                                    .clickable { roomName = name }
                                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                                            ) {
                                                Text(
                                                    text = name,
                                                    style = TextStyle(
                                                        fontFamily = JakartaFamily,
                                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                                        fontSize = 12.5.sp,
                                                        color = if (isSelected) diurnal.accentColor else DetailTextPrimary
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

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
                                text = "Size your room",
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
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (sizingMode == "presets") diurnal.accentColor else Color.Transparent)
                                        .clickable { sizingMode = "presets" }
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
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (sizingMode == "custom") diurnal.accentColor else Color.Transparent)
                                        .clickable { sizingMode = "custom" }
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
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSelected) diurnal.accentColor.copy(alpha = 0.20f) else DetailCardSurfaceElevated)
                                                    .clickable {
                                                        selectedPreset = label
                                                        areaSquareMeters = sqm
                                                        manualWidth = dims.first
                                                        manualLength = dims.second
                                                    }
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

                                PrimaryActionButton(
                                    label = "Confirm Dimensions ($areaSquareMeters m²)",
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
                                            Text(text = "  •  ", color = DetailTextMuted)
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
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(diurnal.accentColor.copy(alpha = 0.18f))
                                                .bouncyClickable {
                                                    isPolygonClosed = true
                                                }
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
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(DetailCardSurface)
                                                .bouncyClickable {
                                                    if (isPolygonClosed) {
                                                        isPolygonClosed = false
                                                    } else if (customVertices.isNotEmpty()) {
                                                        customVertices = customVertices.dropLast(1)
                                                    }
                                                }
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
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(DetailCardSurface)
                                                .bouncyClickable {
                                                    customVertices = emptyList()
                                                    isPolygonClosed = false
                                                }
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

                            // Wall Thermal Material Selection
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(DetailCardSurface)
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Wall Thermal Material",
                                    style = TextStyle(fontFamily = JakartaFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, color = DetailTextPrimary)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("Light\n(Drywall)" to "Light", "Medium\n(Brick)" to "Medium", "Heavy\n(Concrete)" to "Heavy").forEach { (label, type) ->
                                        val isSelected = thermalMass == type
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) diurnal.accentColor.copy(alpha = 0.20f) else DetailCardSurfaceElevated)
                                                .clickable { thermalMass = type }
                                                .padding(vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                style = TextStyle(
                                                    fontFamily = JakartaFamily,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 11.5.sp,
                                                    color = if (isSelected) diurnal.accentColor else DetailTextSecondary,
                                                    textAlign = TextAlign.Center
                                                )
                                            )
                                        }
                                    }
                                }
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
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(DetailCardSurface)
                                    .bouncyClickable {
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
                                    .clickable { galleryLauncher.launch("image/*") }
                                    .padding(vertical = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Skip and choose AC model manually",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = DetailTextMuted
                                ),
                                modifier = Modifier
                                    .clickable { currentStage = WizardStage.CONFIRM_AC }
                                    .padding(4.dp)
                            )
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
                                                text = assessment.status,
                                                style = TextStyle(
                                                    fontFamily = JakartaFamily,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 11.sp,
                                                    color = diurnal.accentColor
                                                )
                                            )
                                        }
                                    }

                                    Text(
                                        text = "$acCapacity • $acInverterType",
                                        style = TextStyle(
                                            fontFamily = JakartaFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = DetailTextSecondary
                                        )
                                    )

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
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(DetailCardSurface)
                                            .bouncyClickable {
                                                acBrand = when (acBrand) {
                                                    "Daikin" -> "Panasonic"
                                                    "Panasonic" -> "Mitsubishi"
                                                    "Mitsubishi" -> "LG"
                                                    else -> "Daikin"
                                                }
                                                acCapacity = when (acCapacity) {
                                                    "1.0 PK" -> "1.5 PK"
                                                    "1.5 PK" -> "0.75 PK"
                                                    else -> "1.0 PK"
                                                }
                                            }
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
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(diurnal.accentColor)
                                            .bouncyClickable {
                                                currentStage = WizardStage.LOCATION_SYNC
                                            }
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
                                                    .clip(CircleShape)
                                                    .background(DetailCardSurfaceElevated)
                                                    .clickable { runGpsDetection() }
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

                            // Quick city selector chips
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(DetailCardSurface)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "City Presets",
                                    style = TextStyle(
                                        fontFamily = JakartaFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = DetailTextSecondary
                                    )
                                )

                                val cities = listOf("South Jakarta", "Bandung", "Surabaya", "Singapore")
                                cities.forEach { city ->
                                    val isSelected = selectedLocation.equals(city, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) diurnal.accentColor.copy(alpha = 0.16f) else DetailCardSurfaceElevated)
                                            .clickable { selectedLocation = city }
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = city,
                                                style = TextStyle(
                                                    fontFamily = JakartaFamily,
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                    fontSize = 13.sp,
                                                    color = if (isSelected) diurnal.accentColor else DetailTextPrimary
                                                )
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = PhosphorIcons.Light.Check,
                                                    contentDescription = null,
                                                    tint = diurnal.accentColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
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
                                        text = "Rp 84.500",
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
                                        text = "$5.40 USD • -38% overnight kWh",
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
                                        value = "1.4",
                                        unit = "trees/mo",
                                        description = "CO₂ captured naturally",
                                        accentColor = diurnal.accentColor,
                                        modifier = Modifier.weight(1f)
                                    )
                                    ImpactStatCard(
                                        title = "Driving Avoided",
                                        value = "138",
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
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) accentColor else DetailCardSurfaceElevated)
            .bouncyClickable(enabled = enabled, onClick = onClick)
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
