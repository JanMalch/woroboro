package io.github.janmalch.woroboro.models

import kotlinx.collections.immutable.ImmutableList
import java.util.UUID
import kotlin.time.Duration

data class Exercise(
    val id: UUID,
    val name: String,
    val description: String,
    val tags: ImmutableList<Tag>,
    val sets: Int,
    val reps: Int?,
    val hold: Duration?,
    val pause: Duration?,
    val isFavorite: Boolean,
)
