package io.github.janmalch.woroboro.business

import io.github.janmalch.woroboro.data.dao.RoutineDao
import io.github.janmalch.woroboro.data.model.asModel
import io.github.janmalch.woroboro.models.Routine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface RoutineRepository {
    /**
     * Finds all routines matched by the given [tags].
     * Returns all routines when [tags] is empty.
     *
     * Also filters the list to only include favorites, if [onlyFavorites] is set to `true`.
     * Otherwise returns both favorites and non-favorites.
     */
    fun findAll(tags: List<String>, onlyFavorites: Boolean): Flow<List<Routine>>
}

class RoutineRepositoryImpl @Inject constructor(
    private val routineDao: RoutineDao
) : RoutineRepository {
    override fun findAll(tags: List<String>, onlyFavorites: Boolean): Flow<List<Routine>> {
        fun Routine.matchesAnyTag(): Boolean =
            tags.isEmpty() || this.tags.any { rTag -> rTag.label in tags }
        return routineDao.findAll(onlyFavorites)
            .map { list ->
                list.mapNotNull {
                    it.asModel().takeIf(Routine::matchesAnyTag)
                }
            }
    }
}
