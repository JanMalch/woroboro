package io.github.janmalch.woroboro.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.janmalch.woroboro.data.dao.ExerciseDao
import io.github.janmalch.woroboro.data.dao.ReminderDao
import io.github.janmalch.woroboro.data.dao.RoutineDao
import io.github.janmalch.woroboro.data.dao.TagDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providesAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "app-database",
    ).addMigrations(MIGRATION_3_4)
        .build()

    @Provides
    fun providesExerciseDao(db: AppDatabase): ExerciseDao = db.exerciseDao()

    @Provides
    fun providesTagDao(db: AppDatabase): TagDao = db.tagDao()

    @Provides
    fun providesRoutineDao(db: AppDatabase): RoutineDao = db.routineDao()

    @Provides
    fun providesReminderDao(db: AppDatabase): ReminderDao = db.reminderDao()

}