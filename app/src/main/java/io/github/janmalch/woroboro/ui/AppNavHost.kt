package io.github.janmalch.woroboro.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import io.github.janmalch.woroboro.ui.exercise.editor.navigateToExerciseEditor
import io.github.janmalch.woroboro.ui.exercise.exercisesGraph

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(200))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(200))
        },
    ) {
        exercisesGraph(
            onCreateExerciseClick = navController::navigateToExerciseEditor,
            onExerciseClick = { navController.navigateToExerciseEditor(exerciseId = it.id) },
            onBackClick = navController::popBackStack,
        )
    }
}
