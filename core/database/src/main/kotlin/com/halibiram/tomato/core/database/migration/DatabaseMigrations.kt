package com.halibiram.tomato.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {

    // Example Migration:
    // val MIGRATION_1_2 = object : Migration(1, 2) {
    // override fun migrate(database: SupportSQLiteDatabase) {
    //        // Example: Adding a new column to an existing table
    // database.execSQL("ALTER TABLE movies ADD COLUMN director TEXT")
    //
    //        // Example: Creating a new table
    // database.execSQL("""
    // CREATE TABLE IF NOT EXISTS `actors` (
    // `actorId` TEXT NOT NULL,
    // `name` TEXT,
    // `movieId` TEXT,
    // PRIMARY KEY(`actorId`),
    // FOREIGN KEY(`movieId`) REFERENCES `movies`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
    // )
    //        """.trimIndent())
    //    }
    // }

    // Add all your migrations to this list
    // val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2 /*, MIGRATION_2_3, ... */)

    // If you have no migrations yet, you can keep this empty or remove ALL_MIGRATIONS from DatabaseModule
     val ALL_MIGRATIONS = emptyArray<Migration>()
}
