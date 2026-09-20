package com.example.klimata.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.dp
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavGraphBuilder
import com.example.klimata.ui.components.LocalPageCornerRadius
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import com.example.klimata.applyStatusBarVisibility
import com.example.klimata.findActivity
import com.example.klimata.data.LocationHelper
import com.example.klimata.data.engine.ThermalCalculationEngine
import com.example.klimata.data.ir.IrBlasterService
import com.example.klimata.data.ir.ThermalScheduleManager
import com.example.klimata.data.network.AmbientWeatherReport
import com.example.klimata.data.network.WeatherApiClient
import com.example.klimata.data.storage.KlimataPreferences
import com.example.klimata.ui.KlimataScreen
import com.example.klimata.ui.screens.AcUnitDetailScreen
import com.example.klimata.ui.screens.CarbonDetailScreen
import com.example.klimata.ui.screens.ManageRoomsScreen
import com.example.klimata.ui.screens.RoomThermalDetailScreen
import com.example.klimata.ui.screens.SavingsDetailScreen
import com.example.klimata.ui.screens.ScheduleDetailScreen
import com.example.klimata.ui.screens.SettingsScreen
import com.example.klimata.ui.screens.provisioning.AddRoomWizardScreen
import com.example.klimata.ui.theme.DiurnalPhase
import com.example.klimata.ui.theme.KlimataTheme
import com.example.klimata.ui.theme.LocalDiurnalColors
import com.example.klimata.ui.theme.currentDiurnalPhase
import com.example.klimata.ui.theme.currentMinuteOfDay
import kotlinx.coroutines.delay

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object ManageRooms : Screen("manage_rooms")
    data object Settings : Screen("settings")
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
    data object AcUnitDetail : Screen("ac_unit/{roomId}") {
        fun createRoute(roomId: String) = "ac_unit/$roomId"
    }
    data object AddRoom : Screen("add_room")
}

private const val PageTransitionDurationMs = 480
private val PageTransitionEasing = FastOutSlowInEasing

private val pageEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(PageTransitionDurationMs, easing = PageTransitionEasing)
    )
}

private val pageExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(PageTransitionDurationMs, easing = PageTransitionEasing)
    )
}

private val pagePopEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(PageTransitionDurationMs, easing = PageTransitionEasing)
    ) { fullWidth -> (fullWidth * 0.30f).toInt() } + fadeIn(
        animationSpec = tween(PageTransitionDurationMs, easing = PageTransitionEasing),
        initialAlpha = 0.50f
    )
}

private val pagePopExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(PageTransitionDurationMs, easing = PageTransitionEasing)
    )
}

private fun NavGraphBuilder.detailPageComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        arguments = arguments,
        enterTransition = pageEnterTransition,
        exitTransition = pageExitTransition,
        popEnterTransition = pagePopEnterTransition,
        popExitTransition = pagePopExitTransition
    ) { backStackEntry ->
        val cornerRadius by transition.animateDp(
            label = "pageCornerRadius",
            transitionSpec = { tween(PageTransitionDurationMs, easing = PageTransitionEasing) }
        ) { state ->
            when (state) {
                EnterExitState.Visible -> 0.dp
                EnterExitState.PreEnter -> 28.dp
                EnterExitState.PostExit -> 28.dp
            }
        }
        CompositionLocalProvider(LocalPageCornerRadius provides cornerRadius) {
            content(backStackEntry)
        }
    }
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
    var currentLiveMinute by remember { mutableStateOf(currentMinuteOfDay()) }
    var selectedPhase by remember {
        mutableStateOf(initialPhase ?: currentDiurnalPhase(currentLiveMinute))
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            val nowMin = currentMinuteOfDay()
            if (nowMin != currentLiveMinute) {
                currentLiveMinute = nowMin
                selectedPhase = currentDiurnalPhase(nowMin)
            }
        }
    }

    val context = LocalContext.current
    var isStatusBarDisabled by remember {
        mutableStateOf(KlimataPreferences.isStatusBarDisabled(context))
    }

    LaunchedEffect(isStatusBarDisabled) {
        val activity = context.findActivity()
        applyStatusBarVisibility(activity, isStatusBarDisabled)
    }

    val irBlaster = remember { IrBlasterService(context) }
    var rooms by remember { mutableStateOf(KlimataPreferences.loadRooms(context)) }
    var weatherReport by remember { mutableStateOf<AmbientWeatherReport?>(KlimataPreferences.loadWeather(context)) }
    var hasCompletedOnboarding by remember { mutableStateOf(false) }
    var activeRoomId by remember(rooms) { mutableStateOf(rooms.firstOrNull()?.id) }
    var energyConfig by remember { mutableStateOf(KlimataPreferences.loadEnergyConfig(context)) }

    LaunchedEffect(Unit) {
        val cachedWeather = weatherReport
        val isWeatherFresh = cachedWeather != null && (System.currentTimeMillis() - cachedWeather.lastUpdatedMillis < 30 * 60 * 1000)
        val report = if (isWeatherFresh) {
            cachedWeather
        } else {
            val loc = LocationHelper.detectLocation(context)
            val weatherResult = WeatherApiClient.fetchWeather(loc.latitude, loc.longitude, loc.cityName)
            if (weatherResult.isSuccess) {
                val freshReport = weatherResult.getOrThrow()
                weatherReport = freshReport
                KlimataPreferences.saveWeather(context, freshReport)
                freshReport
            } else {
                cachedWeather
            }
        }

        if (report != null && rooms.isNotEmpty()) {
            val loc = LocationHelper.detectLocation(context)
            val energyConfig = KlimataPreferences.loadEnergyConfig(context)
            rooms = rooms.map { room ->
                val computation = ThermalCalculationEngine.computeRoomThermalDynamics(
                    areaSquareMeters = room.areaSquareMeters,
                    ceilingHeightMeters = room.ceilingHeightMeters,
                    thermalMassType = room.thermalMassLabel,
                    acCapacity = room.profile.capacity,
                    acInverterType = room.profile.inverterType,
                    targetTemp = room.profile.currentSetpoint,
                    hourlyOutdoorTemps = report.hourlyTemps,
                    energyConfig = energyConfig
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
        ThermalScheduleManager.scheduleNextThermalDispatch(context)
    }

    fun navigateSafely(route: String) {
        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
            navController.navigate(route) {
                launchSingleTop = true
            }
        }
    }

    KlimataTheme(phase = selectedPhase, minuteOfDay = currentLiveMinute) {
        val diurnal = LocalDiurnalColors.current

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(brush = diurnal.skyGradient)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                enterTransition = pageEnterTransition,
                exitTransition = pageExitTransition,
                popEnterTransition = pagePopEnterTransition,
                popExitTransition = pagePopExitTransition,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(
                    route = Screen.Home.route,
                    exitTransition = {
                        if (targetState.destination.route == Screen.ManageRooms.route) {
                            scaleOut(
                                targetScale = 0.88f,
                                animationSpec = tween(PageTransitionDurationMs, easing = PageTransitionEasing)
                            ) + fadeOut(
                                animationSpec = tween(PageTransitionDurationMs, easing = PageTransitionEasing),
                                targetAlpha = 0.15f
                            )
                        } else {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(PageTransitionDurationMs, easing = PageTransitionEasing)
                            ) { fullWidth -> (fullWidth * 0.30f).toInt() } + fadeOut(
                                animationSpec = tween(PageTransitionDurationMs, easing = PageTransitionEasing),
                                targetAlpha = 0.50f
                            )
                        }
                    },
                    popEnterTransition = {
                        if (initialState.destination.route == Screen.ManageRooms.route) {
                            scaleIn(
                                initialScale = 0.88f,
                                animationSpec = tween(PageTransitionDurationMs, easing = PageTransitionEasing)
                            ) + fadeIn(
                                animationSpec = tween(PageTransitionDurationMs, easing = PageTransitionEasing),
                                initialAlpha = 0.15f
                            )
                        } else {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(PageTransitionDurationMs, easing = PageTransitionEasing)
                            ) { fullWidth -> (fullWidth * 0.30f).toInt() } + fadeIn(
                                animationSpec = tween(PageTransitionDurationMs, easing = PageTransitionEasing),
                                initialAlpha = 0.50f
                            )
                        }
                    }
                ) {
                    KlimataScreen(
                        phase = selectedPhase,
                        minuteOfDay = currentLiveMinute,
                        onPhaseChange = { selectedPhase = it },
                        rooms = rooms,
                        activeRoomId = activeRoomId,
                        onActiveRoomChange = { activeRoomId = it },
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
                            ThermalScheduleManager.scheduleNextThermalDispatch(context)
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
                            ThermalScheduleManager.scheduleNextThermalDispatch(context)
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
                            val energyConfig = KlimataPreferences.loadEnergyConfig(context)
                            rooms = rooms.map { room ->
                                if (room.id == roomId) {
                                    val computation = ThermalCalculationEngine.computeRoomThermalDynamics(
                                        areaSquareMeters = room.areaSquareMeters,
                                        ceilingHeightMeters = room.ceilingHeightMeters,
                                        thermalMassType = room.thermalMassLabel,
                                        acCapacity = room.profile.capacity,
                                        acInverterType = room.profile.inverterType,
                                        targetTemp = setpoint,
                                        hourlyOutdoorTemps = weatherReport?.hourlyTemps ?: emptyMap(),
                                        energyConfig = energyConfig
                                    )
                                    room.copy(
                                        profile = room.profile.copy(currentSetpoint = setpoint),
                                        targetTemp = setpoint,
                                        thermalSteps = computation.thermalSteps,
                                        monthlySavings = computation.monthlySavings,
                                        avoidedCarbon = computation.avoidedCarbon,
                                        savingsHistory = computation.savingsHistory,
                                        carbonHistory = computation.carbonHistory,
                                        savingsBreakdown = computation.savingsBreakdown,
                                        carbonEquivalence = computation.carbonEquivalence
                                    )
                                } else {
                                    room
                                }
                            }
                            KlimataPreferences.saveRooms(context, rooms)
                            ThermalScheduleManager.scheduleNextThermalDispatch(context)
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
                            navigateSafely(Screen.AcUnitDetail.createRoute(roomId))
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
                        onAcUnitClick = { roomId ->
                            navigateSafely(Screen.AcUnitDetail.createRoute(roomId))
                        },
                        onAddRoomClick = {
                            navigateSafely(Screen.AddRoom.route)
                        },
                        onManageRoomsClick = {
                            navigateSafely(Screen.ManageRooms.route)
                        },
                        onSettingsClick = {
                            navigateSafely(Screen.Settings.route)
                        }
                    )
                }

                detailPageComposable(
                    route = Screen.ScheduleDetail.route,
                    arguments = listOf(navArgument("roomId") { type = NavType.StringType })
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

                detailPageComposable(
                    route = Screen.RoomThermalDetail.route,
                    arguments = listOf(navArgument("roomId") { type = NavType.StringType })
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

                detailPageComposable(
                    route = Screen.SavingsDetail.route,
                    arguments = listOf(navArgument("roomId") { type = NavType.StringType })
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

                detailPageComposable(
                    route = Screen.CarbonDetail.route,
                    arguments = listOf(navArgument("roomId") { type = NavType.StringType })
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

                detailPageComposable(
                    route = Screen.AddRoom.route
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

                detailPageComposable(
                    route = Screen.AcUnitDetail.route,
                    arguments = listOf(navArgument("roomId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val roomId = backStackEntry.arguments?.getString("roomId")
                    val room = rooms.firstOrNull { it.id == roomId }
                    if (room != null) {
                        AcUnitDetailScreen(
                            room = room,
                            onBackClick = { navController.popBackStack() },
                            onModelSelected = { brand, model, capacity, inverterType, codeSetId ->
                                // Re-compute thermal dynamics immediately with the new unit's capacity,
                                // so schedule and savings cards on the home screen reflect the updated hardware.
                                val energyConfig = KlimataPreferences.loadEnergyConfig(context)
                                val computation = ThermalCalculationEngine.computeRoomThermalDynamics(
                                    areaSquareMeters = room.areaSquareMeters,
                                    ceilingHeightMeters = room.ceilingHeightMeters,
                                    thermalMassType = room.thermalMassLabel,
                                    acCapacity = capacity,
                                    acInverterType = inverterType,
                                    targetTemp = room.profile.currentSetpoint,
                                    hourlyOutdoorTemps = weatherReport?.hourlyTemps ?: emptyMap(),
                                    energyConfig = energyConfig
                                )
                                rooms = rooms.map {
                                    if (it.id == roomId) {
                                        it.copy(
                                            profile = it.profile.copy(
                                                brand = brand,
                                                model = model,
                                                capacity = capacity,
                                                inverterType = inverterType,
                                                irCodeSet = codeSetId
                                            ),
                                            thermalSteps = computation.thermalSteps,
                                            monthlySavings = computation.monthlySavings,
                                            avoidedCarbon = computation.avoidedCarbon,
                                            savingsHistory = computation.savingsHistory,
                                            carbonHistory = computation.carbonHistory,
                                            savingsBreakdown = computation.savingsBreakdown,
                                            carbonEquivalence = computation.carbonEquivalence,
                                            coolingLoadBtu = computation.coolingLoadBtu
                                        )
                                    } else {
                                        it
                                    }
                                }
                                KlimataPreferences.saveRooms(context, rooms)
                            },
                            onCodeSetSelected = { newCodeSetId ->
                                rooms = rooms.map {
                                    if (it.id == roomId) {
                                        it.copy(profile = it.profile.copy(irCodeSet = newCodeSetId))
                                    } else {
                                        it
                                    }
                                }
                                KlimataPreferences.saveRooms(context, rooms)
                            }
                        )
                    } else {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    }
                }

                composable(
                    route = Screen.ManageRooms.route,
                    enterTransition = {
                        scaleIn(
                            initialScale = 1.12f,
                            animationSpec = tween(PageTransitionDurationMs, easing = PageTransitionEasing)
                        ) + fadeIn(
                            animationSpec = tween(PageTransitionDurationMs, easing = PageTransitionEasing)
                        )
                    },
                    exitTransition = {
                        scaleOut(
                            targetScale = 0.88f,
                            animationSpec = tween(PageTransitionDurationMs, easing = PageTransitionEasing)
                        ) + fadeOut(
                            animationSpec = tween(PageTransitionDurationMs, easing = PageTransitionEasing)
                        )
                    },
                    popEnterTransition = {
                        scaleIn(
                            initialScale = 0.88f,
                            animationSpec = tween(PageTransitionDurationMs, easing = PageTransitionEasing)
                        ) + fadeIn(
                            animationSpec = tween(PageTransitionDurationMs, easing = PageTransitionEasing)
                        )
                    },
                    popExitTransition = {
                        scaleOut(
                            targetScale = 1.12f,
                            animationSpec = tween(PageTransitionDurationMs, easing = PageTransitionEasing)
                        ) + fadeOut(
                            animationSpec = tween(PageTransitionDurationMs, easing = PageTransitionEasing)
                        )
                    }
                ) {
                    ManageRoomsScreen(
                        rooms = rooms,
                        activeRoomId = activeRoomId,
                        currentPhase = selectedPhase,
                        onRoomSelected = { selectedId ->
                            activeRoomId = selectedId
                            navController.popBackStack()
                        },
                        onRoomUpdated = { updatedRoom ->
                            rooms = rooms.map { if (it.id == updatedRoom.id) updatedRoom else it }
                            KlimataPreferences.saveRooms(context, rooms)
                        },
                        onRoomDeleted = { deletedId ->
                            rooms = rooms.filter { it.id != deletedId }
                            KlimataPreferences.saveRooms(context, rooms)
                            if (activeRoomId == deletedId) {
                                activeRoomId = rooms.firstOrNull()?.id
                            }
                        },
                        onAddRoomClick = {
                            navigateSafely(Screen.AddRoom.route)
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                detailPageComposable(
                    route = Screen.Settings.route
                ) {
                    SettingsScreen(
                        currentPhase = selectedPhase,
                        onPhaseChange = { selectedPhase = it },
                        isStatusBarDisabled = isStatusBarDisabled,
                        onStatusBarDisabledToggle = { disabled ->
                            isStatusBarDisabled = disabled
                            KlimataPreferences.setStatusBarDisabled(context, disabled)
                        },
                        energyConfig = energyConfig,
                        onEnergyConfigUpdated = { newConfig ->
                            energyConfig = newConfig
                            val report = weatherReport
                            rooms = rooms.map { room ->
                                val computation = ThermalCalculationEngine.computeRoomThermalDynamics(
                                    areaSquareMeters = room.areaSquareMeters,
                                    ceilingHeightMeters = room.ceilingHeightMeters,
                                    thermalMassType = room.thermalMassLabel,
                                    acCapacity = room.profile.capacity,
                                    acInverterType = room.profile.inverterType,
                                    targetTemp = room.profile.currentSetpoint,
                                    hourlyOutdoorTemps = report?.hourlyTemps ?: emptyMap(),
                                    energyConfig = newConfig
                                )
                                room.copy(
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
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
