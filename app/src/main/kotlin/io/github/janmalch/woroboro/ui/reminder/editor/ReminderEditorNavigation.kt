package io.github.janmalch.woroboro.ui.reminder.editor

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


        CollectAsEvents(viewModel.onSaveFinished) {
            when (it) {
                Outcome.Success -> {
                    onShowSnackbar("Erinnerung erfolgreich gespeichert.")
                    onBackClick()
                }

                Outcome.Failure -> {
                    onShowSnackbar("Fehler beim Speichern der Erinnerung.")
                }
            }
        }

        CollectAsEvents(viewModel.onDeleteFinished) {
            when (it) {
                Outcome.Success -> {
                    onShowSnackbar("Erinnerung erfolgreich gelöscht.")
                    onBackClick()
                }

                Outcome.Failure -> {
                    onShowSnackbar("Fehler beim Löschen der Erinnerung.")
                }
            }
        }

        ReminderEditorScreen(
            reminder = reminder,
            availableTags = availableTags,
            onSave = viewModel::save,
            onDelete = viewModel::delete,
            onBackClick = onBackClick,
        )
    }
}
