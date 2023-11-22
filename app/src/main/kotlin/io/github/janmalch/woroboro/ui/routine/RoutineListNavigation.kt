package io.github.janmalch.woroboro.ui.routine

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import io.github.janmalch.woroboro.models.Routine

const val ROUTINE_LIST_ROUTE = "$ROUTINE_GRAPH_ROUTE/routine-list"

fun NavController.navigateToRoutineList(navOptions: NavOptions? = null) {
    this.navigate(ROUTINE_LIST_ROUTE, navOptions)
}

fun NavGraphBuilder.routineListScreen(
    onCreateRoutineClick: () -> Unit,
    onRoutineClick: (Routine) -> Unit,
) {
    composable(
        route = ROUTINE_LIST_ROUTE,
    ) {
        val viewModel = hiltViewModel<RoutineListViewModel>()
        val routines by viewModel.routines.collectAsState()
        val availableTags by viewModel.availableTags.collectAsState()
        val selectedTags by viewModel.selectedTags.collectAsState()
        val isOnlyFavorites by viewModel.isOnlyFavorites.collectAsState()

        RoutineListScreen(
            routines = routines,
            availableTags = availableTags,
            selectedTags = selectedTags,
            isOnlyFavorites = isOnlyFavorites,
            onOnlyFavoritesChange = viewModel::setOnlyFavorites,
            onSelectedTagsChange = viewModel::changeSelectedTags,
            onToggleFavorite = viewModel::toggleFavorite,
            onCreateRoutineClick = onCreateRoutineClick,
            onRoutineClick = onRoutineClick,
        )
    }
}