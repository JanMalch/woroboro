package io.github.janmalch.woroboro.business

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.janmalch.woroboro.business.reminders.AndroidReminderNotifications
import io.github.janmalch.woroboro.business.reminders.AndroidReminderScheduler
import io.github.janmalch.woroboro.business.reminders.ReminderNotifications
import io.github.janmalch.woroboro.business.reminders.ReminderScheduler
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
interface BusinessModule {

    @Binds
    @Singleton
    fun bindsExerciseRepository(impl: ExerciseRepositoryImpl): ExerciseRepository

    @Binds
    @Singleton
    fun bindsTagRepository(impl: TagRepositoryImpl): TagRepository

    @Binds
    @Singleton
    fun bindsRoutineRepository(impl: RoutineRepositoryImpl): RoutineRepository

    @Binds
    @Singleton
    fun bindsReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository

    @Binds
    fun bindsReminderScheduler(impl: AndroidReminderScheduler): ReminderScheduler

    @Binds
    fun bindsReminderNotifications(impl: AndroidReminderNotifications): ReminderNotifications

    @Binds
    @Singleton
    fun bindsMediaFileManager(impl: MediaFileManagerImpl): MediaFileManager

    @Binds
    @Singleton
    fun bindsMediaOptimizer(impl: OnDeviceMediaOptimizer): MediaOptimizer

    @Binds
    @Singleton
    fun bindsImportExportManager(impl: ImportExportManagerImpl): ImportExportManager

}