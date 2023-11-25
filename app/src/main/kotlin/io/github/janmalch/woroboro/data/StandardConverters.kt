package io.github.janmalch.woroboro.data

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.util.UUID
import kotlin.time.Duration

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
    fun fromUuid(value: String?): UUID? {
        return value?.let(UUID::fromString)
    }

    @TypeConverter
    fun toUuid(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun fromDuration(value: String?): Duration? {
        return value?.let { Duration.parseIsoString(it) }
    }

    @TypeConverter
    fun toDuration(value: Duration?): String? {
        return value?.toIsoString()
    }
}
