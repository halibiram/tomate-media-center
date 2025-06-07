package com.halibiram.tomato.core.database.di

import android.content.Context
import androidx.room.Room
import com.halibiram.tomato.core.database.TomatoDatabase
import com.halibiram.tomato.core.database.dao.BookmarkDao
import com.halibiram.tomato.core.database.dao.DownloadDao
import com.halibiram.tomato.core.database.dao.MovieDao
import com.halibiram.tomato.core.database.dao.SeriesDao
// import com.halibiram.tomato.core.database.migration.DatabaseMigrations // Assuming you have this

// Placeholder for Dagger/Hilt module
object DatabaseModule {

    // @Provides
    // @Singleton
    fun provideTomatoDatabase(
        // @ApplicationContext
        context: Context
    ): TomatoDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            TomatoDatabase::class.java,
            "tomato_database"
        )
        // .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS) // Add migrations if any
        .fallbackToDestructiveMigration() // Placeholder: Use proper migrations in production
        .build()
    }

    // @Provides
    fun provideMovieDao(database: TomatoDatabase): MovieDao {
        return database.movieDao()
    }

    // @Provides
    fun provideSeriesDao(database: TomatoDatabase): SeriesDao {
        return database.seriesDao()
    }

    // @Provides
    fun provideDownloadDao(database: TomatoDatabase): DownloadDao {
        return database.downloadDao()
    }

    // @Provides
    fun provideBookmarkDao(database: TomatoDatabase): BookmarkDao {
        return database.bookmarkDao()
    }
}
