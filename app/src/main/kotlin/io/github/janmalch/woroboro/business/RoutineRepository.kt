package io.github.janmalch.woroboro.business

import io.github.janmalch.woroboro.data.dao.RoutineDao
import io.github.janmalch.woroboro.data.model.RoutineStepEntity
import io.github.janmalch.woroboro.models.DurationFilter
import io.github.janmalch.woroboro.models.FullRoutine
import io.github.janmalch.woroboro.models.Routine
import io.github.janmalch.woroboro.models.RoutineStep
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

interface RoutineRepository {
    /**
     * Finds all routines matched by the given [tags].
     * Returns all routines when [tags] is empty.
     *
     * Also filters the list to only include favorites, if [onlyFavorites] is set to `true`.
     * Otherwise returns both favorites and non-favorites.
     */
    fun findAll(
        tags: List<String>,
        onlyFavorites: Boolean,
        durationFilter: DurationFilter
    ): Flow<List<Routine>>

    fun findOne(id: UUID): Flow<FullRoutine?>

    /**
     * Updates the routine entity for the given routine.
     * The derived fields [Routine.media], [Routine.tags], and [Routine.exerciseCount] are ignored.
     */
    suspend fun update(routine: Routine)

    suspend fun insert(routine: FullRoutine): UUID
    suspend fun update(routine: FullRoutine): UUID
    suspend fun delete(routineId: UUID)
    suspend fun appendExerciseToRoutine(exerciseId: UUID, routineId: UUID)

}

class RoutineRepositoryImpl @Inject constructor(
    private val routineDao: RoutineDao
) : RoutineRepository {
    override fun findAll(
        tags: List<String>,
        onlyFavorites: Boolean,
        durationFilter: DurationFilter
    ): Flow<List<Routine>> {
        // TODO: improve this, because it reruns query on every tag change ...
        fun Routine.matchesAnyTag(): Boolean =
            tags.isEmpty() || this.tags.any { rTag -> rTag.label in tags }

        fun Routine.isWithinDurationFilter(): Boolean =
            durationFilter == DurationFilter.Any || (lastRunDuration?.let { it in durationFilter.range }
                ?: true)

        return routineDao.findAll(onlyFavorites).map { list ->
            list.asSequence()
                .filter(Routine::matchesAnyTag)
                .filter(Routine::isWithinDurationFilter)
                .toList()
        }
    }

    override fun findOne(id: UUID): Flow<FullRoutine?> {
        return routineDao.findOneFull(id)
    }

    override suspend fun update(routine: Routine) {
        routineDao.update(routine.asEntity())
    }

    override suspend fun insert(routine: FullRoutine): UUID {
        val id = UUID.randomUUID()
        val (routineEntity, stepEntities) = routine.copy(id = id).asEntities()
        routineDao.insert(routineEntity, stepEntities)
        return id
    }

    override suspend fun update(routine: FullRoutine): UUID {
        val (routineEntity, stepEntities) = routine.asEntities()
        routineDao.update(routineEntity, stepEntities)
        return routine.id
    }

    override suspend fun delete(routineId: UUID) {
        routineDao.delete(routineId)
    }

    override suspend fun appendExerciseToRoutine(exerciseId: UUID, routineId: UUID) {
        val routine = checkNotNull(findOne(routineId).firstOrNull()) {
            "Must find routine to append to, but nothing found for $routineId."
        }
        val nextStepIndex = routine.steps.maxOf(RoutineStep::sortIndex) + 1
        val pauseStepEntity = RoutineStepEntity(
            routineId = routineId,
            sortIndex = nextStepIndex,
            exerciseId = null,
            execution = null,
            pauseStep = 1.minutes,
        )
        val exerciseStepEntity = RoutineStepEntity(
            routineId = routineId,
            sortIndex = nextStepIndex + 1,
            exerciseId = exerciseId,
            execution = null,
            pauseStep = null,
        )
        routineDao.insertSteps(listOf(pauseStepEntity, exerciseStepEntity))
    }
}
