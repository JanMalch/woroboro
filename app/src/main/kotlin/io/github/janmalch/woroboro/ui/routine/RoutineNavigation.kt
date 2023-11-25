package io.github.janmalch.woroboro.ui.routine

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import io.github.janmalch.woroboro.models.FullRoutine
import io.github.janmalch.woroboro.models.Routine
import io.github.janmalch.woroboro.ui.routine.routine.routineScreen


const val ROUTINE_GRAPH_ROUTE = "routines"

fun NavController.navigateToRoutineGraph(navOptions: NavOptions? = null) {
    this.navigate(ROUTINE_GRAPH_ROUTE, navOptions)
}

fun NavGraphBuilder.routinesGraph(
    onCreateRoutineClick: () -> Unit,
    onRoutineClick: (Routine) -> Unit,
    onBackClick: () -> Unit,
) {
    navigation(
        route = ROUTINE_GRAPH_ROUTE,
        startDestination = ROUTINE_LIST_ROUTE,
    ) {
        routineListScreen(
            onCreateRoutineClick = onCreateRoutineClick,
            onRoutineClick = onRoutineClick,
        )
        routineScreen(
            onBackClick = onBackClick,

            )
    }

}