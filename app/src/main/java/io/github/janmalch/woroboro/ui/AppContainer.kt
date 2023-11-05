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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.janmalch.woroboro.ui.exercise.EXERCISES_GRAPH_ROUTE
import io.github.janmalch.woroboro.ui.exercise.navigateToExercisesGraph

@Composable
fun AppContainer(startDestination: String) {
    val navController = rememberNavController()
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AppBottomBar(navController)
        },
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
                startDestination = startDestination
            )
        }
    }
}

@Composable
fun AppBottomBar(navController: NavHostController) {
    val currentBackStack by navController.currentBackStackEntryAsState()
    val isExerciseTabActive =
        currentBackStack?.destination?.route?.startsWith(EXERCISES_GRAPH_ROUTE) ?: false

    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = { /*TODO*/ },
            icon = { Icon(Icons.Outlined.FitnessCenter, contentDescription = null) },
            label = { Text(text = "Routinen") },
        )
        NavigationBarItem(
            selected = isExerciseTabActive,
            onClick = { navController.navigateToExercisesGraph() },
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
