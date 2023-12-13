package io.github.janmalch.woroboro.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.janmalch.woroboro.data.dao.ExerciseDao
import io.github.janmalch.woroboro.data.dao.ReminderDao
import io.github.janmalch.woroboro.data.dao.RoutineDao
import io.github.janmalch.woroboro.data.dao.TagDao
import io.github.janmalch.woroboro.data.model.ExerciseEntity
import io.github.janmalch.woroboro.data.model.ExerciseFtsEntity
import io.github.janmalch.woroboro.data.model.ExerciseTagCrossRefEntity
import io.github.janmalch.woroboro.data.model.MediaEntity
import io.github.janmalch.woroboro.data.model.ReminderEntity
import io.github.janmalch.woroboro.data.model.ReminderFilterTagCrossRefEntity
import io.github.janmalch.woroboro.data.model.RoutineEntity
import io.github.janmalch.woroboro.data.model.RoutineStepEntity
import io.github.janmalch.woroboro.data.model.TagEntity

@Database(
    entities = [
        ExerciseEntity::class,
        ExerciseFtsEntity::class,
        MediaEntity::class,
        TagEntity::class,
        ExerciseTagCrossRefEntity::class,
        RoutineEntity::class,
        RoutineStepEntity::class,
        ReminderEntity::class,
        ReminderFilterTagCrossRefEntity::class,
    ],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
    ]
)
@TypeConverters(StandardConverters::class, DomainConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun tagDao(): TagDao
    abstract fun routineDao(): RoutineDao
    abstract fun reminderDao(): ReminderDao

}
