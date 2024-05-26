package io.github.janmalch.woroboro.ui.reminder

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navigation
import io.github.janmalch.woroboro.ui.reminder.editor.reminderEditorScreen
import java.util.UUID

const val REMINDER_GRAPH_ROUTE = "reminders"

fun NavController.navigateToReminderGraph(navOptions: NavOptions? = null) {
    this.navigate(REMINDER_GRAPH_ROUTE, navOptions)
}

fun NavController.navigateToReminderGraph(builder: NavOptionsBuilder.() -> Unit) {
    this.navigate(REMINDER_GRAPH_ROUTE, builder)
}

fun NavGraphBuilder.remindersGraph(
    onNewReminder: () -> Unit,
    onGoToReminder: (UUID) -> Unit,
    onBackClick: () -> Unit,
    onShowSnackbar: (String) -> Unit,
) {
    navigation(
        route = REMINDER_GRAPH_ROUTE,
        startDestination = REMINDER_LIST_ROUTE,
    ) {
        reminderListScreen(
            onNewReminder = onNewReminder,
            onGoToReminder = onGoToReminder,
            onShowSnackbar = onShowSnackbar,
        )
        reminderEditorScreen(
            onBackClick = onBackClick,
            onShowSnackbar = onShowSnackbar,
        )
    }
}
