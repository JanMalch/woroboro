package io.github.janmalch.woroboro.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.Relation
import java.util.UUID

@Entity(
    tableName = "exercise_tag_cross_ref",
    primaryKeys = ["exercise_id", "tag_label"]
)
data class ExerciseTagCrossRefEntity(
    @ColumnInfo(name = "exercise_id")
    val exerciseId: UUID,
    @ColumnInfo(name = "tag_label")
    val tagLabel: String,
)

data class ExerciseWithTagsEntity(
    @Embedded val exercise: ExerciseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "label",
        associateBy = Junction(
            value = ExerciseTagCrossRefEntity::class,
            parentColumn = "exercise_id",
            entityColumn = "tag_label",
        )
    )
    val tags: List<TagEntity>
)
