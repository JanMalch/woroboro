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
import io.github.janmalch.woroboro.ui.exercise.tageditor.navigateToTagEditor
import io.github.janmalch.woroboro.ui.more.moreGraph
import io.github.janmalch.woroboro.ui.reminder.editor.navigateToReminderEditor
import io.github.janmalch.woroboro.ui.reminder.remindersGraph
import io.github.janmalch.woroboro.ui.routine.ROUTINE_LIST_ROUTE
import io.github.janmalch.woroboro.ui.routine.editor.navigateToRoutineEditor
import io.github.janmalch.woroboro.ui.routine.routine.navigateToRoutineScreen
import io.github.janmalch.woroboro.ui.routine.routinesGraph
import io.github.janmalch.woroboro.utils.SnackbarAction

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    onShowSnackbar: (String) -> Unit,
    onShowSnackbarAction: (SnackbarAction) -> Unit,
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
            onExerciseClick = navController::navigateToExerciseEditor,
            onNavigateToTagEditor = navController::navigateToTagEditor,
            onBackClick = navController::popBackStack,
            onShowSnackbar = onShowSnackbar,
            onShowSnackbarAction = onShowSnackbarAction,
        )
        routinesGraph(
            onCreateRoutineClick = navController::navigateToRoutineEditor,
            onGoToEditor = navController::navigateToRoutineEditor,
            onRoutineClick = { navController.navigateToRoutineScreen(it.id) },
            onBackClick = navController::popBackStack,
            onBackToRoutineList = {
                navController.popBackStack(ROUTINE_LIST_ROUTE, inclusive = false)
            },
            onShowSnackbar = onShowSnackbar,
        )
        remindersGraph(
            onNewReminder = navController::navigateToReminderEditor,
            onGoToReminder = navController::navigateToReminderEditor,
            onBackClick = navController::popBackStack,
            onShowSnackbar = onShowSnackbar,
        )
    }
}
