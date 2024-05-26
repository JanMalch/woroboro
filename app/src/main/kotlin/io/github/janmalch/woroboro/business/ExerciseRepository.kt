package io.github.janmalch.woroboro.business

import android.util.Log
import io.github.janmalch.woroboro.data.dao.ExerciseDao
import io.github.janmalch.woroboro.data.model.ExerciseEntityWithMediaAndTags
import io.github.janmalch.woroboro.data.model.MediaEntity
import io.github.janmalch.woroboro.data.model.asModel
import io.github.janmalch.woroboro.models.EditedExercise
import io.github.janmalch.woroboro.models.Exercise
import io.github.janmalch.woroboro.models.Media
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

interface ExerciseRepository {
    suspend fun insert(exercise: EditedExercise): UUID

    suspend fun update(exercise: EditedExercise): UUID

    fun resolve(id: UUID): Flow<Exercise?>

    /**
     * Finds all exercises matched by the given [tags]. Does not filter by tags, when [tags] is
     * empty.
     *
     * Also filters the list to only include favorites, if [onlyFavorites] is set to `true`.
     * Otherwise returns both favorites and non-favorites.
     */
    fun findAll(
        tags: List<String> = emptyList(),
        onlyFavorites: Boolean = false,
        textQuery: String = ""
    ): Flow<List<Exercise>>

    suspend fun delete(id: UUID)
}

class ExerciseRepositoryImpl
@Inject
constructor(
    private val mediaFileManager: MediaFileManager,
    private val exerciseDao: ExerciseDao,
) : ExerciseRepository {

    override suspend fun insert(exercise: EditedExercise): UUID {
        val exerciseId = UUID.randomUUID()
        val addedMedia = mediaFileManager.add(exercise.addedMedia).map { it.asEntity(exerciseId) }
        Log.d("ExerciseRepositoryImpl", "Optimized ${addedMedia.size} new media files: $addedMedia")
        try {
            val now = Instant.now()
            exerciseDao.upsert(
                exercise.exercise
                    .copy(
                        id = exerciseId,
                        createdAt = now,
                        updatedAt = now,
                    )
                    .asEntity()
                    .copy(
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
        val mediaToRemove =
            exerciseDao.mediaForExerciseOtherThan(
                exercise.exercise.id,
                exercise.exercise.media.map(Media::id)
            )
        val addedMedia =
            mediaFileManager.add(exercise.addedMedia).map { it.asEntity(exercise.exercise.id) }
        val exerciseEntity = exercise.exercise.copy(updatedAt = Instant.now()).asEntity()
        try {
            exerciseDao.upsert(exerciseEntity.copy(media = addedMedia + exerciseEntity.media))
            mediaFileManager.delete(mediaToRemove)
        } catch (e: Exception) {
            mediaFileManager.delete(addedMedia.map(MediaEntity::id))
            throw e
        }
        return exercise.exercise.id
    }

    override suspend fun delete(id: UUID) {
        val existing = resolve(id).firstOrNull() ?: return
        exerciseDao.delete(id)
        // TODO: orphan worker?
        mediaFileManager.delete(existing.media.map(Media::id))
    }

    override fun resolve(id: UUID): Flow<Exercise?> {
        return exerciseDao.resolve(id).map { it?.asModel() }
    }

    override fun findAll(
        tags: List<String>,
        onlyFavorites: Boolean,
        textQuery: String
    ): Flow<List<Exercise>> {
        val flow =
            if (tags.isEmpty())
                exerciseDao.findAll(onlyFavorites = onlyFavorites, textQuery = textQuery.trim())
            else
                exerciseDao.findAll(
                    tags,
                    onlyFavorites = onlyFavorites,
                    textQuery = textQuery.trim()
                )
        return flow.map { list ->
            list
                .map(ExerciseEntityWithMediaAndTags::asModel)
                .distinctBy(Exercise::id) // TODO: prevent duplicates via query?
        }
    }
}
