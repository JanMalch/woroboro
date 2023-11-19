package io.github.janmalch.woroboro.ui.exercise.editor

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.github.janmalch.woroboro.ui.exercise.EXERCISES_GRAPH_ROUTE
import java.util.UUID


private const val ARGUMENT = "exerciseId"
const val EXERCISE_EDITOR_ROUTE = "$EXERCISES_GRAPH_ROUTE/exercise-editor"
private const val EXERCISE_EDITOR_ROUTE_PATTERN = "$EXERCISE_EDITOR_ROUTE?$ARGUMENT={$ARGUMENT}"

data class ExerciseEditorArgs(
    val exerciseId: UUID?
) {
    constructor(savedStateHandle: SavedStateHandle) : this(
        exerciseId = savedStateHandle.get<String?>(ARGUMENT)?.let(UUID::fromString)
    )
}

fun NavController.navigateToExerciseEditor(
    exerciseId: UUID? = null,
    navOptions: NavOptions? = null,
) {
    if (exerciseId != null) {
        this.navigate("$EXERCISE_EDITOR_ROUTE?$ARGUMENT=$exerciseId", navOptions)
    } else {
        this.navigate(EXERCISE_EDITOR_ROUTE, navOptions)
    }
}

fun NavGraphBuilder.exerciseEditorScreen(
    onBackClick: () -> Unit,
) {
    composable(
        route = EXERCISE_EDITOR_ROUTE_PATTERN,
        arguments = listOf(
            navArgument(ARGUMENT) {
                type = NavType.StringType
                nullable = true
            }
        ),
    ) {
        val viewModel = hiltViewModel<ExerciseEditorViewModel>()
        val exercise by viewModel.exerciseToEdit.collectAsState()
        val availableTags by viewModel.availableTags.collectAsState()

        ExerciseEditorScreen(
            availableTags = availableTags,
            exercise = exercise,
            onSave = viewModel::save,
            onDelete = viewModel::delete,
            onBackClick = onBackClick,
        )
    }
}