package com.halibiram.tomato.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.halibiram.tomato.core.database.converter.TypeConverters as DatabaseTypeConverters // Alias
import com.halibiram.tomato.core.database.dao.*
import com.halibiram.tomato.core.database.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MovieEntity::class,
        SeriesEntity::class,
        EpisodeEntity::class,
        DownloadEntity::class,
        BookmarkEntity::class
    ],
    version = 1, // Start with version 1
    exportSchema = true // Recommended to keep schema history
)
@TypeConverters(DatabaseTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao
    abstract fun seriesDao(): SeriesDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun downloadDao(): DownloadDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tomato_database"
                )
                .addCallback(AppDatabaseCallback(scope)) // Add callback for seeding
                // .fallbackToDestructiveMigration() // Use with caution, prefer proper migrations
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database)
                }
            }
        }

        // Called when the database is opened.
        // override fun onOpen(db: SupportSQLiteDatabase) {
        //     super.onOpen(db)
        //     INSTANCE?.let { database ->
        //         scope.launch(Dispatchers.IO) {
        //             // If you need to do something on every open, like repopulate if empty
        //             // populateDatabase(database)
        //         }
        //     }
        // }

        suspend fun populateDatabase(database: AppDatabase) {
            // Add some initial data for testing if needed
            // This is a good place for pre-populating genres, settings, or dummy data for development
            // Example:
            // val movieDao = database.movieDao()
            // movieDao.insertMovie(MovieEntity(id = "testmovie1", title = "Test Movie 1", overview = "This is a test movie.", posterPath = null, backdropPath = null, releaseDate = "2023-01-01", voteAverage = 7.5, genres = listOf("Action", "Test")))
            // val seriesDao = database.seriesDao()
            // seriesDao.insertSeries(SeriesEntity(id = "testseries1", name = "Test Series 1", overview = "A series for testing.", posterPath = null, backdropPath = null, firstAirDate = "2023-01-01", voteAverage = 8.0, genres = listOf("Drama", "Test"), numberOfSeasons = 1, numberOfEpisodes = 10))
            // Log or indicate that seeding is done if necessary
        }
    }
}
