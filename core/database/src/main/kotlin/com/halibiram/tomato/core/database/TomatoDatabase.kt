package com.halibiram.tomato.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.halibiram.tomato.core.database.converter.DateConverter
import com.halibiram.tomato.core.database.converter.ListConverter
import com.halibiram.tomato.core.database.dao.BookmarkDao
import com.halibiram.tomato.core.database.dao.DownloadDao
import com.halibiram.tomato.core.database.dao.MovieDao
import com.halibiram.tomato.core.database.dao.SeriesDao
// No separate EpisodeDao as its methods are in SeriesDao
import com.halibiram.tomato.core.database.entity.BookmarkEntity
import com.halibiram.tomato.core.database.entity.DownloadEntity
import com.halibiram.tomato.core.database.entity.EpisodeEntity
import com.halibiram.tomato.core.database.entity.MovieEntity
import com.halibiram.tomato.core.database.entity.SeriesEntity

@Database(
    entities = [
        MovieEntity::class,
        SeriesEntity::class,
        EpisodeEntity::class,
        DownloadEntity::class,
        BookmarkEntity::class,
        ExtensionEntity::class // Added ExtensionEntity
    ],
    version = 1, // Increment version if schema changes and migrations are needed
    exportSchema = true
)
@TypeConverters(DateConverter::class, ListConverter::class)
abstract class TomatoDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao
    abstract fun seriesDao(): SeriesDao
    abstract fun downloadDao(): DownloadDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun extensionDao(): ExtensionDao // Added ExtensionDao getter

    companion object {
        const val DATABASE_NAME = "tomato_app_database"
    }
}
