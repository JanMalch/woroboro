package io.github.janmalch.woroboro.ui.routine

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import io.github.janmalch.woroboro.models.Routine

const val ROUTINE_LIST_ROUTE = "$ROUTINE_GRAPH_ROUTE/routine-list"

val NavDestination.isRoutineList: Boolean get() = route == ROUTINE_LIST_ROUTE

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
        val uiState by viewModel.uiState.collectAsState()
        val textQuery by viewModel.textQuery.collectAsState()
        val durationFilter by viewModel.durationFilter.collectAsState()
        val isOnlyFavorites by viewModel.isOnlyFavorites.collectAsState()

        when (val state = uiState) {
            RoutineListUiState.Failure -> {
                RoutineListErrorScreen()
            }

            RoutineListUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is RoutineListUiState.Success -> {
                RoutineListScreen(
                    routines = state.routines,
                    availableTags = state.availableTags,
                    selectedTags = state.selectedTags,
                    isOnlyFavorites = isOnlyFavorites,
                    durationFilter = durationFilter,
                    textQuery = textQuery,
                    onTextQueryChange = viewModel::setTextQuery,
                    onDurationFilterChange = viewModel::setDurationFilter,
                    onOnlyFavoritesChange = viewModel::setOnlyFavorites,
                    onSelectedTagsChange = viewModel::changeSelectedTags,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onCreateRoutineClick = onCreateRoutineClick,
                    onRoutineClick = onRoutineClick,
                )
            }
        }

        ReportDrawnWhen { uiState !is RoutineListUiState.Loading }
    }
}

@Composable
private fun RoutineListErrorScreen() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Routinen") },
            )
        },
    ) { padding ->

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Text(
                text = "Leider ist ein unbekannter Fehler aufgetreten.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 48.dp)
            )
        }
    }
}