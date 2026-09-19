package com.example.klimata.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutLinearInEasing
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.klimata.data.MockData
import com.example.klimata.ui.KlimataScreen
import com.example.klimata.ui.screens.CarbonDetailScreen
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
    data object SavingsDetail : Screen("savings/{roomId}") {
        fun createRoute(roomId: String) = "savings/$roomId"
    }
    data object CarbonDetail : Screen("carbon/{roomId}") {
        fun createRoute(roomId: String) = "carbon/$roomId"
    }
}

/**
 * Root navigation graph managing bottom-sheet style vertical transitions
 * across telemetry detail destinations while preserving the atmospheric sky canvas.
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
                        fadeOut(animationSpec = tween(220))
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(260))
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
                            towards = AnimatedContentTransitionScope.SlideDirection.Up,
                            animationSpec = tween(380, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(320))
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Down,
                            animationSpec = tween(320, easing = FastOutLinearInEasing)
                        ) + fadeOut(animationSpec = tween(240))
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Down,
                            animationSpec = tween(320, easing = FastOutLinearInEasing)
                        ) + fadeOut(animationSpec = tween(240))
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
                    route = Screen.SavingsDetail.route,
                    arguments = listOf(navArgument("roomId") { type = NavType.StringType }),
                    enterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Up,
                            animationSpec = tween(380, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(320))
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Down,
                            animationSpec = tween(320, easing = FastOutLinearInEasing)
                        ) + fadeOut(animationSpec = tween(240))
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Down,
                            animationSpec = tween(320, easing = FastOutLinearInEasing)
                        ) + fadeOut(animationSpec = tween(240))
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
                            towards = AnimatedContentTransitionScope.SlideDirection.Up,
                            animationSpec = tween(380, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(320))
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Down,
                            animationSpec = tween(320, easing = FastOutLinearInEasing)
                        ) + fadeOut(animationSpec = tween(240))
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Down,
                            animationSpec = tween(320, easing = FastOutLinearInEasing)
                        ) + fadeOut(animationSpec = tween(240))
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
