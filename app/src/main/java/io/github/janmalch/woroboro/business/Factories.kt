package io.github.janmalch.woroboro.business

import io.github.janmalch.woroboro.data.model.ExerciseEntity
import io.github.janmalch.woroboro.data.model.ExerciseWithTagsEntity
import io.github.janmalch.woroboro.data.model.TagEntity
import io.github.janmalch.woroboro.models.Exercise
import io.github.janmalch.woroboro.models.Tag

fun Exercise.asEntity(): ExerciseWithTagsEntity {
    require(name.isNotBlank()) { "Name of exercise must not be blank." }
    require(description.isNotBlank()) { "Description of exercise must not be blank." }

    return ExerciseWithTagsEntity(
        exercise = ExerciseEntity(
            id = id,
            name = name,
            description = description,
            sets = sets,
            reps = reps,
            hold = hold,
            pause = pause,
            isFavorite = isFavorite,
        ), tags = tags.map(Tag::asEntity)
    )
}

fun Tag.asEntity() = TagEntity(
    label = label,
    type = type,
)
