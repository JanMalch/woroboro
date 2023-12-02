package io.github.janmalch.woroboro.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import io.github.janmalch.woroboro.data.model.ExerciseEntity
import io.github.janmalch.woroboro.data.model.ExerciseEntityWithMediaAndTags
import io.github.janmalch.woroboro.data.model.ExerciseTagCrossRefEntity
import io.github.janmalch.woroboro.data.model.MediaEntity
import io.github.janmalch.woroboro.data.model.TagEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
abstract class ExerciseDao {

    @Transaction
    open suspend fun upsert(entity: ExerciseEntityWithMediaAndTags) {
        insertExercise(entity.exercise)
        deleteMediaOfExercise(entity.exercise.id)
        upsertMedia(entity.media)
        upsertTags(entity.tags)
        deleteTagRefsOfExercise(entity.exercise.id)
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

    @Upsert
    protected abstract suspend fun upsertMedia(media: List<MediaEntity>)

    @Query("DELETE FROM exercise_tag_cross_ref WHERE exercise_id = :exerciseId")
    protected abstract suspend fun deleteTagRefsOfExercise(exerciseId: UUID)

    @Query("DELETE FROM media WHERE exercise_id = :exerciseId")
    protected abstract suspend fun deleteMediaOfExercise(exerciseId: UUID)

    @Query("UPDATE exercise SET is_favorite = :isFavorite WHERE id = :id")
    abstract suspend fun updateFavoriteStatus(id: UUID, isFavorite: Boolean)

    @Transaction
    @Query("SELECT * FROM exercise WHERE id = :id")
    abstract fun resolve(id: UUID): Flow<ExerciseEntityWithMediaAndTags?>

    // TODO: How to make FTS work?
    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT * 
        FROM exercise
        WHERE
            CASE WHEN :onlyFavorites
            THEN is_favorite = 1
            ELSE 1
            END
        AND
            CASE WHEN :useTextQuery
            THEN (exercise.name LIKE '%' || :textQuery || '%' OR exercise.description LIKE '%' || :textQuery || '%')
            ELSE 1
            END
        ORDER BY exercise.name COLLATE NOCASE ASC
        """
    )
    abstract fun findAll(
        onlyFavorites: Boolean,
        textQuery: String,
        useTextQuery: Boolean = textQuery.isNotBlank()
    ): Flow<List<ExerciseEntityWithMediaAndTags>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT *
        FROM exercise
        JOIN exercise_tag_cross_ref as ref
        ON ref.exercise_id = exercise.id
        WHERE ref.tag_label IN (:selectedTags)
        AND
            CASE WHEN :onlyFavorites
            THEN is_favorite = 1
            ELSE 1
            END
        AND
            CASE WHEN :useTextQuery
            THEN (exercise.name LIKE '%' || :textQuery || '%' OR exercise.description LIKE '%' || :textQuery || '%')
            ELSE 1
            END
        ORDER BY exercise.name COLLATE NOCASE ASC
    """
    )
    abstract fun findAll(
        selectedTags: List<String>,
        onlyFavorites: Boolean,
        textQuery: String,
        useTextQuery: Boolean = textQuery.isNotBlank(),
    ): Flow<List<ExerciseEntityWithMediaAndTags>>

    @Query("DELETE FROM exercise WHERE id = :id")
    protected abstract suspend fun deleteExercise(id: UUID)

    @Transaction
    open suspend fun delete(id: UUID) {
        deleteExercise(id)
        deleteMediaOfExercise(id)
        deleteTagRefsOfExercise(id)
    }

    @Query(
        """
        SELECT media.id
        FROM media
        WHERE exercise_id = :exerciseId
        AND media.id NOT IN (:mediaIds)
    """
    )
    abstract suspend fun mediaForExerciseOtherThan(
        exerciseId: UUID,
        mediaIds: List<UUID>
    ): List<UUID>
}
