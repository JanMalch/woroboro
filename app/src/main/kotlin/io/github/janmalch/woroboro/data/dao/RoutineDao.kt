package io.github.janmalch.woroboro.data.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Update
import io.github.janmalch.woroboro.data.model.ExerciseEntityWithMediaAndTags
import io.github.janmalch.woroboro.data.model.MediaEntity
import io.github.janmalch.woroboro.data.model.RoutineEntity
import io.github.janmalch.woroboro.data.model.RoutineStepEntity
import io.github.janmalch.woroboro.data.model.TagEntity
import io.github.janmalch.woroboro.data.model.asModel
import io.github.janmalch.woroboro.models.FullRoutine
import io.github.janmalch.woroboro.models.Routine
import io.github.janmalch.woroboro.models.RoutineStep
import io.github.janmalch.woroboro.models.basedOn
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID

@Dao
abstract class RoutineDao {


    @Query(
        """
        SELECT 
            routine.*,
            COUNT(step.exercise_id) as exercise_count
        FROM routine
        JOIN routine_step as step
        ON step.routine_id = routine.id
        WHERE
            CASE WHEN :onlyFavorites
            THEN routine.is_favorite = 1
            ELSE 1
            END
        GROUP BY routine.id, routine.name, routine.is_favorite, routine.last_run_duration, routine.last_run_ended
        ORDER BY routine.name COLLATE NOCASE ASC
    """
    )
    protected abstract fun findAllRoutines(onlyFavorites: Boolean): Flow<List<RoutineQueryResult>>

    @Query(
        """
        SELECT routine_id, tag.*
        FROM tag
        JOIN exercise_tag_cross_ref as xref
        ON xref.tag_label = tag.label
        JOIN routine_step as step
        ON step.exercise_id = xref.exercise_id
        WHERE step.routine_id IN (:routineIds)
    """
    )
    protected abstract fun findAllTags(routineIds: List<UUID>): Flow<List<RoutineTagsQueryResult>>

    @Query(
        """
        SELECT routine_id, media.*
        FROM media
        JOIN routine_step as step
        ON step.exercise_id = media.exercise_id
        WHERE step.routine_id IN (:routineIds)
    """
    )
    protected abstract fun findAllMedia(routineIds: List<UUID>): Flow<List<RoutineMediaQueryResult>>

    open fun findAll(onlyFavorites: Boolean): Flow<List<Routine>> {
        // TODO: see if one big query is better
        return findAllRoutines(onlyFavorites).flatMapLatest { routines ->
            val routineIds = routines.map { it.routine.id }
            findAllTags(routineIds).combine(findAllMedia(routineIds)) { tags, media ->
                routines.map { routine ->
                    val tagsOfRoutine = tags.filter { tag -> tag.routineId == routine.routine.id }
                    val mediaOfRoutine =
                        media.filter { media -> media.routineId == routine.routine.id }
                    Routine(
                        id = routine.routine.id,
                        name = routine.routine.name,
                        isFavorite = routine.routine.isFavorite,
                        lastRunDuration = routine.routine.lastRunDuration,
                        lastRunEnded = routine.routine.lastRunEnded,
                        exerciseCount = routine.exerciseCount,
                        media = mediaOfRoutine.map { it.mediaEntity.asModel() }.toImmutableList(),
                        tags = tagsOfRoutine.map { it.tagEntity.asModel() }.toImmutableList(),
                    )
                }
            }
        }
    }

    @Query(
        """
        SELECT *
        FROM routine
        JOIN routine_step as step
        ON step.routine_id = routine.id
        WHERE routine.id = :routineId
    """
    )
    protected abstract fun resolveWithSteps(routineId: UUID): Flow<Map<RoutineEntity, List<RoutineStepEntity>>>

    @Query(
        """
        SELECT exercise.*
        FROM exercise 
        JOIN routine_step as step
        ON step.exercise_id = exercise.id
        WHERE step.routine_id = :routineId
        ORDER BY step.sort_index ASC
    """
    )
    protected abstract fun findExercises(routineId: UUID): Flow<List<ExerciseEntityWithMediaAndTags>>

    open fun findOneFull(id: UUID): Flow<FullRoutine?> {
        return resolveWithSteps(id).flatMapLatest { routines ->
            val (routine, steps) = routines.entries.firstOrNull() ?: return@flatMapLatest flowOf(
                null
            )
            findExercises(routine.id).map { exercises ->
                FullRoutine(
                    id = routine.id,
                    name = routine.name,
                    isFavorite = routine.isFavorite,
                    lastRunDuration = routine.lastRunDuration,
                    lastRunEnded = routine.lastRunEnded,
                    steps = steps
                        .map { step -> step.asModel(exercises) }
                        .sortedBy(RoutineStep::sortIndex)
                        .toImmutableList(),
                )
            }
        }
    }

    @Update
    abstract suspend fun update(routineEntity: RoutineEntity)
}

data class RoutineQueryResult(
    @Embedded
    val routine: RoutineEntity,
    @ColumnInfo("exercise_count")
    val exerciseCount: Int,
)

data class RoutineTagsQueryResult(
    @ColumnInfo("routine_id")
    val routineId: UUID,
    @Embedded
    val tagEntity: TagEntity,
)

data class RoutineMediaQueryResult(
    @ColumnInfo("routine_id")
    val routineId: UUID,
    @Embedded
    val mediaEntity: MediaEntity,
)

fun RoutineStepEntity.asModel(exerciseLookup: Collection<ExerciseEntityWithMediaAndTags>): RoutineStep {
    if (pauseStep != null) {
        return RoutineStep.PauseStep(sortIndex = sortIndex, duration = pauseStep)
    }
    val exercise = exerciseLookup.firstOrNull { it.exercise.id == exerciseId }
    checkNotNull(exercise) { "Failed to find exercise $exerciseId for routine $routineId at step index ${sortIndex}." }
    val execution = execution basedOn exercise.exercise.execution
    return RoutineStep.ExerciseStep(
        sortIndex = sortIndex,
        exercise = exercise.asModel(),
        execution = execution,
    )
}
