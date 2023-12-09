package io.github.janmalch.woroboro.data

import androidx.room.TypeConverter
import java.time.DayOfWeek

class DomainConverters {
    @TypeConverter
    fun toSetOfDayOfWeek(value: String): Set<DayOfWeek> {
        return value.split(',').mapTo(mutableSetOf(), DayOfWeek::valueOf)
    }

    @TypeConverter
    fun fromSetOfDayOfWeek(value: Set<DayOfWeek>): String {
        return value.joinToString(",", transform = DayOfWeek::name)
    }

}
