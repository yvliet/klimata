package com.example.klimata.ui.screens.provisioning

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
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
import com.example.klimata.sensor.RoomWalkerSensorManager
import com.example.klimata.sensor.WalkerSnapshot
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
import com.example.klimata.ui.theme.MineralMintActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class WizardStage {
    NAME_ROOM,
    MAP_PERIMETER,
    CONSTRUCTION_ANIMATION,
    CAPTURE_AC,
    CONFIRM_AC,
    LOCATION_SYNC,
    IMPACT_REVEAL
}

@Composable
fun AddRoomWizardScreen(
    isOnboarding: Boolean = true,
    onBackClick: () -> Unit = {},
    onRoomCreated: (RoomState) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var currentStage by remember { mutableStateOf(WizardStage.NAME_ROOM) }

    // Room specification state
    var roomName by remember { mutableStateOf("Master Bedroom") }
    var areaSquareMeters by remember { mutableIntStateOf(20) }
    var ceilingHeight by remember { mutableFloatStateOf(2.8f) }
    var thermalMass by remember { mutableStateOf("Medium") }
    var manualLength by remember { mutableFloatStateOf(5.0f) }
    var manualWidth by remember { mutableFloatStateOf(4.0f) }

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

    // Sensor walker reactive state
    var walkerSnapshot by remember { mutableStateOf(WalkerSnapshot()) }
    val walkerManager = remember {
        RoomWalkerSensorManager(context = context) { snap ->
            walkerSnapshot = snap
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            walkerManager.stopTracking()
        }
    }

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
                        .clickable {
                            when (currentStage) {
                                WizardStage.NAME_ROOM -> onBackClick()
                                WizardStage.MAP_PERIMETER -> currentStage = WizardStage.NAME_ROOM
                                WizardStage.CONSTRUCTION_ANIMATION -> currentStage = WizardStage.MAP_PERIMETER
                                WizardStage.CAPTURE_AC -> currentStage = WizardStage.MAP_PERIMETER
                                WizardStage.CONFIRM_AC -> currentStage = WizardStage.CAPTURE_AC
                                WizardStage.LOCATION_SYNC -> currentStage = WizardStage.CONFIRM_AC
                                WizardStage.IMPACT_REVEAL -> currentStage = WizardStage.LOCATION_SYNC
                            }
                        }
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
                                    focusedIndicatorColor = MineralMintActive,
                                    unfocusedIndicatorColor = DetailCardBorder,
                                    cursorColor = MineralMintActive
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
                                                    .background(if (isSelected) MineralMintActive.copy(alpha = 0.20f) else DetailCardSurface)
                                                    .clickable { roomName = name }
                                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                                            ) {
                                                Text(
                                                    text = name,
                                                    style = TextStyle(
                                                        fontFamily = JakartaFamily,
                                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                                        fontSize = 12.5.sp,
                                                        color = if (isSelected) MineralMintActive else DetailTextPrimary
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
                                                    .background(if (isSelected) MineralMintActive.copy(alpha = 0.20f) else DetailCardSurface)
                                                    .clickable { roomName = name }
                                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                                            ) {
                                                Text(
                                                    text = name,
                                                    style = TextStyle(
                                                        fontFamily = JakartaFamily,
                                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                                        fontSize = 12.5.sp,
                                                        color = if (isSelected) MineralMintActive else DetailTextPrimary
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
                                    walkerManager.startTracking()
                                    currentStage = WizardStage.MAP_PERIMETER
                                }
                            )
                        }

                        // Stage 2: Hybrid Smart Walker
                        WizardStage.MAP_PERIMETER -> {
                            var mappingMode by remember { mutableStateOf("walk") }

                            Text(
                                text = "Map room boundaries",
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
                                text = if (mappingMode == "walk") {
                                    "Walk along walls or tap corner buttons to plot your room."
                                } else {
                                    "Select dimensions with precision sliders."
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
                                        .background(if (mappingMode == "walk") MineralMintActive else Color.Transparent)
                                        .clickable {
                                            mappingMode = "walk"
                                            walkerManager.startTracking()
                                        }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Smart Walker",
                                        style = TextStyle(
                                            fontFamily = JakartaFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = if (mappingMode == "walk") Color(0xFF0F172A) else DetailTextSecondary
                                        )
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (mappingMode == "manual") MineralMintActive else Color.Transparent)
                                        .clickable {
                                            mappingMode = "manual"
                                            walkerManager.stopTracking()
                                        }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Dimension Sliders",
                                        style = TextStyle(
                                            fontFamily = JakartaFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = if (mappingMode == "manual") Color(0xFF0F172A) else DetailTextSecondary
                                        )
                                    )
                                }
                            }

                            if (mappingMode == "walk") {
                                // Crisp Architectural Radar
                                WalkPerimeterCanvas(
                                    corners = walkerSnapshot.corners,
                                    walkedCorners = walkerSnapshot.walkedCorners,
                                    currentWalkerPos = walkerSnapshot.currentPosition,
                                    currentHeadingDeg = walkerSnapshot.currentHeadingDeg,
                                    isClosed = walkerSnapshot.corners.size >= 4,
                                    modifier = Modifier
                                        .size(240.dp)
                                        .padding(vertical = 4.dp)
                                )

                                // Real-time reactive metrics readout
                                val currentArea = walkerSnapshot.calculatedAreaM2.coerceAtLeast(12)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${walkerSnapshot.totalSteps} steps walked",
                                        style = TextStyle(
                                            fontFamily = JakartaFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 12.5.sp,
                                            color = DetailTextSecondary
                                        )
                                    )
                                    Text(text = " • ", color = DetailTextMuted)
                                    Text(
                                        text = "~$currentArea m² estimated",
                                        style = TextStyle(
                                            fontFamily = JakartaFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MineralMintActive
                                        )
                                    )
                                }

                                // Active wall distance fine-tuning row
                                val activeDist = walkerSnapshot.currentWallDistanceMeters.takeIf { it > 0.1f } ?: 4.0f
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(DetailCardSurface)
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Wall: ${String.format(java.util.Locale.US, "%.1f", activeDist)} m",
                                        style = TextStyle(fontFamily = JakartaFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = DetailTextPrimary)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(DetailCardSurfaceElevated)
                                            .clickable { walkerManager.adjustCurrentWall(-0.5f) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "-0.5m", style = TextStyle(fontFamily = JakartaFamily, fontSize = 11.sp, color = DetailTextSecondary))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(DetailCardSurfaceElevated)
                                            .clickable { walkerManager.adjustCurrentWall(0.5f) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "+0.5m", style = TextStyle(fontFamily = JakartaFamily, fontSize = 11.sp, color = MineralMintActive))
                                    }
                                }

                                // Interactive action buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val cornerCount = walkerSnapshot.corners.size
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(DetailCardSurface)
                                            .bouncyClickable {
                                                walkerSnapshot = walkerManager.markCorner()
                                                areaSquareMeters = walkerSnapshot.calculatedAreaM2.coerceIn(10, 80)
                                            }
                                    ) {
                                        Text(
                                            text = "Mark Corner ($cornerCount)",
                                            style = TextStyle(
                                                fontFamily = JakartaFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = DetailTextPrimary
                                            )
                                        )
                                    }

                                    val isReadyToFinish = walkerSnapshot.corners.size >= 3
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (isReadyToFinish) MineralMintActive else DetailCardSurfaceElevated)
                                            .bouncyClickable(enabled = isReadyToFinish) {
                                                val area = walkerManager.finishRoom()
                                                areaSquareMeters = area.coerceIn(10, 80)
                                                currentStage = WizardStage.CONSTRUCTION_ANIMATION
                                            }
                                    ) {
                                        Text(
                                            text = "Finish Room",
                                            style = TextStyle(
                                                fontFamily = JakartaFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isReadyToFinish) Color(0xFF0F172A) else DetailTextMuted
                                            )
                                        )
                                    }
                                }
                            } else {
                                // Manual sliders panel
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
                                        listOf("Compact\n12 m²" to 12, "Standard\n20 m²" to 20, "Spacious\n35 m²" to 35).forEach { (lbl, sqm) ->
                                            val isSelected = areaSquareMeters == sqm
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSelected) MineralMintActive.copy(alpha = 0.20f) else DetailCardSurfaceElevated)
                                                    .clickable {
                                                        areaSquareMeters = sqm
                                                        manualLength = (sqm * 0.55f).coerceAtLeast(3f)
                                                        manualWidth = (sqm / manualLength)
                                                    }
                                                    .padding(vertical = 10.dp)
                                            ) {
                                                Text(
                                                    text = lbl,
                                                    style = TextStyle(
                                                        fontFamily = JakartaFamily,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        fontSize = 12.sp,
                                                        color = if (isSelected) MineralMintActive else DetailTextPrimary,
                                                        textAlign = TextAlign.Center
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    Column {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(text = "Width", style = TextStyle(fontFamily = JakartaFamily, fontSize = 12.sp, color = DetailTextSecondary))
                                            Text(text = "${String.format(java.util.Locale.US, "%.1f", manualWidth)} m", style = TextStyle(fontFamily = JakartaFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DetailTextPrimary))
                                        }
                                        Slider(
                                            value = manualWidth,
                                            onValueChange = {
                                                manualWidth = it
                                                areaSquareMeters = (manualWidth * manualLength).roundToInt().coerceIn(8, 80)
                                            },
                                            valueRange = 2.5f..8.0f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = MineralMintActive,
                                                activeTrackColor = MineralMintActive,
                                                inactiveTrackColor = DetailCardBorder
                                            )
                                        )
                                    }

                                    Column {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(text = "Length", style = TextStyle(fontFamily = JakartaFamily, fontSize = 12.sp, color = DetailTextSecondary))
                                            Text(text = "${String.format(java.util.Locale.US, "%.1f", manualLength)} m", style = TextStyle(fontFamily = JakartaFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DetailTextPrimary))
                                        }
                                        Slider(
                                            value = manualLength,
                                            onValueChange = {
                                                manualLength = it
                                                areaSquareMeters = (manualWidth * manualLength).roundToInt().coerceIn(8, 80)
                                            },
                                            valueRange = 2.5f..10.0f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = MineralMintActive,
                                                activeTrackColor = MineralMintActive,
                                                inactiveTrackColor = DetailCardBorder
                                            )
                                        )
                                    }
                                }

                                PrimaryActionButton(
                                    label = "Confirm Dimensions ($areaSquareMeters m²)",
                                    onClick = { currentStage = WizardStage.CONSTRUCTION_ANIMATION }
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
                                                .background(if (isSelected) MineralMintActive.copy(alpha = 0.20f) else DetailCardSurfaceElevated)
                                                .clickable { thermalMass = type }
                                                .padding(vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                style = TextStyle(
                                                    fontFamily = JakartaFamily,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 11.5.sp,
                                                    color = if (isSelected) MineralMintActive else DetailTextSecondary,
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
                                areaM2 = areaSquareMeters,
                                heightM = ceilingHeight,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )

                            val volume = (areaSquareMeters * ceilingHeight).roundToInt()
                            Text(
                                text = "$areaSquareMeters m² Floor Area • $volume m³ Air Volume",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MineralMintActive,
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
                                            .background(MineralMintActive.copy(alpha = 0.16f))
                                    ) {
                                        Icon(
                                            imageVector = PhosphorIcons.Light.Camera,
                                            contentDescription = "Take Photo",
                                            tint = MineralMintActive,
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
                                        color = MineralMintActive,
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
                                                .background(MineralMintActive.copy(alpha = 0.16f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = assessment.status,
                                                style = TextStyle(
                                                    fontFamily = JakartaFamily,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 11.sp,
                                                    color = MineralMintActive
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
                                            .background(MineralMintActive)
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
                                                color = Color(0xFF0F172A)
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
                                                tint = MineralMintActive,
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
                                                color = MineralMintActive,
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
                                                    tint = MineralMintActive,
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
                                            .background(if (isSelected) MineralMintActive.copy(alpha = 0.16f) else DetailCardSurfaceElevated)
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
                                                    color = if (isSelected) MineralMintActive else DetailTextPrimary
                                                )
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = PhosphorIcons.Light.Check,
                                                    contentDescription = null,
                                                    tint = MineralMintActive,
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
                                            color = MineralMintActive
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
                                        modifier = Modifier.weight(1f)
                                    )
                                    ImpactStatCard(
                                        title = "Driving Avoided",
                                        value = "138",
                                        unit = "km",
                                        description = "Combustion tailpipe offset",
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
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) MineralMintActive else DetailCardSurfaceElevated)
            .bouncyClickable(enabled = enabled, onClick = onClick)
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = JakartaFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (enabled) Color(0xFF0F172A) else DetailTextMuted
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
                        color = MineralMintActive
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
