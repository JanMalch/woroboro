package io.github.janmalch.woroboro.data

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
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
    fun toLocalTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it) }
    }

    @TypeConverter
    fun fromLocalTime(date: LocalTime?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun toUuid(value: String?): UUID? {
        return value?.let(UUID::fromString)
    }

    @TypeConverter
    fun fromUuid(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun toDuration(value: String?): Duration? {
        return value?.let(Duration.Companion::parseIsoString)
    }

    @TypeConverter
    fun fromDuration(value: Duration?): String? {
        return value?.toIsoString()
    }


    @TypeConverter
    fun toInstant(value: Long?): Instant? {
        return value?.let(Instant::ofEpochMilli)
    }

    @TypeConverter
    fun fromInstant(value: Instant?): Long? {
        return value?.toEpochMilli()
    }
}
