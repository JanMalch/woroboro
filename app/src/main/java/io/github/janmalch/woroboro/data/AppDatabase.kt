package io.github.janmalch.woroboro.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.janmalch.woroboro.data.dao.ExerciseDao
import io.github.janmalch.woroboro.data.model.ExerciseEntity
import io.github.janmalch.woroboro.data.model.ExerciseFtsEntity

@Database(
    entities = [
        ExerciseEntity::class,
        ExerciseFtsEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(StandardConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao

}
