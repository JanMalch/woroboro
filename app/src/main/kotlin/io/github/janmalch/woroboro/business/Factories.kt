package io.github.janmalch.woroboro.business

import io.github.janmalch.woroboro.data.model.ExerciseEntity
import io.github.janmalch.woroboro.data.model.ExerciseEntityWithMediaAndTags
import io.github.janmalch.woroboro.data.model.MediaEntity
import io.github.janmalch.woroboro.data.model.TagEntity
import io.github.janmalch.woroboro.models.Exercise
import io.github.janmalch.woroboro.models.Media
import io.github.janmalch.woroboro.models.Tag
import java.util.UUID

fun Exercise.asEntity(): ExerciseEntityWithMediaAndTags {
    require(name.isNotBlank()) { "Name of exercise must not be blank." }
    require(description.isNotBlank()) { "Description of exercise must not be blank." }

    return ExerciseEntityWithMediaAndTags(
        exercise = ExerciseEntity(
            id = id,
            name = name.trim(),
            description = description.trim(),
            sets = sets,
            reps = reps,
            hold = hold,
            pause = pause,
            isFavorite = isFavorite,
        ),
        tags = tags.map(Tag::asEntity),
        media = media.map { it.asEntity(exerciseId = id) },
    )
}

fun Tag.asEntity() = TagEntity(
    label = label.trim(),
    type = type.trim(),
)

fun Media.asEntity(
    exerciseId: UUID,
) = MediaEntity(
    id = id,
    exerciseId = exerciseId,
    thumbnail = thumbnail,
    source = source,
    isVideo = this is Media.Video,
)
