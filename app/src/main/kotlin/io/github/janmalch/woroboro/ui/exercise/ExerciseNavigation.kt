package io.github.janmalch.woroboro.ui.exercise

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import io.github.janmalch.woroboro.ui.exercise.editor.exerciseEditorScreen
import io.github.janmalch.woroboro.ui.exercise.tageditor.tagEditorScreen
import java.util.UUID


const val EXERCISES_GRAPH_ROUTE = "exercises"

fun NavController.navigateToExercisesGraph(navOptions: NavOptions? = null) {
    this.navigate(EXERCISES_GRAPH_ROUTE, navOptions)
}

fun NavGraphBuilder.exercisesGraph(
    onCreateExerciseClick: () -> Unit,
    onNavigateToTagEditor: () -> Unit,
    onExerciseClick: (UUID) -> Unit,
    onBackClick: () -> Unit,
    onShowSnackbar: (String) -> Unit,
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
        exerciseEditorScreen(onBackClick = onBackClick, onShowSnackbar = onShowSnackbar)
        tagEditorScreen(onBackClick = onBackClick)
    }

}