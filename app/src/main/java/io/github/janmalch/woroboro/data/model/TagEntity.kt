package io.github.janmalch.woroboro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.janmalch.woroboro.models.Tag

@Entity(tableName = "tag")
data class TagEntity(
    /**
     * The actual label of the tag, e.g. `"Shoulders"`.
     */
    @PrimaryKey
    val label: String,
    /**
     * The type of the tag, e.g. `"Body part"`.
     */
    val type: String,
)


fun TagEntity.asModel() = Tag(
    label = label,
    type = type,
)

