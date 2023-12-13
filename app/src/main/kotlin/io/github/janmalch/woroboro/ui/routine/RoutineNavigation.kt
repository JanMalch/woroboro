package io.github.janmalch.woroboro.ui.routine

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navigation
import io.github.janmalch.woroboro.models.Routine
import io.github.janmalch.woroboro.ui.routine.editor.routineEditorScreen
import io.github.janmalch.woroboro.ui.routine.routine.routineScreen
import java.util.UUID


const val ROUTINE_GRAPH_ROUTE = "routines"

fun NavController.navigateToRoutineGraph(navOptions: NavOptions? = null) {
    this.navigate(ROUTINE_GRAPH_ROUTE, navOptions)
}

fun NavController.navigateToRoutineGraph(builder: NavOptionsBuilder.() -> Unit) {
    this.navigate(ROUTINE_GRAPH_ROUTE, builder)
}

fun NavGraphBuilder.routinesGraph(
    onCreateRoutineClick: () -> Unit,
    onRoutineClick: (Routine) -> Unit,
    onGoToEditor: (UUID) -> Unit,
    onBackClick: () -> Unit,
    onBackToRoutineList: () -> Unit,
    onShowSnackbar: (String) -> Unit,
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
            onGoToEditor = onGoToEditor,
        )
        routineEditorScreen(
            onBackClick = onBackClick,
            onBackToRoutineList = onBackToRoutineList,
            onShowSnackbar = onShowSnackbar,
        )
    }

}