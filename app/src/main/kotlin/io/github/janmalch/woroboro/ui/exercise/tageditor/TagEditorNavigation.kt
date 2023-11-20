package io.github.janmalch.woroboro.ui.exercise.tageditor

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import io.github.janmalch.woroboro.ui.exercise.EXERCISES_GRAPH_ROUTE

private const val TAG_EDITOR_ROUTE = "$EXERCISES_GRAPH_ROUTE/tag-editor"

fun NavController.navigateToTagEditor(navOptions: NavOptions? = null) {
    this.navigate(TAG_EDITOR_ROUTE, navOptions)
}

fun NavGraphBuilder.tagEditorScreen(
    onBackClick: () -> Unit,
) {
    composable(
        route = TAG_EDITOR_ROUTE,
    ) {
        val viewModel = hiltViewModel<TagEditorViewModel>()
        val groupedTags by viewModel.groupedTags.collectAsState()

        TagEditorScreen(
            value = groupedTags,
            onAddTag = viewModel::addTag,
            onUpdateTag = viewModel::updateTag,
            onDeleteTag = viewModel::deleteTag,
            onBackClick = onBackClick,
        )
    }
}
