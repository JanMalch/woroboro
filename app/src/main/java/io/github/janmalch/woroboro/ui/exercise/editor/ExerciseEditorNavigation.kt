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
import java.util.UUID


const val EXERCISE_EDITOR_ROUTE = "exercise-editor"
private const val ARGUMENT = "exerciseId"

data class ExerciseEditorArgs(
    val exerciseId: UUID?
) {
    constructor(savedStateHandle: SavedStateHandle) : this(
        exerciseId = savedStateHandle.get<String?>(ARGUMENT)?.let(UUID::fromString)
    )
}

fun NavController.navigateToExerciseEditor(navOptions: NavOptions? = null) {
    this.navigate(EXERCISE_EDITOR_ROUTE, navOptions)
}

fun NavGraphBuilder.exerciseEditorScreen() {
    composable(
        route = EXERCISE_EDITOR_ROUTE,
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
        )
    }
}