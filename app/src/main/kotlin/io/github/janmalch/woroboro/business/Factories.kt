package io.github.janmalch.woroboro.business

import io.github.janmalch.woroboro.data.model.ExerciseEntity
import io.github.janmalch.woroboro.data.model.ExerciseEntityWithMediaAndTags
import io.github.janmalch.woroboro.data.model.MediaEntity
import io.github.janmalch.woroboro.data.model.ReminderEntity
import io.github.janmalch.woroboro.data.model.RoutineEntity
import io.github.janmalch.woroboro.data.model.RoutineStepEntity
import io.github.janmalch.woroboro.data.model.TagEntity
import io.github.janmalch.woroboro.models.Exercise
import io.github.janmalch.woroboro.models.FullRoutine
import io.github.janmalch.woroboro.models.Media
import io.github.janmalch.woroboro.models.Reminder
import io.github.janmalch.woroboro.models.Routine
import io.github.janmalch.woroboro.models.RoutineQuery
import io.github.janmalch.woroboro.models.RoutineStep
import io.github.janmalch.woroboro.models.Tag
import io.github.janmalch.woroboro.models.asRoutineFilter
import java.util.UUID

fun Exercise.asEntity(): ExerciseEntityWithMediaAndTags {
    require(name.isNotBlank()) { "Name of exercise must not be blank." }

    return ExerciseEntityWithMediaAndTags(
        exercise = ExerciseEntity(
            id = id,
            name = name.trim(),
            description = description.trim(),
            execution = execution,
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

fun Routine.asEntity() = RoutineEntity(
    id = id,
    name = name,
    isFavorite = isFavorite,
    lastRunDuration = lastRunDuration,
    lastRunEnded = lastRunEnded,
)

fun RoutineStep.asEntity(routineId: UUID, stepId: UUID = id): RoutineStepEntity = when (this) {
    is RoutineStep.ExerciseStep -> RoutineStepEntity(
        id = stepId,
        routineId = routineId,
        sortIndex = sortIndex,
        exerciseId = exercise.id,
        execution = customExecution,
        pauseStep = null,
    )

    is RoutineStep.PauseStep -> RoutineStepEntity(
        id = stepId,
        routineId = routineId,
        sortIndex = sortIndex,
        exerciseId = null,
        execution = null,
        pauseStep = duration,
    )
}

fun FullRoutine.asEntities(overwriteStepIds: Boolean = false): Pair<RoutineEntity, List<RoutineStepEntity>> =
    RoutineEntity(
        id = id,
        name = name,
        isFavorite = isFavorite,
        lastRunDuration = lastRunDuration,
        lastRunEnded = lastRunEnded,
    ) to steps.map { it.asEntity(id, if (overwriteStepIds) UUID.randomUUID() else it.id) }


fun Reminder.asEntities(): Pair<ReminderEntity, List<String>> {
    val filter = query.asRoutineFilter()
    return ReminderEntity(
        id = id,
        name = name,
        remindAt = remindAt,
        weekdays = weekdays,
        repeatEvery = repeat?.every,
        repeatUntil = repeat?.until,
        filterOnlyFavorites = filter.onlyFavorites,
        filterDuration = filter.durationFilter,
        filterRoutineId = (query as? RoutineQuery.Single)?.routineId,
        routinesOrder = filter.routinesOrder,
    ) to filter.selectedTags.map { it.label }
}
