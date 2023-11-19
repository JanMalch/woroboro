package io.github.janmalch.woroboro.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.util.UUID

@Entity(
    tableName = "exercise_tag_cross_ref",
    primaryKeys = ["exercise_id", "tag_label"]
)
data class ExerciseTagCrossRefEntity(
    @ColumnInfo(name = "exercise_id")
    val exerciseId: UUID,
    @ColumnInfo(name = "tag_label", index = true)
    val tagLabel: String,
)
