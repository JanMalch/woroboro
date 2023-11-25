package io.github.janmalch.woroboro.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.time.LocalDateTime
import java.util.UUID
import kotlin.time.Duration

data class Routine(
    val id: UUID,
    val name: String,
    val media: ImmutableList<Media>,
    val tags: ImmutableList<Tag>,
    val exerciseCount: Int,
    val isFavorite: Boolean,
    val lastRunDuration: Duration?,
    val lastRunEnded: LocalDateTime?,
)

data class FullRoutine(
    val id: UUID,
    val name: String,
    val steps: ImmutableList<RoutineStep>,
    val isFavorite: Boolean,
    val lastRunDuration: Duration?,
    val lastRunEnded: LocalDateTime?,
) {
    val exercises: ImmutableList<Exercise> = steps
        .mapNotNull { (it as? RoutineStep.ExerciseStep)?.exercise }
        .toImmutableList()
}

sealed interface RoutineStep {
    /**
     * The index of this step within a routine.
     * Guaranteed to be unique within a routine.
     */
    val sortIndex: Int

    data class ExerciseStep(
        override val sortIndex: Int,
        val exercise: Exercise,
        /**
         * The execution of this step, specific to this routine.
         * Might be the same as the default in [exercise].
         * Always use this in the context of a routine.
         */
        val execution: ExerciseExecution,
    ) : RoutineStep

    data class PauseStep(
        override val sortIndex: Int,
        val duration: Duration,
    ) : RoutineStep
}
