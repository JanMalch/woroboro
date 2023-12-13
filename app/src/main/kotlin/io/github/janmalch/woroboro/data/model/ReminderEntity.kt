package io.github.janmalch.woroboro.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import io.github.janmalch.woroboro.models.DurationFilter
import io.github.janmalch.woroboro.models.Reminder
import io.github.janmalch.woroboro.models.RoutineFilter
import kotlinx.collections.immutable.toImmutableList
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID
import kotlin.time.Duration

@Entity(tableName = "reminder")
data class ReminderEntity(
    @PrimaryKey
    val id: UUID,
    val name: String,
    val weekdays: Set<DayOfWeek>,
    @ColumnInfo("remind_at")
    val remindAt: LocalTime,
    @ColumnInfo("repeat_every")
    val repeatEvery: Duration?,
    @ColumnInfo("repeat_until")
    val repeatUntil: LocalTime?,
    @ColumnInfo("filter_only_favorites")
    val filterOnlyFavorites: Boolean,
    @ColumnInfo("filter_duration")
    val filterDuration: DurationFilter,
)

@Entity(
    tableName = "reminder_filter_tag",
    primaryKeys = ["reminder_id", "tag_label"],
    foreignKeys = [
        ForeignKey(
            entity = ReminderEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("reminder_id"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = arrayOf("label"),
            childColumns = arrayOf("tag_label"),
            onDelete = ForeignKey.CASCADE
        )
    ],
)
data class ReminderFilterTagCrossRefEntity(
    @ColumnInfo(name = "reminder_id", index = true)
    val reminderId: UUID,
    @ColumnInfo(name = "tag_label", index = true)
    val tagLabel: String,
)

data class ReminderEntityWithFilterTags(
    @Embedded
    val reminder: ReminderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "label",
        associateBy = Junction(
            value = ReminderFilterTagCrossRefEntity::class,
            parentColumn = "reminder_id",
            entityColumn = "tag_label",
        )
    )
    val filterTags: List<TagEntity>,
)

fun ReminderEntityWithFilterTags.asModel() = Reminder(
    id = reminder.id,
    name = reminder.name,
    remindAt = reminder.remindAt,
    weekdays = reminder.weekdays,
    repeat = if (reminder.repeatEvery != null && reminder.repeatUntil != null) {
        Reminder.Repeat(
            every = reminder.repeatEvery,
            until = reminder.repeatUntil,
        )
    } else null,
    filter = RoutineFilter(
        onlyFavorites = reminder.filterOnlyFavorites,
        durationFilter = reminder.filterDuration,
        selectedTags = filterTags.map(TagEntity::asModel).toImmutableList(),
    )
)