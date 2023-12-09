package io.github.janmalch.woroboro.ui.reminder

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable


const val REMINDER_LIST_ROUTE = "$REMINDER_GRAPH_ROUTE/reminder-list"

fun NavController.navigateToReminderList(navOptions: NavOptions? = null) {
    this.navigate(REMINDER_LIST_ROUTE, navOptions)
}

fun NavGraphBuilder.reminderListScreen(
) {
    composable(
        route = REMINDER_LIST_ROUTE,
    ) {
        val viewModel = hiltViewModel<ReminderViewModel>()
        val reminders by viewModel.reminders.collectAsState()
        val availableTags by viewModel.availableTags.collectAsState()

        ReminderListScreen(
            reminders = reminders,
            availableTags = availableTags,
            onInsert = viewModel::insert,
            onUpdate = viewModel::update,
            onDelete = viewModel::delete,
        )
    }
}
