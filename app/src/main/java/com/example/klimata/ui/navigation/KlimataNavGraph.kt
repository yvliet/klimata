package com.example.klimata.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.klimata.data.LocationHelper
import com.example.klimata.data.engine.ThermalCalculationEngine
import com.example.klimata.data.network.AmbientWeatherReport
import com.example.klimata.data.network.WeatherApiClient
import com.example.klimata.data.storage.KlimataPreferences
import com.example.klimata.ui.KlimataScreen
import com.example.klimata.ui.screens.CarbonDetailScreen
import com.example.klimata.ui.screens.RoomThermalDetailScreen
import com.example.klimata.ui.screens.SavingsDetailScreen
import com.example.klimata.data.ir.IrBlasterService
import com.example.klimata.ui.components.AcRemotePairingModal
import com.example.klimata.ui.screens.ScheduleDetailScreen
import com.example.klimata.ui.screens.provisioning.AddRoomWizardScreen
import com.example.klimata.ui.theme.DiurnalPhase
import com.example.klimata.ui.theme.KlimataTheme
import com.example.klimata.ui.theme.LocalDiurnalColors
import com.example.klimata.ui.theme.currentDiurnalPhase

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object ScheduleDetail : Screen("schedule/{roomId}") {
        fun createRoute(roomId: String) = "schedule/$roomId"
    }
    data object RoomThermalDetail : Screen("thermal/{roomId}") {
        fun createRoute(roomId: String) = "thermal/$roomId"
    }
    data object SavingsDetail : Screen("savings/{roomId}") {
        fun createRoute(roomId: String) = "savings/$roomId"
    }
    data object CarbonDetail : Screen("carbon/{roomId}") {
        fun createRoute(roomId: String) = "carbon/$roomId"
    }
    data object AddRoom : Screen("add_room")
}

/**
 * Root navigation graph managing cinematic horizontal slide transitions across detail
 * destinations with subtle background parallax, soft recession dimming, and single-top navigation locks.
 */
@Composable
fun KlimataNavGraph(
    modifier: Modifier = Modifier,
    initialPhase: DiurnalPhase? = null,
) {
    val navController = rememberNavController()
    var selectedPhase by remember {
        mutableStateOf(initialPhase ?: currentDiurnalPhase())
    }

    val context = LocalContext.current
    val irBlaster = remember { IrBlasterService(context) }
    var rooms by remember { mutableStateOf(KlimataPreferences.loadRooms(context)) }
    var weatherReport by remember { mutableStateOf<AmbientWeatherReport?>(KlimataPreferences.loadWeather(context)) }
    var hasCompletedOnboarding by remember { mutableStateOf(false) }
    var activeCalibrationRoomId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val loc = LocationHelper.detectLocation(context)
        val weatherResult = WeatherApiClient.fetchWeather(loc.latitude, loc.longitude, loc.cityName)
        if (weatherResult.isSuccess) {
            val report = weatherResult.getOrThrow()
            weatherReport = report
            KlimataPreferences.saveWeather(context, report)

            rooms = rooms.map { room ->
                val computation = ThermalCalculationEngine.computeRoomThermalDynamics(
                    areaSquareMeters = room.areaSquareMeters,
                    ceilingHeightMeters = room.ceilingHeightMeters,
                    thermalMassType = room.thermalMassLabel,
                    acCapacity = room.profile.capacity,
                    acInverterType = room.profile.inverterType,
                    targetTemp = room.profile.currentSetpoint,
                    hourlyOutdoorTemps = report.hourlyTemps
                )
                room.copy(
                    location = loc.cityName,
                    weatherCondition = report.condition,
                    thermalSteps = computation.thermalSteps,
                    monthlySavings = computation.monthlySavings,
                    avoidedCarbon = computation.avoidedCarbon,
                    savingsHistory = computation.savingsHistory,
                    carbonHistory = computation.carbonHistory,
                    savingsBreakdown = computation.savingsBreakdown,
                    carbonEquivalence = computation.carbonEquivalence
                )
            }
            KlimataPreferences.saveRooms(context, rooms)
        }
    }

    fun navigateSafely(route: String) {
        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
            navController.navigate(route) {
                launchSingleTop = true
            }
        }
    }

    KlimataTheme(phase = selectedPhase) {
        val diurnal = LocalDiurnalColors.current

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(brush = diurnal.skyGradient)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(
                    route = Screen.Home.route,
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(480, easing = FastOutSlowInEasing)
                        ) { fullWidth -> (fullWidth * 0.30f).toInt() } + fadeOut(
                            animationSpec = tween(480, easing = FastOutSlowInEasing),
                            targetAlpha = 0.50f
                        )
                    },
                    popEnterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(420, easing = FastOutSlowInEasing)
                        ) { fullWidth -> (fullWidth * 0.30f).toInt() } + fadeIn(
                            animationSpec = tween(420, easing = FastOutSlowInEasing),
                            initialAlpha = 0.50f
                        )
                    }
                ) {
                    KlimataScreen(
                        phase = selectedPhase,
                        onPhaseChange = { selectedPhase = it },
                        rooms = rooms,
                        weatherReport = weatherReport,
                        onPowerToggle = { roomId, isPowerOn ->
                            rooms = rooms.map {
                                if (it.id == roomId) {
                                    it.copy(isPowerOn = isPowerOn)
                                } else {
                                    it
                                }
                            }
                            KlimataPreferences.saveRooms(context, rooms)
                            rooms.find { it.id == roomId }?.let { target ->
                                irBlaster.dispatchAcCommand(
                                    brand = target.profile.brand,
                                    power = isPowerOn,
                                    temp = target.targetTemp,
                                    mode = target.profile.mode,
                                    fanSpeed = target.profile.fanSpeed,
                                    isEco = target.isEcoEnabled,
                                    swing = target.profile.swing,
                                    codeSetId = target.profile.irCodeSet
                                )
                            }
                        },
                        onEcoToggle = { roomId, isEnabled ->
                            rooms = rooms.map {
                                if (it.id == roomId) it.copy(isEcoEnabled = isEnabled) else it
                            }
                            KlimataPreferences.saveRooms(context, rooms)
                            rooms.find { it.id == roomId }?.let { target ->
                                irBlaster.dispatchAcCommand(
                                    brand = target.profile.brand,
                                    power = target.isPowerOn,
                                    temp = target.targetTemp,
                                    mode = target.profile.mode,
                                    fanSpeed = target.profile.fanSpeed,
                                    isEco = isEnabled,
                                    swing = target.profile.swing,
                                    codeSetId = target.profile.irCodeSet
                                )
                            }
                        },
                        onTempChange = { roomId, setpoint ->
                            rooms = rooms.map { room ->
                                if (room.id == roomId) {
                                    val updatedSteps = ThermalCalculationEngine.generateSchedule(
                                        targetTemp = setpoint,
                                        hourlyOutdoorTemps = weatherReport?.hourlyTemps ?: emptyMap(),
                                        thermalMassType = room.thermalMassLabel
                                    )
                                    room.copy(
                                        profile = room.profile.copy(currentSetpoint = setpoint),
                                        targetTemp = setpoint,
                                        thermalSteps = updatedSteps
                                    )
                                } else {
                                    room
                                }
                            }
                            KlimataPreferences.saveRooms(context, rooms)
                            rooms.find { it.id == roomId }?.let { target ->
                                irBlaster.dispatchAcCommand(
                                    brand = target.profile.brand,
                                    power = target.isPowerOn,
                                    temp = setpoint,
                                    mode = target.profile.mode,
                                    fanSpeed = target.profile.fanSpeed,
                                    isEco = target.isEcoEnabled,
                                    swing = target.profile.swing,
                                    codeSetId = target.profile.irCodeSet
                                )
                            }
                        },
                        onModeChange = { roomId, mode ->
                            rooms = rooms.map { room ->
                                if (room.id == roomId) {
                                    room.copy(profile = room.profile.copy(mode = mode))
                                } else {
                                    room
                                }
                            }
                            KlimataPreferences.saveRooms(context, rooms)
                            rooms.find { it.id == roomId }?.let { target ->
                                irBlaster.dispatchAcCommand(
                                    brand = target.profile.brand,
                                    power = target.isPowerOn,
                                    temp = target.targetTemp,
                                    mode = mode,
                                    fanSpeed = target.profile.fanSpeed,
                                    isEco = target.isEcoEnabled,
                                    swing = target.profile.swing,
                                    codeSetId = target.profile.irCodeSet
                                )
                            }
                        },
                        onFanSpeedChange = { roomId, fanSpeed ->
                            rooms = rooms.map { room ->
                                if (room.id == roomId) {
                                    room.copy(profile = room.profile.copy(fanSpeed = fanSpeed))
                                } else {
                                    room
                                }
                            }
                            KlimataPreferences.saveRooms(context, rooms)
                            rooms.find { it.id == roomId }?.let { target ->
                                irBlaster.dispatchAcCommand(
                                    brand = target.profile.brand,
                                    power = target.isPowerOn,
                                    temp = target.targetTemp,
                                    mode = target.profile.mode,
                                    fanSpeed = fanSpeed,
                                    isEco = target.isEcoEnabled,
                                    swing = target.profile.swing,
                                    codeSetId = target.profile.irCodeSet
                                )
                            }
                        },
                        onSwingToggle = { roomId, isSwing ->
                            rooms = rooms.map { room ->
                                if (room.id == roomId) {
                                    room.copy(profile = room.profile.copy(swing = isSwing))
                                } else {
                                    room
                                }
                            }
                            KlimataPreferences.saveRooms(context, rooms)
                            rooms.find { it.id == roomId }?.let { target ->
                                irBlaster.dispatchAcCommand(
                                    brand = target.profile.brand,
                                    power = target.isPowerOn,
                                    temp = target.targetTemp,
                                    mode = target.profile.mode,
                                    fanSpeed = target.profile.fanSpeed,
                                    isEco = target.isEcoEnabled,
                                    swing = isSwing,
                                    codeSetId = target.profile.irCodeSet
                                )
                            }
                        },
                        onCalibrateRemote = { roomId ->
                            activeCalibrationRoomId = roomId
                        },
                        onScheduleClick = { roomId ->
                            navigateSafely(Screen.ScheduleDetail.createRoute(roomId))
                        },
                        onThermalClick = { roomId ->
                            navigateSafely(Screen.RoomThermalDetail.createRoute(roomId))
                        },
                        onSavingsClick = { roomId ->
                            navigateSafely(Screen.SavingsDetail.createRoute(roomId))
                        },
                        onCarbonClick = { roomId ->
                            navigateSafely(Screen.CarbonDetail.createRoute(roomId))
                        },
                        onAddRoomClick = {
                            navigateSafely(Screen.AddRoom.route)
                        }
                    )
                }

                composable(
                    route = Screen.ScheduleDetail.route,
                    arguments = listOf(navArgument("roomId") { type = NavType.StringType }),
                    enterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(480, easing = FastOutSlowInEasing)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(420, easing = FastOutSlowInEasing)
                        )
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(420, easing = FastOutSlowInEasing)
                        )
                    }
                ) { backStackEntry ->
                    val roomId = backStackEntry.arguments?.getString("roomId")
                    val room = rooms.firstOrNull { it.id == roomId }
                    if (room != null) {
                        ScheduleDetailScreen(
                            room = room,
                            onBackClick = { navController.popBackStack() }
                        )
                    } else {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    }
                }

                composable(
                    route = Screen.RoomThermalDetail.route,
                    arguments = listOf(navArgument("roomId") { type = NavType.StringType }),
                    enterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(480, easing = FastOutSlowInEasing)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(420, easing = FastOutSlowInEasing)
                        )
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(420, easing = FastOutSlowInEasing)
                        )
                    }
                ) { backStackEntry ->
                    val roomId = backStackEntry.arguments?.getString("roomId")
                    val room = rooms.firstOrNull { it.id == roomId }
                    if (room != null) {
                        RoomThermalDetailScreen(
                            room = room,
                            onBackClick = { navController.popBackStack() }
                        )
                    } else {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    }
                }

                composable(
                    route = Screen.SavingsDetail.route,
                    arguments = listOf(navArgument("roomId") { type = NavType.StringType }),
                    enterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(480, easing = FastOutSlowInEasing)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(420, easing = FastOutSlowInEasing)
                        )
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(420, easing = FastOutSlowInEasing)
                        )
                    }
                ) { backStackEntry ->
                    val roomId = backStackEntry.arguments?.getString("roomId")
                    val room = rooms.firstOrNull { it.id == roomId }
                    if (room != null) {
                        SavingsDetailScreen(
                            room = room,
                            onBackClick = { navController.popBackStack() }
                        )
                    } else {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    }
                }

                composable(
                    route = Screen.CarbonDetail.route,
                    arguments = listOf(navArgument("roomId") { type = NavType.StringType }),
                    enterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(480, easing = FastOutSlowInEasing)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(420, easing = FastOutSlowInEasing)
                        )
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(420, easing = FastOutSlowInEasing)
                        )
                    }
                ) { backStackEntry ->
                    val roomId = backStackEntry.arguments?.getString("roomId")
                    val room = rooms.firstOrNull { it.id == roomId }
                    if (room != null) {
                        CarbonDetailScreen(
                            room = room,
                            onBackClick = { navController.popBackStack() }
                        )
                    } else {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    }
                }

                composable(
                    route = Screen.AddRoom.route,
                    enterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(480, easing = FastOutSlowInEasing)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(420, easing = FastOutSlowInEasing)
                        )
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(420, easing = FastOutSlowInEasing)
                        )
                    }
                ) {
                    AddRoomWizardScreen(
                        isOnboarding = !hasCompletedOnboarding,
                        onBackClick = { navController.popBackStack() },
                        onRoomCreated = { newRoom ->
                            rooms = rooms + newRoom
                            KlimataPreferences.saveRooms(context, rooms)
                            hasCompletedOnboarding = true
                            navController.popBackStack()
                        }
                    )
                }
            }

            activeCalibrationRoomId?.let { calibRoomId ->
                val targetRoom = rooms.find { it.id == calibRoomId }
                if (targetRoom != null) {
                    AcRemotePairingModal(
                        brand = targetRoom.profile.brand,
                        currentCodeSetId = targetRoom.profile.irCodeSet,
                        onCodeSetSelected = { newCodeSetId ->
                            rooms = rooms.map {
                                if (it.id == calibRoomId) {
                                    it.copy(profile = it.profile.copy(irCodeSet = newCodeSetId))
                                } else {
                                    it
                                }
                            }
                            KlimataPreferences.saveRooms(context, rooms)
                            activeCalibrationRoomId = null
                        },
                        onDismiss = { activeCalibrationRoomId = null }
                    )
                } else {
                    activeCalibrationRoomId = null
                }
            }
        }
    }
}
