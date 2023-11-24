package io.github.janmalch.woroboro.models

import android.net.Uri
import kotlinx.collections.immutable.PersistentList

data class EditedMedia(
    val existing: PersistentList<Media>,
    val added: Set<Uri>,
)

fun EditedMedia.isEmpty(): Boolean = existing.isEmpty() && added.isEmpty()

data class EditedExercise(
    val exercise: Exercise,
    val addedMedia: Set<Uri> = emptySet(),
)
