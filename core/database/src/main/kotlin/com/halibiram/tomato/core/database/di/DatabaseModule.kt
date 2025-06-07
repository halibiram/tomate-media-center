package com.halibiram.tomato.core.database.di

import android.content.Context
import androidx.room.Room
import com.halibiram.tomato.core.database.AppDatabase // Assuming AppDatabase exists
// import com.halibiram.tomato.core.database.dao.YourDao // Placeholder for your DAO
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext, // Use applicationContext for Room
            AppDatabase::class.java,
            "tomato_app.db" // A more conventional name for SQLite DB file
        )
        // .fallbackToDestructiveMigration() // Add this if you don't want to provide migrations yet
        // .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // Example of adding migrations
        .build()
    }

    // Example of providing a DAO. Replace YourDao with your actual DAO interface
    // and ensure AppDatabase has a method to return it.
    /*
    @Provides
    @Singleton
    fun provideYourDao(db: AppDatabase): YourDao {
        return db.yourDao()
    }
    */

    // Add more DAO providers here as needed
}
