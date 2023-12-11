package io.github.janmalch.woroboro.ui.reminder

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import java.util.UUID


const val REMINDER_LIST_ROUTE = "$REMINDER_GRAPH_ROUTE/reminder-list"

fun NavController.navigateToReminderList(navOptions: NavOptions? = null) {
    this.navigate(REMINDER_LIST_ROUTE, navOptions)
}

fun NavGraphBuilder.reminderListScreen(
    onNewReminder: () -> Unit,
    onGoToReminder: (UUID) -> Unit,
) {
    composable(
        route = REMINDER_LIST_ROUTE,
    ) {
        val viewModel = hiltViewModel<ReminderListViewModel>()
        val reminders by viewModel.reminders.collectAsState()

        ReminderListScreen(
            reminders = reminders,
            onNewReminder = onNewReminder,
            onGoToReminder = onGoToReminder,
        )
    }
}
