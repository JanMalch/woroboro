package io.github.janmalch.woroboro.models

import android.os.Parcelable
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

data class Reminder(
    val id: UUID,
    val name: String,
    val weekdays: Set<DayOfWeek>,
    val remindAt: LocalTime,
    val repeat: Repeat?,
    val query: RoutineQuery,
    val isActive: Boolean,
) {

    init {
        require(weekdays.isNotEmpty()) { "Reminder must have at least one weekday." }
        if (repeat != null) {
            require(repeat.until > remindAt) {
                "Repeat until time must be after remind at time, but ${repeat.until} < $remindAt."
            }
        }
    }

    data class Repeat(
        val every: @RawValue Duration,
        val until: LocalTime,
    ) {
        init {
            require(every in VALID_REPEAT_RANGE) {
                "Repeat interval must be within the valid repeat range of $VALID_REPEAT_RANGE, but is $every."
            }
        }

        companion object {
            val VALID_REPEAT_RANGE = 15.minutes..<1.days
        }
    }
}

sealed interface RoutineQuery : Parcelable {

    @Parcelize
    data class Single(
        val routineId: UUID,
    ) : RoutineQuery

    @Parcelize
    data class RoutineFilter(
        val onlyFavorites: Boolean,
        val durationFilter: DurationFilter,
        val selectedTags: List<Tag>,
        val routinesOrder: RoutinesOrder,
    ) : RoutineQuery
}

fun RoutineQuery.asRoutineFilter(): RoutineQuery.RoutineFilter =
    when (this) {
        is RoutineQuery.RoutineFilter -> this
        is RoutineQuery.Single ->
            RoutineQuery.RoutineFilter(
                onlyFavorites = false,
                durationFilter = DurationFilter.Any,
                selectedTags = emptyList(),
                routinesOrder = RoutinesOrder.NameAsc,
            )
    }
