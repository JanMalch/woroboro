package io.github.janmalch.woroboro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.janmalch.woroboro.models.Exercise
import io.github.janmalch.woroboro.models.ExerciseExecution
import io.github.janmalch.woroboro.models.Media
import io.github.janmalch.woroboro.models.Tag
import io.github.janmalch.woroboro.ui.components.ExerciseListItemDefaults.ExecutionAsSupportingContent
import io.github.janmalch.woroboro.ui.components.ExerciseListItemDefaults.TagsAsOverlineContent
import kotlinx.collections.immutable.ImmutableList

object ExerciseListItemDefaults {
    @Stable
    val ImageSizeTwoLines: Dp get() = 56.dp

    @Stable
    val ImageSizeThreeLines: Dp get() = 64.dp

    /** The default corner size for images. */
    @Stable
    val ImageCornerSize: Dp get() = 8.dp

    @Composable
    fun ExecutionAsSupportingContent(execution: ExerciseExecution) {
        Text(
            text = exerciseExecution(execution = execution),
            style = MaterialTheme.typography.bodyMedium
        )
    }

    @Composable
    fun TagsAsOverlineContent(tags: ImmutableList<Tag>) {
        if (tags.isNotEmpty()) {
            Text(
                text = tags.joinToString(transform = Tag::label),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    @Composable
    fun MediaAsLeadingContent(
        media: ImmutableList<Media>,
        imageSize: Dp,
    ) {
        AsyncImage(
            model = media.firstOrNull()?.thumbnail,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(imageSize)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(ImageCornerSize)
                )
                .clip(RoundedCornerShape(ImageCornerSize))
        )
    }
}

@Composable
fun ExerciseListItem(
    exercise: Exercise,
    modifier: Modifier = Modifier,
    execution: ExerciseExecution = exercise.execution,
    overlineContent: @Composable (() -> Unit)? = {
        TagsAsOverlineContent(exercise.tags)
    },
    supportingContent: @Composable (() -> Unit)? = {
        ExecutionAsSupportingContent(execution)
    },
    imageSize: Dp = if (overlineContent != null && supportingContent != null) ExerciseListItemDefaults.ImageSizeThreeLines
    else ExerciseListItemDefaults.ImageSizeTwoLines,
    leadingContent: @Composable (() -> Unit)? = {
        ExerciseListItemDefaults.MediaAsLeadingContent(
            media = exercise.media,
            imageSize = imageSize,
        )
    },
    trailingContent: @Composable (() -> Unit)? = null,

    onClick: (() -> Unit)? = null,
    tonalElevation: Dp = ListItemDefaults.Elevation,
    shadowElevation: Dp = ListItemDefaults.Elevation,
) {
    ListItem(
        leadingContent = leadingContent,
        headlineContent = {
            Text(
                text = exercise.name,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        overlineContent = overlineContent,
        supportingContent = supportingContent,
        trailingContent = trailingContent,
        modifier = if (onClick == null) modifier else modifier.clickable(onClick = onClick),
        shadowElevation = shadowElevation,
        tonalElevation = tonalElevation,
    )
}


@Composable
@ReadOnlyComposable
fun exerciseExecution(
    execution: ExerciseExecution,
): String {
    val base = when {
        execution.reps != null -> "${execution.sets} × ${execution.reps}"
        execution.hold != null -> "${execution.sets} × ${execution.hold.inWholeSeconds}s"
        else -> "${execution.sets}"
    }
    return if (execution.pause != null) "$base · ${execution.pause.inWholeSeconds}s Pause"
    else base
}


