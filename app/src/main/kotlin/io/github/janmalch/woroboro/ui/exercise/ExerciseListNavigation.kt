package io.github.janmalch.woroboro.ui.exercise

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import java.util.UUID

const val EXERCISE_LIST_ROUTE = "$EXERCISES_GRAPH_ROUTE/exercise-list"

fun NavController.navigateToExerciseList(navOptions: NavOptions? = null) {
    this.navigate(EXERCISE_LIST_ROUTE, navOptions)
}

fun NavGraphBuilder.exerciseListScreen(
    onCreateExerciseClick: () -> Unit,
    onNavigateToTagEditor: () -> Unit,
    onExerciseClick: (UUID) -> Unit,
) {
    composable(
        route = EXERCISE_LIST_ROUTE,
    ) {
        val viewModel = hiltViewModel<ExerciseListViewModel>()
        val exercises by viewModel.exercises.collectAsState()
        val textQuery by viewModel.textQuery.collectAsState()
        val availableTags by viewModel.availableTags.collectAsState()
        val selectedTags by viewModel.selectedTags.collectAsState()
        val isOnlyFavorites by viewModel.isOnlyFavorites.collectAsState()

        ExerciseListScreen(
            exercises = exercises,
            availableTags = availableTags,
            selectedTags = selectedTags,
            isOnlyFavorites = isOnlyFavorites,
            textQuery = textQuery,
            onTextQueryChange = viewModel::setTextQuery,
            onOnlyFavoritesChange = viewModel::setOnlyFavorites,
            onSelectedTagsChange = viewModel::changeSelectedTags,
            onToggleFavorite = viewModel::toggleFavorite,
            onCreateExerciseClick = onCreateExerciseClick,
            onExerciseClick = onExerciseClick,
            onNavigateToTagEditor = onNavigateToTagEditor,
        )
    }
}
