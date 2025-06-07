package com.halibiram.tomato.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

// Define a qualifier if you plan to have multiple DataStore instances
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UserPreferencesDataStore

// Define the DataStore instance name at the top level, outside any class or object.
// This is the recommended way by Google.
private const val USER_PREFERENCES_NAME = "tomato_user_prefs"

// This extension creates the DataStore instance.
// The context receiver ensures it's a singleton tied to the application context.
val Context.userPreferences: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    @UserPreferencesDataStore // Use the qualifier if you defined one
    fun provideUserPreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        // It's important to use appContext to ensure it's a singleton.
        return appContext.userPreferences
    }
}
