package io.github.janmalch.woroboro.ui.components.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.janmalch.woroboro.models.DurationFilter
import io.github.janmalch.woroboro.models.Tag
import io.github.janmalch.woroboro.ui.components.common.OnlyFavoritesChip
import io.github.janmalch.woroboro.ui.components.tags.TagSelectors
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

@Composable
fun RoutineFilterRow(
    availableTags: ImmutableMap<String, ImmutableList<String>>,
    selectedTags: ImmutableList<Tag>,
    isOnlyFavorites: Boolean,
    durationFilter: DurationFilter,
    onDurationFilterChange: (DurationFilter) -> Unit,
    onOnlyFavoritesChange: (Boolean) -> Unit,
    onSelectedTagsChange: (List<Tag>) -> Unit,
    containerColor: Color = Color.Unspecified,
    contentPadding: PaddingValues = PaddingValues(vertical = 0.dp, horizontal = 12.dp),
    enabled: Boolean = true,
) {
    Row(
        modifier = androidx.compose.ui.Modifier
            .background(containerColor)
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OnlyFavoritesChip(
            value = isOnlyFavorites,
            onValueChange = onOnlyFavoritesChange,
            enabled = enabled,
        )
        DurationFilterChip(
            value = durationFilter,
            onValueChange = onDurationFilterChange,
            enabled = enabled,
        )
        TagSelectors(
            availableTags = availableTags,
            value = selectedTags,
            isCounterVisible = false,
            onValueChange = onSelectedTagsChange,
            enabled = enabled,
        )
    }
}