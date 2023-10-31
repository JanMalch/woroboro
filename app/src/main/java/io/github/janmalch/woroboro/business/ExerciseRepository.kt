package io.github.janmalch.woroboro.business

import io.github.janmalch.woroboro.data.dao.ExerciseDao
import io.github.janmalch.woroboro.data.dao.TagDao
import io.github.janmalch.woroboro.data.model.ExerciseEntity
import io.github.janmalch.woroboro.data.model.ExerciseWithTagsEntity
import io.github.janmalch.woroboro.data.model.asModel
import io.github.janmalch.woroboro.models.Exercise
import java.util.UUID
import javax.inject.Inject

interface ExerciseRepository {
    suspend fun insert(exercise: Exercise)
    suspend fun update(exercise: Exercise)
    suspend fun delete(id: UUID)
    suspend fun searchInNameOrDescription(query: String): List<Exercise>
}

class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao,
) : ExerciseRepository {

    override suspend fun insert(exercise: Exercise) {
        exerciseDao.upsert(exercise.copy(id = UUID.randomUUID()).asEntity())
    }

    override suspend fun update(exercise: Exercise) {
        exerciseDao.upsert(exercise.asEntity())
    }

    override suspend fun delete(id: UUID) {
        exerciseDao.delete(id)
    }

    override suspend fun searchInNameOrDescription(query: String): List<Exercise> {
        if (query.isBlank()) return emptyList()
        return exerciseDao.searchInNameOrDescription(query.trim())
            .map(ExerciseWithTagsEntity::asModel)
    }

}
