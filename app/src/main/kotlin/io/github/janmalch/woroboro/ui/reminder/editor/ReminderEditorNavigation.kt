package io.github.janmalch.woroboro.ui.reminder.editor

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
import io.github.janmalch.woroboro.ui.reminder.REMINDER_GRAPH_ROUTE
import java.util.UUID


private const val ARGUMENT = "routineId"
const val REMINDER_EDITOR_ROUTE = "$REMINDER_GRAPH_ROUTE/reminder-editor"
private const val REMINDER_EDITOR_ROUTE_PATTERN = "$REMINDER_EDITOR_ROUTE?${ARGUMENT}={${ARGUMENT}}"


data class RoutineEditorArgs(
    val reminderId: UUID?
) {
    constructor(savedStateHandle: SavedStateHandle) : this(
        reminderId = savedStateHandle.get<String?>(ARGUMENT)?.let(UUID::fromString)
    )
}

fun NavController.navigateToReminderEditor(
    reminderId: UUID? = null,
    navOptions: NavOptions? = null
) {
    if (reminderId != null) {
        this.navigate("$REMINDER_EDITOR_ROUTE?$ARGUMENT=$reminderId", navOptions)
    } else {
        this.navigate(REMINDER_EDITOR_ROUTE, navOptions)
    }
}

fun NavGraphBuilder.reminderEditorScreen(
    onBackClick: () -> Unit,
    onShowSnackbar: (String) -> Unit,
) {
    composable(
        route = REMINDER_EDITOR_ROUTE_PATTERN,
        arguments = listOf(
            navArgument(ARGUMENT) {
                type = NavType.StringType
                nullable = true
            }
        ),
        enterTransition = NavigationDefaults.enterEditorTransition,
        exitTransition = NavigationDefaults.exitEditorTransition,
    ) {
        val viewModel = hiltViewModel<ReminderEditorViewModel>()
        val reminder by viewModel.reminderToEdit.collectAsState()
        val availableTags by viewModel.availableTags.collectAsState()
        val routines by viewModel.routines.collectAsState()
        val isLoading = viewModel.isLoading
        val context = LocalContext.current

        CollectAsEvents(viewModel.onSaveFinished) {
            when (it) {
                Outcome.Success -> {
                    onShowSnackbar(context.getString(R.string.reminder_save_success))
                    onBackClick()
                }

                Outcome.Failure -> {
                    onShowSnackbar(context.getString(R.string.reminder_save_error))
                }
            }
        }

        CollectAsEvents(viewModel.onDeleteFinished) {
            when (it) {
                Outcome.Success -> {
                    onShowSnackbar(context.getString(R.string.reminder_delete_success))
                    onBackClick()
                }

                Outcome.Failure -> {
                    onShowSnackbar(context.getString(R.string.reminder_delete_error))
                }
            }
        }

        ReminderEditorScreen(
            reminder = reminder,
            isLoading = isLoading,
            availableTags = availableTags,
            routines = routines,
            onSave = viewModel::save,
            onDelete = viewModel::delete,
            onBackClick = onBackClick,
        )
    }
}
