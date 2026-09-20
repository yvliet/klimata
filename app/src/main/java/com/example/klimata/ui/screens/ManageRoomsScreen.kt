package com.example.klimata.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.ArrowsClockwise
import com.adamglin.phosphoricons.light.Check
import com.adamglin.phosphoricons.light.Cube
import com.adamglin.phosphoricons.light.MagnifyingGlass
import com.adamglin.phosphoricons.light.MapPin
import com.adamglin.phosphoricons.light.PencilSimple
import com.adamglin.phosphoricons.light.Plus
import com.adamglin.phosphoricons.light.Sparkle
import com.adamglin.phosphoricons.light.Trash
import com.adamglin.phosphoricons.light.X
import com.example.klimata.data.EnergyConfig
import com.example.klimata.data.LocationHelper
import com.example.klimata.data.RoomFactory
import com.example.klimata.data.RoomState
import com.example.klimata.data.engine.ThermalCalculationEngine
import com.example.klimata.data.storage.KlimataPreferences
import com.example.klimata.ui.components.AcCatalogBrowserSheet
import com.example.klimata.ui.components.AtmosphericSkyCanvas
import com.example.klimata.ui.components.NavigateBackButton
import com.example.klimata.ui.components.bouncyClickable
import com.example.klimata.ui.theme.DetailBlackBackground
import com.example.klimata.ui.theme.DetailCardBorder
import com.example.klimata.ui.theme.DetailCardSurface
import com.example.klimata.ui.theme.DetailCardSurfaceElevated
import com.example.klimata.ui.theme.DetailTextMuted
import com.example.klimata.ui.theme.DetailTextPrimary
import com.example.klimata.ui.theme.DetailTextSecondary
import com.example.klimata.ui.theme.DiurnalPhase
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.DaySkyStop1
import com.example.klimata.ui.theme.DaySkyStop2
import com.example.klimata.ui.theme.DaySkyStop3
import com.example.klimata.ui.theme.DaySkyStop4
import com.example.klimata.ui.theme.DaySkyStop5
import com.example.klimata.ui.theme.EveningSkyStop1
import com.example.klimata.ui.theme.EveningSkyStop2
import com.example.klimata.ui.theme.EveningSkyStop3
import com.example.klimata.ui.theme.EveningSkyStop4
import com.example.klimata.ui.theme.EveningSkyStop5
import com.example.klimata.ui.theme.LocalDiurnalColors
import com.example.klimata.ui.theme.NightSkyStop1
import com.example.klimata.ui.theme.NightSkyStop2
import com.example.klimata.ui.theme.NightSkyStop3
import com.example.klimata.ui.theme.NightSkyStop4
import com.example.klimata.ui.theme.NightSkyStop5
import com.example.klimata.ui.theme.OvercastSkyStop1
import com.example.klimata.ui.theme.OvercastSkyStop2
import com.example.klimata.ui.theme.OvercastSkyStop3
import com.example.klimata.ui.theme.OvercastSkyStop4
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Manage Rooms screen inspired by the HyperOS Weather city manager interface.
 * Displays atmospheric weather cards with interactive swipe-to-delete (left)
 * and swipe-to-edit (right) gestures, full search filter, and instant room selection.
 */
@Composable
fun ManageRoomsScreen(
    rooms: List<RoomState>,
    activeRoomId: String?,
    currentPhase: DiurnalPhase,
    onRoomSelected: (roomId: String) -> Unit,
    onRoomUpdated: (RoomState) -> Unit,
    onRoomDeleted: (roomId: String) -> Unit,
    onAddRoomClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val diurnal = LocalDiurnalColors.current
    var searchQuery by remember { mutableStateOf("") }
    var roomToEdit by remember { mutableStateOf<RoomState?>(null) }
    var roomToDelete by remember { mutableStateOf<RoomState?>(null) }

    val filteredRooms = remember(rooms, searchQuery) {
        if (searchQuery.isBlank()) {
            rooms
        } else {
            val query = searchQuery.trim().lowercase()
            rooms.filter {
                it.name.lowercase().contains(query) ||
                        it.location.lowercase().contains(query) ||
                        it.profile.brand.lowercase().contains(query) ||
                        it.profile.model.lowercase().contains(query)
            }
        }
    }

    val primaryRoom = filteredRooms.firstOrNull { it.id == activeRoomId } ?: filteredRooms.firstOrNull()
    val otherRooms = filteredRooms.filter { it.id != primaryRoom?.id }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DetailBlackBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Top Bar: Back button, Title "Manage rooms", and Add (+) button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigateBackButton(onClick = onBackClick)

                IconButton(
                    onClick = onAddRoomClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Light.Plus,
                        contentDescription = "Add Room",
                        tint = DetailTextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Manage rooms",
                style = TextStyle(
                    fontFamily = JakartaFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    letterSpacing = (-0.5).sp,
                    color = DetailTextPrimary
                ),
                modifier = Modifier.padding(start = 4.dp, bottom = 14.dp)
            )

            // Search Bar matching the design in user's reference image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DetailCardSurface)
                    .padding(horizontal = 14.dp, vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Light.MagnifyingGlass,
                        contentDescription = "Search",
                        tint = DetailTextMuted,
                        modifier = Modifier.size(18.dp)
                    )

                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Enter location or room name",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontSize = 14.sp,
                                    color = DetailTextMuted
                                )
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(
                            fontFamily = JakartaFamily,
                            fontSize = 14.sp,
                            color = DetailTextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Light.X,
                                contentDescription = "Clear search",
                                tint = DetailTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Rooms List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (primaryRoom != null) {
                    item(key = "header_current") {
                        Text(
                            text = "Current location",
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = DetailTextSecondary
                            ),
                            modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
                        )
                    }

                    item(key = primaryRoom.id) {
                        SwipeableAtmosphericRoomCard(
                            room = primaryRoom,
                            phase = currentPhase,
                            isPrimary = true,
                            onCardClick = { onRoomSelected(primaryRoom.id) },
                            onEditClick = { roomToEdit = primaryRoom },
                            onDeleteClick = { roomToDelete = primaryRoom }
                        )
                    }
                }

                if (otherRooms.isNotEmpty()) {
                    item(key = "header_added") {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Added locations",
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = DetailTextSecondary
                            ),
                            modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
                        )
                    }

                    items(otherRooms, key = { it.id }) { room ->
                        SwipeableAtmosphericRoomCard(
                            room = room,
                            phase = currentPhase,
                            isPrimary = false,
                            onCardClick = { onRoomSelected(room.id) },
                            onEditClick = { roomToEdit = room },
                            onDeleteClick = { roomToDelete = room }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }

        // Delete Room Confirmation Modal
        roomToDelete?.let { targetRoom ->
            AlertDialog(
                onDismissRequest = { roomToDelete = null },
                containerColor = DetailCardSurface,
                shape = RoundedCornerShape(22.dp),
                title = {
                    com.example.klimata.SyncDialogStatusBar()
                    Text(
                        text = "Remove Room?",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = DetailTextPrimary
                        )
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to remove \"${targetRoom.name}\"? Its cooling schedule and savings history will be erased.",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.5.sp,
                            color = DetailTextSecondary
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val id = targetRoom.id
                            roomToDelete = null
                            onRoomDeleted(id)
                        }
                    ) {
                        Text(
                            text = "Delete",
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { roomToDelete = null }) {
                        Text(
                            text = "Cancel",
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Medium,
                                color = DetailTextSecondary
                            )
                        )
                    }
                }
            )
        }

        // Edit Room Bottom Sheet / Overlay
        AnimatedVisibility(
            visible = roomToEdit != null,
            enter = fadeIn(tween(220)) + slideInVertically(
                animationSpec = tween(280, easing = FastOutSlowInEasing),
                initialOffsetY = { it }
            ),
            exit = fadeOut(tween(180)) + slideOutVertically(
                animationSpec = tween(240, easing = FastOutSlowInEasing),
                targetOffsetY = { it }
            )
        ) {
            roomToEdit?.let { targetRoom ->
                EditRoomSheet(
                    room = targetRoom,
                    onDismiss = { roomToEdit = null },
                    onSave = { updated ->
                        onRoomUpdated(updated)
                        roomToEdit = null
                    }
                )
            }
        }
    }
}

/**
 * Atmospheric weather card with interactive swipe actions:
 * - Swipe right to edit (reveals accent blue background with Pencil icon)
 * - Swipe left to delete (reveals red background with Trash icon)
 * - Tap to select and expand back to home
 */
@Composable
fun SwipeableAtmosphericRoomCard(
    room: RoomState,
    phase: DiurnalPhase,
    isPrimary: Boolean,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val dragOffset = remember { Animatable(0f) }
    val diurnal = LocalDiurnalColors.current

    val thresholdPx = 180f

    val cardShape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(102.dp)
            .clip(cardShape)
    ) {
        // Background revealed during swipe
        val currentOffset = dragOffset.value
        val isSwipingRight = currentOffset > 0
        val isSwipingLeft = currentOffset < 0

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    when {
                        isSwipingRight -> diurnal.accentColor.copy(alpha = 0.85f)
                        isSwipingLeft -> Color(0xFFEF4444)
                        else -> Color.Transparent
                    }
                )
                .padding(horizontal = 24.dp),
            contentAlignment = if (isSwipingRight) Alignment.CenterStart else Alignment.CenterEnd
        ) {
            if (isSwipingRight) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Light.PencilSimple,
                        contentDescription = "Edit",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Edit Room",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    )
                }
            } else if (isSwipingLeft) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Delete",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    )
                    Icon(
                        imageVector = PhosphorIcons.Light.Trash,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Atmospheric Sky Card Body
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(dragOffset.value.roundToInt(), 0) }
                .clip(cardShape)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val finalOffset = dragOffset.value
                            coroutineScope.launch {
                                if (finalOffset > thresholdPx) {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    dragOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                    onEditClick()
                                } else if (finalOffset < -thresholdPx) {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    dragOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                    onDeleteClick()
                                } else {
                                    dragOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                }
                            }
                        },
                        onDragCancel = {
                            coroutineScope.launch {
                                dragOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                val newOffset = (dragOffset.value + dragAmount).coerceIn(-280f, 280f)
                                dragOffset.snapTo(newOffset)
                            }
                        }
                    )
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCardClick
                )
        ) {
            // Sky gradient background matching reference screenshot
            AtmosphericCardBackground(phase = phase, condition = room.weatherCondition)

            // Content row matching reference image
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Room Name & Condition / Location
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = room.name,
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                letterSpacing = (-0.3).sp,
                                color = Color.White
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isPrimary) {
                            Icon(
                                imageVector = PhosphorIcons.Light.MapPin,
                                contentDescription = "Active Location",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${room.weatherCondition} | ${room.location}",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Right Column: Temperature & Setpoint / Range
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${room.currentTemp}°",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Light,
                            fontSize = 34.sp,
                            letterSpacing = (-0.5).sp,
                            color = Color.White
                        )
                    )

                    Text(
                        text = "Target ${room.targetTemp}°C",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.80f)
                        )
                    )
                }
            }
        }
    }
}

/**
 * Renders atmospheric sky gradient inside the card to mirror the reference weather cards.
 */
@Composable
private fun AtmosphericCardBackground(
    phase: DiurnalPhase,
    condition: String
) {
    val isOvercast = remember(condition) {
        condition.contains("Rain", ignoreCase = true) ||
        condition.contains("Overcast", ignoreCase = true) ||
        condition.contains("Storm", ignoreCase = true)
    }

    val brush = remember(phase, isOvercast) {
        if (isOvercast) {
            Brush.verticalGradient(
                colors = listOf(
                    OvercastSkyStop1,
                    OvercastSkyStop2,
                    OvercastSkyStop3,
                    OvercastSkyStop4
                )
            )
        } else {
            when (phase) {
                DiurnalPhase.DAY -> Brush.verticalGradient(
                    colors = listOf(
                        DaySkyStop1,
                        DaySkyStop2,
                        DaySkyStop3,
                        DaySkyStop4,
                        DaySkyStop5
                    )
                )
                DiurnalPhase.EVENING -> Brush.verticalGradient(
                    colors = listOf(
                        EveningSkyStop1,
                        EveningSkyStop2,
                        EveningSkyStop3,
                        EveningSkyStop4,
                        EveningSkyStop5
                    )
                )
                DiurnalPhase.NIGHT -> Brush.verticalGradient(
                    colors = listOf(
                        NightSkyStop1,
                        NightSkyStop2,
                        NightSkyStop3,
                        NightSkyStop4,
                        NightSkyStop5
                    )
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (phase == DiurnalPhase.DAY) 0.16f else 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(250f, 60f),
                        radius = 450f
                    )
                )
        )
    }
}

/**
 * Bottom sheet overlay for comprehensive room editing (dimensions, thermal mass, name, AC model).
 */
@Composable
private fun EditRoomSheet(
    room: RoomState,
    onDismiss: () -> Unit,
    onSave: (RoomState) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val diurnal = LocalDiurnalColors.current
    val coroutineScope = rememberCoroutineScope()

    var editedName by remember { mutableStateOf(room.name) }
    var editedAreaSqm by remember { mutableIntStateOf(room.areaSquareMeters) }
    var editedCeilingHeight by remember { mutableFloatStateOf(room.ceilingHeightMeters) }
    var editedThermalMass by remember { mutableStateOf(room.thermalMassLabel) }
    var editedLocation by remember { mutableStateOf(room.location) }

    var acBrand by remember { mutableStateOf(room.profile.brand) }
    var acModel by remember { mutableStateOf(room.profile.model) }
    var acCapacity by remember { mutableStateOf(room.profile.capacity) }
    var acInverterType by remember { mutableStateOf(room.profile.inverterType) }
    var acCodeSetId by remember { mutableStateOf(room.profile.irCodeSet) }

    var showCatalog by remember { mutableStateOf(false) }
    var isDetectingGps by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.70f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(DetailCardSurface)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Room",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = DetailTextPrimary
                    )
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Light.X,
                        contentDescription = "Close",
                        tint = DetailTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Name field
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Room Name",
                    style = TextStyle(fontFamily = JakartaFamily, fontSize = 12.sp, color = DetailTextMuted)
                )
                TextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DetailCardSurfaceElevated,
                        unfocusedContainerColor = DetailCardSurfaceElevated,
                        focusedIndicatorColor = diurnal.accentColor,
                        unfocusedIndicatorColor = DetailCardBorder
                    ),
                    textStyle = TextStyle(fontFamily = JakartaFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = DetailTextPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Dimensions sliders
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DetailCardSurfaceElevated)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Area & Volume",
                        style = TextStyle(fontFamily = JakartaFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DetailTextPrimary)
                    )
                    Text(
                        text = "$editedAreaSqm m² (${(editedAreaSqm * editedCeilingHeight).roundToInt()} m³)",
                        style = TextStyle(fontFamily = JakartaFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = diurnal.accentColor)
                    )
                }

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Floor Area", style = TextStyle(fontFamily = JakartaFamily, fontSize = 12.sp, color = DetailTextSecondary))
                        Text(text = "$editedAreaSqm m²", style = TextStyle(fontFamily = JakartaFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DetailTextPrimary))
                    }
                    Slider(
                        value = editedAreaSqm.toFloat(),
                        onValueChange = { editedAreaSqm = it.roundToInt() },
                        valueRange = 8f..80f,
                        colors = SliderDefaults.colors(thumbColor = diurnal.accentColor, activeTrackColor = diurnal.accentColor, inactiveTrackColor = DetailCardBorder)
                    )
                }

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Ceiling Height", style = TextStyle(fontFamily = JakartaFamily, fontSize = 12.sp, color = DetailTextSecondary))
                        Text(text = "%.1f m".format(editedCeilingHeight), style = TextStyle(fontFamily = JakartaFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DetailTextPrimary))
                    }
                    Slider(
                        value = editedCeilingHeight,
                        onValueChange = { editedCeilingHeight = (it * 10f).roundToInt() / 10f },
                        valueRange = 2.2f..4.0f,
                        colors = SliderDefaults.colors(thumbColor = diurnal.accentColor, activeTrackColor = diurnal.accentColor, inactiveTrackColor = DetailCardBorder)
                    )
                }
            }

            // Thermal Mass picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "Low" to "Low Thermal Mass",
                    "Medium" to "Medium Thermal Mass",
                    "High" to "High Thermal Inertia"
                ).forEach { (label, fullLabel) ->
                    val isSelected = editedThermalMass.contains(label, ignoreCase = true)
                    val shape = RoundedCornerShape(12.dp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .bouncyClickable(
                                shape = shape,
                                onClick = { editedThermalMass = fullLabel }
                            )
                            .clip(shape)
                            .background(if (isSelected) diurnal.accentColor.copy(alpha = 0.20f) else DetailCardSurfaceElevated)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) diurnal.accentColor else DetailTextPrimary
                            )
                        )
                    }
                }
            }

            // Location field with GPS button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = editedLocation,
                    onValueChange = { editedLocation = it },
                    placeholder = { Text("City location", style = TextStyle(fontFamily = JakartaFamily, fontSize = 13.sp, color = DetailTextMuted)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DetailCardSurfaceElevated,
                        unfocusedContainerColor = DetailCardSurfaceElevated,
                        focusedIndicatorColor = diurnal.accentColor,
                        unfocusedIndicatorColor = DetailCardBorder
                    ),
                    textStyle = TextStyle(fontFamily = JakartaFamily, fontSize = 13.5.sp, color = DetailTextPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .bouncyClickable(
                            shape = RoundedCornerShape(14.dp),
                            onClick = {
                                coroutineScope.launch {
                                    isDetectingGps = true
                                    val loc = LocationHelper.detectLocation(context)
                                    editedLocation = loc.cityName
                                    isDetectingGps = false
                                }
                            }
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(DetailCardSurfaceElevated)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Light.ArrowsClockwise,
                        contentDescription = "Sync GPS",
                        tint = diurnal.accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Save Action Button
            val saveShape = RoundedCornerShape(16.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .bouncyClickable(
                        shape = saveShape,
                        onClick = {
                            val finalLocation = editedLocation.ifBlank { room.location }
                            val energyConfig = EnergyConfig.resolveForLocation(cityName = finalLocation)
                            val cachedWeather = KlimataPreferences.loadWeather(context)
                            val computation = ThermalCalculationEngine.computeRoomThermalDynamics(
                                areaSquareMeters = editedAreaSqm,
                                ceilingHeightMeters = editedCeilingHeight,
                                thermalMassType = editedThermalMass,
                                acCapacity = acCapacity,
                                acInverterType = acInverterType,
                                targetTemp = room.targetTemp,
                                hourlyOutdoorTemps = cachedWeather?.hourlyTemps ?: emptyMap(),
                                energyConfig = energyConfig
                            )
                            val updated = room.copy(
                                name = editedName.ifBlank { room.name },
                                location = finalLocation,
                                areaSquareMeters = editedAreaSqm,
                                ceilingHeightMeters = editedCeilingHeight,
                                thermalMassLabel = computation.thermalMassLabel,
                                coolingLoadBtu = computation.coolingLoadBtu,
                                thermalSteps = computation.thermalSteps,
                                monthlySavings = computation.monthlySavings,
                                avoidedCarbon = computation.avoidedCarbon,
                                savingsHistory = computation.savingsHistory,
                                carbonHistory = computation.carbonHistory,
                                savingsBreakdown = computation.savingsBreakdown,
                                carbonEquivalence = computation.carbonEquivalence,
                                profile = room.profile.copy(
                                    roomName = editedName.ifBlank { room.name },
                                    brand = acBrand,
                                    model = acModel,
                                    capacity = acCapacity,
                                    inverterType = acInverterType,
                                    irCodeSet = acCodeSetId
                                )
                            )
                            onSave(updated)
                        }
                    )
                    .clip(saveShape)
                    .background(diurnal.accentColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Save Changes",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = diurnal.onAccent
                    )
                )
            }
        }
    }
}
