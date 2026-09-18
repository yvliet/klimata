package com.example.klimata.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.example.klimata.data.RoomState
import com.example.klimata.ui.components.AcControlPanel
import com.example.klimata.ui.components.AmbientTopBarActions
import com.example.klimata.ui.components.AtmosphericSkyCanvas
import com.example.klimata.ui.components.EmptyRoomsDoodlePrompt
import com.example.klimata.ui.components.HeroForegroundClouds
import com.example.klimata.ui.components.HeroTemperatureDisplay
import com.example.klimata.ui.components.ImpactLedgerGrid
import com.example.klimata.ui.components.RoomIndicator
import com.example.klimata.ui.components.RoomThermalVisualizerCard
import com.example.klimata.ui.components.ScheduleChart
import com.example.klimata.ui.theme.DiurnalPhase
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.KlimataTheme
import com.example.klimata.ui.theme.LocalDiurnalColors
import com.example.klimata.ui.theme.currentDiurnalPhase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Main application screen.
 */
@Composable
fun KlimataScreen(
    modifier: Modifier = Modifier,
    phase: DiurnalPhase = currentDiurnalPhase(),
    minuteOfDay: Int? = null,
    onPhaseChange: (DiurnalPhase) -> Unit = {},
    rooms: List<RoomState> = emptyList(),
    weatherReport: com.example.klimata.data.network.AmbientWeatherReport? = null,
    onPowerToggle: (roomId: String, isPowerOn: Boolean) -> Unit = { _, _ -> },
    onEcoToggle: (roomId: String, isEnabled: Boolean) -> Unit = { _, _ -> },
    onTempChange: (roomId: String, setpoint: Int) -> Unit = { _, _ -> },
    onModeChange: (roomId: String, mode: String) -> Unit = { _, _ -> },
    onFanSpeedChange: (roomId: String, fanSpeed: String) -> Unit = { _, _ -> },
    onSwingToggle: (roomId: String, isSwingEnabled: Boolean) -> Unit = { _, _ -> },
    onCalibrateRemote: (roomId: String) -> Unit = {},
    onScheduleClick: (roomId: String) -> Unit = {},
    onThermalClick: (roomId: String) -> Unit = {},
    onSavingsClick: (roomId: String) -> Unit = {},
    onCarbonClick: (roomId: String) -> Unit = {},
    onAcUnitClick: (roomId: String) -> Unit = {},
    onAddRoomClick: () -> Unit = {},
    onManageRoomsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    activeRoomId: String? = null,
    onActiveRoomChange: (String) -> Unit = {},
) {
    val initialRoomPage = remember(rooms, activeRoomId) {
        val idx = rooms.indexOfFirst { it.id == activeRoomId }
        if (idx >= 0) idx else 0
    }
    val pagerState = rememberPagerState(initialPage = initialRoomPage, pageCount = { rooms.size.coerceAtLeast(1) })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(activeRoomId) {
        if (activeRoomId != null) {
            val targetIdx = rooms.indexOfFirst { it.id == activeRoomId }
            if (targetIdx >= 0 && targetIdx != pagerState.currentPage) {
                pagerState.scrollToPage(targetIdx)
            }
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (rooms.isNotEmpty() && pagerState.currentPage in rooms.indices) {
            onActiveRoomChange(rooms[pagerState.currentPage].id)
        }
    }

    val currentRoom = rooms.getOrNull(pagerState.currentPage)
    val effectiveCondition = weatherReport?.condition ?: currentRoom?.weatherCondition ?: "Partly Cloudy"

    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 45.dp.toPx() }
    val restOffsetYPx = with(density) { 65.dp.toPx() }
    val horizontalOverscrollEffect = rememberOverscrollEffect()

    var scrollJob by remember { mutableStateOf<Job?>(null) }

    // Real-time drag progress towards the 50% midpoint tipping line
    // Wrapped in derivedStateOf to prevent full screen recompositions on subpixel drag deltas
    val targetTempScale by remember {
        derivedStateOf {
            val dragOffsetFraction = abs(pagerState.currentPageOffsetFraction)
            val isDraggingPager = pagerState.isScrollInProgress || dragOffsetFraction > 0.005f
            val dragTransitionProgress = (dragOffsetFraction / 0.5f).coerceIn(0f, 1f)
            if (isDraggingPager) {
                (1f - dragTransitionProgress * 0.25f).coerceIn(0.75f, 1f)
            } else {
                1f
            }
        }
    }
    val targetTempAlpha by remember {
        derivedStateOf {
            val dragOffsetFraction = abs(pagerState.currentPageOffsetFraction)
            val isDraggingPager = pagerState.isScrollInProgress || dragOffsetFraction > 0.005f
            val dragTransitionProgress = (dragOffsetFraction / 0.5f).coerceIn(0f, 1f)
            if (isDraggingPager) {
                val remaining = (1f - dragTransitionProgress).coerceIn(0f, 1f)
                remaining * remaining
            } else {
                1f
            }
        }
    }

    val animatedTempScale by animateFloatAsState(
        targetValue = targetTempScale,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "HeroTempZoomTransition"
    )
    val animatedTempAlpha by animateFloatAsState(
        targetValue = targetTempAlpha,
        animationSpec = spring(
            dampingRatio = 0.80f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "HeroTempAlphaTransition"
    )

    KlimataTheme(phase = phase, minuteOfDay = minuteOfDay) {
        val diurnal = LocalDiurnalColors.current

        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(brush = diurnal.skyGradient)
        ) {
            val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val visibleViewportHeight = maxHeight - statusBarPadding - navBarPadding
            val scheduleAnchorSpacer = (visibleViewportHeight - 538.dp).coerceAtLeast(12.dp)

            AtmosphericSkyCanvas(
                scrollOffsetProvider = { if (rooms.isEmpty()) 0f else scrollState.value.toFloat() },
                phase = phase,
                weatherCondition = effectiveCondition,
                modifier = Modifier.fillMaxSize()
            )

            val headerCutoffPx = with(density) { 62.dp.toPx() }
            val heroFadeDistancePx = with(density) { 140.dp.toPx() }

            if (rooms.isEmpty()) {
                // Non-scrollable Empty State Content Layer
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    Spacer(modifier = Modifier.height(102.dp))

                    HeroTemperatureDisplay(
                        temperature = weatherReport?.currentOutdoorTemp ?: 28,
                        condition = effectiveCondition,
                        highTemp = weatherReport?.highTemp ?: 32,
                        lowTemp = weatherReport?.lowTemp ?: 24,
                        phase = phase,
                        roomIndex = 0,
                        tempScaleProvider = { 1f },
                        tempAlphaProvider = { 1f },
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                // Doodle invitation pointing at (+) button
                EmptyRoomsDoodlePrompt(
                    onAddRoomClick = onAddRoomClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(top = 50.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .drawWithContent {
                            val scrollOffset = scrollState.value.toFloat()
                            val heroProgress = (scrollOffset / heroFadeDistancePx).coerceIn(0f, 1f)
                            val clipTop = if (heroProgress >= 1f) headerCutoffPx else 0f
                            if (clipTop > 0f) {
                                clipRect(top = clipTop) {
                                    this@drawWithContent.drawContent()
                                }
                            } else {
                                this@drawWithContent.drawContent()
                            }
                        }
                        .verticalScroll(scrollState)
                        .navigationBarsPadding()
                ) {
                    // Unified horizontal drag gesture across hero display and dynamic spacer
                    // Dispatches real-time deltas directly to pagerState to enable fluid interactive card peeking
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(rooms.size) {
                                var accumulatedDrag = 0f
                                detectHorizontalDragGestures(
                                    onDragStart = {
                                        scrollJob?.cancel()
                                        accumulatedDrag = 0f
                                    },
                                    onHorizontalDrag = { change, dragAmount ->
                                        change.consume()
                                        accumulatedDrag += dragAmount
                                        if (horizontalOverscrollEffect != null) {
                                            horizontalOverscrollEffect.applyToScroll(
                                                delta = Offset(dragAmount, 0f),
                                                source = NestedScrollSource.UserInput
                                            ) { delta ->
                                                val consumed = pagerState.dispatchRawDelta(-delta.x)
                                                Offset(-consumed, 0f)
                                            }
                                        } else {
                                            pagerState.dispatchRawDelta(-dragAmount)
                                        }
                                    },
                                    onDragEnd = {
                                        if (horizontalOverscrollEffect != null) {
                                            coroutineScope.launch {
                                                horizontalOverscrollEffect.applyToFling(Velocity.Zero) { Velocity.Zero }
                                            }
                                        }
                                        val targetPage = if (accumulatedDrag < -swipeThresholdPx && pagerState.currentPage < rooms.size - 1) {
                                            if (pagerState.currentPageOffsetFraction >= 0f) pagerState.currentPage + 1 else pagerState.currentPage
                                        } else if (accumulatedDrag > swipeThresholdPx && pagerState.currentPage > 0) {
                                            if (pagerState.currentPageOffsetFraction <= 0f) pagerState.currentPage - 1 else pagerState.currentPage
                                        } else {
                                            pagerState.currentPage
                                        }
                                        scrollJob = coroutineScope.launch {
                                            pagerState.animateScrollToPage(targetPage.coerceIn(0, rooms.size - 1))
                                        }
                                        accumulatedDrag = 0f
                                    },
                                    onDragCancel = {
                                        if (horizontalOverscrollEffect != null) {
                                            coroutineScope.launch {
                                                horizontalOverscrollEffect.applyToFling(Velocity.Zero) { Velocity.Zero }
                                            }
                                        }
                                        scrollJob = coroutineScope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage)
                                        }
                                        accumulatedDrag = 0f
                                    }
                                )
                            }
                    ) {
                        // Placeholder space matching the pinned top bar + rest offset of room indicator
                        Spacer(modifier = Modifier.height(102.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    val scrollOffset = scrollState.value.toFloat()
                                    val heroProgress = (scrollOffset / heroFadeDistancePx).coerceIn(0f, 1f)
                                    alpha = (1f - heroProgress).coerceIn(0f, 1f)
                                    translationY = -scrollOffset * 0.15f
                                    val blurPx = heroProgress * 10.dp.toPx()
                                    renderEffect = if (blurPx > 0.5f) BlurEffect(blurPx, blurPx) else null
                                }
                        ) {
                            val baseTemp = currentRoom?.currentTemp ?: weatherReport?.currentOutdoorTemp ?: 28
                            HeroTemperatureDisplay(
                                temperature = baseTemp,
                                condition = effectiveCondition,
                                highTemp = weatherReport?.highTemp ?: (baseTemp + 4),
                                lowTemp = weatherReport?.lowTemp ?: (baseTemp - 4),
                                phase = phase,
                                roomIndex = pagerState.currentPage,
                                tempScaleProvider = { animatedTempScale },
                                tempAlphaProvider = { animatedTempAlpha },
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }

                        // Dynamic spacer anchors Tonight's Schedule at bottom of initial viewport
                        Spacer(
                            modifier = Modifier
                                .height(scheduleAnchorSpacer)
                                .fillMaxWidth()
                        )
                    }

                    // Default CenterVertically misaligns top cards across rooms with differing content heights
                    // Equal pageSpacing and contentPadding ensures adjacent cards sit flush at screen edges without resting peek
                    HorizontalPager(
                        state = pagerState,
                        pageSpacing = 12.dp,
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        verticalAlignment = Alignment.Top,
                        overscrollEffect = horizontalOverscrollEffect,
                        modifier = Modifier.fillMaxWidth()
                    ) { pageIndex ->
                        val room = rooms[pageIndex]

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ScheduleChart(
                                steps = room.thermalSteps,
                                isEcoEnabled = room.isEcoEnabled,
                                onClick = { onScheduleClick(room.id) },
                                modifier = Modifier.fillMaxWidth()
                            )

                            AcControlPanel(
                                profile = room.profile,
                                dispatch = room.dispatchState,
                                initialPowerOn = room.isPowerOn,
                                initialEcoEnabled = room.isEcoEnabled,
                                onPowerToggle = { isPowerOn -> onPowerToggle(room.id, isPowerOn) },
                                onEcoToggle = { isEnabled -> onEcoToggle(room.id, isEnabled) },
                                onTempChange = { setpoint -> onTempChange(room.id, setpoint) },
                                onModeChange = { mode -> onModeChange(room.id, mode) },
                                onFanSpeedChange = { fanSpeed -> onFanSpeedChange(room.id, fanSpeed) },
                                onSwingToggle = { isSwing -> onSwingToggle(room.id, isSwing) },
                                onCalibrateRemote = { onCalibrateRemote(room.id) },
                                onDetailsClick = { onAcUnitClick(room.id) },
                                modifier = Modifier.fillMaxWidth()
                            )

                            RoomThermalVisualizerCard(
                                room = room,
                                onClick = { onThermalClick(room.id) },
                                modifier = Modifier.fillMaxWidth()
                            )

                            ImpactLedgerGrid(
                                savings = room.monthlySavings,
                                carbon = room.avoidedCarbon,
                                onSavingsClick = { onSavingsClick(room.id) },
                                onCarbonClick = { onCarbonClick(room.id) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }
            }

            HeroForegroundClouds(
                phase = phase,
                scrollOffsetProvider = { if (rooms.isEmpty()) 0f else scrollState.value.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 24.dp, end = 20.dp, top = 12.dp, bottom = 12.dp)
            ) {
                if (rooms.isNotEmpty() && currentRoom != null) {
                    RoomIndicator(
                        currentRoom = currentRoom.name,
                        roomCount = rooms.size,
                        currentRoomIndex = pagerState.currentPage,
                        onRoomSelected = { targetIndex ->
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(targetIndex)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .graphicsLayer {
                                val scroll = scrollState.value.toFloat()
                                translationY = (restOffsetYPx - scroll).coerceAtLeast(0f)
                            }
                    )
                }

                AmbientTopBarActions(
                    phase = phase,
                    onPhaseChange = onPhaseChange,
                    onAddRoomClick = onAddRoomClick,
                    onManageRoomsClick = onManageRoomsClick,
                    onSettingsClick = onSettingsClick,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KlimataScreenDayPreview() {
    KlimataScreen(phase = DiurnalPhase.DAY)
}

@Preview(showBackground = true)
@Composable
private fun KlimataScreenNightPreview() {
    KlimataScreen(phase = DiurnalPhase.NIGHT)
}

