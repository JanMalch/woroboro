package io.github.janmalch.woroboro.data

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.util.UUID

class StandardConverters {
    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun fromLocalDateTime(date: LocalDateTime?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun fromUuid(value: String): UUID {
        return UUID.fromString(value)
    }

    @TypeConverter
    fun toUuid(uuid: UUID): String {
        return uuid.toString()
    }
}