package io.github.janmalch.woroboro.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.SportsGymnastics
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.janmalch.woroboro.ui.exercise.EXERCISES_GRAPH_ROUTE
import io.github.janmalch.woroboro.ui.exercise.navigateToExercisesGraph
import io.github.janmalch.woroboro.ui.routine.ROUTINE_GRAPH_ROUTE
import io.github.janmalch.woroboro.ui.routine.navigateToRoutineGraph
import kotlinx.coroutines.launch

@Composable
fun AppContainer(startDestination: String) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AppBottomBar(navController)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal,
                    ),
                ),
        ) {
            AppNavHost(
                navController = navController,
                startDestination = startDestination,
                onShowSnackbar = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(message = it)
                    }
                }
            )
        }
    }
}

@Composable
fun AppBottomBar(navController: NavHostController) {
    val currentBackStack by navController.currentBackStackEntryAsState()
    val isExerciseTabActive =
        currentBackStack?.destination?.route?.startsWith(EXERCISES_GRAPH_ROUTE) ?: false
    val isRoutineTabActive =
        currentBackStack?.destination?.route?.startsWith(ROUTINE_GRAPH_ROUTE) ?: false

    NavigationBar {
        NavigationBarItem(
            selected = isRoutineTabActive,
            onClick = navController::navigateToRoutineGraph,
            icon = { Icon(Icons.Outlined.FitnessCenter, contentDescription = null) },
            label = { Text(text = "Routinen") },
        )
        NavigationBarItem(
            selected = isExerciseTabActive,
            onClick = navController::navigateToExercisesGraph,
            icon = { Icon(Icons.Outlined.SportsGymnastics, contentDescription = null) },
            label = { Text(text = "Übungen") },
        )
        NavigationBarItem(
            selected = false,
            onClick = { /*TODO*/ },
            icon = { Icon(Icons.Outlined.Notifications, contentDescription = null) },
            label = { Text(text = "Erinnerungen") },
        )
    }
}
