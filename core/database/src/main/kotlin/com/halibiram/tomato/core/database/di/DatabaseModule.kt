package com.halibiram.tomato.core.database.di

import android.content.Context
import com.halibiram.tomato.core.database.AppDatabase
import com.halibiram.tomato.core.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        // Create a CoroutineScope that can be used for the database callback.
        // This scope should ideally be managed by your application's lifecycle or a dedicated service.
        // For simplicity, a global scope is used here, but consider a more structured approach.
        val applicationScope = CoroutineScope(Dispatchers.Default)
        return AppDatabase.getInstance(context, applicationScope)
    }

    @Provides
    fun provideMovieDao(appDatabase: AppDatabase): MovieDao {
        return appDatabase.movieDao()
    }

    @Provides
    fun provideSeriesDao(appDatabase: AppDatabase): SeriesDao {
        return appDatabase.seriesDao()
    }

    @Provides
    fun provideEpisodeDao(appDatabase: AppDatabase): EpisodeDao {
        return appDatabase.episodeDao()
    }

    @Provides
    fun provideDownloadDao(appDatabase: AppDatabase): DownloadDao {
        return appDatabase.downloadDao()
    }

    @Provides
    fun provideBookmarkDao(appDatabase: AppDatabase): BookmarkDao {
        return appDatabase.bookmarkDao()
    }
}
