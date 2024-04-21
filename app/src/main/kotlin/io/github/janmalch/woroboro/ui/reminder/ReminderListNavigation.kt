package io.github.janmalch.woroboro.ui.reminder

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import io.github.janmalch.woroboro.R
import io.github.janmalch.woroboro.ui.CollectAsEvents
import io.github.janmalch.woroboro.ui.Outcome
import java.util.UUID


const val REMINDER_LIST_ROUTE = "$REMINDER_GRAPH_ROUTE/reminder-list"

fun NavController.navigateToReminderList(navOptions: NavOptions? = null) {
    this.navigate(REMINDER_LIST_ROUTE, navOptions)
}

fun NavGraphBuilder.reminderListScreen(
    onNewReminder: () -> Unit,
    onGoToReminder: (UUID) -> Unit,
    onShowSnackbar: (String) -> Unit,
) {
    composable(
        route = REMINDER_LIST_ROUTE,
    ) {
        val context = LocalContext.current
        val viewModel = hiltViewModel<ReminderListViewModel>()
        val reminders by viewModel.reminders.collectAsState()

        CollectAsEvents(viewModel.onToggleReminder) {
            if (it == Outcome.Failure) {
                onShowSnackbar(context.getString(R.string.unknown_error_message))
            }
        }

        ReminderListScreen(
            reminders = reminders,
            onNewReminder = onNewReminder,
            onGoToReminder = onGoToReminder,
            onToggleReminderActive = viewModel::toggleReminderActive,
        )
    }
}
