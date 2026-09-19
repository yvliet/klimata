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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.klimata.data.MockData
import com.example.klimata.data.RoomState
import com.example.klimata.ui.components.ACControlPanel
import com.example.klimata.ui.components.AmbientTopBarActions
import com.example.klimata.ui.components.AtmosphericSkyCanvas
import com.example.klimata.ui.components.HeroTemperatureDisplay
import com.example.klimata.ui.components.ImpactLedgerGrid
import com.example.klimata.ui.components.RoomIndicator
import com.example.klimata.ui.components.RoomThermalVisualizerCard
import com.example.klimata.ui.components.ScheduleChart
import com.example.klimata.ui.theme.DiurnalPhase
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.KlimataTheme
import com.example.klimata.ui.theme.LocalDiurnalColors
import com.example.klimata.ui.theme.OvercastSkyStop1
import com.example.klimata.ui.theme.OvercastSkyStop2
import com.example.klimata.ui.theme.OvercastSkyStop3
import com.example.klimata.ui.theme.OvercastSkyStop4
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
    onPhaseChange: (DiurnalPhase) -> Unit = {},
    rooms: List<RoomState> = MockData.rooms,
    onPowerToggle: (roomId: String, isPowerOn: Boolean) -> Unit = { _, _ -> },
    onEcoToggle: (roomId: String, isEnabled: Boolean) -> Unit = { _, _ -> },
    onTempChange: (roomId: String, setpoint: Int) -> Unit = { _, _ -> },
    onScheduleClick: (roomId: String) -> Unit = {},
    onThermalClick: (roomId: String) -> Unit = {},
    onSavingsClick: (roomId: String) -> Unit = {},
    onCarbonClick: (roomId: String) -> Unit = {},
    onAddRoomClick: () -> Unit = {},
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { rooms.size })
    val coroutineScope = rememberCoroutineScope()
    val currentRoom = rooms[pagerState.currentPage]
    val effectiveCondition = currentRoom.weatherCondition

    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 45.dp.toPx() }
    val restOffsetYPx = with(density) { 60.dp.toPx() }

    var scrollJob by remember { mutableStateOf<Job?>(null) }
    var heroSectionHeightPx by remember { mutableFloatStateOf(0f) }

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

    KlimataTheme(phase = phase) {
        val diurnal = LocalDiurnalColors.current

        val activeSkyGradient = remember(phase) {
            if (phase == DiurnalPhase.NIGHT) {
                Brush.verticalGradient(
                    listOf(OvercastSkyStop1, OvercastSkyStop2, OvercastSkyStop3, OvercastSkyStop4)
                )
            } else {
                diurnal.skyGradient
            }
        }

        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(brush = activeSkyGradient)
        ) {
            val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val visibleViewportHeight = maxHeight - statusBarPadding - navBarPadding
            val scheduleAnchorSpacer = (visibleViewportHeight - 538.dp).coerceAtLeast(12.dp)

            AtmosphericSkyCanvas(
                scrollOffsetProvider = { scrollState.value.toFloat() },
                phase = phase,
                weatherCondition = effectiveCondition,
                modifier = Modifier.fillMaxSize()
            )

            val headerCutoffPx = with(density) { 62.dp.toPx() }

            // Scrollable Content Layer
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(scrollState)
                    .navigationBarsPadding()
            ) {
                // Unified horizontal drag gesture across hero display and dynamic spacer
                // Dispatches real-time deltas directly to pagerState to enable fluid interactive card peeking
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { heroSectionHeightPx = it.height.toFloat() }
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
                                    pagerState.dispatchRawDelta(-dragAmount)
                                },
                                onDragEnd = {
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
                                val fadeDistancePx = 140.dp.toPx()
                                val heroProgress = (scrollOffset / fadeDistancePx).coerceIn(0f, 1f)
                                alpha = (1f - heroProgress).coerceIn(0f, 1f)
                                translationY = -scrollOffset * 0.15f
                                val blurPx = heroProgress * 10.dp.toPx()
                                renderEffect = if (blurPx > 0.5f) BlurEffect(blurPx, blurPx) else null
                            }
                    ) {
                        HeroTemperatureDisplay(
                            temperature = currentRoom.currentTemp,
                            condition = effectiveCondition,
                            highTemp = MockData.weather.highTemp,
                            lowTemp = MockData.weather.lowTemp,
                            phase = phase,
                            roomIndex = pagerState.currentPage,
                            tempScale = animatedTempScale,
                            tempAlpha = animatedTempAlpha,
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
                    pageSpacing = 8.dp,
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawWithContent {
                            val clipTop = if (heroSectionHeightPx > 0f) {
                                (headerCutoffPx - (heroSectionHeightPx - scrollState.value)).coerceAtLeast(0f)
                            } else {
                                0f
                            }
                            if (clipTop > 0f) {
                                clipRect(top = clipTop) {
                                    this@drawWithContent.drawContent()
                                }
                            } else {
                                this@drawWithContent.drawContent()
                            }
                        }
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

                        ACControlPanel(
                            profile = room.profile,
                            dispatch = room.dispatchState,
                            initialPowerOn = room.isPowerOn,
                            initialEcoEnabled = room.isEcoEnabled,
                            onPowerToggle = { isPowerOn -> onPowerToggle(room.id, isPowerOn) },
                            onEcoToggle = { isEnabled -> onEcoToggle(room.id, isEnabled) },
                            onTempChange = { setpoint -> onTempChange(room.id, setpoint) },
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

            // Pinned Top Bar Overlay with Docking Room Indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 24.dp, end = 20.dp, top = 12.dp, bottom = 12.dp)
            ) {
                // Room indicator starts above hero temp and glides up to dock at top bar Y on scroll
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

                AmbientTopBarActions(
                    onAddRoomClick = onAddRoomClick,
                    onMenuClick = {
                        val nextPhase = when (phase) {
                            DiurnalPhase.DAY -> DiurnalPhase.EVENING
                            DiurnalPhase.EVENING -> DiurnalPhase.NIGHT
                            DiurnalPhase.NIGHT -> DiurnalPhase.DAY
                        }
                        onPhaseChange(nextPhase)
                    },
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

