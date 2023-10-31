package io.github.janmalch.woroboro.business

import io.github.janmalch.woroboro.data.dao.ExerciseDao
import io.github.janmalch.woroboro.data.model.ExerciseEntity
import io.github.janmalch.woroboro.data.model.asModel
import io.github.janmalch.woroboro.models.Exercise
import javax.inject.Inject

interface ExerciseRepository {
    suspend fun searchInNameOrDescription(query: String): List<Exercise>
}

class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao
) : ExerciseRepository {

    override suspend fun searchInNameOrDescription(query: String): List<Exercise> {
        if (query.isBlank()) return emptyList()
        return exerciseDao.searchInNameOrDescription(query.trim()).map(ExerciseEntity::asModel)
    }

}
