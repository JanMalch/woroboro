package io.github.janmalch.woroboro.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import io.github.janmalch.woroboro.data.model.ExerciseEntityWithMediaAndTags
import io.github.janmalch.woroboro.data.model.RoutineEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface RoutineDao {
    @Transaction
    @Query(
        """
        SELECT routine.*, exercise.* 
        FROM routine
        JOIN routine_exercise_cross_ref as xref
        ON xref.routine_id = routine.id
        JOIN exercise
        ON xref.exercise_id = exercise.id
        WHERE
            CASE WHEN :onlyFavorites
            THEN routine.is_favorite = 1
            ELSE 1
            END
        ORDER BY routine.name COLLATE NOCASE ASC
    """
    )
    fun findAll(onlyFavorites: Boolean): Flow<Map<RoutineEntity, List<ExerciseEntityWithMediaAndTags>>>

    @Transaction
    @Query(
        """
            SELECT routine.*, exercise.*
            FROM routine
            JOIN routine_exercise_cross_ref as xref
            ON xref.routine_id = routine.id
            JOIN exercise
            ON xref.exercise_id = exercise.id
            WHERE routine.id = :id
        """
    )
    fun findOne(id: UUID): Flow<Map<RoutineEntity, List<ExerciseEntityWithMediaAndTags>>>
}
