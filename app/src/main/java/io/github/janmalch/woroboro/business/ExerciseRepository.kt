package io.github.janmalch.woroboro.business

import io.github.janmalch.woroboro.data.dao.ExerciseDao
import io.github.janmalch.woroboro.data.model.ExerciseWithTagsEntity
import io.github.janmalch.woroboro.data.model.asModel
import io.github.janmalch.woroboro.models.Exercise
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

interface ExerciseRepository {
    suspend fun insert(exercise: Exercise): UUID
    suspend fun update(exercise: Exercise): UUID
    fun find(id: UUID): Flow<Exercise?>
    suspend fun delete(id: UUID)
    suspend fun searchInNameOrDescription(query: String): List<Exercise>
}

class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao,
) : ExerciseRepository {

    override suspend fun insert(exercise: Exercise): UUID {
        val uuid = UUID.randomUUID()
        exerciseDao.upsert(exercise.copy(id = uuid).asEntity())
        return uuid
    }

    override suspend fun update(exercise: Exercise): UUID {
        exerciseDao.upsert(exercise.asEntity())
        return exercise.id
    }

    override suspend fun delete(id: UUID) {
        exerciseDao.delete(id)
    }

    override fun find(id: UUID): Flow<Exercise?> {
        return exerciseDao.find(id).map { it?.asModel() }
    }

    override suspend fun searchInNameOrDescription(query: String): List<Exercise> {
        if (query.isBlank()) return emptyList()
        return exerciseDao.searchInNameOrDescription(query.trim())
            .map(ExerciseWithTagsEntity::asModel)
    }

}
