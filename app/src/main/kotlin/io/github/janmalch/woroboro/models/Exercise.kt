package io.github.janmalch.woroboro.models

import android.os.Parcelable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.util.UUID

data class Exercise(
    val id: UUID,
    val name: String,
    val description: String,
    val tags: ImmutableList<Tag>,
    val media: ImmutableList<Media>,
    val execution: ExerciseExecution,
    val isFavorite: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)

sealed interface Media : Parcelable {
    val id: UUID
    val source: String
    val thumbnail: String

    @Parcelize
    data class Image(
        override val id: UUID,
        override val source: String,
        override val thumbnail: String
    ) : Media

    @Parcelize
    data class Video(
        override val id: UUID,
        override val source: String,
        override val thumbnail: String
    ) : Media
}
