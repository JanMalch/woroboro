package io.github.janmalch.woroboro.ui.exercise

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import io.github.janmalch.woroboro.models.Exercise
import io.github.janmalch.woroboro.ui.exercise.editor.exerciseEditorScreen


const val EXERCISES_GRAPH_ROUTE = "exercises"

fun NavController.navigateToExercisesGraph(navOptions: NavOptions? = null) {
    this.navigate(EXERCISES_GRAPH_ROUTE, navOptions)
}

fun NavGraphBuilder.exercisesGraph(
    onCreateExerciseClick: () -> Unit,
    onExerciseClick: (Exercise) -> Unit,
    onBackClick: () -> Unit,
) {
    navigation(
        route = EXERCISES_GRAPH_ROUTE,
        startDestination = EXERCISE_LIST_ROUTE,
    ) {
        exerciseListScreen(
            onCreateExerciseClick = onCreateExerciseClick,
            onExerciseClick = onExerciseClick,
        )
        exerciseEditorScreen(onBackClick = onBackClick)
    }

}