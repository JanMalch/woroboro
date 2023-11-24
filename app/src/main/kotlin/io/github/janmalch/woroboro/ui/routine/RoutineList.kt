package io.github.janmalch.woroboro.ui.routine

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.janmalch.woroboro.models.Media
import io.github.janmalch.woroboro.models.Routine
import io.github.janmalch.woroboro.models.Tag
import io.github.janmalch.woroboro.ui.components.common.FavoriteIcon
import io.github.janmalch.woroboro.ui.components.common.OnlyFavoritesChip
import io.github.janmalch.woroboro.ui.components.tags.TagSelectors
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.coroutines.delay

@Composable
fun RoutineListScreen(
    routines: ImmutableList<Routine>,
    availableTags: ImmutableMap<String, ImmutableList<String>>,
    selectedTags: ImmutableList<Tag>,
    isOnlyFavorites: Boolean,
    onOnlyFavoritesChange: (Boolean) -> Unit,
    onSelectedTagsChange: (List<Tag>) -> Unit,
    onCreateRoutineClick: () -> Unit,
    onToggleFavorite: (Routine) -> Unit,
    onRoutineClick: (Routine) -> Unit,
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
                    Text("Routinen")
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
                    Text(text = "Neue Routine")
                },
                onClick = onCreateRoutineClick
            )
        }
    ) { padding ->
        RoutineList(
            routines = routines,
            availableTags = availableTags,
            selectedTags = selectedTags,
            isTopBarCollapsed = isTopBarCollapsed,
            isOnlyFavorites = isOnlyFavorites,
            onOnlyFavoritesChange = onOnlyFavoritesChange,
            onSelectedTagsChange = onSelectedTagsChange,
            onToggleFavorite = onToggleFavorite,
            onRoutineClick = onRoutineClick,
            modifier = modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(padding),
        )
    }
}

@Composable
fun RoutineList(
    routines: ImmutableList<Routine>,
    availableTags: ImmutableMap<String, ImmutableList<String>>,
    selectedTags: ImmutableList<Tag>,
    isTopBarCollapsed: Boolean,
    isOnlyFavorites: Boolean,
    onOnlyFavoritesChange: (Boolean) -> Unit,
    onSelectedTagsChange: (List<Tag>) -> Unit,
    onRoutineClick: (Routine) -> Unit,
    onToggleFavorite: (Routine) -> Unit,
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

        items(routines, key = { it.id }, contentType = { "Routine" }) { routine ->
            RoutineListItem(
                routine = routine,
                onToggleFavorite = { onToggleFavorite(routine) },
                onClick = { onRoutineClick(routine) }
            )
        }
    }
}

@Composable
fun RoutineListItem(
    routine: Routine,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        leadingContent = {
            RoutinePreview(
                media = routine.media,
            )
        },
        headlineContent = {
            Text(text = routine.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Text(buildString {
                append("${routine.exercises.size} Übungen")
                if (routine.lastRun != null) {
                    append(" · ${routine.lastRun.inWholeMinutes} Minuten")
                }
            })
        },
        overlineContent = {
            if (routine.tags.isNotEmpty()) {
                Text(
                    text = routine.tags.joinToString { it.label },
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        trailingContent = {

            IconButton(onClick = onToggleFavorite) {
                FavoriteIcon(isFavorite = routine.isFavorite)
            }
        },
        modifier = modifier.clickable(onClick = onClick),
    )
    HorizontalDivider()
}


@Composable
fun RoutinePreview(
    media: ImmutableList<Media>,
) {
    val currentMediaList by rememberUpdatedState(media)
    var currentImage by remember {
        mutableStateOf(media.randomOrNull())
    }

    Crossfade(
        targetState = currentImage?.thumbnail,
        label = "RoutinePreviewImageCrossfade",
        animationSpec = tween(500)
    ) {
        AsyncImage(
            model = it,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
        )
    }


    LaunchedEffect(Unit) {
        while (currentMediaList.size > 1) {
            delay(5000L)
            var next: Media
            do {
                next = currentMediaList.random()
            } while (next == currentImage)
            currentImage = next
        }
    }

}