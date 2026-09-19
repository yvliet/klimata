package com.example.klimata.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.klimata.data.MockData
import com.example.klimata.ui.KlimataScreen
import com.example.klimata.ui.screens.CarbonDetailScreen
import com.example.klimata.ui.screens.RoomThermalDetailScreen
import com.example.klimata.ui.screens.SavingsDetailScreen
import com.example.klimata.ui.screens.ScheduleDetailScreen
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
}

/**
 * Root navigation graph managing horizontal slide-only transitions across detail
 * destinations while preserving the atmospheric sky canvas.
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

    var rooms by remember { mutableStateOf(MockData.rooms) }

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
                            animationSpec = tween(380, easing = FastOutSlowInEasing)
                        ) { fullWidth -> (fullWidth * 0.28f).toInt() }
                    },
                    popEnterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(380, easing = FastOutSlowInEasing)
                        ) { fullWidth -> (fullWidth * 0.28f).toInt() }
                    }
                ) {
                    KlimataScreen(
                        initialPhase = selectedPhase,
                        rooms = rooms,
                        onEcoToggle = { roomId, isEnabled ->
                            rooms = rooms.map {
                                if (it.id == roomId) it.copy(isEcoEnabled = isEnabled) else it
                            }
                        },
                        onScheduleClick = { roomId ->
                            navController.navigate(Screen.ScheduleDetail.createRoute(roomId))
                        },
                        onThermalClick = { roomId ->
                            navController.navigate(Screen.RoomThermalDetail.createRoute(roomId))
                        },
                        onSavingsClick = { roomId ->
                            navController.navigate(Screen.SavingsDetail.createRoute(roomId))
                        },
                        onCarbonClick = { roomId ->
                            navController.navigate(Screen.CarbonDetail.createRoute(roomId))
                        }
                    )
                }

                composable(
                    route = Screen.ScheduleDetail.route,
                    arguments = listOf(navArgument("roomId") { type = NavType.StringType }),
                    enterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(380, easing = FastOutSlowInEasing)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(350, easing = FastOutSlowInEasing)
                        )
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(350, easing = FastOutSlowInEasing)
                        )
                    }
                ) { backStackEntry ->
                    val roomId = backStackEntry.arguments?.getString("roomId")
                    val room = rooms.firstOrNull { it.id == roomId } ?: MockData.masterBedRoom
                    ScheduleDetailScreen(
                        room = room,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.RoomThermalDetail.route,
                    arguments = listOf(navArgument("roomId") { type = NavType.StringType }),
                    enterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(380, easing = FastOutSlowInEasing)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(350, easing = FastOutSlowInEasing)
                        )
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(350, easing = FastOutSlowInEasing)
                        )
                    }
                ) { backStackEntry ->
                    val roomId = backStackEntry.arguments?.getString("roomId")
                    val room = rooms.firstOrNull { it.id == roomId } ?: MockData.masterBedRoom
                    RoomThermalDetailScreen(
                        room = room,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.SavingsDetail.route,
                    arguments = listOf(navArgument("roomId") { type = NavType.StringType }),
                    enterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(380, easing = FastOutSlowInEasing)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(350, easing = FastOutSlowInEasing)
                        )
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(350, easing = FastOutSlowInEasing)
                        )
                    }
                ) { backStackEntry ->
                    val roomId = backStackEntry.arguments?.getString("roomId")
                    val room = rooms.firstOrNull { it.id == roomId } ?: MockData.masterBedRoom
                    SavingsDetailScreen(
                        room = room,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.CarbonDetail.route,
                    arguments = listOf(navArgument("roomId") { type = NavType.StringType }),
                    enterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(380, easing = FastOutSlowInEasing)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(350, easing = FastOutSlowInEasing)
                        )
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(350, easing = FastOutSlowInEasing)
                        )
                    }
                ) { backStackEntry ->
                    val roomId = backStackEntry.arguments?.getString("roomId")
                    val room = rooms.firstOrNull { it.id == roomId } ?: MockData.masterBedRoom
                    CarbonDetailScreen(
                        room = room,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
