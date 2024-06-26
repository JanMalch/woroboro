package io.github.janmalch.woroboro.data.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
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
import io.github.janmalch.woroboro.models.RoutinesOrder
import java.util.UUID
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

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
        ORDER BY
        CASE :orderBy WHEN 'NameDesc' THEN routine.name END COLLATE NOCASE DESC,
        CASE :orderBy WHEN 'LastRunRecently' THEN routine.last_run_ended END DESC,
        CASE :orderBy WHEN 'LastRunLongAgo' THEN routine.last_run_ended END ASC,
        routine.name COLLATE NOCASE ASC
    """
    )
    protected abstract fun findAllRoutines(
        onlyFavorites: Boolean,
        orderBy: RoutinesOrder
    ): Flow<List<RoutineQueryResult>>

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

    open fun findAll(onlyFavorites: Boolean, orderBy: RoutinesOrder): Flow<List<Routine>> {
        // TODO: see if one big query is better
        return findAllRoutines(onlyFavorites, orderBy).flatMapLatest { routines ->
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
                        createdAt = routine.routine.createdAt,
                        updatedAt = routine.routine.updatedAt,
                        media =
                            mediaOfRoutine
                                .map { it.mediaEntity.asModel() }
                                .distinct()
                                .toImmutableList(),
                        tags =
                            tagsOfRoutine
                                .map { it.tagEntity.asModel() }
                                .distinct()
                                .toImmutableList(),
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
    protected abstract fun resolveWithSteps(
        routineId: UUID
    ): Flow<Map<RoutineEntity, List<RoutineStepEntity>>>

    @Transaction
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
    protected abstract fun findExercises(
        routineId: UUID
    ): Flow<List<ExerciseEntityWithMediaAndTags>>

    open fun findOneFull(id: UUID): Flow<FullRoutine?> {
        return resolveWithSteps(id).flatMapLatest { routines ->
            val (routine, steps) =
                routines.entries.firstOrNull() ?: return@flatMapLatest flowOf(null)
            findExercises(routine.id).map { exercises ->
                FullRoutine(
                    id = routine.id,
                    name = routine.name,
                    isFavorite = routine.isFavorite,
                    lastRunDuration = routine.lastRunDuration,
                    lastRunEnded = routine.lastRunEnded,
                    createdAt = routine.createdAt,
                    updatedAt = routine.updatedAt,
                    steps =
                        steps
                            .map { step -> step.asModel(exercises) }
                            .sortedBy(RoutineStep::sortIndex)
                            .toImmutableList(),
                )
            }
        }
    }

    @Update abstract suspend fun update(routineEntity: RoutineEntity)

    @Insert protected abstract suspend fun insertRoutine(routineEntity: RoutineEntity)

    @Insert abstract suspend fun insertSteps(steps: List<RoutineStepEntity>)

    @Transaction
    open suspend fun insert(routineEntity: RoutineEntity, steps: List<RoutineStepEntity>) {
        insertRoutine(routineEntity)
        insertSteps(steps)
    }

    @Query("DELETE FROM routine_step WHERE routine_id = :routineId")
    protected abstract suspend fun deleteStepsOf(routineId: UUID)

    @Transaction
    open suspend fun update(routineEntity: RoutineEntity, steps: List<RoutineStepEntity>) {
        deleteStepsOf(routineEntity.id)
        update(routineEntity)
        insertSteps(steps)
    }

    @Query("DELETE FROM routine WHERE id = :routineId") abstract suspend fun delete(routineId: UUID)

    @Query("UPDATE routine SET last_run_duration = NULL, last_run_ended = NULL")
    abstract suspend fun clearLastRuns()
}

data class RoutineQueryResult(
    @Embedded val routine: RoutineEntity,
    @ColumnInfo("exercise_count") val exerciseCount: Int,
)

data class RoutineTagsQueryResult(
    @ColumnInfo("routine_id") val routineId: UUID,
    @Embedded val tagEntity: TagEntity,
)

data class RoutineMediaQueryResult(
    @ColumnInfo("routine_id") val routineId: UUID,
    @Embedded val mediaEntity: MediaEntity,
)

fun RoutineStepEntity.asModel(
    exerciseLookup: Collection<ExerciseEntityWithMediaAndTags>
): RoutineStep {
    if (pauseStep != null) {
        return RoutineStep.PauseStep(id = id, sortIndex = sortIndex, duration = pauseStep)
    }
    val exercise = exerciseLookup.firstOrNull { it.exercise.id == exerciseId }
    checkNotNull(exercise) {
        "Failed to find exercise $exerciseId for routine $routineId at step index ${sortIndex}."
    }
    return RoutineStep.ExerciseStep(
        id = id,
        sortIndex = sortIndex,
        exercise = exercise.asModel(),
        customExecution = execution,
    )
}
