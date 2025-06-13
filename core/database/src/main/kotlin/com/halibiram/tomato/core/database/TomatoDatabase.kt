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
import com.halibiram.tomato.core.database.entity.*

// TomatoDatabase
@Database(
    entities = [
        MovieEntity::class,
        SeriesEntity::class,
        EpisodeEntity::class,
        DownloadEntity::class,
        BookmarkEntity::class
    ],
    version = 1,
    exportSchema = true // Recommended to set to true and check schema into VCS
)
@TypeConverters(DateConverter::class, ListConverter::class)
abstract class TomatoDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun seriesDao(): SeriesDao
    abstract fun downloadDao(): DownloadDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        const val DATABASE_NAME = "tomato_app.db"
    }
}
