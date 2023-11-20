package io.github.janmalch.woroboro.business

import android.util.Log
import io.github.janmalch.woroboro.data.dao.ExerciseDao
import io.github.janmalch.woroboro.data.model.ExerciseEntityWithMediaAndTags
import io.github.janmalch.woroboro.data.model.MediaEntity
import io.github.janmalch.woroboro.data.model.asModel
import io.github.janmalch.woroboro.models.EditedExercise
import io.github.janmalch.woroboro.models.Exercise
import io.github.janmalch.woroboro.models.Media
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

interface ExerciseRepository {
    suspend fun insert(exercise: EditedExercise): UUID
    suspend fun update(exercise: EditedExercise): UUID
    fun resolve(id: UUID): Flow<Exercise?>

    /**
     * Finds all exercises matched by the given [tags].
     * Returns all exercises when [tags] is empty.
     *
     * Also filters the list to only include favorites, if [onlyFavorites] is set to `true`.
     * Otherwise returns both favorites and non-favorites.
     */
    fun findByTags(tags: List<String>, onlyFavorites: Boolean): Flow<List<Exercise>>
    suspend fun delete(id: UUID)
    suspend fun searchInNameOrDescription(query: String): List<Exercise>
}

class ExerciseRepositoryImpl @Inject constructor(
    private val mediaFileManager: MediaFileManager,
    private val exerciseDao: ExerciseDao,
) : ExerciseRepository {

    override suspend fun insert(exercise: EditedExercise): UUID {
        val exerciseId = UUID.randomUUID()
        val addedMedia = mediaFileManager.add(exercise.addedMedia).map { it.asEntity(exerciseId) }
        Log.d("ExerciseRepositoryImpl", "Optimized ${addedMedia.size} new media files: $addedMedia")
        try {
            exerciseDao.upsert(
                exercise.exercise.copy(
                    id = exerciseId,
                ).asEntity().copy(
                    media = addedMedia,
                )
            )
        } catch (e: Exception) {
            mediaFileManager.delete(addedMedia.map(MediaEntity::id))
            throw e
        }
        return exerciseId
    }

    override suspend fun update(exercise: EditedExercise): UUID {
        val mediaToRemove = exerciseDao.mediaForExerciseOtherThan(
            exercise.exercise.id,
            exercise.exercise.media.map(Media::id)
        )
        val addedMedia =
            mediaFileManager.add(exercise.addedMedia).map { it.asEntity(exercise.exercise.id) }
        val exerciseEntity = exercise.exercise.asEntity()
        try {
            exerciseDao.upsert(
                exerciseEntity.copy(
                    media = addedMedia + exerciseEntity.media
                )
            )
            mediaFileManager.delete(mediaToRemove)
        } catch (e: Exception) {
            mediaFileManager.delete(addedMedia.map(MediaEntity::id))
            throw e
        }
        return exercise.exercise.id
    }

    override suspend fun delete(id: UUID) {
        exerciseDao.delete(id)
    }

    override fun resolve(id: UUID): Flow<Exercise?> {
        return exerciseDao.resolve(id).map { it?.asModel() }
    }

    override fun findByTags(tags: List<String>, onlyFavorites: Boolean): Flow<List<Exercise>> {
        val flow = if (tags.isEmpty()) exerciseDao.resolveAll(onlyFavorites = onlyFavorites)
        else exerciseDao.findByTags(tags, onlyFavorites = onlyFavorites)
        return flow.map { list -> list.map(ExerciseEntityWithMediaAndTags::asModel) }
    }

    override suspend fun searchInNameOrDescription(query: String): List<Exercise> {
        if (query.isBlank()) return emptyList()
        return exerciseDao.searchInNameOrDescription(query.trim())
            .map(ExerciseEntityWithMediaAndTags::asModel)
    }

}
