package io.github.janmalch.woroboro.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.util.UUID
import kotlin.time.Duration

data class Routine(
    val id: UUID,
    val name: String,
    val exercises: ImmutableList<Exercise>,
    val isFavorite: Boolean,
    val lastRun: Duration?
) {
    val tags: ImmutableList<Tag> = exercises
        .flatMap { it.tags }
        .distinct()
        .toImmutableList()

    val media: ImmutableList<Media> = exercises
        .flatMap { it.media }
        .distinct()
        .toImmutableList()
}
