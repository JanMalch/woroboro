package io.github.janmalch.woroboro.ui.exercise

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import io.github.janmalch.woroboro.models.Exercise
import io.github.janmalch.woroboro.models.Tag
import io.github.janmalch.woroboro.ui.components.ExerciseListItem
import io.github.janmalch.woroboro.ui.components.common.FavoriteIcon
import io.github.janmalch.woroboro.ui.components.common.MoreMenu
import io.github.janmalch.woroboro.ui.components.common.OnlyFavoritesChip
import io.github.janmalch.woroboro.ui.components.tags.TagSelectors
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

@Composable
fun ExerciseListScreen(
    exercises: ImmutableList<Exercise>,
    availableTags: ImmutableMap<String, ImmutableList<String>>,
    selectedTags: ImmutableList<Tag>,
    isOnlyFavorites: Boolean,
    onOnlyFavoritesChange: (Boolean) -> Unit,
    onSelectedTagsChange: (List<Tag>) -> Unit,
    onCreateExerciseClick: () -> Unit,
    onToggleFavorite: (Exercise) -> Unit,
    onExerciseClick: (Exercise) -> Unit,
    onNavigateToTagEditor: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val isTopBarCollapsed by remember {
        derivedStateOf {
            // TODO: not quite right when list doesn't overflow screen
            scrollBehavior.state.collapsedFraction == 1.0f
        }
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Übungen")
                },
                actions = {
                    MoreMenu {
                        DropdownMenuItem(
                            text = { Text(text = "Tags bearbeiten") },
                            onClick = onNavigateToTagEditor
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                icon = {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                },
                text = {
                    Text(text = "Neue Übung")
                },
                onClick = onCreateExerciseClick
            )
        }
    ) { padding ->
        ExerciseList(
            exercises = exercises,
            availableTags = availableTags,
            selectedTags = selectedTags,
            isTopBarCollapsed = isTopBarCollapsed,
            isOnlyFavorites = isOnlyFavorites,
            onOnlyFavoritesChange = onOnlyFavoritesChange,
            onSelectedTagsChange = onSelectedTagsChange,
            onToggleFavorite = onToggleFavorite,
            onExerciseClick = onExerciseClick,
            modifier = modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(padding),
        )
    }
}

@Composable
fun ExerciseList(
    exercises: ImmutableList<Exercise>,
    availableTags: ImmutableMap<String, ImmutableList<String>>,
    selectedTags: ImmutableList<Tag>,
    isTopBarCollapsed: Boolean,
    isOnlyFavorites: Boolean,
    onOnlyFavoritesChange: (Boolean) -> Unit,
    onSelectedTagsChange: (List<Tag>) -> Unit,
    onExerciseClick: (Exercise) -> Unit,
    onToggleFavorite: (Exercise) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 88.dp), // FAB Spacing
    ) {
        stickyHeader(key = "Filters", contentType = "Filters") {
            val containerColor by animateColorAsState(
                targetValue = TopAppBarDefaults
                    .centerAlignedTopAppBarColors()
                    .let { if (isTopBarCollapsed) it.scrolledContainerColor else it.containerColor },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "Color:Filters"
            )
            Row(
                modifier = Modifier
                    .background(containerColor)
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 0.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OnlyFavoritesChip(
                    value = isOnlyFavorites,
                    onValueChange = onOnlyFavoritesChange,
                )
                TagSelectors(
                    availableTags = availableTags,
                    value = selectedTags,
                    isCounterVisible = false,
                    onValueChange = onSelectedTagsChange,
                )
            }
        }

        items(exercises, key = { it.id }, contentType = { "Exercise" }) { exercise ->
            ExerciseListItem(
                exercise = exercise,
                trailingContent = {
                    IconButton(onClick = { onToggleFavorite(exercise) }) {
                        FavoriteIcon(
                            isFavorite = exercise.isFavorite,
                            crossfadeLabel = "Crossfade:Icon:IsFavorite:${exercise.id}",
                        )
                    }
                },
                onClick = { onExerciseClick(exercise) },
            )
        }
    }
}
