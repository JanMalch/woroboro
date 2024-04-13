package io.github.janmalch.woroboro.ui.exercise.editor

import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.github.janmalch.woroboro.R
import io.github.janmalch.woroboro.ui.CollectAsEvents
import io.github.janmalch.woroboro.ui.Outcome
import io.github.janmalch.woroboro.ui.components.NavigationDefaults
import io.github.janmalch.woroboro.ui.exercise.EXERCISES_GRAPH_ROUTE
import io.github.janmalch.woroboro.utils.SnackbarAction
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
    onShowSnackbarAction: (SnackbarAction) -> Unit,
    onNavigateToExerciseEditor: (exerciseId: UUID) -> Unit,
) {
    composable(
        route = EXERCISE_EDITOR_ROUTE_PATTERN,
        arguments = listOf(
            navArgument(ARGUMENT) {
                type = NavType.StringType
                nullable = true
            }
        ),
        enterTransition = NavigationDefaults.enterEditorTransition,
        exitTransition = NavigationDefaults.exitEditorTransition,
    ) {
        val viewModel = hiltViewModel<ExerciseEditorViewModel>()
        val exercise by viewModel.exerciseToEdit.collectAsState()
        val availableTags by viewModel.availableTags.collectAsState()
        val isLoading = viewModel.isLoading
        val context = LocalContext.current

        CollectAsEvents(viewModel.onSaveFinished) {
            when (it) {
                Outcome.Success -> {
                    onShowSnackbarAction(
                        SnackbarAction(
                            message = context.getString(R.string.exercise_save_success),
                            actionLabel = context.getString(R.string.exercise_save_success_action_edit),
                        ) { res ->
                            if (res == SnackbarResult.ActionPerformed) {
                                val exerciseId = exercise?.id
                                if (exerciseId != null) {
                                    onNavigateToExerciseEditor(exerciseId)
                                } else {
                                    onShowSnackbar(context.getString(R.string.unknown_error_message))
                                }
                            }
                        }
                    )
                    onBackClick()
                }

                Outcome.Failure -> {
                    onShowSnackbar(context.getString(R.string.exercise_save_error))
                }
            }
        }

        CollectAsEvents(viewModel.onDeleteFinished) {
            when (it) {
                Outcome.Success -> {
                    onShowSnackbar(context.getString(R.string.exercise_delete_success))
                    onBackClick()
                }

                Outcome.Failure -> {
                    onShowSnackbar(context.getString(R.string.exercise_delete_error))
                }
            }
        }

        CollectAsEvents(viewModel.onAddToRoutineFinished) {
            when (it) {
                Outcome.Success -> {
                    onShowSnackbar(context.getString(R.string.exercise_add_to_routine_success))
                }

                Outcome.Failure -> {
                    onShowSnackbar(context.getString(R.string.exercise_add_to_routine_error))
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