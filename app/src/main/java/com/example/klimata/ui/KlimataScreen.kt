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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.klimata.data.MockData
import com.example.klimata.ui.components.ACControlPanel
import com.example.klimata.ui.components.AmbientTopBarActions
import com.example.klimata.ui.components.AtmosphericSkyCanvas
import com.example.klimata.ui.components.HeroTemperatureDisplay
import com.example.klimata.ui.components.ImpactLedgerGrid
import com.example.klimata.ui.components.RoomIndicator
import com.example.klimata.ui.components.ScheduleChart
import com.example.klimata.ui.theme.DiurnalPhase
import com.example.klimata.ui.theme.KlimataTheme
import com.example.klimata.ui.theme.LocalDiurnalColors
import com.example.klimata.ui.theme.currentDiurnalPhase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Root screen orchestrating multi-room thermal automation state, full-screen horizontal paging,
 * real-time hero temperature zoom dynamics, sticky docked room indicator header, and diurnal sky backdrop parallax.
 */
@Composable
fun KlimataScreen(
    modifier: Modifier = Modifier,
    initialPhase: DiurnalPhase? = null,
    onScheduleClick: (roomId: String) -> Unit = {},
    onSavingsClick: (roomId: String) -> Unit = {},
    onCarbonClick: (roomId: String) -> Unit = {},
) {
    var selectedPhase by remember {
        mutableStateOf(initialPhase ?: currentDiurnalPhase())
    }

    val rooms = MockData.rooms
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { rooms.size })
    val coroutineScope = rememberCoroutineScope()
    val currentRoom = rooms[pagerState.currentPage]

    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 45.dp.toPx() }
    val restOffsetYPx = with(density) { 50.dp.toPx() }

    var scrollJob by remember { mutableStateOf<Job?>(null) }

    // Real-time drag progress towards the 50% midpoint tipping line
    val dragOffsetFraction = abs(pagerState.currentPageOffsetFraction)
    val isDraggingPager = pagerState.isScrollInProgress || dragOffsetFraction > 0.005f
    val dragTransitionProgress = (dragOffsetFraction / 0.5f).coerceIn(0f, 1f)

    // Subtle 25% shrink combined with steep quadratic fade to invisibility by midpoint
    val targetTempScale = if (isDraggingPager) {
        (1f - dragTransitionProgress * 0.25f).coerceIn(0.75f, 1f)
    } else {
        1f
    }
    val targetTempAlpha = if (isDraggingPager) {
        val remaining = (1f - dragTransitionProgress).coerceIn(0f, 1f)
        remaining * remaining
    } else {
        1f
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

    KlimataTheme(phase = selectedPhase) {
        val diurnal = LocalDiurnalColors.current

        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(brush = diurnal.skyGradient)
        ) {
            val totalViewportHeight = maxHeight
            // Dynamic spacer height ensures Tonight's Schedule is fully visible in resting state
            // with the top header of Current Setpoint slightly peeking above the bottom fold.
            val scheduleAnchorSpacer = (totalViewportHeight - 535.dp).coerceAtLeast(80.dp)

            AtmosphericSkyCanvas(
                scrollOffsetProvider = { scrollState.value.toFloat() },
                phase = selectedPhase,
                modifier = Modifier.fillMaxSize()
            )

            // Scrollable Content Layer
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .statusBarsPadding()
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

                    val scrollOffset = scrollState.value.toFloat()
                    val heroProgress = (scrollOffset / 200f).coerceIn(0f, 1f)
                    val heroBlurDp = (heroProgress * 16f).dp
                    val heroAlpha = (1f - heroProgress * 0.85f).coerceIn(0f, 1f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .blur(radius = heroBlurDp)
                            .graphicsLayer {
                                alpha = heroAlpha
                                translationY = -scrollOffset * 0.20f
                            }
                    ) {
                        HeroTemperatureDisplay(
                            temperature = currentRoom.currentTemp,
                            condition = currentRoom.weatherCondition,
                            highTemp = MockData.weather.highTemp,
                            lowTemp = MockData.weather.lowTemp,
                            phase = selectedPhase,
                            roomIndex = pagerState.currentPage,
                            tempScale = animatedTempScale,
                            tempAlpha = animatedTempAlpha
                        )
                    }

                    // Dynamic spacer anchors Tonight's Schedule at bottom of initial viewport
                    Spacer(
                        modifier = Modifier
                            .height(scheduleAnchorSpacer)
                            .fillMaxWidth()
                    )
                }

                // Interactive horizontal card pager with tight 8dp inter-card packing
                HorizontalPager(
                    state = pagerState,
                    pageSpacing = 12.dp,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) { pageIndex ->
                    val room = rooms[pageIndex]

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScheduleChart(
                            steps = room.thermalSteps,
                            onClick = { onScheduleClick(room.id) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        ACControlPanel(
                            profile = room.profile,
                            dispatch = room.dispatchState,
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

                // Top action icons remain pinned at top-right
                AmbientTopBarActions(
                    onAddRoomClick = { /* TODO: Hook room provisioning flow */ },
                    onMenuClick = { /* TODO: Hook contextual settings menu */ },
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KlimataScreenDayPreview() {
    KlimataScreen(initialPhase = DiurnalPhase.DAY)
}

@Preview(showBackground = true)
@Composable
private fun KlimataScreenNightPreview() {
    KlimataScreen(initialPhase = DiurnalPhase.NIGHT)
}

