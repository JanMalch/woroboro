package io.github.janmalch.woroboro.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import io.github.janmalch.woroboro.data.model.ExerciseEntity
import io.github.janmalch.woroboro.data.model.ExerciseTagCrossRefEntity
import io.github.janmalch.woroboro.data.model.ExerciseWithTagsEntity
import io.github.janmalch.woroboro.data.model.TagEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
abstract class ExerciseDao {

    @Transaction
    open suspend fun upsert(entity: ExerciseWithTagsEntity) {
        insertExercise(entity.exercise)
        upsertTags(entity.tags)
        upsertCrossRefs(entity.tags.map {
            ExerciseTagCrossRefEntity(
                exerciseId = entity.exercise.id,
                tagLabel = it.label,
            )
        })
    }

    @Upsert
    protected abstract suspend fun upsertCrossRefs(crossRefEntities: List<ExerciseTagCrossRefEntity>)

    @Upsert
    protected abstract suspend fun insertExercise(entity: ExerciseEntity)

    @Upsert
    protected abstract suspend fun upsertTags(tags: List<TagEntity>)

    @Query("UPDATE exercise SET is_favorite = :isFavorite WHERE id = :id")
    abstract suspend fun updateFavoriteStatus(id: UUID, isFavorite: Boolean)

    @Query("SELECT * FROM exercise WHERE id = :id")
    abstract fun find(id: UUID): Flow<ExerciseWithTagsEntity?>

    @Query("DELETE FROM exercise WHERE id = :id")
    abstract suspend fun delete(id: UUID)

    @Transaction
    @Query(
        """
        SELECT *
        FROM exercise
        JOIN exercise_fts as fts
        ON exercise.id = fts.id
        WHERE fts.name MATCH :query
        OR fts.description MATCH :query
        ORDER BY fts.name ASC
    """)
    abstract suspend fun searchInNameOrDescription(query: String): List<ExerciseWithTagsEntity>
}
