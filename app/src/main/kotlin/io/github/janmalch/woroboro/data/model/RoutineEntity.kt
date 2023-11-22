package io.github.janmalch.woroboro.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import io.github.janmalch.woroboro.models.Routine
import kotlinx.collections.immutable.toImmutableList
import java.util.UUID
import kotlin.time.Duration

@Entity(tableName = "routine")
data class RoutineEntity(
    @PrimaryKey
    val id: UUID,
    val name: String,
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean,
    @ColumnInfo(name = "last_run")
    val lastRun: Duration?,
)

@Entity(
    tableName = "routine_exercise_cross_ref",
    primaryKeys = ["routine_id", "exercise_id"],
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("routine_id"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("exercise_id"),
            onDelete = ForeignKey.CASCADE
        ),
    ]
)
data class RoutineExerciseCrossRefEntity(
    @ColumnInfo(name = "routine_id", index = true)
    val routineId: UUID,
    @ColumnInfo(name = "exercise_id", index = true)
    val exerciseId: UUID,
)

/*
// TODO: make work if possible, instead of map
data class RoutineEntityWithExercises(
    @Embedded val routine: RoutineEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            RoutineExerciseCrossRefEntity::class,
            parentColumn = "routine_id",
            entityColumn = "exercise_id",
        )
    )
    val exercises: List<ExerciseEntityWithMediaAndTags>
)

fun RoutineEntityWithExercises.asModel() = Routine(
    id = routine.id,
    name = routine.name,
    exercises = exercises.map { it.asModel() }.toImmutableList(),
    isFavorite = routine.isFavorite,
    lastRun = routine.lastRun,
)
*/

fun Map.Entry<RoutineEntity, List<ExerciseEntityWithMediaAndTags>>.asModel() = Routine(
    id = key.id,
    name = key.name,
    exercises = value.map { it.asModel() }.toImmutableList(),
    isFavorite = key.isFavorite,
    lastRun = key.lastRun,
)
