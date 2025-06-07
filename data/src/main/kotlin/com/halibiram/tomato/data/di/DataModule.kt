package com.halibiram.tomato.data.di

import com.halibiram.tomato.data.repository.MovieRepositoryImpl
import com.halibiram.tomato.data.repository.SeriesRepositoryImpl
import com.halibiram.tomato.domain.repository.MovieRepository
import com.halibiram.tomato.domain.repository.SeriesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindMovieRepository(
        movieRepositoryImpl: MovieRepositoryImpl
    ): MovieRepository

    @Binds
    @Singleton
    abstract fun bindSeriesRepository(
        seriesRepositoryImpl: SeriesRepositoryImpl
    ): SeriesRepository
}
