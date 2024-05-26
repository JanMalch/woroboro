package io.github.janmalch.woroboro.ui.routine.editor

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
import io.github.janmalch.woroboro.ui.routine.ROUTINE_GRAPH_ROUTE
import java.util.UUID

private const val ARGUMENT = "routineId"
const val ROUTINE_EDITOR_ROUTE = "$ROUTINE_GRAPH_ROUTE/routine-editor"
private const val ROUTINE_EDITOR_ROUTE_PATTERN = "$ROUTINE_EDITOR_ROUTE?$ARGUMENT={$ARGUMENT}"

data class RoutineEditorArgs(val routineId: UUID?) {
    constructor(
        savedStateHandle: SavedStateHandle
    ) : this(routineId = savedStateHandle.get<String?>(ARGUMENT)?.let(UUID::fromString))
}

fun NavController.navigateToRoutineEditor(
    routineId: UUID? = null,
    navOptions: NavOptions? = null,
) {
    if (routineId != null) {
        this.navigate("$ROUTINE_EDITOR_ROUTE?$ARGUMENT=$routineId", navOptions)
    } else {
        this.navigate(ROUTINE_EDITOR_ROUTE, navOptions)
    }
}

fun NavGraphBuilder.routineEditorScreen(
    onBackClick: () -> Unit,
    onBackToRoutineList: () -> Unit,
    onShowSnackbar: (String) -> Unit,
) {
    composable(
        route = ROUTINE_EDITOR_ROUTE_PATTERN,
        arguments =
            listOf(
                navArgument(ARGUMENT) {
                    type = NavType.StringType
                    nullable = true
                }
            ),
        enterTransition = NavigationDefaults.enterEditorTransition,
        exitTransition = NavigationDefaults.exitEditorTransition,
    ) {
        val viewModel = hiltViewModel<RoutineEditorViewModel>()
        val routine by viewModel.routineToEdit.collectAsState()
        val allExercises by viewModel.allExercises.collectAsState()
        val isLoading = viewModel.isLoading
        val context = LocalContext.current

        CollectAsEvents(viewModel.onSaveFinished) {
            when (it) {
                Outcome.Success -> {
                    onShowSnackbar(context.getString(R.string.routine_save_success))
                    onBackClick()
                }
                Outcome.Failure -> {
                    onShowSnackbar(context.getString(R.string.routine_save_error))
                }
            }
        }

        CollectAsEvents(viewModel.onDeleteFinished) {
            when (it) {
                Outcome.Success -> {
                    onShowSnackbar(context.getString(R.string.routine_delete_success))
                    onBackToRoutineList()
                }
                Outcome.Failure -> {
                    onShowSnackbar(context.getString(R.string.routine_delete_error))
                }
            }
        }

        RoutineEditorScreen(
            routine = routine,
            isLoading = isLoading,
            allExercises = allExercises,
            onSave = viewModel::save,
            onDelete = viewModel::delete,
            onBackClick = onBackClick,
        )
    }
}
