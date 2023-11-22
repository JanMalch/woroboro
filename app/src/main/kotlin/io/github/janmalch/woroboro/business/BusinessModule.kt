package io.github.janmalch.woroboro.business

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
    fun bindsMediaFileManager(impl: MediaFileManagerImpl): MediaFileManager

    @Binds
    @Singleton
    fun bindsMediaOptimizer(impl: OnDeviceMediaOptimizer): MediaOptimizer

}