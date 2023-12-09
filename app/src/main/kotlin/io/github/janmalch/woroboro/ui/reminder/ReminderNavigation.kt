package io.github.janmalch.woroboro.ui.reminder

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation


const val REMINDER_GRAPH_ROUTE = "reminders"

fun NavController.navigateToReminderGraph(navOptions: NavOptions? = null) {
    this.navigate(REMINDER_GRAPH_ROUTE, navOptions)
}

fun NavGraphBuilder.remindersGraph(
    onBackClick: () -> Unit,
    onShowSnackbar: (String) -> Unit,
) {
    navigation(
        route = REMINDER_GRAPH_ROUTE,
        startDestination = REMINDER_LIST_ROUTE,
    ) {
        reminderListScreen()
    }

}