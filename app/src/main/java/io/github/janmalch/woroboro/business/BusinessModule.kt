package io.github.janmalch.woroboro.business

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.janmalch.woroboro.data.AppDatabase
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
interface BusinessModule {

    @Binds
    @Singleton
    fun bindsExerciseRepository(impl: ExerciseRepositoryImpl): ExerciseRepository

}