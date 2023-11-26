package io.github.janmalch.woroboro.models

import kotlin.time.Duration

data class ExerciseExecution(
    /**
     * The amount of sets for this execution of the exercise.
     */
    val sets: Int,
    /**
     * The amount of repetitions for this execution of the exercise.
     * Either [reps] or [hold] should be defined.
     */
    val reps: Int?,
    /**
     * The duration to hold the exercise for this execution.
     * Either [reps] or [hold] should be defined.
     */
    val hold: Duration?,
    /**
     * The pause duration between each set for this execution of the exercise.
     */
    val pause: Duration?,
)

// TODO: use not just in editor, but also for steps?
data class CustomExerciseExecution(
    /**
     * The amount of sets for this execution of the exercise.
     */
    val sets: Int?,
    /**
     * The amount of repetitions for this execution of the exercise.
     * Either [reps] or [hold] should be defined.
     */
    val reps: Int?,
    /**
     * The duration to hold the exercise for this execution.
     * Either [reps] or [hold] should be defined.
     */
    val hold: Duration?,
    /**
     * The pause duration between each set for this execution of the exercise.
     */
    val pause: Duration?,
)

fun CustomExerciseExecution.asExerciseExecutionOrNull(): ExerciseExecution? {
    return ExerciseExecution(
        sets = sets ?: return null, reps = reps, hold = hold, pause = pause
    )
}

fun ExerciseExecution.asCustomExecution(): CustomExerciseExecution {
    return CustomExerciseExecution(
        sets = sets, reps = reps, hold = hold, pause = pause
    )
}


infix fun ExerciseExecution?.basedOn(defaults: ExerciseExecution): ExerciseExecution {
    if (this == null) return defaults
    return if (defaults.reps != null) {
        copy(reps = reps ?: defaults.reps, pause = pause ?: defaults.pause)
    } else {
        copy(hold = hold ?: defaults.hold, pause = pause ?: defaults.pause)
    }
}
