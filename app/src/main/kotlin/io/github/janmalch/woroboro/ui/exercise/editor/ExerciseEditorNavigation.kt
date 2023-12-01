package io.github.janmalch.woroboro.ui.exercise.editor

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import io.github.janmalch.woroboro.ui.CollectAsEvents
import io.github.janmalch.woroboro.ui.Outcome
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
    onShowSnackbar: (String) -> Unit,
) {
    composable(
        route = EXERCISE_EDITOR_ROUTE_PATTERN,
        arguments = listOf(
            navArgument(ARGUMENT) {
                type = NavType.StringType
                nullable = true
            }
        ),
        enterTransition = { fadeIn() + slideInVertically { it / 2 } },
        exitTransition = { fadeOut() + slideOutVertically { it / 2 } },
    ) {
        val viewModel = hiltViewModel<ExerciseEditorViewModel>()
        val exercise by viewModel.exerciseToEdit.collectAsState()
        val availableTags by viewModel.availableTags.collectAsState()
        val isLoading = viewModel.isLoading

        CollectAsEvents(viewModel.onSaveFinished) {
            when (it) {
                Outcome.Success -> {
                    onShowSnackbar("Übung erfolgreich gespeichert.")
                    onBackClick()
                }

                Outcome.Failure -> {
                    onShowSnackbar("Fehler beim Speichern der Übung.")
                }
            }
        }

        CollectAsEvents(viewModel.onDeleteFinished) {
            when (it) {
                Outcome.Success -> {
                    onShowSnackbar("Übung erfolgreich gelöscht.")
                    onBackClick()
                }

                Outcome.Failure -> {
                    onShowSnackbar("Fehler beim Löschen der Übung.")
                }
            }
        }

        CollectAsEvents(viewModel.onAddToRoutineFinished) {
            when (it) {
                Outcome.Success -> {
                    onShowSnackbar("Übung erfolgreich zu Routine hinzugefügt.")
                }

                Outcome.Failure -> {
                    onShowSnackbar("Fehler beim Hinzufügen zur Routine.")
                }
            }
        }

        ExerciseEditorScreen(
            availableTags = availableTags,
            isLoading = isLoading,
            exercise = exercise,
            allRoutinesFlow = viewModel.allRoutines,
            onSave = viewModel::save,
            onDelete = viewModel::delete,
            onAddToRoutine = viewModel::addToRoutine,
            onBackClick = onBackClick,
        )
    }
}