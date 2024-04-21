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
import io.github.janmalch.woroboro.models.RoutineQuery
import io.github.janmalch.woroboro.models.RoutinesOrder
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
    // if filterRoutineId is set, the other values don't matter (see asModel below)
    @ColumnInfo("filter_routine_id")
    val filterRoutineId: UUID?,
    @ColumnInfo("filter_only_favorites")
    val filterOnlyFavorites: Boolean,
    @ColumnInfo("filter_duration")
    val filterDuration: DurationFilter,
    @ColumnInfo("routines_order", defaultValue = "NameAsc")
    val routinesOrder: RoutinesOrder,
    @ColumnInfo("is_active", defaultValue = "1")
    val isActive: Boolean,
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
    isActive = reminder.isActive,
    repeat = if (reminder.repeatEvery != null && reminder.repeatUntil != null) {
        Reminder.Repeat(
            every = reminder.repeatEvery,
            until = reminder.repeatUntil,
        )
    } else null,
    query = if (reminder.filterRoutineId != null) {
        RoutineQuery.Single(reminder.filterRoutineId)
    } else {
        RoutineQuery.RoutineFilter(
            onlyFavorites = reminder.filterOnlyFavorites,
            durationFilter = reminder.filterDuration,
            selectedTags = filterTags.map(TagEntity::asModel).toImmutableList(),
            routinesOrder = reminder.routinesOrder,
        )
    }
)
