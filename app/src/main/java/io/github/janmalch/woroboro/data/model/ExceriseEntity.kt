package io.github.janmalch.woroboro.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import io.github.janmalch.woroboro.models.Exercise
import io.github.janmalch.woroboro.models.Tag
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import java.util.UUID
import kotlin.time.Duration

@Entity(tableName = "exercise")
data class ExerciseEntity(
    @PrimaryKey
    val id: UUID,
    val name: String,
    val description: String,
    val sets: Int,
    val reps: Int?,
    val hold: Duration?,
    val pause: Duration?,
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean,
)

@Fts4(contentEntity = ExerciseEntity::class)
@Entity(tableName = "exercise_fts")
data class ExerciseFtsEntity(
    val id: UUID,
    val name: String,
    val description: String,
)

fun ExerciseWithTagsEntity.asModel() = Exercise(
    id = exercise.id,
    name = exercise.name,
    description = exercise.description,
    sets = exercise.sets,
    reps = exercise.reps,
    hold = exercise.hold,
    pause = exercise.pause,
    isFavorite = exercise.isFavorite,
    tags = tags.map(TagEntity::asModel).toImmutableList(),
)
