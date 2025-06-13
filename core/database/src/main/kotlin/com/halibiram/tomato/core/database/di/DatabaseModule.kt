package com.halibiram.tomato.core.database.di

import android.content.Context
import androidx.room.Room
import com.halibiram.tomato.core.database.TomatoDatabase
import com.halibiram.tomato.core.database.dao.BookmarkDao
import com.halibiram.tomato.core.database.dao.DownloadDao
import com.halibiram.tomato.core.database.dao.MovieDao
import com.halibiram.tomato.core.database.dao.SeriesDao
import com.halibiram.tomato.core.database.migration.DatabaseMigrations // Assuming this exists for migrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTomatoDatabase(
        @ApplicationContext context: Context
    ): TomatoDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            TomatoDatabase::class.java,
            TomatoDatabase.DATABASE_NAME // Use constant from TomatoDatabase
        )
        // Add migrations if any. For now, using fallbackToDestructiveMigration.
        // .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
        .fallbackToDestructiveMigration() // Use proper migrations in production!
        .build()
    }

    @Provides
    @Singleton // DAOs should be singletons if the Database is a singleton
    fun provideMovieDao(database: TomatoDatabase): MovieDao {
        return database.movieDao()
    }

    @Provides
    @Singleton
    fun provideSeriesDao(database: TomatoDatabase): SeriesDao {
        return database.seriesDao()
    }

    @Provides
    @Singleton
    fun provideDownloadDao(database: TomatoDatabase): DownloadDao {
        return database.downloadDao()
    }

    @Provides
    @Singleton
    fun provideBookmarkDao(database: TomatoDatabase): BookmarkDao {
        return database.bookmarkDao()
    }

    @Provides
    @Singleton
    fun provideExtensionDao(database: TomatoDatabase): com.halibiram.tomato.core.database.dao.ExtensionDao {
        return database.extensionDao()
    }

    // No separate EpisodeDao provider as its methods are in SeriesDao
}
