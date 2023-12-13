package io.github.janmalch.woroboro.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Fts4
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import io.github.janmalch.woroboro.models.Exercise
import io.github.janmalch.woroboro.models.ExerciseExecution
import io.github.janmalch.woroboro.models.Media
import kotlinx.collections.immutable.toImmutableList
import java.util.UUID

@Entity(tableName = "exercise")
data class ExerciseEntity(
    @PrimaryKey
    val id: UUID,
    val name: String,
    val description: String,
    @Embedded
    val execution: ExerciseExecution,
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

@Entity(
    tableName = "media",
    foreignKeys = [ForeignKey(
        entity = ExerciseEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("exercise_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class MediaEntity(
    @PrimaryKey
    val id: UUID,
    @ColumnInfo(name = "exercise_id", index = true)
    val exerciseId: UUID,
    val thumbnail: String,
    val source: String,
    @ColumnInfo(name = "is_video")
    val isVideo: Boolean,
)

data class ExerciseEntityWithMediaAndTags(
    @Embedded val exercise: ExerciseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "exercise_id"
    )
    val media: List<MediaEntity>,
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

fun MediaEntity.asModel(): Media = if (isVideo) {
    Media.Video(id = id, source = source, thumbnail = thumbnail)
} else {
    Media.Image(id = id, source = source, thumbnail = thumbnail)
}

fun ExerciseEntityWithMediaAndTags.asModel() = Exercise(
    id = exercise.id,
    name = exercise.name,
    description = exercise.description,
    execution = exercise.execution,
    isFavorite = exercise.isFavorite,
    tags = tags.map(TagEntity::asModel).toImmutableList(),
    media = media.map(MediaEntity::asModel).toImmutableList(),
)
