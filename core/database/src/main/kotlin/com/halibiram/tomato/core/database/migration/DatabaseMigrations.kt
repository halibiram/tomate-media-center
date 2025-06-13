package com.halibiram.tomato.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// DatabaseMigrations
object DatabaseMigrations {
    // Example Migration:
    // val MIGRATION_1_2 = object : Migration(1, 2) {
    //     override fun migrate(database: SupportSQLiteDatabase) {
    //         database.execSQL("ALTER TABLE movies ADD COLUMN new_column TEXT")
    //     }
    // }

    val ALL_MIGRATIONS: Array<Migration> = arrayOf(
        // Add migrations here: MIGRATION_1_2, MIGRATION_2_3
    )
}
