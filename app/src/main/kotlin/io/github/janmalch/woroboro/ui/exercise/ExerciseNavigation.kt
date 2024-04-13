package io.github.janmalch.woroboro.ui.exercise

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navigation
import io.github.janmalch.woroboro.ui.exercise.editor.exerciseEditorScreen
import io.github.janmalch.woroboro.ui.exercise.tageditor.tagEditorScreen
import io.github.janmalch.woroboro.utils.SnackbarAction
import java.util.UUID


const val EXERCISES_GRAPH_ROUTE = "exercises"

fun NavController.navigateToExercisesGraph(navOptions: NavOptions? = null) {
    this.navigate(EXERCISES_GRAPH_ROUTE, navOptions)
}

fun NavController.navigateToExercisesGraph(builder: NavOptionsBuilder.() -> Unit) {
    this.navigate(EXERCISES_GRAPH_ROUTE, builder)
}

fun NavGraphBuilder.exercisesGraph(
    onCreateExerciseClick: () -> Unit,
    onNavigateToTagEditor: () -> Unit,
    onExerciseClick: (UUID) -> Unit,
    onBackClick: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    onShowSnackbarAction: (SnackbarAction) -> Unit,
) {
    navigation(
        route = EXERCISES_GRAPH_ROUTE,
        startDestination = EXERCISE_LIST_ROUTE,
    ) {
        exerciseListScreen(
            onCreateExerciseClick = onCreateExerciseClick,
            onExerciseClick = onExerciseClick,
            onNavigateToTagEditor = onNavigateToTagEditor,
        )
        exerciseEditorScreen(
            onBackClick = onBackClick,
            onShowSnackbar = onShowSnackbar,
            onShowSnackbarAction = onShowSnackbarAction,
            onNavigateToExerciseEditor = onExerciseClick,
        )
        tagEditorScreen(onBackClick = onBackClick)
    }

}