package io.github.janmalch.woroboro.data.model

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import io.github.janmalch.woroboro.models.Exercise
import java.util.UUID

@Entity(tableName = "exercise")
data class ExerciseEntity(
    @PrimaryKey
    val id: UUID,
    val name: String,
    val description: String,
)

@Fts4(contentEntity = ExerciseEntity::class)
@Entity(tableName = "exercise_fts")
data class ExerciseFtsEntity(
    val id: UUID,
    val name: String,
    val description: String,
)

fun ExerciseEntity.asModel() = Exercise(
    id = id,
    name = name,
    description = description,
)
