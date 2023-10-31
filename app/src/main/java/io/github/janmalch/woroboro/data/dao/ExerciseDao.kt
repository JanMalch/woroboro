package io.github.janmalch.woroboro.data.dao

import androidx.room.Dao
import androidx.room.Query
import io.github.janmalch.woroboro.data.model.ExerciseEntity

@Dao
interface ExerciseDao {
    @Query("""
        SELECT exercise.*
        FROM exercise
        JOIN exercise_fts as fts
        ON exercise.id = fts.id
        WHERE fts.name MATCH :query
        OR fts.description MATCH :query
        ORDER BY fts.name ASC
    """)
    suspend fun searchInNameOrDescription(query: String): List<ExerciseEntity>
}
