package io.github.janmalch.woroboro.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.janmalch.woroboro.models.ExerciseExecution
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID
import kotlin.time.Duration

@Entity(tableName = "routine")
data class RoutineEntity(
    @PrimaryKey val id: UUID,
    val name: String,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean,
    @ColumnInfo(name = "last_run_duration") val lastRunDuration: Duration?,
    @ColumnInfo(name = "last_run_ended") val lastRunEnded: LocalDateTime?,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)

@Entity(
    tableName = "routine_step",
    primaryKeys = ["routine_id", "sort_index"],
    indices = [Index("id", unique = true)],
    foreignKeys =
        [
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
data class RoutineStepEntity(
    val id: UUID,
    @ColumnInfo(name = "routine_id", index = true) val routineId: UUID,
    @ColumnInfo(name = "sort_index", index = true) val sortIndex: Int,
    @ColumnInfo(name = "exercise_id", index = true) val exerciseId: UUID?,
    @Embedded("custom_") val execution: ExerciseExecution?,
    @ColumnInfo(name = "pause_step") val pauseStep: Duration?,
)
