package com.cricut.androidassessment.di

import com.cricut.androidassessment.data.repository.MockQuizRepositoryImpl
import com.cricut.androidassessment.data.repository.QuizRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindQuizRepository(
        mockQuizRepositoryImpl: MockQuizRepositoryImpl
    ): QuizRepository

}
