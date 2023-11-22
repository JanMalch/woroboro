package io.github.janmalch.woroboro.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.janmalch.woroboro.data.dao.ExerciseDao
import io.github.janmalch.woroboro.data.dao.RoutineDao
import io.github.janmalch.woroboro.data.dao.TagDao
import io.github.janmalch.woroboro.data.model.ExerciseEntity
import io.github.janmalch.woroboro.data.model.ExerciseFtsEntity
import io.github.janmalch.woroboro.data.model.ExerciseTagCrossRefEntity
import io.github.janmalch.woroboro.data.model.MediaEntity
import io.github.janmalch.woroboro.data.model.RoutineEntity
import io.github.janmalch.woroboro.data.model.RoutineExerciseCrossRefEntity
import io.github.janmalch.woroboro.data.model.TagEntity

@Database(
    entities = [
        ExerciseEntity::class,
        ExerciseFtsEntity::class,
        MediaEntity::class,
        TagEntity::class,
        ExerciseTagCrossRefEntity::class,
        RoutineEntity::class,
        RoutineExerciseCrossRefEntity::class,
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
@TypeConverters(StandardConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun tagDao(): TagDao
    abstract fun routineDao(): RoutineDao

}
