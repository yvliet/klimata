package com.example.klimata.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.klimata.data.MockData
import com.example.klimata.ui.components.ACControlPanel
import com.example.klimata.ui.components.AmbientHeader
import com.example.klimata.ui.components.AtmosphericSkyCanvas
import com.example.klimata.ui.components.HeroTemperatureDisplay
import com.example.klimata.ui.components.ImpactLedgerGrid
import com.example.klimata.ui.components.ScheduleChart
import com.example.klimata.ui.theme.DiurnalPhase
import com.example.klimata.ui.theme.KlimataTheme
import com.example.klimata.ui.theme.LocalDiurnalColors
import com.example.klimata.ui.theme.currentDiurnalPhase
import kotlinx.coroutines.launch

/**
 * Root screen orchestrating multi-room thermal automation state, paging gestures,
 * and diurnal sky backdrop parallax.
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
    val pagerState = rememberPagerState(initialPage = 0) { rooms.size }
    val coroutineScope = rememberCoroutineScope()

    KlimataTheme(phase = selectedPhase) {
        val diurnal = LocalDiurnalColors.current

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(brush = diurnal.skyGradient)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                val room = rooms[pageIndex]
                val scrollState = rememberScrollState()

                Box(modifier = Modifier.fillMaxSize()) {
                    AtmosphericSkyCanvas(
                        scrollOffsetProvider = { scrollState.value.toFloat() },
                        phase = selectedPhase,
                        modifier = Modifier.fillMaxSize()
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    ) {
                        AmbientHeader(
                            currentRoom = room.name,
                            roomCount = rooms.size,
                            currentRoomIndex = pagerState.currentPage,
                            onRoomSelected = { targetIndex ->
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(targetIndex)
                                }
                            },
                            onAddRoomClick = { /* TODO: Hook room provisioning flow */ },
                            onMenuClick = { /* TODO: Hook contextual settings menu */ }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                        ) {
                            HeroTemperatureDisplay(
                                temperature = room.currentTemp,
                                condition = room.weatherCondition,
                                highTemp = MockData.weather.highTemp,
                                lowTemp = MockData.weather.lowTemp,
                                phase = selectedPhase
                            )
                        }

                        Spacer(modifier = Modifier.height(48.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
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

                        Spacer(modifier = Modifier.height(36.dp))
                    }
                }
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
